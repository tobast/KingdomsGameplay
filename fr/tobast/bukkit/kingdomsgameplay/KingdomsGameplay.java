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
import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.ChatColor;

import java.util.ArrayList;

import fr.tobast.bukkit.kingdomsgameplay.EventManager;
import fr.tobast.bukkit.kingdomsgameplay.MapInterpreter;
import fr.tobast.bukkit.kingdomsgameplay.Team;

public class KingdomsGameplay extends JavaPlugin
{
	protected MapInterpreter mapInt=null;
	protected int[] currentVote=null;
	public final int[] getCurrentVote() { return currentVote; }
	public void resetVote() { currentVote=null; }
	protected ArrayList<String> voteNames=null;

	public void onEnable()
	{
		mapInt=new MapInterpreter(this);
		getServer().getPluginManager().registerEvents(new EventManager(mapInt, this), this);
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
				long day=mapInt.getSponges()[0].getWorld().getFullTime() / 24000;
				sender.sendMessage("Current day is day "+String.valueOf(day)+".");
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
							return false;
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
							return false;
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

			return false;
		}
}

