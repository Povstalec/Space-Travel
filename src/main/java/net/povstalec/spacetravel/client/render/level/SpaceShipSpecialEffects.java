package net.povstalec.spacetravel.client.render.level;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.CompoundTag;
import net.povstalec.spacetravel.client.render.SpaceshipRenderer;
import net.povstalec.spacetravel.common.space.Spaceship;
import net.povstalec.stellarview.client.render.level.util.StellarViewFogEffects;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.client.resourcepack.effects.MeteorEffect;
import net.povstalec.stellarview.common.util.AxisRotation;
import org.joml.Matrix4f;

import java.util.Optional;

public class SpaceShipSpecialEffects extends SpaceTravelDimensionSpecialEffects
{
	public static SpaceshipViewCenter spaceshipViewCenter;
	
	public SpaceShipSpecialEffects()
	{
		super(Float.NaN, false, DimensionSpecialEffects.SkyType.NONE, false, false);
		
		spaceshipViewCenter = new SpaceshipViewCenter();
		spaceshipViewCenter.setViewObjectRenderer(new SpaceshipRenderer(new Spaceship()));
	}
	
	@Override
	public boolean renderSky(ClientLevel level, int ticks, float partialTicks, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog)
	{
		if(spaceshipViewCenter == null)
			return false;
		
		return spaceshipViewCenter.renderSky(level, ticks, partialTicks, poseStack, camera, projectionMatrix, isFoggy, setupFog);
	}
	
	
	
	public static class SpaceshipViewCenter extends ViewCenter
	{
		public SpaceshipViewCenter()
		{
			super(Optional.empty(), Optional.empty(), new AxisRotation(), 0, DayBlending.DAY_BLENDING, ViewCenter.DayBlending.SUN_DAY_BLENDING,
					new MeteorEffect.ShootingStar(), new MeteorEffect.MeteorShower(), false, false, true, 0);
		}
		
		@Override
		protected boolean renderSkyObjectsFrom(ClientLevel level, Camera camera, float partialTicks, PoseStack stack, Matrix4f projectionMatrix, Runnable setupFog, BufferBuilder bufferbuilder)
		{
			if(viewObject == null)
				return false;
			
			coords = viewObject.spaceCoords();
			this.ticks = level.getGameTime();
			this.starBrightness = 1F;//StarLike.getStarBrightness(this, level, camera, partialTicks);
			this.dustCloudBrightness = 0.5F;//GeneralConfig.dust_clouds.get() ? StarField.dustCloudBrightness(this, level, camera, partialTicks) : 0.0F;
			
			stack.pushPose();
			
			stack.mulPose(Axis.YP.rotation((float) viewObject.axisRotation().yAxis()));
			stack.mulPose(Axis.ZP.rotation((float) viewObject.axisRotation().zAxis()));
			stack.mulPose(Axis.XP.rotation((float) viewObject.axisRotation().xAxis()));
			
			viewObject.renderFrom(this, level, partialTicks, stack, camera, projectionMatrix, StellarViewFogEffects.isFoggy(minecraft, camera), setupFog, bufferbuilder);
			
			stack.popPose();
			
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			return true;
		}
		
		public void deserializeNBT(CompoundTag tag)
		{
			if(viewObject instanceof SpaceshipRenderer spaceship)
				spaceship.renderedObject().deserializeNBT(tag);
		}
	}
}
