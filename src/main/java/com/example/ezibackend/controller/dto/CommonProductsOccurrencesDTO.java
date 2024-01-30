package com.example.ezibackend.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonProductsOccurrencesDTO {
    private Long productId;
    private int amountOfCommonProducts;
    private int amountOfOccurrences;
}
