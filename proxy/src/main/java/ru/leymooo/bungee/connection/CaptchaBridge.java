package ru.leymooo.bungee.connection;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.connection.UpstreamBridge;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.Chat;
import net.md_5.bungee.protocol.packet.ClientSettings;
import net.md_5.bungee.protocol.packet.ConfirmTransaction;
import net.md_5.bungee.protocol.packet.KeepAlive;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.md_5.bungee.protocol.packet.TeleportConfirm;
import ru.leymooo.config.CaptchaConfig;
import ru.leymooo.ycore.Connection;

public class CaptchaBridge extends PacketHandler {
    //Captcha
    private final UserConnection con;
    private String captcha = null;
    private int captchaId = 0;
    private boolean settings = false;
    private long joinTime = System.currentTimeMillis();
    private int retrys = 3;
    private boolean tpconfirm = false;
    private boolean mcbrand = false;
    private boolean alive = false;
    private boolean transaction = false;
    private int aliveid;
    private String ip;
    private boolean needReset = true;
    //Captcha settings
    public static boolean underAttack = false;
    public static boolean captchaGenerating = false;
    public static HashSet<String> bannedips = new HashSet<String>();
    private static final Random random = new Random();
    private static int teleportId;
    public static ArrayList<String> strings = new ArrayList<String>();
    private static String TIMEOUT;
    private static String ENTER_CAPTCHA;
    private static String INVALID;
    private static Map<UserConnection, CaptchaBridge> connections = new ConcurrentHashMap<UserConnection, CaptchaBridge>();
    private static CaptchaConfig sql;
    private static AtomicInteger a = new AtomicInteger();
    private static long start = System.currentTimeMillis();

    public static void init() {
        CaptchaBridge.sql = new CaptchaConfig();
        CaptchaBridge.TIMEOUT = CaptchaConfig.messageTimeout;
        CaptchaBridge.ENTER_CAPTCHA = CaptchaConfig.messageEnter;
        CaptchaBridge.INVALID = CaptchaConfig.messageInvalid;
        teleportId = Connection.getTeleportId();
        CaptchaBridge.captchaGenerating = true;
        try {
            CaptchaGenerator captchagenerator = new CaptchaGenerator();
            captchagenerator.generate(CaptchaBridge.sql.threads, CaptchaConfig.maxCaptcha);
        } catch (Exception exception) {
            System.out.println("Exception while generate maps");
            exception.printStackTrace();
            System.exit(0);
        }

    }
    public CaptchaBridge() {
        this.con = null;
    }

    public static void resetAllCaptcha() {
        Iterator<CaptchaBridge> iterator = CaptchaBridge.connections.values().iterator();
        CaptchaBridge b;
        while (iterator.hasNext()) {
            b = (CaptchaBridge) iterator.next();
            b.resetCaptcha();
            b.needReset = false;
        }
    }
    public UserConnection getCon() {
        return this.con;
    }

    public long getJoinTime() {
        return this.joinTime;
    }
    public void setJoinTime() {
        this.joinTime = System.currentTimeMillis();
    }
    public CaptchaBridge(UserConnection connection) {
        this.con = connection;
        CaptchaBridge.a.incrementAndGet();
        this.connected();
    }

    private void connected() {
        ip = this.con.getAddress().getAddress().getHostAddress();
        if (bannedips.contains(ip)) {
            this.con.disconnect("§cНа сервер ведётся бот атака. Зайдите через §a5 §cминут!");
            return;
        }
        Channel channel = this.con.getCh().getHandle();
        int protocol = this.con.getPendingConnection().getHandshake().getProtocolVersion();
        this.con.setClientEntityId(-1);
        byte id1;
        byte id2;
        byte id3;
        if (protocol > 47) {
            id1 = 35;
            id2 = 67;
            id3 = 46;
        } else {
            id1 = 1;
            id2 = 5;
            id3 = 8;
        }
        aliveid = random.nextInt(999);
        this.write(channel, Connection.login, protocol, id1);
        this.write(channel, Connection.spawnPosition, protocol, id2);
        if (protocol > 47) {
            this.write(channel, Connection.abilities1_9, protocol, 43);
            this.write(channel, Connection.playerPosition, protocol, id3);
            this.write(channel, Connection.chunk1_9, protocol, 32);
            this.write(channel, Connection.setSlot, protocol, 22);
            this.write(channel, Connection.transaction, protocol, 17);
            this.write(channel, new KeepAlive(aliveid), protocol, 31);
        } else {
            tpconfirm = true;
            this.write(channel, Connection.playerPosition, protocol, id3);
            this.write(channel, Connection.setSlot, protocol, 47);
            this.write(channel, Connection.transaction, protocol, 50);
            this.write(channel, new KeepAlive(aliveid), protocol, 0);
        }
        if (captchaGenerating == false && underAttack == false) {
            needReset = false;
            this.resetCaptcha();
        }
        BungeeCord.getInstance().getLogger().info("[" + this.con.getName() + "] <-> CaptchaBridge has connected");
        CaptchaBridge.connections.put(this.con, this);
    }

    private void write(Channel channel, DefinedPacket packet, int protocol, int id) {
        ByteBuf buf = channel.alloc().buffer();

        DefinedPacket.writeVarInt(id, buf);
        packet.write(buf, ProtocolConstants.Direction.TO_CLIENT, protocol);
        channel.write(buf);
    }

    private void write(Channel channel, ByteBuf buf) {
        ByteBuf buffer = buf.copy();

        channel.write(buffer);
    }

    public void exception(Throwable t) throws Exception {
        this.con.disconnect(Util.exception(t));
        CaptchaBridge.connections.remove(this.con);
        BungeeCord.getInstance().removeConnection( this.con );

    }

    public void disconnected(ChannelWrapper channel) throws Exception {
        CaptchaBridge.connections.remove(this.con);
        BungeeCord.getInstance().getPluginManager().callEvent(new PlayerDisconnectEvent(this.con));
        BungeeCord.getInstance().removeConnection( this.con );
    }

    private void resetCaptcha() {
        int protocol = this.con.getPendingConnection().getHandshake().getProtocolVersion();
        Channel channel = this.con.getCh().getHandle();

        this.captchaId = getRandomCaptcha();
        this.captcha = strings.get(captchaId);
        this.write(channel, Connection.getCaptcha(this.captchaId, protocol));
        channel.flush();
    }

    private static int getRandomCaptcha() {
        return random.nextInt(strings.size());
    }
    public void handle(ConfirmTransaction transaction) {
        this.transaction = true;
    }
    public void handle(PluginMessage message) {
        if (message.getTag().equals("MC|Brand")) {
            mcbrand = true;
        }
    }
    public void handle(TeleportConfirm confirm) throws Exception {
        if (confirm.getTeleportId() == teleportId) {
            tpconfirm = true;
        } else {
            this.con.disconnect("§cСкорее всего вы бот :(");
        }
    }
    public void handle(KeepAlive alive) throws Exception {
        if (alive.getRandomId() == aliveid ) {
            this.alive = true;
        }
    }
    public void handle(ClientSettings settings) throws Exception {
        this.settings = true;
        this.con.setSettings(settings);
    }
    public void handle(Chat chat) throws Exception {
        if (captchaGenerating) return;
        String msg = chat.getMessage();
        if (msg.length() >= 5) {
            if (--this.retrys == 0) {
                if (underAttack) bannedips.add(ip);
                this.con.disconnect("[§cCaptcha§f] Неверная капча");
            }
            return;
        }
        if (!msg.equalsIgnoreCase(String.valueOf(this.captcha))) {
            if (--this.retrys == 0) {
                this.con.disconnect("[§cCaptcha§f] Неверная капча");
                if (underAttack) bannedips.add(ip);
            } else {
                this.resetCaptcha();
                this.con.sendMessage(String.format(CaptchaBridge.INVALID, new Object[] { Integer.valueOf(this.retrys), this.retrys == 1 ? "а" : "и"}));
            }
        } else {
            this.finish();
        }
    }

    @SuppressWarnings("deprecation")
    private void finish() {
        this.con.serverr = true;
        ((HandlerBoss) this.con.getCh().getHandle().pipeline().get(HandlerBoss.class)).setHandler(new UpstreamBridge(ProxyServer.getInstance(), this.con));
        ProxyServer.getInstance().getPluginManager().callEvent(new PostLoginEvent(this.con));
        this.con.connect(ProxyServer.getInstance().getServerInfo(this.con.getPendingConnection().getListener().getDefaultServer()), (Callback<Boolean>) null, true);
        CaptchaBridge.sql.addIp(this.con.getAddress().getAddress().getHostAddress());
        CaptchaBridge.connections.remove(this.con);
    }


    public String toString() {
        return "[" + this.con.getName() + "] <-> CaptchaBridge ";
    }
    static {
        (new Thread(new Runnable() {
            public void run() {
                while (true) {
                    if (!Thread.interrupted()) {
                        try {
                            Thread.sleep(3000L);
                        } catch (InterruptedException interruptedexception) {
                            interruptedexception.printStackTrace();
                        }

                        long curr = System.currentTimeMillis();
                        Iterator<CaptchaBridge> iterator = CaptchaBridge.connections.values().iterator();
                        CaptchaBridge b;
                        while (iterator.hasNext()) {
                            b = (CaptchaBridge) iterator.next();
                            if (captchaGenerating) {
                                b.setJoinTime();
                                b.getCon().sendMessage("§aГенерирую капчу... Пожалуйста подождите!");
                                continue;
                            }
                            b.getCon().sendMessage(CaptchaBridge.ENTER_CAPTCHA);
                            if (curr - b.getJoinTime() >= (long) CaptchaConfig.timeout) {
                                b.getCon().disconnect(CaptchaBridge.TIMEOUT);
                                if (underAttack) bannedips.add(b.ip);
                                iterator.remove();
                            } else {
                                if (curr - b.getJoinTime() >= 3000) {
                                    if (underAttack)  {
                                        if (b.settings == false || b.tpconfirm == false || b.mcbrand == false || b.alive == false || b.transaction == false) {
                                            b.getCon().disconnect("§cСкорее всего вы бот :(");
                                            if (underAttack) bannedips.add(b.ip);
                                            iterator.remove();
                                        } else if (b.needReset) {
                                            b.resetCaptcha();
                                            b.needReset = false;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }, "Captcha TimeoutHandler")).start();
        (new Thread(new Runnable() {
            public void run() {
                while (true) {
                    if (!Thread.interrupted()) {
                        try {
                            Thread.sleep(3000L);
                            long curr = System.currentTimeMillis();
                            if (!(curr - CaptchaBridge.start >= 1000*60*3)) {
                                CaptchaBridge.a.set(0);
                                continue;                                
                            }
                            if (CaptchaBridge.a.get() > 60) {
                                CaptchaBridge.init();
                                CaptchaBridge.underAttack = true;
                                Thread.sleep(1000*60*5);
                                CaptchaBridge.underAttack = false;
                                CaptchaBridge.bannedips.clear();
                                CaptchaBridge.init();
                            }
                            CaptchaBridge.a.set(0);
                        } catch (InterruptedException interruptedexception) {
                            interruptedexception.printStackTrace();
                        }
                    }

                }
            }
        }, "Captcha JoinCounter")).start();
    }
}