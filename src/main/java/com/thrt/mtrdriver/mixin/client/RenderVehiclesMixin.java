package com.thrt.mtrdriver.mixin.client;

import com.thrt.mtrdriver.client.MTDRenderContext;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.mtr.mapping.holder.Box;
import org.mtr.mapping.holder.Vector3d;
import org.mtr.mod.data.VehicleExtension;
import org.mtr.mod.render.PositionAndRotation;
import org.mtr.mod.render.RenderVehicles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderVehicles.class)
public class RenderVehiclesMixin {
	@Inject(method = "lambda$render$9", at = @At("HEAD"), remap = false)
	private static void mtd$captureCurrentVehicle(ObjectArrayList list, Vector3d playerPosition, PositionAndRotation positionAndRotation, VehicleExtension vehicle, ObjectArrayList openDoorways, Box box, CallbackInfo ci) {
		MTDRenderContext.set(vehicle);
	}
}
