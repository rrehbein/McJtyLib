package mcjty.lib;

import mcjty.lib.gui.IKeyReceiver;
import mcjty.lib.gui.WindowManager;
import mcjty.lib.gui.widgets.Widget;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientEventHandler {

    public ClientEventHandler(){
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMouseDragged(GuiScreenEvent.MouseDragEvent.Pre event) {
        if (event.getGui() instanceof IKeyReceiver) {
            IKeyReceiver container = (IKeyReceiver) event.getGui();
            if (container.getWindow() != null) {
                WindowManager manager = container.getWindow().getWindowManager();
                if (manager != null) {
                    if (manager.getModalWindows().findFirst().isPresent()) {
                        // There is a modal window. Eat this event and send it directly to the window
                        manager.mouseDragged(event.getMouseX(), event.getMouseY(), event.getMouseButton());
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMouseScolled(GuiScreenEvent.MouseScrollEvent.Pre event) {
        if (event.getGui() instanceof IKeyReceiver) {
            IKeyReceiver container = (IKeyReceiver) event.getGui();
            if (container.getWindow() != null) {
                WindowManager manager = container.getWindow().getWindowManager();
                if (manager != null) {
                    if (manager.getModalWindows().findFirst().isPresent()) {
                        // There is a modal window. Eat this event and send it directly to the window
                        if (container.mouseScrolledFromEvent(event.getMouseX(), event.getMouseY(), event.getScrollDelta())) {
                            event.setCanceled(true);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMouseClicked(GuiScreenEvent.MouseClickedEvent event) {
        if (event.getGui() instanceof IKeyReceiver) {
            IKeyReceiver container = (IKeyReceiver) event.getGui();
            if (container.getWindow() != null) {
                WindowManager manager = container.getWindow().getWindowManager();
                if (manager != null) {
                    if (manager.getModalWindows().findFirst().isPresent()) {
                        // There is a modal window. Eat this event and send it directly to the window
                        if (container.mouseClickedFromEvent(event.getMouseX(), event.getMouseY(), event.getButton())) {
                            event.setCanceled(true);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMouseReleased(GuiScreenEvent.MouseReleasedEvent event) {
        if (event.getGui() instanceof IKeyReceiver) {
            IKeyReceiver container = (IKeyReceiver) event.getGui();
            if (container.getWindow() != null) {
                WindowManager manager = container.getWindow().getWindowManager();
                if (manager != null) {
                    if (manager.getModalWindows().findFirst().isPresent()) {
                        // There is a modal window. Eat this event and send it directly to the window
                        if (container.mouseReleasedFromEvent(event.getMouseX(), event.getMouseY(), event.getButton())) {
                            event.setCanceled(true);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onGuiInput(GuiScreenEvent.KeyboardCharTypedEvent event) {
        if (event.getGui() instanceof IKeyReceiver) {
            IKeyReceiver container = (IKeyReceiver) event.getGui();
            if (container.getWindow() != null) {
                Widget<?> focus;
                if (container.getWindow().getWindowManager() == null) {
                    focus = container.getWindow().getTextFocus();
                } else {
                    focus = container.getWindow().getWindowManager().getTextFocus();
                }
                if (focus != null) {
                    event.setCanceled(true);
                    container.charTypedFromEvent(event.getCodePoint());
                    // @todo 1.14 check
//                int c0 = event.getKeyCode();
//                if (Keyboard.getEventKey() == 0 && c0 >= 32 || Keyboard.getEventKeyState()) {
//                    container.keyTypedFromEvent(c0, Keyboard.getEventKey());
//                    Minecraft.getInstance().dispatchKeypresses();
//                }
                }
            }
        }

    }

    @SubscribeEvent
    public void onKeyboardInput(GuiScreenEvent.KeyboardKeyPressedEvent event) {
        if (event.getGui() instanceof IKeyReceiver) {
            IKeyReceiver container = (IKeyReceiver) event.getGui();
            if (container.getWindow() != null) {
                Widget<?> focus;
                if (container.getWindow().getWindowManager() == null) {
                    focus = container.getWindow().getTextFocus();
                } else {
                    focus = container.getWindow().getWindowManager().getTextFocus();
                }
                if (focus != null) {
                    event.setCanceled(true);
                    container.keyTypedFromEvent(event.getKeyCode(), event.getScanCode());
                    // @todo 1.14 check
//                int c0 = event.getKeyCode();
//                if (Keyboard.getEventKey() == 0 && c0 >= 32 || Keyboard.getEventKeyState()) {
//                    container.keyTypedFromEvent(c0, Keyboard.getEventKey());
//                    Minecraft.getInstance().dispatchKeypresses();
//                }
                }
            }
        }
    }

}
