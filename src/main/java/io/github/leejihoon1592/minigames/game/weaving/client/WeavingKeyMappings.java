package io.github.leejihoon1592.minigames.game.weaving.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/**
 * 천 배짜기 게임의 사용자 입력 설정.
 *
 * 기본값:
 * 파란 노트 = 마우스 좌클릭
 * 빨간 노트 = 마우스 우클릭
 *
 * 마인크래프트 키 지정 메뉴에서
 * 키보드 또는 다른 마우스 버튼으로 변경할 수 있다.
 *
 * 실제 노트 판정은 WeavingGame이 담당한다.
 */
public final class WeavingKeyMappings {

    private WeavingKeyMappings() {
    }

    public static final KeyMapping BLUE_INPUT =
            new KeyMapping(
                    "key.minigames.weaving_blue",
                    InputConstants.Type.MOUSE,
                    GLFW.GLFW_MOUSE_BUTTON_LEFT,
                    "key.categories.minigames"
            );

    public static final KeyMapping RED_INPUT =
            new KeyMapping(
                    "key.minigames.weaving_red",
                    InputConstants.Type.MOUSE,
                    GLFW.GLFW_MOUSE_BUTTON_RIGHT,
                    "key.categories.minigames"
            );
}