package mcjty.lib.network;

import mcjty.lib.McJtyLib;
import mcjty.lib.blockcommands.CommandInfo;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.Logging;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Packet to send back the list to the client. This requires
 * that the command is registered to McJtyLib.registerListCommandInfo
 */
public class PacketSendResultToClient {

    private final BlockPos pos;
    private final List list;
    private final String command;

    public PacketSendResultToClient(PacketBuffer buf) {
        pos = buf.readBlockPos();
        command = buf.readUtf(32767);
        CommandInfo<?> info = McJtyLib.getCommandInfo(command);
        if (info == null) {
            throw new IllegalStateException("Command '" + command + "' is not registered!");
        }
        Function<PacketBuffer, ?> deserializer = info.getDeserializer();
        int size = buf.readInt();
        if (size != -1) {
            list = new ArrayList<>(size);
            for (int i = 0 ; i < size ; i++) {
                list.add(deserializer.apply(buf));
            }
        } else {
            list = null;
        }
    }

    public PacketSendResultToClient(BlockPos pos, String command, List list) {
        this.pos = pos;
        this.command = command;
        this.list = new ArrayList<>(list);
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeUtf(command);
        CommandInfo<?> info = McJtyLib.getCommandInfo(command);
        if (info == null) {
            throw new IllegalStateException("Command '" + command + "' is not registered!");
        }
        BiConsumer<PacketBuffer, Object> serializer = (BiConsumer<PacketBuffer, Object>) info.getSerializer();
        if (serializer == null) {
            throw new IllegalStateException("Command '" + command + "' is not registered!");
        }
        if (list == null) {
            buf.writeInt(-1);
        } else {
            buf.writeInt(list.size());
            for (Object item : list) {
                serializer.accept(buf, item);
            }
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TileEntity te = McJtyLib.proxy.getClientWorld().getBlockEntity(pos);
            if (te instanceof GenericTileEntity) {
                ((GenericTileEntity) te).handleListFromServer(command, McJtyLib.proxy.getClientPlayer(), TypedMap.EMPTY, list);
            } else {
                Logging.logError("Can't handle command '" + command + "'!");
            }
        });
        ctx.setPacketHandled(true);
    }

}
