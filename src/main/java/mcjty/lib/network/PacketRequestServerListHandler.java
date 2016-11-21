package mcjty.lib.network;

import mcjty.lib.varia.Logging;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

/**
 * Handler for PacketRequestServerList
 *
 * @param <M> is the type of the PacketRequestServerList instance
 * @param <T> is the type of the items in the list that is requested from the server
 */
public abstract class PacketRequestServerListHandler<M extends PacketRequestServerList, T> implements IMessageHandler<M, IMessage> {

    public PacketRequestServerListHandler() {
    }

    @Override
    public IMessage onMessage(M message, MessageContext ctx) {
        FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
        return null;
    }

    private void handle(M message, MessageContext ctx) {
        TileEntity te = ctx.getServerHandler().playerEntity.getEntityWorld().getTileEntity(message.pos);
        if(!(te instanceof CommandHandler)) {
            Logging.log("TileEntity is not a CommandHandler!");
            return;
        }
        CommandHandler commandHandler = (CommandHandler) te;
        List<T> list = (List<T>) commandHandler.executeWithResultList(message.command, message.args);
        if (list == null) {
            Logging.log("Command " + message.command + " was not handled!");
            return;
        }
        sendToClient(message.pos, list, ctx);
    }

    protected abstract void sendToClient(BlockPos pos, List<T> list, MessageContext ctx);
}
