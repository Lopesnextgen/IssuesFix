package davi.lopes.issuesfix.nametag;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;

public final class NameTagVisibility {
    private static final double MAX_DISTANCE_SQUARED = 4096.0;

    private NameTagVisibility() {
    }

    public static boolean canRepair(Entity entity, double distanceToCameraSq) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer viewer = client.player;
        if (viewer == null) {
            return false;
        }

        if (entity == viewer) {
            return true;
        }

        if (entity.isSpectator() || entity.isInvisibleTo(viewer)) {
            return false;
        }

        return distanceToCameraSq <= MAX_DISTANCE_SQUARED;
    }
}
