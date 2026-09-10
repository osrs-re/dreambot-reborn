package com.dreambotreborn.api.methods.quest.book.requirement.skill;

import java.util.Arrays;
import com.dreambotreborn.api.methods.quest.book.requirement.Requirement;
import com.dreambotreborn.api.methods.skills.Skills;

public class SkillRequirement extends Requirement
{
    private SkillReq[] reqs;
    public SkillRequirement(SkillReq... reqs) { setReqs(reqs); }
    @Override public boolean meetsRequirement()
    {
        for (SkillReq req : reqs)
            if (req == null || req.getSkill() == null
                || Skills.getRealLevel(req.getSkill()) < req.getLevel()) return false;
        return true;
    }
    public SkillReq[] getReqs() { return Arrays.copyOf(reqs, reqs.length); }
    public void setReqs(SkillReq[] value)
    {
        reqs = value == null ? new SkillReq[0] : Arrays.copyOf(value, value.length);
    }
}
