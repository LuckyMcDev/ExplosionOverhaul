package de.luckydev.explosionoverhaul;

import de.luckydev.explosionoverhaul.shake.ScreenShakeHandler;
import dev.architectury.event.events.client.ClientTickEvent;

public class ExplosionOverhaulClient {

    public static void init()  {

        ClientTickEvent.CLIENT_POST.register(client -> {
            if (!client.isPaused()) {
                ScreenShakeHandler.tick();
            }
        });
    }
}
