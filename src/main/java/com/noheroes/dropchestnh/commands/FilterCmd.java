/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.commands;

import com.noheroes.dropchestnh.exceptions.InsufficientPermissionsException;
import com.noheroes.dropchestnh.exceptions.MissingOrIncorrectParametersException;
import com.noheroes.dropchestnh.internals.Properties;
import com.noheroes.dropchestnh.internals.Utils;
import com.noheroes.dropchestnh.internals.Utils.EditMode;
import com.noheroes.dropchestnh.internals.Utils.Filter;
import org.bukkit.command.CommandSender;

/**
 *
 * @author PIETER
 */
public class FilterCmd extends Cmd {
    Filter filter;
    
    public FilterCmd(CommandSender cs, String args[], Filter filter) {
        super(cs, args);
        this.filter = filter;
        switch (filter) {
            case SUCK:
                permission = Properties.createSuckPerm;
            case PULL:
                permission = Properties.createPullPerm;
            case PUSH:
                permission = Properties.createPullPerm;
        }
    }
    
    public boolean execute() throws InsufficientPermissionsException, MissingOrIncorrectParametersException {
        errorCheck();
        // No extra parameters -- Using interactive mode
        if (args.length == 1) {
            getPlayer();
            if (dc.addPlayerToEditor(player, EditMode.FILTER, filter)) {
                player.sendMessage("Hit the dropchest with an item to add/remove it from the filter, right click when done");            
            }
            return true;
        }
        // Not interactive mode, need at least 2 extra parameters for chest and material
        if (args.length < 3) {
            throw new MissingOrIncorrectParametersException("You must name the chest followed by the item(s) to be filtered, or use /dc " + filter.toString().toLowerCase() + " for interactive mode");
        }
        if (!dc.getDcHandler().chestExists(args[1])) {
            throw new InsufficientPermissionsException("This chest does not exist");
        }
        // Only the owner of the chest or an admin can make modifications
        if (!Utils.isAdmin(cs)) {
            getPlayer();
            if (!dc.getDcHandler().ownsChest(args[1], player)) {            
                throw new InsufficientPermissionsException("You do not own this chest");
            }
        }
        
        // Add a filter for each extra argument
        for (int i = 2; i < args.length; i++) {
            try {
                // Update filter.  getMaterialFromString call is used to turn numbers into enums when giving feedback
                if (dc.getDcHandler().updateFilter(args[i], args[1], filter)) {
                    cs.sendMessage(dc.getDcHandler().getMaterialFromString(args[i]).toString() + " has been added to the " + filter.toString() + " filter of chest " + args[1]);
                }
                else {
                    cs.sendMessage(dc.getDcHandler().getMaterialFromString(args[i]).toString() + " has been removed from the " + filter.toString() + " filter of chest " + args[1]);
                }
            // Catch exceptions here because we want to finish the loop in case valid materials are entered as well
            } catch (MissingOrIncorrectParametersException ex) {
                cs.sendMessage(ex.getMessage());
            }
        }
        return true;
    }
}
