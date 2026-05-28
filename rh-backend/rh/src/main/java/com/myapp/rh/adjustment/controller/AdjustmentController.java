package com.myapp.rh.adjustment.controller;

import com.myapp.rh.adjustment.dto.AdjustmentRequestDTO;
import com.myapp.rh.adjustment.dto.AdjustmentResponseDTO;
import com.myapp.rh.adjustment.service.AdjustmentService;
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
@RequestMapping("/api/timeclock/adjustment")
@RequiredArgsConstructor
@Tag(name = "Time Adjustment", description = "Time record adjustment requests and approvals")
public class AdjustmentController {

    private final AdjustmentService adjustmentService;

    @Operation(summary = "Request adjustment", description = "Employee requests a time record correction")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Adjustment requested"),
            @ApiResponse(responseCode = "400", description = "Invalid data or pending adjustment already exists")
    })
    @PostMapping
    public ResponseEntity<AdjustmentResponseDTO> requestAdjustment(
            @RequestBody @Valid AdjustmentRequestDTO dto,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adjustmentService.requestAdjustment(
                        authentication.getName(), dto));
    }

    @Operation(summary = "My adjustments", description = "Returns all adjustment requests for authenticated employee")
    @ApiResponse(responseCode = "200", description = "Adjustments returned successfully")
    @GetMapping("/me")
    public ResponseEntity<List<AdjustmentResponseDTO>> getMyAdjustments(
            Authentication authentication) {
        return ResponseEntity.ok(
                adjustmentService.getMyAdjustments(authentication.getName()));
    }

    @Operation(summary = "Pending adjustments", description = "Returns all pending adjustments — HR_MANAGER only")
    @ApiResponse(responseCode = "200", description = "Pending adjustments returned")
    @GetMapping("/pending")
    public ResponseEntity<List<AdjustmentResponseDTO>> getPendingAdjustments() {
        return ResponseEntity.ok(adjustmentService.getPendingAdjustments());
    }

    @Operation(summary = "Approve adjustment", description = "Approves and applies the time correction — HR_MANAGER only")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Adjustment approved"),
            @ApiResponse(responseCode = "400", description = "Adjustment already processed"),
            @ApiResponse(responseCode = "404", description = "Adjustment not found")
    })
    @PutMapping("/{id}/approve")
    public ResponseEntity<AdjustmentResponseDTO> approve(
            @PathVariable UUID id,
            Authentication authentication) {
        return ResponseEntity.ok(
                adjustmentService.approveAdjustment(id, authentication.getName()));
    }

    @Operation(summary = "Reject adjustment", description = "Rejects the time correction request — HR_MANAGER only")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Adjustment rejected"),
            @ApiResponse(responseCode = "400", description = "Adjustment already processed"),
            @ApiResponse(responseCode = "404", description = "Adjustment not found")
    })
    @PutMapping("/{id}/reject")
    public ResponseEntity<AdjustmentResponseDTO> reject(
            @PathVariable UUID id,
            Authentication authentication) {
        return ResponseEntity.ok(
                adjustmentService.rejectAdjustment(id, authentication.getName()));
    }
}