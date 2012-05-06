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

import org.bukkit.Server;
import java.lang.Runnable;

public class RunnableRestartServer implements Runnable {
	Server serv;
	public RunnableRestartServer(Server s) {
		serv=s;
	}

	public void run() {
		serv.shutdown();
	}
}

