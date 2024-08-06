package net.povstalec.spacetravel.client.render;

import java.nio.ByteBuffer;

import javax.annotation.Nullable;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL31C;
import org.lwjgl.opengl.GL40;

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
	   }

	   public void upload(BufferBuilder.RenderedBuffer p_231222_) {
	      if (!this.isInvalid()) {
	         RenderSystem.assertOnRenderThread();
	         try {
	            BufferBuilder.DrawState bufferbuilder$drawstate = p_231222_.drawState();
	            this.format = this.uploadVertexBuffer(bufferbuilder$drawstate, p_231222_.vertexBuffer());
	            this.sequentialIndices = this.uploadIndexBuffer(bufferbuilder$drawstate, p_231222_.indexBuffer());
	            this.indexCount = bufferbuilder$drawstate.indexCount();
	            this.indexType = bufferbuilder$drawstate.indexType();
	            this.mode = bufferbuilder$drawstate.mode();
	         } finally {
	            p_231222_.release();
	         }

	      }
	   }

	   private VertexFormat uploadVertexBuffer(BufferBuilder.DrawState p_231219_, ByteBuffer p_231220_) {
	      boolean flag = false;
	      if (!p_231219_.format().equals(this.format)) {
	         if (this.format != null) {
	            this.format.clearBufferState();
	         }

	         GlStateManager._glBindBuffer(34962, this.vertexBufferId);
	         p_231219_.format().setupBufferState();
	         flag = true;
	      }

	      if (!p_231219_.indexOnly()) {
	         if (!flag) {
	            GlStateManager._glBindBuffer(34962, this.vertexBufferId);
	         }

	         RenderSystem.glBufferData(34962, p_231220_, 35044);
	      }

	      return p_231219_.format();
	   }

	   @Nullable
	   private RenderSystem.AutoStorageIndexBuffer uploadIndexBuffer(BufferBuilder.DrawState p_231224_, ByteBuffer p_231225_) {
	      if (!p_231224_.sequentialIndex()) {
	         GlStateManager._glBindBuffer(34963, this.indexBufferId);
	         RenderSystem.glBufferData(34963, p_231225_, 35044);
	         return null;
	      } else {
	         RenderSystem.AutoStorageIndexBuffer rendersystem$autostorageindexbuffer = RenderSystem.getSequentialBuffer(p_231224_.mode());
	         if (rendersystem$autostorageindexbuffer != this.sequentialIndices || !rendersystem$autostorageindexbuffer.hasStorage(p_231224_.indexCount())) {
	            rendersystem$autostorageindexbuffer.bind(p_231224_.indexCount());
	         }

	         return rendersystem$autostorageindexbuffer;
	      }
	   }

	   public void bind() {
	      BufferUploader.invalidate();
	      GlStateManager._glBindVertexArray(this.arrayObjectId);
	   }

	   public static void unbind() {
	      BufferUploader.invalidate();
	      GlStateManager._glBindVertexArray(0);
	   }

	   public void draw() {
		   RenderSystem.drawElements(this.mode.asGLMode, this.indexCount, this.getIndexType().asGLType);
		   // Custom
		   //RenderSystem.assertOnRenderThread();
		   //GL31C.glDrawArraysInstanced(GL40.GL_PATCHES, 0, indexCount, 1);
		   //GL31C.glDrawArraysInstanced(GL40.GL_PATCHES, 0, 1, indexCount);
	   }

	   private VertexFormat.IndexType getIndexType() {
	      RenderSystem.AutoStorageIndexBuffer rendersystem$autostorageindexbuffer = this.sequentialIndices;
	      return rendersystem$autostorageindexbuffer != null ? rendersystem$autostorageindexbuffer.type() : this.indexType;
	   }

	   public void drawWithShader(Matrix4f modelViewMatrix, Matrix4f projectionMatrix, Vector3f relativeSpacePos, StarShaderInstance shaderInstance) {
	      if (!RenderSystem.isOnRenderThread()) {
	         RenderSystem.recordRenderCall(() -> {
	            this._drawWithShader(new Matrix4f(modelViewMatrix), new Matrix4f(projectionMatrix), relativeSpacePos, shaderInstance);
	         });
	      } else {
	         this._drawWithShader(modelViewMatrix, projectionMatrix, relativeSpacePos, shaderInstance);
	      }

	   }

	   private void _drawWithShader(Matrix4f modelViewMatrix, Matrix4f projectionMatrix, Vector3f relativeSpacePos, StarShaderInstance shaderInstance) {
	      for(int i = 0; i < 12; ++i) {
	         int j = RenderSystem.getShaderTexture(i);
	         shaderInstance.setSampler("Sampler" + i, j);
	      }

	      if (shaderInstance.MODEL_VIEW_MATRIX != null) {
	         shaderInstance.MODEL_VIEW_MATRIX.set(modelViewMatrix);
	      }

	      if (shaderInstance.PROJECTION_MATRIX != null) {
	         shaderInstance.PROJECTION_MATRIX.set(projectionMatrix);
	      }

	      if (shaderInstance.INVERSE_VIEW_ROTATION_MATRIX != null) {
	         shaderInstance.INVERSE_VIEW_ROTATION_MATRIX.set(RenderSystem.getInverseViewRotationMatrix());
	      }

	      if (shaderInstance.COLOR_MODULATOR != null) {
	         shaderInstance.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
	      }

	      if (shaderInstance.FOG_START != null) {
	         shaderInstance.FOG_START.set(RenderSystem.getShaderFogStart());
	      }

	      if (shaderInstance.FOG_END != null) {
	         shaderInstance.FOG_END.set(RenderSystem.getShaderFogEnd());
	      }

	      if (shaderInstance.FOG_COLOR != null) {
	         shaderInstance.FOG_COLOR.set(RenderSystem.getShaderFogColor());
	      }

	      if (shaderInstance.FOG_SHAPE != null) {
	         shaderInstance.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
	      }

	      if (shaderInstance.TEXTURE_MATRIX != null) {
	         shaderInstance.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
	      }

	      if (shaderInstance.GAME_TIME != null) {
	         shaderInstance.GAME_TIME.set(RenderSystem.getShaderGameTime());
	      }

	      if (shaderInstance.SCREEN_SIZE != null) {
	         Window window = Minecraft.getInstance().getWindow();
	         shaderInstance.SCREEN_SIZE.set((float)window.getWidth(), (float)window.getHeight());
	      }

	      if (shaderInstance.LINE_WIDTH != null && (this.mode == VertexFormat.Mode.LINES || this.mode == VertexFormat.Mode.LINE_STRIP)) {
	         shaderInstance.LINE_WIDTH.set(RenderSystem.getShaderLineWidth());
	      }

	      if (shaderInstance.RELATIVE_SPACE_POS != null) {
	         shaderInstance.RELATIVE_SPACE_POS.set(relativeSpacePos);
	      }

	      RenderSystem.setupShaderLights(shaderInstance);
	      shaderInstance.apply();
	      this.draw();
	      shaderInstance.clear();
	   }

	   public void close() {
	      if (this.vertexBufferId >= 0) {
	         RenderSystem.glDeleteBuffers(this.vertexBufferId);
	         this.vertexBufferId = -1;
	      }

	      if (this.indexBufferId >= 0) {
	         RenderSystem.glDeleteBuffers(this.indexBufferId);
	         this.indexBufferId = -1;
	      }

	      if (this.arrayObjectId >= 0) {
	         RenderSystem.glDeleteVertexArrays(this.arrayObjectId);
	         this.arrayObjectId = -1;
	      }

	   }

	   public VertexFormat getFormat() {
	      return this.format;
	   }

	   public boolean isInvalid() {
	      return this.arrayObjectId == -1;
	   }
}
