package fr.tobast.bukkit.kingdomsgameplay;

import org.bukkit.entity.Player;

import fr.tobast.bukkit.kingdomsgameplay.MapInterpreter;
import fr.tobast.bukkit.kingdomsgameplay.Team;

public class KingHandler
{
	String kings[]=new String[2];
	MapInterpreter map=null;

	public KingHandler(MapInterpreter map)
	{
		this.map=map;
		kings=map.getKings();
	}

	public String getTeamKing(Team team)
	{
		if(team==Team.RED)
			return kings[0];
		else if(team==Team.BLUE)
			return kings[1];
		return null;
	}

	public void setKing(Player newKing, Team team)
	{
		dismissTeamKing(team);
		crownKing(newKing.getPlayerName(), team);

		map.setKing(team, newKing.getPlayerName());
	}
	
	private void dismissTeamKing(Team team)
	{
		dismissKing(getTeamKing(team), team);
	}

	private void dismissKing(String king, Team team)
	{
		if(king==null)
			return;

		if(team==Team.RED)
			kings[0]=null;
		else if(team==Team.BLUE)
			kings[1]=null;
		else
			return;

		// TODO: change player displayname.
	}

	private void crownKing(String king, Team team)
	{
		if(king==null)
			return;

		if(map.getPlayerTeam(king) != team)
			return;

		if(team==Team.RED)
			kings[0]=king;
		else if(team==Team.BLUE)
			kings[1]=king;
		else
			return;

		// TODO: change player displayname to [KING]playername
	}
}

