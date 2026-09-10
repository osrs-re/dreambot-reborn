package com.dreambotreborn.api.data.requirements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Composable requirement; the base type requires every nested requirement. */
public class Requirement
{
    private final List<Requirement> requirements = new ArrayList<>();
    public Requirement(Requirement... requirements)
    {
        if (requirements != null) for (Requirement requirement : requirements)
            if (requirement != null) this.requirements.add(requirement);
    }
    public Requirement add(Requirement requirement)
    { if (requirement != null) requirements.add(requirement); return this; }
    public boolean meetsRequirement()
    {
        for (Requirement requirement : requirements)
            if (!requirement.meetsRequirement()) return false;
        return true;
    }
    public List<Requirement> getRequirements()
    { return Collections.unmodifiableList(requirements); }
}
