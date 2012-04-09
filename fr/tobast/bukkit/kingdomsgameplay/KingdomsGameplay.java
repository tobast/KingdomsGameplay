package fr.tobast.bukkit.kingdomsgameplay;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.tobast.bukkit.kingdomsgameplay.EventManager;
import fr.tobast.bukkit.kingdomsgameplay.ConfRead;

public class KingdomsGameplay extends JavaPlugin
{
	protected ConfRead config=null;
	public void onEnable()
	{
		config=new ConfRead(this);
		getServer().getPluginManager().registerEvents(new EventManager(config), this);
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
				player.sendMessage("You're in "+config.getZoneLabel(config.getPlayerZone(player.getName(),player.getLocation()))+" zone.");
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

