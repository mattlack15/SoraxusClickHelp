package com.soraxus.clickhelp.util;

import com.google.common.collect.Lists;
import com.soraxus.clickhelp.SpigotClickHelp;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.RegisteredListener;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Objects;

public class EventSubscriptions implements Listener {

    private class DuelObjectClass<T> {

        WeakReference object;
        Class<?> clazz;

        DuelObjectClass(T object, Class<? extends T> clazz){
            this.object = new WeakReference<>(object);
            this.clazz = clazz;
        }

        @Override
        public boolean equals(Object o) {
            if(o == this){
                return true;
            }
            if(o instanceof DuelObjectClass){
                if(object.get() == null || ((DuelObjectClass) o).object.get() == null){
                    return false;
                }
                return Objects.equals(object.get(), ((DuelObjectClass) o).object.get()) && ((DuelObjectClass) o).clazz.equals(clazz);
            }
            return false;
        }
    }

    public static EventSubscriptions instance;

    private WeakList<Object> subscribedObjects = new WeakList<>();
    private ArrayList<DuelObjectClass> abstractObjects = new ArrayList<>();

    private RegisteredListener registeredListener = new RegisteredListener(EventSubscriptions.this, (listener, event) -> callMethods(event), EventPriority.HIGHEST, SpigotClickHelp.instance, false);

    public EventSubscriptions() {
        instance = this;

        //Register for all events
        this.registerEventsInPackage(Event.class.getPackage().getName(), Event.class.getClassLoader());

        new InvulnEntities();
    }

    public void registerEventsInPackage(String packageName, ClassLoader classLoader){
        for (Class<?> clazz : ReflectionUtil.getClassesInPackage(packageName, classLoader)) {
            if (Event.class.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers())) {
                try {
                    Method method = clazz.getDeclaredMethod("getHandlerList");
                    HandlerList list = (HandlerList) method.invoke(null);
                    list.register(registeredListener);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException ignored) {
                }
            }
        }
    }


    public synchronized void callMethods(Object e) {

        if (e instanceof PluginDisableEvent) {
            if (((PluginDisableEvent) e).getPlugin().equals(SpigotClickHelp.instance)) {
                this.onDisable();
            }
        }

        //Normal objects
        for (Object o : Lists.newArrayList(this.subscribedObjects.iterator())) { //.iterator() because WeakList doesn't currently support .toArray(), which newArrayList(Collection) uses
            Class<?> c = o.getClass();
            ArrayList<Method> methodsToCheck = Lists.newArrayList(c.getDeclaredMethods());
            for (Method meths : c.getMethods()) {
                if (!methodsToCheck.contains(meths)) {
                    methodsToCheck.add(meths);
                }
            }
            for (Method methods : methodsToCheck) {
                if (methods.isAnnotationPresent(EventSubscription.class)) {
                    if (methods.getParameterCount() > 0) {
                        Class<?> inType = methods.getParameterTypes()[0];

                        if (inType.isInstance(e) || inType.isAssignableFrom(e.getClass())) {
                            try {
                                methods.setAccessible(true);
                                methods.invoke(o, e);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }

        }

        this.abstractObjects.removeIf(d -> d.object.get() == null);

        //Abstract objects
        for (DuelObjectClass duel : Lists.newArrayList(this.abstractObjects.iterator())) {
            Class<?> c = duel.clazz;
            Object o = duel.object.get();
            if(o == null){
                this.abstractObjects.remove(duel);
                continue;
            }
            ArrayList<Method> methodsToCheck = Lists.newArrayList(c.getDeclaredMethods());
            for (Method meths : c.getMethods()) {
                if (!methodsToCheck.contains(meths)) {
                    methodsToCheck.add(meths);
                }
            }
            for (Method methods : methodsToCheck) {
                if (methods.isAnnotationPresent(EventSubscription.class)) {
                    if (methods.getParameterCount() > 0) {
                        Class<?> inType = methods.getParameterTypes()[0];
                        if (inType.isInstance(e) || inType.isAssignableFrom(e.getClass())) {
                            try {
                                methods.setAccessible(true);
                                methods.invoke(o, e);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }

        }

    }

    public boolean isSubscribed(Object o) {
        return this.subscribedObjects.contains(o);
    }

    public void onDisable() {

        for (Object o : Lists.newArrayList(this.subscribedObjects.iterator())) {
            Class<?> c = o.getClass();
            ArrayList<Method> methodsToCheck = Lists.newArrayList(c.getDeclaredMethods());
            for (Method meths : c.getMethods()) {
                if (!methodsToCheck.contains(meths)) {
                    methodsToCheck.add(meths);
                }
            }
            for (Method methods : methodsToCheck) {
                if (methods.isAnnotationPresent(OnDisable.class)) {
                    if (methods.getParameterCount() == 0) {
                        try {
                            methods.setAccessible(true);
                            methods.invoke(o);
                        } catch (IllegalAccessException ex) {
                            ex.printStackTrace();
                        } catch (InvocationTargetException ex) {
                            ex.getTargetException().printStackTrace();
                        }
                    }
                }
            }

        }
        //Abstract Objects

        this.abstractObjects.removeIf(d -> d.object.get() == null);

        for (DuelObjectClass duel : Lists.newArrayList(this.abstractObjects.iterator())) {
            Class<?> c = duel.clazz;
            Object o = duel.object.get();
            if(o == null){
                this.abstractObjects.remove(duel);
                continue;
            }
            ArrayList<Method> methodsToCheck = Lists.newArrayList(c.getDeclaredMethods());
            for (Method meths : c.getMethods()) {
                if (!methodsToCheck.contains(meths)) {
                    methodsToCheck.add(meths);
                }
            }
            for (Method methods : methodsToCheck) {
                if (methods.isAnnotationPresent(OnDisable.class)) {
                    if (methods.getParameterCount() == 0) {
                        try {
                            methods.setAccessible(true);
                            methods.invoke(o);
                        } catch (IllegalAccessException ex) {
                            ex.printStackTrace();
                        } catch (InvocationTargetException ex) {
                            ex.getTargetException().printStackTrace();
                        }
                    }
                }
            }

        }

        for (Player players : Bukkit.getOnlinePlayers()) {
            this.unInjectPlayerPacketListener(players);
        }

    }

    /**
     * Register normal object
     *
     * @param o Object
     */
    public synchronized void subscribe(Object o) {
        if (!this.subscribedObjects.contains(o)) {
            this.subscribedObjects.add(o);
        }
    }

    /**
     * Register abstract object
     *
     * @param o     Object
     * @param clazz Class of the abstract object
     */
    public synchronized <T> void abstractSubscribe(T o, Class<? extends T> clazz) {
        DuelObjectClass wrapper = new DuelObjectClass<>(o, clazz);
        if (!this.abstractObjects.contains(wrapper)) {
            this.abstractObjects.add(wrapper);
        }
    }

    public synchronized void unSubscribe(Object o) {
        this.subscribedObjects.remove(o);
    }

    public synchronized <T> void unSubscribeAbstract(T o, Class<? extends T> clazz) {
        this.abstractObjects.remove(new DuelObjectClass<>(o, clazz));
    }


    private void injectPlayerPacketListener(Player player) {
        ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                Bukkit.getLogger().info("Read " + msg.getClass().getSimpleName());
                if (msg.getClass().getName().contains("Packet")) {
                    PacketEvent<?> event = new PacketEvent<>(msg, player.getUniqueId());
                    callMethods(event);
                    if (event.isCancelled()) {
                        return;
                    }
                }
                super.channelRead(ctx, msg);
            }

            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                if (msg.getClass().getName().contains("Packet")) {
                    PacketEvent<?> event = new PacketEvent<>(msg, player.getUniqueId());
                    callMethods(event);
                    if (event.isCancelled()) {
                        return;
                    }
                }
                super.write(ctx, msg, promise);
            }
        };
//        Channel channel = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel;
//        channel.eventLoop().submit(() -> {
//            if (channel.pipeline().get(HANDLER_NAME) == null) {
//                channel.pipeline().addAfter("packet_handler", HANDLER_NAME, channelDuplexHandler);
//            }
//        });

    }


    private void unInjectPlayerPacketListener(Player p) {
//        Channel channel = ((CraftPlayer) p).getHandle().playerConnection.networkManager.channel;
//        channel.eventLoop().submit(() -> {
//            if (channel.pipeline().get(HANDLER_NAME) != null) {
//                channel.pipeline().remove(HANDLER_NAME);
//            }
//        });
    }

//    @EventHandler
//    public void onInteract(PlayerInteractEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(PlayerEggThrowEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(PlayerBucketEmptyEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(PlayerMoveEvent e) { callMethods(e); }
//
//    @EventHandler
//    public void onInteract(PlayerJoinEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(PlayerItemConsumeEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(PlayerItemDamageEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(PlayerQuitEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(PlayerTeleportEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(PlayerLoginEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(AsyncPlayerPreLoginEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(AsyncPlayerChatEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(PlayerVelocityEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(PlayerInteractEntityEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(PlayerInteractAtEntityEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(PlayerDeathEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(PlayerChatTabCompleteEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(PlayerHandshakeEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(PlayerGameModeChangeEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(PlayerItemMendEvent e) { callMethods(e); }
//
//    public void onInteract(org.bukkit.event.inventory.InventoryClickEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.inventory.InventoryCloseEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.inventory.InventoryOpenEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.player.PlayerEditBookEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.player.PlayerItemHeldEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.player.PlayerAnimationEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.player.PlayerRespawnEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.player.PlayerItemBreakEvent e) { callMethods(e); }    public void onInteract(PlayerBedEnterEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(PlayerDropItemEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(PlayerBedLeaveEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.player.PlayerEditBookEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.player.PlayerItemHeldEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.player.PlayerAnimationEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.player.PlayerRespawnEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.player.PlayerItemBreakEvent e) { callMethods(e); }    public void onInteract(PlayerBedEnterEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(PlayerDropItemEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(PlayerBedLeaveEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.player.PlayerEditBookEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.player.PlayerItemHeldEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.player.PlayerAnimationEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.player.PlayerRespawnEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.player.PlayerItemBreakEvent e) { callMethods(e); }    public void onInteract(PlayerBedEnterEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(PlayerDropItemEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(PlayerBedLeaveEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.player.PlayerEditBookEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.player.PlayerItemHeldEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.player.PlayerAnimationEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.player.PlayerRespawnEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.player.PlayerItemBreakEvent e) { callMethods(e); }
//    public void onInteract(PlayerBedEnterEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(PlayerDropItemEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(PlayerBedLeaveEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.player.PlayerEditBookEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.player.PlayerItemHeldEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.player.PlayerAnimationEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.player.PlayerRespawnEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.player.PlayerItemBreakEvent e) { callMethods(e); }    public void onInteract(PlayerBedEnterEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(PlayerDropItemEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(PlayerBedLeaveEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.player.PlayerEditBookEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.player.PlayerItemHeldEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.player.PlayerAnimationEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.player.PlayerRespawnEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.player.PlayerItemBreakEvent e) { callMethods(e); }    public void onInteract(PlayerBedEnterEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(PlayerDropItemEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(PlayerBedLeaveEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.player.PlayerEditBookEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.player.PlayerItemHeldEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.player.PlayerAnimationEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.player.PlayerRespawnEvent e) { callMethods(e); }
//    @EventHandler
//    public void onInteract(org.bukkit.event.player.PlayerItemBreakEvent e) { callMethods(e); }


}
