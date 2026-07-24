package com.hasfatempire.sms.repository;

import com.hasfatempire.sms.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByReference(String reference);
    List<PaymentTransaction> findByInvoiceId(Long invoiceId);
}
