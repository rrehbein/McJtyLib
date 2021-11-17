package mcjty.lib.blockcommands;

import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.TypedMap;
import net.minecraft.entity.player.PlayerEntity;

@FunctionalInterface
public interface ICommand<TE extends GenericTileEntity> {
    void run(TE te, PlayerEntity player, TypedMap params);
}
