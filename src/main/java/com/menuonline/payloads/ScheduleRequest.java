package com.menuonline.payloads;

import com.menuonline.entity.Schedule;
import com.menuonline.entity.UserEntity;

public record ScheduleRequest(String days, String openHour, String closeHour,
        String startLaunch, String endLaunch) {
    public Schedule toEntity(UserEntity u) {
        Schedule a = new Schedule();
        a.setUser(u);
        a.setDays(days);
        a.setOpenHour(openHour);
        a.setCloseHour(closeHour);
        a.setStartLaunch(startLaunch);
        a.setEndLaunch(endLaunch);
        return a;
    }
}
