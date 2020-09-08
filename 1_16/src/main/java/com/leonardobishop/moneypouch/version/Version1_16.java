package com.leonardobishop.moneypouch.version;

import org.bukkit.entity.Player;

public class Version1_16 extends VersionBase {

    public Version1_16(String version, String... redirects) {
        super(version, redirects);
    }

    @Override
    public void sendTitle(Player player, String title, String subTitle) {
        sendTitle(player, title, subTitle, 0, 50, 0);
    }

    @Override
    public void sendTitle(Player player, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(title, subTitle, fadeIn, stay, fadeOut);
    }
}