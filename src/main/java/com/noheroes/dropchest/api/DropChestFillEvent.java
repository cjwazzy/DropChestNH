/*
 * Copyright (C) 2011 Moritz Schmale <narrow.m@gmail.com>
 *
 * DropChest is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/gpl.html>.
 */

package com.noheroes.dropchest.api;

import com.noheroes.dropchest.DropChestItem;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DropChestFillEvent extends Event {
	private DropChestItem chest;
        private static final HandlerList handlers = new HandlerList();
        
	public DropChestFillEvent(DropChestItem chest) {
		super("DropChestFillEvent");
		this.chest = chest;
	}

	/**
	 * @return the chest
	 */
	public DropChestItem getChest() {
		return chest;
	}

	public double getNewFillRate(){
		return chest.getPercentFull();
	}
        
        @Override
        public HandlerList getHandlers() {
            return handlers;
        }
 
        public static HandlerList getHandlerList() {
            return handlers;
        }
}
