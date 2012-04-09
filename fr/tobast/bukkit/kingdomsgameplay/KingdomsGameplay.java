package fr.tobast.bukkit.kingdomsgameplay;

import org.bukkit.plugin.java.JavaPlugin;
import fr.tobast.bukkit.kingdomsgameplay.EventManager;
import fr.tobast.bukkit.kingdomsgameplay.ConfRead;

public class KingdomsGameplay extends JavaPlugin
{
	public void onEnable()
	{
		ConfRead config=new ConfRead(this);
		getServer().getPluginManager().registerEvents(new EventManager(config), this);
	}

	public void onDisable()
	{
	}
}

