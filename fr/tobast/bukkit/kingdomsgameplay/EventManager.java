package fr.tobast.bukkit.kingdomsgameplay;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.entity.Player;
import org.bukkit.block.Block;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Location;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.logging.Logger;

import fr.tobast.bukkit.kingdomsgameplay.ConfRead;
import fr.tobast.bukkit.kingdomsgameplay.ConfRead.ZoneType;
import fr.tobast.bukkit.kingdomsgameplay.Team;

public class EventManager implements Listener
{
	ConfRead config;
	Logger log=Logger.getLogger("Minecraft"); // TODO REMOVE

	protected static final int neutralZoneSlowness=3;
	protected static final int ennemyZoneSlowness=10;

	public EventManager(ConfRead i_config)
	{
		config=i_config;
	}
	
	@EventHandler(priority=EventPriority.HIGHEST) // Must have the final word on the spawn point
	public void onPlayerJoinEvent(PlayerJoinEvent e)
	{
		Player player=e.getPlayer();
		String playerName=player.getName();
		Team playerTeam=config.getPlayerTeam(playerName);

		if(playerTeam==null) // The player isn't assigned to any team
			playerTeam=config.newPlayer(player.getName());

		if(playerTeam==Team.DAFUQ)
			player.sendMessage("There is a problem with your team definition. Please contact a server administrator.");
		else
			player.sendMessage("Welcome, " + player.getName() + ". You are in the " + config.teamToString(playerTeam) + " team.");
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerRespawnEvent(PlayerRespawnEvent e)
	{
		if(e.isBedSpawn())
			return; 
		
		Location spawn=config.getPlayerSpawn(e.getPlayer().getName());
		if(spawn!=null)
		{
			spawn.getChunk().load(); // Quite a great thing to not fall immediately
			e.setRespawnLocation(spawn);
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST) // Final word
	public void onBlockPlaceEvent(BlockPlaceEvent e)
	{
//		long time=e.getBlock().getWorld().getFullTime(), day=time/24000;
		ZoneType plZone = config.getPlayerZone(e.getPlayer().getName(), e.getPlayer().getLocation());

		switch(plZone)
		{
			case NEUTRAL:
			case ALLY_NOMANSLAND:
				ItemStack currSt=e.getPlayer().getItemInHand();
				if(currSt.getAmount() >= 2)
					currSt.setAmount(currSt.getAmount()-2);
				else
				{
					e.setCancelled(true);
					e.getPlayer().sendMessage("You're in Neutral zone, placing 1 block costs 3 blocks!");
				}
				break;
			
			case ENNEMY_NOMANSLAND:
				Material blockType=e.getBlock().getType();
				if(blockType!=Material.TNT)
				{
					e.setCancelled(true);
					e.getPlayer().sendMessage("You cannot build on the ennemy no man's land, except TNT!");
				}
				break;

			case ENNEMY:
				e.setCancelled(true);
				e.getPlayer().sendMessage("You cannot build on the ennemy's base!");
				break;
			
			default:
				break;
		}
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onBlockDamageEvent(BlockDamageEvent e)
	{
		ZoneType plZone = config.getPlayerZone(e.getPlayer().getName(), e.getPlayer().getLocation());
		Player player=e.getPlayer();
	
		switch(plZone)
		{
			case ALLY:
				if(player.hasPotionEffect(PotionEffectType.getByName("SLOW_DIGGING")))
					player.removePotionEffect(PotionEffectType.getByName("SLOW_DIGGING"));
				break;

			case NEUTRAL:
			case ALLY_NOMANSLAND:
				player.addPotionEffect(PotionEffectType.getByName("SLOW_DIGGING").createEffect(20, neutralZoneSlowness));
				break;

			case ENNEMY_NOMANSLAND:
				player.addPotionEffect(PotionEffectType.getByName("SLOW_DIGGING").createEffect(200, ennemyZoneSlowness));
				break;

			case ENNEMY:
				Material blockType=e.getBlock().getType();
				if(blockType==Material.COBBLESTONE || blockType==Material.OBSIDIAN ||
					blockType==Material.DIAMOND_BLOCK || blockType==Material.GOLD_BLOCK || blockType==Material.IRON_BLOCK)
				{
					e.setCancelled(true);
					player.sendMessage("This block is not damageable in the ennemy's base (at least, by an human being).");
				}
				else
					player.addPotionEffect(PotionEffectType.getByName("SLOW_DIGGING").createEffect(200, ennemyZoneSlowness));
				break;


			default:
				break;
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onBlockBreakEvent(BlockBreakEvent e)
	{
		if(config.getPlayerZone(e.getPlayer().getName(), e.getPlayer().getLocation()) == ZoneType.ENNEMY)
		{
				Material blockType=e.getBlock().getType();
				if(blockType==Material.COBBLESTONE || blockType==Material.OBSIDIAN ||
					blockType==Material.DIAMOND_BLOCK || blockType==Material.GOLD_BLOCK || blockType==Material.IRON_BLOCK)
					e.setCancelled(true);
		}			
	}
}

