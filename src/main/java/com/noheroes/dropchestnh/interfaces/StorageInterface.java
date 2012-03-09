/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.interfaces;

import com.noheroes.dropchestnh.internals.DropChestObj;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author PIETER
 */
public interface StorageInterface {
    
    public void saveAll(HashMap<Integer, DropChestObj> chestList);
    public void save (DropChestObj chest);
    
    public DropChestObj loadChest(Integer chestID);
    public List<DropChestObj> loadAll();
}
