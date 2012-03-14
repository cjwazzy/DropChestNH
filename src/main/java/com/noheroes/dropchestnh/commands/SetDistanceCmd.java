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
public class SetDistanceCmd extends Cmd {

    public SetDistanceCmd(CommandSender cs, String args[]) {
        super(cs, args);
        minArgs = 3;
    }
    
    @Override
    public boolean execute() throws InsufficientPermissionsException, MissingOrIncorrectParametersException {
        errorCheck();
        chestExistCheck(args[1]);
        ownershipCheck(args[1]);
        
        Integer distance;
        try {
            distance = Integer.valueOf(args[2]);
        } catch (NumberFormatException ex) {
            throw new MissingOrIncorrectParametersException("The distance must be a non-negative number between 0 and " + Properties.maxDistance);
        }
        if ((distance < 0 ) || (distance > Properties.maxDistance)) {
            throw new MissingOrIncorrectParametersException("The distance must be a non-negative number between 0 and " + Properties.maxDistance);
        }
        dch.setSuckDistance(args[1], distance);
        Utils.sendMessage(cs, "Suck distance for chest " + args[1] + " changed to " + distance, MsgType.INFO); 
        return true;
    }
}
