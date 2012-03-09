/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.internals;

import com.mini.Arguments;
import com.mini.Mini;
import com.noheroes.dropchestnh.DropChestNH;
import com.noheroes.dropchestnh.interfaces.StorageInterface;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 *
 * @author PIETER
 */
public class MiniStorage implements StorageInterface {
    private DropChestNH dc;
    private String folder;
    
    private static final String chestNameKey = "chestname";
    private static final String suckFilterKey = "suckfilter";
    private static final String pushFilterKey = "pushfilter";
    private static final String pullFilterKey = "pullfilter";
    private static final String primaryLocKey = "primarylocation";
    private static final String secondaryLocKey = "secondarylocation";
    private static final String ownerNameKey = "chestowner";
    private static final String chestSuckDistance = "suckdistance";
    private static final String chestSuckHeight = "suckheight";
    public Mini minidb;
    
    public MiniStorage(DropChestNH dc, String folder) {
        this.dc = dc;
        this.folder = folder;
        minidb = new Mini(folder, Properties.miniFileName);
    }

    public void saveAll(HashMap<Integer, DropChestObj> chestList) {
        Arguments arg;
        for (DropChestObj dropchest : chestList.values()) {
            arg = DCOToArg(dropchest);
            minidb.addIndex(dropchest.getID().toString(), arg);
        }
        minidb.update();
    }

    public void save(DropChestObj chest) {
        Arguments arg = DCOToArg(chest);
        minidb.addIndex(chest.getID().toString(), arg);
        minidb.update();
    }

    public DropChestObj loadChest(Integer chestID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<DropChestObj> loadAll() {
        Set<String> keySet = minidb.getIndices().keySet();
        if ((keySet == null) || (keySet.isEmpty())) {
            return null;
        }
        List<DropChestObj> chestList = new LinkedList<DropChestObj>();
        Arguments arg;
        DropChestObj dropchest;
        for (String key : keySet) {
            arg = minidb.getArguments(key);
            dropchest = argToDCO(arg);
            if (dropchest != null) {
                chestList.add(dropchest);
            }
        }
        return chestList;
    }  
    
    private Arguments DCOToArg(DropChestObj dropchest) {
        // Create new argument using chest ID as key
        Arguments arg = new Arguments(dropchest.getID().toString());
        Set<Integer> filterSet;
        List<Integer> filterList;
        // Add chest name if applicable
        if (dropchest.getName() != null) {
            arg.setValue(chestNameKey, dropchest.getName());
        }
        // Add each filter if it's not empty
        if (dropchest.isFilterInUse(Utils.Filter.PUSH)) {
            filterSet = dropchest.getFilter(Utils.Filter.PUSH);
            filterList = new ArrayList<Integer>(filterSet);
            arg.setValue(pushFilterKey, filterList);
        }
        if (dropchest.isFilterInUse(Utils.Filter.PULL)) {
            filterSet = dropchest.getFilter(Utils.Filter.PULL);
            filterList = new ArrayList<Integer>(filterSet);
            arg.setValue(pullFilterKey, filterList);
        }
        if (dropchest.isFilterInUse(Utils.Filter.SUCK)) {
            filterSet = dropchest.getFilter(Utils.Filter.SUCK);
            filterList = new ArrayList<Integer>(filterSet);
            arg.setValue(suckFilterKey, filterList);
        }
        String[] locStr;
        // Add primary location
        locStr = locToString(dropchest.getPrimaryLocation());
        arg.setValue(primaryLocKey, locStr);
        // Add secondary location if it exists
        if (dropchest.getSecondaryLocation() != null) {
            locStr = locToString(dropchest.getSecondaryLocation());
            arg.setValue(secondaryLocKey, locStr);
        }
        // Add owner's name
        arg.setValue(ownerNameKey, dropchest.getOwner());
        // Add chest suck distance and suck height
        arg.setValue(chestSuckDistance, dropchest.getSuckDistance().toString());
        arg.setValue(chestSuckHeight, dropchest.getSuckHeight().toString());
        return arg;
    }
    
    private DropChestObj argToDCO(Arguments arg) {
        Integer chestID;
        try {
            chestID = Integer.valueOf(arg.getKey());
        } catch (NumberFormatException ex) {
            dc.log(Level.SEVERE, "Error loading dropchest from file, chest ID is not valid");
            return null;
        }
        String ownerName = arg.getValue(ownerNameKey);
        String chestName = arg.getValue(chestNameKey);
        String locStr[] = arg.getArray(primaryLocKey);
        Location primaryLocation = stringToLoc(locStr);
        locStr = arg.getArray(secondaryLocKey);
        Location secondaryLocation = stringToLoc(locStr);
        List<Integer> suckList = stringArrayToIntegerList(arg.getArray(suckFilterKey));
        List<Integer> pullList = stringArrayToIntegerList(arg.getArray(pullFilterKey));
        List<Integer> pushList = stringArrayToIntegerList(arg.getArray(pushFilterKey));
        Integer suckDistance;
        try {
            suckDistance = arg.getInteger(chestSuckDistance);
        } catch (NumberFormatException ex) {
            dc.log(Level.WARNING, "Error converting chest suck distance to int, setting to 0");
            suckDistance = 0;
        }
        Integer suckHeight;
        try {
            suckHeight = arg.getInteger(chestSuckHeight);
        } catch (NumberFormatException ex) {
            dc.log(Level.WARNING, "Error converting chest suck height to int, setting to 0");
            suckHeight = 0;
        }
        // Create dropchest
        DropChestObj dropchest = new DropChestObj(chestID, ownerName, chestName, primaryLocation, secondaryLocation);
        // Set values not passed to constructor
        dropchest.setSuckDistance(suckDistance);
        dropchest.setSuckHeight(suckHeight);
        if (suckList != null) {
            for (Integer matID : suckList) {
                dropchest.updateFilter(matID, Utils.Filter.SUCK);
            }
        }
        if (pullList != null) {
            for (Integer matID : pullList) {
                dropchest.updateFilter(matID, Utils.Filter.PULL);
            }
        }
        if (pushList != null) {
            for (Integer matID : pushList) {
                dropchest.updateFilter(matID, Utils.Filter.PUSH);
            }
        }
        return dropchest;
    }
    
    private List<Integer> stringArrayToIntegerList(String[] strArray) {
        if (strArray == null) {
            return null;
        }
        List<Integer> intList = new LinkedList<Integer>();
        Integer tempInt;
        for (String str : strArray) {
            try {
                tempInt = Integer.valueOf(str);
                intList.add(tempInt);
            } catch (NumberFormatException ex) {
                dc.log(Level.SEVERE, "Error converting filter from string back to int, value " + str + " is invalid");
            }
        }
        return intList;
    }
    
    private String[] locToString(Location location) {
        String[] locStr = new String[4];
        locStr[0] = location.getWorld().getName();
        locStr[1] = String.valueOf(location.getBlockX());
        locStr[2] = String.valueOf(location.getBlockY());
        locStr[3] = String.valueOf(location.getBlockZ());
        return locStr;
    }
    
    private Location stringToLoc(String locStr[]) {
        // locStr being equal to null is not uncommon, it happens any time there is no secondary location
        if (locStr == null) {
            return null;
        }
        // Location string should be length 4 or it is invalid
        if (locStr.length != 4) {
            dc.log(Level.SEVERE, "Error getting location out of string, string is not correct length");
            return null;
        }
        World world = Bukkit.getWorld(locStr[0]);
        Integer xLoc;
        Integer yLoc;
        Integer zLoc;
        try {
            xLoc = Integer.valueOf(locStr[1]);
            yLoc = Integer.valueOf(locStr[2]);
            zLoc = Integer.valueOf(locStr[3]);
        } catch (NumberFormatException ex) {
            dc.log(Level.SEVERE, "Error getting location out of string, coordinates are not integers");
            return null;
        }
        return new Location(world, xLoc, yLoc, zLoc);
    }
}
