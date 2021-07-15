package com.williamsimoni.afkfinder;

/*
* Handle the Afkzone
* */

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AfkZoneHandler {

    private AfkFinder afkFinder;
    private AfkZone afkZone;
    private Map<UUID, Location> playerLocations;

    public AfkZoneHandler(AfkFinder afkFinder, String fileName){
        this.afkFinder = afkFinder;
        //create afkZone
        this.afkZone = new AfkZone(afkFinder,fileName);
        this.playerLocations = new HashMap<>();
    }

    public AfkZone getAfkZone(){
        return this.afkZone;
    }

    //move player into afk zone
    public void AddPlayer(Player player){
        PlayerData playerData = this.afkFinder.playersData.get(player.getUniqueId());

        //set position
        this.playerLocations.put(player.getUniqueId(), player.getLocation());

        //define location where the server will tp the player
        Location tpLocation = this.afkZone.occupyZone(player.getUniqueId());

        if (tpLocation == null){
            //if the afk zone is full put the player into the waiting queue
            //TODO
        } else {
            //if there is space, teleport the player into that location
            player.teleport(tpLocation);
        }
    }

    //remove player data from all data structures
    public void playerQuit(Player player){
        this.afkZone.freeZone(player.getUniqueId());
        this.playerLocations.remove(player.getUniqueId());
    }

    //remove the player from the afk zone
    /*
    * the player we want to remove from the zone
    * the data of the player
    * the name of the server where the player was
    * */
    public boolean removePlayer(Player player, PlayerData playerData, String serverName){
        if (!this.afkZone.freeZone(player.getUniqueId())){

            this.afkFinder.loggerHandler.error_message("player was considered in Afk Zone, but there is not any player with id " + player.getUniqueId() + " inside this afk zone");
            return false;

        } else {
            if (serverName != null){
                if (this.afkFinder.serverName.equalsIgnoreCase(serverName)){
                    //player was in the same server of the afkZone
                    //so, we must simply tp the player
                    player.teleport(this.playerLocations.get(player.getUniqueId()));
                } else {
                    //player was in different server
                    //move the player in that server
                    if (!this.afkFinder.afkCentralConnect.sendToServer(player, serverName)){
                        this.afkFinder.loggerHandler.error_message("Unable to send the player into another server");
                        //moving in the location inside the server
                        player.teleport(this.playerLocations.get(player.getUniqueId()));
                    }
                }

            } else {
                //this.afkFinder.loggerHandler.error_message("player was in Afk zone, but it is not Afk");
                //moving in the location inside the server
                player.teleport(this.playerLocations.get(player.getUniqueId()));
            }

        }

        return true;
    }

    public boolean saveZones(String filename){
        return this.afkZone.save(filename);
    }

}
