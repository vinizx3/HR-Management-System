package com.myapp.rh.employee.repository;

import com.myapp.rh.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    Optional<Employee> findByEmail(String email);
    List<Employee> findAllByActiveTrue();
    boolean existsByEmail(String email);
}
