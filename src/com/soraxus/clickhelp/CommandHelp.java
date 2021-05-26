package com.soraxus.clickhelp;

import com.soraxus.clickhelp.gui.MenuHelpSection;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class CommandHelp extends GravCommand {
    @Override
    public String getDescription() {
        return "Help command";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("clickhelp");
    }

    @Override
    public String getPermission() {
        return "";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length == 0){
            sendHelpSection(sender, SpigotClickHelp.instance.getHelpSection());
        } else {
            try{
                if(args[0].equalsIgnoreCase("reload") && sender.hasPermission("clickhelp.reload")){
                    SpigotClickHelp.instance.reloadHelp();
                    return sendErrorMessage(sender, SpigotClickHelp.PLUGIN_PREFIX + "&aHelp Sections reloaded!");
                }
                UUID id = UUID.fromString(args[0]);
                HelpSection section = SpigotClickHelp.instance.getHelpSection().findSection(id);
                if(section == null)
                    return sendErrorMessage(sender, SpigotClickHelp.PLUGIN_PREFIX + "&cInvalid help section ID");
                sendHelpSection(sender, section);
            } catch (Exception e){
                return sendErrorMessage(sender, SpigotClickHelp.PLUGIN_PREFIX + "&cInvalid help section ID");
            }
        }
        return true;
    }

    private void sendHelpSection(CommandSender sender, HelpSection section){
        if(sender instanceof Player && SpigotClickHelp.usingGui){
            new MenuHelpSection(((Player) sender).getUniqueId(), section).open((Player)sender);
        } else {
            for (HelpSectionObject object : section.getObjects()) {
                if (object instanceof HelpMessage) {
                    sendErrorMessage(sender, ((HelpMessage) object).getMessage());
                } else if (object instanceof HelpSection) {
                    String displayName = ChatColor.translateAlternateColorCodes('&', ((HelpSection) object).getDisplayName());
                    if (sender instanceof Player) {
                        TextComponent component = ComponentUtil.getClickHoverComponent(displayName, "Click to view help for " + displayName, ClickEvent.Action.RUN_COMMAND, "/clickhelp " + ((HelpSection) object).getId());
                        ((Player) sender).spigot().sendMessage(component);
                    } else {
                        sendErrorMessage(sender, ((HelpSection) object).getDisplayName());
                    }
                }
            }
        }
    }
}
