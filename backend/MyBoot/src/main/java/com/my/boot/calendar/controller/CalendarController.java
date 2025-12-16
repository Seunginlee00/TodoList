package com.my.boot.calendar.controller;

import com.my.boot.calendar.entity.CalendarEntity;
import com.my.boot.calendar.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping
    public ResponseEntity<List<CalendarEntity>> getAllEvents() {
        return ResponseEntity.ok(calendarService.getAllEvents());
    }

    @PostMapping
    public ResponseEntity<CalendarEntity> createEvent(@RequestBody CalendarEntity event) {
        return ResponseEntity.ok(calendarService.createEvent(event));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        calendarService.deleteEvent(id);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CalendarEntity> updateEvent(@PathVariable Long id, @RequestBody CalendarEntity event) {
        return ResponseEntity.ok(calendarService.updateEvent(id, event));
    }
}
