package net.povstalec.spacetravel.client;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class ClientAccess
{
	protected static Minecraft minecraft = Minecraft.getInstance();
    
    public static void updateDimensions(Set<ResourceKey<Level>> levelKeys, boolean add)
    {
    	final LocalPlayer player = minecraft.player;
        if(player == null)
            return;

        final Set<ResourceKey<Level>> dimensionList = player.connection.levels();
        if(dimensionList == null)
            return;
        
        if(add)
        {
        	for(ResourceKey<Level> levelKey : levelKeys)
            {
            	dimensionList.add(levelKey);
            }
        }
        else
        {
        	for(ResourceKey<Level> levelKey : levelKeys)
            {
            	dimensionList.remove(levelKey);
            }
        }
    }
}