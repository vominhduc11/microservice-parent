package com.devwonder.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admins")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin {
    
    @Id
    @Column(name = "account_id")
    private Long accountId;
    
    private String name;
    
    private String email;
    
    private String phone;
    
    @Column(name = "company_name")
    private String companyName;
}