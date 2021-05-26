package com.soraxus.clickhelp.util;

import org.bukkit.event.entity.EntityDamageEvent;

import java.util.ArrayList;
import java.util.UUID;

public class InvulnEntities {
    private static volatile ArrayList<UUID> ids = new ArrayList<>();

    public InvulnEntities(){
        EventSubscriptions.instance.subscribe(this);
    }

    public static ArrayList<UUID> getEntities(){
        return ids;
    }

    public static void addEntity(UUID id){
        ids.add(id);
    }

    public static void removeEntity(UUID id){
        ids.remove(id);
    }

    @EventSubscription
    private void onDamage(EntityDamageEvent event){
        if(ids.contains(event.getEntity().getUniqueId()))
            event.setCancelled(true);
    }
}
