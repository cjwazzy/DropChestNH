/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.internals;

import org.bukkit.ChatColor;

/**
 *
 * @author PIETER
 */
public class Properties {

    private Properties () {}
    
    
    public static int maxDistance;      
    public static int maxHeight;      
    
    public static int itemLoopDelay;
    
    public static int saveDelay;
    public static boolean saveInstantly;
    
    public static int minecartFilterDistance;
    public static boolean minecartVerticalPickup;
    
    public static final String miniFileName = "dropchest.mini";
    public static final int defaultAlmostFullWarningThreshold = 85;
    public static final int defaultWarningDelay = 30;                       // In minutes
    
    // Colors
    public static final ChatColor chestIDColor = ChatColor.AQUA;
    public static final ChatColor chestNameColor = ChatColor.GREEN;
    public static final ChatColor chestLocColor = ChatColor.BLUE;
    public static final ChatColor chestFilterColor = ChatColor.DARK_AQUA;
    public static final ChatColor chestSlotsColor = ChatColor.DARK_PURPLE;
    public static final ChatColor chestFilledColor = ChatColor.RED;
    public static final ChatColor infoColor = ChatColor.GREEN;
    public static final ChatColor errorColor = ChatColor.RED;
    public static final ChatColor nextStepColor = ChatColor.YELLOW;

    public static final int linesPerPage = 7;
    
    // Permissions  
    public static final String createSuckPerm = "dropchest.create.suck";
    public static final String createPullPerm = "dropchest.create.pull";
    public static final String createPushPerm = "dropchest.create.push";
    public static final String basicAdmin = "dropchest.admin.basic";        // Only permission to look at chests
    public static final String fullAdmin = "dropchest.admin.full";          // All permissions
    
    public static final String createChestPerm = "dropchestCreatePerm";      // Not an actual permission, only use for internal permission handling, do not change
}
