package com.leonardobishop.moneypouch.economytype;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class XPEconomyType extends EconomyType {

    public XPEconomyType(String prefix, String suffix) {
        super(prefix, suffix);
    }

    @Override
    public void processPayment(Player player, long amount) {
        if (!player.isOnline()) {
            Bukkit.getLogger().warning("Player is offline!");
            return;
        }

        player.giveExp(Integer.parseInt(String.valueOf(amount)));
    }

    @Override
    public String toString() {
        return "XP";
    }
}