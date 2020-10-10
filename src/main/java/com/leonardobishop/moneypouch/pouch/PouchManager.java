package com.leonardobishop.moneypouch.pouch;

import com.google.common.base.Strings;
import com.leonardobishop.moneypouch.PouchPlugin;
import com.leonardobishop.moneypouch.StringUtil;
import com.leonardobishop.moneypouch.economytype.EconomyType;
import com.leonardobishop.moneypouch.economytype.VaultEconomyType;
import com.leonardobishop.moneypouch.economytype.XPEconomyType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PouchManager {

    private final PouchPlugin plugin;

    private final Map<String, EconomyType> economyTypes = new HashMap<>();

    private final Map<String, Pouch> loadedPouches = new HashMap<>();

    public PouchManager(PouchPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {

        this.loadedPouches.clear();

        FileConfiguration config = plugin.getConfig();

        for (String s : config.getConfigurationSection("pouches.tier").getKeys(false)) {

            ItemStack item = loadItemStack("pouches.tier." + s, config);

            String economyTypeId = config.getString("pouches.tier." + s + ".options.economytype", "VAULT");

            long priceMin = config.getLong("pouches.tier." + s + ".pricerange.from", 0);
            long priceMax = config.getLong("pouches.tier." + s + ".pricerange.to", 0);

            EconomyType economyType = loadEconomyType(economyTypeId);

            if (economyType == null)
                economyType = loadEconomyType("VAULT");

            String id = s.replace(" ", "_");

            Pouch pouch = new Pouch(id, priceMin, priceMax, item, economyType);

            loadedPouches.put(id, pouch);
        }
    }

    private EconomyType loadEconomyType(String id) {
        switch (id.toLowerCase()) {
            case "vault":
                if (!economyTypes.containsKey("Vault")) economyTypes.put("Vault", new VaultEconomyType(
                        plugin.getConfig().getString("economy.prefixes.vault", "$"),
                        plugin.getConfig().getString("economy.suffixes.vault", "")));
                return economyTypes.get("Vault");
            case "xp":
                if (!economyTypes.containsKey("XP")) economyTypes.put("XP", new XPEconomyType(
                        plugin.getConfig().getString("economy.prefixes.xp", ""),
                        plugin.getConfig().getString("economy.suffixes.xp", " XP")));
                return economyTypes.get("XP");
            default:
                return null;
        }
    }

    public ItemStack loadItemStack(String path, FileConfiguration config) {

        String typeString = config.getString(path + ".item", path + ".item");
        if (Strings.isNullOrEmpty(typeString)) {
            plugin.getLogger().severe("Invalid material set for the pouches, cannot give.");
            return null;
        }

        Material type = Material.matchMaterial(typeString);
        if (type == null) {
            plugin.getLogger().severe("Invalid material set for the pouches, cannot give.");
            return null;
        }

        int data = 0;

        String name = config.getString(path + ".name", path + ".name");
        List<String> lore = config.getStringList(path + ".lore");

        lore = lore.stream()
                .map(StringUtil::color)
                .collect(Collectors.toList());
        name = StringUtil.color(name);

        ItemStack item = new ItemStack(type, 1, (short) data);

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            plugin.getLogger().severe("Could not create pouch. Bukkit failed to provide clean ItemMeta.");
            return null;
        }

        meta.setLore(lore);
        meta.setDisplayName(name);
        item.setItemMeta(meta);

        if (config.isSet(path + ".enchantments")) {
            for (String key : plugin.getConfig().getStringList(path + ".enchantments")) {

                String[] split = key.split(":");
                String enchantmentName = split[0];

                int level = 1;

                if (split.length > 1)
                    try {
                        level = Integer.parseInt(split[1]);
                    } catch (NumberFormatException e) {
                        plugin.getLogger().warning("Could not parse enchantment level for " + enchantmentName);
                    }

                Enchantment enchantment = Enchantment.getByKey(new NamespacedKey(plugin, enchantmentName));
                if (enchantment == null) {
                    plugin.getLogger().warning("Could not parse enchantment " + enchantmentName);
                    continue;
                }

                item.addUnsafeEnchantment(enchantment, level);
            }
        }

        return item;
    }

    public Pouch getPouch(String id) {
        return this.loadedPouches.get(id);
    }

    public Map<String, Pouch> getLoadedPouches() {
        return Collections.unmodifiableMap(loadedPouches);
    }

    public Set<Pouch> getPouches() {
        return Collections.unmodifiableSet(new HashSet<>(this.loadedPouches.values()));
    }
}