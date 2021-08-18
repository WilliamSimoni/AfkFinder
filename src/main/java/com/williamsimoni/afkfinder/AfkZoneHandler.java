package com.williamsimoni.afkfinder;

/*
* Handle the Afkzone
* */

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class AfkZoneHandler {

    private AfkFinder afkFinder;
    private AfkZone afkZone;

    public AfkZoneHandler(AfkFinder afkFinder, String fileName){
        this.afkFinder = afkFinder;
        //create afkZone
        this.afkZone = new AfkZone(afkFinder,fileName);
    }

    public AfkZone getAfkZone(){
        return this.afkZone;
    }

    //move player into afk zone
    public boolean AddPlayer(Player player){
        //define location where the server will tp the player
        Location tpLocation = this.afkZone.occupyZone(player.getUniqueId());

        if (tpLocation == null){
            //if the afk zone is full do nothing (for now)
        } else {
            //if there is space, teleport the player into that location
            player.teleport(tpLocation);
        }

        return true;
    }

    //remove player data from all data structures
    public void playerQuit(Player player){
        this.afkZone.freeZone(player.getUniqueId());
    }

    //remove the player from the afk zone
    /*
    * the player we want to remove from the zone
    * the data of the player
    * the name of the server where the player was
    * */
    public boolean removePlayer(Player player, String serverName){
        if (!this.afkZone.freeZone(player.getUniqueId())){

            this.afkFinder.loggerHandler.error_message("Player was considered in Afk Zone, but there is not any player with id " + player.getUniqueId() + " inside this afk zone");
            return false;

        } else {
            //teleport the player in its original position
            player.teleport(this.afkFinder.playersData.get(player.getUniqueId()).getLocation());
            if (serverName != null){
                if (!this.afkFinder.serverName.equalsIgnoreCase(serverName)){
                    //player was in different server
                    //move the player in that server
                    if (!this.afkFinder.afkCentralConnect.sendToServer(player, serverName)){
                        this.afkFinder.loggerHandler.error_message("Unable to send the player into another server");
                    }
                }
            }
        }

        return true;
    }

    public boolean saveZones(String filename){
        return this.afkZone.save(filename);
    }

}
