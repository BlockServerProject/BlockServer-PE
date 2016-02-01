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

import org.blockserver.core.modules.message.Message;
import org.blockserver.core.modules.message.PlayerLoginMessage;
import org.blockserver.core.modules.network.BinaryBuffer;
import org.blockserver.core.modules.network.NetworkConverter;
import org.blockserver.core.modules.network.RawPacket;

import static org.blockserver.pe.network.NetworkInfo.*;

/**
 * Converter for Minecraft: PE
 */
public class PENetworkConverter implements NetworkConverter {

    @Override
    public RawPacket toPacket(Message message) {
        return null;
    }

    @Override
    public Message toMessage(RawPacket rawPacket) {
        BinaryBuffer bb = rawPacket.getBuffer();
        bb.setPosition(0);
        byte pid = bb.getByte();
        switch (pid) {
            case LOGIN_PACKET:
                //return new PlayerLoginMessage(player); //TODO: GET PLAYER
            default:
                return null;
        }
    }
}
