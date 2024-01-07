package com.example.ezibackend.controller;

import com.example.ezibackend.model.ClientAction;
import com.example.ezibackend.service.ClientActionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/client-actions")
@RequiredArgsConstructor
public class ClientActionController {
    private final ClientActionService clientActionService;

    @GetMapping
    public List<ClientAction> getAllClientActions() {
        return clientActionService.getAllClientActions();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientAction> getClientActionById(@PathVariable Long id) {
        Optional<ClientAction> clientAction = clientActionService.getClientActionById(id);
        return clientAction.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
