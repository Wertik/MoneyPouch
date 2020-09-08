package com.leonardobishop.moneypouch.version;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class VersionBase implements VersionProvider {

    private final String version;
    private final List<String> redirects = new ArrayList<>();

    public VersionBase(String version, String... redirects) {
        this.version = version;
        this.redirects.addAll(Arrays.asList(redirects));
    }

    public boolean supportsVersion(String version) {
        if (version.startsWith(this.version)) return true;
        return redirects.stream().anyMatch(version::startsWith);
    }

    public String getVersion() {
        return version;
    }

    public List<String> getRedirects() {
        return redirects;
    }
}