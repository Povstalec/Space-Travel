package net.povstalec.spacetravel.client.render.shaders;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.povstalec.spacetravel.SpaceTravel;

public class SpaceTravelShaders
{
	@Nullable
    private static StarShaderInstance rendertypeStarShater;
	private static StarShaderInstance rendertypeStarTexShader;
	
	@Mod.EventBusSubscriber(modid = SpaceTravel.MODID, value = Dist.CLIENT, bus= Mod.EventBusSubscriber.Bus.MOD)
    public static class ShaderInit
    {
        @SubscribeEvent
        public static void registerShaders(RegisterShadersEvent event) throws IOException
        {
            event.registerShader(new StarShaderInstance(event.getResourceProvider(), new ResourceLocation(SpaceTravel.MODID, "rendertype_star"), SpaceTravelVertexFormat.STAR_POS_COLOR_LY),
            		(shaderInstance) ->
            		{
            			rendertypeStarShater = (StarShaderInstance) shaderInstance;
            		});
			
			event.registerShader(new StarShaderInstance(event.getResourceProvider(), new ResourceLocation(SpaceTravel.MODID,"rendertype_star_tex"), SpaceTravelVertexFormat.STAR_POS_COLOR_LY_TEX),
					(shaderInstance) ->
					{
						rendertypeStarTexShader = (StarShaderInstance) shaderInstance;
					});
        }
    }
	
	public static StarShaderInstance starShader()
	{
		return rendertypeStarShater;
	}
	
	public static StarShaderInstance starTexShader()
	{
		return rendertypeStarTexShader;
	}
}
