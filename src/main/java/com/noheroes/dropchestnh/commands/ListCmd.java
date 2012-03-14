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
import java.util.LinkedList;
import java.util.List;
import org.bukkit.command.CommandSender;

/**
 *
 * @author PIETER
 */
public class ListCmd extends Cmd {
    
    public ListCmd(CommandSender cs, String args[]) {
        super (cs, args);
        permission = Properties.basicAdmin;  // Not used for error check, only for admin part of this command
    }
    
    @Override
    public boolean execute() throws InsufficientPermissionsException, MissingOrIncorrectParametersException {
        
        // No parameters besides list -- Player looking up his own chests
        if (args.length == 1) {
            getPlayer();
            showListPage(player.getName(), 1, cs);
            return true;
        }
        // This should really always be true if it's not equal to 1 but just to be safe and avoid NPE's
        if (args.length > 1) {
            Integer pageNr;
            // Check if the second argument is a page number -- Player looking up another page of his own chest list
            pageNr = Utils.getPageNr(args[1]);
            // Second argument was a page number, display page <pageNr> of player's own chest list
            if (pageNr != null) {
                getPlayer();
                showListPage(player.getName(), pageNr, cs);
                return true;
            }
            // Second argument was not a page number -- Admin performing lookup on a player name
            if(!Utils.hasPermission(cs, permission)) {
                // This exception message is on the assumption a player typo'd their page number, not attempted an admin lookup
                throw new MissingOrIncorrectParametersException("That is not a valid page number");
            }
            // No page number specified, look up first page
            if (args.length == 2) {
                showListPage(args[1], 1, cs);
                return true;
            }
            // Check if third argument is a page number
            pageNr = Utils.getPageNr(args[2]);
            if (pageNr == null) {
                throw new MissingOrIncorrectParametersException("That is not a valid page number");
            }
            showListPage(args[1], pageNr, cs);
        }
        return true;
    }
    
    // Shows a specific page of the chest list owned by playerName to cs
    private void showListPage(String playerName, Integer pageNr, CommandSender cs) throws MissingOrIncorrectParametersException {
        List<Integer> chestList = dch.getPlayerChestList(playerName);
        // No chests owned by this player
        if ((chestList == null) || chestList.isEmpty()) {
            if (playerName.equals(cs.getName())) {
                throw new MissingOrIncorrectParametersException("You do not own any chests");
            }
            else {
                throw new MissingOrIncorrectParametersException("Player " + playerName + " does not own any chests");
            }
        }
        List<Integer> subChestList;
        try {
            subChestList = Utils.getListPage(chestList, pageNr);
        } catch (IndexOutOfBoundsException ex) {
            throw new MissingOrIncorrectParametersException("That is not a valid page number");
        }
        Utils.sendMessage(cs, "Chests owned by player " + playerName + ". Page: " + pageNr + "/" + Utils.getNumPages(chestList), MsgType.INFO);
        String msg;
        for (Integer chest : subChestList) {
            msg = Utils.getChestInfoMsg(cs, chest);
            cs.sendMessage(msg);
        }
    }
}