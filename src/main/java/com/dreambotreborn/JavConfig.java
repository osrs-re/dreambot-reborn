package com.dreambotreborn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import com.dreambotreborn.settings.PreferredWorld;
import net.runelite.api.ClientConfiguration;

final class JavConfig implements ClientConfiguration
{
    private final URL codeBase;
    private final String initialClass;
    private final Map<String, String> parameters;

    private JavConfig(URL codeBase, String initialClass, Map<String, String> parameters)
    {
        this.codeBase = codeBase;
        this.initialClass = initialClass;
        this.parameters = parameters;
    }

    static JavConfig fetch(URI uri) throws IOException
    {
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setConnectTimeout(10_000);
        connection.setReadTimeout(10_000);
        connection.setRequestProperty("User-Agent", "dreambot-reborn/1.0");

        Map<String, String> properties = new LinkedHashMap<>();
        Map<String, String> parameters = new LinkedHashMap<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
            connection.getInputStream(), StandardCharsets.ISO_8859_1)))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                int separator = line.indexOf('=');
                if (separator < 0)
                {
                    continue;
                }

                String key = line.substring(0, separator);
                String value = line.substring(separator + 1);

                if ("param".equals(key))
                {
                    int parameterSeparator = value.indexOf('=');
                    if (parameterSeparator >= 0)
                    {
                        parameters.put(
                            value.substring(0, parameterSeparator),
                            value.substring(parameterSeparator + 1));
                    }
                }
                else if (!"msg".equals(key))
                {
                    properties.put(key, value);
                }
            }
        }
        finally
        {
            connection.disconnect();
        }

        String codeBase = require(properties, "codebase");
        String initialClass = require(properties, "initial_class");
        if (initialClass.endsWith(".class"))
        {
            initialClass = initialClass.substring(0, initialClass.length() - ".class".length());
        }

        return new JavConfig(
            new URL(codeBase),
            initialClass,
            Collections.unmodifiableMap(parameters));
    }

    String getInitialClass()
    {
        return initialClass;
    }

    JavConfig withPreferredWorld(int worldId) throws IOException
    {
        if (worldId == 0)
        {
            return this;
        }
        if (worldId < 301 || worldId > 999)
        {
            throw new IOException("Invalid preferred world " + worldId);
        }

        URL preferredCodeBase = new URL(
            codeBase.getProtocol(),
            PreferredWorld.worldHost(worldId),
            codeBase.getPort(),
            "/");
        Map<String, String> preferredParameters = new LinkedHashMap<>(parameters);
        // The current official Jagex jav_config uses parameter 12 as world id.
        preferredParameters.put("12", Integer.toString(worldId));
        return new JavConfig(
            preferredCodeBase,
            initialClass,
            Collections.unmodifiableMap(preferredParameters));
    }

    @Override
    public URL getCodeBase()
    {
        return codeBase;
    }

    @Override
    public String getParameter(String key)
    {
        return parameters.get(key);
    }

    @Override
    public void onError(String code)
    {
        System.err.println("Old School RuneScape client error: " + code);
    }

    private static String require(Map<String, String> values, String key) throws IOException
    {
        String value = values.get(key);
        if (value == null || value.isEmpty())
        {
            throw new IOException("jav_config is missing " + key);
        }
        return value;
    }
}
