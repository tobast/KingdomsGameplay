
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

//import org.bukkit.World;
import org.bukkit.entity.Player;

import fr.tobast.bukkit.kingdomsgameplay.KingdomsGameplay;

class RunnableRoutine implements Runnable {
	private KingdomsGameplay plugin;
	private boolean setCompass = true;

	RunnableRoutine(KingdomsGameplay plugin) {
		this.plugin = plugin;

		if(plugin.getConfig().getInt("gameplay.compassPointsEnnemyTill", -1) < 0)
			setCompass = false;
	}

	public void run() {
		if(setCompass && plugin.getEventHandler().currDayNum() >= plugin.getConfig().getInt("gameplay.compassPointsEnnemyTill", -1)) {
			Player[] players = plugin.getServer().getOnlinePlayers();

			for(Player player : players) { 
				plugin.getMapInt().setPlayerCompassToSponge(player);
			}
		}
	}
}

