# AfkFinder
Plugin to handle afk players. 

## Configuration tutorial

In this section, we will try to explain how to configure the AfkFinder in your server. To do that, we will consider a situation where we have two Paper servers, called Lobby and Survival, connected with a Bungeecord server. In addition, we will need a database containing the tables afkplayer and twitchids (see Database section to create those tables). 

First thing first, we need to copy the plugin jar in the plugin directory of the Lobby and Survival servers. 

We will start configuring the Lobby where we will set up our AFK zone. So, we create an AfkFinder directory, and then we insert inside it a config.yaml file with the following structure:

```yaml
AfkFinder:
  AfkTimes:
    Sub0: 10
    Sub1: 20
    Sub2: 30
    Sub3: -1
  DatabaseAfk:
    Url: localhost
    Port: 3306
    Username: root
    Password: ''
    DatabaseName: mc
  Log:
    Active: true
  AfkZone:
    Active: true 
    TpToAfkZone: true
    ServerName: Lobby
  ServerName: Lobby
```
* The **AfkTimes** fields indicate after how long (in minutes) the plugin considers a player with a certain sub-level an AFK player. In our example, a player with sub-level 0 is considered AFK after 10 minutes of inactivity. On the other hand, players with sub-level three do not have to worry about the sinister Afk finder because the plugin will never consider them AFK.

* The **DatabaseAfk** fields contain information that allows the plugin to communicate with the database. In our example, the database is in the same machine as the Paper servers through port 3306. The database containing our tables of interest is called mc. 

* The **Log.Active** field is a boolean. If it is true, the plugin will print some log information in the server console.

These first fields will probably be the same in all the servers where the plugin will run. That is for sure true in our example. 

* **AfkZone.Active** is a boolean. If it is true, it means that you are planning to have an AFKzone in this server. In our example, we indeed want to have the AFK zone in the Lobby. Therefore, we put this field to true.

* **AfkZone.TpToAfkZone** is a boolean. This flag only matters if we put the previous field to false. So, in the Lobby config file, this field is pretty useless. If we set this field to true, then the plugin will move the player to the server whose name is in the **AfkZone.ServerName** field. The name must be the same as indicated in the Bungeecord config file. 

* **ServerName** must contain the name of the server as indicated in the Bungeecord config file. 

Let us now write the config file for the Survival server:

```yaml
AfkFinder:
  AfkTimes:
    Sub0: 10
    Sub1: 20
    Sub2: 30
    Sub3: -1
  DatabaseAfk:
    Url: localhost
    Port: 3306
    Username: root
    Password: ''
    DatabaseName: mc
  Log:
    Active: true
  AfkZone:
    Active: false
    TpToAfkZone: true
    ServerName: Lobby
  ServerName: Survival
```
As you can see, almost nothing changed except the last rows:

* We set **AfkZone.Active** to false because we do not want an AFK zone in the Survival server.
* We set **AfkZone.TpToAfkZone** to true because we want the plugin to move players to another server when they are AFK. If we fixed it to false, the plugin would only update the database without moving the player.
* We set **AfkZone.ServerName** to Lobby because we want the plugin to move players in the Lobby server when they are AFK.
* **ServerName** is now Survival.

And that it is. The plugin is configured and ready to run. 

## Create and AFK zone
To create the AFK zone, we have to say to the plugin the locations where we plan to teleport the players when they are AFK. Notice that those locations can potentially be in wholly different parts of the world or the server. 

To add an AFK position to the AFK zone of a server, we will use the **addAfkloc [name]** command, which has as an optional parameter the name you want to give to the AFK location. Consider **addAfkloc** as a button to take pictures. Once you perform that command, the server will record your location. If the plugin teleports players in that AFK location, they will see what you see now. For the sake of organization, you should always give a name to a location. In general, we suggest writing down on a piece of paper the structure of your AFK zone and write for every location its name. If you do not give a name to a location, the plugin will take care of it.

You can remove an AFK location using **rmAfkLoc -name-**, which requires the name of the location you plan to delete.

If you do not remember how an AFK location looks, you can use the **tpAfkLoc -name-** command, which will teleport you to the location called -name-.

Once you are satisfied, remember to use the **saveafkzone**, which will write a .json file in the plugin directory.

Finally, you can monitor the AFK positions using **infoafkzone**. Infoafkzone will list all the AFK locations, coloring green the available locations, and coloring red the occupied ones. In the end, it will show the percentual of AFK locations occupied.

## Database
The queries to build the Afkplayer and twitchids tables are the following:
```sql
CREATE TABLE Afkplayer (
  name varchar(32) NOT NULL,
  server varchar(128) NOT NULL,
  PRIMARY KEY (name)
);

CREATE TABLE twitchIds (
name varchar(32) PRIMARY KEY, 
id text, 
sub int DEFAULT 0
);
```

