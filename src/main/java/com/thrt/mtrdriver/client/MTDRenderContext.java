package com.thrt.mtrdriver.client;

import org.mtr.mod.data.VehicleExtension;

public final class MTDRenderContext {
	private static VehicleExtension currentVehicle;

	private MTDRenderContext() {
	}

	public static void set(VehicleExtension vehicle) {
		currentVehicle = vehicle;
	}

	public static VehicleExtension get() {
		return currentVehicle;
	}
}
