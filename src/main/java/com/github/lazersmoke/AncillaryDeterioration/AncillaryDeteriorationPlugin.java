package com.github.lazersmoke.AncillaryDeterioration;

import org.bukkit.configuration.ConfigurationSection;
import java.io.File;
import vg.civcraft.mc.civmodcore.ACivMod;

public final class AncillaryDeteriorationPlugin extends ACivMod {
  private static AncillaryDeteriorationPlugin instance;
  private static AncillaryDeteriorationListener listener;
  private static AncillaryDeteriorationConfig config;
  private static AncillaryDeteriorationDAO dao;

  @Override
  public void onEnable(){
    instance = this;
    listener = new AncillaryDeteriorationListener();
    config = new AncillaryDeteriorationConfig();
    dao = setupDatabase();
    saveDefaultConfig();
    config.reloadConfig(this.getConfig());
    getServer().getPluginManager().registerEvents(listener, this);
        // Register commands.
    AncillaryDeteriorationCmdHandler handle = new AncillaryDeteriorationCmdHandler();
    setCommandHandler(handle);
    handle.registerCommands();
  }

  private AncillaryDeteriorationDAO setupDatabase() {
    ConfigurationSection config = getConfig().getConfigurationSection("mysql");
    String host = config.getString("host");
    int port = config.getInt("port");
    String user = config.getString("user");
    String pass = config.getString("password");
    String dbname = config.getString("database");
    int poolsize = config.getInt("poolsize");
    long connectionTimeout = config.getLong("connectionTimeout");
    long idleTimeout = config.getLong("idleTimeout");
    long maxLifetime = config.getLong("maxLifetime");
    return new AncillaryDeteriorationDAO(this, user, pass, host, port, dbname, poolsize, connectionTimeout, idleTimeout, maxLifetime);
  }

  public String getPluginName(){
    return "AncillaryDeterioration";
  }

  public static AncillaryDeteriorationPlugin getInstance(){
    return instance;
  }

  public static AncillaryDeteriorationConfig getConfiguration(){
    return config;
  }

  public static AncillaryDeteriorationDAO getDAO(){
    return dao;
  }
}
