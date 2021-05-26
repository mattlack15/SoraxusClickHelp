package com.soraxus.clickhelp.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class NMSUtil {
    public static Class<?> getClass(String classname) {
        try {
            String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
            String path = classname.replace("{nms}", "net.minecraft.server." + version)
                    .replace("{nm}", "net.minecraft." + version)
                    .replace("{cb}", "org.bukkit.craftbukkit.." + version);
            return Class.forName(path);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    private static String serverVersion;
    private static Method getHandle;
    private static Method getNBTTag;
    private static Class<?> nmsEntityClass;
    private static Class<?> nbtTagClass;
    private static Method c;
    private static Method setInt;
    private static Method f;
    private static Method getHandleChunk;

    private static void getServerVersion() {
        if (serverVersion == null) {
            String name = Bukkit.getServer().getClass().getName();
            String[] parts = name.split("\\.");
            serverVersion = parts[3];
        }
    }

    public static void sendTitle(Player player, String text){
        sendTitle(player, text, 5, 60, 5, ChatColor.WHITE);
    }

    /**
     * Send a title to player
     * @param player Player to send the title to
     * @param text The text displayed in the title
     * @param fadeInTime The time the title takes to fade in
     * @param showTime The time the title is displayed
     * @param fadeOutTime The time the title takes to fade out
     * @param color The color of the title
     */
    public static void sendTitle(Player player, String text, int fadeInTime, int showTime, int fadeOutTime, ChatColor color)
    {
        try
        {
            Object chatTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\": \"" + text + "\",color:" + color.name().toLowerCase() + "}");

            Constructor<?> titleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"), int.class, int.class, int.class);
            Object packet = titleConstructor.newInstance(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE").get(null), chatTitle, fadeInTime, showTime, fadeOutTime);

            sendPacket(player, packet);
        }

        catch (Exception ex)
        {
            //Do something
        }
    }

    private static void sendPacket(Player player, Object packet)
    {
        try
        {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
        }
        catch(Exception ex)
        {
            //Do something
        }
    }

    /**
     * Get NMS class using reflection
     * @param name Name of the class
     * @return Class
     */
    private static Class<?> getNMSClass(String name)
    {
        try
        {
            return Class.forName("net.minecraft.server" + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + "." + name);
        }
        catch(ClassNotFoundException ex)
        {
            //Do something
        }
        return null;
    }

    private static void getGetHandleEntity() {
        getServerVersion();
        try {
            if (getHandle == null) {
                Class<?> craftEntity = Class.forName("org.bukkit.craftbukkit." + serverVersion + ".entity.CraftEntity");
                getHandle = craftEntity.getDeclaredMethod("getHandle");
                getHandle.setAccessible(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void getGetHandleChunk() {
        getServerVersion();
        try {
            if (getHandleChunk == null) {
                Class<?> craftChunk = Class.forName("org.bukkit.craftbukkit." + serverVersion + ".CraftChunk");
                getHandleChunk = craftChunk.getDeclaredMethod("getHandle");
                getHandleChunk.setAccessible(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setChunkMustSave(Chunk chunk, boolean mustSave) {
        getGetHandleChunk();
        try {
            Object nmsChunk = getHandleChunk.invoke(chunk);
            Field field = nmsChunk.getClass().getDeclaredField("mustSave");
            field.setAccessible(true);
            field.set(nmsChunk, mustSave);
            field.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    Not done yet (not even started)
     */
    public static void unloadChunk(Chunk chunk, boolean save) {
        //TODO
    }

    @Deprecated
    public static void setTagInt(Entity entity, String tagName, int value) {
        try {
            getGetHandleEntity();
            Object nmsEntity = getHandle.invoke(entity);
            if (nmsEntityClass == null) {
                nmsEntityClass = Class.forName("net.minecraft.server." + serverVersion + ".Entity");
            }
            if (getNBTTag == null) {
                getNBTTag = nmsEntityClass.getDeclaredMethod("getNBTTag");
                getNBTTag.setAccessible(true);
            }
            Object tag = getNBTTag.invoke(nmsEntity);
            if (nbtTagClass == null) {
                nbtTagClass = Class.forName("net.minecraft.server." + serverVersion + ".NBTTagCompound");
            }
            if (tag == null) {
                tag = nbtTagClass.newInstance();
            }
            if (c == null) {
                c = nmsEntityClass.getDeclaredMethod("c", nbtTagClass);
                c.setAccessible(true);
            }
            c.invoke(nmsEntity, tag);
            if (setInt == null) {
                setInt = nbtTagClass.getDeclaredMethod("setInt", String.class, Integer.TYPE);
                setInt.setAccessible(true);
            }
            setInt.invoke(tag, tagName, value);
            if (f == null) {
                f = nmsEntityClass.getDeclaredMethod("f", nbtTagClass);
                f.setAccessible(true);
            }
            f.invoke(nmsEntity, tag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setEntitySilent(Entity entity, boolean b) {
        try {
            if (serverVersion == null) {
                String name = Bukkit.getServer().getClass().getName();
                String[] parts = name.split("\\.");
                serverVersion = parts[3];
            }
            if (getHandle == null) {
                Class<?> craftEntity = Class.forName("org.bukkit.craftbukkit." + serverVersion + ".entity.CraftEntity");
                getHandle = craftEntity.getDeclaredMethod("getHandle");
                getHandle.setAccessible(true);
            }
            Object nmsEntity = getHandle.invoke(entity);
            if (nmsEntityClass == null) {
                nmsEntityClass = Class.forName("net.minecraft.server." + serverVersion + ".Entity");
            }
            nmsEntityClass.getDeclaredMethod("b", boolean.class).invoke(nmsEntity, b);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Object getNmsPlayer(Player p) throws Exception {
        Method getHandle = p.getClass().getMethod("getHandle");
        return getHandle.invoke(p);
    }

    public static Object getNmsScoreboard(Scoreboard s) throws Exception {
        Method getHandle = s.getClass().getMethod("getHandle");
        return getHandle.invoke(s);
    }

    public static Object getFieldValue(Object instance, String fieldName) throws Exception {
        Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(instance);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Field field, Object obj) {
        try {
            return (T) field.get(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Field getField(Class<?> clazz, String fieldName) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }

    public static void setValue(Object instance, String field, Object value) {
        try {
            Field f = instance.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(instance, value);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void sendAllPacket(Object packet) throws Exception {
        for (Player p : Bukkit.getOnlinePlayers()) {
            Object nmsPlayer = getNmsPlayer(p);
            Object connection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
            connection.getClass().getMethod("sendPacket", NMSUtil.getClass("{nms}.Packet")).invoke(connection, packet);
        }
    }

    public static void sendListPacket(List<String> players, Object packet) {
        try {
            for (String name : players) {
                Object nmsPlayer = getNmsPlayer(Bukkit.getPlayer(name));
                Object connection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
                connection.getClass().getMethod("sendPacket", NMSUtil.getClass("{nms}.Packet")).invoke(connection, packet);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void sendPlayerPacket(Player p, Object packet) throws Exception {
        Object nmsPlayer = getNmsPlayer(p);
        Object connection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
        connection.getClass().getMethod("sendPacket", NMSUtil.getClass("{nms}.Packet")).invoke(connection, packet);
    }
}