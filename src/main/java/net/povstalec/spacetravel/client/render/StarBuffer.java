package net.povstalec.spacetravel.client.render;

import java.nio.ByteBuffer;

import javax.annotation.Nullable;

import com.mojang.blaze3d.platform.MemoryTracker;
import net.minecraft.util.Mth;
import net.povstalec.spacetravel.common.util.SpaceCoords;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.*;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.povstalec.spacetravel.client.render.shaders.StarShaderInstance;

public class StarBuffer implements AutoCloseable
{
	private int vertexBufferId;
	private int indexBufferId;
	private int arrayObjectId;
	// Instanced
	//TODO private int instanceBufferId;
	@Nullable
	private VertexFormat format;
	@Nullable
	private RenderSystem.AutoStorageIndexBuffer sequentialIndices;
	private VertexFormat.IndexType indexType;
	private int indexCount;
	private VertexFormat.Mode mode;
	
	public StarBuffer()
	{
		RenderSystem.assertOnRenderThread();
		this.vertexBufferId = GlStateManager._glGenBuffers();
		this.indexBufferId = GlStateManager._glGenBuffers();
		this.arrayObjectId = GlStateManager._glGenVertexArrays();
		// Instanced
		//TODO this.instanceBufferId = GlStateManager._glGenBuffers();
	}
	
	// Instanced
	public void setupBufferState()
	{
		//TODO Describe what this is doing
		//GL33C.glEnableVertexAttribArray(1);//TODO
		
		//glVertexAttribPointer(1, 4, GL_FLOAT, false, this.lightSize, 0); //TODO
		
		//glVertexAttribDivisor(1, 1); //TODO
	}
	
	public void upload(BufferBuilder.RenderedBuffer buffer, long size)
	{
		if (!this.isInvalid())
		{
			RenderSystem.assertOnRenderThread();
			try
			{
				BufferBuilder.DrawState bufferbuilder$drawstate = buffer.drawState();
				this.format = this.uploadVertexBuffer(bufferbuilder$drawstate, buffer.vertexBuffer());
				this.sequentialIndices = this.uploadIndexBuffer(bufferbuilder$drawstate, buffer.indexBuffer());
				this.indexCount = bufferbuilder$drawstate.indexCount();
				this.indexType = bufferbuilder$drawstate.indexType();
				this.mode = bufferbuilder$drawstate.mode();
				
				// Instanced
				/*TODO GlStateManager._glBindBuffer(GL15C.GL_ARRAY_BUFFER, this.instanceBufferId); // Binds a new buffer for instancing
				GL15C.glBufferData(GL15C.GL_ARRAY_BUFFER, size, GL15C.GL_DYNAMIC_DRAW); // Creates a dynamic buffer data store
				setupBufferState();
				GlStateManager._glBindBuffer(GL15C.GL_ARRAY_BUFFER, 0);
				
				long offset = 0;
				ByteBuffer dataBuffer = MemoryTracker.create(256 * 6); // 256 is an arbitrary number as of now
				GL15C.glBufferSubData(GL15C.GL_ARRAY_BUFFER, offset, dataBuffer);*/ //TODO This probably should be somewhere in Star Field (InstancedLightRenderer#updateLights)
				//TODO It should be possibly to only update the buffer with the information from here, without having to recreate the buffer constantly
			}
			finally
			{
				buffer.release();
			}
			
		}
	}
	
	private VertexFormat uploadVertexBuffer(BufferBuilder.DrawState drawState, ByteBuffer vertexBuffer)
	{
		boolean formatEquals = false;
		if(!drawState.format().equals(this.format))
		{
			if(this.format != null)
				this.format.clearBufferState();
			
			GlStateManager._glBindBuffer(GL15C.GL_ARRAY_BUFFER, this.vertexBufferId);
			drawState.format().setupBufferState();
			formatEquals = true;
		}
		
		if(!drawState.indexOnly())
		{
			if(!formatEquals)
				GlStateManager._glBindBuffer(GL15C.GL_ARRAY_BUFFER, this.vertexBufferId);
			
			RenderSystem.glBufferData(GL15C.GL_ARRAY_BUFFER, vertexBuffer, 35044);
		}
		
		return drawState.format();
	}
	
	@Nullable
	private RenderSystem.AutoStorageIndexBuffer uploadIndexBuffer(BufferBuilder.DrawState drawState, ByteBuffer indexBuffer)
	{
		if(!drawState.sequentialIndex())
		{
			GlStateManager._glBindBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, this.indexBufferId);
			RenderSystem.glBufferData(GL15C.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15C.GL_STATIC_DRAW);
			return null;
		}
		else
		{
			RenderSystem.AutoStorageIndexBuffer rendersystem$autostorageindexbuffer = RenderSystem.getSequentialBuffer(drawState.mode());
			if (rendersystem$autostorageindexbuffer != this.sequentialIndices || !rendersystem$autostorageindexbuffer.hasStorage(drawState.indexCount()))
				rendersystem$autostorageindexbuffer.bind(drawState.indexCount());
			
			return rendersystem$autostorageindexbuffer;
		}
	}
	
	public void bind()
	{
		BufferUploader.invalidate();
		GlStateManager._glBindVertexArray(this.arrayObjectId);
	}
	
	public static void unbind()
	{
		BufferUploader.invalidate();
		GlStateManager._glBindVertexArray(0);
	}
	
	public void draw()
	{
		RenderSystem.drawElements(this.mode.asGLMode, this.indexCount, this.getIndexType().asGLType);
		// Custom
		//RenderSystem.assertOnRenderThread();
		//GL31C.glDrawArraysInstanced(GL40.GL_PATCHES, 0, indexCount * 4, 1);
		//TODO GL31C.glDrawArraysInstanced(GL40.GL_PATCHES, 0, 1, indexCount);
	}
	
	private VertexFormat.IndexType getIndexType()
	{
		RenderSystem.AutoStorageIndexBuffer rendersystem$autostorageindexbuffer = this.sequentialIndices;
		return rendersystem$autostorageindexbuffer != null ? rendersystem$autostorageindexbuffer.type() : this.indexType;
	}
	
	public void drawWithShader(Matrix4f modelViewMatrix, Matrix4f projectionMatrix, Vector3f relativeVectorLy, Vector3f relativeVectorKm, StarShaderInstance shaderInstance)
	{
		if(!RenderSystem.isOnRenderThread())
		{
			RenderSystem.recordRenderCall(() ->
			{
				this._drawWithShader(new Matrix4f(modelViewMatrix), new Matrix4f(projectionMatrix), relativeVectorLy, relativeVectorKm, shaderInstance);
			});
		}
		else
			this._drawWithShader(modelViewMatrix, projectionMatrix, relativeVectorLy, relativeVectorKm, shaderInstance);
	}
	
	private void _drawWithShader(Matrix4f modelViewMatrix, Matrix4f projectionMatrix, Vector3f relativeSpaceLy, Vector3f relativeSpaceKm, StarShaderInstance shaderInstance)
	{
		for(int i = 0; i < 12; ++i)
		{
			int j = RenderSystem.getShaderTexture(i);
			shaderInstance.setSampler("Sampler" + i, j);
		}
		
		if(shaderInstance.MODEL_VIEW_MATRIX != null)
			shaderInstance.MODEL_VIEW_MATRIX.set(modelViewMatrix);
		
		if(shaderInstance.PROJECTION_MATRIX != null)
			shaderInstance.PROJECTION_MATRIX.set(projectionMatrix);
		
		if(shaderInstance.INVERSE_VIEW_ROTATION_MATRIX != null)
			shaderInstance.INVERSE_VIEW_ROTATION_MATRIX.set(RenderSystem.getInverseViewRotationMatrix());
		
		if(shaderInstance.COLOR_MODULATOR != null)
			shaderInstance.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
		
		if(shaderInstance.FOG_START != null)
			shaderInstance.FOG_START.set(RenderSystem.getShaderFogStart());
		
		if(shaderInstance.FOG_END != null)
			shaderInstance.FOG_END.set(RenderSystem.getShaderFogEnd());
		
		if(shaderInstance.FOG_COLOR != null)
			shaderInstance.FOG_COLOR.set(RenderSystem.getShaderFogColor());
		
		if(shaderInstance.FOG_SHAPE != null)
			shaderInstance.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
		
		if(shaderInstance.TEXTURE_MATRIX != null)
			shaderInstance.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
		
		if(shaderInstance.GAME_TIME != null)
			shaderInstance.GAME_TIME.set(RenderSystem.getShaderGameTime());
		
		if(shaderInstance.SCREEN_SIZE != null)
		{
			Window window = Minecraft.getInstance().getWindow();
			shaderInstance.SCREEN_SIZE.set((float)window.getWidth(), (float)window.getHeight());
		}
		
		if(shaderInstance.LINE_WIDTH != null && (this.mode == VertexFormat.Mode.LINES || this.mode == VertexFormat.Mode.LINE_STRIP))
			shaderInstance.LINE_WIDTH.set(RenderSystem.getShaderLineWidth());
		
		if(shaderInstance.RELATIVE_SPACE_LY != null)
			shaderInstance.RELATIVE_SPACE_LY.set(relativeSpaceLy);
		
		if(shaderInstance.RELATIVE_SPACE_KM != null)
			shaderInstance.RELATIVE_SPACE_KM.set(relativeSpaceKm);
		
		RenderSystem.setupShaderLights(shaderInstance);
		shaderInstance.apply();
		this.draw();
		shaderInstance.clear();
	}
	
	public void close()
	{
		if (this.vertexBufferId >= 0)
		{
			RenderSystem.glDeleteBuffers(this.vertexBufferId);
			this.vertexBufferId = -1;
		}
		
		if (this.indexBufferId >= 0)
		{
			RenderSystem.glDeleteBuffers(this.indexBufferId);
			this.indexBufferId = -1;
		}
		
		if (this.arrayObjectId >= 0)
		{
			RenderSystem.glDeleteVertexArrays(this.arrayObjectId);
			this.arrayObjectId = -1;
		}
		
		/*TODO if (this.instanceBufferId >= 0)
		{
			RenderSystem.glDeleteVertexArrays(this.instanceBufferId);
			this.instanceBufferId = -1;
		}*/
		
	}
	
	public VertexFormat getFormat()
	{
		return this.format;
	}
	
	public boolean isInvalid()
	{
		return this.arrayObjectId == -1;
	}
}
