package com.leonardobishop.moneypouch.version;

import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class VersionUtil {

    private final VersionProvider defaultProvider = new Version1_16("1_16");

    private final List<VersionBase> registeredProviders = new ArrayList<>() {{
        add(new Version1_16("1_16"));
    }};

    private final String serverVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].substring(1);
    private final VersionProvider currentProvider = findProvider();

    private VersionProvider findProvider() {
        for (VersionBase base : registeredProviders) {
            if (base.supportsVersion(serverVersion)) return base;
        }
        return defaultProvider;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public VersionProvider getCurrentProvider() {
        return currentProvider;
    }
}