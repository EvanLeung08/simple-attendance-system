package com.evan.controller;

import com.evan.model.AttendanceRecord;
import com.evan.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class AdminController {

    @Autowired
    private AttendanceService attendanceService;

    @GetMapping("/admin")
    public String showAdminPage(Model model) {
        List<AttendanceRecord> records = attendanceService.findAllRecords();
        model.addAttribute("records", records);
        return "admin";
    }

    @GetMapping("/admin/edit")
    public String editAttendanceRecord(@RequestParam("id") Long id, Model model) {
        AttendanceRecord record = attendanceService.findRecordById(id);
        model.addAttribute("record", record);
        return "edit";
    }

    @PostMapping("/admin/update")
    public String updateAttendanceRecord(@RequestParam("id") Long id,
                                         @RequestParam("username") String username,
                                         @RequestParam("daysOfWeek") String daysOfWeek,
                                         @RequestParam("leaveDays") int leaveDays,
                                         @RequestParam("team") String team) {
        AttendanceRecord record = attendanceService.findRecordById(id);
        if (record != null) {
            record.setUsername(username);
            record.setDaysOfWeek(daysOfWeek);
            record.setLeaveDays(leaveDays);
            record.setTeam(team);
            attendanceService.saveAttendanceRecord(record);
        }
        return "redirect:/admin";
    }

    @GetMapping("/admin/delete")
    public String deleteAttendanceRecord(@RequestParam("id") Long id) {
        attendanceService.deleteAttendanceRecord(id);
        return "redirect:/admin";
    }
}