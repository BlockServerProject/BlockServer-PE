/*
 * This file is part of BlockServerPE.
 *
 * BlockServerPE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BlockServerPE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with BlockServerPE.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.blockserver.pe.network;

import lombok.Getter;
import net.beaconpe.jraklib.JRakLib;
import net.beaconpe.jraklib.Logger;
import net.beaconpe.jraklib.protocol.EncapsulatedPacket;
import net.beaconpe.jraklib.server.JRakLibServer;
import net.beaconpe.jraklib.server.ServerHandler;
import net.beaconpe.jraklib.server.ServerInstance;
import org.blockserver.core.Server;
import org.blockserver.core.modules.logging.LoggingModule;
import org.blockserver.core.modules.network.*;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NetworkProvider implementation for JRakLib.
 */
public class PENetworkProvider extends NetworkProvider implements ServerInstance, Dispatcher {
    private JRakLibServer rakLibServer;
    @Getter private String broadcastName = "Default";
    private ServerHandler handler;

    private boolean runHandleThread = false;
    private Thread handlingThread;

    private Map<String, String> identifiers = new ConcurrentHashMap<>();

    public PENetworkProvider(NetworkHandlerModule handler, Server server) {
        super(handler, server);
        System.out.println(handler);
    }

    @Override
    public void onEnable() {
        rakLibServer = new JRakLibServer(new JRakLibLogger(getServer().getModule(LoggingModule.class)), 19132, "0.0.0.0");
        handler = new ServerHandler(rakLibServer, this);
        handler.sendOption("name", "MCPE;"+broadcastName+";"+NetworkInfo.CURRENT_PROTOCOL+";"+NetworkInfo.CURRENT_VERSION+";0;0"); //TODO: Player count

        handlingThread = new Thread(() -> {
            Thread.currentThread().setName("PENetworkProcessor");
            while(runHandleThread) {
                handler.handlePacket();
            }
        });
        runHandleThread = true;
        handlingThread.start();
        getServer().getModule(NetworkHandlerModule.class).registerDispatcher(this);
        getServer().getModule(LoggingModule.class).info("Minecraft: PE Server started on 0.0.0.0:19132.");
    }

    @Override
    public void onDisable() {
        getServer().getModule(NetworkHandlerModule.class).unregisterDispatcher(this);
        handler.shutdown();
    }

    public void setBroadcastName(String name) {
        if(isEnabled()) {
            handler.sendOption("name", "MCPE;"+name+";"+NetworkInfo.CURRENT_PROTOCOL+";"+NetworkInfo.CURRENT_VERSION+";0;0");
        } else {
            broadcastName = name;
        }
    }

    @Override
    public void openSession(String identifier, String address, int port, long clientID) {
        getServer().getModule(LoggingModule.class).debug("("+identifier+"): Session OPENED {clientID: "+clientID+"}");
    }

    @Override
    public void closeSession(String identifier, String reason) {
        getServer().getModule(LoggingModule.class).debug("("+identifier+"): Session CLOSED {reason: "+reason+"}");
    }

    @Override
    public void handleEncapsulated(String identifier, EncapsulatedPacket packet, int flags) {
        RawPacket pk = new RawPacket(BinaryBuffer.wrapBytes(Arrays.copyOfRange(packet.buffer, 1, packet.buffer.length), ByteOrder.BIG_ENDIAN), identifierToSocketAddress(identifier));
        getServer().getModule(LoggingModule.class).debug("("+identifier+"): Packet IN {buffer: "+pk.getBuffer().singleLineHexDump()+"}");
        provide(pk);
    }

    @Override
    public void handleRaw(String address, int port, byte[] payload) {

    }

    @Override
    public void notifyACK(String identifier, int identifierACK) {

    }

    @Override
    public void exceptionCaught(String s, String s1) {

    }

    @Override
    public void handleOption(String option, String value) {

    }

    @Override
    public void dispatch(RawPacket rawPacket) {
        EncapsulatedPacket packet = new EncapsulatedPacket();
        packet.reliability = 2;
        packet.messageIndex = 0;
        BinaryBuffer bb = BinaryBuffer.newInstance(rawPacket.getBuffer().toArray().length + 1, ByteOrder.BIG_ENDIAN); //TODO: find a better way to do this
        bb.putByte((byte) 0x8E); //Strange 0.14.0 update thing
        bb.put(rawPacket.getBuffer().toArray());
        packet.buffer = bb.toArray();

        if(!identifiers.containsKey(rawPacket.getAddress().toString())) {
            identifiers.put(rawPacket.getAddress().toString(), socketAddressToIdentifier(rawPacket.getAddress()));
        }
        String ident = identifiers.get(rawPacket.getAddress().toString());
        getServer().getModule(LoggingModule.class).debug("("+ident+") Packet OUT: {buffer: "+rawPacket.getBuffer().singleLineHexDump()+"}");
        handler.sendEncapsulated(ident, packet, (byte) (0 | JRakLib.PRIORITY_NORMAL));
    }

    public static String socketAddressToIdentifier(SocketAddress address) {
        if(address instanceof InetSocketAddress) {
            InetSocketAddress isa = (InetSocketAddress) address;
            return isa.getAddress().getHostAddress() + ":" + isa.getPort();
        }
        return "";
    }

    public static InetSocketAddress identifierToSocketAddress(String identifier) {
        String ip = identifier.split(":")[0];
        int port = Integer.parseInt(identifier.split(":")[1]);
        return new InetSocketAddress(ip, port);
    }

    public static class JRakLibLogger implements Logger {
        private final LoggingModule logger;

        public JRakLibLogger(LoggingModule logger) {
            this.logger = logger;
        }

        @Override
        public void notice(String s) {
            logger.info("[NOTICE] "+s);
        }

        @Override
        public void critical(String s) {
            logger.warn("[CRITICAL] "+s);
        }

        @Override
        public void emergency(String s) {
            logger.error("[EMERGENCY] "+s);
        }
    }
}
