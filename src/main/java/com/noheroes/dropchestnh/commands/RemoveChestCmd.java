/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.commands;

import com.noheroes.dropchestnh.exceptions.InsufficientPermissionsException;
import com.noheroes.dropchestnh.exceptions.MissingOrIncorrectParametersException;
import org.bukkit.command.CommandSender;

/**
 *
 * @author PIETER
 */
public class RemoveChestCmd extends Cmd {
    
    public RemoveChestCmd (CommandSender cs, String args[]) {
        super (cs, args);
        minArgs = 2;
    }
    
    @Override
    public boolean execute() throws InsufficientPermissionsException, MissingOrIncorrectParametersException {
        errorCheck();
        // Chest doesn't exist
        chestExistCheck(args[1]);
        // Check for admin
        ownershipCheck(args[1]);
        // Remove chest
        if (dc.getDcHandler().removeChest(args[1])) {
            cs.sendMessage("Chest " + args[1] + " has been successfully removed");
        }
        return true;
    }
}
