package com.hasfatempire.sms.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.hasfatempire.sms.exception.BadRequestException;
import com.hasfatempire.sms.model.School;
import com.hasfatempire.sms.repository.SchoolRepository;
import com.hasfatempire.sms.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Settlement setup — the school admin enters THEIR bank/MoMo details once.
 * We create a Paystack subaccount so every parent payment is split
 * automatically: school's share goes DIRECTLY to the school's account,
 * the platform keeps only its small commission. EduSuite never holds
 * school money.
 */
@RestController
@RequestMapping("/api/school/settlement")
@RequiredArgsConstructor
public class SettlementController {

    private final PaystackService paystackService;
    private final SchoolRepository schoolRepository;
    private final TenantContext tenantContext;

    @Value("${app.payments.platform-fee-percent:1.0}")
    private double defaultPlatformFeePercent;

    /** Banks + mobile money providers available for settlement (Ghana = GHS). */
    @GetMapping("/banks")
    @PreAuthorize("hasRole('ADMIN')")
    public JsonNode banks(@RequestParam(defaultValue = "GHS") String currency) {
        return paystackService.listBanks(currency);
    }

    /** Current settlement status for my school. */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> status(Authentication auth) {
        School school = tenantContext.getCurrentSchool(auth);
        boolean configured = school.getPaystackSubaccountCode() != null;
        return Map.of(
                "configured", configured,
                "bankName", school.getSettlementBankName() == null ? "" : school.getSettlementBankName(),
                "accountNumber", maskAccount(school.getSettlementAccountNumber()),
                "accountName", school.getSettlementAccountName() == null ? "" : school.getSettlementAccountName(),
                "platformFeePercent", school.getPlatformFeePercent() == null ? defaultPlatformFeePercent : school.getPlatformFeePercent()
        );
    }

    public record SettlementRequest(String bankCode, String bankName,
                                     String accountNumber, String accountName) {}

    /**
     * Configure (or update) the school's payout account.
     * Creates the Paystack subaccount for automatic split payments.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> configure(@RequestBody SettlementRequest request,
                                                          Authentication auth) {
        if (request.bankCode() == null || request.bankCode().isBlank()
                || request.accountNumber() == null || request.accountNumber().isBlank()) {
            throw new BadRequestException("Bank and account number are required");
        }

        School school = tenantContext.getCurrentSchool(auth);
        double feePercent = school.getPlatformFeePercent() != null
                ? school.getPlatformFeePercent() : defaultPlatformFeePercent;

        String subaccountCode = paystackService.createSubaccount(
                school.getName(), request.bankCode(), request.accountNumber(), feePercent);

        school.setPaystackSubaccountCode(subaccountCode);
        school.setSettlementBankCode(request.bankCode());
        school.setSettlementBankName(request.bankName());
        school.setSettlementAccountNumber(request.accountNumber());
        school.setSettlementAccountName(request.accountName());
        school.setPlatformFeePercent(feePercent);
        schoolRepository.save(school);

        return ResponseEntity.ok(Map.of(
                "configured", true,
                "message", "Settlement account saved. Parent payments will now be deposited directly to "
                        + request.bankName() + " (" + maskAccount(request.accountNumber()) + ").",
                "platformFeePercent", feePercent
        ));
    }

    private String maskAccount(String account) {
        if (account == null || account.length() < 4) return "";
        return "****" + account.substring(account.length() - 4);
    }
}
