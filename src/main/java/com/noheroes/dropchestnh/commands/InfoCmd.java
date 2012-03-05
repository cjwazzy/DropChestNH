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
        permission = Properties.basicAdmin;   // Not used for error check, only for admin part of this command
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
            if (!dc.getDcHandler().chestExists(args[1])) {
                throw new MissingOrIncorrectParametersException("That dropchest does not exist");
            }
            // Check if sender has basic admin permissions or owns the chest
            if (!Utils.hasPermission(cs, Properties.basicAdmin)) {
                getPlayer();
                if (!dc.getDcHandler().ownsChest(args[1], player)) {
                    throw new InsufficientPermissionsException("That is not your dropchest");
                }
            }
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