package com.myapp.rh.vacation.controller;

import com.myapp.rh.vacation.dto.VacationRequestDTO;
import com.myapp.rh.vacation.dto.VacationResponseDTO;
import com.myapp.rh.vacation.service.VacationRequestService;
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
@RequestMapping("/api/vacations")
@RequiredArgsConstructor
@Tag(name = "Vacations", description = "Vacation request and approval management")
public class VacationRequestController {

    private final VacationRequestService vacationRequestService;

    @Operation(summary = "Request vacation", description = "Employee requests vacation — minimum 30 days in advance")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Vacation requested"),
            @ApiResponse(responseCode = "400", description = "Invalid dates or CLT rules violated")
    })
    @PostMapping("/request")
    public ResponseEntity<VacationResponseDTO> requestVacation(
            Authentication authentication,
            @RequestBody @Valid VacationRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vacationRequestService.requestVacation(
                        authentication.getName(), request));
    }

    @Operation(summary = "My vacations", description = "Returns all vacation requests for authenticated employee")
    @ApiResponse(responseCode = "200", description = "Vacations returned successfully")
    @GetMapping("/me")
    public ResponseEntity<List<VacationResponseDTO>> getMyVacations(
            Authentication authentication) {
        return ResponseEntity.ok(
                vacationRequestService.getMyVacations(authentication.getName()));
    }

    @Operation(summary = "All vacations", description = "Returns all vacation requests — HR_MANAGER only")
    @ApiResponse(responseCode = "200", description = "All vacations returned")
    @GetMapping("/all")
    public ResponseEntity<List<VacationResponseDTO>> getAllVacations() {
        return ResponseEntity.ok(vacationRequestService.getAllVacations());
    }

    @Operation(summary = "Pending vacations", description = "Returns pending vacation requests — HR_MANAGER only")
    @ApiResponse(responseCode = "200", description = "Pending vacations returned")
    @GetMapping("/pending")
    public ResponseEntity<List<VacationResponseDTO>> getPendingRequests() {
        return ResponseEntity.ok(vacationRequestService.getPendingRequests());
    }

    @Operation(summary = "Approve vacation", description = "Approves vacation request — HR_MANAGER only")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vacation approved"),
            @ApiResponse(responseCode = "400", description = "Already processed"),
            @ApiResponse(responseCode = "404", description = "Vacation not found")
    })
    @PutMapping("/{id}/approve")
    public ResponseEntity<VacationResponseDTO> approvedRequest(
            @PathVariable UUID id, Authentication authentication) {
        return ResponseEntity.ok(
                vacationRequestService.approveRequest(id, authentication.getName()));
    }

    @Operation(summary = "Reject vacation", description = "Rejects vacation request — HR_MANAGER only")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vacation rejected"),
            @ApiResponse(responseCode = "400", description = "Already processed"),
            @ApiResponse(responseCode = "404", description = "Vacation not found")
    })
    @PutMapping("/{id}/reject")
    public ResponseEntity<VacationResponseDTO> rejectRequest(
            @PathVariable UUID id, Authentication authentication) {
        return ResponseEntity.ok(
                vacationRequestService.rejectRequest(id, authentication.getName()));
    }

    @Operation(summary = "Expiring vacations", description = "Returns approved vacations starting soon — HR_MANAGER only")
    @ApiResponse(responseCode = "200", description = "Expiring vacations returned")
    @GetMapping("/expiring")
    public ResponseEntity<List<VacationResponseDTO>> getExpiringVacations(
            @RequestParam(defaultValue = "30") int daysAhead) {
        return ResponseEntity.ok(
                vacationRequestService.getExpiringVacations(daysAhead));
    }
}
