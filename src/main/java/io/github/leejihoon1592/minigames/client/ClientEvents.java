package io.github.leejihoon1592.minigames.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.leejihoon1592.minigames.MiniGames;
import io.github.leejihoon1592.minigames.game.path.PathScreen;
import io.github.leejihoon1592.minigames.game.timing.TimingScreen;
import io.github.leejihoon1592.minigames.game.flight.FlightScreen;
import io.github.leejihoon1592.minigames.game.weaving.WeavingScreen;
import io.github.leejihoon1592.minigames.game.weaving.client.WeavingKeyMappings;
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

    /*
     * =========================
     * 테스트용 키
     * =========================
     */

    public static final KeyMapping OPEN_TIMING_GAME = new KeyMapping(
            "key.minigames.open_timing_game",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            "key.categories.minigames"
    );

    public static final KeyMapping OPEN_PATH_GAME = new KeyMapping(
            "key.minigames.open_path_game",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            "key.categories.minigames"
    );
    public static final KeyMapping OPEN_FLIGHT_GAME = new KeyMapping(
            "key.minigames.open_flight_game",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            "key.categories.minigames"
    );
    /*
     * V - 천 배짜기 리듬게임
     */
    public static final KeyMapping OPEN_WEAVING_GAME = new KeyMapping(
            "key.minigames.open_weaving_game",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "key.categories.minigames"
    );

    /*
     * =========================
     * 키 등록
     * =========================
     */

    @Mod.EventBusSubscriber(
            modid = MiniGames.MOD_ID,
            bus = Mod.EventBusSubscriber.Bus.MOD,
            value = Dist.CLIENT
    )
    public static final class ModEvents {

        private ModEvents() {
        }

        @SubscribeEvent
        public static void registerKeyMappings(
                RegisterKeyMappingsEvent event
        ) {
            event.register(OPEN_TIMING_GAME);
            event.register(OPEN_PATH_GAME);
            event.register(OPEN_FLIGHT_GAME);
            event.register(OPEN_WEAVING_GAME);

            /*
             * 천 배짜기 게임 조작키
             */
            event.register(WeavingKeyMappings.BLUE_INPUT);
            event.register(WeavingKeyMappings.RED_INPUT);
            event.register(WeavingKeyMappings.WHITE_INPUT);

        }
    }

    /*
     * =========================
     * 테스트용 게임 실행
     * =========================
     */

    @Mod.EventBusSubscriber(
            modid = MiniGames.MOD_ID,
            bus = Mod.EventBusSubscriber.Bus.FORGE,
            value = Dist.CLIENT
    )
    public static final class ForgeEvents {

        private ForgeEvents() {
        }

        @SubscribeEvent
        public static void onClientTick(
                TickEvent.ClientTickEvent event
        ) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }

            Minecraft minecraft = Minecraft.getInstance();

            if (minecraft.player == null) {
                return;
            }

            /*
             * Z
             * Timing Game
             */
            while (OPEN_TIMING_GAME.consumeClick()) {

                minecraft.setScreen(
                        new TimingScreen(
                                io.github.leejihoon1592.minigames.game.timing.TimingConfig.defaultConfig(),
                                result -> {

                                    if (minecraft.player == null) {
                                        return;
                                    }
                                }
                        )
                );
            }

            /*
             * X
             * Path Game
             */
            while (OPEN_PATH_GAME.consumeClick()) {

                minecraft.setScreen(
                        new PathScreen(
                                result -> {

                                    if (minecraft.player == null) {
                                        return;
                                    }
                                }
                        )
                );
            }
            /*
             * C
             * Flight Game
             */
            while (OPEN_FLIGHT_GAME.consumeClick()) {

                minecraft.setScreen(
                        new FlightScreen()
                );

            }
            while (OPEN_WEAVING_GAME.consumeClick()) {

                minecraft.setScreen(
                        new WeavingScreen()
                );
            }
        }
    }
}