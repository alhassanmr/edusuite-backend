package com.hasfatempire.sms.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hasfatempire.sms.dto.FeePaymentRequest;
import com.hasfatempire.sms.exception.BadRequestException;
import com.hasfatempire.sms.exception.ResourceNotFoundException;
import com.hasfatempire.sms.model.FeeInvoice;
import com.hasfatempire.sms.model.PaymentTransaction;
import com.hasfatempire.sms.model.User;
import com.hasfatempire.sms.repository.FeeInvoiceRepository;
import com.hasfatempire.sms.repository.PaymentTransactionRepository;
import com.hasfatempire.sms.repository.UserRepository;
import com.hasfatempire.sms.service.FeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Online fee payments via Paystack.
 * Flow: parent clicks Pay Now -> initialize -> redirect to Paystack checkout
 * (card / MTN MoMo / Telecel Cash / AT Money) -> callback verify -> invoice credited.
 */
@RestController
@RequestMapping("/api/payments/paystack")
@RequiredArgsConstructor
public class PaymentController {

    private final PaystackService paystackService;
    private final PaymentTransactionRepository transactionRepository;
    private final FeeInvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final FeeService feeService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Initialize payment for an invoice. Parents can pay only their own
     * children's invoices; admins can initialize for anyone (e.g. desk payment).
     */
    @PostMapping("/initialize/{invoiceId}")
    public ResponseEntity<Map<String, String>> initialize(@PathVariable Long invoiceId,
                                                           @RequestParam(required = false) BigDecimal amount,
                                                           Authentication auth) {
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        FeeInvoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        // Parent ownership check
        if (user.getRole().name().equals("PARENT")) {
            Long parentId = user.getLinkedParentId();
            if (parentId == null || invoice.getStudent() == null
                    || invoice.getStudent().getParentGuardian() == null
                    || !invoice.getStudent().getParentGuardian().getId().equals(parentId)) {
                throw new ResourceNotFoundException("Invoice not found");
            }
        }

        BigDecimal outstanding = invoice.getAmountDue().subtract(invoice.getAmountPaid());
        if (outstanding.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("This invoice is already fully paid");
        }

        // Pay full outstanding by default; allow partial if amount provided
        BigDecimal payAmount = (amount != null && amount.compareTo(BigDecimal.ZERO) > 0
                && amount.compareTo(outstanding) <= 0) ? amount : outstanding;

        String reference = "EDU-" + invoiceId + "-" + UUID.randomUUID().toString().substring(0, 8);
        String currency = "GHS"; // TODO: make per-school configurable

        Map<String, String> init = paystackService.initialize(user.getEmail(), payAmount, currency, reference);

        transactionRepository.save(PaymentTransaction.builder()
                .reference(reference)
                .invoice(invoice)
                .school(invoice.getSchool())
                .amount(payAmount)
                .currency(currency)
                .status(PaymentTransaction.Status.INITIALIZED)
                .initiatedByUsername(user.getUsername())
                .build());

        return ResponseEntity.ok(init);
    }

    /**
     * Verify after redirect back from Paystack. Idempotent — safe to call twice.
     */
    @GetMapping("/verify/{reference}")
    public ResponseEntity<Map<String, Object>> verify(@PathVariable String reference) {
        PaymentTransaction txn = transactionRepository.findByReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (txn.getStatus() == PaymentTransaction.Status.SUCCESS) {
            return ResponseEntity.ok(Map.of("status", "success", "message", "Payment already confirmed"));
        }

        JsonNode data = paystackService.verify(reference);
        String paystackStatus = data.path("status").asText();

        if ("success".equals(paystackStatus)) {
            creditInvoice(txn, data);
            return ResponseEntity.ok(Map.of("status", "success",
                    "message", "Payment confirmed. Receipt sent.",
                    "amount", txn.getAmount(),
                    "channel", data.path("channel").asText()));
        }

        txn.setStatus(PaymentTransaction.Status.FAILED);
        transactionRepository.save(txn);
        return ResponseEntity.ok(Map.of("status", paystackStatus,
                "message", "Payment not completed (" + paystackStatus + "). You can try again."));
    }

    /**
     * Paystack webhook — server-to-server confirmation (survives closed browser).
     * Configure in Paystack dashboard: https://yourapi.com/api/payments/paystack/webhook
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody String rawBody,
                                         @RequestHeader(value = "x-paystack-signature", required = false) String signature) {
        if (signature == null || !paystackService.isValidWebhookSignature(rawBody, signature)) {
            return ResponseEntity.status(401).build();
        }
        try {
            JsonNode event = objectMapper.readTree(rawBody);
            if ("charge.success".equals(event.path("event").asText())) {
                String reference = event.path("data").path("reference").asText();
                transactionRepository.findByReference(reference).ifPresent(txn -> {
                    if (txn.getStatus() != PaymentTransaction.Status.SUCCESS) {
                        creditInvoice(txn, event.path("data"));
                    }
                });
            }
        } catch (Exception ignored) {
            // Always 200 to prevent Paystack retry storms on parse errors
        }
        return ResponseEntity.ok().build();
    }

    private void creditInvoice(PaymentTransaction txn, JsonNode paystackData) {
        txn.setStatus(PaymentTransaction.Status.SUCCESS);
        txn.setChannel(paystackData.path("channel").asText());
        txn.setVerifiedAt(Instant.now());
        transactionRepository.save(txn);

        // Credit the invoice — reuses FeeService so parent gets SMS/email receipt
        feeService.recordPayment(txn.getInvoice().getId(),
                new FeePaymentRequest(txn.getAmount(),
                        "PAYSTACK_" + paystackData.path("channel").asText().toUpperCase(),
                        txn.getReference()),
                txn.getInitiatedByUsername());
    }
}
