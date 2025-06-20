package net.povstalec.spacetravel.common.space.generation.parameters;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.povstalec.spacetravel.common.space.generation.ParameterLocation;
import net.povstalec.spacetravel.common.space.generation.ParameterLocations;
import net.povstalec.spacetravel.common.space.generation.SpaceTravelParameters;
import net.povstalec.stellarview.api.common.space_objects.OrbitingObject;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public abstract class OrbitingObjectParameters<T extends OrbitingObject> extends SpaceObjectParameters<T>
{
	public static final String ORBIT_PARAMETERS = "orbit_parameters";
	
	@Nullable
	protected OrbitParameters orbitParameters;
	
	public OrbitingObjectParameters(Optional<OrbitParameters> orbitParameters, Optional<List<ParameterLocations>> instantChildrenParameters, Optional<List<ParameterLocations>> childrenParameters)
	{
		super(instantChildrenParameters, childrenParameters);
		this.orbitParameters = orbitParameters.orElse(null);
	}
	
	public abstract T generateOrbiting(Random random, long seed, SpaceCoords spaceCoords, AxisRotation axisRotation, @Nullable OrbitingObject.OrbitInfo orbitInfo);
	
	public T generate(Random random, long seed, SpaceCoords spaceCoords, AxisRotation axisRotation)
	{
		if(orbitParameters == null)
			return generateOrbiting(random, seed, spaceCoords, axisRotation, null);
		else
			return generateOrbiting(random, seed, spaceCoords, axisRotation, orbitParameters.randomOrbitInfo(random));
	}
	
	
	
	public static class OrbitalPeriodParameters
	{
		protected SpaceTravelParameters.LongRange ticks;
		protected SpaceTravelParameters.DoubleRange orbits;
		
		protected boolean synodic;
		
		public static final Codec<OrbitalPeriodParameters> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				SpaceTravelParameters.LongRange.codec(0L, Long.MAX_VALUE).fieldOf(OrbitingObject.OrbitalPeriod.TICKS).forGetter(parameters -> parameters.ticks),
				SpaceTravelParameters.DoubleRange.codec(0D, Double.MAX_VALUE).fieldOf(OrbitingObject.OrbitalPeriod.ORBITS).forGetter(parameters -> parameters.orbits),
				
				Codec.BOOL.fieldOf(OrbitingObject.OrbitalPeriod.SYNODIC).forGetter(parameters -> parameters.synodic)
		).apply(instance, OrbitalPeriodParameters::new));
		
		public OrbitalPeriodParameters(SpaceTravelParameters.LongRange ticks, SpaceTravelParameters.DoubleRange orbits, boolean synodic)
		{
			this.ticks = ticks;
			this.orbits = orbits;
			
			this.synodic = synodic;
		}
		
		public OrbitingObject.OrbitalPeriod randomOrbitalPeriod(Random random)
		{
			return new OrbitingObject.OrbitalPeriod(ticks.nextLong(random), orbits.nextDouble(random), synodic);
		}
	}
	
	public static class OrbitParameters
	{
		protected SpaceTravelParameters.FloatRange apoapsis;
		protected SpaceTravelParameters.FloatRange periapsis;
		protected float orbitClampDistance;
		
		protected OrbitalPeriodParameters orbitalPeriod;
		
		protected SpaceTravelParameters.FloatRange argumentOfPeriapsis;
		
		protected SpaceTravelParameters.FloatRange inclination;
		protected SpaceTravelParameters.FloatRange longtitudeOfAscendingNode;
		
		private SpaceTravelParameters.FloatRange epochMeanAnomaly;
		
		public static final Codec<OrbitParameters> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				SpaceTravelParameters.FloatRange.POSITIVE_RANGE_CODEC.fieldOf(OrbitingObject.OrbitInfo.APOAPSIS).forGetter(parameters -> parameters.apoapsis),
				SpaceTravelParameters.FloatRange.POSITIVE_RANGE_CODEC.fieldOf(OrbitingObject.OrbitInfo.PERIAPSIS).forGetter(parameters -> parameters.periapsis),
				Codec.floatRange(0F, Float.MAX_VALUE).optionalFieldOf(OrbitingObject.OrbitInfo.ORBIT_CLAMP_DISTANCE, 0F).forGetter(parameters -> parameters.orbitClampDistance),
				
				OrbitalPeriodParameters.CODEC.fieldOf(OrbitingObject.OrbitInfo.ORBITAL_PERIOD).forGetter(parameters -> parameters.orbitalPeriod),
				
				SpaceTravelParameters.FloatRange.ANGLE_RANGE_CODEC.fieldOf(OrbitingObject.OrbitInfo.ARGUMENT_OF_PERIAPSIS).forGetter(parameters -> parameters.argumentOfPeriapsis),
				
				SpaceTravelParameters.FloatRange.ANGLE_RANGE_CODEC.fieldOf(OrbitingObject.OrbitInfo.INCLINATION).forGetter(parameters -> parameters.inclination),
				SpaceTravelParameters.FloatRange.ANGLE_RANGE_CODEC.fieldOf(OrbitingObject.OrbitInfo.LONGTITUDE_OF_ASCENDING_NODE).forGetter(parameters -> parameters.longtitudeOfAscendingNode),
				
				SpaceTravelParameters.FloatRange.ANGLE_RANGE_CODEC.fieldOf(OrbitingObject.OrbitInfo.EPOCH_MEAN_ANOMALY).forGetter(parameters -> parameters.epochMeanAnomaly)
		).apply(instance, OrbitParameters::new));
		
		public OrbitParameters(SpaceTravelParameters.FloatRange apoapsis, SpaceTravelParameters.FloatRange periapsis, float orbitClampDistance, OrbitalPeriodParameters orbitalPeriod,
							   SpaceTravelParameters.FloatRange argumentOfPeriapsis, SpaceTravelParameters.FloatRange inclination, SpaceTravelParameters.FloatRange longtitudeOfAscendingNode,
							   SpaceTravelParameters.FloatRange epochMeanAnomaly)
		{
			this.apoapsis = apoapsis;
			this.periapsis = periapsis;
			this.orbitClampDistance = orbitClampDistance;
			
			this.orbitalPeriod = orbitalPeriod;
			
			this.argumentOfPeriapsis = argumentOfPeriapsis;
			
			this.inclination = inclination;
			this.longtitudeOfAscendingNode = longtitudeOfAscendingNode;
			
			this.epochMeanAnomaly = epochMeanAnomaly;
		}
		
		public OrbitingObject.OrbitInfo randomOrbitInfo(Random random)
		{
			return new OrbitingObject.OrbitInfo(apoapsis.nextFloat(random), periapsis.nextFloat(random), orbitClampDistance,
					orbitalPeriod.randomOrbitalPeriod(random),
					argumentOfPeriapsis.nextFloat(random),
					inclination.nextFloat(random), longtitudeOfAscendingNode.nextFloat(random),
					epochMeanAnomaly.nextFloat(random));
		}
	}
}
