/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.internals;

import org.bukkit.Location;
import org.bukkit.World;

/**
 *
 * @author PIETER
 */

/* This class creates a box around a location with with distance of <distance> away from the location in the x and z planes,
 * while distance of <height> away from the location in the y plane that can be iteratored through with next()
 */
public class FilterBox {
    private World world;
    private int xMin;
    private int xMax;
    private int yMin;
    private int yMax;
    private int zMin;
    private int zMax;
    private int currentX;
    private int currentY;
    private int currentZ;
    private boolean hasNext;
    
    public FilterBox(int distance, int height, Location location) {
        this.world = location.getWorld();
        xMin = location.getBlockX() - distance;
        xMax = location.getBlockX() + distance;
        yMin = location.getBlockY() - height;
        yMax = location.getBlockY() + height;
        zMin = location.getBlockZ() - distance;
        zMax = location.getBlockZ() + distance;        
        currentX = xMin;
        currentY = yMin;
        currentZ = zMin;
        hasNext = true;
    }
    public Location next() {
        if (!hasNext) {
            return null;
        }
        // Construct location for current location
        Location loc = new Location(world, currentX, currentY, currentZ);
        // Move forward one step, first along x, second along z, lastly going up one along y
        currentX++;
        if (currentX > xMax) {
            currentX = xMin;
            currentZ++;
            if (currentZ > zMax) {
                currentZ = zMin;
                currentY++;
                if (currentY > yMax) {
                    hasNext = false;
                }
            }
        }       
        return loc;
    }
    
    public boolean hasNext() {
        return hasNext;
    }
}
