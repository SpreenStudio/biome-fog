package io.github.steveplays28.biomefog.client;

import io.github.steveplays28.biomefog.command.ClientCommandRegistration;
import io.github.steveplays28.biomefog.config.BiomeFogConfigLoader;
import io.github.steveplays28.biomefog.util.WorldUtil;
import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import ladysnake.satin.api.managed.uniform.Uniform4f;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.nio.file.Path;

@Environment(EnvType.CLIENT)
public class BiomeFogClient implements ClientModInitializer {
	private static BiomeFogClient mod;
	public static final String MOD_ID = "biome-fog";
	public static final String MOD_PATH = "biome_fog";
	public static final String MOD_COMMAND_ID = "biomefog";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	public static final Path MOD_LOADER_CONFIG_FOLDER_PATH = FabricLoader.getInstance().getConfigDir();

	public static String currentBiome = "";
	public static String currentDimension = "";

	private final KeyBinding shader_key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"cum.test",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_H,
			"category.cum"
	));

	private boolean shader_enabled = false;

	@Override
	public void onInitializeClient() {
		mod = this;
		BiomeFogConfigLoader.load();

		// Listen for when the server is reloading (i.e. /reload), and reload the config
		ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((s, m) -> {
			LOGGER.info("[Biome Fog] Reloading config!");
			BiomeFogConfigLoader.load();
		});

		// Register commands
		ClientCommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess) -> ClientCommandRegistration.registerCommands(dispatcher));


		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			if (WorldUtil.isWorldBlacklisted(WorldUtil.getWorldOrServerName())) {
				BiomeFogClient.LOGGER.info("Current world/server is blacklisted, disabling Biome Fog.");
			}
		});

		shader();
	}

	private final ManagedShaderEffect shader = ShaderEffectManager.getInstance().manage(new Identifier(MOD_ID, "shaders/post/blit.json"));
	private final Uniform4f color = shader.findUniform4f("ColorModulate");

	private void shader(){
		color.set(1f, 1f, 0.4f, 1.0f);
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (shader_key.wasPressed()) {
				shader_enabled = !shader_enabled;
			}
		});

		ShaderEffectRenderCallback.EVENT.register(tickDelta -> {
			if (shader_enabled) {

				shader.render(tickDelta);
			}
		});
	}

	public void setShaderColor(String color){
		float[] colors = parseColor(color);
		float red = colors[0];
		float green = colors[1];
		float blue = colors[2];
		this.color.set(red, green, blue, 1.0f);
	}

	public float[] parseColor(String color){
		Color c = Color.decode(color);

		int preRed = c.getRed();
		int preGreen = c.getGreen();
		int preBlue = c.getBlue();

		float red = preRed / 255.0f;
		float green = preGreen / 255.0f;
		float blue = preBlue / 255.0f;

		return new float[]{red, green, blue};
	}

	public static BiomeFogClient getInstance(){
		return mod;
	}
}
