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
package org.blockserver.pe;

import lombok.Getter;
import org.blockserver.core.Server;
import org.blockserver.core.module.Module;
import org.blockserver.core.modules.logging.LoggingModule;
import org.blockserver.pe.network.PENetworkConverter;
import org.blockserver.pe.network.PENetworkProvider;

/**
 * Global Module for Minecraft PE
 */
public class PEModule extends Module {

    @Getter private PENetworkProvider network;

    public PEModule(Server server) {
        super(server);
        network = new PENetworkProvider(server, new PENetworkConverter());

        server.addModuleToEnable(network);
    }

    @Override
    public void onEnable() {
        network.setBroadcastName("BlockServer MCPE Default");
        getServer().getModule(LoggingModule.class).info("PEModule enabled!");
    }
}
