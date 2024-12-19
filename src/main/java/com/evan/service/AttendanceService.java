package com.evan.service;

import com.evan.model.AttendanceRecord;
import com.evan.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    public void saveAttendanceRecord(AttendanceRecord record) {
        attendanceRepository.save(record);
    }

    public Page<AttendanceRecord> findRecordsByYearMonthAndTeam(YearMonth yearMonth, String team, Pageable pageable) {
        if (team == null || team.isEmpty()) {
            return attendanceRepository.findAllByDateBetweenOrderByDateDesc(yearMonth.atDay(1), yearMonth.atEndOfMonth(), pageable);
        } else {
            return attendanceRepository.findAllByDateBetweenAndTeamOrderByDateDesc(yearMonth.atDay(1), yearMonth.atEndOfMonth(), team, pageable);
        }
    }

    public List<String> findAllTeams() {
        return attendanceRepository.findAll().stream()
                .map(AttendanceRecord::getTeam)
                .distinct()
                .collect(Collectors.toList());
    }

    public void deleteAttendanceRecord(Long id) {
        attendanceRepository.deleteById(id);
    }

    public List<AttendanceRecord> findAllRecords() {
        return attendanceRepository.findAll();
    }

    public AttendanceRecord findRecordById(Long id) {
        return attendanceRepository.findById(id).orElse(null);
    }
}