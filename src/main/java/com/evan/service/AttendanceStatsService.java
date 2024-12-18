package com.evan.service;

import com.evan.model.AttendanceRecord;
import com.evan.repository.AttendanceRepository;
import com.evan.utils.WorkdayCalculator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class AttendanceStatsService {
    @Value("${workday.percentage}")
    private double workdayPercentage;

    private final AttendanceRepository attendanceRepository;

    public AttendanceStatsService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    public Map<String, Map<String, String>> calculateAttendanceStats(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);


        Map<String, List<AttendanceRecord>> recordsByUser = attendanceRepository.findAll().stream()
                .filter(record -> record.getDate() != null && yearMonth.equals(YearMonth.from(record.getDate())))
                .collect(Collectors.groupingBy(AttendanceRecord::getUsername));

        Map<String, Map<String, String>> attendanceStats = new HashMap<>();
        for (Map.Entry<String, List<AttendanceRecord>> entry : recordsByUser.entrySet()) {
            String username = entry.getKey();
            List<AttendanceRecord> userRecords = entry.getValue();
            Map<String, String> stats = new HashMap<>();
            int attendedDays = 0;
            int leaveDays = 0;

            // Count the attended days for each user
            for (AttendanceRecord record : userRecords) {
                List<DayOfWeek> daysOfWeek = Arrays.stream(record.getDaysOfWeek().split(", "))
                        .map(String::trim)
                        .map(day -> DayOfWeek.valueOf(day.toUpperCase()))
                        .collect(Collectors.toList());
                attendedDays += countDaysInMonth(yearMonth, daysOfWeek);
                leaveDays += record.getLeaveDays();
            }

            int requiredDays =  (int) ((WorkdayCalculator.countWorkdays(year, month)-leaveDays) * workdayPercentage);
            stats.put("attendedDays", String.valueOf(attendedDays));
            stats.put("requiredDays", String.valueOf(requiredDays));
            stats.put("totalLeaveDays", String.valueOf(leaveDays));
            stats.put("complianceRatio", (int) ((attendedDays / (double) requiredDays) * 100)+"%");
            attendanceStats.put(username, stats);
        }

        return attendanceStats;
    }

    private int countDaysInMonth(YearMonth yearMonth, List<DayOfWeek> daysOfWeek) {
        int count = 0;
        for (DayOfWeek day : daysOfWeek) {
            count++;
        }
        return count;
    }


}