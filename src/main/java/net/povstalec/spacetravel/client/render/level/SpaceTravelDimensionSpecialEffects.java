package net.povstalec.spacetravel.client.render.level;

import javax.annotation.Nullable;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.povstalec.spacetravel.SpaceTravel;

public class SpaceTravelDimensionSpecialEffects extends DimensionSpecialEffects
{
    public static final ResourceLocation SPACE_EFFECTS = new ResourceLocation(SpaceTravel.MODID, "space_effects");
	public static final ResourceLocation PLANET_EFFECTS = new ResourceLocation(SpaceTravel.MODID, "planet_effects");
    
	public SpaceTravelDimensionSpecialEffects(float cloudLevel, boolean hasGround, SkyType skyType,
            boolean forceBrightLightmap, boolean constantAmbientLight)
	{
		super(cloudLevel, hasGround, skyType, forceBrightLightmap, constantAmbientLight);
	}

    @Nullable
	@Override
    public float[] getSunriseColor(float p_108872_, float p_108873_)
    {
          return new float[] {0, 0, 0, 0};
    }

	@Override
	public Vec3 getBrightnessDependentFogColor(Vec3 biomeFogColor, float daylight)
	{
		return new Vec3(0, 0, 0);
	}

	@Override
	public boolean isFoggyAt(int x, int y)
	{
		return false;
	}
	
	@Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog)
    {
		return true;
    }

    @Override
    public boolean renderClouds(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, double camX, double camY, double camZ, Matrix4f projectionMatrix)
    {
        return false;
    }
	
	
	
	public static void register(RegisterDimensionSpecialEffectsEvent event)
	{
		event.register(SpaceTravelDimensionSpecialEffects.SPACE_EFFECTS, new SpaceShipSpecialEffects());
		event.register(SpaceTravelDimensionSpecialEffects.PLANET_EFFECTS, new PlanetSpecialEffects());
	}
}
