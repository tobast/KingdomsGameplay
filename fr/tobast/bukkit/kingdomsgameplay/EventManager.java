package fr.tobast.bukkit.kingdomsgameplay;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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
import org.bukkit.util.Vector;

import java.util.logging.Logger;

import fr.tobast.bukkit.kingdomsgameplay.ConfRead;
import fr.tobast.bukkit.kingdomsgameplay.ConfRead.ZoneType;
import fr.tobast.bukkit.kingdomsgameplay.Team;
import fr.tobast.bukkit.kingdomsgameplay.InitialGeneration; // Static var

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

	@EventHandler(priority=EventPriority.NORMAL)
		public void onPlayerInteractEvent(PlayerInteractEvent e)
		{
			if(e.getClickedBlock().getType() == Material.STONE_BUTTON)
			{
				Location baseLoc=null, wool_a=null, wool_b=null;
				if(isFlag(e.getClickedBlock().getLocation().clone(), baseLoc, wool_a, wool_b)) // A flag was activated
				{
					// Zone check (each corner)
					Location loc=e.getClickedBlock().getLocation().clone();
					int zoneWidth=2*InitialGeneration.baseRadius + 1; // 2 radius + center (1)
					String playerName=e.getPlayer().getName();
					ZoneType cornerType;
					boolean ennemyZone=false;

					loc.add(InitialGeneration.baseRadius, 0, InitialGeneration.baseRadius);
					cornerType=config.getPlayerZone(playerName, loc);
					if(cornerType==ZoneType.ENNEMY_NOMANSLAND || cornerType==ZoneType.ENNEMY)
						ennemyZone=true;

					loc.add(0,0,zoneWidth*-1);
					cornerType=config.getPlayerZone(playerName, loc);
					if(cornerType==ZoneType.ENNEMY_NOMANSLAND || cornerType==ZoneType.ENNEMY)
						ennemyZone=true;

					loc.add(zoneWidth*-1, 0,0);
					cornerType=config.getPlayerZone(playerName, loc);
					if(cornerType==ZoneType.ENNEMY_NOMANSLAND || cornerType==ZoneType.ENNEMY)
						ennemyZone=true;

					loc.add(0,0,zoneWidth);
					cornerType=config.getPlayerZone(playerName, loc);
					if(cornerType==ZoneType.ENNEMY_NOMANSLAND || cornerType==ZoneType.ENNEMY)
						ennemyZone=true;

					// Check corner now.
					if(ennemyZone)
					{
						e.getPlayer().sendMessage("Your new base has a part into ennemy's base or no man's land. It cannot be built.");
						return;
					}

					Team plTeam=config.getPlayerTeam(e.getPlayer().getName());
					colourFlag(plTeam, wool_a, wool_b);
					config.newBase(plTeam, baseLoc);
				}
			}
		}

	protected boolean isFlag(Location blockPtr, Location baseLoc, Location wool_a, Location wool_b)
	{
		// getting block against
		byte data=blockPtr.getBlock().getData();
		if(data >= 8) // Pushed flag
			data -= 0x08;

		switch(data)
		{
			case 1: blockPtr.add(1,0,0); break;		// East
			case 2: blockPtr.add(-1,0,0); break;	// West
			case 3: blockPtr.add(0,0,-1); break;	// South
			case 4: blockPtr.add(0,0,1); break;		// North
		}

		blockPtr.add(0,-1,0);

		baseLoc=blockPtr.clone();
		for(int i=0;i<6;i++)
		{
			if(blockPtr.getBlock().getType() != Material.LOG)
				return false;
			blockPtr.add(0,1,0);
		}
		for(int i=0;i<4;i++)
		{
			Vector v;
			switch(data)
			{
				case 1: v=new Vector(1,0,0);	break;	// East
				case 2: v=new Vector(-1,0,0);	break;	// West
				case 3: v=new Vector(0,0,-1);	break;	// South
				case 4: v=new Vector(0,0,1);	break;	// North
				default:v=new Vector(0,0,0);	break;	// Dafuq.
			}
			blockPtr.add(v);
			blockPtr.add(0,-1,0);
			wool_a=blockPtr.clone();

			boolean cont=false;
			for(int j=0;j<3;j++)
			{
				if(blockPtr.getBlock().getType() != Material.WOOL)
				{
					cont=true;
					break;
				}
				blockPtr.add(v);
			}
			if(cont)
				continue;

			Vector v2=v.clone();
			v2.setX(v.getX()*-1);
			v2.setZ(v.getZ()*-1);

			blockPtr.add(0,-1,0);
			blockPtr.add(v2);
			for(int j=0;j<3;j++)
			{
				if(blockPtr.getBlock().getType() != Material.WOOL)
				{
					cont=true;
					break;
				}
				blockPtr.add(v2);
			}

			if(cont)
				continue;

			blockPtr.add(v); // Go backward to the last wool
			wool_b=blockPtr.clone();

			return true; // If we've reached here, it's a flag.
		}

		return false;
	}

	protected void colourFlag(Team team, Location wool_a, Location wool_b)
	{
		byte woolValue=0;
		if(team==Team.RED) // Red team
			woolValue=14;
		else // Blue team 
			woolValue=11;

		Vector v;
		if(wool_a.getX() < wool_b.getX()) // X increases
			v=new Vector(1,0,0);
		else if(wool_a.getX() > wool_b.getX()) // X decreases
			v=new Vector(-1,0,0);
		else if(wool_a.getZ() < wool_b.getZ()) // Z increases
			v=new Vector(0,0,1);
		else if(wool_a.getZ() > wool_b.getZ()) // Z decreases
			v=new Vector(0,0,-1);
		else
			v=new Vector(0,0,0); // Dafuq.

		for(int i=0;i<2;i++)
		{
			for(int j=0;j<3;j++)
			{
				wool_a.getBlock().setData(woolValue);
				wool_a.add(v);
			}
			wool_a.add(0,-1,0);
			v.setX(v.getX()*-1);
			v.setZ(v.getZ()*-1);
		}
	}
}

