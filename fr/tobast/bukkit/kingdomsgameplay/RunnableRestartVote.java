package fr.tobast.bukkit.kingdomsgameplay;

import org.bukkit.plugin.java.JavaPlugin;

import fr.tobast.bukkit.kingdomsgameplay.RunnableVote;

public class RunnableRestartVote extends RunnableVote
{
	public RunnableRestartVote(JavaPlugin instance)
	{
		super(instance);
	}

	// called by run() if all is ok.
	protected void execute()
	{
		if(super.currentVote[0] > super.currentVote[1]) // Yes won
		{
			instance.getServer().broadcastMessage("Server restart vote: YES won. Server will restart in 15 seconds.");
			instance.getServer().getScheduler().scheduleSyncDelayedTask(instance, new RunnableRestartServer(instance.getServer()), 300L);
		}
		else
		{
			instance.getServer().broadcastMessage("Server restart vote: NO won. The server willn't restart.");
		}
	}
}

