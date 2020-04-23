package sith.me.durablock;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin implements Listener
{
	private Random ran = new Random();
	private String modName = ChatColor.WHITE + "["+ ChatColor.GOLD +"DuraBlock"+ ChatColor.WHITE +"] "+ ChatColor.RESET;
	private Boolean isEnabled = true;
	private Material[] allowedBlocks = new Material[] {
			Material.BLACK_CONCRETE,
			Material.BLUE_CONCRETE,
			Material.BROWN_CONCRETE,
			Material.CYAN_CONCRETE,
			Material.GRAY_CONCRETE,
			Material.GREEN_CONCRETE,
			Material.MAGENTA_CONCRETE,
			Material.LIGHT_BLUE_CONCRETE,
			Material.LIGHT_GRAY_CONCRETE,
			Material.LIME_CONCRETE,
			Material.MAGENTA_CONCRETE,
			Material.ORANGE_CONCRETE,
			Material.PINK_CONCRETE,
			Material.PURPLE_CONCRETE,
			Material.WHITE_CONCRETE,
			Material.YELLOW_CONCRETE
	};
	private List<String> allowedWorlds;
	
	private int breakChance = 20;
		
	@Override
	public void onEnable() {
		
		getServer().getPluginManager().registerEvents(this, this);
		
		loadConfig();
	}
	@Override
	public void onDisable() {}
	 
	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
	    Boolean isListed = false;
	    for (String w : allowedWorlds) {
	    	if (Bukkit.getWorld(w) == p.getWorld()) {
	    		isListed = true;
	    	}
	    }
	    if (!isListed) { return; }
		if (p.getInventory().getItemInMainHand().getType() == Material.GOLDEN_PICKAXE) {
			World w = p.getWorld();
			w.playEffect(p.getLocation(), Effect.SMOKE, ran.nextInt(10)+5);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
		if (e.isCancelled() || !isEnabled) { return; }
		if (e.getPlayer() == null) { return; }
		Player p = e.getPlayer();
		if (p.getGameMode() == GameMode.CREATIVE) { return; }
	    Boolean isListed = false;
	    for (String w : allowedWorlds) {
	    	if (Bukkit.getWorld(w) == p.getWorld()) {
	    		isListed = true;
	    	}
	    }
	    if (!isListed) { return; }
	    
	    PlayerInventory inv = p.getInventory();
    	for (Material b : allowedBlocks) {
    		Block currentBlock = e.getBlock();
    		if (currentBlock.getType() == b) {
    			if (inv.getItemInMainHand().getType() == Material.GOLDEN_PICKAXE) {
	    			int chance = ran.nextInt(breakChance);
	    			if (chance != breakChance/2) {
	    				e.setCancelled(true);
	    			} else {
    					p.sendMessage(modName + "Well shit, looks like you got through...");
    					p.getWorld().playEffect(currentBlock.getLocation(), Effect.MOBSPAWNER_FLAMES, 10);
    					p.getWorld().playSound(currentBlock.getLocation(), Sound.BLOCK_STONE_BREAK, 10, 1);
	    			}
    			} else {
					e.setCancelled(true);
    				p.sendMessage(modName + "You can't break a DuraBlock without a golden pickaxe...");
    			}
    		}
	    }
    }
	
	@EventHandler
	public boolean onCommand(CommandSender s, Command cmd, String cmdtype, String[] args) 
	{
		Player p;
		if (s instanceof Player) { p = (Player) s; } else { return true; }
		if (!s.isOp()) { return true; }
		switch(cmdtype) {
			case "dbtoggle":
				isEnabled = !isEnabled;
				Bukkit.broadcastMessage(modName + "Is enabled has been set to "+isEnabled);
				updateConfig();
				break;
			case "dbadd":
				if (!allowedWorlds.contains(p.getWorld().getName())) {
					allowedWorlds.add(p.getWorld().getName());
					Bukkit.broadcastMessage(modName + "An operator has added " + ChatColor.GREEN + p.getWorld().getName() + ChatColor.RESET + " to the active world list.");
					updateConfig();
				} else {
					p.sendMessage(modName + "That world is already on the active list...");
				}
				break;
			case "dbremove":
				if (allowedWorlds.contains(p.getWorld().getName())) {
					allowedWorlds.remove(p.getWorld().getName());
					Bukkit.broadcastMessage(modName + "An operator has removed " + ChatColor.RED + p.getWorld().getName() + ChatColor.RESET + " from the active world list.");
					updateConfig();
				} else {
					p.sendMessage(modName + "That world is not on the active world list...");
				}
				break;
			case "dbchance":
				if (args.length >= 1) {
					try { 
						breakChance = Integer.parseInt(args[0]);
						Bukkit.broadcastMessage(modName + "An operator has set the break chance to " + ChatColor.YELLOW + args[0] + ChatColor.RESET);
						updateConfig();
					} catch(NumberFormatException error) {
						p.sendMessage(modName + "Failed to parse that integer");
					}
				} else {
					p.sendMessage(modName + "You need to provide a positive number, default(20)");
				}
				break;
			default:
				p.sendMessage(modName + "Something went wrong...");
				break;
		}
		return true;
	}
	
	public void loadConfig() {
		this.getConfig().options().copyDefaults(true);
		
		breakChance = this.getConfig().getInt("global-chance");
		isEnabled = this.getConfig().getBoolean("is-enabled");
		if (this.getConfig().getStringList("allowed-worlds") != null) {
			allowedWorlds = this.getConfig().getStringList("allowed-worlds");
		}
		this.saveConfig();
	}
	
	public void updateConfig() {
		this.getConfig().set("is-enabled", isEnabled);
		this.getConfig().set("global-chance", breakChance);
		this.getConfig().set("allowed-worlds", allowedWorlds);
		this.saveConfig();
		Bukkit.broadcastMessage(modName + "The config was updated.");
	}
}
