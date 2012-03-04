/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.internals;

import com.noheroes.dropchestnh.DropChestNH;

/**
 *
 * @author PIETER
 */
public class InventoryData {
    private int filledSlots;
    private int freeSlots;
    private int usedSpace;
    
    public InventoryData(int filledSlots, int freeSlots, int usedSpace) {
        this.filledSlots = filledSlots;
        this.freeSlots = freeSlots;
        this.usedSpace = usedSpace;
    }
    
    public int getFilledSlots() {
        return filledSlots;
    }
    
    public int getFreeSlots() {
        return freeSlots;
    }
    
    public int getUsedSpace() {
        return usedSpace;
    }
    
    public int getTotalSlots() {
        return (filledSlots + freeSlots);
    }
    
    public int getPercentageUsed() {
        // Assume free slots have 64 items worth of space since most items stack to 64
        if (usedSpace == 0) {
            return 0;
        }
        return (int)Math.round(((float)usedSpace / (((float)freeSlots * 64.0) + (float)usedSpace))  * 100.0);
    }
    
    public InventoryData add(InventoryData addInv) {
        if (addInv == null) {
            return this;
        }
        return new InventoryData(this.filledSlots + addInv.getFilledSlots(), 
                this.freeSlots + addInv.freeSlots, this.usedSpace + addInv.getUsedSpace());
    }
}
