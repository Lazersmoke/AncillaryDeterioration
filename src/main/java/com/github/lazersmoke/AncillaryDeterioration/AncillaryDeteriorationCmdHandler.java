package com.github.lazersmoke.AncillaryDeterioration;

import vg.civcraft.mc.civmodcore.command.CommandHandler;

public class AncillaryDeteriorationCmdHandler extends CommandHandler{
  @Override
  public void registerCommands() {
    addCommands(new AncillaryDeteriorationCmdRot("CmdRot"));
  }
}
