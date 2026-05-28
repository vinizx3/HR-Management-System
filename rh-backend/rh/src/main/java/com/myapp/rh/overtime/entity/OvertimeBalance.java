package com.myapp.rh.overtime.entity;

import com.myapp.rh.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "overtime_balance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OvertimeBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "employee_id", unique = true, nullable = false)
    private Employee employee;

    @Builder.Default
    @Column(nullable = false)
    private Integer totalMinutes = 0;
}
