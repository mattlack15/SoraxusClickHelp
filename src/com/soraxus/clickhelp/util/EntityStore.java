package com.soraxus.clickhelp.util;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.UUID;

public class EntityStore<T extends Entity> {
    private UUID uuid = null;
    private Class<? extends Entity> type = null;
    private String world;

    private boolean isNull = false;

    public static Entity getEntity(UUID id){
        for(World world : Bukkit.getWorlds()){
            for(Entity entity : world.getEntities()){
                if(entity.getUniqueId().equals(id)){
                    return entity;
                }
            }
        }
        return null;
    }

    public EntityStore(T entity){
        if(entity == null){
            isNull = true;
            return;
        }
        this.uuid = entity.getUniqueId();
        this.type = entity.getClass();
        this.world = entity.getWorld().getName();
    }

    public T getEntity(){
        if(isNull){
            return null;
        }

        try {
            World world = Bukkit.getWorld(this.world);

            if (world == null) {
                return (T) getEntity(this.uuid);
            }
            for (Entity ents : world.getEntitiesByClass(type)) {
                if (ents.getUniqueId().equals(this.uuid)) {
                    return (T) ents;
                }
            }
            return (T) getEntity(this.uuid);
        } catch (Exception e){
            return null;
        }

    }

    public UUID getUuid() {
        return uuid;
    }

    public Class<? extends Entity> getType(){
        return this.type;
    }
}
