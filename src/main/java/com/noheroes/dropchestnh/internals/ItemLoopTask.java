/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.internals;

import com.noheroes.dropchestnh.DropChestNH;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;

/**
 *
 * @author PIETER
 */
public class ItemLoopTask implements Runnable {
    
    DropChestNH dc;
    
    ItemLoopTask(DropChestNH dc) {
        this.dc = dc;
    }
    
    @Override
    public void run() {
        for (World world : dc.getServer().getWorlds()) {
            for(Entity entity : world.getEntities()) {
                if (entity instanceof Item) {
                    // Perform item lookup.
                }
            }
        }
    }
}
