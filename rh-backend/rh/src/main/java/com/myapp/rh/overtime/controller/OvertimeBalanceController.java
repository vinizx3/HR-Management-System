package com.myapp.rh.overtime.controller;

import com.myapp.rh.overtime.dto.OvertimeBalanceResponseDTO;
import com.myapp.rh.overtime.dto.OvertimeCompensationRequestDTO;
import com.myapp.rh.overtime.service.OvertimeBalanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/overtime")
@RequiredArgsConstructor
@Tag(name = "Overtime Balance", description = "Overtime balance management")
public class OvertimeBalanceController {

    private final OvertimeBalanceService overtimeBalanceService;

    @Operation(summary = "My overtime balance", description = "Returns authenticated employee overtime balance")
    @ApiResponse(responseCode = "200", description = "Balance returned successfully")
    @GetMapping("/me")
    public ResponseEntity<OvertimeBalanceResponseDTO> getMyBalance(
            Authentication authentication) {
        return ResponseEntity.ok(
                overtimeBalanceService.getMyBalance(authentication.getName()));
    }

    @Operation(summary = "Employee overtime balance", description = "Returns overtime balance for specific employee — HR_MANAGER only")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Balance returned successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @GetMapping("/{employeeId}")
    public ResponseEntity<OvertimeBalanceResponseDTO> getEmployeeBalance(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(
                overtimeBalanceService.getBalanceByEmployeeId(employeeId));
    }

    @Operation(summary = "Compensate overtime", description = "Use overtime balance to compensate absence")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compensation applied"),
            @ApiResponse(responseCode = "400", description = "Insufficient overtime balance")
    })
    @PostMapping("/compensate")
    public ResponseEntity<OvertimeBalanceResponseDTO> compensate(
            @RequestBody @Valid OvertimeCompensationRequestDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(
                overtimeBalanceService.compensate(authentication.getName(), dto));
    }
}
