package com.samwolfson.spleeftime.listeners;

import com.samwolfson.spleeftime.SpleefTime;
import com.samwolfson.spleeftime.config.SpleefChest;
import org.bukkit.ChatColor;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

import static org.bukkit.Material.CHEST;

/**
 * Listen for the creation of new Spleef chests, i.e.,
 * those with a sign on them that says:
 * [Spleef]
 */
public class SpleefChestListener implements Listener {
    @EventHandler
    public void onSpleefChestCreate(SignChangeEvent e) {
        if (!signAttachedToChest(e.getBlock())) {
            return;
        }

        String username = validateSign(e.getLines());

        if (username == null) {
            return;
        }

        if (SpleefTime.getInstance().getConfigData().getChests().containsKey(e.getPlayer().getName())) {
            e.getPlayer().sendMessage("You already have a chest for Spleef.");
            e.getPlayer().sendMessage("Remove the sign on that chest before creating another.");
            return;
        }

        // replace third line with username
        e.setLine(2, e.getPlayer().getName());
        e.getPlayer().sendMessage(ChatColor.GREEN + "You've created a Spleef Chest!");
        e.getPlayer().sendMessage(ChatColor.GREEN + "From now on, your items will be stored here during games.");

        WallSign signData = (WallSign) e.getBlock().getState().getBlockData();
        BlockFace facingBack = signData.getFacing().getOppositeFace();
        Block attached = e.getBlock().getRelative(facingBack);
        SpleefTime.getInstance().getConfigData().getChests().put(e.getPlayer().getName(), new SpleefChest(attached.getLocation(), e.getPlayer()));
        SpleefTime.getInstance().saveConfigData();
    }

    @EventHandler
    public void onSpleefChestDestroy(BlockBreakEvent e) {
        if (e.getBlock().getType().equals(CHEST)) {
            handleChestDestroy(e);
        } else if (signAttachedToChest(e.getBlock())) {
            handleSignDestroy(e);
        }
    }

    public void handleSignDestroy(BlockBreakEvent e) {
        Block signBlock = e.getBlock();
        WallSign signData = (WallSign) e.getBlock().getState().getBlockData();
        BlockFace facingBack = signData.getFacing().getOppositeFace();

        String username = validateSign(((Sign) signBlock.getState()).getLines());

        if (username == null || username.isEmpty()) {
            return;
        }

        // username on sign must match player attempting to destroy
        if (!username.equals(e.getPlayer().getName())) {
            e.getPlayer().sendMessage("You can't destroy someone else's Spleef chest.");
            e.setCancelled(true);
            return;
        }

        removePlayerChest(e.getPlayer());
    }

    public void handleChestDestroy(BlockBreakEvent e) {
        Block chestBlock = e.getBlock();
        org.bukkit.block.data.type.Chest chestData = (org.bukkit.block.data.type.Chest) e.getBlock().getState().getBlockData();
        BlockFace facingBlock = chestData.getFacing();

        // destroyed chest must have attached sign
        Block attached = chestBlock.getRelative(facingBlock);
        if (!Tag.SIGNS.isTagged(attached.getType())) {
            return;
        }

        Sign attachedSign = (Sign) attached.getState();
        String username = validateSign(attachedSign.getLines());

        if (username == null || username.isEmpty()) {
            return;
        }

        // username on sign must match player attempting to destroy
        if (!username.equals(e.getPlayer().getName())) {
            e.getPlayer().sendMessage("You can't destroy someone else's Spleef chest.");
            e.setCancelled(true);
            return;
        }

        removePlayerChest(e.getPlayer());
    }

    /**
     * Validate that a sign is valid for a Spleef chest.
     *
     * @return Player's username if exists; empty if none; null if invalid.
     */
    private String validateSign(String[] lines) {
        if (!(lines[0].equals("[Spleef]"))) {
            return null;
        }

        // attached sign must be otherwise blank except for the username of the creator
        if (!lines[1].isEmpty() || !lines[3].isEmpty()) {
            return null;
        }

        return lines[2];
    }

    /**
     * Check that b is 1) a sign, 2) attached to a chest.
     *
     * @param b
     * @return
     */
    private boolean signAttachedToChest(Block b) {
        if (!Tag.SIGNS.isTagged(b.getType())) {
            return false;
        }

        WallSign signData = (WallSign) b.getState().getBlockData();
        BlockFace facingBack = signData.getFacing().getOppositeFace();

        // needs to be attached to a chest
        Block attached = b.getRelative(facingBack);
        return attached.getType().equals(CHEST);
    }

    private void removePlayerChest(Player p) {
        if (SpleefTime.getInstance().getConfigData().getChests().containsKey(p.getName())) {
            SpleefTime.getInstance().getConfigData().getChests().remove(p.getName());
            SpleefTime.getInstance().saveConfigData();
            p.sendMessage("Your Spleef chest was destroyed!");
            p.sendMessage("You'll need to create a new one before participating in Spleef.");
        }
    }

//    @EventHandler
//    public void chestTouchEvent(PlayerInteractEvent e) {
//        Chest c = (Chest) e.getClickedBlock().getState();
//
//        System.out.println(c.getInventory().getSize());
//    }
}
