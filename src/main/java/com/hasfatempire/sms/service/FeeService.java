package com.hasfatempire.sms.service;

import com.hasfatempire.sms.dto.FeePaymentRequest;
import com.hasfatempire.sms.exception.BadRequestException;
import com.hasfatempire.sms.exception.ResourceNotFoundException;
import com.hasfatempire.sms.model.FeeInvoice;
import com.hasfatempire.sms.model.FeePayment;
import com.hasfatempire.sms.repository.FeeInvoiceRepository;
import com.hasfatempire.sms.repository.FeePaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeeService {

    private final FeeInvoiceRepository invoiceRepository;
    private final FeePaymentRepository paymentRepository;

    public List<FeeInvoice> findAll() { return invoiceRepository.findAll(); }

    public FeeInvoice findById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));
    }

    public List<FeeInvoice> byStudent(Long studentId) {
        return invoiceRepository.findByStudentId(studentId);
    }

    public FeeInvoice createInvoice(FeeInvoice invoice) {
        invoice.setAmountPaid(BigDecimal.ZERO);
        invoice.setStatus(FeeInvoice.InvoiceStatus.UNPAID);
        return invoiceRepository.save(invoice);
    }

    public FeeInvoice recordPayment(Long invoiceId, FeePaymentRequest request, String receivedBy) {
        FeeInvoice invoice = findById(invoiceId);

        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Payment amount must be greater than zero");
        }

        FeePayment payment = FeePayment.builder()
                .invoice(invoice)
                .amount(request.amount())
                .paymentDate(LocalDate.now())
                .method(request.method())
                .reference(request.reference())
                .receivedBy(receivedBy)
                .build();
        paymentRepository.save(payment);

        invoice.setAmountPaid(invoice.getAmountPaid().add(request.amount()));
        if (invoice.getAmountPaid().compareTo(invoice.getAmountDue()) >= 0) {
            invoice.setStatus(FeeInvoice.InvoiceStatus.PAID);
        } else {
            invoice.setStatus(FeeInvoice.InvoiceStatus.PARTIALLY_PAID);
        }
        return invoiceRepository.save(invoice);
    }

    public BigDecimal totalCollected() {
        return invoiceRepository.findAll().stream()
                .map(FeeInvoice::getAmountPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal totalOutstanding() {
        return invoiceRepository.findAll().stream()
                .map(inv -> inv.getAmountDue().subtract(inv.getAmountPaid()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
