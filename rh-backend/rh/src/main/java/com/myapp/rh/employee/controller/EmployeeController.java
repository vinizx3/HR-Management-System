package com.myapp.rh.employee.controller;

import com.myapp.rh.employee.dto.EmployeeRequestDTO;
import com.myapp.rh.employee.dto.EmployeeResponseDTO;
import com.myapp.rh.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Tag(name = "Employees", description = "Employee management — HR_MANAGER only for write operations")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Operation(summary = "Create employee", description = "Register a new employee — HR_MANAGER only")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Employee created"),
            @ApiResponse(responseCode = "400", description = "Email already registered")
    })
    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> create(
            @RequestBody @Valid EmployeeRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(employeeService.create(dto));
    }

    @Operation(summary = "List all employees", description = "Returns only active employees — HR_MANAGER only")
    @ApiResponse(responseCode = "200", description = "List returned successfully")
    @GetMapping
    public ResponseEntity<List<EmployeeResponseDTO>> findAll() {
        return ResponseEntity.ok(employeeService.findAll());
    }

    @Operation(summary = "Get my profile", description = "Returns authenticated employee data")
    @ApiResponse(responseCode = "200", description = "Profile returned successfully")
    @GetMapping("/me")
    public ResponseEntity<EmployeeResponseDTO> findMe(
            Authentication authentication) {
        return ResponseEntity.ok(
                employeeService.findMe(authentication.getName()));
    }

    @Operation(summary = "Get employee by ID", description = "HR_MANAGER only")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee found"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> findById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(employeeService.findById(id));
    }

    @Operation(summary = "Update employee", description = "HR_MANAGER only")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee updated"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> update(
            @PathVariable UUID id,
            @RequestBody @Valid EmployeeRequestDTO dto) {
        return ResponseEntity.ok(employeeService.update(id, dto));
    }

    @Operation(summary = "Deactivate employee", description = "Soft delete — HR_MANAGER only")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Employee deactivated"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
