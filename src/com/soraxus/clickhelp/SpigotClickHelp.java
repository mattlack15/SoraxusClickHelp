package com.soraxus.clickhelp;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Function;

public class SpigotClickHelp extends JavaPlugin implements Listener {

    public static SpigotClickHelp instance;

    public static final String HELP_SECTION = "help";

    public static String PLUGIN_PREFIX = "";

    public static boolean usingGui = false;

    private boolean a;

    @Getter
    private HelpSection helpSection = new HelpSection();

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        PLUGIN_PREFIX = getConfig().getString("plugin-prefix");
        a = getConfig().getBoolean("reroute-help-command");
        this.reloadHelp();
        Bukkit.getPluginManager().registerEvents(instance, instance);
        new CommandHelp();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCmd(PlayerCommandPreprocessEvent event){
        if(!a)
            return;
        if(event.getMessage().equalsIgnoreCase("/help")){
            event.setMessage("/clickhelp");
            event.setCancelled(false);
        }
    }

    public void reloadHelp() {
        this.reloadConfig();
        ConfigurationSection section = getConfig().getConfigurationSection(HELP_SECTION);
        usingGui = getConfig().getBoolean("use-gui");
        Recursive<Function<ConfigurationSection, HelpSection>> parser = new Recursive<>();
        parser.func = (conf) -> {

            HelpSection hSection = new HelpSection();

            if (conf.isString("display-name"))
                hSection.setDisplayName(conf.getString("display-name"));

            if (conf.isList("messages"))
                for (String strs : conf.getStringList("messages"))
                    hSection.addObject(new HelpMessage(strs));

            for (String keys : conf.getKeys(false)) {
                if (conf.isConfigurationSection(keys)) {
                    hSection.addObject(parser.func.apply(conf.getConfigurationSection(keys)));
                } else if(conf.isString(keys)){
                    hSection.getData().put(keys, conf.getString(keys));
                } else if(conf.isInt(keys)){
                    hSection.getData().put(keys, conf.getInt(keys) + "");
                }
            }
            return hSection;
        };
        helpSection = parser.func.apply(section);
    }
}
