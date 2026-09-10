package com.dreambotreborn.api.data.requirements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.dreambotreborn.api.methods.skills.Skill;
import com.dreambotreborn.api.methods.skills.Skills;

public class SkillTotalRequirement extends Requirement
{
    private List<Skill> skills;
    private int total;
    public SkillTotalRequirement(int total, Skill... skills)
    { this.total = total; setSkills(skills == null ? Collections.emptyList() : Arrays.asList(skills)); }
    @Override public boolean meetsRequirement()
    {
        int value = 0;
        for (Skill skill : skills) value += Skills.getRealLevel(skill);
        return value >= total;
    }
    public List<Skill> getSkills() { return Collections.unmodifiableList(skills); }
    public int getTotal() { return total; }
    public void setSkills(List<Skill> value)
    {
        skills = new ArrayList<>();
        if (value != null) for (Skill skill : value) if (skill != null) skills.add(skill);
    }
    public void setTotal(int value) { total = value; }
}
