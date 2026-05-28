package com.myapp.rh.adjustment.entity;

import com.myapp.rh.employee.entity.Employee;
import com.myapp.rh.timeclock.entity.TimeRecord;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "adjustment_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Adjustment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "time_record_id", nullable = false)
    private TimeRecord timeRecord;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDateTime requestedClockIn;

    @Column(nullable = false)
    private LocalDateTime requestedClockOut;

    @Column(nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdjustmentStatus status;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private Employee reviewedBy;
}