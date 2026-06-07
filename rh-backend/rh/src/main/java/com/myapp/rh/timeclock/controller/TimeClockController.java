package com.myapp.rh.timeclock.controller;

import com.myapp.rh.timeclock.dto.TimeRecordResponseDTO;
import com.myapp.rh.timeclock.service.TimeClockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/timeclock")
@RequiredArgsConstructor
@Tag(name = "Time Clock", description = "Clock-in/out and time record management")
public class TimeClockController {

    private final TimeClockService timeClockService;

    @Operation(summary = "Clock in", description = "Register clock-in for authenticated employee")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Clock-in registered"),
            @ApiResponse(responseCode = "400", description = "Already clocked in today")
    })
    @PostMapping("/clock-in")
    public ResponseEntity<TimeRecordResponseDTO> clockIn(Authentication authentication) {
        return ResponseEntity.ok(timeClockService.clockIn(authentication.getName()));
    }

    @Operation(summary = "Clock out", description = "Register clock-out and calculate worked hours")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Clock-out registered"),
            @ApiResponse(responseCode = "400", description = "No open record found or already closed")
    })
    @PostMapping("/clock-out")
    public ResponseEntity<TimeRecordResponseDTO> clockOut(Authentication authentication) {
        return ResponseEntity.ok(timeClockService.clockOut(authentication.getName()));
    }

    @Operation(summary = "My time records", description = "Returns all time records for authenticated employee")
    @ApiResponse(responseCode = "200", description = "Records returned successfully")
    @GetMapping("/me")
    public ResponseEntity<List<TimeRecordResponseDTO>> getMyRecords(Authentication authentication) {
        return ResponseEntity.ok(timeClockService.getMyRecords(authentication.getName()));
    }

    @Operation(summary = "All time records", description = "Returns all time records — HR_MANAGER only")
    @ApiResponse(responseCode = "200", description = "Records returned successfully")
    @GetMapping("/all")
    @PreAuthorize("hasRole('HR_MANAGER')")
    public ResponseEntity<List<TimeRecordResponseDTO>> getAllRecords() {
        return ResponseEntity.ok(timeClockService.getAllRecords());
    }

    @Operation(summary = "Employee time records", description = "Returns time records for a specific employee")
    @ApiResponse(responseCode = "200", description = "Records returned successfully")
    @GetMapping("/{employeeId}")
    public ResponseEntity<List<TimeRecordResponseDTO>> getEmployeeRecords(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(timeClockService.getEmployeeRecords(employeeId));
    }
}