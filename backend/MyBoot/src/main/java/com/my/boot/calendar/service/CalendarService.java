package com.my.boot.calendar.service;

import com.my.boot.calendar.entity.CalendarEntity;
import com.my.boot.calendar.repository.CalendarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarService {

    private final CalendarRepository calendarRepository;

    public List<CalendarEntity> getAllEvents() {
        return calendarRepository.findAll();
    }

    @Transactional
    public CalendarEntity createEvent(CalendarEntity event) {
        return calendarRepository.save(event);
    }

    @Transactional
    public void deleteEvent(Long id) {
        calendarRepository.deleteById(id);
    }

    @Transactional
    public CalendarEntity updateEvent(Long id, CalendarEntity updatedEvent) {
        CalendarEntity event = calendarRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + id));
        
        event.setTitle(updatedEvent.getTitle());
        event.setStart(updatedEvent.getStart());
        event.setEnd(updatedEvent.getEnd());
        event.setAllDay(updatedEvent.getAllDay());
        
        return event;
    }
}
