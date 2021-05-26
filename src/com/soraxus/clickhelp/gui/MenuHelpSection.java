package com.soraxus.clickhelp.gui;

import com.soraxus.clickhelp.HelpMessage;
import com.soraxus.clickhelp.HelpSection;
import com.soraxus.clickhelp.HelpSectionObject;
import com.soraxus.clickhelp.util.ItemBuilder;
import com.soraxus.clickhelp.util.TextUtil;
import com.soraxus.clickhelp.util.menus.Menu;
import com.soraxus.clickhelp.util.menus.MenuElement;
import com.soraxus.clickhelp.util.menus.MenuManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

public class MenuHelpSection extends Menu {
    private UUID player;

    public MenuHelpSection(UUID player, HelpSection section) {
        super(section.getDisplayName(), 5);
        this.player = player;
        this.setup(section, 0);
    }

    public void setup(HelpSection section, int page) {
        this.setTitle(ChatColor.translateAlternateColorCodes('&', section.getDisplayName()));
        this.setAll(null);
        ArrayList<MenuElement> elements = new ArrayList<>();
        ArrayList<String> messages = new ArrayList<>();
        ItemBuilder infoBuilder = new ItemBuilder(Material.EMPTY_MAP, 1).setName(section.getDisplayName());
        int index = 0;
        for (HelpSectionObject object : section.getObjects()) {
            if(index < page * 8 /*amount of objects per page*/ ) {
                index++;
                continue;
            }
            if (object instanceof HelpSection && ((HelpSection) object).getData().containsKey("permission"))
                if (Bukkit.getPlayer(player) != null && !Bukkit.getPlayer(player).hasPermission(((HelpSection) object).getData().get("permission")))
                    continue;
            if (object instanceof HelpMessage)
                messages.add(((HelpMessage) object).getMessage());
            try {
                if (object instanceof HelpSection) {
                    ItemBuilder builder = new ItemBuilder(((HelpSection) object).getData().containsKey("material") ? Material.matchMaterial(((HelpSection) object).getData().get("material")) : Material.PAPER, 1,
                            ((HelpSection) object).getData().containsKey("data") ? Byte.parseByte(((HelpSection) object).getData().get("data")) : 0).setName(((HelpSection) object).getDisplayName());
                    boolean hasMore = false;
                    for (HelpSectionObject object1 : ((HelpSection) object).getObjects()) {
                        if (!(object1 instanceof HelpMessage))
                            hasMore = true;
                    }
                    ((HelpSection) object).getObjects().forEach(o -> {
                        if (o instanceof HelpMessage) {
                            int maxLineLength = 1000;
                            if (((HelpSection) object).getData().containsKey("gui-max-line-length")) {
                                maxLineLength = Integer.parseInt(((HelpSection) object).getData().get("gui-max-line-length"));
                            }
                            String lastColor = ChatColor.WHITE + "";
                            for (String lines : TextUtil.splitIntoLines(((HelpMessage) o).getMessage(), maxLineLength)) {
                                builder.addLore(lastColor + lines);
                                lastColor = ChatColor.getLastColors(ChatColor.translateAlternateColorCodes('&', lastColor + lines));
                            }
                        }
                        if (o instanceof HelpSection)
                            builder.addLore("&7 >> &f" + ((HelpSection) o).getDisplayName());
                    });

                    boolean finalHasMore = hasMore;
                    builder.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    elements.add(new MenuElement(builder.build()).setClickHandler((e, i) -> {
                        if (finalHasMore) {
                            setup((HelpSection) object, 0);
                        }
                    }));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            index++;
        }
        setElement(13, null);
        if (messages.size() != 0) {
            messages.forEach(m -> {
                int maxLineLength = 1000;
                if (section.getData().containsKey("gui-max-line-length")) {
                    maxLineLength = Integer.parseInt(section.getData().get("gui-max-line-length"));
                }
                String lastColor = ChatColor.WHITE + "";
                for (String lines : TextUtil.splitIntoLines(m, maxLineLength)) {
                    infoBuilder.addLore(lastColor + lines);
                    lastColor = ChatColor.getLastColors(ChatColor.translateAlternateColorCodes('&', lastColor + lines));
                }
            });
            infoBuilder.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            setElement(13, new MenuElement(infoBuilder.build()));
        }

        Iterator<MenuElement> it = elements.iterator();
        ArrayList<MenuElement> holder = new ArrayList<>();

        int row = 2;
        boolean hasMore = false;
        this.setRow(row, null);
        while (it.hasNext()) {
            holder.add(it.next());
            if (holder.size() == 4) {
                evenlyDistribute(row, holder.toArray(new MenuElement[0]));
                holder.clear();
                if (++row == this.getRows() - 1) {
                    hasMore = it.hasNext();
                    break;
                }
            }
        }
        evenlyDistribute(row, holder.toArray(new MenuElement[0]));

        if (section.getParent() != null)
            setElement(4, new MenuElement(new ItemBuilder(Material.BARRIER, 1).setName("&fBack").addLore("&7Takes you back a section").build()).setClickHandler((e, i) -> {
                setup(section.getParent(), 0);
            }));

        if (hasMore)
            setElement(9 * 5 - 1, new MenuElement(new ItemBuilder(Material.ARROW, 1).setName("&f&lNext Page").addLore("&7Takes you to the next page").build()).setClickHandler((e, i) -> {
                setup(section, page + 1);
            }));

        if(page > 0)
            setElement(9 * 4, new MenuElement(new ItemBuilder(Material.ARROW, 1).setName("&f&lPrevious Page").addLore("&7Takes you back a page").build()).setClickHandler((e, i) -> {
                setup(section, page - 1);
            }));

        this.fillElement(new MenuElement(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build()));
        MenuManager.instance.invalidateInvsForMenu(this);
    }
}
