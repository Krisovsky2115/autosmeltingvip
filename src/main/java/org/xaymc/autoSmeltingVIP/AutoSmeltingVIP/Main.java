package org.xaymc.autoSmeltingVIP.AutoSmeltingVIP;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class Main extends JavaPlugin implements CommandExecutor, Listener {

    private static final Set<UUID> autoSmelt = new HashSet<>();
    private static final Map<Material, Material> materialsRemapped = new HashMap<>();
    public static int stackAfter = 128;

    @Override
    public void onEnable() {
        // Initialize material remapping
        materialsRemapped.put(Material.RAW_GOLD, Material.GOLD_INGOT);
        materialsRemapped.put(Material.RAW_IRON, Material.IRON_INGOT);
        materialsRemapped.put(Material.COBBLESTONE, Material.STONE);
        materialsRemapped.put(Material.RAW_COPPER, Material.COPPER_INGOT);

        // Register events and commands
        getServer().getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("przepalaj")).setExecutor(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic (if needed)
    }

    @EventHandler
    public void onPlayerMineBlock(BlockBreakEvent e) {
        Player player = e.getPlayer();
        if (!player.hasPermission("autosmeltingvip.use")) return;

        if (autoSmelt.contains(player.getUniqueId())) {
            e.setCancelled(true); // Cancel the default block break handling

            // Collect smelted drops
            Collection<ItemStack> drops = e.getBlock().getDrops(player.getInventory().getItemInMainHand());
            List<ItemStack> smeltedDrops = new ArrayList<>();

            for (ItemStack drop : drops) {
                if (drop == null) continue;

                Material remapped = materialsRemapped.getOrDefault(drop.getType(), drop.getType());
                smeltedDrops.add(new ItemStack(remapped, drop.getAmount()));
            }

            // Remove the block and drop the smelted items
            e.getBlock().setType(Material.AIR);
            smeltedDrops.forEach(item -> player.getWorld().dropItemNaturally(e.getBlock().getLocation(), item));

            if (!smeltedDrops.equals(drops)) {
                // Send action bar message
                player.spigot().sendMessage(
                        ChatMessageType.ACTION_BAR,
                        TextComponent.fromLegacyText("§8[§ePrzepalarka§8] §7Automatyczne przepalanie jest §aaktywne§7.")
                );
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("przepalaj")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Komenda nie może zostać użyta przez konsolę!");
                return true;
            }

            if (player.hasPermission("autosmeltingvip.use")) {
                if (autoSmelt.contains(player.getUniqueId())) {
                    autoSmelt.remove(player.getUniqueId());
                    player.sendMessage("§8[§aPrzepalarka§8] §aAutomatyczne przepalanie zostało wyłączone.");
                } else {
                    autoSmelt.add(player.getUniqueId());
                    player.sendMessage("§8[§aPrzepalarka§8] §aAutomatyczne przepalanie jest aktywne. Kiedy wykopiesz rudę, zostanie automatycznie przepalona.");
                }
            } else {
                player.sendMessage("§cNie możesz skorzystać z tej komendy! Kup rangę SVIP, aby mieć do niej dostęp.");
            }
            return true;
        }
        return false;
    }
}
