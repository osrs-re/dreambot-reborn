package com.dreambotreborn.api.script.schedule;

import java.util.ArrayList;
import java.util.List;

public class ScriptSchedule implements Cloneable
{
    private String name = "Schedule";
    private boolean repeating;
    private List<ScheduledScript> scheduledScripts = new ArrayList<>();

    @Override
    public ScriptSchedule clone()
    {
        ScriptSchedule clone = new ScriptSchedule();
        clone.name = name;
        clone.repeating = repeating;
        clone.scheduledScripts = new ArrayList<>(scheduledScripts);
        return clone;
    }

    public String getName() { return name; }
    public boolean isRepeating() { return repeating; }
    public List<ScheduledScript> getScheduledScripts() { return new ArrayList<>(scheduledScripts); }
    public void setName(String value) { name = value == null ? "Schedule" : value; }
    public void setRepeating(boolean value) { repeating = value; }
    public void setScheduledScripts(List<ScheduledScript> value)
    {
        scheduledScripts = value == null ? new ArrayList<>() : new ArrayList<>(value);
    }
}
