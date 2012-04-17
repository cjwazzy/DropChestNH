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
        if (args.length == 1) {
            if (dc.addPlayerToEditor(player, EditMode.ADD_CHEST)) {
                Utils.sendMessage(cs, "Left click on the chest you would like to add as dropchest or right click to cancel", MsgType.NEXT_STEP);
            }
            return true;
        }
        Integer sd;
        try {
            sd = Integer.valueOf(args[1]);
        } catch (NumberFormatException ex) {
            throw new MissingOrIncorrectParametersException("The distance must be a non-negative number between 1 and " + Properties.maxDistance);
        }
        if ((sd == null) || (sd <= 0) || (sd > Properties.maxDistance)) {
            throw new MissingOrIncorrectParametersException("The distance must be a non-negative number between 1 and " + Properties.maxDistance);
        }
        if (args.length == 2) {
            if (dc.addPlayerToEditor(player, EditMode.ADD_CHEST, sd)) {
                Utils.sendMessage(cs, "Left click on the chest you would like to add as dropchest or right click to cancel", MsgType.NEXT_STEP);
            }            
        }
        if (args.length >= 3) {
            Integer sh;
            try {
                sh = Integer.valueOf((args[2]));
            } catch (NumberFormatException ex) {
                throw new MissingOrIncorrectParametersException("The height must be a non-negative number between 1 and " + Properties.maxHeight);
            }
            if ((sh == null) || (sh <= 0) || (sh > Properties.maxHeight)) {
                throw new MissingOrIncorrectParametersException("The height must be a non-negative number between 1 and " + Properties.maxHeight);
            }
            if (dc.addPlayerToEditor(player, EditMode.ADD_CHEST, sd, sh)) {
                Utils.sendMessage(cs, "Left click on the chest you would like to add as dropchest or right click to cancel", MsgType.NEXT_STEP);
            }            
        }
        return true;
    }
}
