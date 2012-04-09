package fr.tobast.bukkit.kingdomsgameplay;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Chunk;

import java.util.logging.Logger;

import fr.tobast.bukkit.kingdomsgameplay.ConfRead;
import fr.tobast.bukkit.kingdomsgameplay.Team;

public class EventManager implements Listener
{
	ConfRead config;
	Logger log=Logger.getLogger("Minecraft");

	public EventManager(ConfRead i_config)
	{
		config=i_config;
	}
	
	@EventHandler(priority=EventPriority.HIGH) // Processed quickly, but before all.
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
		log.info("Detected login, team "+config.teamToString(playerTeam));
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

}

