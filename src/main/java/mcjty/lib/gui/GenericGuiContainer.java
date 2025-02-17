package mcjty.lib.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mcjty.lib.McJtyLib;
import mcjty.lib.bindings.Value;
import mcjty.lib.blockcommands.Command;
import mcjty.lib.client.GuiTools;
import mcjty.lib.client.RenderHelper;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.widgets.BlockRender;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.Widget;
import mcjty.lib.network.PacketSendServerCommand;
import mcjty.lib.network.PacketServerCommandTyped;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.Tools;
import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The main parent for all container based gui's in McJtyLib based mods
 */
public abstract class GenericGuiContainer<T extends GenericTileEntity, C extends Container> extends ContainerScreen<C> implements IKeyReceiver {

    protected Window window;
    private WindowManager windowManager;
    protected final T tileEntity;

    private GuiSideWindow sideWindow;

    public void setWindowDimensions(int x, int y) {
        this.imageWidth = x;
        this.imageHeight = y;
    }

    public GenericGuiContainer(T tileEntity, C container, PlayerInventory inventory, ManualEntry manualEntry) {
        super(container, inventory, new StringTextComponent("test"));   // @todo
        this.tileEntity = tileEntity;
        sideWindow = new GuiSideWindow(manualEntry.getManual(), manualEntry.getEntry(), manualEntry.getPage());
        windowManager = null;
    }

    // Mostly for JEI: get a list of all bounds additional to the main window. That includes modal windows, the side window, ...
    public List<Rectangle2d> getExtraWindowBounds() {
        if (sideWindow.getWindow() == null || sideWindow.getWindow().getToplevel() == null) {
            Logging.getLogger().error(new RuntimeException("Internal error! getExtraWindowBounds() called before initGui!"));
            return Collections.emptyList();
        }
        List<Rectangle2d> bounds = new ArrayList<>();
        Rectangle r1 = sideWindow.getWindow().getToplevel().getBounds();
        bounds.add(new Rectangle2d(r1.x, r1.y, r1.width, r1.height));
        if (windowManager != null) {
            for (Window w : windowManager.getWindows()) {
                Rectangle r = w.getToplevel().getBounds();
                bounds.add(new Rectangle2d(r.x, r.y, r.width, r.height));
            }
        }
        return bounds;
    }


    @Override
    public void init() {
        windowManager = null;
        super.init();
        sideWindow.initGui(minecraft, this, leftPos, topPos, imageWidth, imageHeight);
    }

    /**
     * Override this method to register your own windows to the window manager.
     */
    protected void registerWindows(WindowManager mgr) {
    }

    protected WindowManager getWindowManager() {
        if (windowManager == null) {
            if (sideWindow.getWindow() == null) {
                RuntimeException e = new RuntimeException("Internal error! getWindowManager() called before initGui!");
                Logging.getLogger().error(e); // Some mods (NEI) will swallow this exception without a trace unless we log it ourself.
                throw e;
            }
            windowManager = new WindowManager(this);
            windowManager.addWindow(sideWindow.getWindow());
            windowManager.addWindow(window);
            registerWindows(windowManager);
        }
        return windowManager;
    }

    @Override
    protected void renderLabels(@Nonnull MatrixStack matrixStack, int p_230451_2_, int p_230451_3_) {
        getWindowManager().drawTooltips(matrixStack);
    }

    public void drawHoveringText(MatrixStack matrixStack, List<String> textLines, List<ItemStack> items, int x, int y, FontRenderer font) {
        if (!textLines.isEmpty()) {
            matrixStack.pushPose();
            RenderSystem.disableRescaleNormal();
            net.minecraft.client.renderer.RenderHelper.turnOff();
            RenderSystem.disableLighting();
            RenderSystem.disableDepthTest();
            int i = 0;

            int linesWithItemStacks = 0;
            for (String s : textLines) {
                int j;
                if (s != null && items != null && s.contains("@") && !items.isEmpty()) {
                    List<Object> list = WindowTools.parseString(s, items);
                    boolean lineHasItemStacks = false;
                    j = 0;
                    for (Object o : list) {
                        if (o instanceof String) {
                            j += font.width((String) o);
                        } else {
                            j += 20;    // ItemStack
                            lineHasItemStacks = true;
                        }
                    }
                    if(lineHasItemStacks) {
                        ++linesWithItemStacks;
                    }

                } else {
                    j = font.width(s);
                }

                if (j > i) {
                    i = j;
                }
            }

            int xx = x + 12;
            int yy = y - 12;
            int k = 8;

            if (textLines.size() > 1) {
                k += 2 + (textLines.size() - 1) * 10 + linesWithItemStacks * 8;
            }

            if (xx > this.width - leftPos - i - 5) {
                xx -= 28 + i;
            }

            if (xx < 4 - leftPos) {
                xx = 4 - leftPos;
            }

            if (yy > this.height - topPos - k - 4) {
                yy = this.height - topPos - k - 4;
            } else if (yy < 4 - topPos) {
                yy = 4 - topPos;
            }

//            this.zLevel = 300.0F;     // @todo 1.14
            setBlitOffset(300);
            this.itemRenderer.blitOffset = 300.0F;
            int l = -267386864;
            this.fillGradient(matrixStack, xx - 3, yy - 4, xx + i + 3, yy - 3, l, l);
            this.fillGradient(matrixStack, xx - 3, yy + k + 3, xx + i + 3, yy + k + 4, l, l);
            this.fillGradient(matrixStack, xx - 3, yy - 3, xx + i + 3, yy + k + 3, l, l);
            this.fillGradient(matrixStack, xx - 4, yy - 3, xx - 3, yy + k + 3, l, l);
            this.fillGradient(matrixStack, xx + i + 3, yy - 3, xx + i + 4, yy + k + 3, l, l);
            int i1 = 1347420415;
            int j1 = (i1 & 16711422) >> 1 | i1 & -16777216;
            this.fillGradient(matrixStack, xx - 3, yy - 3 + 1, xx - 3 + 1, yy + k + 3 - 1, i1, j1);
            this.fillGradient(matrixStack, xx + i + 2, yy - 3 + 1, xx + i + 3, yy + k + 3 - 1, i1, j1);
            this.fillGradient(matrixStack, xx - 3, yy - 3, xx + i + 3, yy - 3 + 1, i1, i1);
            this.fillGradient(matrixStack, xx - 3, yy + k + 2, xx + i + 3, yy + k + 3, j1, j1);

            RenderSystem.translated(0.0D, 0.0D, this.itemRenderer.blitOffset);

            renderTextLines(matrixStack, textLines, items, font, xx, yy);

            setBlitOffset(0);
            this.itemRenderer.blitOffset = 0.0F;
            RenderSystem.enableLighting();
            RenderSystem.enableDepthTest();
            net.minecraft.client.renderer.RenderHelper.turnBackOn();
            RenderSystem.enableRescaleNormal();
            matrixStack.popPose();
        }
    }

    private void renderTextLines(MatrixStack matrixStack, List<String> textLines, List<ItemStack> items, FontRenderer font, int xx, int yy) {
        for (int i = 0; i < textLines.size(); ++i) {
            String s1 = textLines.get(i);
            if (s1 != null && items != null && s1.contains("@") && !items.isEmpty()) {
                List<Object> list = WindowTools.parseString(s1, items);
                int curx = xx;
                boolean lineHasItemStacks = false;
                for (Object o : list) {
                    if (o instanceof String) {
                        String s2 = (String)o;
                        font.drawShadow(matrixStack, s2, curx, yy, -1);
                        curx += font.width(s2);
                    } else {
                        RenderHelper.renderObject(matrixStack, curx + 1, yy, o, false);
                        curx += 20;
                        lineHasItemStacks = true;
                    }
                }
                if(lineHasItemStacks) {
                    yy += 8;
                }
            } else {
                font.drawShadow(matrixStack, s1, xx, yy, -1);
            }

            if (i == 0) {
                yy += 2;
            }

            yy += 10;
        }
    }

    @Override
    protected void renderBg(@Nonnull MatrixStack matrixStack, float partialTicks, int x, int y) {
        drawWindow(matrixStack);
    }

    protected void drawWindow(MatrixStack matrixStack) {
        if (window == null) {
            return;
        }
        renderBackground(matrixStack);
        getWindowManager().syncBindings(tileEntity);
        getWindowManager().draw(matrixStack);
    }

    @Override
    public void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (window == null) {
            return;
        }
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
        drawStackTooltips(matrixStack, mouseX, mouseY);
    }

    @Override
    public void renderSlot(@Nonnull MatrixStack matrixStack, @Nonnull Slot slot) {
        // Prevent slots from being rendered if they are (partially) covered by a modal window
        if (!isPartiallyCoveredByModalWindow(slot)) {
            super.renderSlot(matrixStack, slot);
        }
    }

    @Nullable
    @Override
    public Slot getSlotUnderMouse() {
        Slot slot = super.getSlotUnderMouse();
        if (slot == null) {
            return null;
        }
        if (isPartiallyCoveredByModalWindow(slot)) {
            return null;
        }
        return slot;
    }

    @Override
    public boolean isHovering(@Nonnull Slot slotIn, double mouseX, double mouseY) {
        if (isPartiallyCoveredByModalWindow(slotIn)) {
            return false;
        }
        return super.isHovering(slotIn, mouseX, mouseY);
    }

    private boolean isPartiallyCoveredByModalWindow(Slot slotIn) {
        int xPos = slotIn.x + window.getToplevel().getBounds().x;
        int yPos = slotIn.y + window.getToplevel().getBounds().y;

        return getWindowManager().getModalWindows()
                .anyMatch(window -> window.getToplevel().getBounds().intersects(new Rectangle(xPos, yPos, 18, 18)));
    }

    /**
     * Draw tooltips for itemstacks that are in BlockRender widgets
     */
    protected void drawStackTooltips(MatrixStack matrixStack, int mouseX, int mouseY) {
        int x = GuiTools.getRelativeX(window.getGui());
        int y = GuiTools.getRelativeY(window.getGui());
        Widget<?> widget = window.getToplevel().getWidgetAtPosition(x, y);
        if (widget instanceof BlockRender) {
            BlockRender blockRender = (BlockRender) widget;
            Object renderItem = blockRender.getRenderItem();
            ItemStack itemStack;
            if (renderItem instanceof ItemStack) {
                itemStack = (ItemStack) renderItem;
            } else if (renderItem instanceof Block) {
                itemStack = new ItemStack((Block) renderItem);
            } else if (renderItem instanceof Item) {
                itemStack = new ItemStack((Item) renderItem);
            } else {
                itemStack = ItemStack.EMPTY;
            }
            if (!itemStack.isEmpty()) {
                customRenderToolTip(matrixStack, blockRender, itemStack, mouseX, mouseY);
            }
        }
    }

    protected List<ITextComponent> addCustomLines(List<ITextComponent> oldList, BlockRender blockRender, ItemStack stack) {
        return oldList;
    }

    protected void customRenderToolTip(MatrixStack matrixStack, BlockRender blockRender, ItemStack stack, int x, int y) {
        List<ITextComponent> list;
        //noinspection ConstantConditions
        if (stack.getItem() == null) {
            // Protection for bad itemstacks
            list = new ArrayList<>();
        } else {
            ITooltipFlag flag = this.getMinecraft().options.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL;
            list = stack.getTooltipLines(this.getMinecraft().player, flag);
        }

        list = addCustomLines(list, blockRender, stack);

        FontRenderer font = null;
        stack.getItem();
        font = stack.getItem().getFontRenderer(stack);
        GuiUtils.preItemToolTip(stack);
        GuiUtils.drawHoveringText(matrixStack, list, x, y, imageWidth, imageHeight, -1, (font == null ? getMinecraft().font : font));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        super.removed();
        getMinecraft().keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        boolean rc = super.mouseClicked(x, y, button);
        if (window != null) {
            getWindowManager().mouseClicked(x, y, button);
        }
        return rc;
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double scaledX, double scaledY) {
        boolean rc = super.mouseDragged(x, y, button, scaledX, scaledY);
        if (window != null) {
            getWindowManager().mouseDragged(x, y, button);
        }
        return rc;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double amount) {
        boolean rc = super.mouseScrolled(x, y, amount);
        if (window != null) {
            getWindowManager().mouseScrolled(x, y, amount);
        }
        return false;
    }

    /*
     * 99% sure this is the correct one
     */
    @Override
    public boolean mouseReleased(double x, double y, int button) {
        boolean rc = super.mouseReleased(x, y, button);
        if (window != null) {
            getWindowManager().mouseReleased(x, y, button);
        }
        return rc;
    }

    @Override
    public Window getWindow() {
        return window;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean b = window == null || getWindowManager().keyTyped(keyCode, scanCode);
        if (b) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseClickedFromEvent(double x, double y, int button) {
        WindowManager manager = getWindow().getWindowManager();
        manager.mouseClicked(x, y, button);
        return true;
    }

    @Override
    public boolean mouseReleasedFromEvent(double x, double y, int button) {
        WindowManager manager = getWindow().getWindowManager();
        manager.mouseReleased(x, y, button);
        return true;
    }

    @Override
    public boolean mouseScrolledFromEvent(double x, double y, double amount) {
        WindowManager manager = getWindow().getWindowManager();
        manager.mouseScrolled(x, y, amount);
        return true;
    }

    @Override
    public void keyTypedFromEvent(int keyCode, int scanCode) {
        if (window != null) {
            if (getWindowManager().keyTyped(keyCode, scanCode)) {
                super.keyPressed(keyCode, scanCode, 0); // @todo 1.14: modifiers?
            }
        }
    }

    @Override
    public void charTypedFromEvent(char codePoint) {
        if (window != null) {
            if (getWindowManager().charTyped(codePoint)) {
                // @todo 1.14?
//                super.keyPressed(keyCode, scanCode, 0); // @todo 1.14: modifiers?
            }
        }
    }

    /**
     * Set a 'Value' and make sure it gets communicated to the server
     */
    public <T> void setValue(SimpleChannel channel, Value<?, T> value, T v) {
        sendServerCommandTyped(channel, tileEntity.getDimension(), GenericTileEntity.COMMAND_SYNC_BINDING.getName(),
                TypedMap.builder()
                        .put(value.getKey(), v)
                        .build());
    }

    public void sendServerCommandTyped(SimpleChannel network, String command, TypedMap params) {
        network.sendToServer(new PacketServerCommandTyped(tileEntity.getBlockPos(), tileEntity.getDimension(), command, params));
    }

    public void sendServerCommandTyped(SimpleChannel network, Command<?> command, TypedMap params) {
        network.sendToServer(new PacketServerCommandTyped(tileEntity.getBlockPos(), tileEntity.getDimension(), command.getName(), params));
    }

    public void sendServerCommandTyped(SimpleChannel network, RegistryKey<World> dimensionId, String command, TypedMap params) {
        network.sendToServer(new PacketServerCommandTyped(tileEntity.getBlockPos(), dimensionId, command, params));
    }

    public void sendServerCommand(SimpleChannel network, String modid, String command, @Nonnull TypedMap arguments) {
        network.sendToServer(new PacketSendServerCommand(modid, command, arguments));
    }

    public void sendServerCommand(SimpleChannel network, String modid, String command) {
        network.sendToServer(new PacketSendServerCommand(modid, command, TypedMap.EMPTY));
    }

    // Register a container/gui on the client side
    public static <C extends GenericContainer, S extends GenericGuiContainer<T,C>, T extends GenericTileEntity> void register(
            ContainerType<C> type,
            GuiSupplier<C, S, T> guiSupplier) {
        ScreenManager.IScreenFactory<C, S> factory = (container, inventory, title) -> {
            TileEntity te = McJtyLib.proxy.getClientWorld().getBlockEntity(container.getPos());
            return Tools.safeMap(te, (T tile) -> guiSupplier.create(tile, container, inventory), "Invalid tile entity!");
        };
        ScreenManager.register(type, factory);
    }

    @FunctionalInterface
    public static interface GuiSupplier<C extends GenericContainer, S extends GenericGuiContainer, T extends GenericTileEntity> {
        S create(T tile, C container, PlayerInventory inventory);
    }

    protected void updateEnergyBar(EnergyBar energyBar) {
        tileEntity.getCapability(CapabilityEnergy.ENERGY).ifPresent(e -> {
            energyBar.maxValue(((GenericEnergyStorage)e).getCapacity());
            energyBar.value(((GenericEnergyStorage)e).getEnergy());
        });
    }
}
