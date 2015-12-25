package org.blockserver.pe.network;

import net.beaconpe.jraklib.JRakLib;
import net.beaconpe.jraklib.protocol.EncapsulatedPacket;
import net.beaconpe.jraklib.server.JRakLibServer;
import net.beaconpe.jraklib.server.ServerHandler;
import net.beaconpe.jraklib.server.ServerInstance;
import org.blockserver.core.Server;
import org.blockserver.core.modules.network.NetworkProvider;
import org.blockserver.core.modules.network.RawPacket;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
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

    public PENetworkProvider(Server server) {
        super(server);
    }

    @Override
    public void onEnable() {
        rakLibServer = new JRakLibServer(null, 19132, "0.0.0.0");
        handler = new ServerHandler(rakLibServer, this);
        handler.sendOption("name", "MCPE;"+broadcastName+";34;0.13.1;0;0");
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

    @Override
    public void sendPacket(RawPacket rawPacket) {
        EncapsulatedPacket packet = new EncapsulatedPacket();
        packet.reliability = 2;
        packet.messageIndex = 0;
        packet.buffer = rawPacket.getBuffer();
        if(!identifiers.containsKey(rawPacket.getAddress().toString())) {
            identifiers.put(rawPacket.getAddress().toString(), socketAddressToIdentifier(rawPacket.getAddress()));
        }
        handler.sendEncapsulated(identifiers.get(rawPacket.getAddress().toString()), packet, (byte) (0 | JRakLib.PRIORITY_NORMAL));
    }

    @Override
    public void openSession(String identifier, String address, int port, long clientID) {

    }

    @Override
    public void closeSession(String identifier, String reason) {

    }

    @Override
    public void handleEncapsulated(String identifier, EncapsulatedPacket encapsulatedPacket, int flags) {

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
}
