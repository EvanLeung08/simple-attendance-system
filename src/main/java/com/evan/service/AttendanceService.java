package com.evan.service;


import com.evan.model.AttendanceRecord;
import com.evan.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    public void saveAttendanceRecord(AttendanceRecord record) {
        attendanceRepository.save(record);
    }

    public List<AttendanceRecord> findAllRecords() {
        return attendanceRepository.findAll();
    }
    public List<AttendanceRecord> findRecordsByYearMonth(YearMonth yearMonth) {
        return attendanceRepository.findAllByDateBetween(yearMonth.atDay(1), yearMonth.atEndOfMonth());
    }
    public Map<String, Integer> findLeaveDaysByYearMonth(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();
        return attendanceRepository.findLeaveDaysByYearMonth(startOfMonth, endOfMonth);
    }
}