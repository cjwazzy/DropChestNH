/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.listeners;

import com.noheroes.dropchestnh.DropChestNH;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author PIETER
 */
public class DCListener implements Listener {
    
    DropChestNH dc;
    
    public DCListener(DropChestNH dc) {
        this.dc = dc;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {        
        if (!dc.isInEditMode(event.getPlayer())) {
            return;
        }
        
        boolean done = false;
        
        // Left clicked on a chest
        if ((event.getAction().equals(Action.LEFT_CLICK_BLOCK)) && event.getClickedBlock().getType().equals(Material.CHEST)) {
            done = dc.getPlayerEditor(event.getPlayer()).leftClickEvent(event.getClickedBlock(), 
                    event.getPlayer().getItemInHand().getType());
            event.setCancelled(true);
        }
        
        if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            done = dc.getPlayerEditor(event.getPlayer()).rightClickEvent();
            event.setCancelled(true);
        }
        if (done) {
            dc.removePlayerFromEditor(event.getPlayer());
        }
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock().getType().equals(Material.CHEST)) {
            dc.getDcHandler().doubleChestCheck(event.getBlock());
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        dc.removePlayerFromEditor(event.getPlayer());
    }
}
