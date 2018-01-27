package com.github.lazersmoke.AncillaryDeterioration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.time.LocalDate;
import org.bukkit.Location;

import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

public class AncillaryDeteriorationDAO {

  private static final String GET_STAMP = "select stamp from physicalDecay where x=? and y=? and z=? and world=?;";
  private static final String APPLY_STAMP = "replace into physicalDecay (stamp,x,y,z,world) values (?,?,?,?,?);";
  private static final String FORGET_STAMP = "delete from physicalDecay where x=? and y=? and z=? and world=?;";

  private final ManagedDatasource db;

  public AncillaryDeteriorationDAO(AncillaryDeteriorationPlugin plugin, String user, String pass, String host, int port, String database, int poolSize, long connectionTimeout, long idleTimeout, long maxLifetime) {
    ManagedDatasource theDB = null;
    try {
      theDB = new ManagedDatasource(plugin, user, pass, host, port, database, poolSize, connectionTimeout, idleTimeout, maxLifetime);
      theDB.getConnection().close(); // Test connection
      registerMigrations(theDB);
      if(!theDB.updateDatabase()) {
        plugin.warning("Failed to updated database, stopping AncillaryDeterioration");
        plugin.getServer().getPluginManager().disablePlugin(plugin);
      }
    } catch(Exception e) {
      plugin.warning("Could not connect to database, stopping AncillaryDeterioration", e);
      plugin.getServer().getPluginManager().disablePlugin(plugin);
    } finally {db = theDB;}
  }

  private void registerMigrations(ManagedDatasource theDB) {
    theDB.registerMigration(1, true,
        "create table if not exists physicalDecay ("
        + "stamp date not null,"
        + "x int not null,"
        + "y int not null,"
        + "z int not null,"
        + "world varchar(10) not null,"
        + "primary key(x,y,z));");
  }

  public LocalDate getStamp(Location loc) {
    try (Connection conn = db.getConnection();
        PreparedStatement ps = conn.prepareStatement(GET_STAMP)) {
      ps.setInt(1, loc.getBlockX());
      ps.setInt(2, loc.getBlockY());
      ps.setInt(3, loc.getBlockZ());
      ps.setString(4, loc.getWorld().getName());
      ResultSet res = ps.executeQuery();
      if(res.next()) {
        return res.getDate("stamp").toLocalDate();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  public void forgetStamp(Location loc) {
    try (Connection conn = db.getConnection();
        PreparedStatement ps = conn.prepareStatement(FORGET_STAMP)) {
      ps.setInt(1, loc.getBlockX());
      ps.setInt(2, loc.getBlockY());
      ps.setInt(3, loc.getBlockZ());
      ps.setString(4, loc.getWorld().getName());
      ps.execute();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void applyStamp(LocalDate date, Location loc) {
    try (Connection conn = db.getConnection();
        PreparedStatement ps = conn.prepareStatement(APPLY_STAMP)) {
      ps.setDate(1, java.sql.Date.valueOf(date));
      ps.setInt(2, loc.getBlockX());
      ps.setInt(3, loc.getBlockY());
      ps.setInt(4, loc.getBlockZ());
      ps.setString(5, loc.getWorld().getName());
      ps.execute();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}

