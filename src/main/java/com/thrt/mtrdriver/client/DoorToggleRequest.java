package com.thrt.mtrdriver.client;

public final class DoorToggleRequest {
	private static boolean pending;

	private DoorToggleRequest() {
	}

	public static void request() {
		pending = true;
	}

	public static void clear() {
		pending = false;
	}

	public static boolean consume() {
		final boolean value = pending;
		pending = false;
		return value;
	}
}
