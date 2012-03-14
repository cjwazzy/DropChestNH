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
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author PIETER
 */
public class HelpCmd extends Cmd {
    private ChatColor cc;
    private ChatColor ic; 
    
    public HelpCmd(CommandSender cs, String args[]) {
        super(cs, args);
        if (cs instanceof Player) {
            cc = ChatColor.YELLOW;
            ic = ChatColor.GREEN;
        }
        else {
            cc = ChatColor.WHITE;
            ic = ChatColor.WHITE;
        }
    }

    @Override
    public boolean execute() throws InsufficientPermissionsException, MissingOrIncorrectParametersException {
        List<String> helpMsgList;
        Integer pageNr = null;
        if (args.length == 1) {
            pageNr = 1;
        }
        // pageNr isn't set yet
        if (pageNr == null) {
            pageNr = Utils.getPageNr(args[1]);
        }
        // If pageNr is still null no valid page was entered
        if (pageNr != null) {
            helpMsgList = Utils.getListPage(getHelpList(), pageNr);
            sendHelpListHeader(pageNr);
            sendMsgList(helpMsgList);
        }
        else {
            String param = args[1];
            List<String> msgList = new ArrayList<String>();
            if (param.equalsIgnoreCase("add")) {
                msgList.add(cc + "/dc add [ChestName]" + ic + " - Adds a new dropchest.");
                msgList.add(ic + "ChestName is an optional name for the chest");
            }
            else if (param.equalsIgnoreCase("pull")) {
                msgList.add(cc + "/dc pull" + ic + " - Enter interactive mode.  While holding");
                msgList.add(ic + "an item left click on a chest to add/remove it to the filter");
                msgList.add(cc + "/dc pull chest item1 [item2] [etc]" + ic + " - Adds each item");
                msgList.add(ic + "listed to the filter.  Separate items by spaced (not commas)");
                if (Utils.isAdmin(cs)) {
                    msgList.add(ic + "As full admin this command can be used on chests owned");
                    msgList.add(ic + "by other players in the same way as normal");
                }
            }
            else if (param.equalsIgnoreCase("push")) {
                msgList.add(cc + "/dc push" + ic + " - Enter interactive mode.  While holding");
                msgList.add(ic + "an item left click on a chest to add/remove it to the filter");
                msgList.add(cc + "/dc push chest item1 [item2] [etc]" + ic + " - Adds each item");
                msgList.add(ic + "listed to the filter.  Separate items by spaced (not commas)");
                if (Utils.isAdmin(cs)) {
                    msgList.add(ic + "As full admin this command can be used on chests owned");
                    msgList.add(ic + "by other players in the same way as normal");
                }
            }
            else if (param.equalsIgnoreCase("suck")) {
                msgList.add(cc + "/dc suck" + ic + " - Enter interactive mode.  While holding");
                msgList.add(ic + "an item left click on a chest to add/remove it to the filter");
                msgList.add(cc + "/dc suck chest item1 [item2] [etc]" + ic + " - Adds each item");
                msgList.add(ic + "listed to the filter.  Separate items by spaced (not commas)");
                if (Utils.isAdmin(cs)) {
                    msgList.add(ic + "As full admin this command can be used on chests owned");
                    msgList.add(ic + "by other players in the same way as normal");
                }
            }
            else if (param.equalsIgnoreCase("list")) {
                msgList.add(cc + "/dc list [PageNr]" + ic + " - A list of all chests owned, displaying");
                msgList.add(ic + "in order: Chest ID, Chest Name (if applicable), Location, ");
                msgList.add(ic + "which filters are in use, used slots/total slots, % filled");
                if (Utils.hasPermission(cs, Properties.basicAdmin)) {
                    msgList.add(cc + "/dc list playername [PageNr] <A>" + ic + " Admin command");
                    msgList.add(ic + "displaying a list of chests owned by PlayerName.");
                }
            }
            else if (param.equalsIgnoreCase("info")) {
                msgList.add(cc + "/dc info" + ic + " - Left click on a chest to get detailed");
                msgList.add(ic + "information about the chest.");
                msgList.add(cc + "/dc info chest" + ic + " - Get detailed information");
                msgList.add(ic + "about the specified chest.");
                if(Utils.hasPermission(cs, Properties.basicAdmin)) {
                    msgList.add(ic + "As basic admin this command can be used on chests owned by");
                    msgList.add(ic + "owned by other players in the same way as normal");
                }
            }
            else if (param.equalsIgnoreCase("remove") || param.equalsIgnoreCase("delete")) {
                msgList.add(cc + "/dc remove" + ic + " - Removes the dropchest. Destroying it");
                msgList.add(ic + "will automatically remove it as well");
                if (Utils.isAdmin(cs)) {
                    msgList.add(ic + "As full admin this command can be used on chests owned");
                    msgList.add(ic + "by other players in the same way as normal");
                }
            }
            else if (param.equalsIgnoreCase("setdistance") || param.equalsIgnoreCase("sd")) {
                msgList.add(cc + "/dc setdistance|sd chest dist" + ic + " - Sets the suck distance for");
                msgList.add(ic + "a dropchest.  This is measured in blocks, creating a square");
                msgList.add(ic + "or rectangle (for double chests). Max value: " + Properties.maxDistance);
                if(Utils.hasPermission(cs, Properties.basicAdmin)) {
                    msgList.add(ic + "As basic admin this command can be used on chests owned by");
                    msgList.add(ic + "owned by other players in the same way as normal");
                }
            }
            else if (param.equalsIgnoreCase("setheight") || param.equalsIgnoreCase("sh")) {
                msgList.add(cc + "/dc setheight|sh chest height" + ic + " - Sets the suck height for");
                msgList.add(ic + "a dropchest. This is measure in blocks. By default a chest only");
                msgList.add(ic + "sucks items up that are on it's own level. Max value: " + Properties.maxHeight);
                if(Utils.hasPermission(cs, Properties.basicAdmin)) {
                    msgList.add(ic + "As basic admin this command can be used on chests owned by");
                    msgList.add(ic + "owned by other players in the same way as normal");
                }
            }
            else if (param.equalsIgnoreCase("setname") || param.equalsIgnoreCase("sn")) {
                msgList.add(cc + "/dc setname|sn chest ChestName" + ic + " - Sets the name of a");
                msgList.add(ic + "dropchest or renames it if it already had a name");
                if(Utils.hasPermission(cs, Properties.basicAdmin)) {
                    msgList.add(ic + "As basic admin this command can be used on chests owned by");
                    msgList.add(ic + "owned by other players in the same way as normal");
                }
            }
            else if (param.equalsIgnoreCase("warnfull") || param.equalsIgnoreCase("wf")) {
                msgList.add(cc + "/dc warnfull|wf chest" + ic + " - Sends a warning to you when your");
                msgList.add(ic + "chest picks up an item that fills it past " + Properties.defaultAlmostFullWarningThreshold + "%. The warning");
                msgList.add(ic + "is sent every " + Properties.defaultWarningDelay + " minutes");
                msgList.add(cc + "/dc warnfull|wf chest [filled%] [delay]" + ic + " - Allows you to specify");
                msgList.add(ic + " at what % full a chest warns, and how often (in mins)");
                if(Utils.hasPermission(cs, Properties.basicAdmin)) {
                    msgList.add(ic + "As basic admin this command can be used on chests owned by");
                    msgList.add(ic + "owned by other players in the same way as normal");
                }
            }
            else if (param.equalsIgnoreCase("listall") && Utils.hasPermission(cs, Properties.basicAdmin)) {
                msgList.add(cc + "/dc listall <A>" + ic + " - Admin only command to list the number of");
                msgList.add(ic + "chests in use by each player. Use /dc list for detailed info");
            }
            else {
                Utils.sendMessage(cs, "Unknown command " + param + ". /dc help for a list of commands", MsgType.ERROR);
            }
            sendMsgList(msgList);
        }
        return true;
    }   
    
    private List<String> getHelpList() {
        List<String> helpMsgList = new ArrayList<String>();
        helpMsgList.add(cc + "/dc add [Name]" + ic + " - Add a new dropchest");
        helpMsgList.add(cc + "/dc pull|push|suck" + ic + " - Interactive filter mode");
        helpMsgList.add(cc + "/dc pull|push|suck chest item1 [item2] [etc]" + ic + " - Add each item");
        helpMsgList.add(cc + "/dc list [PageNr]" + ic + " - A list of your chests");
        helpMsgList.add(cc + "/dc info [chest]" + ic + " - Info about chest"); 
        helpMsgList.add(cc + "/dc remove|delete chest" + ic + " - Removes chest as dropchest");
        helpMsgList.add(cc + "/dc setdistance|sd chest dist" + ic + " - Sets suck distance in blocks");
        helpMsgList.add(cc + "/dc setheight|sh chest height" + ic + " - Sets suck high in blocks");
        helpMsgList.add(cc + "/dc setname|sn chest name" + ic + " - Set name/renames a chest");
        helpMsgList.add(cc + "/dc warnfull|wf chest [Filled%] [Delay]" + ic + " - Chest full warning");
        helpMsgList.add(cc + "/dc help command" + ic + " - Detailed information about the command");
        if (Utils.hasPermission(cs, Properties.basicAdmin)) {
            helpMsgList.add(cc + "/dc listall <A>" + ic + " - List of chests for each player");
            helpMsgList.add(cc + "/dc info [chest] <A>" + ic + " - Works on other player's chests");
            helpMsgList.add(cc + "/dc list [PlayerName] [PageNr] <A>" + ic + " - List player's chests");
        }
        if (Utils.isAdmin(cs)) {
            helpMsgList.add(cc + "Full Admin" + ic + " - All commands work on other player's chests");
        }
        return helpMsgList;
    }
    
    private void sendMsgList(List<String> msgList) {
        for (String string : msgList) {
            cs.sendMessage(string);
        }
    }
    
    private void sendHelpListHeader(Integer pageNr) {
        List<String> msg = new ArrayList<String>();
        msg.add(ChatColor.GREEN + "Help commands. Chest is any chest identifier (name or ID). " + pageNr + "/" + Utils.getNumPages(getHelpList()));
        if (Utils.hasPermission(cs, Properties.basicAdmin)) {
            msg.add(ChatColor.GREEN + "Things in [brackets] are optional.  <A> denotes admin command");
        }
        else {
            msg.add(ChatColor.GREEN + "Things in [brackets] are optional.");
        }
        sendMsgList(msg);
    }
}
