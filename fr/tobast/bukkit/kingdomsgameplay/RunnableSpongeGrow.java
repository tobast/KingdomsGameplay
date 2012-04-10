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
	RunnableSpongeGrow(Location i_spongeLoc, Location i_refPos)
	{
		spongeLoc=i_spongeLoc;
		refPos=i_refPos;
	}

	public void run()
	{
		if(spongeLoc == null || spongeLoc.getBlock() == null || spongeLoc.getBlock().getType() != Material.SPONGE)
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

