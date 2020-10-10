package com.leonardobishop.moneypouch;

import com.leonardobishop.moneypouch.commands.BaseCommand;
import com.leonardobishop.moneypouch.events.UseEvent;
import com.leonardobishop.moneypouch.pouch.PouchManager;
import com.leonardobishop.moneypouch.title.TitleHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class PouchPlugin extends JavaPlugin {

    private PouchManager pouchManager;

    private TitleHandler titleHandler;

    public static PouchPlugin getInstance() {
        return getPlugin(PouchPlugin.class);
    }

    @Override
    public void onEnable() {
        File directory = new File(String.valueOf(this.getDataFolder()));

        if (!directory.exists() && !directory.isDirectory()) {
            directory.mkdir();
        }

        File config = new File(this.getDataFolder() + File.separator + "config.yml");

        if (!config.exists()) {
            saveResource("config.yml", false);
        }

        reloadConfig();

        this.pouchManager = new PouchManager(this);
        this.pouchManager.load();

        this.setupTitle();

        super.getServer().getPluginCommand("moneypouch").setExecutor(new BaseCommand(this));
        super.getServer().getPluginManager().registerEvents(new UseEvent(this), this);
    }

    public void reload() {
        this.reloadConfig();
        this.pouchManager.load();
    }

    public TitleHandler getTitleHandle() {
        return titleHandler;
    }

    public PouchManager getPouchManager() {
        return pouchManager;
    }

    private void setupTitle() {
        this.titleHandler = new TitleHandler();
    }
}