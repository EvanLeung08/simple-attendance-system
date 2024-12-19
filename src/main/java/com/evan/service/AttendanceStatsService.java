package com.evan.service;

import com.evan.model.AttendanceRecord;
import com.evan.repository.AttendanceRepository;
import com.evan.utils.WorkdayCalculator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttendanceStatsService {
    @Value("${workday.percentage}")
    private double workdayPercentage;

    private final AttendanceRepository attendanceRepository;

    public AttendanceStatsService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    public Map<String, Map<String, String>> calculateAttendanceStats(int year, int month, String team) {
        YearMonth yearMonth = YearMonth.of(year, month);

        List<AttendanceRecord> records;
        if (team == null || team.isEmpty()) {
            records = attendanceRepository.findAll();
        } else {
            records = attendanceRepository.findAll().stream()
                    .filter(record -> team.equals(record.getTeam()))
                    .collect(Collectors.toList());
        }

        Map<String, List<AttendanceRecord>> recordsByUser = records.stream()
                .filter(record -> record.getDate() != null && yearMonth.equals(YearMonth.from(record.getDate())))
                .collect(Collectors.groupingBy(AttendanceRecord::getUsername));

        Map<String, Map<String, String>> attendanceStats = new HashMap<>();
        for (Map.Entry<String, List<AttendanceRecord>> entry : recordsByUser.entrySet()) {
            String username = entry.getKey();
            List<AttendanceRecord> userRecords = entry.getValue();
            Map<String, String> stats = new HashMap<>();
            int attendedDays = 0;
            int leaveDays = 0;
            String userTeam = "";

            for (AttendanceRecord record : userRecords) {
                List<DayOfWeek> daysOfWeek = Arrays.stream(record.getDaysOfWeek().split(", "))
                        .map(String::trim)
                        .map(day -> DayOfWeek.valueOf(day.toUpperCase()))
                        .collect(Collectors.toList());
                attendedDays += countDaysInMonth(yearMonth, daysOfWeek);
                leaveDays += record.getLeaveDays();
                userTeam = record.getTeam(); // Get the team of the user
            }

            int requiredDays = (int) ((WorkdayCalculator.countWorkdays(year, month) - leaveDays) * workdayPercentage);
            stats.put("attendedDays", String.valueOf(attendedDays));
            stats.put("requiredDays", String.valueOf(requiredDays));
            stats.put("totalLeaveDays", String.valueOf(leaveDays));
            stats.put("complianceRatio", (int) ((attendedDays / (double) requiredDays) * 100) + "%");
            stats.put("team", userTeam); // Add the team to the stats
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

    public Page<Map.Entry<String, Map<String, String>>> getPaginatedStats(Map<String, Map<String, String>> stats, Pageable pageable) {
        List<Map.Entry<String, Map<String, String>>> statsList = new ArrayList<>(stats.entrySet());
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), statsList.size());
        return new PageImpl<>(statsList.subList(start, end), pageable, statsList.size());
    }

    public Map<String, Double> calculateTeamComplianceRates(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        List<AttendanceRecord> records = attendanceRepository.findAll().stream()
                .filter(record -> record.getDate() != null && yearMonth.equals(YearMonth.from(record.getDate())))
                .collect(Collectors.toList());

        Map<String, List<AttendanceRecord>> recordsByTeam = records.stream()
                .filter(record -> record.getTeam() != null)
                .collect(Collectors.groupingBy(AttendanceRecord::getTeam));

        Map<String, Double> teamComplianceRates = new HashMap<>();
        for (Map.Entry<String, List<AttendanceRecord>> entry : recordsByTeam.entrySet()) {
            String team = entry.getKey();
            List<AttendanceRecord> teamRecords = entry.getValue();
            Map<String, List<AttendanceRecord>> recordsByUser = teamRecords.stream()
                    .collect(Collectors.groupingBy(AttendanceRecord::getUsername));

            double totalComplianceRate = 0;
            int userCount = 0;

            for (Map.Entry<String, List<AttendanceRecord>> userEntry : recordsByUser.entrySet()) {
                List<AttendanceRecord> userRecords = userEntry.getValue();
                int attendedDays = 0;
                int leaveDays = 0;

                for (AttendanceRecord record : userRecords) {
                    List<DayOfWeek> daysOfWeek = Arrays.stream(record.getDaysOfWeek().split(", "))
                            .map(String::trim)
                            .map(day -> DayOfWeek.valueOf(day.toUpperCase()))
                            .collect(Collectors.toList());
                    attendedDays += countDaysInMonth(yearMonth, daysOfWeek);
                    leaveDays += record.getLeaveDays();
                }

                int workdaysInMonth = WorkdayCalculator.countWorkdays(year, month);
                int requiredDays = (int) ((workdaysInMonth - leaveDays) * workdayPercentage);
                double complianceRate = (attendedDays / (double) requiredDays) * 100;
                totalComplianceRate += complianceRate;
                userCount++;
            }

            double averageComplianceRate = totalComplianceRate / userCount;
            teamComplianceRates.put(team, averageComplianceRate);
        }

        return teamComplianceRates;
    }
}