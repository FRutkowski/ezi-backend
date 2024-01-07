package com.example.ezibackend.controller.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    private Integer finalPrice;
    private LocalDateTime date;
    private Integer clientId;
    private List<Integer> productIds;
}
