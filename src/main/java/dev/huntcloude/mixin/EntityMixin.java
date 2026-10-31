package dev.huntcloude.mixin;

import dev.huntcloude.State;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    private void huntcloude$noclip(MoverType type, Vec3 delta, CallbackInfo ci) {
        if (!State.noclip) return;
        Minecraft mc = Minecraft.getInstance();
        Entity self = (Entity) (Object) this;
        if (mc.player != self) return;

        double speed = mc.options.keySprint.isDown() ? 1.2 : 0.5;
        double f = (mc.options.keyUp.isDown() ? 1 : 0) - (mc.options.keyDown.isDown() ? 1 : 0);
        double s = (mc.options.keyRight.isDown() ? 1 : 0) - (mc.options.keyLeft.isDown() ? 1 : 0);
        double v = (mc.options.keyJump.isDown() ? 1 : 0) - (mc.options.keyShift.isDown() ? 1 : 0);

        Vec3 look = self.getLookAngle();
        double hl = Math.sqrt(look.x * look.x + look.z * look.z);
        double rx = hl < 1.0E-4 ? 0 : -look.z / hl;
        double rz = hl < 1.0E-4 ? 0 : look.x / hl;

        double dx = (look.x * f + rx * s) * speed;
        double dy = (look.y * f + v) * speed;
        double dz = (look.z * f + rz * s) * speed;

        self.setPos(self.getX() + dx, self.getY() + dy, self.getZ() + dz);
        self.setDeltaMovement(Vec3.ZERO);
        self.resetFallDistance();
        ci.cancel();
    }
}
