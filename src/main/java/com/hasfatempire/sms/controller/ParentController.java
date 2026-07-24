package com.hasfatempire.sms.controller;

import com.hasfatempire.sms.model.ParentGuardian;
import com.hasfatempire.sms.service.ParentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parents")
@RequiredArgsConstructor
public class ParentController {

    private final ParentService parentService;

    @GetMapping
    public List<ParentGuardian> all(Authentication auth) { return parentService.findAll(auth); }

    @GetMapping("/{id}")
    public ParentGuardian byId(@PathVariable Long id, Authentication auth) { return parentService.findById(id, auth); }

    @GetMapping("/{id}/children")
    public Object children(@PathVariable Long id, Authentication auth) { return parentService.children(id, auth); }

    @PostMapping
    public ResponseEntity<ParentGuardian> create(@Valid @RequestBody ParentGuardian parent, Authentication auth) {
        return ResponseEntity.ok(parentService.create(parent, auth));
    }

    @PutMapping("/{id}")
    public ParentGuardian update(@PathVariable Long id, @Valid @RequestBody ParentGuardian parent, Authentication auth) {
        return parentService.update(id, parent, auth);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        parentService.delete(id, auth);
        return ResponseEntity.noContent().build();
    }
}
