/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.commands;

import com.noheroes.dropchestnh.exceptions.InsufficientPermissionsException;
import com.noheroes.dropchestnh.exceptions.MissingOrIncorrectParametersException;
import com.noheroes.dropchestnh.internals.Properties;
import com.noheroes.dropchestnh.internals.Utils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author PIETER
 */
public class ImportCmd extends Cmd {
    
    public ImportCmd(CommandSender cs, String args[]) {
        super(cs, args);
        permission = Properties.fullAdmin;
    }

    @Override
    public boolean execute() throws InsufficientPermissionsException, MissingOrIncorrectParametersException {
        errorCheck();
        String filename;
        Yaml yaml = new Yaml();
        if (args.length == 1) {
            filename = "dropchests.yml";
        }
        else {
            filename = args[1];
        }
        boolean saveSetting = Properties.saveInstantly;
        Properties.saveInstantly = false;
        File yamlFile = new File(dc.getDataFolder().getPath(), filename);
        if (!yamlFile.exists()) {
            throw new MissingOrIncorrectParametersException(filename + " does not exist, please ensure it is in the dropchest directory");
        }
        try {
            FileReader reader = new FileReader(yamlFile);
            Map<String, Object> chestList = (Map<String, Object>)yaml.load(reader);
            for (Object chest : chestList.values()) {
                Map<String, Object> chestData = (Map<String, Object>)chest;
                addChest(chestData);
            }
        } catch (FileNotFoundException ex) {
            dc.log(Level.WARNING, ex.getMessage());
        }
        finally {
            Properties.saveInstantly = saveSetting;
            dch.saveChangedChests();
        }
        return true;
    }
    
    private void addChest(Map<String, Object> chestData) {
        String chestName;
        String chestOwner;
        Integer radius;
        ArrayList<String> locArrayList;
        Map<String, Object> filterMap;
        ArrayList<String> suckList;
        ArrayList<String> pullList;
        ArrayList<String> pushList;
        try {
            radius = (Integer)chestData.get("radius");
            chestName = (String)chestData.get("name");
            chestOwner = (String)chestData.get("owner");
            locArrayList = (ArrayList<String>)chestData.get("location");
            filterMap = (Map<String, Object>)chestData.get("filter");
            suckList = (ArrayList<String>)filterMap.get("SUCK");
            pullList = (ArrayList<String>)filterMap.get("PULL");
            pushList = (ArrayList<String>)filterMap.get("PUSH");
        } catch (Exception ex) {
            dc.log(Level.WARNING, "Exception while trying to load a chest, skipping it");
            dc.log(Level.WARNING, ex.getMessage());
            return;
        }
        // The original dropchest automatically assigns the chest's ID as it's name if none was provided, we simply leave it null
        if (chestName.matches("#\\d+?")) {
            chestName = null;
        }
        String locArray[] = new String[4];
        locArray[0] = locArrayList.get(0);
        locArray[1] = locArrayList.get(2);
        locArray[2] = locArrayList.get(3);
        locArray[3] = locArrayList.get(4);
        Location location = Utils.stringToLoc(locArray);
        if (location == null) {
            dc.log(Level.WARNING, "Unable to retrieve chest location, skipping this chest");
            return;
        }
        Block block = location.getBlock();
        if (block == null) {
            dc.log(Level.WARNING, "Unable to retrieve chest block, skipping this chest");
        }
        Integer chestID;
        try {
            chestID = dch.addChest(block, chestOwner, chestName);
            if (chestID == null) {
                dc.log(Level.WARNING, "No chest found at location " + location.toString() + " skipped adding it");
                return;
            }
        } catch (MissingOrIncorrectParametersException ex) {
            dc.log(Level.WARNING, ex.getMessage());
            return;
        }
        for (String filterItem : suckList) {
            try {
                dch.updateFilter(filterItem, chestID, Utils.Filter.SUCK);
            } catch (MissingOrIncorrectParametersException ex) {
                dc.log(ex.getMessage());
            }
        }
        for (String filterItem : pullList) {
            try {
                dch.updateFilter(filterItem, chestID, Utils.Filter.PULL);
            } catch (MissingOrIncorrectParametersException ex) {
                dc.log(ex.getMessage());
            }
        }
        for (String filterItem : pushList) {
            try {
                dch.updateFilter(filterItem, chestID, Utils.Filter.PUSH);
            } catch (MissingOrIncorrectParametersException ex) {
                dc.log(ex.getMessage());
            }
        }
        if (dch.isFilterInUse(chestID, Utils.Filter.SUCK) && (radius != null) && (radius != 0)) {
            Integer distance = Math.round((float)radius / 2);
            dch.setSuckDistance(chestID, distance);
            dch.setSuckHeight(chestID, 2);
        }
    }
}
