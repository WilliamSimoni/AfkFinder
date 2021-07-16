package com.williamsimoni.afkfinder;

/*
* Class that represents players that are not AFK
* */

import org.bukkit.Location;

public class PlayerData {

    private Integer maxAfkMinutes;      //max number of afkMinutes. if afkMinutes > maxAfkMinutes, then the player is afk
    private boolean isAfk;              //if true, the player is labelled as AFK
    private Location position;          //last recorded position of the player
    private int afkMinutes;             //number of times the position was the same for the player

    public PlayerData(Location position, Integer maxAfkMinutes){
        this.position = position;
        this.maxAfkMinutes = maxAfkMinutes;
        this.isAfk = false;
    }

    private boolean comparePosition(Location a, Location b){
        if (a == null || b == null) return false;
        return //a.getWorld().getName().equalsIgnoreCase(b.getWorld().getName()) &&
                a.getBlockX() == b.getBlockX() &&
                a.getBlockY() == b.getBlockY() &&
                a.getBlockZ() == b.getBlockZ() &&
                a.getYaw() == b.getYaw();
    }

    //return true if the new position is the same of the previous
    public boolean setLocation(Location position){
        boolean sameLocation = comparePosition(this.position, position);
        this.position = position;
        return sameLocation;
    }

    //increase of 1 afkMinutes. If afkMinutes > maxAfkMinutes, set the player afk
    public int increaseAfkMinutes(){
        this.afkMinutes+=1;
        if (this.afkMinutes >= maxAfkMinutes){
            this.isAfk = true;
        }
        return  this.afkMinutes;
    }

    //reset afk status of the player
    public void resetAfkStatus(){
        this.afkMinutes = 0;
        this.isAfk = false;
    }

    //reset counter of afkMinutes
    public void resetAfkMinutes(){
        this.afkMinutes = 0;
    }

    public void setAfkStatus(boolean isAfk){
        this.isAfk = isAfk;
    }

    public boolean isAfk(){
        return this.isAfk;
    }

}
