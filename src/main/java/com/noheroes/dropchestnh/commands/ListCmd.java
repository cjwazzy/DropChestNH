/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.commands;

import com.noheroes.dropchestnh.exceptions.InsufficientPermissionsException;
import com.noheroes.dropchestnh.exceptions.MissingOrIncorrectParametersException;
import com.noheroes.dropchestnh.internals.Properties;
import com.noheroes.dropchestnh.internals.Utils;
import java.util.LinkedList;
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
            pageNr = getPageNr(args[1]);
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
            pageNr = getPageNr(args[2]);
            if (pageNr == null) {
                throw new MissingOrIncorrectParametersException("That is not a valid page number");
            }
            showListPage(args[1], pageNr, cs);
        }
        return true;
    }
    
    // Shows a specific page of the chest list owned by playerName to cs
    private void showListPage(String playerName, Integer pageNr, CommandSender cs) throws MissingOrIncorrectParametersException {
        LinkedList<Integer> chestList = dch.getPlayerChestList(playerName);
        // No chests owned by this player
        if ((chestList == null) || chestList.isEmpty()) {
            if (playerName.equals(cs.getName())) {
                throw new MissingOrIncorrectParametersException("You do not own any chests");
            }
            else {
                throw new MissingOrIncorrectParametersException("Player " + playerName + " does not own any chests");
            }
        }
        // Calculate starting index based on page number and chests per page
        int i = ((pageNr - 1) * Properties.chestsPerPage);
        // Check if index is within bounds
        if (i >= chestList.size()) {
            throw new MissingOrIncorrectParametersException("This page does not exist");
        }
        // List pages numbers if there's more than one page worth of lines
        if (chestList.size() > Properties.chestsPerPage) {
            int pages;
            pages = (int)Math.ceil((float)chestList.size() / Properties.chestsPerPage);
            cs.sendMessage("Chests owned by player " + playerName + ". Page: " + pageNr + "/" + pages);
        }
        // Single page
        else {
            cs.sendMessage("Chests owned by player " + playerName);
        }
        String msg;
        int chestID;
        // Loop through chests until correct number are displayed on page
        for ( ; i < (pageNr * Properties.chestsPerPage); i++) {
            // End of list
            if (i >= chestList.size())
                break;            
            chestID = chestList.get(i);
            // Create list info string
            msg = Utils.getChestInfoMsg(cs, chestID);
            cs.sendMessage(msg);
        }
    }
    
    private Integer getPageNr(String pageString) {
        Integer pageNr;
        try {
            pageNr = Integer.valueOf(pageString);
        } catch (NumberFormatException ex) {
            pageNr = null;
        }        
        return pageNr;
    }
}