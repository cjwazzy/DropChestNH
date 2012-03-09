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
    
    public FilterBox(int distance, int height, Location primaryLocation, Location secondaryLocation) {
        // Either location is fine for world info
        this.world = primaryLocation.getWorld();
        // Single chest
        if (secondaryLocation == null) {
            xMin = primaryLocation.getBlockX() - distance;
            xMax = primaryLocation.getBlockX() + distance;
            zMin = primaryLocation.getBlockZ() - distance;
            zMax = primaryLocation.getBlockZ() + distance;
        }
        // Double chest
        else {
            if (primaryLocation.getBlockX() < secondaryLocation.getBlockX()) {
                xMin = primaryLocation.getBlockX() - distance;
                xMax = secondaryLocation.getBlockX() + distance;
            }
            else {
                xMin = secondaryLocation.getBlockX() - distance;
                xMax = primaryLocation.getBlockX() + distance;
            }
            if (primaryLocation.getBlockZ() < secondaryLocation.getBlockZ()) {
                zMin = primaryLocation.getBlockZ() - distance;
                zMax = secondaryLocation.getBlockZ() + distance;
            }
            else {
                zMin = secondaryLocation.getBlockZ() - distance;                
                zMax = primaryLocation.getBlockZ() + distance;
            }
        }
        // Y locations are the same either way since double chests cannot stack along the Y axis
        yMin = primaryLocation.getBlockY() - height;
        yMax = primaryLocation.getBlockY() + height;
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
