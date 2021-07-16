package com.williamsimoni.afkfinder;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;

public class FindAfkTask extends BukkitRunnable {

    //Put database

    private final AfkFinder afkFinder;
    public FindAfkTask(AfkFinder afkFinder){
        this.afkFinder = afkFinder;
    }

    public void handleAfk(PlayerData playerData, UUID playerUuid){
        this.afkFinder.loggerHandler.info_message("Player " + playerUuid + " is Afk");

        //add player to afk database
        Player player = Bukkit.getPlayer(playerUuid);
        if (player != null) {
            if (!this.afkFinder.afkDatabase.addAdkPlayer(player, this.afkFinder.serverName)){
                //if addition to the server fail, treat the player still as non afk
                this.afkFinder.loggerHandler.error_message("Database is unreachable");
                playerData.setAfkStatus(false);
            } else {
                //add player in afk list
                this.afkFinder.afkPlayers.add(playerUuid);
            }
        }

        if (this.afkFinder.afkZoneActive){
            //handle tp of the player
        } else if (this.afkFinder.afkZoneTp) {
            //handle transfer of the player in other server
        }

    }

    @Override
    public void run() {
        //iterate over players in data structure
        for (Map.Entry<UUID, PlayerData> uuidPlayerDataEntry : this.afkFinder.playersData.entrySet()) {

            PlayerData playerData = (PlayerData) ((Map.Entry) uuidPlayerDataEntry).getValue();

            //update the position of the player (if the player is not afk)
            if (!playerData.isAfk()){
                Player player = Bukkit.getPlayer((UUID) ((Map.Entry) uuidPlayerDataEntry).getKey());
                if (player != null) {
                    if (playerData.setLocation(player.getLocation())) {
                        //the location is the same previously recorded.
                        //Hence we increase the afkMinute counter of the player
                        playerData.increaseAfkMinutes();
                        if (playerData.isAfk()) {
                            handleAfk(playerData, (UUID) ((Map.Entry) uuidPlayerDataEntry).getKey());
                        }
                    } else {
                        //location changed. So reset th afk information about the player
                        playerData.resetAfkMinutes();
                    }
                } else {
                    this.afkFinder.loggerHandler.warning_message("Player with id " + ((Map.Entry) uuidPlayerDataEntry).getKey() + "was considered online");
                    this.afkFinder.playersData.remove((UUID) ((Map.Entry) uuidPlayerDataEntry).getKey());
                }
            }
        }
    }
}
