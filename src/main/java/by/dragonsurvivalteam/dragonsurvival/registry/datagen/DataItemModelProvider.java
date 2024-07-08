package by.dragonsurvivalteam.dragonsurvival.registry.datagen;

import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

public class DataItemModelProvider extends ItemModelProvider {
	public DataItemModelProvider(final PackOutput output, final String modId, final ExistingFileHelper existingFileHelper) {
		super(output, modId, existingFileHelper);
	}

	@Override
	protected void registerModels(){
		DSItems.DS_ITEMS.getEntries().forEach((holder) -> {
			if(!(holder.get() instanceof BlockItem)) {
				basicItem(holder.get());
			}
		});
	}

	@Override
	public @NotNull String getName() {
		return "Dragon Survival Item models";
	}
}