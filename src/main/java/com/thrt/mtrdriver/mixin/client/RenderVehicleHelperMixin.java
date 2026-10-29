package com.thrt.mtrdriver.mixin.client;

import com.thrt.mtrdriver.client.MTDDoorState;
import com.thrt.mtrdriver.client.MTDRenderContext;
import org.mtr.mapping.holder.Box;
import org.mtr.mod.data.VehicleExtension;
import org.mtr.mod.render.PositionAndRotation;
import org.mtr.mod.render.RenderVehicleHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderVehicleHelper.class)
public class RenderVehicleHelperMixin {
	@Inject(method = "canOpenDoors", at = @At("HEAD"), cancellable = true, remap = false)
	private static void mtd$skipClosedSide(Box box, PositionAndRotation positionAndRotation, double doorValue, CallbackInfoReturnable<Boolean> cir) {
		final VehicleExtension vehicle = MTDRenderContext.get();
		if (vehicle == null) {
			return;
		}

		final MTDDoorState state = (MTDDoorState) (Object) vehicle.persistentVehicleData;
		if (state.mtd$isManual() && !mtd$isOpen(state, mtd$isLeftSide(box))) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "canOpenDoors", at = @At("RETURN"), cancellable = true, remap = false)
	private static void mtd$forceOpenSide(Box box, PositionAndRotation positionAndRotation, double doorValue, CallbackInfoReturnable<Boolean> cir) {
		final VehicleExtension vehicle = MTDRenderContext.get();
		if (vehicle == null) {
			return;
		}

		final MTDDoorState state = (MTDDoorState) (Object) vehicle.persistentVehicleData;
		if (state.mtd$isForceDoorways() || (state.mtd$isManual() && mtd$isOpen(state, mtd$isLeftSide(box)))) {
			cir.setReturnValue(true);
		}
	}

	private static boolean mtd$isLeftSide(Box box) {
		return (box.getMinXMapped() + box.getMaxXMapped()) * 0.5D < 0.0D;
	}

	private static boolean mtd$isOpen(MTDDoorState state, boolean leftSide) {
		return leftSide ? state.mtd$isLeftOpen() : state.mtd$isRightOpen();
	}
}
