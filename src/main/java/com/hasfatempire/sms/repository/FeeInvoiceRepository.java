package com.hasfatempire.sms.repository;

import com.hasfatempire.sms.model.FeeInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeeInvoiceRepository extends JpaRepository<FeeInvoice, Long> {
    List<FeeInvoice> findByStudentId(Long studentId);
    List<FeeInvoice> findByStatus(FeeInvoice.InvoiceStatus status);
    List<FeeInvoice> findBySchoolId(Long schoolId);
}
