package com.myapp.rh.auth.service;

import com.myapp.rh.auth.dto.LoginRequestDTO;
import com.myapp.rh.auth.dto.LoginResponseDTO;
import com.myapp.rh.auth.security.JwtService;
import com.myapp.rh.employee.entity.Employee;
import com.myapp.rh.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final EmployeeRepository employeeRepository;
    private final JwtService jwtService;

    public LoginResponseDTO login(LoginRequestDTO dto) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            dto.getEmail(),
                            dto.getPassword()
                    )
            );

        } catch (Exception e) {
            throw new RuntimeException("Invalid email or password");
        }

        Employee employee = employeeRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtService.generateToken(employee);

        return LoginResponseDTO.builder()
                .token(token)
                .type("Bearer")
                .build();
    }
}
