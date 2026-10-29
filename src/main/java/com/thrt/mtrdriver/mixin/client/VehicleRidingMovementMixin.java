package com.thrt.mtrdriver.mixin.client;

import com.thrt.mtrdriver.client.DoorToggleRequest;
import com.thrt.mtrdriver.client.MTRDriverClient;
import org.mtr.mod.client.VehicleRidingMovement;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VehicleRidingMovement.class)
public class VehicleRidingMovementMixin {
	@Shadow(remap = false)
	private static int pressingDoorsTicks;
	@Shadow(remap = false)
	private static int pressingAtoTicks;
	@Shadow(remap = false)
	private static long ridingVehicleId;

	@Inject(method = "tick", at = @At(value = "FIELD", target = "Lorg/mtr/mod/client/VehicleRidingMovement;pressingDoorsTicks:I", opcode = Opcodes.PUTSTATIC, shift = At.Shift.AFTER), remap = false)
	private static void mtd$handleDoorKeys(CallbackInfo ci) {
		if (ridingVehicleId == 0) {
			DoorToggleRequest.clear();
			return;
		}

		if (MTRDriverClient.isAnyDoorKeyPressed()) {
			pressingDoorsTicks = 0;
		}
		if (DoorToggleRequest.consume()) {
			pressingDoorsTicks = 1;
		}
	}

	@Inject(method = "tick", at = @At(value = "FIELD", target = "Lorg/mtr/mod/client/VehicleRidingMovement;pressingAtoTicks:I", opcode = Opcodes.PUTSTATIC, shift = At.Shift.AFTER), remap = false)
	private static void mtd$suppressAtoToggle(CallbackInfo ci) {
		if (ridingVehicleId != 0 && MTRDriverClient.isAnyDoorKeyPressed()) {
			pressingAtoTicks = 0;
		}
	}
}
