/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.internals;

import org.bukkit.command.CommandSender;

/**
 *
 * @author PIETER
 */
public class Utils {
    
    public static enum Filter {
        PUSH, PULL, SUCK
    }
    
    public static enum EditMode {
        ADD_CHEST, FILTER
    }
    // Temp set to true
    public static boolean hasPermission(CommandSender cs, String perm) {
        return true;
    }
    // Temp set to op only
    public static boolean isAdmin(CommandSender cs) {
        return cs.isOp();
    }
}
