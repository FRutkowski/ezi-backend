package com.example.ezibackend.repository;

import com.example.ezibackend.model.ClientAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClientActionRepository extends JpaRepository<ClientAction, Long> {
    List<ClientAction> findByClientIdOrderByDateDesc(Long id);

    List<ClientAction> getClientActionByClientIdAndTypeAndDateAfterAndObjectIdIn(Long clientId, ClientAction.Type type, LocalDateTime localDateTime, List<Long> sameCategoryProductIds);
}
