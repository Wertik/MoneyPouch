package com.leonardobishop.moneypouch;

import com.google.common.base.Strings;
import org.bukkit.ChatColor;

public class StringUtil {

    public static String color(String str) {
        return Strings.isNullOrEmpty(str) ? str : ChatColor.translateAlternateColorCodes('&', str);
    }
}
