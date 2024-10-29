package com.example.demo.dto;

import lombok.Getter;

@Getter
public class CustomerDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phone;

    public CustomerDTO(Long id, String fullName, String email, String phone) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
    }
}
