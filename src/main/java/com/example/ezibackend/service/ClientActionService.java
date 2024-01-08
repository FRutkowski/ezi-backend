package com.example.ezibackend.service;

import com.example.ezibackend.controller.dto.ClientActionCreateDTO;
import com.example.ezibackend.model.Client;
import com.example.ezibackend.model.ClientAction;
import com.example.ezibackend.repository.ClientActionRepository;
import com.example.ezibackend.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientActionService {
    private final ClientActionRepository clientActionRepository;
    private final ClientService clientService;

    public List<ClientAction> getAllClientActions() {
        return clientActionRepository.findAll();
    }

    public Optional<ClientAction> getClientActionById(Long id) {
        return clientActionRepository.findById(id);
    }

    public List<ClientAction> getClientActionByClientId(Long id) {
        return clientActionRepository.findByClientIdOrderByDateDesc(id);
    }

    public ClientAction createClientAction(ClientActionCreateDTO clientActionDTO) {
        Client client = clientService.getClientById(Long.valueOf(clientActionDTO.getClientId())).orElseThrow(() ->
                new NotFoundException(Client.class, Long.valueOf(clientActionDTO.getClientId())));

        System.out.println(clientActionDTO.getObjectId());
        ClientAction clientAction = new ClientAction();
        clientAction.setClient(client);
        clientAction.setType(clientActionDTO.getType());
        clientAction.setDate(LocalDateTime.now());
        clientAction.setObjectId(Long.valueOf(clientActionDTO.getObjectId()));
        return clientActionRepository.save(clientAction);
    }
}
