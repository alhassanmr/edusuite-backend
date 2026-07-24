package com.hasfatempire.sms.controller;

import com.hasfatempire.sms.dto.FeePaymentRequest;
import com.hasfatempire.sms.model.FeeInvoice;
import com.hasfatempire.sms.service.FeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fees")
@RequiredArgsConstructor
public class FeeController {

    private final FeeService feeService;

    @GetMapping("/invoices")
    public List<FeeInvoice> all() { return feeService.findAll(); }

    @GetMapping("/invoices/{id}")
    public FeeInvoice byId(@PathVariable Long id) { return feeService.findById(id); }

    @GetMapping("/invoices/student/{studentId}")
    public List<FeeInvoice> byStudent(@PathVariable Long studentId) { return feeService.byStudent(studentId); }

    @PostMapping("/invoices")
    public ResponseEntity<FeeInvoice> createInvoice(@Valid @RequestBody FeeInvoice invoice) {
        return ResponseEntity.ok(feeService.createInvoice(invoice));
    }

    @PostMapping("/invoices/{id}/payments")
    public FeeInvoice pay(@PathVariable Long id, @RequestBody FeePaymentRequest request, Authentication authentication) {
        return feeService.recordPayment(id, request, authentication.getName());
    }
}
