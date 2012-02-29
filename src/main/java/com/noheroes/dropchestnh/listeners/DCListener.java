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
import org.bukkit.inventory.ItemStack;

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
        /*
        if (!event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            return;
        }*/
        // These events simply exist for debugging purposes right now, left click adds dropchests, right click fills their inventory with whatever you're holding
        if (event.getAction().equals((Action.LEFT_CLICK_BLOCK))) {
            if (event.getClickedBlock().getType().equals(Material.CHEST)) {
                dc.getDcHandler().addChest(event.getClickedBlock(), event.getPlayer());
            }
        }
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (event.getClickedBlock().getType().equals(Material.CHEST)) {
                ItemStack iss = dc.getDcHandler().addItem(event.getClickedBlock().getLocation(), event.getPlayer().getItemInHand());
                if (iss != null) {
                    dc.log(iss.toString());
                }
            }
        }
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock().getType().equals(Material.CHEST)) {
            dc.getDcHandler().doubleChestCheck(event.getBlock());
        }
    }
}
