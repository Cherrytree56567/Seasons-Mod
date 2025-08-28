package com.seasons.Winter;

import net.minecraft.util.math.MathHelper;

public class FogState {
    private static float fogEnable = 0f;
    
    public static float getFogEnable() { 
		return fogEnable; 
	}
	
    public static void setFogEnable(float value) { 
		fogEnable = MathHelper.clamp(value, 0f, 1f); 
	}
}
