package com.thrt.mtrdriver.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.mtr.mod.KeyBindings;
import org.mtr.mod.client.MinecraftClientData;
import org.mtr.mod.client.VehicleRidingMovement;
import org.mtr.mod.data.VehicleExtension;
import org.mtr.mod.item.ItemDriverKey;

import java.util.UUID;

public class MTRDriverClient implements ClientModInitializer {
	private static final String CATEGORY = "key.categories.mtrdriver";
	private static KeyBinding leftDoorKey;
	private static KeyBinding rightDoorKey;
	private static boolean hasActiveStates;

	@Override
	public void onInitializeClient() {
		leftDoorKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.mtrdriver.left_door", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT, CATEGORY));
		rightDoorKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.mtrdriver.right_door", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT, CATEGORY));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			final boolean leftPressed = leftDoorKey != null && leftDoorKey.wasPressed();
			final boolean rightPressed = rightDoorKey != null && rightDoorKey.wasPressed();
			final boolean vanillaPressed = KeyBindings.TRAIN_TOGGLE_DOORS != null && KeyBindings.TRAIN_TOGGLE_DOORS.wasPressed();

			if (client.player == null || client.world == null) {
				return;
			}
			if (client.currentScreen != null) {
				cleanupDoorStates();
				return;
			}
			if (!leftPressed && !rightPressed && !vanillaPressed) {
				cleanupDoorStates();
				return;
			}

			final VehicleExtension drivenVehicle = findDrivenVehicle(client);

			if (vanillaPressed && !leftPressed && !rightPressed) {
				if (drivenVehicle != null) {
					((MTDDoorState) (Object) drivenVehicle.persistentVehicleData).mtd$reset();
				}
				cleanupDoorStates();
				return;
			}

			if (drivenVehicle == null) {
				client.player.sendMessage(Text.translatable("message.mtrdriver.not_driving"), true);
				cleanupDoorStates();
				return;
			}

			final ItemDriverKey key = VehicleRidingMovement.getValidHoldingKey(drivenVehicle.vehicleExtraData.getDepotId());
			if (key == null) {
				client.player.sendMessage(Text.translatable("message.mtrdriver.no_driver_key"), true);
				cleanupDoorStates();
				return;
			}
			if (!key.canOpenDoors) {
				client.player.sendMessage(Text.translatable("message.mtrdriver.no_door_control"), true);
				cleanupDoorStates();
				return;
			}
			if (Math.abs(drivenVehicle.getSpeed()) > 0.01D) {
				client.player.sendMessage(Text.translatable("message.mtrdriver.not_stopped"), true);
				cleanupDoorStates();
				return;
			}

			final MTDDoorState state = (MTDDoorState) (Object) drivenVehicle.persistentVehicleData;
			hasActiveStates = true;
			state.mtd$setManual(true);
			state.mtd$setForceDoorways(true);

			if (leftPressed) {
				state.mtd$setLeftOpen(!state.mtd$isLeftOpen());
			}
			if (rightPressed) {
				state.mtd$setRightOpen(!state.mtd$isRightOpen());
			}

			final boolean desiredGlobal = state.mtd$isLeftOpen() || state.mtd$isRightOpen();
			final boolean actualGlobal = drivenVehicle.vehicleExtraData.getDoorMultiplier() > 0;
			final boolean currentGlobal = state.mtd$hasExpectedGlobal() ? state.mtd$getExpectedGlobal() : actualGlobal;
			final double doorValue = drivenVehicle.persistentVehicleData.getDoorValue();

			if (!desiredGlobal) {
				state.mtd$setManual(false);
				if (!currentGlobal && doorValue <= 0.001D) {
					state.mtd$setForceDoorways(false);
				}
			}

			if (desiredGlobal != currentGlobal) {
				DoorToggleRequest.request();
				state.mtd$setExpectedGlobal(desiredGlobal, 40);
			}

			final String leftText = Text.translatable(state.mtd$isLeftOpen() ? "message.mtrdriver.open" : "message.mtrdriver.closed").getString();
			final String rightText = Text.translatable(state.mtd$isRightOpen() ? "message.mtrdriver.open" : "message.mtrdriver.closed").getString();
			client.player.sendMessage(Text.translatable("message.mtrdriver.status", leftText, rightText), true);

			cleanupDoorStates();
		});
	}

	public static boolean isAnyDoorKeyPressed() {
		return (leftDoorKey != null && leftDoorKey.isPressed()) || (rightDoorKey != null && rightDoorKey.isPressed());
	}

	private static VehicleExtension findDrivenVehicle(MinecraftClient client) {
		if (client.player == null) {
			return null;
		}

		final UUID playerUuid = client.player.getUuid();
		for (final VehicleExtension vehicle : MinecraftClientData.getInstance().vehicles) {
			final boolean[] driven = {false};
			vehicle.vehicleExtraData.iterateRidingEntities(entity -> {
				if (entity.isDriver() && entity.uuid.equals(playerUuid)) {
					driven[0] = true;
				}
			});
			if (driven[0]) {
				return vehicle;
			}
		}
		return null;
	}

	private static void cleanupDoorStates() {
		if (!hasActiveStates) {
			return;
		}

		boolean anyActive = false;
		for (final VehicleExtension vehicle : MinecraftClientData.getInstance().vehicles) {
			final MTDDoorState state = (MTDDoorState) (Object) vehicle.persistentVehicleData;
			if (!state.mtd$isManual() && !state.mtd$isForceDoorways()) {
				continue;
			}

			final boolean targetOpen = vehicle.vehicleExtraData.getDoorMultiplier() > 0;
			final double doorValue = vehicle.persistentVehicleData.getDoorValue();
			state.mtd$tickExpectedGlobal(targetOpen);
			boolean activeAfter = true;
			if (state.mtd$isManual()) {
				final boolean desiredGlobal = state.mtd$isLeftOpen() || state.mtd$isRightOpen();
				if (!targetOpen && doorValue <= 0.001D && (!desiredGlobal || !state.mtd$hasExpectedGlobal())) {
					state.mtd$reset();
					activeAfter = false;
				}
			} else if (state.mtd$isForceDoorways()) {
				if ((!targetOpen && doorValue <= 0.001D) || (targetOpen && doorValue >= 0.999D)) {
					state.mtd$setForceDoorways(false);
					activeAfter = false;
				}
			}
			anyActive = anyActive || activeAfter;
		}
		hasActiveStates = anyActive;
	}
}
