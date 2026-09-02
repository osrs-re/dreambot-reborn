package com.dreambotreborn.api.methods.walking.pathfinding.impl.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.dreambotreborn.api.methods.map.Tile;
import com.dreambotreborn.api.methods.walking.path.impl.GlobalPath;
import com.dreambotreborn.api.methods.walking.web.node.AbstractWebNode;
import com.dreambotreborn.api.wrappers.items.Item;

public class WebPathQuery
{
    private Tile from;
    private Tile to;
    private boolean useBankCache;
    private final List<Item> withItems = new ArrayList<>();
    private WebPathResponse calculated;
    public boolean canUseBankCache() { return useBankCache; }
    public WebPathResponse calculate()
    {
        calculated = WebPathResponse.builder().query(this)
            .nodes(WebFinder.getWebFinder().calculate(this).path()).build();
        return calculated;
    }
    public boolean hasRequiredItems() { return true; }
    public GlobalPath<AbstractWebNode> getGlobalPath()
    {
        return calculated == null ? WebFinder.getWebFinder().calculate(this)
            : new GlobalPath<>(calculated.getNodes());
    }
    public static WebPathQueryBuilder builder() { return new WebPathQueryBuilder(); }
    public Tile getFrom() { return from; }
    public Tile getTo() { return to; }
    public boolean isUseBankCache() { return useBankCache; }
    public List<Item> getWithItems() { return Collections.unmodifiableList(withItems); }
    public WebPathResponse getCalculated() { return calculated; }
    public void setFrom(Tile value) { from = value; }
    public void setTo(Tile value) { to = value; }
    public void setUseBankCache(boolean value) { useBankCache = value; }
    public void setWithItems(List<Item> values)
    {
        withItems.clear(); if (values != null) withItems.addAll(values);
    }
    public void setCalculated(WebPathResponse value) { calculated = value; }

    public static class WebPathQueryBuilder
    {
        private final WebPathQuery query = new WebPathQuery();
        public WebPathQueryBuilder from(Tile value) { query.from = value; return this; }
        public WebPathQueryBuilder to(Tile value) { query.to = value; return this; }
        public WebPathQueryBuilder useBankCache(boolean value) { query.useBankCache = value; return this; }
        public WebPathQueryBuilder withItem(Item value)
        {
            if (value != null) query.withItems.add(value); return this;
        }
        public WebPathQueryBuilder withItems(Collection<? extends Item> values)
        {
            if (values != null) query.withItems.addAll(values); return this;
        }
        public WebPathQueryBuilder clearWithItems() { query.withItems.clear(); return this; }
        public WebPathQueryBuilder calculated(WebPathResponse value) { query.calculated = value; return this; }
        public WebPathQuery build()
        {
            WebPathQuery result = new WebPathQuery();
            result.from = query.from; result.to = query.to; result.useBankCache = query.useBankCache;
            result.withItems.addAll(query.withItems); result.calculated = query.calculated;
            return result;
        }
        @Override public String toString() { return "WebPathQuery{" + query.from + " -> " + query.to + '}'; }
    }
}
