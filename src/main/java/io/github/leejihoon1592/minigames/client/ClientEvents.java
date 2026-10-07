package io.github.leejihoon1592.minigames.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.leejihoon1592.minigames.MiniGames;
import io.github.leejihoon1592.minigames.client.screen.TimingScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

public final class ClientEvents {

    private ClientEvents() {
    }

    public static final KeyMapping OPEN_TIMING_GAME = new KeyMapping(
            "key.minigames.open_timing_game",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            "key.categories.minigames"
    );

    @Mod.EventBusSubscriber(
            modid = MiniGames.MOD_ID,
            bus = Mod.EventBusSubscriber.Bus.MOD,
            value = Dist.CLIENT
    )
    public static final class ModEvents {

        private ModEvents() {
        }

        @SubscribeEvent
        public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(OPEN_TIMING_GAME);
        }
    }

    @Mod.EventBusSubscriber(
            modid = MiniGames.MOD_ID,
            bus = Mod.EventBusSubscriber.Bus.FORGE,
            value = Dist.CLIENT
    )
    public static final class ForgeEvents {

        private ForgeEvents() {
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }

            Minecraft minecraft = Minecraft.getInstance();

            if (minecraft.player == null) {
                return;
            }

            while (OPEN_TIMING_GAME.consumeClick()) {
                minecraft.setScreen(new TimingScreen());
            }
        }
    }
}