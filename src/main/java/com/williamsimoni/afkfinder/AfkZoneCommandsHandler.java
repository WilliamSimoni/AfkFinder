package com.williamsimoni.afkfinder;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AfkZoneCommandsHandler implements CommandExecutor, TabCompleter {

    private AfkFinder afkFinder;

    public AfkZoneCommandsHandler(AfkFinder afkFinder){
        this.afkFinder = afkFinder;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }

        if (player != null){
            if (player.isOp()){

                //JUST FOR TEST
                if (command.getName().equalsIgnoreCase("tpBungeeServer")){
                    String name = args[0];
                    boolean res = this.afkFinder.afkCentralConnect.sendToServer(player,name);
                    System.out.println(res);
                    return true;
                }

                //add afk location /////*******//////////*******//////////*******//////////*******//////////*******/////
                if (command.getName().equalsIgnoreCase("addAfkLoc")){
                    //get afkzone object
                    AfkZone afkZone = this.afkFinder.afkZoneHandler.getAfkZone();
                    String zoneName;

                    if (args.length == 0){
                        //no name provided, so, generate a new one
                        int id = afkZone.getID(0);
                        zoneName = "zone" + id;

                    } else {

                        //name provided by the player
                        zoneName = args[0];
                        if (afkZone.findZoneId(zoneName) != null){
                            //zone already exists
                            player.sendMessage(ChatColor.RED + "Position " + zoneName + " already exists");
                            return true;
                        }

                    }

                    //add a zone to the afk zone
                    afkZone.addZone(zoneName, player.getLocation());

                    //send feedback message to the player
                    player.sendMessage(ChatColor.GREEN + "Position " + zoneName + " added to the afk zone");

                    return true;
                }

                //remove afk location /////*******//////////*******//////////*******//////////*******//////////*******//
                if (command.getName().equalsIgnoreCase("rmAfkLoc")){

                    if (args.length == 0){
                        player.sendMessage(ChatColor.RED + "You should indicate the name of the position you plan to delete");
                        return false;
                    }

                    AfkZone afkZone = this.afkFinder.afkZoneHandler.getAfkZone();

                    if (afkZone.removeZone(args[0])){
                        //success removing the location
                        player.sendMessage(ChatColor.GREEN + "Position " + args[0] + " removed from the afk zone");
                    } else {
                        //the location does not exist
                        player.sendMessage(ChatColor.RED + "Position " + args[0] + " does not exist in the afk zone of this server");
                    }

                    return true;
                }

                //save afk zone /////*******//////////*******//////////*******//////////*******//////////*******/////***
                if (command.getName().equalsIgnoreCase("saveAfkZone")){
                    if (this.afkFinder.afkZoneHandler.saveZones("./plugins/AfkFinder/afkPositions.json"))
                        player.sendMessage(ChatColor.GREEN + "Locations saved. Check afkPositions.json in the plugin directory");
                    else
                        player.sendMessage(ChatColor.RED + "Problems in saving the new positions.");
                    return true;
                }

                //info afk zone /////*******//////////*******//////////*******//////////*******//////////*******/////***
                if (command.getName().equalsIgnoreCase("infoAfkZone")){
                    AfkZone afkZone = this.afkFinder.afkZoneHandler.getAfkZone();
                    player.sendMessage("List of afk positions:");
                    player.sendMessage(afkZone.infoZone());
                    return true;
                }

                //tp to afk location with the given name /////*******//////////*******//////////*******//////////*******
                if (command.getName().equalsIgnoreCase("tpAfkLoc")){
                    AfkZone afkZone = this.afkFinder.afkZoneHandler.getAfkZone();

                    if (args.length == 0){
                        player.sendMessage(ChatColor.RED + "You should indicate the name of the position you plan to tp to");
                        return false;
                    }

                    Location tpLocation = afkZone.getZoneLocation(args[0]);
                    if (tpLocation != null){
                        player.teleport(tpLocation);
                    } else {
                        player.sendMessage(ChatColor.RED + "Position " + args[0] + " does not exist in the afk zone of this server");
                    }
                    return true;
                }

            } else {
                //inform player that only operator can perform the command
                player.sendMessage(ChatColor.RED + "Solo gli operatori del server possono usare questo comando");
                return true;
            }
        }

        return false;
    }

    /**
     * Requests a list of possible completions for a command argument.
     *
     * @param sender  Source of the command.  For players tab-completing a
     *                command inside of a command block, this will be the player, not
     *                the command block.
     * @param command Command which was executed
     * @param alias   The alias used
     * @param args    The arguments passed to the command, including final
     *                partial argument to be completed and command label
     * @return A List of possible completions for the final argument, or null
     * to default to the command executor
     */
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }

        if (player != null && player.isOp() && command.getName().equalsIgnoreCase("rmAfkLoc")){
            if (args.length == 1)
                return this.afkFinder.afkZoneHandler.getAfkZone().getZoneNames(args[0]);
            else
                return this.afkFinder.afkZoneHandler.getAfkZone().getZoneNames("");
        }

        if (player != null && player.isOp() && command.getName().equalsIgnoreCase("tpAfkLoc")){
            if (args.length == 1)
                return this.afkFinder.afkZoneHandler.getAfkZone().getZoneNames(args[0]);
            else
                return this.afkFinder.afkZoneHandler.getAfkZone().getZoneNames("");
        }

        return null;
    }
}
