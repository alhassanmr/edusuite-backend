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
    public List<FeeInvoice> all(Authentication auth) { return feeService.findAll(auth); }

    @GetMapping("/invoices/{id}")
    public FeeInvoice byId(@PathVariable Long id, Authentication auth) { return feeService.findById(id, auth); }

    @GetMapping("/invoices/student/{studentId}")
    public List<FeeInvoice> byStudent(@PathVariable Long studentId, Authentication auth) {
        return feeService.byStudent(studentId, auth);
    }

    @PostMapping("/invoices")
    public ResponseEntity<FeeInvoice> createInvoice(@Valid @RequestBody FeeInvoice invoice, Authentication auth) {
        return ResponseEntity.ok(feeService.createInvoice(invoice, auth));
    }

    @PostMapping("/invoices/{id}/payments")
    public FeeInvoice pay(@PathVariable Long id, @RequestBody FeePaymentRequest request, Authentication auth) {
        return feeService.recordPaymentAsAdmin(id, request, auth);
    }
}
