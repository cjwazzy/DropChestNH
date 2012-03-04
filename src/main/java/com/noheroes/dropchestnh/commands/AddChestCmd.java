/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.commands;

import com.noheroes.dropchestnh.exceptions.InsufficientPermissionsException;
import com.noheroes.dropchestnh.exceptions.MissingOrIncorrectParametersException;
import com.noheroes.dropchestnh.internals.Properties;
import com.noheroes.dropchestnh.internals.Utils.EditMode;
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
            cs.sendMessage("Left click on the chest you would like to add as dropchest or right click to cancel");
            dc.addPlayerToEditor(player, EditMode.ADD_CHEST, args[1]);
        }
        else {
            cs.sendMessage("Left click on the chest you would like to add as dropchest or right click to cancel");
            dc.addPlayerToEditor(player, EditMode.ADD_CHEST);
        }
        return true;
    }
}
