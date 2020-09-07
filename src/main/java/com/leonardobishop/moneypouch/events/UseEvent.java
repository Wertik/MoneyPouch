package com.leonardobishop.moneypouch.events;

import com.leonardobishop.moneypouch.MoneyPouch;
import com.leonardobishop.moneypouch.Pouch;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class UseEvent implements Listener {

    private final MoneyPouch plugin;
    private final ArrayList<UUID> opening = new ArrayList<>();

    public UseEvent(MoneyPouch plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }

        if (player.getInventory().getItemInMainHand().getType() == Material.AIR && player.getInventory().getItemInOffHand().getType() == Material.AIR) {
            return;
        }

        for (Pouch p : plugin.getPouches()) {

            if (opening.contains(player.getUniqueId())) {
                player.sendMessage(plugin.getMessage(MoneyPouch.Message.ALREADY_OPENING));
                return;
            }

            EquipmentSlot slot = detectSlot(player, p.getItemStack());

            if (slot == null) continue;

            event.setCancelled(true);

            consume(slot, player);

            usePouch(player, p);
        }
    }

    private void consume(EquipmentSlot slot, Player player) {
        ItemStack item = player.getInventory().getItem(slot);
        if (item.getAmount() == 1)
            player.getInventory().setItem(slot, null);
        else item.setAmount(item.getAmount() - 1);

        player.updateInventory();
    }

    private EquipmentSlot detectSlot(Player player, ItemStack item) {
        if (player.getInventory().getItemInMainHand().isSimilar(item))
            return EquipmentSlot.HAND;
        else if (player.getInventory().getItemInOffHand().isSimilar(item))
            return EquipmentSlot.OFF_HAND;
        else return null;
    }

    private void usePouch(Player player, Pouch p) {
        opening.add(player.getUniqueId());
        long random = ThreadLocalRandom.current().nextLong(p.getMinRange(), p.getMaxRange());

        player.playSound(player.getLocation(), Sound.valueOf(plugin.getConfig().getString("pouches.sound.opensound")), 3, 1);

        new BukkitRunnable() {
            int position = 0;
            final String prefixColour = ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("pouches.title.prefix-colour"));
            final String suffixColour = ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("pouches.title.suffix-colour"));
            final String revealColour = ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("pouches.title.reveal-colour"));
            final String obfuscateColour = ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("pouches.title.obfuscate-colour"));
            final String obfuscateDigitChar = plugin.getConfig().getString("pouches.title.obfuscate-digit-char", "#");
            final String obfuscateDelimiterChar = ",";
            final boolean delimiter = plugin.getConfig().getBoolean("pouches.title.format.enabled", false);
            final boolean revealComma = plugin.getConfig().getBoolean("pouches.title.format.reveal-comma", false);
            final String number = (delimiter ? (new DecimalFormat("#,###").format(random)) : String.valueOf(random));
            final boolean reversePouchReveal = plugin.getConfig().getBoolean("reverse-pouch-reveal");

            @Override
            public void run() {
                if (!player.isOnline()) {
                    position = number.length();
                } else {
                    player.playSound(player.getLocation(), Sound.valueOf(plugin.getConfig().getString("pouches.sound.revealsound")), 3, 1);

                    String prefix = prefixColour + p.getEconomyType().getPrefix();
                    StringBuilder viewedTitle = new StringBuilder();
                    String suffix = suffixColour + p.getEconomyType().getSuffix();

                    for (int i = 0; i < position; i++) {
                        if (reversePouchReveal) {
                            viewedTitle.insert(0, number.charAt(number.length() - i - 1)).insert(0, revealColour);
                        } else {
                            viewedTitle.append(revealColour).append(number.charAt(i));
                        }
                        if ((i == (position - 1)) && (position != number.length())
                                && (reversePouchReveal
                                ? (revealComma && (number.charAt(number.length() - i - 1)) == ',')
                                : (revealComma && (number.charAt(i + 1)) == ','))) {
                            position++;
                        }
                    }

                    for (int i = position; i < number.length(); i++) {
                        if (reversePouchReveal) {
                            char at = number.charAt(number.length() - i - 1);
                            if (at == ',') {
                                if (revealComma) {
                                    viewedTitle.insert(0, at).insert(0, revealColour);
                                } else
                                    viewedTitle.insert(0, obfuscateDelimiterChar).insert(0, ChatColor.MAGIC).insert(0, obfuscateColour);
                            } else
                                viewedTitle.insert(0, obfuscateDigitChar).insert(0, ChatColor.MAGIC).insert(0, obfuscateColour);
                        } else {
                            char at = number.charAt(i);
                            if (at == ',') {
                                if (revealComma) viewedTitle.append(revealColour).append(at);
                                else
                                    viewedTitle.append(obfuscateColour).append(ChatColor.MAGIC).append(obfuscateDelimiterChar);
                            } else
                                viewedTitle.append(obfuscateColour).append(ChatColor.MAGIC).append(obfuscateDigitChar);
                        }
                    }

                    plugin.getTitleHandle().sendTitle(player, prefix + viewedTitle.toString() + suffix,
                            plugin.color(plugin.getConfig().getString("pouches.title.subtitle")));
                }

                if (position == number.length()) {
                    opening.remove(player.getUniqueId());
                    this.cancel();
                    if (player.isOnline()) {
                        player.playSound(player.getLocation(), Sound.valueOf(plugin.getConfig().getString("pouches.sound.endsound")), 3, 1);
                        player.sendMessage(plugin.getMessage(MoneyPouch.Message.PRIZE_MESSAGE)
                                .replace("%prefix%", p.getEconomyType().getPrefix())
                                .replace("%suffix%", p.getEconomyType().getSuffix())
                                .replace("%prize%", String.valueOf(random)));
                    }
                    try {
                        p.getEconomyType().processPayment(player, random);
                    } catch (Throwable t) {
                        plugin.getLogger().log(Level.SEVERE, "Failed to process payment for " + player.getName()
                                + " of amount " + random + " of economy " + p.getEconomyType().toString() + ", did they disconnect?");
                        t.printStackTrace();
                    }
                    return;
                }
                position++;
            }
        }.runTaskTimer(plugin, 10, plugin.getConfig().getInt("pouches.title.speed-in-tick"));
    }
}