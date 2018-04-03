package com.github.lazersmoke.AncillaryDeterioration;

import org.bukkit.block.Block;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.BrewingStandFuelEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.entity.Item;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public final class AncillaryDeteriorationListener implements Listener{
  private static final SimpleDateFormat timeStamp = new SimpleDateFormat("yyyy-MM-dd");
  @EventHandler
  public void onEat(PlayerItemConsumeEvent e){
    Player p = e.getPlayer();
    ItemStack i = e.getItem();
    if(i.getItemMeta().hasLore() && AncillaryDeteriorationPlugin.getInstance().getConfiguration().isModified(i.getType())){
      if(isRotted(i,7)){
        p.sendMessage("That is rotten, you can't eat it");
        e.setCancelled(true);
      }
    }
  }

  @EventHandler
  public void onInteract(PlayerInteractEvent e){
    if(!e.hasItem() || !(e.getItem().getType() == Material.SPLASH_POTION || e.getItem().getType() == Material.LINGERING_POTION) || !AncillaryDeteriorationPlugin.getInstance().getConfiguration().isModified(e.getItem().getType())){
      return;
    }
    if(isRotted(e.getItem(),7)){
      e.getPlayer().sendMessage("That is rotten, you can't use it");
      e.setCancelled(true);
    }
  }
  
  @EventHandler
  public void onItemSpawn(ItemSpawnEvent e){
    Item item = e.getEntity();
    ItemStack i = item.getItemStack();
    if(timestampIt(i)){
      item.setItemStack(i);
    }
  }

  @EventHandler
  public void onClick(InventoryClickEvent e){
    ItemStack c = e.getCurrentItem();
    if(timestampIt(c)){
      e.setCurrentItem(c);
    }
    timestampIt(e.getCursor());
    final Inventory clickerInv = e.getWhoClicked().getInventory();
    if(e.getAction() == InventoryAction.HOTBAR_SWAP || e.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD){
      ItemStack h = clickerInv.getItem(e.getHotbarButton());
      if(timestampIt(h)){
        clickerInv.setItem(e.getHotbarButton(),h);
      }
    }
  }

  @EventHandler
  public void onHopper(InventoryMoveItemEvent e){
    ItemStack i = e.getItem();
    boolean cancelIt = needsTimestamp(i);
    if(cancelIt){
      e.setCancelled(true);
      // No access to problem item until next tick
      Bukkit.getScheduler().runTask(AncillaryDeteriorationPlugin.getInstance(),() -> {
        e.getSource().forEach(AncillaryDeteriorationListener::timestampIt);
      });
    }
  }

  @EventHandler
  public void onBrewRefill(BrewingStandFuelEvent e){
    ItemStack blaze = e.getFuel();
    if(isRotted(blaze,7)){
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onBrew(BrewEvent e){
    ItemStack ingred = e.getContents().getIngredient();
    if(isRotted(ingred,7)){
      e.setCancelled(true);
      return;
    }
    // No access to new pots until next tick
    Bukkit.getScheduler().runTask(AncillaryDeteriorationPlugin.getInstance(),() -> {
      e.getContents().forEach(AncillaryDeteriorationListener::timestampIt);
    });
  }

  @EventHandler
  public void onPreCraft(PrepareItemCraftEvent e){
    fixCraftingInventory(e.getInventory());
  }

  @EventHandler
  public void onCraft(CraftItemEvent e){
    fixCraftingInventory(e.getInventory());
  }

  @EventHandler(ignoreCancelled=true)
  public void onPlace(BlockPlaceEvent e){
    LocalDate stamp = getTimestamp(e.getItemInHand());
    if(stamp != null){
      AncillaryDeteriorationPlugin.getDAO().applyStamp(stamp,e.getBlockPlaced().getLocation());
    }
  }

  @EventHandler(ignoreCancelled=true)
  public void onBreak(BlockBreakEvent e){
    e.setDropItems(!handleBlockDestroyed(e.getBlock(),e.getPlayer().getInventory().getItemInMainHand()));
  }

  @EventHandler(ignoreCancelled=true)
  public void onBreak(BlockPhysicsEvent e){
    Material m = e.getBlock().getType();
    switch(m){
      case ACACIA_DOOR:
      case ACTIVATOR_RAIL:
      case BEETROOT_BLOCK:
      case BIRCH_DOOR:
      case BROWN_MUSHROOM:
      case CACTUS:
      case CARPET:
      case CARROT:
      case CHORUS_FLOWER:
      case CHORUS_FRUIT:
      case CHORUS_FRUIT_POPPED:
      case CHORUS_PLANT:
      case COCOA:
      case CROPS:
      case DARK_OAK_DOOR:
      case DEAD_BUSH:
      case DETECTOR_RAIL:
      case DIODE_BLOCK_ON:
      case DIODE_BLOCK_OFF:
      case DOUBLE_PLANT:
      case DRAGON_EGG:
      case FLOWER_POT:
      case JUNGLE_DOOR:
      case LADDER:
      case LEVER:
      case LONG_GRASS:
      case MELON_STEM:
      case NETHER_WARTS:
      case POTATO:
      case POWERED_RAIL:
      case PUMPKIN_STEM:
      case RAILS:
      case RED_MUSHROOM:
      case RED_ROSE:
      case REDSTONE_COMPARATOR_OFF:
      case REDSTONE_COMPARATOR_ON:
      case REDSTONE_TORCH_OFF:
      case REDSTONE_TORCH_ON:
      case REDSTONE_WIRE:
      case SAND:
      case SAPLING:
      case SIGN_POST:
      case SKULL:
      case SNOW:
      case SPRUCE_DOOR:
      case STANDING_BANNER:
      case STONE_BUTTON:
      case STONE_PLATE:
      case SUGAR_CANE_BLOCK:
      case TORCH:
      case TRIPWIRE_HOOK:
      case VINE:
      case WALL_BANNER:
      case WALL_SIGN:
      case WOOD_BUTTON:
      case WOOD_PLATE:
      case YELLOW_FLOWER:
        handleBlockDestroyed(e.getBlock());
    }
  }

  public static boolean handleBlockDestroyed(Block b){
    return handleBlockDestroyed(b,null);
  }

  public static boolean handleBlockDestroyed(Block b, ItemStack tool){
    LocalDate stamp = AncillaryDeteriorationPlugin.getDAO().getStamp(b.getLocation());
    if(stamp != null){
      AncillaryDeteriorationPlugin.getDAO().forgetStamp(b.getLocation());
      Collection<ItemStack> defaultDrops = tool == null ? b.getDrops() : b.getDrops(tool);
      defaultDrops.stream().forEach(i -> {
        if(needsTimestamp(i)){
          addSpecificTimestamp(i,stamp);
        }
        b.getWorld().dropItemNaturally(b.getLocation().add(0.5,0.5,0.5),i);
      });
      return true;
    }
    return false;
  }

  public static void fixCraftingInventory(CraftingInventory craft){
    LocalDate date = null;
    // Get oldest stamp from input
    for(ItemStack i : craft.getMatrix()){
      LocalDate stamp = getTimestamp(i);
      if(stamp != null && (date == null || stamp.toEpochDay() < date.toEpochDay())){
        date = stamp;
      }
    }
    ItemStack result = craft.getResult();
    if(date != null && result != null && needsTimestamp(result)){
      addSpecificTimestamp(result,date);
      craft.setResult(result);
    }
  }

  public static boolean isRotted(ItemStack i,long rotTime){
    LocalDate date = getTimestamp(i);
    return date != null && LocalDate.now().toEpochDay() - date.toEpochDay() > rotTime;
  }

  public static boolean timestampIt(ItemStack i){
    if(needsTimestamp(i)){
      addTimestamp(i);
      return true;
    }
    return false;
  }

  public static boolean needsTimestamp(ItemStack i){
    return i != null && getTimestamp(i) == null && AncillaryDeteriorationPlugin.getInstance().getConfiguration().isModified(i.getType());
  }

  public static void addTimestamp(ItemStack i){
    addSpecificTimestamp(i,LocalDate.now());
  }

  public static void addSpecificTimestamp(ItemStack i,LocalDate date){
    ItemMeta meta = i.getItemMeta();
    List<String> lore = meta.getLore();
    if(lore == null){
      lore = new ArrayList<String>();
    }
    lore.add(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
    meta.setLore(lore);
    i.setItemMeta(meta);
  }

  public static void stripTimestamp(ItemStack i){
    if(i == null){
      return;
    }
    ItemMeta meta = i.getItemMeta();
    if(meta == null || !meta.hasLore()){
      return;
    }
    meta.setLore(null);
    i.setItemMeta(meta);
  }

  public static LocalDate getTimestamp(ItemStack i){
    if(i == null){
      return null;
    }
    ItemMeta meta = i.getItemMeta();
    if(meta == null || !meta.hasLore()){
      return null;
    }
    List<String> lore = meta.getLore();
    LocalDate date = null;
    for(String l : lore){
      try{
        date = LocalDate.parse(l,DateTimeFormatter.ISO_LOCAL_DATE);
        break;
      }catch(DateTimeParseException e){continue;}
    }
    return date;
  }
}
