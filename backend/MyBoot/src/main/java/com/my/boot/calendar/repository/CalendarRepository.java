package com.my.boot.calendar.repository;

import com.my.boot.calendar.entity.CalendarEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalendarRepository extends JpaRepository<CalendarEntity, Long> {
    // 필요한 경우 추가 쿼리 메서드 작성
    // 예: 날짜 범위로 조회 등
}
