package toughasnails.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import toughasnails.network.message.MessageUpdateTemperature;
import toughasnails.temperature.TemperatureStats;

public class TemperatureStatHandler
{
    @SubscribeEvent
    public void onPlayerConstructing(EntityConstructing event)
    {
        if (event.entity instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer)event.entity;
            
            player.registerExtendedProperties("temperature", new TemperatureStats());
        }
    }
    
    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent event)
    {
        EntityPlayer player = event.player;
        World world = player.worldObj;
        
        if (!world.isRemote)
        {
            TemperatureStats temperatureStats = (TemperatureStats)player.getExtendedProperties("temperature");
            int temperatureLevel = temperatureStats.getTemperature().getScalePos();
            
            PacketHandler.instance.sendTo(new MessageUpdateTemperature(temperatureLevel), (EntityPlayerMP)player);
        }
    }
    
    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event)
    {
        EntityPlayer player = event.player;
        World world = player.worldObj;
        
        if (event.phase == Phase.END)
        {
            TemperatureStats temperatureStats = (TemperatureStats)player.getExtendedProperties("temperature");
            int temperatureLevel = temperatureStats.getTemperature().getScalePos();
            
            if (!world.isRemote)
            {
                temperatureStats.update(world, player);
            }
            
            if (temperatureStats.getPrevTemperature().getScalePos() != temperatureLevel)
            {
                temperatureStats.setPrevTemperature(temperatureLevel);
                
                if (!world.isRemote)
                {
                    System.out.println("TEMP UPDATE: " + temperatureStats.getTemperature().getScalePos());
                    
                    PacketHandler.instance.sendTo(new MessageUpdateTemperature(temperatureLevel), (EntityPlayerMP)player);
                }
            }
        }
    }
}