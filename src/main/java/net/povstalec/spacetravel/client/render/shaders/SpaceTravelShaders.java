package net.povstalec.spacetravel.client.render.shaders;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;

import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.povstalec.spacetravel.SpaceTravel;

public class SpaceTravelShaders
{
	@Nullable
    private static ShaderInstance rendertypeStarShater;
	
	@Mod.EventBusSubscriber(modid = SpaceTravel.MODID, value = Dist.CLIENT, bus= Mod.EventBusSubscriber.Bus.MOD)
    public static class ShaderInit
    {
        @SubscribeEvent
        public static void registerShaders(RegisterShadersEvent event) throws IOException
        {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation(SpaceTravel.MODID, "rendertype_star"), DefaultVertexFormat.NEW_ENTITY),
            		(shaderInstance) ->
            {
            	rendertypeStarShater = shaderInstance;
            });
        }
    }
	
	public static ShaderInstance starShader()
	{
		return rendertypeStarShater;
	}
}
