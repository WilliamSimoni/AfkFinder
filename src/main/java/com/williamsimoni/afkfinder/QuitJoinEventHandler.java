package com.williamsimoni.afkfinder;

/*
* Handle whenever a player go online or go offline
* */

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;


public final class QuitJoinEventHandler implements Listener {

    private AfkFinder afkFinder;
    public QuitJoinEventHandler(AfkFinder afkFinder){
        this.afkFinder = afkFinder;
    }

    //TODO
    //Control the player sub

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();

        //get from database the sub level of the player
        int subLevel = this.afkFinder.afkDatabase.getSub(player);

        if (this.afkFinder.afkTimePerSub.get(subLevel) > 0) {
            //put in data structure only if we need to control the player
            //read whether the player is Afk or not
            PlayerData playerData = new PlayerData(player.getLocation(), this.afkFinder.afkTimePerSub.get(subLevel));
            String server = this.afkFinder.afkDatabase.getServer(player);
            if (server != null){
                if (this.afkFinder.afkZoneActive){
                    //set afk status of the player to true
                    playerData.setAfkStatus(true);
                    //put the player in list of AFK players
                    this.afkFinder.afkPlayers.add(player.getUniqueId());
                    //teleport the player in the afk zone
                    this.afkFinder.afkZoneHandler.AddPlayer(player);
                } else {
                    //remove the player from the AFK database if we are not in a server with AFK zone
                    this.afkFinder.afkDatabase.removeAfkPlayerData(player);
                }
            }
            //put player in data structure containing info about all players
            this.afkFinder.playersData.put(player.getUniqueId(), playerData);

        } else {
            if (this.afkFinder.afkDatabase.isAfk(player)){
                //this should not happen (BUT non si sa mai)
                this.afkFinder.loggerHandler.warning_message("Player with sub level " + subLevel + " was into Afk database");
                this.afkFinder.afkDatabase.removeAfkPlayerData(player);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        Player player = e.getPlayer();
        PlayerData playerData = this.afkFinder.playersData.get(player.getUniqueId());

        if (playerData == null)
            return;

        if (this.afkFinder.afkZoneActive || !this.afkFinder.afkZoneTp){
            if (playerData.isAfk()) {
                this.afkFinder.afkDatabase.removeAfkPlayerData(player);
                if (this.afkFinder.afkZoneActive)
                    this.afkFinder.afkZoneHandler.playerQuit(player);
            }
        }

        //Note: if afkZoneTp=true, then the quit event could happen when the player is moved in the AFK zone in
        // another server. So, we can't delete her/him from the database.

        //delete the player from the data structures
        this.afkFinder.playersData.remove(player.getUniqueId());

        if (playerData.isAfk()) {
            this.afkFinder.afkPlayers.remove(player.getUniqueId());
        }

    }

}
