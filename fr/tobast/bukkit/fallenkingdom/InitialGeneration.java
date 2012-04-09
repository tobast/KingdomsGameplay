package fr.tobast.bukkit.fallenkingdom;

import java.lang.Math;
import java.util.Random;
import java.util.Hashtable;
import java.util.Collection;

import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.block.Block;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import fr.tobast.bukkit.fallenkingdom.Team;

public class InitialGeneration
{
	// PARAMETERS - Feel free to change that

	private static final int rangeToOrigin=250; // Maximum distance of base 1 to the origin point of the map
	private static final int rangeToBase1=500; // Maximum distance of base 2 to the base 1
	private static final int baseRadius=50; // Side lenght of a base

	// END PARAMETERS

	private JavaPlugin instance;
	private Location[] bases=null;

	public InitialGeneration(JavaPlugin i_instance)
	{
		instance=i_instance;
	}

	public World getDefaultWorld()
	{
		return instance.getServer().getWorld("world"); // TODO get the real world
	}

	public Location[] getBases()
	{
		if(bases==null)
			genBases();
		return bases;
	}

	protected void genBases() // generates the bases
	{
		Random rand=new Random();
		World dftWorld=getDefaultWorld();
		bases = new Location[2];

		// Generate the 1st base
		bases[0]=new Location(dftWorld, rand.nextInt(2*rangeToOrigin)-rangeToOrigin, 256, rand.nextInt(2*rangeToOrigin)-rangeToOrigin);
		while(bases[0].getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR && bases[0].getY() > 0)
			bases[0].add(0,-1,0);

		// Generate the 2nd base
		// Apply Pythagoras' theorem
		int square=((int)Math.pow(rangeToBase1, 2));
		double x1=rand.nextInt(square);
		double x2=Math.ceil(Math.sqrt(square - Math.abs(x1)));
		x1=Math.ceil(Math.sqrt(x1));
		if(rand.nextInt(2)==0)
			x1=x1*-1;
		if(rand.nextInt(2)==0)
			x2=x2*-1;

		x1+=bases[0].getX();
		x2+=bases[0].getZ();

		bases[1]=new Location(dftWorld, x1, 256, x2);

		while(bases[1].getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR && bases[0].getY() > 0)
			bases[1].add(0,-1,0);

		drawBases();
	}

	protected void drawBases() // plants a flag on each base
	{
		for(int i=0;i<2;i++)
		{
			byte woolValue=0;
			if(i==0) // Red team
				woolValue=14;
			else // Blue team 
				woolValue=11;

			Location base=bases[i].clone();

			// Here, base represents the map floor.

			for(int j=0;j<6;j++) // Here's the flagpole
			{
				base.getBlock().setType(Material.LOG);
				base.add(0,1,0);
			}

			base.add(1,-1,0);

			for(int j=0;j<3;j++) // Here's the flag.
			{
				base.getBlock().setType(Material.WOOL);
				base.getBlock().setData(woolValue);
				base.add(1,0,0);
			}
			base.add(-1,-1,0);
			for(int j=0;j<3;j++)
			{
				base.getBlock().setType(Material.WOOL);
				base.getBlock().setData(woolValue);
				base.add(-1,0,0);
			}
		}
	}
/*
	protected void drawBases() // replaces ground with wool at the boundaries of the base
	{
		for(int i=0;i<2;i++)
		{
			Location currentPos=bases[i][0].clone();

			byte woolValue=0;
			if(i==0) // Red team
				woolValue=14;
			else // Blue team 
				woolValue=11;

			while(currentPos.getX() <= bases[i][1].getX())
				drawWoolLine(woolValue, currentPos, new Vector(1.0,0.0,0.0));
			while(currentPos.getZ() <= bases[i][1].getZ())
				drawWoolLine(woolValue, currentPos, new Vector(0.0,0.0,1.0));
			while(currentPos.getX() >= bases[i][0].getX())
				drawWoolLine(woolValue, currentPos, new Vector(-1.0,0.0,0.0));
			while(currentPos.getZ() >= bases[i][0].getZ())
				drawWoolLine(woolValue, currentPos, new Vector(0.0,0.0,-1.0));
		}
	}

	protected void drawWoolLine(byte woolValue, Location currentPos, Vector addMask)
	{
		// Fetch the floor
		if(currentPos.getBlock().getType() != Material.AIR)
		{
			while(currentPos.getBlock().getRelative(BlockFace.UP).getType() != Material.AIR)
				currentPos.add(0,1,0);
		}
		else
		{
			while(currentPos.getBlock().getType() == Material.AIR)
				currentPos.add(0,-1,0);
		}

		// Replace it with appropriate wool
		currentPos.getBlock().setType(Material.WOOL);
		currentPos.getBlock().setData(woolValue);

		currentPos.add(addMask);
	}
*/
	public Team newPlayer(Hashtable<String, Team> playerTeams)
	{
		Collection<Team> teamCollect=playerTeams.values();
		Team[] teamAry=teamCollect.toArray(new Team[0]);
		int red=0, blue=0;
		for(int i=0;i<teamAry.length;i++)
		{
			if(teamAry[i]==Team.RED)
				red++;
			else if(teamAry[i]==Team.BLUE)
				blue++;
		}
		if(red!=blue)
		{
			if(red < blue)
				return Team.RED;
			return Team.BLUE;
		}
		else
		{
			Random rand=new Random();
			if(rand.nextInt(2) == 0)
				return Team.RED;
			return Team.BLUE;
		}
	}
}

