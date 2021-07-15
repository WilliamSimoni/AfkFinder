package com.williamsimoni.afkfinder;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class AfkFinder extends JavaPlugin {

    public Map<UUID, PlayerData> playersData;       //contains the playerData for non afk player
    public List<UUID> afkPlayers;                   //list of the ids of the player that are considered afk

    public FileConfiguration config;                //config file

    public Map<Integer, Integer> afkTimePerSub;     //map of times after which player with sub level i is considered AFK

    public Database afkDatabase;                    //instance of the afkDatabase to access to the afk table

    public LoggerHandler loggerHandler;             //handle logging into the console

    public String serverName;                       //name of the server where the plugin is running

    //afkZoneInformation
    public boolean afkZoneActive;                   //if AfkZone is true then the plugin will handle an Afk zone
    public boolean afkZoneTp;                       //if true, change player's server
    public String afkZoneServer;                    //name of the server to which send the afk players

    //Bungeecord Connection
    public AfkCentralConnect afkCentralConnect;     //to communicate with Bungeecord

    //AfkZone Handler
    public AfkZoneHandler afkZoneHandler;           //handle teleport of the players from the AFK positions to their original positions and viceversa

    //Given a config file, generate the config file with the fields needed by this plugin
    public void defaultConfigCreate(FileConfiguration config){
        //afk times settings

        config.addDefault("AfkFinder.ServerName", null);

        config.addDefault("AfkFinder.AfkTimes.Sub0", 0);
        config.addDefault("AfkFinder.AfkTimes.Sub1", 10);
        config.addDefault("AfkFinder.AfkTimes.Sub2", 20);
        config.addDefault("AfkFinder.AfkTimes.Sub3", -1);

        //database settings
        config.addDefault("AfkFinder.DatabaseAfk.Url", "localhost");
        config.addDefault("AfkFinder.DatabaseAfk.Port", 3306);
        config.addDefault("AfkFinder.DatabaseAfk.Username", "root");
        config.addDefault("AfkFinder.DatabaseAfk.Password", "");
        config.addDefault("AfkFinder.DatabaseAfk.DatabaseName", "mc");

        config.addDefault("AfkFinder.Log.Active", true);

        config.addDefault("AfkFinder.AfkZone.Active", false);
        config.addDefault("AfkFinder.AfkZone.TpToAfkZone", false);
        config.addDefault("AfkFinder.AfkZone.ServerName", "");

        config.addDefault("AfkFinder.ServerName", "");

        config.options().copyDefaults(true);
        saveConfig();
    }

    @Override
    public void onEnable() {

        // *-*-*-*-*-*-*-*-*-*-* Handling configuration file *-*-*-*-*-*-*-*-*-*-*
        this.config = this.getConfig();
        defaultConfigCreate(config);

        //set the logger
        this.loggerHandler = new LoggerHandler(config.getBoolean("AfkFinder.Log.Active", true));

        //Getting times which indicate whenever a player is considered AFK according to its sub level
        this.afkTimePerSub = new HashMap<>();
        this.afkTimePerSub.put(0, this.config.getInt("AfkFinder.AfkTimes.Sub0", 0));
        this.afkTimePerSub.put(1000, this.config.getInt("AfkFinder.AfkTimes.Sub1", 10));
        this.afkTimePerSub.put(2000, this.config.getInt("AfkFinder.AfkTimes.Sub2", 20));
        this.afkTimePerSub.put(3000, this.config.getInt("AfkFinder.AfkTimes.Sub3", -1));

        //getting database information
        //database information
        //url of the database
        String afk_database_url = config.getString("AfkFinder.DatabaseAfk.Url", "localhost");
        //database port
        int afk_database_port = config.getInt("AfkFinder.DatabaseAfk.Port", 3306);
        //username of the user through which we will do the access
        String afk_database_username = config.getString("AfkFinder.DatabaseAfk.Username", "root");
        //password of the user with the username indicated in the config
        String afk_database_password = config.getString("AfkFinder.DatabaseAfk.Password", "");
        //name of the database
        String afk_database_database = config.getString("AfkFinder.DatabaseAfk.DatabaseName", "mc");

        //set afkZone
        this.afkZoneActive = config.getBoolean("AfkFinder.AfkZone.Active", false);
        this.afkZoneTp = config.getBoolean("AfkFinder.AfkZone.TpToAfkZone", false);
        this.afkZoneServer = config.getString("AfkFinder.AfkZone.ServerName", "");

        //set the name of the server
        this.serverName = config.getString("AfkFinder.ServerName", "");
        if (this.serverName.equals("")){
            this.loggerHandler.error_message("You should indicate the name (in Bungeecord) of this server ");
            if (this.afkZoneTp){
                this.loggerHandler.warning_message("Disabling teleport of the players");
                this.afkZoneTp = false;
            }
            this.serverName = "";
        }

        this.loggerHandler.info_message("configuration file loaded");

        afkCentralConnect = new AfkCentralConnect(this);

        //register the commands to handle afk zone and create the afkzone handler
        if (this.afkZoneActive) {
            AfkZoneCommandsHandler afkZoneCommandsHandler = new AfkZoneCommandsHandler(this);
            getCommand("tpBungeeServer").setExecutor(afkZoneCommandsHandler); //to be deleted (just for test)
            getCommand("addAfkLoc").setExecutor(afkZoneCommandsHandler);
            getCommand("rmAfkLoc").setExecutor(afkZoneCommandsHandler);
            getCommand("saveAfkZone").setExecutor(afkZoneCommandsHandler);
            getCommand("infoAfkZone").setExecutor(afkZoneCommandsHandler);
            getCommand("tpAfkLoc").setExecutor(afkZoneCommandsHandler);
            this.afkZoneHandler = new AfkZoneHandler(this, "./plugins/AfkFinder/afkPositions.json");
            this.loggerHandler.info_message("commands set");
        }

        // *-*-*-*-*-*-*-*-*-*-* Creating data structures for the plugin *-*-*-*-*-*-*-*-*-*-*

        //creating the database object
        try {
            this.afkDatabase = new Database(afk_database_url,
                    afk_database_port,
                    afk_database_username,
                    afk_database_password,
                    afk_database_database);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        //removing previously afk players associated with this server
        if (this.afkDatabase.removeAllRows(this.serverName)) {
            this.loggerHandler.info_message("server database initialized");
        } else {
            this.loggerHandler.error_message("can not communicate with the database. Disabling plugin");
            return;
        }

        //initialize data structure containing players timestamp
        playersData = new ConcurrentHashMap<>();
        afkPlayers = new LinkedList<>();
        this.loggerHandler.info_message("all data structures created");

        //register event handlers
        getServer().getPluginManager().registerEvents(new QuitJoinEventHandler(this), this);
        getServer().getPluginManager().registerEvents(new PlayerActionEvents(this), this);

        //run AFK finder task (1200L = 1 minute)
        new FindAfkTask(this).runTaskTimerAsynchronously(this, 1200L, 1200L);
        this.loggerHandler.info_message("ready to find AFK players");
    }


    //TODO
    @Override
    public void onDisable() {
    }
}
