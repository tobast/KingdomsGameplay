/*
 * PROGRAM:
 *   KingdomsGameplay - bukkit plugin
 *
 * AUTHOR:
 *   Théophile BASTIAN (a.k.a. Tobast)
 *
 * CONTACT & WEBSITE:
 *   http://tobast.fr/ (contact feature included)
 *   error-report@tobast.fr (error reporting only)
 *
 * SHORT DESCRIPTION:
 *   See first license line.
 *
 * LICENSE:
 *   KingdomsGameplay is a Bukkit plugin designed to add a new gameplay to the server. Rules described on DevBukkit.
 *   Copyright (C) 2012  Théophile BASTIAN
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see http://www.gnu.org/licenses/gpl.txt.
*/

package fr.tobast.bukkit.kingdomsgameplay;

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
import org.bukkit.block.Biome;

import fr.tobast.bukkit.kingdomsgameplay.Team;

public class InitialGeneration
{
	// PARAMETERS - Feel free to change that

//	public static int rangeToOrigin=1500; // Maximum distance of base 1 to the origin point of the map
	public static int rangeToBase1=150; // Maximum distance of base 2 to the base 1
	public static int baseRadius=25; // Radius of a base

	// END PARAMETERS

	private JavaPlugin instance;
	private Location[] bases=null;
	private Location[] sponges=null;

	public InitialGeneration(JavaPlugin i_instance)
	{
		instance=i_instance;
//		rangeToOrigin=instance.getConfig().getInt("gen.distanceToOrigin", rangeToOrigin);
		rangeToBase1=instance.getConfig().getInt("gen.basesDistance", rangeToBase1);
		baseRadius=instance.getConfig().getInt("geometry.baseRadius", baseRadius);
	}

	public World getDefaultWorld()
	{
		return instance.getServer().getWorld(instance.getConfig().getString("worlds.main"));
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

		do {
			// Generate the 1st base
			bases[0]=new Location(dftWorld, rand.nextInt()%10000, 256, rand.nextInt()%10000);

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
		} while(!basePossibleBiome(bases[0].getBlock().getBiome()) || !basePossibleBiome(bases[1].getBlock().getBiome()));

		// Lower the y value to the floor
		while(bases[0].getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR && bases[0].getY() > 0)
			bases[0].add(0,-1,0);
		while(bases[1].getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR && bases[1].getY() > 0)
			bases[1].add(0,-1,0);

		drawBases();
	}

	protected boolean basePossibleBiome(Biome biome) {
		if(biome == Biome.OCEAN || biome == Biome.RIVER || biome == Biome.FROZEN_OCEAN 	|| biome == Biome.FROZEN_RIVER)
			return false;
		return true;
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
			base.add(0,-3,1);
			base.getBlock().setType(Material.STONE_BUTTON);
			base.getBlock().setData((byte)3);
		}
	}
	
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

	public Location[] getSponges()
	{
		if(sponges==null)
			genSponges();
		return sponges;
	}

	private void genSponges()
	{
		if(bases==null)
			return;

		sponges=new Location[2];
		Vector spongeVector=new Vector(10,0,10);

		for(int i=0;i<2;i++)
		{
			sponges[i]=bases[i].clone();
			sponges[i].add(spongeVector);
			sponges[i].setY(256);
			// Fetch the floor
			while(sponges[i].getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR && bases[i].getY() > 0)
				sponges[i].add(0,-1,0);
		}
		drawSponges();
	}

	private void drawSponges()
	{
		if(sponges==null)
			return;
		for(int i=0;i<2;i++)
		{
			Location currPtr=sponges[i].clone();
			currPtr.add(1,1,1);

			currPtr.getBlock().setType(Material.SPONGE);
			currPtr.add(1,0,0);
			currPtr.getBlock().setType(Material.SPONGE);
			currPtr.add(0,1,0);
			currPtr.getBlock().setType(Material.SPONGE);
			currPtr.add(-1,0,0);
			currPtr.getBlock().setType(Material.SPONGE);
			currPtr.add(0,0,1);
			currPtr.getBlock().setType(Material.SPONGE);
			currPtr.add(1,0,0);
			currPtr.getBlock().setType(Material.SPONGE);
			currPtr.add(0,-1,0);
			currPtr.getBlock().setType(Material.SPONGE);
			currPtr.add(-1,0,0);
			currPtr.getBlock().setType(Material.SPONGE);
		}
	}
}

