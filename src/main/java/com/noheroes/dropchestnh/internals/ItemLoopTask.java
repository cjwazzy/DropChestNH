/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.internals;

import com.noheroes.dropchestnh.DropChestNH;
import com.noheroes.dropchestnh.events.DropChestSuckEvent;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author PIETER
 */
public class ItemLoopTask implements Runnable {
    
    DropChestNH dc;
    
    public ItemLoopTask(DropChestNH dc) {
        this.dc = dc;
    }
    
    @Override
    public void run() {
        for (World world : dc.getServer().getWorlds()) {
            for(Entity entity : world.getEntities()) {
                if (entity instanceof Item) {
                    // Fire event to prevent dropchest from sucking up items it's not supposed to, this can be cancelled by other plugins
                    DropChestSuckEvent dcEvent = new DropChestSuckEvent((Item)entity);
                    dc.getServer().getPluginManager().callEvent(dcEvent);
                    // Skip this item if event is cancelled
                    if (dcEvent.isCancelled())
                        continue;
                    // Add items to chest and get leftovers
                    ItemStack leftover = dc.getDcHandler().pickupItem(((Item)entity).getItemStack(), entity.getLocation());
                    if ((leftover == null) || (leftover.getAmount() == 0)) {
                        entity.remove();
                    }
                    else {
                        ((Item)entity).getItemStack().setAmount(leftover.getAmount());
                    }
                }
            }
        }
    }
}
