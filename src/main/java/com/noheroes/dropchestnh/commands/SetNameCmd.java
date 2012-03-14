/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.commands;

import com.noheroes.dropchestnh.exceptions.InsufficientPermissionsException;
import com.noheroes.dropchestnh.exceptions.MissingOrIncorrectParametersException;
import com.noheroes.dropchestnh.internals.Utils;
import com.noheroes.dropchestnh.internals.Utils.MsgType;
import org.bukkit.command.CommandSender;

/**
 *
 * @author PIETER
 */
public class SetNameCmd extends Cmd {
    
    public SetNameCmd(CommandSender cs, String[] args) {
        super(cs, args);
        minArgs = 3;
    }
    
    @Override
    public boolean execute() throws InsufficientPermissionsException, MissingOrIncorrectParametersException {
        errorCheck();
        chestExistCheck(args[1]);
        ownershipCheck(args[1]);
        dch.setName(args[1], args[2]);
        Utils.sendMessage(cs, "Chest " + args[1] + " has been renamed to " + args[2], MsgType.INFO);
        return true;
    }
}
