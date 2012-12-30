/*
 * PROGRAM:
 *   KingdomsGameplay - bukkit plugin
 *
 * AUTHOR:
 *   Théophile BASTIAN (a.k.a. Tobast)
 *
 * CONTACT & WEBSITE:
 *   http://tobast.fr/ (contact feature included)
 *   error-report@tobast.fr (error reporting only)
 *
 * SHORT DESCRIPTION:
 *   See first license line.
 *
 * LICENSE:
 *   KingdomsGameplay is a Bukkit plugin designed to add a new gameplay to the server. Rules described on DevBukkit.
 *   Copyright (C) 2012  Théophile BASTIAN
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see http://www.gnu.org/licenses/gpl.txt.
 */

package fr.tobast.bukkit.kingdomsgameplay;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;
import org.bukkit.OfflinePlayer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.logging.Logger;

import fr.tobast.bukkit.kingdomsgameplay.EventManager;
import fr.tobast.bukkit.kingdomsgameplay.MapInterpreter;
import fr.tobast.bukkit.kingdomsgameplay.Team;
import fr.tobast.bukkit.kingdomsgameplay.RunnableRoutine;

public class KingdomsGameplay extends JavaPlugin
{
	protected MapInterpreter mapInt=null;
	public final MapInterpreter getMapInt() { return mapInt; }
	protected EventManager eventHandler=null;
	public final EventManager getEventHandler() { return eventHandler; }
	protected int[] currentVote=null;
	public final int[] getCurrentVote() { return currentVote; }
	public void resetVote() { currentVote=null; }
	protected ArrayList<String> voteNames=null;
	protected long beginTime=-1;
	public void setBeginTime(final long time) { this.beginTime=time; }

	Logger log=Logger.getLogger("minecraft");

	public void onEnable()
	{
		loadConfig();
		mapInt=new MapInterpreter(this);
		eventHandler=new EventManager(mapInt, this, beginTime);
		getServer().getPluginManager().registerEvents(eventHandler, this);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new RunnableRoutine(this), 0, 1200);
	}

	public void loadConfig()
	{
		// ===== Set default values =====
		
		// Generation
		getConfig().addDefault("gen.basesDistance", 150);
//		getConfig().addDefault("gen.distanceToOrigin", 250); // Not longer an option.
		
		// Geometry
		getConfig().addDefault("geometry.baseRadius", 25);
		
		// Blocks
		getConfig().addDefault("block.ennemyBase.allowedId", 4); // smoothstone
		getConfig().addDefault("block.ennemyBase.allowedCost", 5); // 5 for 1
		getConfig().addDefault("block.ennemyBase.sinceDay", 0);

		// Worlds
		getConfig().addDefault("worlds.main", "world");
		getConfig().addDefault("worlds.ignored", java.util.Collections.emptyList());

		// Days
		getConfig().addDefault("days.harmPlayer", 2);
		getConfig().addDefault("days.chest", 3);
		getConfig().addDefault("days.breakBase", 5);
		getConfig().addDefault("days.harmSponge", 7);

		// Gameplay
		getConfig().addDefault("gameplay.compassPointsEnnemyTill", 5);

		// King costs
		getConfig().addDefault("costs.wool", 3);
		getConfig().addDefault("costs.sheep", 12);
		getConfig().addDefault("costs.sword", 6);
		getConfig().addDefault("costs.chestplate", 18);
		getConfig().addDefault("costs.iron", 2);
		getConfig().addDefault("costs.log", 2);
		getConfig().addDefault("costs.diamond", 40);
		getConfig().addDefault("costs.enchanting", 90);
		getConfig().addDefault("costs.brewing", 90);
		getConfig().addDefault("costs.wart", 2);
		getConfig().addDefault("costs.glowstone", 2);
		getConfig().addDefault("costs.gunpowder", 5);
		getConfig().addDefault("costs.cobble", 5); // Full stack

		// ===== End default =====

		getConfig().options().copyDefaults(true);
		saveConfig();
	}

	public void onDisable()
	{
	}


	@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
		{
			if(label.equals("zone"))
			{
				if(sender instanceof Player)
				{
					Player player=(Player)sender;
					player.sendMessage("You're in "+mapInt.getZoneLabel(mapInt.getPlayerZone(player.getName(),player.getLocation()))+" zone.");
					return true;
				}
				else
				{
					sender.sendMessage("Not a player!");
					return false;
				}
			}

			else if(label.equals("canbuildflag"))
			{
				if(!(sender instanceof Player))
				{
					sender.sendMessage("You must be a player to perform that action!");
					return false;
				}
				Player player=(Player)sender;
				
				if(mapInt.canBuildFlag(player, player.getLocation()) == true)
					sender.sendMessage("You can build a flag here!");
				else
					sender.sendMessage("You cannot build a flag here, as a part of your base would be into the ennemy's base or no man's land.");
				return true;
			}

			else if(label.equals("team"))
			{
				if(args.length == 1)
				{
					String team=mapInt.teamToString(mapInt.getPlayerTeam(args[0]));
					if(team==null)
					{
						sender.sendMessage("Player does not exist.");
						return false;
					}
					sender.sendMessage(args[0]+" is in the "+team+" team.");
					return true;
				}
			}

			else if(label.equals("daynum"))
			{
				long day=eventHandler.currDayNum();
				sender.sendMessage("Current day is day "+String.valueOf(day)+".");
				return true;
			}

			else if(label.equals("dayreset"))
			{
				eventHandler.dayReset();
				return true;
			}

			else if(label.equals("teamset"))
			{
				if(args.length == 2)
				{
					Team newTeam=mapInt.teamFromId(args[1]);
					if(newTeam==Team.DAFUQ)
					{
						sender.sendMessage("Invalid team. The identifier must be uppercase!");
						return false;
					}

					OfflinePlayer[] oPlayers=getServer().getOfflinePlayers();
					boolean ok=false;
					for(int i=0;i<oPlayers.length;i++)
					{
						if(oPlayers[i].getName().equals(args[0]))
						{
							ok=true;
							break;
						}

					}
					if(!ok)
					{
						sender.sendMessage("Player \""+args[0]+"\" does not exists.");
						return false;
					}

					mapInt.changeTeam(args[0], newTeam);
					sender.sendMessage("Player \""+args[0]+"\" have been switched to team "+args[1]+".");
					getServer().getPlayer(args[0]).sendMessage("You have been switched to team "+args[1]+" by "+sender.getName()+".");
					return true;
				}
			}

			else if(label.equals("breakforce"))
			{
				if(args.length == 4)
				{
					World wd=getServer().getWorld(args[3]);
					if(wd==null)
					{
						sender.sendMessage("Unknown world.");
						return false;
					}
					Location loc=new Location(wd, Integer.valueOf(args[0]), Integer.valueOf(args[1]), Integer.valueOf(args[2]));
					loc.getBlock().setType(Material.AIR);
					return true;
				}
			}

			else if(label.equals("ts") || label.equals("teamsay"))
			{
				if(!(sender instanceof Player))
				{
					sender.sendMessage("You must be a player.");
					return false;
				}
				if(args.length >= 1)
				{
					String msg=ChatColor.GOLD+"<"+((Player)sender).getName()+"> ";

					for(int i=0;i<args.length;i++)
						msg+=args[i]+" ";
					Player[] players=getServer().getOnlinePlayers();
					Team team=mapInt.getPlayerTeam(((Player)sender).getName());
					for(int i=0;i<players.length;i++)
					{
						if(mapInt.getPlayerTeam(players[i].getName()) == team)
						{	
							players[i].sendMessage(msg);
						}
					}
					return true;
				}
			}

			else if(label.equals("kg-vote"))
			{
				if(args.length == 1)
				{
					if(args[0].equals("restart")) // Begin a restart vote
					{
						if(currentVote != null) // A vote has been started
						{
							sender.sendMessage("A vote is already started.");
							return false;
						}

						currentVote=new int[2];
						voteNames=new ArrayList<String>();
						getServer().broadcastMessage("Restart vote has started. You have 1min to vote, using /kg-vote yes and /kg-vote no command.");
						getServer().getScheduler().scheduleSyncDelayedTask(this, new RunnableRestartVote(this), 1200L); // 1200 ticks = 1min

						return true;
					}

					else if(args[0].equals("yes"))
					{
						if(currentVote==null)
						{
							sender.sendMessage("There's no vote started.");
							return true;
						}
						if(voteNames.contains(sender.getName()))
						{
							sender.sendMessage("You already voted!");
							return true;
						}

						currentVote[0]++;
						voteNames.add(sender.getName());
						sender.sendMessage("You voted yes.");
						return true;
					}

					else if(args[0].equals("no"))
					{
						if(currentVote==null)
						{
							sender.sendMessage("There's no vote started.");
							return true;
						}
						if(voteNames.contains(sender.getName()))
						{
							sender.sendMessage("You already voted!");
							return true;
						}

						currentVote[1]++;
						voteNames.add(sender.getName());
						sender.sendMessage("You voted yes.");
						return true;
					}
					sender.sendMessage("Wrong parameter.");
					return false;
				}
				sender.sendMessage("No enough parameters given.");
				return false;
			}

			else if(label.equals("kg-cancelvote"))
			{
				getServer().broadcastMessage(sender.getName()+" cancelled the current vote.");
				currentVote=null;
			}

			else if(label.equals("king-buy"))
			{
				if(sender instanceof Player)
				{
					Player plSender=(Player)sender;
					if(eventHandler.getKingHandler().isKing(plSender.getName()) != null) // The sender is a king. A real one!!1
					{
						if(args.length == 2)
						{
							int price = getItemPrice(args[0]) * Integer.valueOf(args[1]);
							if(price == -1)
							{
								sender.sendMessage("I'm sorry, my Lord, but the item you want to purchase is not available.");
								return false;
							}
							if(price==0 || Integer.valueOf(args[1]) <= 0)
							{
								sender.sendMessage("I'm sorry, my Lord, but you must buy at least one thing!");
								return false;
							}

							ItemStack[] invContents = plSender.getInventory().getContents();
							int goldSum=0;

							for(int i=0; i<invContents.length; i++)
								if(invContents[i] != null && invContents[i].getType() == Material.GOLD_NUGGET)
									goldSum += invContents[i].getAmount();

							if(goldSum < price)
							{
								sender.sendMessage("I'm sorry, my Lord, but you do not have enough money to buy that.");
								return true;
							}

							while(price > 0)
							{
								int id=plSender.getInventory().first(Material.GOLD_NUGGET);
								if(invContents[id].getAmount() > price)
								{
									invContents[id].setAmount(invContents[id].getAmount() - price);
									price=0;
								}
								else
								{
									price -= invContents[id].getAmount();
									plSender.getInventory().clear(id);
								}
							}

							if(args[0].equals("sheep"))
							{
								int number=Integer.valueOf(args[1]);
								for(int i=0; i<number; i++)
									plSender.getWorld().spawnEntity(plSender.getLocation(), EntityType.SHEEP);
							}
							else
							{
								ItemStack[] purchased = getPurchasedItemStack(args[0], Integer.valueOf(args[1]));
								for(int i=0; i<purchased.length; i++)
									plSender.getInventory().addItem(purchased[i]);
							}

							sender.sendMessage("All right, my Lord! Here is what you've purchased!");
							return true;
						}
					}
					else
					{
						sender.sendMessage("You must be the King to perform that action!");
						return false;
					}
				}
				else
				{
					sender.sendMessage("You must be the King to perform that action!");
					return false;
				}
			}

			return false;
		}

	int getItemPrice(String itemName)
	{
		return getConfig().getInt("costs."+itemName, -1);
	}

	ItemStack[] getPurchasedItemStack(String itemName, int number)
	{
		if(itemName.equals("chestplate") || itemName.equals("sword") || itemName.equals("cobble")) // Unitary stacks
		{
			ItemStack base;
			if(itemName.equals("chestplate"))		base=new ItemStack(Material.IRON_CHESTPLATE);
			else if(itemName.equals("sword"))		base=new ItemStack(Material.IRON_SWORD);
			else if(itemName.equals("cobble"))		base=new ItemStack(Material.COBBLESTONE, 64);
			else									return new ItemStack[0];
			
			ItemStack[] output=new ItemStack[number];
			for(int i=0; i<output.length; i++)
				output[i]=base;

			return output;
		}
		else
		{
			ItemStack base;
			if(itemName.equals("wool"))				base=new ItemStack(Material.WOOL, 64);
			else if(itemName.equals("iron"))		base=new ItemStack(Material.IRON_INGOT, 64);
			else if(itemName.equals("log"))			base=new ItemStack(Material.LOG, 64);
			else if(itemName.equals("diamond"))		base=new ItemStack(Material.DIAMOND, 64);
			else if(itemName.equals("wart"))		base=new ItemStack(Material.NETHER_WARTS, 64);
			else if(itemName.equals("glowstone"))	base=new ItemStack(Material.GLOWSTONE_DUST, 64);
			else if(itemName.equals("gunpowder"))	base=new ItemStack(Material.SULPHUR, 64);
			else if(itemName.equals("enchanting"))	base=new ItemStack(Material.ENCHANTMENT_TABLE);
			else if(itemName.equals("brewing"))		base=new ItemStack(Material.BREWING_STAND_ITEM);
			else									return new ItemStack[0];

			int outSize=number/64;
			if(number%64 > 0)
				outSize++;
			ItemStack[] output=new ItemStack[outSize];
			for(int i=0; i<number/64; i++)
				output[i]=base;

			if((number % 64) > 0)
			{
				ItemStack last=new ItemStack(base.getType(), number%64);
				output[outSize-1]=last;
			}

			return output;
		}
	}
}

