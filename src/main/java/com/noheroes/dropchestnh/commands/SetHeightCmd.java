/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.commands;

import com.noheroes.dropchestnh.exceptions.InsufficientPermissionsException;
import com.noheroes.dropchestnh.exceptions.MissingOrIncorrectParametersException;
import com.noheroes.dropchestnh.internals.Properties;
import com.noheroes.dropchestnh.internals.Utils;
import com.noheroes.dropchestnh.internals.Utils.MsgType;
import org.bukkit.command.CommandSender;

/**
 *
 * @author PIETER
 */
public class SetHeightCmd extends Cmd {
    
    public SetHeightCmd(CommandSender cs, String args[]) {
        super(cs, args);
        minArgs = 3;
    }
    
    @Override
    public boolean execute() throws InsufficientPermissionsException, MissingOrIncorrectParametersException {
        errorCheck();
        chestExistCheck(args[1]);
        ownershipCheck(args[1]);
        
        Integer height;
        try {
            height = Integer.valueOf(args[2]);
        } catch (NumberFormatException ex) {
            throw new MissingOrIncorrectParametersException("The height must be a non-negative number between 0 and " + Properties.maxHeight);
        }
        if ((height < 0 ) || (height > Properties.maxHeight)) {
            throw new MissingOrIncorrectParametersException("The height must be a non-negative number between 0 and " + Properties.maxHeight);
        }
        dch.setSuckHeight(args[1], height);
        Utils.sendMessage(cs, "Suck height for chest " + args[1] + " changed to " + height, MsgType.INFO); 
        return true;
    }
}
