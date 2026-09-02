package com.dreambotreborn.api.script.schedule;

import java.util.ArrayList;
import java.util.List;
import com.dreambotreborn.api.script.schedule.conditions.RunningTime;
import com.dreambotreborn.api.script.schedule.conditions.ScriptStop;
import com.dreambotreborn.api.script.schedule.conditions.StopCondition;

public class ScheduledScript
{
    private String script = "";
    private String parametersString = "";
    private String accountId = "";
    private String accountName = "";
    private StopCondition stopCondition = new RunningTime();

    public String[] getParameters()
    {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int index = 0; index < parametersString.length(); index++)
        {
            char value = parametersString.charAt(index);
            if (value == '"') quoted = !quoted;
            else if (Character.isWhitespace(value) && !quoted)
            {
                if (current.length() > 0) { result.add(current.toString()); current.setLength(0); }
            }
            else current.append(value);
        }
        if (current.length() > 0) result.add(current.toString());
        return result.toArray(new String[0]);
    }

    public String getScript() { return script; }
    public String getParametersString() { return parametersString; }
    public StopCondition getStopCondition() { return stopCondition; }
    public String getAccountId() { return accountId; }
    public String getAccountName() { return accountName; }
    public void setScript(String value) { script = value == null ? "" : value; }
    public void setParametersString(String value) { parametersString = value == null ? "" : value; }
    public void setStopCondition(StopCondition value) { stopCondition = value == null ? new ScriptStop() : value; }
    public void setAccountId(String value) { accountId = value == null ? "" : value; }
    public void setAccountName(String value) { accountName = value == null ? "" : value; }
}
