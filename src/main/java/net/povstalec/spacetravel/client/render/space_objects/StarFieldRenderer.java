package net.povstalec.spacetravel.client.render.space_objects;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.povstalec.spacetravel.client.RenderCenter;
import net.povstalec.spacetravel.client.render.StarBuffer;
import net.povstalec.spacetravel.client.render.shaders.SpaceTravelShaders;
import net.povstalec.spacetravel.client.render.shaders.SpaceTravelVertexFormat;
import net.povstalec.spacetravel.common.space.SpaceRegion;
import net.povstalec.spacetravel.common.space.objects.StarField;
import net.povstalec.spacetravel.common.util.*;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL33C;

import javax.annotation.Nullable;

public class StarFieldRenderer<SF extends StarField> extends SpaceObjectRenderer<StarField>
{
	private static final Vector3f NULL_VECTOR = new Vector3f();
	private static final float MAX_RENDER_DISTANCE = new Vector3f(SpaceRegion.LY_PER_REGION * (SpaceRegion.SPACE_REGION_LOAD_DISTANCE - 1), 0, 0).lengthSquared();
	private static final float FADE_START_DISTANCE = new Vector3f(SpaceRegion.LY_PER_REGION * (SpaceRegion.SPACE_REGION_LOAD_DISTANCE - 2), 0, 0).lengthSquared();
	private static final float FADE = MAX_RENDER_DISTANCE - FADE_START_DISTANCE;
	
	@Nullable
	protected StarBuffer starBuffer;
	
	protected StarData starData;
	
	protected SpaceCoords oldDifference;
	
	public StarFieldRenderer(StarField starField)
	{
		super(starField);
	}
	
	public boolean requiresSetup()
	{
		return starBuffer == null;
	}
	
	public void reset()
	{
		starBuffer = null;
	}
	
	protected void generateStars(BufferBuilder bufferBuilder, RandomSource randomsource)
	{
		for(int i = 0; i < spaceObject.getStars(); i++)
		{
			// This generates random coordinates for the Star close to the camera
			double distance = spaceObject.clumpStarsInCenter() ? randomsource.nextDouble() : Math.cbrt(randomsource.nextDouble());
			double theta = randomsource.nextDouble() * 2F * Math.PI;
			double phi = Math.acos(2F * randomsource.nextDouble() - 1F); // This prevents the formation of that weird streak that normally happens
			
			Vector3d cartesian = new SphericalCoords(distance * spaceObject.getDiameter(), theta, phi).toCartesianD();
			
			cartesian.x *= spaceObject.xStretch();
			cartesian.y *= spaceObject.yStretch();
			cartesian.z *= spaceObject.zStretch();
			
			spaceObject.getAxisRotation().quaterniond().transform(cartesian);
			
			starData.newStar(spaceObject.getStarInfo(), bufferBuilder, randomsource, cartesian.x, cartesian.y, cartesian.z, i);
		}
	}
	
	protected BufferBuilder.RenderedBuffer generateStarBuffer(BufferBuilder bufferBuilder)
	{
		RandomSource randomsource = RandomSource.create(spaceObject.getSeed());
		bufferBuilder.begin(VertexFormat.Mode.QUADS, SpaceTravelVertexFormat.STAR_POS_COLOR_LY);
		
		double sizeMultiplier = spaceObject.getDiameter() / 30D;
		
		starData = new StarData(spaceObject.getTotalStars());
		
		generateStars(bufferBuilder, randomsource);
		
		int numberOfStars = spaceObject.getStars();
		for(StarField.SpiralArm arm : spaceObject.getSpiralArms()) //Draw each arm
		{
			generateArmStars(arm, bufferBuilder, spaceObject.getAxisRotation(), starData, spaceObject.getStarInfo(), randomsource, numberOfStars, sizeMultiplier);
			numberOfStars += arm.armStars();
		}
		
		return bufferBuilder.end();
	}
	
	protected BufferBuilder.RenderedBuffer getStarBuffer(BufferBuilder bufferBuilder)
	{
		bufferBuilder.begin(VertexFormat.Mode.QUADS, SpaceTravelVertexFormat.STAR_POS_COLOR_LY);
		
		for(int i = 0; i < spaceObject.getTotalStars(); i++)
		{
			starData.createStar(bufferBuilder, i);
		}
		return bufferBuilder.end();
	}
	
	public void setStarBuffer(SpaceCoords difference)
	{
		if(starBuffer != null)
			starBuffer.close();
		oldDifference = difference;
		
		starBuffer = new StarBuffer();
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		RenderSystem.setShader(GameRenderer::getPositionShader);
		BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer;
		
		bufferbuilder$renderedbuffer = getStarBuffer(bufferBuilder);
		
		starBuffer.bind();
		starBuffer.upload(bufferbuilder$renderedbuffer, this.spaceObject.getTotalStars());
		VertexBuffer.unbind();
	}
	
	public void setupBuffer(SpaceCoords difference)
	{
		if(starBuffer != null)
			starBuffer.close();
		oldDifference = difference;
		
		starBuffer = new StarBuffer();
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		RenderSystem.setShader(GameRenderer::getPositionShader);
		BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer;
		
		bufferbuilder$renderedbuffer = generateStarBuffer(bufferBuilder);
		
		starBuffer.bind();
		starBuffer.upload(bufferbuilder$renderedbuffer, this.spaceObject.getTotalStars());
		VertexBuffer.unbind();
	}
	
	@Override
	public void render(RenderCenter viewCenter, ClientLevel level, float partialTicks, PoseStack stack, Camera camera,
					   Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, BufferBuilder bufferbuilder,
					   Vector3f parentVector, AxisRotation parentRotation)
	{
		SpaceCoords difference = viewCenter.getCoords().sub(spaceObject.getSpaceCoords());
		
		if(requiresSetup())
			setupBuffer(difference);
		
		float starBrightness = getStarBrightness(level, camera, partialTicks);
		
		if(starBrightness > 0.0F)
		{
			Vector3f relativeVectorLy = new Vector3f(
					Mth.lerp(partialTicks, (float) oldDifference.x().ly(), (float) difference.x().ly()),
					Mth.lerp(partialTicks, (float) oldDifference.y().ly(), (float) difference.y().ly()),
					Mth.lerp(partialTicks, (float) oldDifference.z().ly(), (float) difference.z().ly()));
			Vector3f relativeVectorKm = new Vector3f((float) difference.x().km(), (float) difference.y().km(), (float) difference.z().km());
			
			float alpha = 1;
			
			float lyDistance = relativeVectorLy.lengthSquared();
			
			if(lyDistance > MAX_RENDER_DISTANCE)
				alpha = 0;
			else if(lyDistance > FADE_START_DISTANCE)
			{
				float value = (MAX_RENDER_DISTANCE - lyDistance);
				alpha = value / FADE;
				
				if(alpha > 1)
					alpha = 1;
			}
			
			if(alpha > 0)
			{
				stack.pushPose();
				
				//stack.translate(0, 0, 0);
				RenderSystem.setShaderColor(1, 1, 1, starBrightness * alpha);
				//RenderSystem.setShaderTexture(0, new ResourceLocation("textures/environment/sun.png"));
				FogRenderer.setupNoFog();
				
				Quaternionf q = SpaceCoords.getQuaternionf(level, viewCenter, partialTicks);
				
				stack.mulPose(q);
				this.starBuffer.bind();
				this.starBuffer.drawWithShader(stack.last().pose(), projectionMatrix, relativeVectorLy, relativeVectorKm, SpaceTravelShaders.starShader());
				//this.starBuffer.drawWithShader(stack.last().pose(), projectionMatrix, GameRenderer.getPositionColorTexShader());
				VertexBuffer.unbind();
				
				setupFog.run();
				stack.popPose();
			}
		}
		
		for(SpaceObjectRenderer<?> child : clientChildren)
		{
			child.render(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder, NULL_VECTOR, new AxisRotation());
		}
		
		oldDifference = difference;
	}
	
	/**
	 * @param level The Level the Player is currently in
	 * @param camera Player Camera
	 * @param partialTicks
	 * @return
	 */
	public static float getStarBrightness(ClientLevel level, Camera camera, float partialTicks)
	{
		//float rain = 1.0F - level.getRainLevel(partialTicks);
		//float starBrightness = level.getStarBrightness(partialTicks) * rain;
		
		return 1;//starBrightness; //TODO Change this back
	}
	
	public static void generateArmStars(StarField.SpiralArm arm, BufferBuilder bufferBuilder, AxisRotation axisRotation, StarData starData, StarInfo starInfo, RandomSource randomsource, int numberOfStars, double sizeMultiplier)
	{
		for(int i = 0; i < arm.armStars(); i++)
		{
			// Milky Way is 90 000 ly across
			
			double progress = (double) i / arm.armStars();
			
			double phi = arm.armLength() * Math.PI * progress - arm.armRotation();
			double r = StellarCoordinates.spiralR(5, phi, arm.armRotation());
			
			// This generates random coordinates for the Star close to the camera
			double distance = arm.clumpStarsInCenter() ? randomsource.nextDouble() : Math.cbrt(randomsource.nextDouble());
			double theta = randomsource.nextDouble() * 2F * Math.PI;
			double sphericalphi = Math.acos(2F * randomsource.nextDouble() - 1F); // This prevents the formation of that weird streak that normally happens
			
			Vector3d cartesian = new SphericalCoords(distance * arm.armThickness(), theta, sphericalphi).toCartesianD();
			
			double x =  r * Math.cos(phi) + cartesian.x * arm.armThickness() / (progress * 1.5);
			double z =  r * Math.sin(phi) + cartesian.z * arm.armThickness() / (progress * 1.5);
			double y =  cartesian.y * arm.armThickness() / (progress * 1.5);
			
			cartesian.x = x * sizeMultiplier;
			cartesian.y = y * sizeMultiplier;
			cartesian.z = z * sizeMultiplier;
			
			axisRotation.quaterniond().transform(cartesian);
			
			starData.newStar(starInfo, bufferBuilder, randomsource, cartesian.x, cartesian.y, cartesian.z, numberOfStars + i);
		}
	}
}
