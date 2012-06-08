/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.listeners;

import com.noheroes.dropchestnh.DropChestNH;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;

/**
 *
 * @author PIETER
 */
public class DCListener implements Listener {
    
    DropChestNH dc;
    
    public DCListener(DropChestNH dc) {
        this.dc = dc;
    }
    
    // Player interact event to get left click/right click on chests/etc
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {        
        if (!dc.isInEditMode(event.getPlayer())) {
            return;
        }
        
        boolean done = false;
        
        // Left clicked on a chest
        if ((event.getAction().equals(Action.LEFT_CLICK_BLOCK)) && event.getClickedBlock().getType().equals(Material.CHEST)) {
            done = dc.getPlayerEditor(event.getPlayer()).leftClickEvent(event.getClickedBlock(), 
                    event.getPlayer().getItemInHand(), event.getPlayer().isSneaking());
            event.setCancelled(true);
        }
        // Right click to cancel edit mode
        if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            done = dc.getPlayerEditor(event.getPlayer()).rightClickEvent();
            event.setCancelled(true);
        }
        if (done) {
            dc.removePlayerFromEditor(event.getPlayer());
        }
    }
    
    // Checks for double chest placement
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock().getType().equals(Material.CHEST)) {
            dc.getDcHandler().doubleChestCheck(event.getBlock());
        }
    }
    
    // Exits edit mode if a player logs off
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        dc.removePlayerFromEditor(event.getPlayer());
    }
    
    // Monitors block breaking to remove dropchests broken
    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType().equals((Material.CHEST))) {
            Location loc = event.getBlock().getLocation();
            if (dc.getDcHandler().chestExists(loc)) {
                Player player = dc.getServer().getPlayer(dc.getDcHandler().getOwner(loc));
                if (player != null) {
                    player.sendMessage("Your dropchest #" + dc.getDcHandler().getChestID(loc) + " was broken and has been removed");
                }
                dc.getDcHandler().removeChest(loc);
            }
        }
    }
    
    // Monitors explosions to remove dropchests blown up
    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        List<Block> blockList = event.blockList();
        Location loc;
        for (Block block : blockList) {
            loc = block.getLocation();
            if (dc.getDcHandler().chestExists(loc)) {
                Player player = dc.getServer().getPlayer(dc.getDcHandler().getOwner(loc));
                if (player != null) {
                    player.sendMessage("Your dropchest #" + dc.getDcHandler().getChestID(loc) + " was destroyed by an explosion and has been removed");
                }
                dc.getDcHandler().removeChest(loc);
            }
        }
    }
    
    // Vehicle movement event needed for push/pull aspect of dropchest
    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        if (event.getVehicle() instanceof StorageMinecart) {
            dc.getDcHandler().minecartMovement((StorageMinecart)event.getVehicle());
        }
    }
}
