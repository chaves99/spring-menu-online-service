package com.menuonline.payloads;

import java.util.List;

import com.menuonline.entity.Schedule;

public record ScheduleResponse(Long id, String days, String openHour, String closeHour,
        String startLaunch, String endLaunch) {

    public static List<ScheduleResponse> from(List<Schedule> schedules) {
        return schedules.stream().map(ScheduleResponse::from).toList();
    }

    public static ScheduleResponse from(Schedule a) {
        return new ScheduleResponse(a.getId(),
                a.getDays(),
                a.getOpenHour(),
                a.getCloseHour(),
                a.getStartLaunch(),
                a.getEndLaunch());
    }
}
