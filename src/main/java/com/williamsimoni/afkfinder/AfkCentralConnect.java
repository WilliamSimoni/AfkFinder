package com.williamsimoni.afkfinder;

//handle comunication with AfkCentral (in Bungeecord) to move players

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class AfkCentralConnect {

    private AfkFinder afkFinder;

    public AfkCentralConnect(AfkFinder afkFinder){
        this.afkFinder = afkFinder;
        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this.afkFinder, "BungeeCord");
    }

    /* move player in server named serverName
    * return true if operation have success, false otherwise
    * */
    public boolean sendToServer(Player player, String serverName){
        //create and initialize the variables
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        //Cast the commands to the DataOutputStream
        try {
            dataOutputStream.writeUTF("Connect");
            dataOutputStream.writeUTF(serverName);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        //Send the message as a plugin message
        player.sendPluginMessage(this.afkFinder, "BungeeCord", byteArrayOutputStream.toByteArray());

        try {
            dataOutputStream.close();
            byteArrayOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
