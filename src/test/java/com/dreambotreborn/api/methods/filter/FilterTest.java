package com.dreambotreborn.api.methods.filter;

import com.dreambotreborn.api.methods.filter.impl.IdFilter;
import com.dreambotreborn.api.methods.filter.impl.NameFilter;
import com.dreambotreborn.api.wrappers.interactive.Identifiable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilterTest
{
    @Test
    void filtersComposeLikePredicates()
    {
        Identifiable tree = value(1276, "Tree");
        Filter<Identifiable> named = new NameFilter<>("TREE");
        Filter<Identifiable> id = new IdFilter<>(1276);
        assertTrue(named.and(id).match(tree));
        assertFalse(named.and(new IdFilter<>(1)).match(tree));
        assertTrue(named.negate().negate().test(tree));
    }

    private static Identifiable value(int id, String name)
    {
        return new Identifiable()
        {
            @Override public int getId() { return id; }
            @Override public String getName() { return name; }
        };
    }
}
