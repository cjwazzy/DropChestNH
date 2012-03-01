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

package com.noheroes.dropchest;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.ContainerBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Handle events for all Player related events
 * @author narrowtux
 */
public class DropChestPlayerListener implements Listener {
	private final DropChest plugin;

	public DropChestPlayerListener(DropChest instance) {
		plugin = instance;

	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
			if(event.isCancelled()){
				return;
			}
			DropChestPlayer dplayer = DropChestPlayer.getPlayerByName(event.getPlayer().getName());
			Block b = event.getClickedBlock();
			if(DropChestItem.acceptsBlockType(b.getType())){
				DropChestItem chestdci = plugin.getChestByBlock(b);

				if(chestdci!=null&&chestdci.isProtect()&&(!chestdci.getOwner().equals(dplayer.getPlayer().getName()))){
					event.setCancelled(true);
					dplayer.getPlayer().sendMessage("That's not your chest");
					dplayer.setChestRequestType(ChestRequestType.NONE);
					return;
				}

				if(dplayer!=null&&!dplayer.getChestRequestType().equals(ChestRequestType.NONE)){
					switch(dplayer.getChestRequestType()){
					case CREATE:
						if(chestdci==null){
							ContainerBlock chest = (ContainerBlock)b.getState();
							int radius = dplayer.getRequestedRadius();
							if(radius < 2)
								radius = 2;

							DropChestItem dci = new DropChestItem(chest, radius, b, plugin);

							dci.setOwner(dplayer.getPlayer().getName());
							dci.setProtect(false);

							plugin.addChest(dci);

							event.getPlayer().sendMessage("Created DropChest. ID: #"+dci.getId());
						} else {
							dplayer.getPlayer().sendMessage(ChatColor.RED+"This DropChest already exists. ID: #"+chestdci.getId());
						}
						break;
					case WHICH:
						if(chestdci!=null){
							dplayer.sendMessage(chestdci.info());
						} else {
							event.getPlayer().sendMessage("This is not a DropChest!");
						}
						break;
					}
					dplayer.setChestRequestType(ChestRequestType.NONE);
					event.setCancelled(true);
				}
			}
			if(dplayer!=null&&dplayer.getChestRequestType().equals(ChestRequestType.CHESTINFO)){
				if(DropChestItem.acceptsBlockType(b.getType())){
					ContainerBlock cb = (ContainerBlock)b.getState();
					dplayer.sendMessage(DropChest.chestInformation(cb.getInventory(), "of clicked chest"));
					dplayer.setChestRequestType(ChestRequestType.NONE);
					event.setCancelled(true);
				}
			}
		} else if(event.getAction()==Action.LEFT_CLICK_BLOCK){
			Player player = event.getPlayer();
			DropChestPlayer dplayer = DropChestPlayer.getPlayerByName(player.getName());
			Block b = event.getClickedBlock();
			if(DropChestItem.acceptsBlockType(b.getType())){
				DropChestItem chestdci = plugin.getChestByBlock(b);
				if(chestdci!=null&&plugin.hasPermission(player, "dropchest.filter.set")&&dplayer.isEditingFilter()){
					Material m = player.getItemInHand().getType();
					boolean found = false;
					if(m.getId()==0&&plugin.hasPermission(player, "dropchest.filter.reset")){
						chestdci.getFilter(dplayer.getEditingFilterType()).clear();
						player.sendMessage(ChatColor.GREEN.toString()+"All items will be accepted.");
					} else{
						for (Material ma : chestdci.getFilter(dplayer.getEditingFilterType())){
							if(m.getId()==ma.getId()){
								chestdci.getFilter(dplayer.getEditingFilterType()).remove(ma);
								found = true;
								if(chestdci.getFilter(dplayer.getEditingFilterType()).size()==0){
									player.sendMessage(ChatColor.GREEN.toString()+"All items will be accepted.");
								} else {
									player.sendMessage(ChatColor.RED.toString()+ma.toString()+" won't be accepted.");
								}
								break;
							}
						}
						if(!found)
						{
							chestdci.getFilter(dplayer.getEditingFilterType()).add(m);
							player.sendMessage(ChatColor.GREEN.toString()+m.toString()+" will be accepted.");
						}
					}
					plugin.save();
				}
			}
		}
	}
}
