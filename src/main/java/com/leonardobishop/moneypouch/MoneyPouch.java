package com.leonardobishop.moneypouch;

import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import com.leonardobishop.moneypouch.commands.BaseCommand;
import com.leonardobishop.moneypouch.economytype.EconomyType;
import com.leonardobishop.moneypouch.economytype.VaultEconomyType;
import com.leonardobishop.moneypouch.economytype.XPEconomyType;
import com.leonardobishop.moneypouch.events.UseEvent;
import com.leonardobishop.moneypouch.title.TitleHandler;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MoneyPouch extends JavaPlugin {

    private TitleHandler titleHandler;

    private final HashMap<String, EconomyType> economyTypes = new HashMap<>();

    private final ArrayList<Pouch> pouches = new ArrayList<>();

    @Override
    public void onEnable() {
        File directory = new File(String.valueOf(this.getDataFolder()));
        if (!directory.exists() && !directory.isDirectory()) {
            directory.mkdir();
        }

        File config = new File(this.getDataFolder() + File.separator + "config.yml");
        if (!config.exists()) {
            try {
                config.createNewFile();
                try (InputStream in = MoneyPouch.class.getClassLoader().getResourceAsStream("config.yml")) {
                    OutputStream out = new FileOutputStream(config);
                    ByteStreams.copy(in, out);
                } catch (IOException e) {
                    super.getLogger().severe("Failed to create config.");
                    e.printStackTrace();
                    super.getLogger().severe(ChatColor.RED + "...please delete the MoneyPouch directory and try RESTARTING (not reloading).");
                }
            } catch (IOException e) {
                super.getLogger().severe("Failed to create config.");
                e.printStackTrace();
                super.getLogger().severe(ChatColor.RED + "...please delete the MoneyPouch directory and try RESTARTING (not reloading).");
            }
        }
        this.reloadConfig();
        this.setupTitle();


        super.getServer().getPluginCommand("moneypouch").setExecutor(new BaseCommand(this));
        super.getServer().getPluginManager().registerEvents(new UseEvent(this), this);
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

        pouches.clear();
        for (String s : this.getConfig().getConfigurationSection("pouches.tier").getKeys(false)) {
            ItemStack is = getItemStack("pouches.tier." + s, this.getConfig());
            String economyTypeId = this.getConfig().getString("pouches.tier." + s + ".options.economytype", "VAULT");
            long priceMin = this.getConfig().getLong("pouches.tier." + s + ".pricerange.from", 0);
            long priceMax = this.getConfig().getLong("pouches.tier." + s + ".pricerange.to", 0);

            EconomyType economyType = getEconomyType(economyTypeId);
            if (economyType == null) economyType = getEconomyType("VAULT");

            pouches.add(new Pouch(s.replace(" ", "_"), priceMin, priceMax, is, economyType));
        }
    }

    private EconomyType getEconomyType(String id) {
        switch (id.toLowerCase()) {
            case "vault":
                if (!economyTypes.containsKey("Vault")) economyTypes.put("Vault", new VaultEconomyType(
                        this.getConfig().getString("economy.prefixes.vault", "$"),
                        this.getConfig().getString("economy.suffixes.vault", "")));
                return economyTypes.get("Vault");
            case "xp":
                if (!economyTypes.containsKey("XP")) economyTypes.put("XP", new XPEconomyType(
                        this.getConfig().getString("economy.prefixes.xp", ""),
                        this.getConfig().getString("economy.suffixes.xp", " XP")));
                return economyTypes.get("XP");
            default:
                return null;
        }
    }

    public String getMessage(Message message) {
        return ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("messages."
                + message.getId(), message.getDef()));
    }

    public ArrayList<Pouch> getPouches() {
        return pouches;
    }

    public TitleHandler getTitleHandle() {
        return titleHandler;
    }

    public String color(String str) {
        return str == null ? null : ChatColor.translateAlternateColorCodes('&', str);
    }

    public ItemStack getItemStack(String path, FileConfiguration config) {

        String typeString = config.getString(path + ".item", path + ".item");
        if (Strings.isNullOrEmpty(typeString)) {
            getLogger().severe("Invalid material set for the pouches, cannot give.");
            return null;
        }

        Material type = Material.matchMaterial(typeString);
        if (type == null) {
            getLogger().severe("Invalid material set for the pouches, cannot give.");
            return null;
        }

        int data = 0;

        String name = config.getString(path + ".name", path + ".name");
        List<String> lore = config.getStringList(path + ".lore");

        lore = lore.stream()
                .map(this::color)
                .collect(Collectors.toList());
        name = color(name);

        ItemStack item = new ItemStack(type, 1, (short) data);

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            getLogger().severe("Could not create pouch. Bukkit failed to provide clean ItemMeta.");
            return null;
        }

        meta.setLore(lore);
        meta.setDisplayName(name);
        item.setItemMeta(meta);

        if (config.isSet(path + ".enchantments")) {
            for (String key : getConfig().getStringList(path + ".enchantments")) {

                String[] split = key.split(":");
                String enchantmentName = split[0];

                int level = 1;

                if (split.length > 1)
                    try {
                        level = Integer.parseInt(split[1]);
                    } catch (NumberFormatException e) {
                        getLogger().warning("Could not parse enchantment level for " + enchantmentName);
                    }

                Enchantment enchantment = Enchantment.getByKey(new NamespacedKey(this, enchantmentName));
                if (enchantment == null) {
                    getLogger().warning("Could not parse enchantment " + enchantmentName);
                    continue;
                }

                item.addUnsafeEnchantment(enchantment, level);
            }
        }

        return item;
    }

    private void setupTitle() {
        this.titleHandler = new TitleHandler();
    }

    public enum Message {

        FULL_INV("full-inv", "&c%player%'s inventory is full!"),
        GIVE_ITEM("give-item", "&6Given &e%player% %item%&6."),
        RECEIVE_ITEM("receive-item", "&6You have been given %item%&6."),
        PRIZE_MESSAGE("prize-message", "&6You have received &c%prefix%%prize%%suffix%&6!"),
        ALREADY_OPENING("already-opening", "&cPlease wait for your current pouch opening to complete first!");

        private String id;
        private String def; // (default message if undefined)

        Message(String id, String def) {
            this.id = id;
            this.def = def;
        }

        public String getId() {
            return id;
        }

        public String getDef() {
            return def;
        }
    }

}
