package com.evan.controller;

import com.evan.model.AttendanceRecord;
import com.evan.service.AttendanceService;
import com.evan.service.AttendanceStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    @GetMapping("/")
    public String showAttendanceForm(Model model) {
        model.addAttribute("attendanceRecord", new AttendanceRecord());
        model.addAttribute("daysOfWeek", Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"));
        return "attendance";
    }

    @PostMapping("/submit")
    public String submitAttendance(@RequestParam("username") String username,
                                   @RequestParam("daysOfWeek") List<String> daysOfWeek,
                                   @RequestParam(name = "leaveDays", defaultValue = "0") int leaveDays,
                                   @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                   @RequestParam("team") String team) {
        AttendanceRecord record = new AttendanceRecord();
        record.setUsername(username);
        record.setDate(date);
        record.setDaysOfWeek(String.join(", ", daysOfWeek));
        record.setLeaveDays(leaveDays);
        record.setTeam(team);
        attendanceService.saveAttendanceRecord(record);
        return "redirect:/stats";
    }

    @GetMapping("/stats")
    public String showStats(Model model,
                            @RequestParam(required = false) Integer year,
                            @RequestParam(required = false) Integer month,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "0") int statsPage,
                            @RequestParam(required = false) String team) {
        if (year == null || month == null) {
            year = YearMonth.now().getYear();
            month = YearMonth.now().getMonthValue();
        }
        YearMonth yearMonth = YearMonth.of(year, month);
        model.addAttribute("currentYearMonth", yearMonth);

        Map<String, Map<String, String>> stats = attendanceStatsService.calculateAttendanceStats(year, month, team);
        model.addAttribute("stats", stats);

        Page<AttendanceRecord> recordsPage = attendanceService.findRecordsByYearMonthAndTeam(yearMonth, team, PageRequest.of(page, 5));
        model.addAttribute("recordsPage", recordsPage);

        Page<Map.Entry<String, Map<String, String>>> statsPageResult = attendanceStatsService.getPaginatedStats(stats, PageRequest.of(statsPage, 5));
        model.addAttribute("statsPage", statsPageResult);

        List<String> teams = attendanceService.findAllTeams();
        model.addAttribute("teams", teams);

        Map<String, Double> teamComplianceRates = attendanceStatsService.calculateTeamComplianceRates(year, month);
        model.addAttribute("teamComplianceRates", teamComplianceRates);

        return "stats";
    }
    @GetMapping("/delete")
    public String deleteAttendanceRecord(@RequestParam("id") Long id) {
        attendanceService.deleteAttendanceRecord(id);
        return "redirect:/stats";
    }
}