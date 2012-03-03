/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.commands;

import com.noheroes.dropchestnh.DropChestNH;
import com.noheroes.dropchestnh.exceptions.InsufficientPermissionsException;
import com.noheroes.dropchestnh.exceptions.MissingOrIncorrectParametersException;
import com.noheroes.dropchestnh.internals.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author PIETER
 */
abstract public class Cmd {
    CommandSender cs;
    String[] args;
    Player player;
    DropChestNH dc;
    
    String permission = null;   // null permission defaults to permitted
    int minArgs = 1;    // With a value of 1 this will pass errorCheck() by default unless it is changed
    
    public Cmd(CommandSender cs, String args[]) {
        this.cs = cs;
        this.args = args;
        dc = DropChestNH.getInstance();
    }
    
    protected void getPlayer() throws InsufficientPermissionsException {
        if (!(cs instanceof Player)) {
            throw new InsufficientPermissionsException("Only players can use this command");
        }
        player = (Player)cs;
    }
    
    protected void errorCheck() throws InsufficientPermissionsException, MissingOrIncorrectParametersException {
        if (args.length < minArgs) {
            throw new MissingOrIncorrectParametersException("The command is missing parameters");
        }
        if (!Utils.hasPermission(cs, permission)) {
            throw new InsufficientPermissionsException();
        }
    }
    
    abstract public boolean execute() throws InsufficientPermissionsException, MissingOrIncorrectParametersException;
}