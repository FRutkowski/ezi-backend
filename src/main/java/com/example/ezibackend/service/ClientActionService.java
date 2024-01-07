package com.example.ezibackend.service;

import com.example.ezibackend.model.ClientAction;
import com.example.ezibackend.repository.ClientActionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientActionService {
    private final ClientActionRepository clientActionRepository;

    public List<ClientAction> getAllClientActions() {
        return clientActionRepository.findAll();
    }

    public Optional<ClientAction> getClientActionById(Long id) {
        return clientActionRepository.findById(id);
    }

    public List<ClientAction> getClientActionByClientId(Long id) {
        return clientActionRepository.findByClientIdOrderByDateDesc(id);
    }
}
