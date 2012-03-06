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
            else if (com.equalsIgnoreCase("remove") || com.equalsIgnoreCase("delete")) {
                cmd = new RemoveChestCmd(sender, args);
            }
            else if (com.equalsIgnoreCase("list")) {
                cmd = new ListCmd(sender, args);
            }
            else if (com.equalsIgnoreCase("listall")) {
                cmd = new ListAllCmd(sender, args);
            }
            else if (com.equalsIgnoreCase("info")) {
                cmd = new InfoCmd(sender, args);
            }
            else if (com.equalsIgnoreCase("setdistance") || com.equalsIgnoreCase("sd")) {
                cmd = new SetDistanceCmd(sender, args);
            }
            else if (com.equalsIgnoreCase("setheight") || com.equalsIgnoreCase("sh")) {
                cmd = new SetHeightCmd(sender, args);
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