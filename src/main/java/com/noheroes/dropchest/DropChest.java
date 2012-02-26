/*
 * Copyright (C) 2011 Moritz Schmale <narrow.m@gmail.com>
 *
 * DropChest is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/gpl.html>.
 */

package com.noheroes.dropchest;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

public class DropChest extends JavaPlugin {
	private List<DropChestItem> chests = new ArrayList<DropChestItem>();
	private Map<Integer, DropChestItem> chestsHashInteger = new HashMap<Integer, DropChestItem>();
	private Map<String, DropChestItem> chestsHashName = new HashMap<String, DropChestItem>();
	private Map<Integer, DropChestItem> chestsHashBlocks = new HashMap<Integer, DropChestItem>();
	private final DropChestPlayerListener playerListener = new DropChestPlayerListener(this);
	private final DropChestBlockListener blockListener = new DropChestBlockListener(this);
	private final DropChestWorldListener worldListener = new DropChestWorldListener();
	private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
	private EntityWatcher entityWatcher;
	public PermissionHandler Permissions = null;
	@SuppressWarnings("unused")
	private String version = "0.0";
	private DropChestVehicleListener vehicleListener = new DropChestVehicleListener(this);
	public Logger log;
	private int watcherid;
	public Configuration config;
        private boolean isLoading;

	public DropChest() {
		// NOTE: Event registration should be done in onEnable not here as all events are unregistered when a plugin is disabled
		DropChestPlayer.plugin = this;
		DropChestWorldListener.plugin = this;
	}

	public void onEnable() {
		log = getServer().getLogger();
		setupPermissions();

		//Register the Entity Watcher
		entityWatcher = new EntityWatcher(this);
		watcherid = getServer().getScheduler().scheduleSyncRepeatingTask(this, entityWatcher, 10,10);
		// Register our events
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
		pm.registerEvent(Type.VEHICLE_MOVE, vehicleListener, Priority.Normal, this);
		//pm.registerEvent(Type.CHUNK_UNLOAD, worldListener, Priority.Normal, this);
		pm.registerEvent(Type.REDSTONE_CHANGE, blockListener, Priority.Normal, this);

		//Read plugin file
		PluginDescriptionFile pdfFile = this.getDescription();
		log.log( Level.INFO, pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
		version = pdfFile.getVersion();

		//Read configuration
		File settings = new File(getDataFolder().getAbsolutePath()+"/dropchest.cfg");
		config = new Configuration(settings);

		// Load our stuff
		load();
	}

	public void addChest(DropChestItem item){
		chestsHashInteger.put(item.getId(), item);
		chestsHashName.put(item.getName(), item);
		chestsHashBlocks.put(item.getBlock().getLocation().hashCode(), item);
		chests.add(item);
		DropChestPlayer player = item.getOwnerDCPlayer();
		player.addChest(item);
		save();
	}

	public void setupPermissions() {
		try{
			Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");

			if(this.Permissions == null) {
				try{
					this.Permissions = ((Permissions)test).getHandler();
				} catch(Exception e) {
					this.Permissions = null;
					//log.log(Level.WARNING, "Permissions is not enabled! All Operations are allowed!");
				}
			}
		} catch(java.lang.NoClassDefFoundError e){
			this.Permissions = null;
			//log.log(Level.WARNING, "Permissions not found! All Operations are allowed!");
		}
	}

	private void load(){
                isLoading = true;
                try {
			File dir = getDataFolder();
			if(!dir.exists()){
				log.log(Level.INFO, "DropChest directory does not exist. Creating on next save!");
				return;
			}
			File yamlFile = new File(dir, "dropchests.yml");
			if(yamlFile.exists()){
				//TODO: Do yaml loading
				Yaml yaml = new Yaml();
				try {
					FileReader reader = new FileReader(yamlFile);
					@SuppressWarnings("unchecked")
					Map<String, Object> chests = (Map<String, Object>) yaml.load(reader);
					for(Object chest:chests.values()){
						@SuppressWarnings("unchecked")
						Map<String, Object> chestconv = (Map<String, Object>)chest;
						DropChestItem item = new DropChestItem(chestconv, this);
						if(item.isLoadedProperly()){
							addChest(item);
							//log.log(Level.INFO, "Chest loaded.");
						}
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return;
			}
            } catch (Exception e) {
                    e.printStackTrace();
            } finally {
                isLoading = false;
            }
	}

	public void save(){
		File dir = getDataFolder();
		if(!dir.exists()){
			log.log(Level.INFO, "Creating DropChest directory.");
			dir.mkdir();
		}
		File file = new File(dir.getAbsolutePath()+"/dropchests.yml");
		if(!file.exists()){
			log.log(Level.INFO, "no file. Trying to create it.");
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
		try {
			Map<String, Object> data = new HashMap<String, Object>();
			for(DropChestItem dci:getChests()){
				Map<String, Object> chest = new HashMap<String, Object>();
				dci.save(chest);
				data.put(""+dci.getId(), chest);
			}
			Yaml yaml = new Yaml();
			FileWriter writer = new FileWriter(file);
			yaml.dump(data, writer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isDebugging(final Player player) {
		if (debugees.containsKey(player)) {
			return debugees.get(player);
		} else {
			return false;
		}
	}

	public void setDebugging(final Player player, final boolean value) {
		debugees.put(player, value);
	}

	@Override
	public void onDisable() {
		getServer().getScheduler().cancelTask(watcherid);
		save();
	}

	public Boolean isNear(Location loc1, Location loc2, double maxDistance){
		if(loc1.getWorld().getId()!=loc2.getWorld().getId()){
			return false;
		}
		double x1 = loc1.getX(), x2 = loc2.getX(), y1 = loc1.getY(), y2 = loc2.getY(), z1 = loc1.getZ(), z2 = loc2.getZ();
		double dx = x1-x2, dy = y1-y2, dz = z1-z2;
		double maxDistance2 = maxDistance*maxDistance;
		double distance2 = dx*dx+dy*dy+dz*dz;
		return distance2 <= maxDistance2;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String args[])
	{
		Player player = null;
		DropChestPlayer dplayer = null;
		if(sender.getClass().getName().contains("Player")){
			player = (Player)sender;
			dplayer = DropChestPlayer.getPlayerByName(player.getName());
		}
		if(cmd.getName().equals("dropchest"))
		{
			boolean syntaxerror=false;
			if(!hasPermission(player, "dropchest")){
				player.sendMessage("You may not use DropChest. Please ask your Operator to enable this awesome plugin for you.");
				return false;
			}
			if(args.length==0){
				syntaxerror = true;
			} else {
				if(args[0].equalsIgnoreCase("add")){
					/*****************
					 *	  ADD	  *
					 *****************/
					if(!hasPermission(player, "dropchest.create")){
						player.sendMessage("You may not create DropChests.");
						return false;
					}
					DropChestPlayer player2 = DropChestPlayer.getPlayerByName(player.getName());
					player2.setChestRequestType(ChestRequestType.CREATE);
					int requestedRadius = config.getDefaultRadius();
					if(args.length==2&&hasPermission(player, "dropchest.radius.set")){
						requestedRadius = (int)Integer.valueOf(args[1]);
						if(requestedRadius>getMaximumRadius(player)&&!hasPermission(player, "dropchest.radius.setBig")){
							requestedRadius = getMaximumRadius(player);
						}
					}
					player2.setRequestedRadius(requestedRadius);
					requestedRadius = config.getDefaultRadius();
					sender.sendMessage(ChatColor.GREEN.toString()+"Now rightclick on the Chest that you want to add");
				} else if(args[0].equalsIgnoreCase("remove")){
					/*****************
					 *	 REMOVE	*
					 *****************/
					if(!hasPermission(player, "dropchest.remove")){
						player.sendMessage("You may not remove DropChests.");
						return false;
					}
					if(args.length==2){
						DropChestItem dci = getChestByIdOrName(args[1]);
						if(dci!=null){
							if(ownsChest(dci, sender)){
								removeChest(dci);
								save();
								sender.sendMessage(ChatColor.RED.toString()+"Removed Chest.");
							} else {
								sender.sendMessage("That's not your chest.");
							}
						} else {
							sender.sendMessage(ChatColor.RED+"Dropchest not found.");
						}
					} else {
						syntaxerror = true;
					}
				} else if(args[0].equalsIgnoreCase("list")){
					/*****************
					 *	  LIST	 *
					 *****************/
					if(!hasPermission(player, "dropchest.list")){
						player.sendMessage("You may not list DropChests.");
						return false;
					}
					int i = 1;

					//Page limit is 6 items per page
					//calculation of needed pages
					List<DropChestItem> chests;
					if(dplayer!=null&&!(args.length>2&&args[2].equals("all"))){
						chests = dplayer.getChests();
					} else {
						chests = this.chests;
					}
					int num = chests.size();
					int needed = (int) Math.ceil((double)num/6.0);
					int current = 1;
					if(args.length==2){
						current = Integer.valueOf(args[1]);
					}
					if(current>needed)
						current = 1;
					if(needed!=1){
						sender.sendMessage(ChatColor.BLUE.toString()+"Page "+String.valueOf(current)+" of "+ String.valueOf(needed));
					}
					sender.sendMessage(ChatColor.BLUE.toString()+"Name | % full | filters | radius");
					sender.sendMessage(ChatColor.BLUE.toString()+"------");
					for(i = (current-1)*6;i<Math.min(current*6, chests.size()); i++){
						sender.sendMessage(chests.get(i).listString());
					}

				} else if(args[0].equalsIgnoreCase("tp")){
					/*****************
					 *   TELEPORT	*
					 *****************/
					if(!hasPermission(player, "dropchest.teleport")){
						player.sendMessage("You may not teleport to DropChests.");
						return false;
					}
					DropChestItem dci = getChestByIdOrName(args[1]);
					if(dci!=null){
						if(player!=null){
							player.teleport(dci.getBlock().getLocation());
						}
					} else {
						sender.sendMessage(ChatColor.RED.toString()+"This chest does not exist.");
					}
				} else if(args[0].equalsIgnoreCase("setradius")){
					/*****************
					 *   SETRADIUS   *
					 *****************/
					if(!hasPermission(player, "dropchest.radius.set")){
						player.sendMessage("You may not set the radius of a DropChest.");
						return false;
					}
					if(args.length==3){
						int radius = Integer.valueOf(args[2]);
						DropChestItem dci = getChestByIdOrName(args[1]);
						if(dci != null){
							if(ownsChest(dci, sender)){
								boolean force=true;
								if(!hasPermission(player, "dropchest.radius.setBig")){
									force =  false;
								}
								if(radius>getMaximumRadius(player)&&!force){
									radius = getMaximumRadius(player);
								}
								dci.setRadius(radius);
								sender.sendMessage("Radius of Chest #"+dci.getId()+" set to "+String.valueOf(dci.getRadius()));
								save();
							} else {
								sender.sendMessage("That's not your chest.");
							}
						} else {
							syntaxerror = true;
						}
					} else {
						syntaxerror = true;
					}
				} else if(args[0].equalsIgnoreCase("setdelay")){
					/*****************
					 *   SETDELAY   *
					 *****************/
					if(!hasPermission(player, "dropchest.delay")){
						player.sendMessage("You may not set the delay of a DropChest.");
						return true;
					}
					if(args.length==3){
						int delay = Integer.valueOf(args[2]);
						delay = (delay > 0 ? delay : 0); //Derp derp, I'll set a negative delay! Nope.
						DropChestItem dci = getChestByIdOrName(args[1]);
						if(dci != null){
							if(ownsChest(dci, sender)){
								dci.setDelay(delay);
								sender.sendMessage(ChatColor.GREEN+"Delay of Chest '"+dci.getName()+"' set to "+String.valueOf(dci.getDelay()) + " milliseconds.");
								save();
							} else {
								sender.sendMessage("That's not your chest.");
							}
						} else {
							syntaxerror = true;
						}
					} else {
						syntaxerror = true;
					}
				}
				else if(args[0].equalsIgnoreCase("which")){
					/*****************
					 *	 WHICH	 *
					 *****************/
					if(!hasPermission(player, "dropchest.which")){
						player.sendMessage("You may not use this command.");
						return true;
					}
					if(player != null){
						DropChestPlayer pl = DropChestPlayer.getPlayerByName(player.getName());
						pl.setChestRequestType(ChestRequestType.WHICH);
						sender.sendMessage(ChatColor.GREEN.toString()+"Now rightclick on a chest to get its properties.");
					}

				}else if(args[0].equalsIgnoreCase("filter")){
					/*****************
					 *	 FILTER	*
					 *****************/
					if(!hasPermission(player, "dropchest.filter"))
					{
						player.sendMessage("You may not use DropChest filters!");
						return false;
					}
					//dropchest filter {suck|push|pull|finish} [{chestid} {itemid|itemtype|clear}]
					if(args.length>=2){
						String typestring = args[1];
						FilterType type = null;
						try{
							type = FilterType.valueOf(typestring.toUpperCase());
						} catch(java.lang.IllegalArgumentException e){
							type = null;
						}
						if(type!=null){
							if(args.length==2&&dplayer!=null){
								dplayer.setEditingFilter(true);
								dplayer.setEditingFilterType(type);
								dplayer.getPlayer().sendMessage(ChatColor.GREEN.toString()+"You're now entering interactive mode for filtering "+type.toString().toLowerCase()+"ed items");
							} else if(dplayer==null&&args.length==2) {
								sender.sendMessage("You can't use interactive mode from a console!");
							} else if(args.length==4) {
								String itemstring = args[3];
								DropChestItem chest = getChestByIdOrName(args[2]);
								Material item = null;
								if(itemstring.equalsIgnoreCase("clear")){
									if(ownsChest(chest, sender)){
										chest.getFilter(type).clear();
										sender.sendMessage(ChatColor.GREEN.toString()+"Filter cleared.");
									} else {
										sender.sendMessage("That's not your chest.");
									}
								} else if(itemstring.equalsIgnoreCase("all")){
									if(chest!=null&&ownsChest(chest, sender)){
										List<Material> filter = chest.getFilter(type);
										for (Material m:Material.values()){
											if(!m.equals(Material.AIR)){
												if(!filter.contains(m)){
													filter.add(m);
												}
											}
										}
										sender.sendMessage(ChatColor.GREEN+"All items set.");
									}
								} else {
									if(chest!=null){
										if(ownsChest(chest, sender)){
											try{
												item = Material.valueOf(itemstring.toUpperCase());
											} catch (java.lang.IllegalArgumentException e){
												item = null;
											}
											boolean materialNotFound = false;
											if(item==null){
												Integer itemid = null;
												try{
													itemid = Integer.valueOf(itemstring);
												} catch(java.lang.NumberFormatException e)
												{
													itemid = null;
												}
												if(itemid!=null){
													item = Material.getMaterial(itemid);
													if(item==null){
														materialNotFound = true;
													}
												} else {
													materialNotFound = true;
												}
											}
											if(!materialNotFound){
												List<Material> filter = chest.getFilter(type);
												if(filter.contains(item)){
													filter.remove(item);
													sender.sendMessage(ChatColor.GREEN.toString()+item.toString()+" is no more being "+type.toString().toLowerCase()+"ed.");
												} else {
													filter.add(item);
													sender.sendMessage(ChatColor.GREEN.toString()+item.toString()+" is now being "+type.toString().toLowerCase()+"ed.");
												}
											} else {
												sender.sendMessage("Material "+itemstring+" not found.");
											}
											save();
										} else {
											sender.sendMessage("That's not your chest.");
										}


									} else {
										log.log(Level.INFO,"No such chest "+args[1]+".");
										syntaxerror = true;
									}
								}
							} else {
								log.log(Level.INFO,"Too much arguments.");
								syntaxerror = true;
							}
						} else if(typestring.equalsIgnoreCase("finish")) {
							if(dplayer!=null)
							{
								dplayer.setEditingFilter(false);
								dplayer.getPlayer().sendMessage(ChatColor.GREEN.toString()+"You're now leaving interactive mode!");
							} else {
								sender.sendMessage("You can't use interactive mode from a console!");
							}
						} else {
							log.log(Level.INFO,"Filter type not found.");
							syntaxerror = true;
						}
					}
				} else if(args[0].equalsIgnoreCase("setname")){
					/*****************
					 *	SETNAME	*
					 *****************/
					if(!hasPermission(player, "dropchest"))
					{
						player.sendMessage("You may not set names of Dropchests!");
						return false;
					}
					if(args.length==3){
						String name = args[2];
						DropChestItem item = getChestByIdOrName(args[1]);
						if(item!=null){
							if(ownsChest(item, sender)){
								item.setName(name);
								sender.sendMessage(ChatColor.GREEN+"Set name to "+item.getName());
								updateName(item);
								save();
							} else {
								sender.sendMessage("That's not your chest.");
							}
						}
					} else {
						syntaxerror = true;
					}
				} else if(args[0].equalsIgnoreCase("protect")){
					if(args.length==3){
						String cheststring = args[1];
						String mode = args[2];
						DropChestItem item = getChestByIdOrName(cheststring);
						if(item==null){
							sender.sendMessage("This chest does not exist.");
							return false;
						}
						if(!config.isLetUsersProtectChests()){
							sender.sendMessage("Chest protection is disabled on this server.");
							return false;
						}
						boolean mayProtect = true;
						if(sender instanceof Player){
							mayProtect = hasPermission((Player)sender, "dropchest.protect");
						}
						if(ownsChest(item, sender)&&mayProtect){
							if(mode.equalsIgnoreCase("off")){
								item.setProtect(false);
								sender.sendMessage("Chest is not anymore protected.");
							} else if(mode.equalsIgnoreCase("on")){
								item.setProtect(true);
								sender.sendMessage("Chest is now protected.");
							} else {
								syntaxerror = true;
							}
							save();
						} else {
							sender.sendMessage("You may not set this attribute.");
						}
					}
				} else if(args[0].equalsIgnoreCase("setowner")){
					if(args.length==3){
						String cheststring = args[1];
						String newowner = args[2];
						DropChestItem item = getChestByIdOrName(cheststring);
						if(item==null){
							sender.sendMessage("Chest not found.");
							return false;
						}
						if(ownsChest(item, sender)){
							item.setOwner(newowner);
							save();
							sender.sendMessage("Owner of chest "+item.getId()+" set to "+newowner+".");
						} else {
							sender.sendMessage("That's not your chest!");
						}
					} else {
						syntaxerror = true;
					}
				} else if(args[0].equalsIgnoreCase("info")){
					if(args.length==2){
						DropChestItem chest = getChestByIdOrName(args[1]);
						if(chest!=null){
							sender.sendMessage(chest.info());
						} else {
							sender.sendMessage("Chest not found.");
						}
					}
				} else {
					log.log(Level.INFO, "Command not found.");
					syntaxerror = true;
				}
			}

			if(syntaxerror){
				if(onPermissionSend(sender, "dropchest", ChatColor.BLUE.toString()+"DropChest Commands:")){
					sender.sendMessage(ChatColor.BLUE.toString()+"{this} is a required variable argument");
					sender.sendMessage(ChatColor.BLUE.toString()+"[this=x] can be omitted, x is standard");
					sender.sendMessage(ChatColor.BLUE.toString()+"{chest} can be either a name or an id");
					onPermissionSend(sender, "dropchest.create", " /dropchest add [radius=2]");
					onPermissionSend(sender, "dropchest.remove", " /dropchest remove {chest}");
					onPermissionSend(sender, "dropchest.list", " /dropchest list [page=1]");
					onPermissionSend(sender, "dropchest.list", "/dropchest info {chestname|chestid}");
					onPermissionSend(sender, "dropchest.radius.set", " /dropchest setradius {chest} {radius}");
					onPermissionSend(sender, "dropchest.which", " /dropchest which");
					onPermissionSend(sender, "dropchest.teleport", " /dropchest tp {chest}");
					onPermissionSend(sender, "dropchest.filter", " /dropchest filter {suck|push|pull} [{chest} {itemid|itemtype|clear|all}]");
					onPermissionSend(sender, "dropchest.delay", " /dropchest setdelay {chest} {delay}");
                                        onPermissionSend(sender, "dropchest", " /dropchest setname {chest} {name}");
					int max = getMaximumRadius(player);
					String maxs = String.valueOf(max);
					if(hasPermission(player, "dropchest.radius.setBig")||max==65536){
						maxs = "unlimited";
					}
					sender.sendMessage("Your maximum radius is "+maxs);
				}
			}
		}
		if(cmd.getName().equals("dcitem")){
			if(args.length==1){
				int id = 0;
				Material m = null;
				try{
					id = Integer.valueOf(args[0]);
				} catch(Exception e){
					m = Material.matchMaterial(args[0].toUpperCase());
				}
				if(id!=0){
					m = Material.getMaterial(id);
					if(m!=null){
						sender.sendMessage(ChatColor.YELLOW.toString()+id+ChatColor.WHITE+" is "+ChatColor.YELLOW.toString()+m.toString());
					} else {
						sender.sendMessage(ChatColor.RED+"That item does not exist.");
					}
				} else {
					if(m!=null){
						id = m.getId();
						sender.sendMessage(ChatColor.YELLOW+m.toString()+ChatColor.WHITE+" is "+ChatColor.YELLOW+id);
					} else {
						sender.sendMessage(ChatColor.RED+"That item does not exist.");
					}
				}
				return true;
			}
		}
		if(cmd.getName().equals("chestinfo")){
			if(!hasPermission(player, "dropchest.moderator")){
				sender.sendMessage("You may not use this command");
				return false;
			}
			if(args.length==1){
				DropChestItem dci = getChestByIdOrName(args[0]);
				if(dci==null){
					sender.sendMessage("This chest doesn't exist.");
					return false;
				}
				String info = chestInformation(dci.getInventory(),"of "+dci.getName());
				if(dplayer != null){
					dplayer.sendMessage(info);
				} else {
					sender.sendMessage(info);
				}
				return true;
			} else {
				if(dplayer != null){
					dplayer.setChestRequestType(ChestRequestType.CHESTINFO);
					dplayer.sendMessage("Rightclick on any chest to get information");
				} else {
					sender.sendMessage("You can't use interactive commands from the console");
					return false;
				}
			}
		}
		return false;
	}

	public int getChestCount() {
		return chests.size();
	}


	public void removeChest(DropChestItem dci) {
		chests.remove(dci);
		chestsHashBlocks.remove(dci.getBlock().getLocation().hashCode());
		chestsHashInteger.remove(dci.getId());
		chestsHashName.remove(dci.getName());
		dci.getOwnerDCPlayer().removeChest(dci);
	}

	public void updateName(DropChestItem dci){
		chestsHashName.put(dci.getName(), dci);
	}


	public int getMaximumRadius(Player player) {
		if(player == null){
			return 65536;
		} else {
			if(Permissions==null){
				return 65536;
			}
			int max = Permissions.getUserPermissionInteger(player.getWorld().getName(), player.getName(), "dropchestmaxradius");
			if(max==-1){
				max = Permissions.getGroupPermissionInteger(player.getWorld().getName(), Permissions.getGroup(player.getWorld().getName(), player.getName()), "dropchestmaxradius");
				if(max==-1)
					max = config.getFallbackRadius();
			}
			return max;
		}
	}

	public boolean hasPermission(Player player, String node){
		if(Permissions==null)
		{
			return player.hasPermission(node);
		}
		if(player==null)
		{
			return true;
		} else {
			if(Permissions.has(player, "*")){
				return true;
			} else {
				return Permissions.has(player, node);
			}
		}
	}

	public boolean onPermissionSend(CommandSender sender, String node, String message){
		Player player = null;
		if(sender.getClass().getName().contains("Player")){
			player = (Player)sender;
		}
		if(hasPermission(player, node)){
			sender.sendMessage(message);
			return true;
		} else {
			return false;
		}
	}

	public World getWorldWithId(long worldid){
		for (World w : getServer().getWorlds()){
			if(w.getId()==worldid){
				return w;
			}
		}
		if(worldid==0){
			return getServer().getWorlds().get(0);
		}
		return null;
	}

	public Location locationOf(Block block){
		return block.getLocation();
	}

	public boolean locationsEqual(Location loc1, Location loc2){
		return loc1.equals(loc2);
	}

	public DropChestItem getChestByBlock(Block block)
	{
		return chestsHashBlocks.get(block.getLocation().hashCode());
	}

	public DropChestItem getChestById(int id){
		return chestsHashInteger.get(id);
	}

	public DropChestItem getChestByIdOrName(String arg){
		int id = 0;
		DropChestItem dci = null;
		try{
			id = Integer.valueOf(arg);
			dci = getChestById(id);
		} catch(Exception e){
			dci = getChestByName(arg);
		}
		return dci;
	}

	public DropChestItem getChestByName(String name){
		if(name.equals(""))
			return null;
		return chestsHashName.get(name);
	}

	public boolean ownsChest(DropChestItem dci, CommandSender sender){
		if(sender.isOp()){
			return true;
		}
		if(sender instanceof Player){
			Player p = (Player)sender;
			if(dci.getOwner().equals(p.getName())){
				return true;
			}
			if(hasPermission(p, "dropchest.moderator")){
				return true;
			}
		}
		if(dci.getOwner().equals("")){
			return true;
		}
		return false;
	}

	public static String chestInformation(Inventory inv, String identifier){
		HashMap<Material, Integer> map = new HashMap<Material, Integer>();
		for(int i = 0; i<inv.getSize(); i++){
			ItemStack stack = inv.getItem(i);
			if(stack!=null&&stack.getTypeId()!=0){
				Material mat = stack.getType();
				Integer count = stack.getAmount();
				if(map.containsKey(mat)){
					int excnt = map.get(mat);
					count+=excnt;
				}
				map.put(mat, count);
			}
		}
		String ret = ChatColor.GREEN+"Statistics for inventory "+identifier+":\n";
		int i = 0;
		for(Material mat:map.keySet()){
			int count = map.get(mat);
			ret+=ChatColor.YELLOW+mat.toString()+ChatColor.WHITE+": "+count;
			if(i!=map.size()-1){
				ret+="\n";
			}
			i++;
		}
		return ret;
	}

	public List<DropChestItem> getChests() {
		return Collections.unmodifiableList(chests);
	}

	public boolean chestExists(Block b){
		return chestsHashBlocks.containsKey(b);
	}
}
