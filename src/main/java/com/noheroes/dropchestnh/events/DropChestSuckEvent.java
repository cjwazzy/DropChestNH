/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.events;

import org.bukkit.entity.Item;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author PIETER
 */
public class DropChestSuckEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private Item item;
    private boolean cancelled;

    public DropChestSuckEvent(Item item) {
        this.item = item;
        cancelled = false;
    }
    
    public Item getItem() {
        return item;
    }
    
    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
    
    // Outdated?  It does not need to be overriden anymore
    public HandlerList getHandlerList() {
        return handlers;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
