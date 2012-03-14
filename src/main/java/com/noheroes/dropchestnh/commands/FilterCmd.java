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
import com.noheroes.dropchestnh.internals.Utils.MsgType;
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
                break;
            case PULL:
                permission = Properties.createPullPerm;
                break;
            case PUSH:
                permission = Properties.createPullPerm;
                break;
        }
    }
    
    public boolean execute() throws InsufficientPermissionsException, MissingOrIncorrectParametersException {
        errorCheck();
        // No extra parameters -- Using interactive mode
        if (args.length == 1) {
            getPlayer();
            if (dc.addPlayerToEditor(player, EditMode.FILTER, filter)) {
                Utils.sendMessage(player, "Hit the dropchest with an item to add/remove it from the filter, right click when done", MsgType.NEXT_STEP);
            }
            return true;
        }
        // Not interactive mode, need at least 2 extra parameters for chest and material
        if (args.length < 3) {
            throw new MissingOrIncorrectParametersException("You must name the chest followed by the item(s) to be filtered, or use /dc " + filter.toString().toLowerCase() + " for interactive mode");
        }
        // Ensure chest exists
        chestExistCheck(args[1]);
        // Only the owner of the chest or an admin can make modifications
        ownershipCheck(args[1]);
        
        // Add a filter for each extra argument
        for (int i = 2; i < args.length; i++) {
            try {
                if (args[i].equalsIgnoreCase("all")) {
                    dch.addAllFilter(args[1], filter);
                    Utils.sendMessage(cs, "Everything added to filter", MsgType.INFO);
                }
                else if (args[i].equalsIgnoreCase("clear")) {
                    dch.clearFilter(args[1], filter);
                    Utils.sendMessage(cs, "Filter cleared", MsgType.INFO);
                }
                else {
                    // Update filter.  getMaterialFromString call is used to turn numbers into enums when giving feedback
                    if (dch.updateFilter(args[i], args[1], filter)) {
                        Utils.sendMessage(cs, dch.getMaterialFromString(args[i]).toString() 
                                + " has been added to the " + filter.toString() + " filter of chest " + args[1], MsgType.INFO);
                    }
                    else {
                        Utils.sendMessage(cs, dch.getMaterialFromString(args[i]).toString() 
                                + " has been removed from the " + filter.toString() + " filter of chest " + args[1], MsgType.INFO);
                    }
                }
            // Catch exceptions here because we want to finish the loop in case valid materials are entered as well
            } catch (MissingOrIncorrectParametersException ex) {
                Utils.sendMessage(cs, ex.getMessage(), MsgType.ERROR);
            }
        }
        return true;
    }
}
