package com.myapp.rh.employee.dto;

import com.myapp.rh.employee.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EmployeeRequestDTO {

    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotNull
    private Role role;

    private String department;

    @NotNull
    private BigDecimal salary;

}
