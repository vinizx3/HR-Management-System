package com.myapp.rh.timeclock.entity;

import com.myapp.rh.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "time_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalDateTime clockIn;

    private LocalDateTime clockOut;

    private Integer workedMinutes;

    private Integer overtimeMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TimeRecordStatus status;
}
