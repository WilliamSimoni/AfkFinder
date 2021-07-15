package com.williamsimoni.afkfinder;

/*
* Handle events for specific player actions
* */

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public final class PlayerActionEvents implements Listener {

    /*
    * Whenever one of these events is fired, the timestamp of the player is updated
    * */

    private AfkFinder afkFinder;
    public PlayerActionEvents(AfkFinder afkFinder){
        this.afkFinder = afkFinder;
    }

    private void update(Player player){

        //get player data
        PlayerData playerData = this.afkFinder.playersData.get(player.getUniqueId());

        if (playerData != null){
            //remove the player from the AFK database
            if (this.afkFinder.afkDatabase.removeAfkPlayerData(player)){
                //set player status to false (if operation with database goes well)
                playerData.resetAfkStatus();
                //remove the player from the list of afk players
                this.afkFinder.afkPlayers.remove(player.getUniqueId());
                //logging
                this.afkFinder.loggerHandler.info_message("Player " + player.getUniqueId() + " is not anymore AFK");

                if (this.afkFinder.afkZoneActive){
                    //handle tp of the player
                } else if (this.afkFinder.afkZoneTp){
                    //teleport player in other server
                }
            } else {
                //unable to remove the player from the database, so treat him still as AFK
                this.afkFinder.loggerHandler.error_message("Database is unreachable");
            }
        }
    }

    @EventHandler
    public void PlayerMoveHandler(PlayerMoveEvent e){
        Player player = e.getPlayer();
        //update timestamp
        if (this.afkFinder.afkPlayers.contains(player.getUniqueId()))
            update(player);
    }
}