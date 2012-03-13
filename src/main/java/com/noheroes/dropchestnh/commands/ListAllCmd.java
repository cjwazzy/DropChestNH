/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.commands;

import com.noheroes.dropchestnh.exceptions.InsufficientPermissionsException;
import com.noheroes.dropchestnh.exceptions.MissingOrIncorrectParametersException;
import com.noheroes.dropchestnh.internals.Properties;
import java.util.HashMap;
import java.util.Map.Entry;
import org.bukkit.command.CommandSender;

/**
 *
 * @author PIETER
 */
public class ListAllCmd extends Cmd {
    
    public ListAllCmd(CommandSender cs, String args[]){
        super(cs, args);
        permission = Properties.basicAdmin;
    }
    
    @Override
    public boolean execute() throws InsufficientPermissionsException, MissingOrIncorrectParametersException {
        errorCheck();
        String msg = "Number of chests owned by each player: ";
        boolean first = true;
        HashMap<String, Integer> playerList = dch.getAllChestList();
        for (Entry<String, Integer> entry : playerList.entrySet()) {
            if (!first) {
                msg += ", ";
            }
            else {
                first = false;
            }
            msg += entry.getKey() + "-" + entry.getValue();
        }
        cs.sendMessage(msg);
        return true;
    }
}
