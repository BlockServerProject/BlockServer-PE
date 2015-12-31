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

import net.beaconpe.jraklib.JRakLib;
import net.beaconpe.jraklib.Logger;
import net.beaconpe.jraklib.protocol.EncapsulatedPacket;
import net.beaconpe.jraklib.server.JRakLibServer;
import net.beaconpe.jraklib.server.ServerHandler;
import net.beaconpe.jraklib.server.ServerInstance;
import org.blockserver.core.Server;
import org.blockserver.core.modules.logging.LoggingModule;
import org.blockserver.core.modules.network.BinaryBuffer;
import org.blockserver.core.modules.network.NetworkConverter;
import org.blockserver.core.modules.network.NetworkProvider;
import org.blockserver.core.modules.network.RawPacket;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NetworkProvider implementation for JRakLib.
 */
public class PENetworkProvider extends NetworkProvider implements ServerInstance{
    private JRakLibServer rakLibServer;
    private String broadcastName;
    private ServerHandler handler;

    private Map<String, String> identifiers = new ConcurrentHashMap<>();

    public PENetworkProvider(Server server, NetworkConverter converter) {
        super(server, converter);
    }

    @Override
    public void onEnable() {
        rakLibServer = new JRakLibServer(new JRakLibLogger(getServer().getModule(LoggingModule.class)), 19132, "0.0.0.0");
        handler = new ServerHandler(rakLibServer, this);
        handler.sendOption("name", "MCPE;"+broadcastName+";"+NetworkInfo.CURRENT_PROTOCOL+";"+NetworkInfo.CURRENT_VERSION+";0;0"); //TODO: Player count
        getServer().getModule(LoggingModule.class).info("Minecraft: PE Server started on 0.0.0.0:19132.");
    }

    @Override
    public void onDisable() {
        handler.shutdown();
    }

    public void setBroadcastName(String name) {
        if(isEnabled()) {
            handler.sendOption("name", "MCPE;"+name+";34;0.13.1;0;0");
        } else {
            broadcastName = name;
        }
    }

    public void sendPacket(RawPacket rawPacket) {
        EncapsulatedPacket packet = new EncapsulatedPacket();
        packet.reliability = 2;
        packet.messageIndex = 0;
        packet.buffer = rawPacket.getBuffer().toArray();
        if(!identifiers.containsKey(rawPacket.getAddress().toString())) {
            identifiers.put(rawPacket.getAddress().toString(), socketAddressToIdentifier(rawPacket.getAddress()));
        }
        handler.sendEncapsulated(identifiers.get(rawPacket.getAddress().toString()), packet, (byte) (0 | JRakLib.PRIORITY_NORMAL));
    }

    @Override
    public void openSession(String identifier, String address, int port, long clientID) {
        //TODO: create player
    }

    @Override
    public void closeSession(String identifier, String reason) {
        //TODO: remove player
    }

    @Override
    public void handleEncapsulated(String identifier, EncapsulatedPacket packet, int flags) {
        queueInboundPackets(new RawPacket(BinaryBuffer.wrapBytes(packet.buffer, ByteOrder.BIG_ENDIAN), identifierToSocketAddress(identifier)));
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
