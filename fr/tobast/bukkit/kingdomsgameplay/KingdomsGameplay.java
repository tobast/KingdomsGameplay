package fr.tobast.bukkit.kingdomsgameplay;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
		return false;
	}
}

