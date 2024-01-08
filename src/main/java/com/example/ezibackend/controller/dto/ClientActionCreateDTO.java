package com.example.ezibackend.controller.dto;


import com.example.ezibackend.model.ClientAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientActionCreateDTO {

    private Integer clientId;
    private Integer objectId;
    private ClientAction.Type type;
}
