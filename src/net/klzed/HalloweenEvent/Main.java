package net.klzed.HalloweenEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin implements Listener {
	
	private Map<String, ArrayList<String>> activeCommands = new HashMap<String,ArrayList<String>>();
	private Map<Location,ArrayList<String>> halloweenChests = new HashMap<Location,ArrayList<String>>();
	private Map<Location,ArrayList<String>> openedChests = new HashMap<Location,ArrayList<String>>();
	private Map<Location,FileConfiguration> chestConfigs = new HashMap<Location,FileConfiguration>();
	
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		PluginManager pm = getServer().getPluginManager();
		if (pm.getPermission("halloweenevent.all") == null) {
			pm.addPermission(new Permission("halloweenevent.all", PermissionDefault.OP));
		}
		
		Bukkit.getLogger().log(Level.INFO, "Loading all Halloween Chests before enabling.");
		File[] listOfChests = getChestsFolder().listFiles();
		for (int i = 0; i < listOfChests.length; i++) {
			if(listOfChests[i].isFile() && listOfChests[i].getName().endsWith(".yml")) {
				FileConfiguration chest = getChestConfig(listOfChests[i]);
				Bukkit.getLogger().log(Level.INFO, "Trying to load chest config: " + listOfChests[i].getName());
				Location l = new Location(Bukkit.getWorld("world"), chest.getDouble("location.x"), chest.getDouble("location.y"), chest.getDouble("location.z"));
				ArrayList<String> item = new ArrayList<String>();
				item.add(chest.getString("item.id"));
				item.add(chest.getString("item.dv"));
				if (chest.getString("item.name") != null) {
					item.add(chest.getString("item.name"));
				}

				chestConfigs.put(l, chest);
				halloweenChests.put(l, item);
				openedChests.put(l, (ArrayList<String>) chest.getStringList("playersUsed"));
				
				Bukkit.getLogger().log(Level.INFO, "Loaded chest at x"+l.getBlockX()+"y"+l.getBlockY()+"z"+l.getBlockZ()+ "!");
			}
		}
		Bukkit.getLogger().log(Level.INFO, "All Halloween Chests have been loaded. Enabling.");
	}

	@Override
	public void onDisable() {
		Bukkit.getLogger().log(Level.INFO, "Saving all Halloween Chests before disabling.");
		for (Entry<Location, ArrayList<String>> entry : halloweenChests.entrySet()) {
			Location l = entry.getKey();
			ArrayList<String> item = entry.getValue();
			if (chestConfigs.containsKey(l)) {
				chestConfigs.get(l).set("location.x", l.getBlockX());
				chestConfigs.get(l).set("location.y", l.getBlockY());
				chestConfigs.get(l).set("location.z", l.getBlockZ());
				chestConfigs.get(l).set("item.id", item.get(0));
				if (item.size() < 2) {
					chestConfigs.get(l).set("item.dv", "0");
				} else {
					chestConfigs.get(l).set("item.dv", item.get(1));
				}
				
				if (item.size() == 3) {
					chestConfigs.get(l).set("item.name", item.get(2));
				}
				chestConfigs.get(l).set("playersUsed",openedChests.get(l));
				String fileName = "x"+l.getBlockX()+"y"+l.getBlockY()+"z"+l.getBlockZ()+".yml";
				try {
					chestConfigs.get(l).save(new File(getChestsFolder(), fileName));
				} catch (IOException e) {
					Bukkit.getLogger().log(Level.WARNING, "Could not save chest: "+fileName+"!");
					Bukkit.getLogger().log(Level.WARNING, e.toString());
				}
		
			}
		}
		PluginManager pm = getServer().getPluginManager();
		pm.removePermission("halloweenevent.all");
		Bukkit.getLogger().log(Level.INFO, "Done loading all Halloween Chests. Enabling.");
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		if (command.getName().equalsIgnoreCase("halloweenchest")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("Console cannot set a halloween chest.");
				return true;
			}
			if (args.length < 1) {
				sender.sendMessage("Usage: /halloweenchest <set/delete> <id[:dv]>");
				return true;
			}
			
			if (sender instanceof Player) {
				Player p = (Player) sender;
				if (!p.hasPermission("halloweenevent.all")) {
					p.sendMessage(ChatColor.RED + "You do not have permission to run this command.");
					return true;
				}
			}
			
			if (args[0].equals("set")) {
				Player p = (Player) sender;
				if (activeCommands.containsKey(p.getName())) {
					activeCommands.remove(p.getName());
				}
				try {
					String[] item = args[1].split(":");
					int id = Integer.parseInt(item[0]);
					if (item.length > 1) {
						//for verification purposes.
						Short.parseShort(item[1]);
					}
					if (Material.getMaterial(id) == null) {
						p.sendMessage("Cannot find the specified item.");
						return true;
					}
				} catch (NumberFormatException ex) {
					p.sendMessage("Your item needs to be an item ID[:DV].");
					return true;
				}
				if (args.length > 2) {
					String name = args[2];
					name = name.replace("_"," ");
					args[2] = name;
				}
				
				ArrayList<String> argsList = new ArrayList<String>();
				
				for (int i = 0; i < args.length; i++ ) {
					argsList.add(args[i]);
				}
				
				
				activeCommands.put(p.getName(), argsList);
				p.sendMessage("Please left click a chest to set the item-spawning.");
				return true;
			} else if (args[0].equals("delete")) {
				Player p = (Player) sender;
				if (activeCommands.containsKey(p.getName())) {
					activeCommands.remove(p.getName());
				}
				
				ArrayList<String> argsList = new ArrayList<String>();
				
				for (int i = 0; i < args.length; i++ ) {
					argsList.add(args[i]);
				}
				
				activeCommands.put(p.getName(), argsList);
				p.sendMessage("Please left click a chest to delete the item-spawning.");
				return true;
			}
			
		} else if (command.getName().equalsIgnoreCase("save-chests")) {
			sender.sendMessage("Working on it!~ /save-chests");
			for (Entry<Location, ArrayList<String>> entry : halloweenChests.entrySet()) {
				Location l = entry.getKey();
				sender.sendMessage("Saving chest at: " + l.toString());
				ArrayList<String> item = entry.getValue();
				if (chestConfigs.containsKey(l)) {
					chestConfigs.get(l).set("location.x", l.getBlockX());
					chestConfigs.get(l).set("location.y", l.getBlockY());
					chestConfigs.get(l).set("location.z", l.getBlockZ());
					chestConfigs.get(l).set("item.id", item.get(0));
					if (item.size() < 2) {
						chestConfigs.get(l).set("item.dv", "0");
					} else {
						chestConfigs.get(l).set("item.dv", item.get(1));
					}
					
					if (item.size() == 3) {
						chestConfigs.get(l).set("item.name", item.get(2));
					}
					chestConfigs.get(l).set("playersUsed",openedChests.get(l));
					String fileName = "x"+l.getBlockX()+"y"+l.getBlockY()+"z"+l.getBlockZ()+".yml";
					try {
						chestConfigs.get(l).save(new File(getChestsFolder(), fileName));
						sender.sendMessage("Saved "+fileName+" !~");
					} catch (IOException e) {
						sender.sendMessage("Could not save chest: "+fileName+"!");
						sender.sendMessage(e.toString());
					}
			
				}
			}
			sender.sendMessage("Chests saved!~");
			return true;
		} else if (command.getName().equalsIgnoreCase("load-chests")) {
			sender.sendMessage("Working on it!~ /load-chests");
			File[] listOfChests = getChestsFolder().listFiles();
			sender.sendMessage(listOfChests.toString());
			for (int i = 0; i < listOfChests.length; i++) {
				if(listOfChests[i].isFile() && listOfChests[i].getName().endsWith(".yml")) {
					FileConfiguration chest = getChestConfig(listOfChests[i]);
					sender.sendMessage("Trying to load chest config: " + listOfChests[i].getName());
					Location l = new Location(Bukkit.getWorld("world"), chest.getDouble("location.x"), chest.getDouble("location.y"), chest.getDouble("location.z"));
					ArrayList<String> item = new ArrayList<String>();
					item.add(chest.getString("item.id"));
					item.add(chest.getString("item.dv"));
					if (chest.getString("item.name") != null) {
						item.add(chest.getString("item.name"));
					}

					chestConfigs.put(l, chest);
					halloweenChests.put(l, item);
					openedChests.put(l, (ArrayList<String>) chest.getStringList("playersUsed"));
					
					sender.sendMessage("Loaded chest at x"+l.getBlockX()+"y"+l.getBlockY()+"z"+l.getBlockZ()+ "!");
				}
			}
			sender.sendMessage("Chests loaded!~");
			return true;
		} else if (command.getName().equalsIgnoreCase("chest-debug")) {
			sender.sendMessage(ChatColor.RED + "Chest Configs loaded: " + chestConfigs.toString());
			sender.sendMessage(ChatColor.LIGHT_PURPLE + "Halloween Chests active: " + halloweenChests.toString());
			sender.sendMessage(ChatColor.AQUA + "Opened Chests loaded: " + openedChests.toString());
			return true;
		}
		return false;
	}
	
	public File getChestsFolder() {
		File folder = new File (getDataFolder(), "chests");
		if (!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}
		if (!folder.exists()) {
			folder.mkdir();
		}
		return folder;
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.getPlayer() instanceof Player) {
			if ((event.getBlock().getType() == Material.CHEST || event.getBlock().getType() == Material.TRAPPED_CHEST) && halloweenChests.containsKey(event.getBlock().getLocation())) {
				if (!event.getPlayer().hasPermission("halloweenevent.all")) {
					event.setCancelled(true);
					event.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to destroy this chest.");
					return;
				}
				Block b = event.getBlock();
				halloweenChests.remove(b.getLocation());
				openedChests.remove(b.getLocation());
				event.getPlayer().sendMessage("The item chest was destroyed. Removed item spawning from location.");
				return;
			}
		}
	}
	
	public FileConfiguration getChestConfig(Location l) {
		String fileName = "x"+l.getBlockX()+"y"+l.getBlockY()+"z"+l.getBlockZ()+".yml";
		File chestFile = new File(getChestsFolder(), fileName);
		FileConfiguration chestConfig = YamlConfiguration.loadConfiguration(chestFile);
		return chestConfig;
	}
	
	public FileConfiguration getChestConfig(File f) {
		return YamlConfiguration.loadConfiguration(f);
	}
	
	public boolean checkForDoubleChest(Location l, Block b) {
		Location north = new Location(l.getWorld(), l.getX(), l.getY(), l.getZ() + 1d);
		Location south = new Location(l.getWorld(), l.getX(), l.getY(), l.getZ() - 1d);
		Location east = new Location(l.getWorld(), l.getX() + 1d, l.getY(), l.getZ());
		Location west = new Location(l.getWorld(), l.getX() - 1d, l.getY(), l.getZ());
		if (halloweenChests.containsKey(b.getLocation())) return true;
		else if (halloweenChests.containsKey(north)) return true;
		else if (halloweenChests.containsKey(south)) return true;
		else if (halloweenChests.containsKey(east)) return true;
		else if (halloweenChests.containsKey(west)) return true;
		return false;
	}
	
	public boolean checkForPlayerOpened(Block b, Player p) {
		Location l = b.getLocation();
		Location north = new Location(l.getWorld(), l.getX(), l.getY(), (l.getZ() + 1));
		Location south = new Location(l.getWorld(), l.getX(), l.getY(), (l.getZ() - 1));
		Location east = new Location(l.getWorld(), (l.getX() + 1), l.getY(), l.getZ());
		Location west = new Location(l.getWorld(), (l.getX() - 1), l.getY(), l.getZ());
		if (openedChests.containsKey(west)) {
			if (openedChests.get(west).contains(p.getName())) {
				return true;
			}
		}
		if (openedChests.containsKey(south)) { 
			if (openedChests.get(south).contains(p.getName())) {
				return true;
			}
		}
		if (openedChests.containsKey(north)) {
			if (openedChests.get(north).contains(p.getName())){
				return true;
			}
		}
		if (openedChests.containsKey(east)) {
			if (openedChests.get(east).contains(p.getName())) {
				return true;
			}
		}
		if (openedChests.containsKey(b.getLocation())) {
			if (openedChests.get(b.getLocation()).contains(p.getName())) {
				return true;
			} 
		}
		
		return false;
	}
	
	public void addPlayerToOpened(Chest c, Player p) {
		Location l = c.getLocation();
		//openedChests.get(c.getLocation()).add(p.getName());
		if (!openedChests.containsKey(l)) {
			Location north = new Location(l.getWorld(), l.getX(), l.getY(), (l.getZ() + 1));
			Location south = new Location(l.getWorld(), l.getX(), l.getY(), (l.getZ() - 1));
			Location east = new Location(l.getWorld(), (l.getX() + 1), l.getY(), l.getZ());
			Location west = new Location(l.getWorld(), (l.getX() - 1), l.getY(), l.getZ());
			
			if (openedChests.containsKey(north)) {
				openedChests.get(north).add(p.getName());
			}
			else if (openedChests.containsKey(south)) {
				openedChests.get(south).add(p.getName());		
			}
			else if (openedChests.containsKey(east)) {
				openedChests.get(east).add(p.getName());
			}
			else if (openedChests.containsKey(west)) {
				openedChests.get(west).add(p.getName());
			}
		} else if (openedChests.containsKey(l)) {
			openedChests.get(l).add(p.getName());
		}
	}
	
	public ArrayList<String> getChestItem(Block b) {
		Location l = b.getLocation();
		Location north = new Location(l.getWorld(), l.getX(), l.getY(), (l.getZ() + 1));
		Location south = new Location(l.getWorld(), l.getX(), l.getY(), (l.getZ() - 1));
		Location east = new Location(l.getWorld(), (l.getX() + 1), l.getY(), l.getZ());
		Location west = new Location(l.getWorld(), (l.getX() - 1), l.getY(), l.getZ());
		
		if (halloweenChests.containsKey(west)) {
			return halloweenChests.get(west);
		}
		if (halloweenChests.containsKey(south)) { 
			return halloweenChests.get(south);
		}
		if (halloweenChests.containsKey(north)) {
			return halloweenChests.get(north);
		}
		if (halloweenChests.containsKey(east)) {
			return halloweenChests.get(east);
		}
		if (halloweenChests.containsKey(b.getLocation())) {
			return halloweenChests.get(b.getLocation());
			
		}
		
		return null;
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
			Player p = event.getPlayer();
			Block b = event.getClickedBlock();
			if (b.getType() == Material.CHEST || b.getType() == Material.TRAPPED_CHEST) {
				Chest c = (Chest) b.getState();
				if (activeCommands.containsKey(p.getName())) {
					ArrayList<String> args = activeCommands.get(p.getName());
					if (args.get(0).equals("set")) {
						String[] item = args.get(1).split(":");
						ArrayList<String> itemList = new ArrayList<String>();
						for (int i = 0; i < item.length;i++) {
							itemList.add(item[i]);
						}
						if (itemList.size() < 2) {
							itemList.add("0");
						}
						if (args.size() == 3) {
							itemList.add(args.get(2));
						}
						p.sendMessage(itemList.toString());
						halloweenChests.put(c.getLocation(), itemList);
						openedChests.put(b.getLocation(), new ArrayList<String>()) ;
						chestConfigs.put(b.getLocation(), getChestConfig(b.getLocation()));
						if (args.size() == 3) {
							p.sendMessage("Chest saved! Item ID: "+args.get(1) + ", Item Name: \""+args.get(2)+"\"");
						} else {
							p.sendMessage("Chest saved! Item ID: "+args.get(1));
						}
						activeCommands.remove(p.getName());
					} else if (args.get(0).equals("delete")) {
						if (halloweenChests.containsKey(c.getLocation())) {
							halloweenChests.remove(c.getLocation());
							openedChests.remove(c.getLocation());
							p.sendMessage("Chest removed!");
							activeCommands.remove(p.getName());
						} else {
							p.sendMessage("Selected chest was not an item chest!");
							activeCommands.remove(p.getName());
						}
					}
					event.setCancelled(true);
					return;
				}
			} else if (activeCommands.containsKey(p.getName())) {
				activeCommands.remove(p.getName());
				p.sendMessage("Block selected was not a single chest!");
			}
		}
		
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Player p = event.getPlayer();
			Block b = event.getClickedBlock();
			if (b.getType() == Material.CHEST || b.getType() == Material.TRAPPED_CHEST) {
				if (checkForDoubleChest(b.getLocation(), b)) {
					if (checkForPlayerOpened(b, p)) {
						p.sendMessage("You've already recieved this item!");
						event.setCancelled(true);
						return;
					}
					ArrayList<String> item = getChestItem(b);
					Chest c = (Chest) b.getState();
					//Bukkit.broadcastMessage("Saved chest found at: " + chestLocation.getBlockX() + "/"+ chestLocation.getBlockY() + "/"+ chestLocation.getBlockZ());
					Inventory i = c.getBlockInventory();
					int id;
					short dv;
					id = Integer.parseInt(item.get(0));
					dv = 0;
					if (item.size() > 1) {
						dv = Short.parseShort(item.get(1));
					}
					ItemStack is = new ItemStack(id, 1, dv);
					ItemMeta im = is.getItemMeta();
					im.setLore(asList("Found by: "+ p.getName()));
					if (item.size() == 3) {
						im.setDisplayName(item.get(2));
					}
					is.setItemMeta(im);
					i.addItem(is);
					c.update();
					p.sendMessage("You found a spooky chest!");
					addPlayerToOpened(c,p);
				}
			}
		}
	}
}
