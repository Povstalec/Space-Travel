package net.povstalec.spacetravel.client.render;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.GameRenderer;

public class TestBuffer implements AutoCloseable
{
	//private int vertexBufferId;
	//private int instanceBufferId;
	
	
	
	public TestBuffer()
	{
		//this.vertexBufferId = GlStateManager._glGenBuffers();
		//this.instanceBufferId = GlStateManager._glGenBuffers();
	}
	
	public static ByteBuffer getBuffer()
	{
		RenderSystem.assertOnRenderThread();
		
		//GameRenderer.getPositionShader().apply();
		
		ByteBuffer vertexBuffer = MemoryTracker.create(9 * 6);
		
		vertexBuffer.putFloat(0); // 4 bytes
		vertexBuffer.putFloat(1000); // 4 bytes
		vertexBuffer.putFloat(-10); // 4 bytes
		
		vertexBuffer.putFloat(-1000); // 4 bytes
		vertexBuffer.putFloat(-1000); // 4 bytes
		vertexBuffer.putFloat(-10); // 4 bytes
		
		vertexBuffer.putFloat(1000); // 4 bytes
		vertexBuffer.putFloat(-1000); // 4 bytes
		vertexBuffer.putFloat(-10); // 4 bytes
		
		//vertexBuffer.putFloat(1); // 4 bytes
		//vertexBuffer.putFloat(-1); // 4 bytes
		//vertexBuffer.putFloat(1); // 4 bytes

		/*int vertexBufferId = GlStateManager._glGenBuffers();
		int arrayObjectId = GlStateManager._glGenVertexArrays();
		
		GlStateManager._glBindVertexArray(arrayObjectId);
		GlStateManager._glBindBuffer(GL15C.GL_ARRAY_BUFFER, vertexBufferId);
		
        RenderSystem.glBufferData(GL15C.GL_ARRAY_BUFFER, vertexBuffer, GL15C.GL_STATIC_DRAW);
		GL20C.glEnableVertexAttribArray(0); // Attribute with index 0
		GL20C.glVertexAttribPointer(0, 3, GL15C.GL_FLOAT, false, 0, vertexBuffer); // Attribute 0 has 3 float elements
		
		GlStateManager._glBindVertexArray(0);
		
		//RenderSystem.drawElements(GL15C.GL_TRIANGLES, 0, VertexFormat.IndexType.SHORT.asGLType);
		//GL11C.glDrawElements(GL15C.GL_TRIANGLES, vertexBuffer);
		GL11C.glDrawArrays(GL15C.GL_TRIANGLES, 0, 3);
		
		RenderSystem.glDeleteBuffers(vertexBufferId);
		RenderSystem.glDeleteVertexArrays(arrayObjectId);
		
		GameRenderer.getPositionShader().clear();*/
		
		return vertexBuffer;
	}
	
	public static void doStuff()
	{
		RenderSystem.assertOnRenderThread();
		
		GameRenderer.getPositionShader().apply();
		
		ByteBuffer vertexBuffer = MemoryTracker.create(36); // 3 * 3 * 4 bytes
		
		vertexBuffer.putFloat(0); // 4 bytes
		vertexBuffer.putFloat(1000); // 4 bytes
		vertexBuffer.putFloat(-10); // 4 bytes
		
		vertexBuffer.putFloat(-1000); // 4 bytes
		vertexBuffer.putFloat(-1000); // 4 bytes
		vertexBuffer.putFloat(-10); // 4 bytes
		
		vertexBuffer.putFloat(1000); // 4 bytes
		vertexBuffer.putFloat(-1000); // 4 bytes
		vertexBuffer.putFloat(-10); // 4 bytes
		
		//vertexBuffer.putFloat(1); // 4 bytes
		//vertexBuffer.putFloat(-1); // 4 bytes
		//vertexBuffer.putFloat(1); // 4 bytes

		int vertexBufferId = GlStateManager._glGenBuffers();
		int arrayObjectId = GlStateManager._glGenVertexArrays();
		
		GlStateManager._glBindVertexArray(arrayObjectId);
		GlStateManager._glBindBuffer(GL15C.GL_ARRAY_BUFFER, vertexBufferId);
		
        RenderSystem.glBufferData(GL15C.GL_ARRAY_BUFFER, vertexBuffer, GL15C.GL_STATIC_DRAW);
		GL20C.glEnableVertexAttribArray(0); // Attribute with index 0
		GL20C.glVertexAttribPointer(0, 3, GL15C.GL_FLOAT, false, 0, vertexBuffer); // Attribute 0 has 3 float elements
		
		//RenderSystem.drawElements(GL15C.GL_TRIANGLES, 0, VertexFormat.IndexType.SHORT.asGLType);
		//GL11C.glDrawElements(GL15C.GL_TRIANGLES, vertexBuffer);
		GL11C.glDrawArrays(GL15C.GL_TRIANGLES, 0, 3);
		
		GlStateManager._glBindVertexArray(0);
		
		RenderSystem.glDeleteBuffers(vertexBufferId);
		RenderSystem.glDeleteVertexArrays(arrayObjectId);
		
		GameRenderer.getPositionShader().clear();
	}
	
	/*public void bind()
	{
		BufferUploader.invalidate();
		
		GlStateManager._glBindVertexArray(this.vertexBufferId);
	}
	
	public void unbind()
	{
		BufferUploader.invalidate();
		GlStateManager._glBindVertexArray(0);
	}*/
	
	@Override
	public void close() throws Exception
	{
		
	}
}
