/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.commands;

import com.noheroes.dropchestnh.exceptions.InsufficientPermissionsException;
import com.noheroes.dropchestnh.exceptions.MissingOrIncorrectParametersException;
import org.bukkit.command.CommandSender;

/**
 *
 * @author PIETER
 */
public class SetDistanceCmd extends Cmd {

    public SetDistanceCmd(CommandSender cs, String args[]) {
        super(cs, args);
    }
    
    @Override
    public boolean execute() throws InsufficientPermissionsException, MissingOrIncorrectParametersException {
        dc.log("Set distance command NYI");
        return true;
    }
}