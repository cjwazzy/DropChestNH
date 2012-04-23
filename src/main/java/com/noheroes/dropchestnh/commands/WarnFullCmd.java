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
public class WarnFullCmd extends Cmd {
    
    public WarnFullCmd(CommandSender cs, String args[]) {
        super(cs, args);
        minArgs = 2;
    }

    @Override
    public boolean execute() throws InsufficientPermissionsException, MissingOrIncorrectParametersException {
        errorCheck();
        chestExistCheck(args[1]);
        ownershipCheck(args[1]);

        //if (args.length >= 2) {
            // Toggle the warn status
            boolean warning = dch.toggleWarning(args[1]);
            if (warning) {
                Utils.sendMessage(cs, "Almost full warning for chest " + args[1] + " turned on", MsgType.INFO);
            }
            else {
                Utils.sendMessage(cs, "Almost full warning for chest " + args[1] + " turned off", MsgType.INFO);
            }
            return true;
        //}
        /*
        if (args.length == 3) {
            dch.setWarning(args[1], args[2]);
        }
        if (args.length > 3) {
            dch.setWarning(args[1], args[2], args[3]);
        }*/
        //Utils.sendMessage(cs, "Almost full warning turned on with a threshold of " + dch.getWarnThreshold(args[1]) + "%", MsgType.INFO);
        //Utils.sendMessage(cs, "and a delay of " + dch.getWarnDelay(args[1]) + " minutes", MsgType.INFO);
        //return true;
    }
}
