package fr.tobast.bukkit.kingdomsgameplay;

import org.bukkit.plugin.java.JavaPlugin;

import fr.tobast.bukkit.kingdomsgameplay.KingdomsGameplay;

public abstract class RunnableVote implements Runnable
{
	protected JavaPlugin instance;
	protected int[] currentVote;

	public RunnableVote(JavaPlugin instance)
	{
		this.instance=instance;
	}

	public void run()
	{
		currentVote=((KingdomsGameplay)instance).getCurrentVote();
		if(currentVote==null)
			return;

		execute();
		cleanup();
	}

	protected abstract void execute();

	protected void cleanup()
	{
		((KingdomsGameplay)instance).resetVote();
	}
}

