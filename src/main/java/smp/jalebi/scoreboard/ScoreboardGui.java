package smp.jalebi.scoreboard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreboardGui implements Listener {

    private static final Map<Integer, Line> SLOTS = new HashMap<>();
    static {
        SLOTS.put(4, Line.SCOREBOARD);
        SLOTS.put(11, Line.MONEY);
        SLOTS.put(12, Line.KILLS);
        SLOTS.put(13, Line.DEATHS);
        SLOTS.put(14, Line.PLAYTIME);
        SLOTS.put(15, Line.SHARDS);
    }

    private static class Holder implements InventoryHolder {
        private Inventory inv;
        @Override public Inventory getInventory() { return inv; }
    }

    private final JalebiScoreboard plugin;

    public ScoreboardGui(JalebiScoreboard plugin) {
        this.plugin = plugin;
    }

    public void open(Player p) {
        Holder h = new Holder();
        Inventory inv = Bukkit.createInventory(h, 27,
                Component.text(SmallCaps.of("Scoreboard Settings"), TextColor.fromHexString("#FFA500"))
                        .decorate(TextDecoration.BOLD));
        h.inv = inv;
        render(p, inv);
        p.openInventory(inv);
    }

    private void render(Player p, Inventory inv) {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fm = filler.getItemMeta();
        fm.displayName(Component.space());
        filler.setItemMeta(fm);
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, filler);

        for (Map.Entry<Integer, Line> e : SLOTS.entrySet()) {
            Line l = e.getValue();
            boolean on = plugin.data().isEnabled(p.getUniqueId(), l);
            ItemStack item = new ItemStack(l.icon);
            ItemMeta m = item.getItemMeta();
            m.displayName(Component.text(l.symbol + " " + SmallCaps.of(l.label), TextColor.fromHexString(l.hex))
                    .decoration(TextDecoration.ITALIC, false).decorate(TextDecoration.BOLD));
            m.lore(List.of(
                    Component.empty(),
                    Component.text(SmallCaps.of("Status: "), NamedTextColor.GRAY)
                            .append(Component.text(on ? SmallCaps.of("ON") : SmallCaps.of("OFF"),
                                    on ? NamedTextColor.GREEN : NamedTextColor.RED).decorate(TextDecoration.BOLD))
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text(SmallCaps.of("Click to toggle"), NamedTextColor.DARK_GRAY)
                            .decoration(TextDecoration.ITALIC, false)));
            m.setEnchantmentGlintOverride(on);
            item.setItemMeta(m);
            inv.setItem(e.getKey(), item);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getView().getTopInventory().getHolder() instanceof Holder)) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (e.getClickedInventory() != e.getView().getTopInventory()) return;

        Line l = SLOTS.get(e.getSlot());
        if (l == null) return;

        boolean now = !plugin.data().isEnabled(p.getUniqueId(), l);
        plugin.data().setEnabled(p.getUniqueId(), l, now);
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, now ? 1.4f : 0.8f);

        render(p, e.getView().getTopInventory());
        plugin.boards().update(p);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getView().getTopInventory().getHolder() instanceof Holder) e.setCancelled(true);
    }
}
