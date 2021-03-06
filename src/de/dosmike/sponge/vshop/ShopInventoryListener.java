package de.dosmike.sponge.vshop;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

public class ShopInventoryListener implements Consumer<ClickInventoryEvent>{
	NPCguard npc;
	public ShopInventoryListener(NPCguard npc) {
		this.npc=npc;
	}
	
	@Override
	public void accept(ClickInventoryEvent event) {
		Optional<Player> clicker = event.getCause().first(Player.class);
		if (!clicker.isPresent()) return;
		if (!VillagerShops.openShops.containsKey(clicker.get().getUniqueId())) return;
		
		if (VillagerShops.actionUnstack.contains(clicker.get().getUniqueId())) {
			event.getTransactions().forEach(action -> { action.setValid(false); });
			event.getCursorTransaction().setCustom(ItemStackSnapshot.NONE);
			event.getCursorTransaction().setValid(false);
			event.setCancelled(true);
			return;
		}
		
		int slotIndex=-1;
		
		//thanks for this great API so far *eyes rolling*
		
		//Inventory target = event.getTargetInventory().query(OrderedInventory.class); //not working
		
		//items we added hold a slotum value, so we can simply check for that and retrieve the correct slot
		//props to codeHusky for figuring the slotnum workaround...
		Optional<Object> unsafeslot = event.getCursorTransaction().getDefault().toContainer().get(DataQuery.of("UnsafeData", "vShopSlotNum"));
		if (unsafeslot.isPresent()) {
			slotIndex = (int)unsafeslot.get();
		}
		
		//clear cursor
		event.getCursorTransaction().setValid(false);
		event.getCursorTransaction().setCustom(ItemStackSnapshot.NONE);
		event.getTransactions().forEach(action -> { action.setValid(false); });
		
		if (slotIndex >= 0) { 
			InteractionHandler.clickInventory(clicker.get(), slotIndex);
			Sponge.getScheduler().createSyncExecutor(VillagerShops.getInstance())
				.schedule(() -> {
					VillagerShops.actionUnstack.remove(clicker.get().getUniqueId());
				}, 50, TimeUnit.MILLISECONDS);
		}
		event.setCancelled(true);
	}
	
	/*void drizzleInventory(Inventory i, String p) {
		VillagerShops.l("%s%s", p, i.getClass().getSimpleName());
		for (Inventory ii : i) drizzleInventory(ii, p+"+");
	}*/
}
