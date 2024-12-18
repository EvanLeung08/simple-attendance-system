package com.evan.repository;



import com.evan.model.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceRecord, Long> {
    AttendanceRecord findTopByUsernameAndDate(String username, LocalDate date);
    List<AttendanceRecord> findAllByDateBetween(LocalDate start, LocalDate end);
    // 注意：:start 和 :end 应该是方法参数名称，而不是硬编码的字符串
    // Now the method expects LocalDate objects for start and end dates
    @Query("SELECT new map(a.username, SUM(a.leaveDays)) FROM AttendanceRecord a WHERE a.date BETWEEN :start AND :end")
    Map<String, Integer> findLeaveDaysByYearMonth(LocalDate start, LocalDate end);

}