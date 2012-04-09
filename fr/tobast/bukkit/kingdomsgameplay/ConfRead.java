package fr.tobast.bukkit.kingdomsgameplay;

import java.lang.Double;
import java.io.*;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Location;
import org.bukkit.World;

import fr.tobast.bukkit.kingdomsgameplay.Team;
import fr.tobast.bukkit.kingdomsgameplay.InitialGeneration;

public class ConfRead
{
	private static final String confpath="plugins/KingdomsGameplay/data.cfg";
	
	// Corners of each base
//	private Location[][] bases = new Location[2][3];
	private ArrayList<Location> bases_r = new ArrayList<Location>();
	private ArrayList<Location> bases_b = new ArrayList<Location>();
	public ArrayList<Location> getBases(Team team) { if(team==Team.RED) return bases_r; else if(team==Team.BLUE) return bases_b; else return null; }
	
	private Hashtable<String, Team> playerTeams=new Hashtable<String,Team>(); // Player -> Team
	public Hashtable<String, Team> getPlayerTeams() { return playerTeams; }
	private Hashtable<Location, Team> chestsTeams=new Hashtable<Location,Team>(); // Location -> Team
	public Hashtable<Location, Team> getChestTeams() { return chestsTeams; }

	private JavaPlugin instance;

	InitialGeneration generator=null;

		Logger log=Logger.getLogger("Minecraft"); // TODO DELETE

	public ConfRead(JavaPlugin i_instance)
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
/*
					String baseline="ZN;" + String.valueOf(bases[0][0].getX())+":"+String.valueOf(bases[0][0].getZ()) + "/" + String.valueOf(bases[0][1].getX())+":"+String.valueOf(bases[0][1].getZ()) + ";"
						+ String.valueOf(bases[1][0].getX())+":"+String.valueOf(bases[1][0].getZ()) + "/" + String.valueOf(bases[1][1].getX())+":"+String.valueOf(bases[1][1].getZ());
*/
					writer.write("FL;"+bases[0].getX()+";"+bases[0].getY()+";"+bases[0].getZ()+";R\n");
					writer.write("FL;"+bases[1].getX()+";"+bases[1].getY()+";"+bases[1].getZ()+";B\n");

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

					/*
						// ZoNes : line type « ZN;x_red_a:z_red_a|x_red_b:z_red_b;x_blue_a:z_blue_a|x_blue_b:z_blue_b »
						// Put in a nutshell, Zone Red ; Zone Blue, where a zone contains two locations separated by a |, where each location is a x and a z coordinate separated by a :.
						if(line.startsWith("ZN;"))
						{
							String[] zones=line.split(";");
							for(int i=1;i<=2;i++)
							{
								String[] zone=zones[i].split("/");
								String[] pointA=zone[0].split(":");
								String[] pointB=zone[1].split(":");

								log.info("zones="+zones[i]);
								log.info("zone="+zone[0]+","+zone[1]);
//								log.info("A="+pointA[0]+","+pointA[1] + " ; B="+pointB[0]+","+pointB[1]);

								bases[i-1][0]=new Location(defaultWorld, Double.valueOf(pointA[0]), 0, Double.valueOf(pointA[1]));
								bases[i-1][1]=new Location(defaultWorld, Double.valueOf(pointB[0]), 0, Double.valueOf(pointB[1]));
							}
						}
					*/
						if(line.startsWith("FL;")) // Flag. Line type : « FL;x;y;z;(R|B) »
						{
							String[] split=line.split(";");
							Location loc=new Location(defaultWorld, Double.valueOf(split[1]), Double.valueOf(split[2]), Double.valueOf(split[3]));
							if(split[4].startsWith("R"))
								bases_r.add(loc);
							else if(split[4].startsWith("B"))
								bases_b.add(loc);
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
}

