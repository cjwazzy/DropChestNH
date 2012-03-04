/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.internals;

/**
 *
 * @author PIETER
 */
public class Properties {
   
    private Properties () {}
    
    
    public static final int chestsPerPage = 5;
    
    // Permissions  
    public static final String createSuckPerm = "dropchest.create.suck";
    public static final String createPullPerm = "dropchest.create.pull";
    public static final String createPushPerm = "dropchest.create.push";
    public static final String basicAdmin = "dropchest.admin.basic";    // Only permission to look at chests
    public static final String fullAdmin = "dropchest.admin.full";      // All permissions
    
    public static final String createChestPerm = "dropchestCreatePerm";   // Not an actual permission, only use for internal permission handling, do not change
}
