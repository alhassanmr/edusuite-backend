package com.hasfatempire.sms.controller;

import com.hasfatempire.sms.model.Notice;
import com.hasfatempire.sms.service.NoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping
    public List<Notice> all() { return noticeService.findAll(); }

    @PostMapping
    public ResponseEntity<Notice> create(@Valid @RequestBody Notice notice, Authentication authentication) {
        return ResponseEntity.ok(noticeService.create(notice, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        noticeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
