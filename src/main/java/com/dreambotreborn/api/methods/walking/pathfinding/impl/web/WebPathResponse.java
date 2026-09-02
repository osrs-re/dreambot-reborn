package com.dreambotreborn.api.methods.walking.pathfinding.impl.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.dreambotreborn.api.methods.walking.web.node.AbstractWebNode;

public class WebPathResponse
{
    private WebPathQuery query;
    private final List<AbstractWebNode> nodes = new ArrayList<>();
    public WebPathQuery getQuery() { return query; }
    public boolean haveAllRequiredItems(boolean bankCache) { return true; }
    public static WebPathResponseBuilder builder() { return new WebPathResponseBuilder(); }
    public List<AbstractWebNode> getNodes() { return Collections.unmodifiableList(nodes); }
    public void setQuery(WebPathQuery value) { query = value; }

    public static class WebPathResponseBuilder
    {
        private final WebPathResponse response = new WebPathResponse();
        public WebPathResponseBuilder query(WebPathQuery value) { response.query = value; return this; }
        public WebPathResponseBuilder nodes(java.util.Collection<? extends AbstractWebNode> values)
        {
            response.nodes.clear(); if (values != null) response.nodes.addAll(values); return this;
        }
        public WebPathResponse build() { return response; }
    }
}
