package com.leonardobishop.moneypouch.title;

import org.bukkit.entity.Player;

public class TitleHandler {
    public void sendTitle(Player player, String message, String submessage) {
        player.sendTitle(message, submessage, 0, 50, 20);
    }
}