package com.myapp.rh.employee.dto;

import com.myapp.rh.employee.entity.Role;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class EmployeeResponseDTO {

    private UUID id;
    private String name;
    private String email;
    private Role role;
    private String department;
    private BigDecimal salary;
    private boolean active;
}
