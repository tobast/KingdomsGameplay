package fr.tobast.bukkit.fallenkingdom;

import org.bukkit.plugin.java.JavaPlugin;
import fr.tobast.bukkit.fallenkingdom.EventManager;
import fr.tobast.bukkit.fallenkingdom.ConfRead;

public class FallenKingdom extends JavaPlugin
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

