package fr.tobast.bukkit.kingdomsgameplay;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;

import fr.tobast.bukkit.kingdomsgameplay.EventManager;
import fr.tobast.bukkit.kingdomsgameplay.MapInterpreter;

public class KingdomsGameplay extends JavaPlugin
{
	protected MapInterpreter mapInt=null;
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
					if(oPlayers[i].getName() == args[0])
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
			}
		}

		return false;
	}
}

