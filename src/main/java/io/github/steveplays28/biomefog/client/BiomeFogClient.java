package io.github.steveplays28.biomefog.client;

import com.google.common.collect.Lists;
import io.github.steveplays28.biomefog.command.ClientCommandRegistration;
import io.github.steveplays28.biomefog.config.BiomeFogConfigLoader;
import io.github.steveplays28.biomefog.objects.ColorAndBlurShader;
import io.github.steveplays28.biomefog.objects.Shader;
import io.github.steveplays28.biomefog.util.WorldUtil;
import ladysnake.satin.api.event.ShaderEffectRenderCallback;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private final ColorAndBlurShader shader = new ColorAndBlurShader().setWithColor(true).setWithBlur(true).setColor("#fffccc");

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

	public Map<String, Shader> shaders = new HashMap<>();
	public String selected_shader = null;

	private void shader(){
		shaders.put("desert_blur", new ColorAndBlurShader().setWithColor(true).setWithBlur(true).setColor("#fffccc"));
		shaders.put("desert", new ColorAndBlurShader().setWithColor(true).setWithBlur(false).setColor("#fffccc"));
		shaders.put("green_blur", new ColorAndBlurShader().setWithColor(true).setWithBlur(true).setColor("#d8f3dc"));
		shaders.put("green", new ColorAndBlurShader().setWithColor(true).setWithBlur(false).setColor("#d8f3dc"));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (shader_key.wasPressed()) {
				shader_enabled = !shader_enabled;
			}
		});

		ShaderEffectRenderCallback.EVENT.register(tickDelta -> {
			if (shader_enabled) {
				Shader selected = getSelectedShader();
				if(selected != null)
					selected.getShader().render(tickDelta);
			}
		});
	}

	public static BiomeFogClient getInstance(){
		return mod;
	}

	public Shader getSelectedShader(){
		return selected_shader == null ? null : shaders.get(selected_shader);
	}
}
