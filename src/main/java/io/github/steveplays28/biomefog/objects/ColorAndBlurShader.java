package io.github.steveplays28.biomefog.objects;

import io.github.steveplays28.biomefog.client.BiomeFogClient;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import net.minecraft.util.Identifier;

import java.awt.*;

public class ColorAndBlurShader implements Shader{
    private boolean withColor = true;
    private boolean withBlur = false;
    private String color = "#ffffff";
    private final ManagedShaderEffect blurAndColor = ShaderEffectManager.getInstance().manage(new Identifier(BiomeFogClient.MOD_ID, "shaders/post/blit.json"));
    private final ManagedShaderEffect onlyColor = ShaderEffectManager.getInstance().manage(new Identifier(BiomeFogClient.MOD_ID, "shaders/post/color.json"));

    @Override
    public ManagedShaderEffect getShader() {
        if(!withColor && !withBlur)
            return null;

        if(withColor && withBlur){
            float[] colors = parseColor(color);
            float red = colors[0];
            float green = colors[1];
            float blue = colors[2];
            blurAndColor.setUniformValue("ColorModulate", red, green, blue, 1f);
            return blurAndColor;
        }


        if(withBlur && !withColor){
            blurAndColor.setUniformValue("ColorModulate", 1f, 1f, 1f, 1f);
            return blurAndColor;
        }

        float[] colors = parseColor(color);
        float red = colors[0];
        float green = colors[1];
        float blue = colors[2];

        onlyColor.setUniformValue("ColorModulate", red, green, blue, 1f);
        return onlyColor;
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

    public ColorAndBlurShader setWithColor(boolean withColor){
        this.withColor = withColor;
        return this;
    }

    public ColorAndBlurShader setWithBlur(boolean withBlur){
        this.withBlur = withBlur;
        return this;
    }

    public ColorAndBlurShader setColor(String color){
        this.color = color;
        return this;
    }
}
