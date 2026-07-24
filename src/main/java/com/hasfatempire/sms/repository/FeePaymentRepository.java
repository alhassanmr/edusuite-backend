package com.hasfatempire.sms.repository;

import com.hasfatempire.sms.model.FeePayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeePaymentRepository extends JpaRepository<FeePayment, Long> {
    List<FeePayment> findByInvoiceId(Long invoiceId);
}
