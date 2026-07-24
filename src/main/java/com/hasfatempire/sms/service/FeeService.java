package com.hasfatempire.sms.service;

import com.hasfatempire.sms.dto.FeePaymentRequest;
import com.hasfatempire.sms.exception.BadRequestException;
import com.hasfatempire.sms.exception.ResourceNotFoundException;
import com.hasfatempire.sms.model.FeeInvoice;
import com.hasfatempire.sms.model.FeePayment;
import com.hasfatempire.sms.model.ParentGuardian;
import com.hasfatempire.sms.model.Student;
import com.hasfatempire.sms.notification.NotificationService;
import com.hasfatempire.sms.repository.FeeInvoiceRepository;
import com.hasfatempire.sms.repository.FeePaymentRepository;
import com.hasfatempire.sms.repository.StudentRepository;
import com.hasfatempire.sms.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeeService {

    private final FeeInvoiceRepository invoiceRepository;
    private final FeePaymentRepository paymentRepository;
    private final StudentRepository studentRepository;
    private final NotificationService notificationService;
    private final TenantContext tenantContext;

    public List<FeeInvoice> findAll(Authentication auth) {
        return invoiceRepository.findBySchoolId(tenantContext.getCurrentSchoolId(auth));
    }

    public FeeInvoice findById(Long id, Authentication auth) {
        Long schoolId = tenantContext.getCurrentSchoolId(auth);
        FeeInvoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));
        if (invoice.getSchool() == null || !invoice.getSchool().getId().equals(schoolId)) {
            throw new ResourceNotFoundException("Invoice not found: " + id);
        }
        return invoice;
    }

    /** Internal lookup without tenant check — used by payment webhook flow. */
    public FeeInvoice findByIdInternal(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));
    }

    public List<FeeInvoice> byStudent(Long studentId, Authentication auth) {
        Long schoolId = tenantContext.getCurrentSchoolId(auth);
        return invoiceRepository.findByStudentId(studentId).stream()
                .filter(inv -> inv.getSchool() != null && inv.getSchool().getId().equals(schoolId))
                .toList();
    }

    public FeeInvoice createInvoice(FeeInvoice invoice, Authentication auth) {
        Long schoolId = tenantContext.getCurrentSchoolId(auth);
        // Verify student belongs to this school; attach full student for notifications
        Student student = studentRepository.findById(invoice.getStudent().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        if (student.getSchool() == null || !student.getSchool().getId().equals(schoolId)) {
            throw new ResourceNotFoundException("Student not found");
        }
        invoice.setStudent(student);
        invoice.setSchool(student.getSchool());
        invoice.setAmountPaid(BigDecimal.ZERO);
        invoice.setStatus(FeeInvoice.InvoiceStatus.UNPAID);
        FeeInvoice saved = invoiceRepository.save(invoice);
        notifyParentInvoiceCreated(saved);
        return saved;
    }

    private void notifyParentInvoiceCreated(FeeInvoice invoice) {
        Student student = invoice.getStudent();
        if (student == null || student.getParentGuardian() == null) return;
        ParentGuardian parent = student.getParentGuardian();
        String message = String.format(
                "%s: New fee invoice for %s %s — %s %s due %s. Amount: GHS %s. Please arrange payment. Thank you.",
                invoice.getSchool() != null ? invoice.getSchool().getName() : "School",
                student.getFirstName(), student.getLastName(),
                invoice.getTerm() != null ? invoice.getTerm() : "",
                invoice.getAcademicYear() != null ? invoice.getAcademicYear() : "",
                invoice.getDueDate() != null ? invoice.getDueDate().toString() : "soon",
                invoice.getAmountDue());
        notificationService.notifyBoth(invoice.getSchool(), parent.getPhone(), parent.getEmail(),
                "New fee invoice — " + student.getFirstName() + " " + student.getLastName(), message);
    }

    /**
     * Record a payment. Used by both the admin desk flow and the Paystack
     * verification/webhook flow (which has already validated the invoice).
     */
    public FeeInvoice recordPayment(Long invoiceId, FeePaymentRequest request, String receivedBy) {
        FeeInvoice invoice = findByIdInternal(invoiceId);

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
        FeeInvoice updated = invoiceRepository.save(invoice);
        notifyParentPaymentReceived(updated, request.amount());
        return updated;
    }

    /** Admin desk payment — tenant-checked wrapper. */
    public FeeInvoice recordPaymentAsAdmin(Long invoiceId, FeePaymentRequest request, Authentication auth) {
        findById(invoiceId, auth); // ownership check
        return recordPayment(invoiceId, request, auth.getName());
    }

    private void notifyParentPaymentReceived(FeeInvoice invoice, BigDecimal amount) {
        Student student = invoice.getStudent();
        if (student == null || student.getParentGuardian() == null) return;
        ParentGuardian parent = student.getParentGuardian();
        BigDecimal balance = invoice.getAmountDue().subtract(invoice.getAmountPaid());
        String message = String.format(
                "%s: Payment of GHS %s received for %s %s. Balance: GHS %s. Status: %s. Thank you!",
                invoice.getSchool() != null ? invoice.getSchool().getName() : "School",
                amount, student.getFirstName(), student.getLastName(),
                balance.max(BigDecimal.ZERO), invoice.getStatus());
        notificationService.notifyBoth(invoice.getSchool(), parent.getPhone(), parent.getEmail(),
                "Payment received — " + student.getFirstName() + " " + student.getLastName(), message);
    }

    public BigDecimal totalCollected(Long schoolId) {
        return invoiceRepository.findBySchoolId(schoolId).stream()
                .map(FeeInvoice::getAmountPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal totalOutstanding(Long schoolId) {
        return invoiceRepository.findBySchoolId(schoolId).stream()
                .map(inv -> inv.getAmountDue().subtract(inv.getAmountPaid()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
