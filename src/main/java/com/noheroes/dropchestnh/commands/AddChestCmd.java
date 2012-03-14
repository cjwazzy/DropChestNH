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
import com.noheroes.dropchestnh.internals.Utils.MsgType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author PIETER
 */
public class AddChestCmd extends Cmd {
    
    public AddChestCmd(CommandSender cs, String args[]) {
        super(cs, args);
        permission = Properties.createChestPerm;
    }
    
    @Override
    public boolean execute() throws InsufficientPermissionsException, MissingOrIncorrectParametersException {
        getPlayer();
        errorCheck();
        if (args.length > 1) {
            if (dc.addPlayerToEditor(player, EditMode.ADD_CHEST, args[1])) {
                Utils.sendMessage(cs, "Left click on the chest you would like to add as dropchest or right click to cancel", MsgType.NEXT_STEP);
            }
        }
        else {
            if (dc.addPlayerToEditor(player, EditMode.ADD_CHEST)) {
                Utils.sendMessage(cs, "Left click on the chest you would like to add as dropchest or right click to cancel", MsgType.NEXT_STEP);
            }
        }
        return true;
    }
}
