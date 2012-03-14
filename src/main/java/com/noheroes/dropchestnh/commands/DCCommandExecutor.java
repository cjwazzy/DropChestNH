/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.commands;

import com.noheroes.dropchestnh.DropChestNH;
import com.noheroes.dropchestnh.exceptions.InsufficientPermissionsException;
import com.noheroes.dropchestnh.exceptions.MissingOrIncorrectParametersException;
import com.noheroes.dropchestnh.internals.Utils;
import com.noheroes.dropchestnh.internals.Utils.Filter;
import com.noheroes.dropchestnh.internals.Utils.MsgType;
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
    
    public boolean onCommand(CommandSender cs, Command command, String label, String[] args) {
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
                cmd = new HelpCmd(cs, args);
            }
            else if (com.equalsIgnoreCase("add")) {
                cmd = new AddChestCmd(cs, args);
            } 
            else if (com.equalsIgnoreCase("pull")) {
                cmd = new FilterCmd(cs, args, Filter.PULL);
            }
            else if (com.equalsIgnoreCase("push")) {
                cmd = new FilterCmd(cs, args, Filter.PUSH);
            }
            else if (com.equalsIgnoreCase("suck")) {
                cmd = new FilterCmd(cs, args, Filter.SUCK);
            }
            else if (com.equalsIgnoreCase("remove") || com.equalsIgnoreCase("delete")) {
                cmd = new RemoveChestCmd(cs, args);
            }
            else if (com.equalsIgnoreCase("list")) {
                cmd = new ListCmd(cs, args);
            }
            else if (com.equalsIgnoreCase("listall")) {
                cmd = new ListAllCmd(cs, args);
            }
            else if (com.equalsIgnoreCase("info")) {
                cmd = new InfoCmd(cs, args);
            }
            else if (com.equalsIgnoreCase("setdistance") || com.equalsIgnoreCase("sd")) {
                cmd = new SetDistanceCmd(cs, args);
            }
            else if (com.equalsIgnoreCase("setheight") || com.equalsIgnoreCase("sh")) {
                cmd = new SetHeightCmd(cs, args);
            }
            else if (com.equalsIgnoreCase("setname") || com.equalsIgnoreCase("sn")) {
                cmd = new SetNameCmd(cs, args);
            }
            else if (com.equalsIgnoreCase("warnfull") || com.equalsIgnoreCase("wf")) {
                cmd = new WarnFullCmd(cs, args);
            }
            else if (com.equalsIgnoreCase("import")) {
                cmd = new ImportCmd(cs, args);
            }
            else {
                throw new MissingOrIncorrectParametersException("Unknown command " + com + ". Type /dc help for a list of commands");
            }
            return cmd.execute();
        } catch (InsufficientPermissionsException ex) {
            Utils.sendMessage(cs, ex.getMessage(), MsgType.ERROR);
        } catch (MissingOrIncorrectParametersException ex) {
            Utils.sendMessage(cs, ex.getMessage(), MsgType.ERROR);
        }
        return true;
    }
}