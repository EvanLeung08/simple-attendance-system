package com.evan.controller;


import com.evan.model.AttendanceRecord;
import com.evan.repository.AttendanceRepository;
import com.evan.service.AttendanceService;
import com.evan.service.AttendanceStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private AttendanceStatsService attendanceStatsService;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @GetMapping("/")
    public String showAttendanceForm(Model model) {
        model.addAttribute("attendanceRecord", new AttendanceRecord());
        model.addAttribute("daysOfWeek", Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"));
        return "attendance";
    }

    @PostMapping("/submit")
    public String submitAttendance(@RequestParam("username") String username,
                                   @RequestParam("daysOfWeek") List<String> daysOfWeek,
                                   @RequestParam(name = "leaveDays", defaultValue = "0") int leaveDays, // 设置默认值为0
                                   @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        AttendanceRecord record = new AttendanceRecord();
        record.setUsername(username);
        record.setDate(date);
        if (daysOfWeek != null && !daysOfWeek.isEmpty()) {
            record.setDaysOfWeek(daysOfWeek.stream().collect(Collectors.joining(", ")));
        }
        record.setLeaveDays(leaveDays); // Set the leave days
        attendanceService.saveAttendanceRecord(record);
        return "redirect:/stats";
    }



    @GetMapping("/stats")
    public String showStats(Model model, @RequestParam(required = false) Integer year, @RequestParam(required = false) Integer month) {
        if (year == null || month == null) {
            year = YearMonth.now().getYear();
            month = YearMonth.now().getMonthValue();
        }
        YearMonth yearMonth = YearMonth.of(year, month);
        model.addAttribute("currentYearMonth", yearMonth);

        Map<String, Map<String, String>> stats = attendanceStatsService.calculateAttendanceStats(year, month);
        model.addAttribute("stats", stats);


        List<AttendanceRecord> records = attendanceService.findRecordsByYearMonth(yearMonth);
        model.addAttribute("records", records);

        return "stats";
    }

}