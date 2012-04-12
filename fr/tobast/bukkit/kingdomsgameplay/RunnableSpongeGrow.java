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

import java.lang.Runnable;
import java.util.Random;
import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.Material;

class RunnableSpongeGrow implements Runnable
{
	Location spongeLoc;
	Location refPos;
	ArrayList<Location> fedSponges;
	RunnableSpongeGrow(Location i_spongeLoc, Location i_refPos, ArrayList<Location> sponges)
	{
		spongeLoc=i_spongeLoc;
		refPos=i_refPos;
		fedSponges=sponges;
	}

	public void run()
	{
		if(spongeLoc == null)
			return;

		fedSponges.remove(fedSponges.indexOf(spongeLoc));
		
		if(spongeLoc.getBlock() == null || spongeLoc.getBlock().getType() != Material.SPONGE)
			return; // Not a sponge anymore.
		
		ArrayList<BlockFace> availableFaces=new ArrayList<BlockFace>();
		for(int i=0;i<6;i++)
		{
			Block relativeBlock=spongeLoc.getBlock().getRelative(BlockFace.values()[i]);
			if(relativeBlock.getType() == Material.AIR && locInRef(relativeBlock.getLocation()))
				availableFaces.add(BlockFace.values()[i]);
		}
		Random rand=new Random();
		
		if(availableFaces.size()==0)
			return;

		Location randPos=spongeLoc.getBlock().getRelative(availableFaces.get(rand.nextInt(availableFaces.size()))).getLocation();

		randPos.getBlock().setType(Material.SPONGE);
	}

	protected boolean locInRef(Location loc)
	{
		if(loc.getX() >= refPos.getX() && loc.getY() >= refPos.getY() && loc.getZ() >= refPos.getZ() &&
			loc.getX() <= refPos.getX()+3 && loc.getY() <= refPos.getY()+3 && loc.getZ() <= refPos.getZ()+3)
		{
			return true;
		}
		return false;
	}
}

