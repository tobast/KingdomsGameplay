package fr.tobast.bukkit.kingdomsgameplay;

import java.lang.Double;
import java.io.*;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Material;

import fr.tobast.bukkit.kingdomsgameplay.Team;
import fr.tobast.bukkit.kingdomsgameplay.InitialGeneration;

public class MapInterpreter
{
	private static final String confpath="plugins/KingdomsGameplay/data.cfg";
	
	private ArrayList<Location> bases_r = new ArrayList<Location>();
	private ArrayList<Location> bases_b = new ArrayList<Location>();
	public final ArrayList<Location> getBases(Team team) { if(team==Team.RED) return bases_r; else if(team==Team.BLUE) return bases_b; else return null; }
	
	private Hashtable<String, Team> playerTeams=new Hashtable<String,Team>(); // Player -> Team
	public final Hashtable<String, Team> getPlayerTeams() { return playerTeams; }
	private Hashtable<Location, Team> chestsTeams=new Hashtable<Location,Team>(); // Location -> Team
	public final Hashtable<Location, Team> getChestTeams() { return chestsTeams; }

	private Location[] sponges = new Location[2];
	public final Location[] getSponges() { return sponges; }

	private JavaPlugin instance;

	InitialGeneration generator=null;

		Logger log=Logger.getLogger("Minecraft"); // TODO DELETE

	enum ZoneType
	{
		ALLY, ALLY_NOMANSLAND, NEUTRAL, ENNEMY_NOMANSLAND, ENNEMY
	}

	public MapInterpreter(JavaPlugin i_instance)
	{
		this.instance=i_instance;

		generator = new InitialGeneration(instance);

		try {
			File file = new File(confpath);

			if(!file.exists()) // File doesn't exists, let's create a new one and generate all needed things.
			{
				file.createNewFile();
				Writer writer=new BufferedWriter(new FileWriter(file));

				// GENERATION
				try {
					// -- Bases --
					Location[] bases=generator.getBases();
					bases_r.add(bases[0]);
					bases_b.add(bases[1]);

					writer.write("FL;"+bases[0].getX()+";"+bases[0].getY()+";"+bases[0].getZ()+";R\n");
					writer.write("FL;"+bases[1].getX()+";"+bases[1].getY()+";"+bases[1].getZ()+";B\n");

					/// -- Sponges --
					sponges=generator.getSponges();
					writer.write("SP;"+sponges[0].getX()+";"+sponges[0].getY()+";"+sponges[0].getZ()+";R\n");
					writer.write("SP;"+sponges[1].getX()+";"+sponges[1].getY()+";"+sponges[1].getZ()+";B\n");
				}
				finally {
					writer.close();
				}
			}

			else
			{
				BufferedReader reader=new BufferedReader(new FileReader(file));

				try {
					String line=null;
					World defaultWorld=instance.getServer().getWorld("world");
					while ((line=reader.readLine()) != null)
					{	
						if(line.startsWith("FL;")) // Flag. Line type : « FL;x;y;z;(R|B) »
						{
							String[] split=line.split(";");
							Location loc=new Location(defaultWorld, Double.valueOf(split[1]), Double.valueOf(split[2]), Double.valueOf(split[3]));
							if(split[4].startsWith("R"))
								bases_r.add(loc);
							else if(split[4].startsWith("B"))
								bases_b.add(loc);
						}
						else if(line.startsWith("SP;")) // Sponge. Line type : « SP;x;y;z;(R|B) »
						{
							String[] split=line.split(";");
							Location loc=new Location(defaultWorld, Double.valueOf(split[1]), Double.valueOf(split[2]), Double.valueOf(split[3]));
							if(split[4].startsWith("R"))
								sponges[0]=loc;
							else if(split[4].startsWith("B"))
								sponges[1]=loc;
								
						}
						else if(line.startsWith("PL;")) // Player. Line type : « PL;player_name;(R|B) »
						{
							String[] split=line.split(";");
							playerTeams.put(split[1], teamFromId(split[2]));
						}

						else if(line.startsWith("CH;")) // Chest. Line type : « CH;x;y;z;(R|B) »
						{
							String[] split=line.split(";");
							Location loc=new Location(defaultWorld, Double.valueOf(split[1]), Double.valueOf(split[2]), Double.valueOf(split[3]));
							
							chestsTeams.put(loc, teamFromId(split[4]));
						}
					}
				}
				finally {
					reader.close();
				}
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	protected Team teamFromId(String id)
	{
		if(id.startsWith("R"))
			return Team.RED;
		else if(id.startsWith("B"))
			return Team.BLUE;
		return Team.DAFUQ;
	}
	
	protected String teamToId(Team team)
	{
		if(team==Team.RED)
			return "R";
		else if(team==Team.BLUE)
			return "B";
		return "";
	}
	
	public String teamToString(Team team)
	{
		if(team==Team.RED)
			return "red";
		else if(team==Team.BLUE)
			return "blue";
		return "";
	}

	public Team newPlayer(String playerName)
	{
		Team plTeam=generator.newPlayer(playerTeams);
		
		playerTeams.put(playerName, plTeam);

		try {
			Writer writer=new BufferedWriter(new FileWriter(new File(confpath), true));
			try {
				String line="PL;"+playerName+";"+teamToId(plTeam);
				writer.write(line+"\n");
			}
			finally {
				writer.close();
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		
		return plTeam;
	}

	public void newBase(Team team, Location loc)
	{
		if(team==Team.RED)
			bases_r.add(loc);
		else
			bases_b.add(loc);

		try {
			Writer writer=new BufferedWriter(new FileWriter(new File(confpath), true));
			try {
				String line="FL;"+String.valueOf(loc.getX())+";"+String.valueOf(loc.getY())+";"+String.valueOf(loc.getZ())+";"+teamToId(team);
				writer.write(line+"\n");
			}
			finally {
				writer.close();
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public Team getPlayerTeam(String playerName)
	{
		return playerTeams.get(playerName);
	}

	public Location getPlayerSpawn(String playerName)
	{
		Location loc;
		if(getPlayerTeam(playerName) == Team.RED)
			loc=bases_r.get(0).clone();
		else if(getPlayerTeam(playerName) == Team.BLUE)
			loc=bases_b.get(0).clone();
		else
			return null;

		
		loc.add(0,0,2);
		return loc;
	}

	public ZoneType getPlayerZone(String playerName, Location plLoc)
	{
		Team plTeam=getPlayerTeam(playerName);
		ZoneType toRet=ZoneType.NEUTRAL;
		
		if(plTeam == Team.BLUE) // First process the ennemy bases, for no man's lands
		{
			for(int i=0;i<bases_r.size();i++)
			{
				double bx=bases_r.get(i).getX();
				if(plLoc.getX() <= bx + generator.baseRadius*2 && plLoc.getX() >= bx - generator.baseRadius * 2)
				{
					double bz=bases_r.get(i).getZ();
					if(plLoc.getZ() <= bz + generator.baseRadius*2 && plLoc.getZ() >= bz - generator.baseRadius*2) // Ennemy no man's land
					{
						if(plLoc.getX() <= bx + generator.baseRadius && plLoc.getX() >= bx - generator.baseRadius &&
							plLoc.getZ() <= bz+ generator.baseRadius && plLoc.getZ() >= bz - generator.baseRadius) // Ennemy base
							return ZoneType.ENNEMY;
						else
							toRet=ZoneType.ENNEMY_NOMANSLAND; // Maybe there's an ennemy base too!
					}
				}
			}
			if(toRet!=ZoneType.NEUTRAL)
				return toRet; // No base found, so.
		}

		for(int i=0;i<bases_b.size();i++)
		{
			double bx=bases_b.get(i).getX();
			if(plLoc.getX() <= bx + generator.baseRadius*2 && plLoc.getX() >= bx - generator.baseRadius * 2)
			{
				double bz=bases_b.get(i).getZ();
				if(plLoc.getZ() <= bz + generator.baseRadius*2 && plLoc.getZ() >= bz - generator.baseRadius*2) // no man's land
				{
					if(plLoc.getX() <= bx + generator.baseRadius && plLoc.getX() >= bx - generator.baseRadius &&
						plLoc.getZ() <= bz+ generator.baseRadius && plLoc.getZ() >= bz - generator.baseRadius) // base
					{
						if(plTeam==Team.RED)
							return ZoneType.ENNEMY;
						else
							return ZoneType.ALLY;
					}
					else
					{
						if(plTeam==Team.RED)
							toRet=ZoneType.ENNEMY_NOMANSLAND;
						else
							toRet=ZoneType.ALLY_NOMANSLAND; // Maybe there's a base too!
					}
				}
			}
		}
		if(toRet != ZoneType.NEUTRAL)
			return toRet; // No base found, so.

		if(plTeam == Team.RED) // First process the ennemy bases, for no man's lands
		{
			for(int i=0;i<bases_r.size();i++)
			{
				double bx=bases_r.get(i).getX();
				if(plLoc.getX() <= bx + generator.baseRadius*2 && plLoc.getX() >= bx - generator.baseRadius * 2)
				{
					double bz=bases_r.get(i).getZ();
					if(plLoc.getZ() <= bz + generator.baseRadius*2 && plLoc.getZ() >= bz - generator.baseRadius*2) // Ally no man's land
					{
						if(plLoc.getX() <= bx + generator.baseRadius && plLoc.getX() >= bx - generator.baseRadius &&
							plLoc.getZ() <= bz+ generator.baseRadius && plLoc.getZ() >= bz - generator.baseRadius) // Ally base
							return ZoneType.ALLY;
						else
							toRet=ZoneType.ALLY_NOMANSLAND; // Maybe there's an ally base too!
					}
				}
			}
			if(toRet != ZoneType.NEUTRAL)
				return toRet; // No base found, so.
		}

		return ZoneType.NEUTRAL;
	}

	public String getZoneLabel(ZoneType type)
	{
		switch(type)
		{
			case ALLY:				return "Ally";
			case ALLY_NOMANSLAND:	return "Ally no man's land";
			case NEUTRAL:			return "Neutral";
			case ENNEMY_NOMANSLAND:	return "Ennemy no man's land";
			case ENNEMY: 			return "Ennemy";
			default:				return "Unknown";
		}
	}

	public boolean baseExists(Location loc)
	{
		return baseExists(loc, Team.DAFUQ);
	}

	public boolean baseExists(Location loc, Team team)
	{
		if(team==Team.RED)
			return bases_r.contains(loc);
		else if(team==Team.BLUE)
			return bases_b.contains(loc);
		else
		{
			if(bases_r.contains(loc))
				return true;
			else if(bases_b.contains(loc))
				return true;
			return false;
		}
	}

	public Team isBaseLocation(Location loc)
	{
		for(int i=0;i<bases_r.size();i++)
		{
			if(loc.getX()==bases_r.get(i).getX() && loc.getZ()==bases_r.get(i).getZ())
				return Team.RED;
		}
		for(int i=0;i<bases_b.size();i++)
		{
			if(loc.getX()==bases_b.get(i).getX() && loc.getZ()==bases_b.get(i).getZ())
				return Team.BLUE;
		}
		return null;
	}

	public Team spongeOwner(Location loc)
	{
		for(int i=0;i<2;i++)
		{
			if(loc.getX() >= sponges[i].getX() && loc.getY() >= sponges[i].getY() && loc.getZ() >= sponges[i].getZ() &&
				loc.getX() <= sponges[i].getX()+3 && loc.getY() <= sponges[i].getY()+3 && loc.getZ() <= sponges[i].getZ()+3)
			{
				if(i==0)
					return Team.RED;
				else
					return Team.BLUE;
			}
		}
		return null;
	}

	public boolean isSpongeAlive(Team team)
	{
		Location loc;
		if(team==Team.RED)
			loc=sponges[0].clone();
		else if(team==Team.BLUE)
			loc=sponges[1].clone();
		else
			return true; // Each sponge team is alive, so.

		for(int x=0;x<4;x++)
		{
			for(int y=0;y<4;y++)
			{
				for(int z=0;z<4;z++)
				{
					if(loc.getBlock().getType()==Material.SPONGE)
						return true;
					loc.add(0,0,1);
				}
				loc.add(0,1,-4);
			}
			loc.add(1,-4,0);
		}
		return false;
	}
}

