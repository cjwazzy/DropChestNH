/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.internals;

import com.miniDC.Arguments;
import com.miniDC.Mini;
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
import org.bukkit.Material;
import org.bukkit.World;
import org.yaml.snakeyaml.Yaml;

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
    private static final String almostFullWarning = "warnfull";
    private static final String almostFullWarningThreshold = "warnthresh";
    private static final String almostFullWarningDelay = "warndelay";
    private Mini minidb;
    
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
    

    public void removeChest(DropChestObj chest) {
        Integer chestID = chest.getID();
        minidb.removeIndex(chestID.toString());
        minidb.update();
    }
    
    public void write() {
        minidb.update();
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
        locStr = Utils.locToString(dropchest.getPrimaryLocation());
        arg.setValue(primaryLocKey, locStr);
        // Add secondary location if it exists
        if (dropchest.getSecondaryLocation() != null) {
            locStr = Utils.locToString(dropchest.getSecondaryLocation());
            arg.setValue(secondaryLocKey, locStr);
        }
        // Add chest warning info
        arg.setValue(almostFullWarning, dropchest.getAlmostFullWarning());
        arg.setValue(almostFullWarningThreshold, dropchest.getAlmostFullThreshold());
        arg.setValue(almostFullWarningDelay, dropchest.getWarnDelay());
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
        Location primaryLocation = Utils.stringToLoc(locStr);
        locStr = arg.getArray(secondaryLocKey);
        Location secondaryLocation = Utils.stringToLoc(locStr);
        // Location couldn't be loaded
        if (primaryLocation == null) {
            dc.log(Level.WARNING, "Error loading a chest, primary location is not correct");
            return null;
        }
        // No chest at primary location, delete dropchest entry
        if (!primaryLocation.getBlock().getState().getType().equals(Material.CHEST)) {
            dc.log("Error loading dropchest from file, no chest exists at that location, removing...");
            minidb.removeIndex(chestID.toString());
            return null;
        }
        // If secondary location exists and is not a chest, set to null
        if (secondaryLocation != null) {
            if (!secondaryLocation.getBlock().getState().getType().equals(Material.CHEST)) {
                secondaryLocation = null;
            }
        }
        
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
        if (suckDistance > Properties.maxDistance) {
            suckDistance = Properties.maxDistance;
        }
        if (suckHeight > Properties.maxHeight) {
            suckHeight = Properties.maxHeight;
        }
        boolean warnFull = arg.getBoolean(almostFullWarning);
        Integer warnThreshold;
        Integer warnDelay;
        try {
            warnThreshold = arg.getInteger(almostFullWarningThreshold);
            warnDelay = arg.getInteger(almostFullWarningDelay);
        } catch (NumberFormatException ex) {
            dc.log(Level.WARNING, "Error converting chest warning information back to int, using defaults");
            warnThreshold = Properties.defaultAlmostFullWarningThreshold;
            warnDelay = Properties.defaultWarningDelay;
        }
        
        // Create dropchest
        DropChestObj dropchest = new DropChestObj(chestID, ownerName, chestName, primaryLocation, secondaryLocation);
        // Set values not passed to constructor
        dropchest.setSuckDistance(suckDistance);
        dropchest.setSuckHeight(suckHeight);
        dropchest.setWarnDelay(warnDelay);
        dropchest.setAlmostFullThreshold(warnThreshold);
        dropchest.setAlmostFullWarning(warnFull);
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
}
