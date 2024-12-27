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
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.spacetravel.client.render.ClientSpaceship;
import net.povstalec.stellarview.client.render.level.util.StellarViewFogEffects;
import net.povstalec.stellarview.client.resourcepack.Skybox;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.client.resourcepack.effects.MeteorEffect;
import net.povstalec.stellarview.client.resourcepack.objects.OrbitingObject;
import net.povstalec.stellarview.client.resourcepack.objects.SpaceObject;
import net.povstalec.stellarview.common.config.GeneralConfig;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Optional;

public class SpaceShipSpecialEffects extends SpaceTravelDimensionSpecialEffects
{
	public static SpaceshipViewCenter spaceshipViewCenter;
	
	public SpaceShipSpecialEffects()
	{
		super(Float.NaN, false, DimensionSpecialEffects.SkyType.NONE, false, false);
		
		spaceshipViewCenter = new SpaceshipViewCenter();
		spaceshipViewCenter.setViewCenterObject(new ClientSpaceship());
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
			super(Optional.empty(), Optional.empty(), new AxisRotation(), 0, 1, 0, 0,
					new MeteorEffect.ShootingStar(), new MeteorEffect.MeteorShower(), false, false, true, 0);
		}
		
		@Override
		protected boolean renderSkyObjectsFrom(ClientLevel level, Camera camera, float partialTicks, PoseStack stack, Matrix4f projectionMatrix, Runnable setupFog, BufferBuilder bufferbuilder)
		{
			if(viewCenterObject == null)
				return false;
			
			coords = viewCenterObject.getCoords();
			
			stack.pushPose();
			
			stack.mulPose(Axis.YP.rotation((float) viewCenterObject.getAxisRotation().yAxis()));
			stack.mulPose(Axis.ZP.rotation((float) viewCenterObject.getAxisRotation().zAxis()));
			stack.mulPose(Axis.XP.rotation((float) viewCenterObject.getAxisRotation().xAxis()));
			
			viewCenterObject.renderFrom(this, level, partialTicks, stack, camera, projectionMatrix, StellarViewFogEffects.isFoggy(minecraft, camera), setupFog, bufferbuilder);
			
			stack.popPose();
			
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			return true;
		}
		
		public void deserializeNBT(CompoundTag tag)
		{
			if(viewCenterObject instanceof ClientSpaceship spaceship)
				spaceship.deserializeNBT(tag);
		}
	}
}
