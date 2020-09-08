package com.leonardobishop.moneypouch.version;

import org.bukkit.entity.Player;

public interface VersionProvider {
    void sendTitle(Player player, String title, String subTitle);

    void sendTitle(Player player, String title, String subTitle, int fadeIn, int stay, int fadeOut);
}