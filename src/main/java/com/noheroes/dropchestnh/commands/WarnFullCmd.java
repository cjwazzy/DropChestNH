/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.commands;

import com.noheroes.dropchestnh.exceptions.InsufficientPermissionsException;
import com.noheroes.dropchestnh.exceptions.MissingOrIncorrectParametersException;
import com.noheroes.dropchestnh.internals.Utils;
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

        if (args.length == 2) {
            // Toggle the warn status
            boolean warning = dch.toggleWarning(args[1]);
            if (warning) {
                cs.sendMessage("Almost full warning turned on with a threshold of " + dch.getWarnThreshold(args[1]) + "%");
                cs.sendMessage("and a delay of " + dch.getWarnDelay(args[1]) + " minutes");
            }
            else {
                cs.sendMessage("Almost full warning turned off");
            }
            return true;
        }
        if (args.length == 3) {
            dch.setWarning(args[1], args[2]);
        }
        if (args.length > 3) {
            dch.setWarning(args[1], args[2], args[3]);
        }
        cs.sendMessage("Almost full warning turned on with a threshold of " + dch.getWarnThreshold(args[1]) + "%");
        cs.sendMessage("and a delay of " + dch.getWarnDelay(args[1]) + " minutes");
        return true;
    }
}
