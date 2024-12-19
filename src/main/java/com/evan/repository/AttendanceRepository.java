package com.evan.repository;

import com.evan.model.AttendanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceRecord, Long> {
    AttendanceRecord findTopByUsernameAndDate(String username, LocalDate date);

    List<AttendanceRecord> findAllByDateBetween(LocalDate start, LocalDate end);

    Page<AttendanceRecord> findAllByDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<AttendanceRecord> findAllByDateBetweenAndTeam(LocalDate startDate, LocalDate endDate, String team, Pageable pageable);

    Page<AttendanceRecord> findAllByDateBetweenOrderByDateDesc(LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<AttendanceRecord> findAllByDateBetweenAndTeamOrderByDateDesc(LocalDate startDate, LocalDate endDate, String team, Pageable pageable);
}