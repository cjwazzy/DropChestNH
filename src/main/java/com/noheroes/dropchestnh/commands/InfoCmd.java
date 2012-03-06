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
                player.sendMessage("Click on the chest to get information about it");
            }
            return true;
        }
        if (args.length > 1) {
            // Check if dropchest exists
            chestExistCheck(args[1]);
            // Check if sender has basic admin permissions or owns the chest
            ownershipCheck(args[1], Properties.basicAdmin);
            Integer chestID = dc.getDcHandler().getChestID(args[1]);
            String msg;
            // Display basis chest info
            msg = Utils.getChestInfoMsg(cs, chestID);
            cs.sendMessage(msg);
            // Display full filter info for each filter
            for (Filter f : Filter.values()) {
                msg = Utils.getChestFilterInfoMsg(cs, chestID, f);
                cs.sendMessage(msg);
            }
        }
        return true;
    }
}
