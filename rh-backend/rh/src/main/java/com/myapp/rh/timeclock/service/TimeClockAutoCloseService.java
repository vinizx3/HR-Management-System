package com.myapp.rh.timeclock.service;

import com.myapp.rh.overtime.service.OvertimeBalanceService;
import com.myapp.rh.timeclock.entity.TimeRecord;
import com.myapp.rh.timeclock.entity.TimeRecordStatus;
import com.myapp.rh.timeclock.entity.WorkTimeCalculator;
import com.myapp.rh.timeclock.repository.TimeRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeClockAutoCloseService {

    private final TimeRecordRepository timeRecordRepository;
    private final OvertimeBalanceService overtimeBalanceService;
    private final Clock clock;

    @Scheduled(cron = "0 55 23 * * *")
    @Transactional
    public void autoCloseOpenRecords() {

        List<TimeRecord> openRecords =
                timeRecordRepository.findAll()
                        .stream()
                        .filter(r -> r.getStatus() == TimeRecordStatus.OPEN)
                        .toList();

        if (openRecords.isEmpty()) {
            log.info("No open time records to auto-close");
            return;
        }

        log.info("Auto-closing {} open time records", openRecords.size());

        for (TimeRecord record : openRecords) {

            try {
                LocalDateTime clockOut = record.getDate().atTime(18, 0);

                if (record.getClockIn() != null &&
                        clockOut.isBefore(record.getClockIn())) {
                    clockOut = record.getClockIn().plusHours(8);
                }

                record.setClockOut(clockOut);

                long minutes = ChronoUnit.MINUTES.between(
                        record.getClockIn(),
                        clockOut
                );

                record.setWorkedMinutes((int) minutes);

                int overtime = WorkTimeCalculator.calculateOvertime(minutes);
                record.setOvertimeMinutes(overtime);

                if (overtime > 0) {
                    overtimeBalanceService.addOvertime(record.getEmployee(), overtime);
                }

                record.setStatus(TimeRecordStatus.CLOSED);

                timeRecordRepository.save(record);

            } catch (Exception e) {
                log.error("Error auto-closing record id={}", record.getId(), e);
            }
        }
    }
}
