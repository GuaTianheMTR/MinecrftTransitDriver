package com.thrt.mtrdriver.mixin.client;

import com.thrt.mtrdriver.client.MTDDoorState;
import org.mtr.mod.data.PersistentVehicleData;
import org.mtr.mod.resource.DoorAnimationType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PersistentVehicleData.class)
public abstract class PersistentVehicleDataMixin implements MTDDoorState {
	@Unique
	private boolean mtd$manual;
	@Unique
	private boolean mtd$leftOpen;
	@Unique
	private boolean mtd$rightOpen;
	@Unique
	private boolean mtd$forceDoorways;
	@Unique
	private boolean mtd$hasExpectedGlobal;
	@Unique
	private boolean mtd$expectedGlobal;
	@Unique
	private int mtd$expectedGlobalTicks;

	@Override
	public boolean mtd$isManual() {
		return mtd$manual;
	}

	@Override
	public void mtd$setManual(boolean value) {
		mtd$manual = value;
	}

	@Override
	public boolean mtd$isLeftOpen() {
		return mtd$leftOpen;
	}

	@Override
	public void mtd$setLeftOpen(boolean value) {
		mtd$leftOpen = value;
	}

	@Override
	public boolean mtd$isRightOpen() {
		return mtd$rightOpen;
	}

	@Override
	public void mtd$setRightOpen(boolean value) {
		mtd$rightOpen = value;
	}

	@Override
	public boolean mtd$isForceDoorways() {
		return mtd$forceDoorways;
	}

	@Override
	public void mtd$setForceDoorways(boolean value) {
		mtd$forceDoorways = value;
	}

	@Override
	public boolean mtd$hasExpectedGlobal() {
		return mtd$hasExpectedGlobal;
	}

	@Override
	public boolean mtd$getExpectedGlobal() {
		return mtd$expectedGlobal;
	}

	@Override
	public void mtd$setExpectedGlobal(boolean value, int ticks) {
		mtd$hasExpectedGlobal = true;
		mtd$expectedGlobal = value;
		mtd$expectedGlobalTicks = ticks;
	}

	@Override
	public void mtd$tickExpectedGlobal(boolean actual) {
		if (mtd$hasExpectedGlobal) {
			if (actual == mtd$expectedGlobal) {
				mtd$hasExpectedGlobal = false;
			} else if (--mtd$expectedGlobalTicks <= 0) {
				mtd$hasExpectedGlobal = false;
			}
		}
	}

	@Override
	public void mtd$reset() {
		mtd$manual = false;
		mtd$leftOpen = false;
		mtd$rightOpen = false;
		mtd$forceDoorways = false;
		mtd$hasExpectedGlobal = false;
		mtd$expectedGlobal = false;
		mtd$expectedGlobalTicks = 0;
	}

	@Inject(method = "getInterpolatedDoorValue", at = @At("HEAD"), cancellable = true, remap = false)
	private void mtd$gateDoorSide(DoorAnimationType doorAnimationType, double doorZMultiplier, boolean flipped, double doorValue, boolean opening, CallbackInfoReturnable<Float> cir) {
		if (mtd$manual) {
			final boolean leftSide = flipped;
			if (leftSide ? !mtd$leftOpen : !mtd$rightOpen) {
				cir.setReturnValue(0.0F);
			}
		}
	}
}
