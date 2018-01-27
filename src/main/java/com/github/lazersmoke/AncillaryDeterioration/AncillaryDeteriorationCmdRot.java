package com.github.lazersmoke.AncillaryDeterioration;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.LinkedList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AncillaryDeteriorationCmdRot extends PlayerCommand {
  public AncillaryDeteriorationCmdRot(String name) {
    super(name);
    setIdentifier("ancillaryrot");
    setDescription("Rot an item");
    setUsage("/ancillaryrot");
    setArguments(0,1);
  }

  public boolean execute(CommandSender sender, String [] args) {
    if(!(sender instanceof Player)){
      return true;
    }
    Player p = (Player) sender;
    ItemStack i = p.getInventory().getItemInMainHand();
    LocalDate date = AncillaryDeteriorationListener.getTimestamp(i);
    int rotAmount = 1;
    if(args.length > 0){
      try {
        rotAmount = Integer.parseInt(args[0]);
      } catch (Exception e) {
        msg("<i>%s <b>is not a valid amount of rot",args[0]);
        return true;
      }
    }
    if(date != null){
      AncillaryDeteriorationListener.stripTimestamp(i);
      AncillaryDeteriorationListener.addSpecificTimestamp(i,date.minusDays(rotAmount));
      msg("<g>Rotted from <i>%s <g>to <i>%s<g>.",date.format(DateTimeFormatter.ISO_LOCAL_DATE),date.minusDays(rotAmount).format(DateTimeFormatter.ISO_LOCAL_DATE));
    }
    return true;
  }

  public List <String> tabComplete(CommandSender sender, String [] args) {
    return new LinkedList <String> (); //empty list
  }
}
