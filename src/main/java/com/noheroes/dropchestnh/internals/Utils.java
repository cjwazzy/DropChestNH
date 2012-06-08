/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.internals;

import com.noheroes.dropchestnh.DropChestNH;
import com.noheroes.dropchestnh.exceptions.MissingOrIncorrectParametersException;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author PIETER
 */
public class Utils {
    
    public static enum Filter {
        PUSH, PULL, SUCK
    }
    
    public static enum EditMode {
        ADD_CHEST, FILTER, INFO
    }
    
    public static enum MsgType {
        INFO, ERROR, NEXT_STEP
    }
    // Temp set to true
    public static boolean hasPermission(CommandSender cs, String perm) {
        if (perm == null) {
            return true;
        }
        if (isAdmin(cs)) {
            return true;
        }
        if (perm.equals(Properties.createChestPerm)) {
            return (cs.hasPermission(Properties.createPullPerm) || 
                    cs.hasPermission(Properties.createPushPerm) || cs.hasPermission(Properties.createSuckPerm));
        }
        return cs.hasPermission(perm);
    }
    
    // Temp set to op only
    public static boolean isAdmin(CommandSender cs) {
        return (cs.hasPermission(Properties.fullAdmin) || (cs instanceof ConsoleCommandSender) || cs.isOp());
    }
    
    public static void sendMessage(CommandSender cs, String msg, MsgType type) {
        ChatColor color;
        switch (type) {
            case INFO:
                color = Properties.infoColor;
                break;
            case ERROR:
                color = Properties.errorColor;
                break;
            case NEXT_STEP:
                color = Properties.nextStepColor;
                break;
            default:
                color = ChatColor.WHITE;
                break;
        }
        sendMessage(cs, msg, color);
    }
    
    public static void sendMessage(CommandSender cs, String msg, ChatColor color) {
        if (cs instanceof Player) {
            cs.sendMessage(color + msg);
        }
        else {
            cs.sendMessage(msg);
        }
    }
    
    public static void sendMessage(CommandSender cs, String msg) {
        sendMessage(cs, msg, ChatColor.WHITE);
    }
    
    public static String getChestInfoMsg(CommandSender cs, Integer chestID) {
        String msg;
        DropChestNH dc = DropChestNH.getInstance();
        msg = getColor(cs, Properties.chestIDColor) + "#" + chestID;
        if (dc.getDcHandler().getChestName(chestID) != null) {
            msg += getColor(cs, Properties.chestNameColor) + " " + dc.getDcHandler().getChestName(chestID);
        }
        Location loc = dc.getDcHandler().getChestLocation(chestID);
        msg += getColor(cs, Properties.chestLocColor) + " X:" + loc.getBlockX() + " Z:" + loc.getBlockZ() + " Y:" + loc.getBlockY();
        for (Filter f : Filter.values()) {
            if (dc.getDcHandler().isFilterInUse(chestID, f)) {
                msg += getColor(cs, Properties.chestFilterColor) + " " + f.name().toString();
            }
        }
        InventoryData invData = dc.getDcHandler().getInventoryData(chestID);
        if (invData != null) {
            msg += getColor(cs, Properties.chestSlotsColor) + " " + invData.getFilledSlots() + "/" + invData.getTotalSlots();
            msg += getColor(cs, Properties.chestFilledColor) + " " + invData.getPercentageUsed() + "%";       
        }
        return msg;
    }
    
    public static String getChestFilterInfoMsg(CommandSender cs, Integer chestID, Filter filter) {
        Set<String> filterSet;
        String msg;
        filterSet = DropChestNH.getInstance().getDcHandler().getFilter(chestID, filter);
        // Empty filter
        if ((filterSet == null) || filterSet.isEmpty()) {
            msg = getColor(cs, Properties.chestFilterColor) + filter.toString() + ": None";
        }
        // All materials besides Material.AIR added
        else if (filterSet.size() == (Material.values().length - 1)) {
            msg = getColor(cs, Properties.chestFilterColor) + filter.toString() + ": All";
        }
        // Non-empty, grab material list from filter
        else {             
            msg = getColor(cs, Properties.chestFilterColor) + filter.toString() + ": ";
            boolean firstItem = true;
            // Loop through all items in filter and grab their material name
            for (String mat : filterSet) {
                if (firstItem) {
                    firstItem = false;
                }
                else {
                    msg += ", ";
                }
                msg += MaterialNames.getItemName(mat);
            }
        }
        return msg;
    }

    // Only colors messages sent to players
    public static String getColor(CommandSender cs, ChatColor color) {
        if (cs instanceof Player) {
            return color.toString();
        }
        else {
            return "";
        }
    }
    
    public static String[] locToString(Location location) {
        String[] locStr = new String[4];
        locStr[0] = location.getWorld().getName();
        locStr[1] = String.valueOf(location.getBlockX());
        locStr[2] = String.valueOf(location.getBlockY());
        locStr[3] = String.valueOf(location.getBlockZ());
        return locStr;
    }
    
    public static Location stringToLoc(String locStr[]) {
        // locStr being equal to null is not uncommon, it happens any time there is no secondary location
        if (locStr == null) {
            return null;
        }
        // Location string should be length 4 or it is invalid
        if (locStr.length != 4) {
            return null;
        }
        World world = Bukkit.getWorld(locStr[0]);
        if (world == null) {
            return null;
        }
        Integer xLoc;
        Integer yLoc;
        Integer zLoc;
        try {
            xLoc = Integer.valueOf(locStr[1]);
            yLoc = Integer.valueOf(locStr[2]);
            zLoc = Integer.valueOf(locStr[3]);
        } catch (NumberFormatException ex) {
            return null;
        }       
        return new Location(world, xLoc, yLoc, zLoc);
    }
    
    // Shows a specific page of the chest list owned by playerName to cs
    public static <T>List<T> getListPage(List<T> list, int pageNr) throws IndexOutOfBoundsException {
           
            // Calculate starting index based on page number and chests per page
            int i = ((pageNr - 1) * Properties.linesPerPage);
           
            // Check if index is within bounds
            if (i >= list.size())
                    throw new IndexOutOfBoundsException();
           
            int j = i + Properties.linesPerPage;
            j = (j > list.size()) ? list.size() : j;
           
            return list.subList(i, j);
    }
   
    public static <T>Integer getNumPages(List<T> list){
            int pages = (int)Math.ceil((float)list.size() / Properties.linesPerPage);
            return (pages < 1 ? 1 : pages);
    }
    
    public static Integer getPageNr(String pageString) {
        Integer pageNr;
        try {
            pageNr = Integer.valueOf(pageString);
        } catch (NumberFormatException ex) {
            return null;
        }        
        if (pageNr < 1) {
            pageNr = null;
        }
        return pageNr;
    }
    /*
    public static String itemStackToString(ItemStack is) {
        return Utils.itemStackToString(is, true);
    }*/
    
    public static String itemStackToString(ItemStack is, boolean useDataValue) {
        String returnStr;
        // Item has no durability, durability is data value and data value is being used
        if ((is.getType().getMaxDurability() == Properties.noDurabilityValue) && useDataValue) {
            returnStr = is.getTypeId() + ":" + is.getDurability();
        }
        // Item has a durability value, we only use the itemID
        else {
            returnStr = String.valueOf(is.getTypeId());
        }
        return returnStr;
    }
    
    public static boolean compareISToString(ItemStack is, String matData) {
        if (matData.contains(":")) {
            String[] splitStr = matData.split(":");
            return (splitStr[0].equals(String.valueOf(is.getTypeId())) && splitStr[1].equals(String.valueOf(is.getDurability())));
        }
        else {
            return (matData.equals(String.valueOf(is.getTypeId())));
        }
    }
    
    public static String getMaterialDataFromString(String mat) throws MissingOrIncorrectParametersException {
        if (mat.contains(":")) {
            String[] splitStr = mat.split(":");
            // More than one colon in the string, invalid material data
            if (splitStr.length != 2) {
                throw new MissingOrIncorrectParametersException(mat + " is not a valid material");
            }
            else {
                Integer itemID;
                Integer dataValue;
                // Convert strings to ints, throw exception if we cannot
                try {
                    itemID = Integer.valueOf(splitStr[0]);
                    dataValue = Integer.valueOf(splitStr[1]);
                } catch (NumberFormatException ex) {
                    throw new MissingOrIncorrectParametersException(mat + " is not a valid material");
                }
                if ((itemID == null) || (dataValue == null)) {
                    throw new MissingOrIncorrectParametersException(mat + " is not a valid material");
                }
                // Material ID does not exist
                if (Material.getMaterial(itemID) == null) {
                    throw new MissingOrIncorrectParametersException(itemID + " is not a valid material ID");
                }
                // Material does not have a data value
                if (Material.getMaterial(itemID).getMaxDurability() != Properties.noDurabilityValue) {
                    throw new MissingOrIncorrectParametersException("Item number " + itemID + " cannot have data values");
                }
                return (itemID.toString() + ":" + dataValue.toString());
            }
        }
        else {
            Material material = getMaterialFromString(mat);
            if (mat == null) {
                throw new MissingOrIncorrectParametersException(mat + " is not a valid material");
            }
            return String.valueOf(material.getId());
        }
    }
         
    public static Material getMaterialFromString(String mat) {
        Material material;
        // Check if material is referenced by enum
        material = Material.getMaterial(mat.toUpperCase());
        // Material was matched
        if (material != null) {
            return material;
        }
        // Material was not matched, check if material is referenced by ID
        else {
            Integer materialID;
            try {
                materialID = Integer.valueOf(mat);
            }
            catch (NumberFormatException ex) {
                // mat is not an integer or valid material
                return null;
            }
            // Check if material ID exists
            material = Material.getMaterial(materialID);
            if (material == null) {
                return null;
            }
            return material;
        }
    }
}
