package com.williamsimoni.afkfinder;

import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Database {

    private final MysqlDataSource source;

    public Database(String serverName, int port, String username, String password, String database) throws SQLException {
        MysqlDataSource source = new MysqlConnectionPoolDataSource();
        source.setServerName(serverName);
        source.setPortNumber(port);
        source.setDatabaseName(database);
        source.setUser(username);
        source.setPassword(password);
        this.source = source;
    }

    public Connection getConnection() throws SQLException {
        return source.getConnection();
    }

    /*
    * player must be different from null
    *
    * return the player data stored in the database
    * */
    public String getServer(Player player) {
        try(Connection conn = getConnection(); PreparedStatement query = conn.prepareStatement("select server from afkplayer where name = ?")) {
            query.setString(1, player.getName());
            ResultSet set = query.executeQuery();
            if(set.next())
                return set.getString("server");
            else
                return null;
        } catch(SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    /*
    * Remove the user from the afk database
    *
    * Returns true if operation ends with success. False otherwise.
    * */
    public boolean removeAfkPlayerData(Player player){
        try(Connection conn = getConnection(); PreparedStatement query = conn.prepareStatement("DELETE FROM afkplayer WHERE afkplayer.name=?")) {
            query.setString(1, player.getName());
            query.executeUpdate();
            return true;
        } catch(SQLException exception) {
            exception.printStackTrace();
        }
        return false;
    }

    /*
    * Add the user to the afk database
    *
    * Returns true if operation ends with success. False otherwise.
    * */
    public boolean addAdkPlayer(Player player, String serverName){
        try(Connection conn = getConnection(); PreparedStatement query = conn.prepareStatement("INSERT INTO afkplayer (`name`, `server`) VALUES (?,?)")) {
            query.setString(1, player.getName());
            query.setString(2, serverName);
            query.executeUpdate();
            return true;
        } catch(SQLException exception) {
            exception.printStackTrace();
        }
        return false;
    }


    /*
    * returns true if the player is afk (is in the database) and false if it is not afk
    * */
    public boolean isAfk(Player player){
        try(Connection conn = getConnection(); PreparedStatement query = conn.prepareStatement("SELECT 1 FROM afkplayer WHERE EXISTS(SELECT 1 FROM afkplayer afk WHERE afk.name=?)")) {
            query.setString(1, player.getName());
            ResultSet set = query.executeQuery();
            return set.next();
        } catch(SQLException exception) {
            exception.printStackTrace();
        }
        return false;
    }

    public int getSub(Player player) {
        try(Connection connection = getConnection(); PreparedStatement st = connection.prepareStatement("select sub from twitchIds where name = ? limit 1")) {
            st.setString(1, player.getName());
            ResultSet set = st.executeQuery();
            if (set.next())
                return set.getInt(1);
            else
                return 0;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }

    //remove all rows with server=serverName
    public boolean removeAllRows(String serverName){
        try(Connection conn = getConnection(); PreparedStatement query = conn.prepareStatement("DELETE FROM afkplayer WHERE server = ?")) {
            query.setString(1, serverName);
            query.executeUpdate();
            return true;
        } catch(SQLException exception) {
            exception.printStackTrace();
        }
        return false;
    }
}