package fr.tobast.bukkit.kingdomsgameplay;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;
import org.bukkit.block.Block;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Location;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;
import java.util.ArrayList;

import fr.tobast.bukkit.kingdomsgameplay.MapInterpreter;
import fr.tobast.bukkit.kingdomsgameplay.MapInterpreter.ZoneType;
import fr.tobast.bukkit.kingdomsgameplay.Team;
import fr.tobast.bukkit.kingdomsgameplay.InitialGeneration; // Static var
import fr.tobast.bukkit.kingdomsgameplay.RunnableSpongeGrow;

public class EventManager implements Listener
{
	MapInterpreter mapInt;
	Logger log=Logger.getLogger("Minecraft"); // TODO REMOVE

	protected static final int neutralZoneSlowness=3;
	protected static final int ennemyZoneSlowness=10;
	public static final int days_playerHarming=3;
	public static final int days_baseBreaking=6;
	public static final int days_chestOpening=9;
	public static final int days_spongeHarming=12;

	JavaPlugin instance;

	ArrayList<Location> fedSponges=new ArrayList<Location>();

	public EventManager(MapInterpreter i_mapInt, JavaPlugin instance)
	{
		mapInt=i_mapInt;
		this.instance=instance;
	}

	@EventHandler(priority=EventPriority.HIGHEST) // Must have the final word on the spawn point
		public void onPlayerJoinEvent(PlayerJoinEvent e)
		{
			Player player=e.getPlayer();
			String playerName=player.getName();
			Team playerTeam=mapInt.getPlayerTeam(playerName);

			if(playerTeam==null) // The player isn't assigned to any team
			{
				playerTeam=mapInt.newPlayer(player.getName());

				Location spawn=mapInt.getPlayerSpawn(playerName);
				if(spawn!=null)
				{
					spawn.getChunk().load();
					player.teleport(spawn);
				}
			}

			if(playerTeam==Team.DAFUQ)
				player.sendMessage("There is a problem with your team definition. Please contact a server administrator.");
			else
				player.sendMessage("Welcome, " + player.getName() + ". You are in the " + mapInt.teamToString(playerTeam) + " team.");
		}

	@EventHandler(priority=EventPriority.NORMAL)
		public void onPlayerRespawnEvent(PlayerRespawnEvent e)
		{
			if(e.isBedSpawn())
				return; 

			Location spawn=mapInt.getPlayerSpawn(e.getPlayer().getName());
			if(spawn!=null)
			{
				spawn.getChunk().load(); // Quite a great thing to not fall immediately
				e.setRespawnLocation(spawn);
			}
		}

	@EventHandler(priority=EventPriority.HIGHEST) // Final word
		public void onBlockPlaceEvent(BlockPlaceEvent e)
		{
			if(e.getBlock().getType() == Material.CHEST)
			{
				Team plTeam=mapInt.getPlayerTeam(e.getPlayer().getName());
				mapInt.newChest(e.getBlock().getLocation(), plTeam);
			}

			//		long time=e.getBlock().getWorld().getFullTime(), day=time/24000;
			ZoneType plZone = mapInt.getPlayerZone(e.getPlayer().getName(), e.getPlayer().getLocation());

			ItemStack currSt;
			switch(plZone)
			{
				case NEUTRAL:
				case ALLY_NOMANSLAND:
					currSt=e.getPlayer().getItemInHand();
					if(currSt.getAmount() >= 2)
						currSt.setAmount(currSt.getAmount()-2);
					else
					{
						e.setCancelled(true);
						e.getPlayer().sendMessage("You're in Neutral zone, placing 1 block costs 3 blocks!");
					}
					break;

				case ENNEMY_NOMANSLAND:
				case ENNEMY:
					Material blockType=e.getBlock().getType();
					if(blockType!=Material.TNT && blockType!=Material.LEVER)
					{
						e.setCancelled(true);
						e.getPlayer().sendMessage("You cannot build on the ennemy base and the surrounding no man's land, except TNT and levers!");
					}
					else
					{
						currSt=e.getPlayer().getItemInHand();
						if(currSt.getAmount() >= 4) // 5 to 1
							currSt.setAmount(currSt.getAmount()-4);
						else
						{
							e.setCancelled(true);
							e.getPlayer().sendMessage("You're in ennemy zone, placing 1 TNT or lever costs 5!");
						}
					}
					break;

				default:
					break;
			}
		}

	@EventHandler(priority=EventPriority.NORMAL)
		public void onBlockDamageEvent(BlockDamageEvent e)
		{
			long day=e.getBlock().getLocation().getWorld().getFullTime() / 24000;
			ZoneType plZone = mapInt.getPlayerZone(e.getPlayer().getName(), e.getPlayer().getLocation());
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
					if(day < days_baseBreaking)
					{
						e.setCancelled(true);
						e.getPlayer().sendMessage("You cannot break anything in an ennemy base or no man's land before day "+String.valueOf(days_baseBreaking)+"!");
						return;
					}
					player.addPotionEffect(PotionEffectType.getByName("SLOW_DIGGING").createEffect(200, ennemyZoneSlowness));
					break;

				case ENNEMY:
					if(day < days_baseBreaking)
					{
						e.setCancelled(true);
						e.getPlayer().sendMessage("You cannot break anything in an ennemy base or no man's land before day "+String.valueOf(days_baseBreaking)+"!");
						return;
					}

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
			long day=e.getBlock().getLocation().getWorld().getFullTime() / 24000;
			if(e.getBlock().getType() == Material.CHEST)
			{
				if(day < days_chestOpening && mapInt.getPlayerTeam(e.getPlayer().getName()) != mapInt.chestOwner(e.getBlock().getLocation()))
				{
					e.setCancelled(true);
					e.getPlayer().sendMessage("You cannot break ennemy's chests before day "+String.valueOf(days_chestOpening)+"!");
					return;
				}
				mapInt.delChest(e.getBlock().getLocation(), mapInt.chestOwner(e.getBlock().getLocation()));
			}

			if(e.getBlock().getType() == Material.SPONGE)
			{
				if(day < days_spongeHarming)
				{
					e.setCancelled(true);
					e.getPlayer().sendMessage("The sponge cannot be killed before day "+String.valueOf(days_spongeHarming)+"!");
					return;
				}

				Team spongeTeam=mapInt.spongeOwner(e.getBlock().getLocation());
				Player[] onlinePlayers=instance.getServer().getOnlinePlayers();
				for(int i=0;i<onlinePlayers.length;i++)
				{
					if(mapInt.getPlayerTeam(onlinePlayers[i].getName()) == spongeTeam)
						onlinePlayers[i].sendMessage("You can hear the sponge screaming into your head...");
				}

				e.setCancelled(true);
				Block b=e.getBlock();
				if(b!=null)
					b.setType(Material.AIR);

				if(!mapInt.isSpongeAlive(spongeTeam))
				{
					Player[] players=e.getPlayer().getServer().getOnlinePlayers();
					Team killed=mapInt.spongeOwner(e.getBlock().getLocation());
					Team winners;
					if(killed==Team.RED)
						winners=Team.BLUE;
					else if(killed==Team.BLUE)
						winners=Team.RED;
					else
						return;

					String message=ChatColor.RED+"The "+mapInt.teamToString(killed)+" sponge has been killed! Congratulations, "+mapInt.teamToString(winners)+" team, you won!"+ChatColor.RESET;
					for(int i=0;i<players.length;i++)
						players[i].sendMessage(message);
					// TODO server restart?
				}
			}

			if(mapInt.isBaseLocation(e.getBlock().getLocation()) != null && e.getBlock().getType() == Material.LOG)
			{
				e.getPlayer().sendMessage("You cannot break a flagpole.");
				e.setCancelled(true);
				return;
			}

			if(mapInt.getPlayerZone(e.getPlayer().getName(), e.getPlayer().getLocation()) == ZoneType.ENNEMY)
			{
				Material blockType=e.getBlock().getType();
				if(blockType==Material.COBBLESTONE || blockType==Material.DIAMOND_BLOCK || blockType==Material.GOLD_BLOCK || blockType==Material.IRON_BLOCK)
					e.setCancelled(true);
				return;
			}			
		}

	@EventHandler(priority=EventPriority.NORMAL)
		public void onPlayerInteractEvent(PlayerInteractEvent e)
		{
			if(!e.hasBlock() || e.getClickedBlock() == null)
				return;

			if(e.getClickedBlock().getType() == Material.CHEST)
			{
				if(e.getClickedBlock().getLocation().getWorld().getFullTime()/24000 < days_chestOpening &&
					mapInt.getPlayerTeam(e.getPlayer().getName()) != mapInt.chestOwner(e.getClickedBlock().getLocation()))
				{
					e.setCancelled(true);
					e.getPlayer().sendMessage("You cannot steal from ennemy's chests before day "+String.valueOf(days_chestOpening)+"!");
					return;
				}
			}

			// FLAG PLANTING
			if(e.getClickedBlock().getType() == Material.STONE_BUTTON)
			{
				Location[] locationArray=isFlag(e.getClickedBlock().getLocation().clone()); // [0] -> base, [1] -> wool_a, [2] -> wool_b. If not a flag, [0] -> null
				if(locationArray[0]!=null) // A flag was activated
				{
					// Existance check
					if(mapInt.baseExists(locationArray[0]))
					{
						e.getPlayer().sendMessage("This flag is already planted!");
						return;
					}

					// Zone check (each corner)
					Location loc=locationArray[0].clone();
					int zoneWidth=2*InitialGeneration.baseRadius + 1; // 2 radius + center (1)
					String playerName=e.getPlayer().getName();
					ZoneType cornerType;
					boolean ennemyZone=false;

					loc.add(InitialGeneration.baseRadius, 0, InitialGeneration.baseRadius);
					cornerType=mapInt.getPlayerZone(playerName, loc);
					if(cornerType==ZoneType.ENNEMY_NOMANSLAND || cornerType==ZoneType.ENNEMY)
						ennemyZone=true;

					loc.add(0,0,zoneWidth*-1);
					cornerType=mapInt.getPlayerZone(playerName, loc);
					if(cornerType==ZoneType.ENNEMY_NOMANSLAND || cornerType==ZoneType.ENNEMY)
						ennemyZone=true;

					loc.add(zoneWidth*-1, 0,0);
					cornerType=mapInt.getPlayerZone(playerName, loc);
					if(cornerType==ZoneType.ENNEMY_NOMANSLAND || cornerType==ZoneType.ENNEMY)
						ennemyZone=true;

					loc.add(0,0,zoneWidth);
					cornerType=mapInt.getPlayerZone(playerName, loc);
					if(cornerType==ZoneType.ENNEMY_NOMANSLAND || cornerType==ZoneType.ENNEMY)
						ennemyZone=true;

					// Check corner now.
					if(ennemyZone)
					{
						e.getPlayer().sendMessage("Your new base has a part into ennemy's base or no man's land. It cannot be built.");
						return;
					}

					Team plTeam=mapInt.getPlayerTeam(e.getPlayer().getName());
					colourFlag(plTeam, locationArray[1], locationArray[2]);
					mapInt.newBase(plTeam, locationArray[0]);

					e.getPlayer().sendMessage("Congratulations, you planted a flag! The zone is now yours.");
				}
			}

			// SPONGE FEEDING
			else if(e.getClickedBlock().getType()==Material.SPONGE && e.getPlayer().getItemInHand().getType() == Material.SUGAR)
			{
				if(fedSponges.contains(e.getClickedBlock().getLocation()))
				{
					e.getPlayer().sendMessage("The sponge burps. It seems that it had been already fed.");
					return;
				}
				Location spongeRef;
				Team plTeam=mapInt.spongeOwner(e.getClickedBlock().getLocation());
				if(plTeam == Team.RED)
					spongeRef=mapInt.getSponges()[0];
				else if(plTeam==Team.BLUE)
					spongeRef=mapInt.getSponges()[1];
				else
				{
					e.getPlayer().sendMessage("You cannot feed this sponge!");
					return;
				}

				ItemStack st=e.getPlayer().getItemInHand();
				if(st.getAmount()<1)
					return;
				st.setAmount(st.getAmount()-1);

				e.getPlayer().getServer().getScheduler().scheduleSyncDelayedTask(instance, new RunnableSpongeGrow(e.getClickedBlock().getLocation(), spongeRef, fedSponges), 3600L); // 3600 ticks = 3*60*20 = 3min
				fedSponges.add(e.getClickedBlock().getLocation());
				e.getPlayer().sendMessage("You successfully fed the sponge.");
			}
		}

	@EventHandler(priority=EventPriority.HIGH)
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e)
	{
		long day=e.getEntity().getLocation().getWorld().getFullTime()/24000; // A bit hacky, huh?
		if(e.getEntityType()==EntityType.PLAYER && day >= days_playerHarming)
		{
			if(e.getDamager().getType()==EntityType.PLAYER)
			{
				e.setCancelled(true);
				((Player)e.getDamager()).sendMessage("You cannot hurt a player before day "+String.valueOf(days_playerHarming)+"!");
			}
			else if(e.getDamager().getType()==EntityType.ARROW) // Fuck the skeletons, player protection is needed.
			{
				e.setCancelled(true);
			}
		}
	}

	protected Location[] isFlag(Location blockPtr)
	{
		Location baseLoc, wool_a, wool_b;
		Location[] nullAry = {null};

		// getting block against
		byte data=blockPtr.getBlock().getData();
		if(data >= 8) // Pushed flag
			data -= 0x08;

		switch(data)
		{
			case 1: blockPtr.add(-1,0,0);	break;	// East
			case 2: blockPtr.add(1,0,0);	break;	// West
			case 3: blockPtr.add(0,0,-1);	break;	// South
			case 4: blockPtr.add(0,0,1);	break;	// North
		}
		blockPtr.add(0,-1,0);

		baseLoc=blockPtr.clone();
		for(int i=0;i<6;i++)
		{
			if(blockPtr.getBlock().getType() != Material.LOG)
				return nullAry;
			blockPtr.add(0,1,0);
		}
		Location ptrBackup=blockPtr.clone();
		for(int i=0;i<4;i++)
		{
			blockPtr=ptrBackup.clone();
			Vector v;
			switch(i)
			{
				case 0: v=new Vector(1,0,0);	break;	// East
				case 1: v=new Vector(-1,0,0);	break;	// West
				case 2: v=new Vector(0,0,-1);	break;	// South
				case 3: v=new Vector(0,0,1);	break;	// North
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
			wool_b=blockPtr.clone();
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

			Location[] retAry = {baseLoc, wool_a,  wool_b};
			return retAry; // If we've reached down here, it's a flag.
		}

		return nullAry;
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
			wool_a.add(v);
		}
	}
}

