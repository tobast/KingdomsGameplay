package fr.tobast.bukkit.kingdomsgameplay;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.tobast.bukkit.kingdomsgameplay.MapInterpreter;
import fr.tobast.bukkit.kingdomsgameplay.Team;

public class KingHandler
{
	private String kings[]=new String[2];
	private MapInterpreter mapInt=null;

	public KingHandler(MapInterpreter mapInt)
	{
		this.mapInt=mapInt;
		kings=mapInt.getKings();
	}

	public String getTeamKing(Team team)
	{
		if(team==Team.RED)
			return kings[0];
		else if(team==Team.BLUE)
			return kings[1];
		return null;
	}

	public Team isKing(String player)
	{
		if(kings[0] != null && kings[0].equals(player))
			return Team.RED;
		else if(kings[1] != null && kings[1].equals(player))
			return Team.BLUE;
		return null;
	}

	public void setKing(Player newKing, Team team)
	{
		dismissTeamKing(team);
		crownKing(newKing.getName(), team);

		mapInt.setKing(team, newKing.getName());
	}
	
	public void dismissTeamKing(Team team)
	{
		dismissKing(getTeamKing(team), team);
	}

	private void dismissKing(String king, Team team)
	{
		if(king==null)
			return;

		Player kingPl=mapInt.getInstance().getServer().getPlayer(king);
		if(kingPl==null)
			return;

		if(team==Team.RED)
			kings[0]=null;
		else if(team==Team.BLUE)
			kings[1]=null;
		else
			return;

		kingPl.setPlayerListName(king);
		kingPl.setDisplayName(king);

		Player[] players=mapInt.getInstance().getServer().getOnlinePlayers();
		for(int i=0; i<players.length; i++)
			if(mapInt.getPlayerTeam(players[i].getName()) == team)
				players[i].sendMessage("Your team is now kingless! One may forge a crown and wear it.");
	}

	private void crownKing(String king, Team team)
	{
		if(king==null)
			return;

		Player kingPl=mapInt.getInstance().getServer().getPlayer(king);
		if(kingPl==null)
			return;

		if(mapInt.getPlayerTeam(king) != team)
			return;

		if(team==Team.RED)
			kings[0]=king;
		else if(team==Team.BLUE)
			kings[1]=king;
		else
			return;

		kingPl.setPlayerListName("[KING] "+king);
		kingPl.setDisplayName("[KING] "+king);

		Player[] players=mapInt.getInstance().getServer().getOnlinePlayers();
		for(int i=0; i<players.length; i++)
			if(mapInt.getPlayerTeam(players[i].getName()) == team)
				players[i].sendMessage("All hail to "+king+", your new king!");
	}

	public void setKingDisplays(Team team)
	{
		String kingName=mapInt.getKing(team);

		Player kingPl=mapInt.getInstance().getServer().getPlayer(kingName);
		if(kingPl==null)
			return;

		kingPl.setPlayerListName("[KING] "+kingName);
		kingPl.setDisplayName("[KING] "+kingName);
	}

}

