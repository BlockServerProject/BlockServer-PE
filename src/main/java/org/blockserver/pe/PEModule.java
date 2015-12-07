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

import org.blockserver.core.Server;
import org.blockserver.core.module.Module;

/**
 * Global Module for Minecraft PE
 */
public class PEModule extends Module {
    public PEModule(Server server) {
        super(server);
    }
}
