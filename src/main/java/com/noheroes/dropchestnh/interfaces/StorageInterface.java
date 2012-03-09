/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.interfaces;

import com.noheroes.dropchestnh.internals.DropChestObj;
import java.util.List;

/**
 *
 * @author PIETER
 */
public interface StorageInterface {
    
    /**
     * Save a chest to permanent storage
     * @param chest The drop chest being saved
     */
    public void save (DropChestObj chest);
    
    /**
     * Loads all drop chests from storage
     * @return A list of all <code>DropChestObj</code> stored
     */
    public List<DropChestObj> loadAll();
    
    /**
     * Removes a chest from permanent storage
     * @param chest The drop chest being removed
     */
    public void removeChest(DropChestObj chest);
    
    /**
     * Write all information sent with save to permanent storage
     */
    public void write();
}
