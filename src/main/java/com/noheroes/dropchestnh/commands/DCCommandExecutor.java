/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.commands;

import com.noheroes.dropchestnh.DropChestNH;
import com.noheroes.dropchestnh.exceptions.InsufficientPermissionsException;
import com.noheroes.dropchestnh.exceptions.MissingOrIncorrectParametersException;
import com.noheroes.dropchestnh.internals.Utils.Filter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author PIETER
 */
public class DCCommandExecutor implements CommandExecutor {
    private final DropChestNH dc;
    
    public DCCommandExecutor (DropChestNH dc) {
        this.dc = dc;
    }
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String com;
        if (args.length == 0) {
            com = "help";
        }
        else {
            com = args[0];
        }
        Cmd cmd;
        try {
            if(com.equalsIgnoreCase("help") || com.equalsIgnoreCase("?")) {
                cmd = new HelpCmd(sender, args);
            }
            else if (com.equalsIgnoreCase("add")) {
                cmd = new AddChestCmd(sender, args);
            } 
            else if (com.equalsIgnoreCase("pull")) {
                cmd = new FilterCmd(sender, args, Filter.PULL);
            }
            else if (com.equalsIgnoreCase("push")) {
                cmd = new FilterCmd(sender, args, Filter.PUSH);
            }
            else if (com.equalsIgnoreCase("suck")) {
                cmd = new FilterCmd(sender, args, Filter.SUCK);
            }
            else {
                cmd = new HelpCmd(sender, args);
            }
            return cmd.execute();
        } catch (InsufficientPermissionsException ex) {
            dc.log("Exception caught: " + ex.getMessage());
        } catch (MissingOrIncorrectParametersException ex) {
            dc.log("Exception caught: " + ex.getMessage());
        }
        return true;
    }
}