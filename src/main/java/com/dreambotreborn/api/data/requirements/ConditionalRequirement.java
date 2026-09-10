package com.dreambotreborn.api.data.requirements;

public class ConditionalRequirement extends Requirement
{
    private LogicType logicType;
    public ConditionalRequirement(LogicType logicType, Requirement... requirements)
    { super(requirements); this.logicType = logicType == null ? LogicType.AND : logicType; }
    @Override public boolean meetsRequirement()
    {
        if (logicType == LogicType.OR)
        {
            for (Requirement requirement : getRequirements())
                if (requirement.meetsRequirement()) return true;
            return getRequirements().isEmpty();
        }
        if (logicType == LogicType.NOT)
        {
            for (Requirement requirement : getRequirements())
                if (requirement.meetsRequirement()) return false;
            return true;
        }
        return super.meetsRequirement();
    }
    public LogicType getLogicType() { return logicType; }
    public void setLogicType(LogicType value) { logicType = value == null ? LogicType.AND : value; }
}
