package net.povstalec.spacetravel.client.render;

import net.povstalec.spacetravel.common.space.Spaceship;
import net.povstalec.stellarview.client.render.space_objects.ViewObjectRenderer;

public class SpaceshipRenderer<T extends Spaceship> extends ViewObjectRenderer<T>
{
	public SpaceshipRenderer(T spaceship)
	{
		super(spaceship);
	}
}
