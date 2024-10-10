package net.povstalec.spacetravel.client.render.space_objects;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.povstalec.spacetravel.client.RenderCenter;
import net.povstalec.spacetravel.common.space.objects.SpaceObject;
import net.povstalec.spacetravel.common.space.objects.Star;
import net.povstalec.spacetravel.common.util.Color;
import net.povstalec.spacetravel.common.util.SpaceCoords;
import net.povstalec.spacetravel.common.util.SphericalCoords;
import net.povstalec.spacetravel.common.util.TextureLayer;
import org.joml.Matrix4f;

public class StarRenderer extends TexturedObjectRenderer<Star>
{
	public StarRenderer(Star star)
	{
		super(star);
	}
	
	//============================================================================================
	//*****************************************Rendering******************************************
	//============================================================================================
	
	/*TODO @Override
	protected void renderTextureLayer(TextureLayer textureLayer, RenderCenter viewCenter, ClientLevel level, Camera camera, BufferBuilder bufferbuilder, Matrix4f lastMatrix, SphericalCoords sphericalCoords, long ticks, double distance, float partialTicks)
	{
		double lyDistance = distance / SpaceCoords.LY_TO_KM;
		
		Color.FloatRGBA starRGBA = spaceObject.supernovaRGBA(ticks, lyDistance);
		
		if(starRGBA.alpha() <= 0.0F || textureLayer.rgba().alpha() <= 0)
			return;
		
		float size = (float) textureLayer.mulSize(SpaceObject.distanceSize(distance));
		
		if(size < textureLayer.minSize())
		{
			if(textureLayer.clampAtMinSize())
			{
				size = (float) textureLayer.minSize();
				
				// Once the star has reached its usual min size, it will start getting smaller slowly again, but only up to a certain point
				size = starSize(size, lyDistance);
			}
			else
				return;
		}
		
		if(isSupernova())
			size = supernovaSize(size, ticks, lyDistance);
		
		renderOnSphere(textureLayer.rgba(), starRGBA, textureLayer.texture(), textureLayer.uv(),
				level, camera, bufferbuilder, lastMatrix, sphericalCoords,
				ticks, distance, partialTicks, dayBrightness(viewCenter, size, ticks, level, camera, partialTicks), size, (float) textureLayer.rotation(), textureLayer.shoulBlend());
	}
	
	@Override
	protected void renderTextureLayers(ViewCenter viewCenter, ClientLevel level, Camera camera, BufferBuilder bufferbuilder, Matrix4f lastMatrix, SphericalCoords sphericalCoords, long ticks, double distance, float partialTicks)
	{
		if(isSupernova() && supernovaInfo.supernovaEnded(ticks))
			return;
		
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		
		for(TextureLayer textureLayer : textureLayers)
		{
			renderTextureLayer(textureLayer, viewCenter, level, camera, bufferbuilder, lastMatrix, sphericalCoords, ticks, distance, partialTicks);
		}
	}*/
}
