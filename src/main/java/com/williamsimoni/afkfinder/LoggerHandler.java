package com.williamsimoni.afkfinder;

/*
* Handle logging in the server
* */

import org.bukkit.Bukkit;

public class LoggerHandler {

    private boolean active;
    private String idLog;

    public LoggerHandler(boolean active){
        this.active = active;
        this.idLog = "[AfkFinder] ";
    }

    public void info_message(String msg){
        if (this.active)
            Bukkit.getLogger().info(this.idLog + msg);
    }

    public void error_message(String msg){
        if (this.active)
            Bukkit.getLogger().severe(this.idLog + msg);
    }

    public void warning_message(String msg){
        if (this.active)
            Bukkit.getLogger().warning(this.idLog + msg);
    }

}
