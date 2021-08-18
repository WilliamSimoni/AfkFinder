package com.williamsimoni.afkfinder;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.*;

public class AfkZone {

    private List<Integer> freeZones;                    //list of free zones
    private Map<Integer, Zone> zones;                   //list of all the zones
    private int nextID;                             //nextID to be associated to a zone
    private Map<UUID, Integer> occupiedZones;           //map between afk player and occupied zone
    private AfkFinder afkFinder;

    /*
    * fileName is the name of the JSON file from which we initialize the AfkZone
    * */
    public AfkZone(AfkFinder afkFinder, String fileName){
        //initialize data structures
        this.freeZones = new LinkedList <>();
        this.occupiedZones = new HashMap<>();
        this.zones = new HashMap<>();
        this.afkFinder = afkFinder;
        this.nextID = 0;

        //read from the file
        if (fileName != null)
            read(fileName);
    }

    //save the JSON
    public boolean save(String filename){
        JSONObject jsonObject = new JSONObject();
        JSONArray positions = new JSONArray();

        Iterator<Zone> iterator = this.zones.values().iterator();
        while (iterator.hasNext()){
            Zone zone = iterator.next();

            //creating element list
            JSONObject el = new JSONObject();
            el.put("world", zone.location.getWorld().getName());
            el.put("x", zone.location.getBlockX());
            el.put("y", zone.location.getBlockY());
            el.put("z", zone.location.getBlockZ());
            el.put("yaw", zone.location.getYaw());
            el.put("pitch", zone.location.getPitch());
            el.put("name", zone.name);

            //adding element at the end of the JSON array
            positions.add(el);
        }
        jsonObject.put("positions", positions);


        //writing the file
        try (FileWriter file = new FileWriter(filename)) {
            file.write(jsonObject.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    //read the JSON
    private boolean read(String filename){
        JSONParser parser = new JSONParser();

        try (Reader reader = new FileReader(filename)) {

            JSONObject jsonObject = (JSONObject) parser.parse(reader);

            // loop array
            JSONArray positions = (JSONArray) jsonObject.get("positions");
            Iterator<JSONObject> iterator = positions.iterator();
            while (iterator.hasNext()) {
                JSONObject el = iterator.next();

                //reading data from the single json element in the list
                World world = this.afkFinder.getServer().getWorld((String) el.get("world"));
                Double x = ((Number)el.get("x")).doubleValue();
                Double y = ((Number)el.get("y")).doubleValue();
                Double z = ((Number)el.get("z")).doubleValue();
                Float yaw = ((Number)el.get("yaw")).floatValue();
                Float pitch = ((Number)el.get("pitch")).floatValue();

                //setting the location
                Location location = new Location(world, x ,y, z);
                location.setYaw(yaw);
                location.setPitch(pitch);

                //adding a new free zone
                this.addZone((String) el.get("name"), location);
            }

        } catch (FileNotFoundException e) {
            this.afkFinder.loggerHandler.info_message(filename + " does not exist. So, there is not any position in Afk Zone\nUse addAfkLoc to add a position and rmAfkLoc to remove a previously created position");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public int getID(int inc){
        int tmp = this.nextID;
        this.nextID += inc;
        return tmp;
    }

    //add a zone and put at the end of freeZones (and add into JSON)
    public void addZone(String name, Location location){
        Zone tmp = new Zone(name, location);
        //get id for the new zone
        int id = this.getID(1);
        //add element in data structure
        this.zones.put(id, tmp);
        //add element in free list
        this.freeZones.add(id);
    }

    //remove the zone from the freeZones (and if deletion went well, remove from JSON)
    //can not delete a zone occupied by a player
    public boolean removeZone(String name){
        //search for the id of the zone
        Integer id = findZoneId(name);

        if (id != null) {
            //if the zone is not occupied
            if (this.freeZones.contains(id)) {
                this.freeZones.remove(id);
                this.zones.remove(id);
                return true;
            }
        }

        return false;
    }

    //find the id of the zone with the given name
    //return null if the name is not associated to any zone
    public Integer findZoneId(String name){
        Iterator iterator = this.zones.entrySet().iterator();

        //search for the id of the zone
        while (iterator.hasNext()){
            Map.Entry pair = (Map.Entry)iterator.next();
            Zone zone = (Zone) pair.getValue();
            if (zone.name.equalsIgnoreCase(name)){
                return (Integer) pair.getKey();
            }
        }

        return null;
    }

    //put player into a free zone. Returns null if all zones are occupied
    public Location occupyZone(UUID playerUuid){
        if (this.freeZones.isEmpty())
            return null;

        Integer id = this.freeZones.get(0);
        this.freeZones.remove(0);
        this.occupiedZones.put(playerUuid,id);

        Zone tmp = this.zones.get(id);
        tmp.occupied = true;

        return tmp.location;
    }

    //free a zone. Returns false if the player is not contained in any of the occupied zones.
    public boolean freeZone(UUID playerUuid){
        Integer id = this.occupiedZones.get(playerUuid);

        if (id == null)
            return false;

        this.occupiedZones.remove(id);
        this.freeZones.add(id);
        this.zones.get(id).occupied = false;

        return true;
    }

    //return a string containing information regarding the zone
    public String infoZone(){
        Iterator iterator = this.zones.values().iterator();
        String result = "";
        int numOcp = 0;

        //iterate over all the zones
        while (iterator.hasNext()){
            Zone zone = (Zone) iterator.next();
            if (!zone.occupied){
                result += ChatColor.GREEN + "FREE" + ChatColor.WHITE + " | " + ChatColor.DARK_GREEN + zone.name + "\n";
            } else {
                result += ChatColor.RED + "OCCP" + ChatColor.WHITE + " | " + ChatColor.DARK_RED + zone.name + "\n";
                numOcp++;
            }
        }

        int percentageOccupiedZones = ((numOcp * 100)/this.zones.size())/5;
        System.out.println((numOcp * 100)/this.zones.size());
        System.out.println((numOcp * 100));
        String percentageString = "";

        for (int i = 0; i < percentageOccupiedZones; i++){
            percentageString += "#";
        }
        for (int i = percentageOccupiedZones; i < 20; i++){
            percentageString += "-";
        }

        if (percentageOccupiedZones > 10){
            result += ChatColor.YELLOW;
            if (percentageOccupiedZones > 18){
                result += ChatColor.DARK_RED;
            }
        }

        result += "% of locations occupied: " + percentageString;

        return  result;
    }

    public List<String> getZoneNames(String name){
        Iterator iterator = this.zones.values().iterator();
        List<String> list = new ArrayList<>();

        while (iterator.hasNext()){
            Zone zone = (Zone) iterator.next();
            if (zone.name.startsWith(name))
                list.add(zone.name);
        }

        return list;
    }

    public Location getZoneLocation(String name){
        Integer id = this.findZoneId(name);

        if (id != null){
            return this.zones.get(id).location;
        }

        return  null;
    }
}

final class Zone{
    String name;
    Location location;
    Boolean occupied;

    public Zone(String name, Location location){
        this.name = name;
        this.location = location;
        this.occupied = false;
    }
}