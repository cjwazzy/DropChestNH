/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.commands;

import com.noheroes.dropchestnh.exceptions.InsufficientPermissionsException;
import com.noheroes.dropchestnh.exceptions.MissingOrIncorrectParametersException;
import com.noheroes.dropchestnh.internals.Properties;
import com.noheroes.dropchestnh.internals.Utils;
import com.noheroes.dropchestnh.internals.Utils.Filter;
import com.noheroes.dropchestnh.internals.Utils.MsgType;
import org.bukkit.command.CommandSender;

/**
 *
 * @author PIETER
 */
public class InfoCmd extends Cmd {
    
    public InfoCmd(CommandSender cs, String args[]) {
        super(cs, args);
    }
    
    public boolean execute() throws InsufficientPermissionsException, MissingOrIncorrectParametersException {
        if (args.length == 1) {
            getPlayer();
            if (dc.addPlayerToEditor(player, Utils.EditMode.INFO)) {
                Utils.sendMessage(player, "Click on the chest to get information about it", MsgType.NEXT_STEP);
            }
            return true;
        }
        if (args.length > 1) {
            // Check if dropchest exists
            chestExistCheck(args[1]);
            // Check if sender has basic admin permissions or owns the chest
            ownershipCheck(args[1], Properties.basicAdmin);
            Integer chestID = dch.getChestID(args[1]);
            String msg;
            // Display basis chest info
            msg = Utils.getChestInfoMsg(cs, chestID);
            cs.sendMessage(msg);
            // Display full filter info for each filter
            for (Filter f : Filter.values()) {
                msg = Utils.getChestFilterInfoMsg(cs, chestID, f);
                cs.sendMessage(msg);
            }
            if (dch.isFilterInUse(chestID, Filter.SUCK)) {
                Utils.sendMessage(cs, "This chest is picking up items in an area of " + dch.getXArea(chestID) + "x"
                        + dch.getZArea(chestID) + "x" + dch.getYArea(chestID) + " (XxZxY)", MsgType.INFO);
            }
            if (dch.getWarnFull(chestID)) {
                Utils.sendMessage(cs, "This chest has almost full warning turned on with a threshold", MsgType.INFO);
                Utils.sendMessage(cs, "of " + dch.getWarnThreshold(chestID) + 
                        "% and a delay of " + dch.getWarnDelay(chestID) + " minutes", MsgType.INFO);
            }
        }
        return true;
    }
}
