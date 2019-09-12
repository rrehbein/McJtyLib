package mcjty.lib.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

/**
 * Use this in case you want a container with no slots (for example, for energy storage only).
 */
public class EmptyContainer extends GenericContainer {

    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory() {
        @Override
        protected void setup() {
        }
    };


    public EmptyContainer(PlayerEntity player, IInventory inventory) {
        super(null, 0, EmptyContainerFactory.getInstance(), BlockPos.ZERO, null);
    }

    public EmptyContainer(PlayerEntity player) {
        this(player, null);
    }

    @Override
    public void putStackInSlot(int index, ItemStack stack) {
    }
}
