package com.dreambotreborn.api.script.schedule;

import java.util.ArrayList;
import java.util.List;

public class ScheduleSettings
{
    private List<ScriptSchedule> schedules = new ArrayList<>();
    public List<ScriptSchedule> getSchedules() { return new ArrayList<>(schedules); }
    public void setSchedules(List<ScriptSchedule> value)
    {
        schedules = value == null ? new ArrayList<>() : new ArrayList<>(value);
    }
}
