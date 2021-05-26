package com.soraxus.clickhelp.util;

import org.bukkit.entity.ArmorStand;

public interface ArmorStandTextHolder {
    void removeStand();
    void createStand(String text);
    ArmorStand getStand();
    void setText(String text);
}
