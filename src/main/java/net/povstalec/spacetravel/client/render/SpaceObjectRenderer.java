package net.povstalec.spacetravel.client.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.povstalec.spacetravel.common.util.SphericalCoords;
import net.povstalec.spacetravel.common.util.TextureLayer;

public class SpaceObjectRenderer
{
	//============================================================================================
	//**********************************Texture Layer Rendering***********************************
	//============================================================================================
	
	public static void renderTextureLayer(TextureLayer textureLayer, BufferBuilder bufferbuilder,
			Matrix4f lastMatrix, SphericalCoords sphericalCoords,
			long ticks, float brightness, double mulSize, double addRotation)
	{
		if(brightness <= 0.0F || textureLayer.rgba().alpha() <= 0)
			return;
		
		float size = textureLayer.mulSize(mulSize);
		
		if(size < textureLayer.minSize())
		{
			if(textureLayer.clampAtMinSize())
				size = (float) textureLayer.minSize();
			else
				return;
		}
		
		float rotation = textureLayer.rotation(addRotation);
		//System.out.println(texture + " " + size);
		
		Vector3f corner00 = SphericalCoords.placeOnSphere(-size, -size, sphericalCoords, rotation);
		Vector3f corner10 = SphericalCoords.placeOnSphere(size, -size, sphericalCoords, rotation);
		Vector3f corner11 = SphericalCoords.placeOnSphere(size, size, sphericalCoords, rotation);
		Vector3f corner01 = SphericalCoords.placeOnSphere(-size, size, sphericalCoords, rotation);
	
	
		if(textureLayer.shoulBlend())
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		
		RenderSystem.setShaderColor(textureLayer.rgba().red() / 255F, textureLayer.rgba().green() / 255F, textureLayer.rgba().blue() / 255F, brightness * textureLayer.rgba().alpha() / 255F);
		
		RenderSystem.setShaderTexture(0, textureLayer.texture());
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        
        bufferbuilder.vertex(lastMatrix, corner00.x, corner00.y, corner00.z).uv(textureLayer.uv().topRight().u(ticks), textureLayer.uv().topRight().v(ticks)).endVertex();
        bufferbuilder.vertex(lastMatrix, corner10.x, corner10.y, corner10.z).uv(textureLayer.uv().bottomRight().u(ticks), textureLayer.uv().bottomRight().v(ticks)).endVertex();
        bufferbuilder.vertex(lastMatrix, corner11.x, corner11.y, corner11.z).uv(textureLayer.uv().bottomLeft().u(ticks), textureLayer.uv().bottomLeft().v(ticks)).endVertex();
        bufferbuilder.vertex(lastMatrix, corner01.x, corner01.y, corner01.z).uv(textureLayer.uv().topLeft().u(ticks), textureLayer.uv().topLeft().v(ticks)).endVertex();
        
        BufferUploader.drawWithShader(bufferbuilder.end());
        
        RenderSystem.defaultBlendFunc();
	}
	
	public static void renderTextureLayer(TextureLayer textureLayer, BufferBuilder bufferbuilder, Matrix4f lastMatrix, SphericalCoords sphericalCoords, long ticks, float brightness)
	{
		renderTextureLayer(textureLayer, bufferbuilder, lastMatrix, sphericalCoords, ticks, brightness, 1, 0);
	}
}
