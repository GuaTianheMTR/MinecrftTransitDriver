package com.thrt.mtrdriver.client;

public interface MTDDoorState {
	boolean mtd$isManual();

	void mtd$setManual(boolean value);

	boolean mtd$isLeftOpen();

	void mtd$setLeftOpen(boolean value);

	boolean mtd$isRightOpen();

	void mtd$setRightOpen(boolean value);

	boolean mtd$isForceDoorways();

	void mtd$setForceDoorways(boolean value);

	boolean mtd$hasExpectedGlobal();

	boolean mtd$getExpectedGlobal();

	void mtd$setExpectedGlobal(boolean value, int ticks);

	void mtd$tickExpectedGlobal(boolean actual);

	void mtd$reset();
}
