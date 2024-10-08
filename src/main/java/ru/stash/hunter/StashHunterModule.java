package ru.stash.hunter;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import org.rusherhack.client.api.events.client.EventUpdate;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.BooleanSetting;
import org.rusherhack.core.setting.NumberSetting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.rusherhack.client.api.RusherHackAPI.*;

public class StashHunterModule extends ToggleableModule {
    public StashHunterModule(){
        super("StashHunter","Auromatically scans territory", ModuleCategory.PLAYER);
        //settings
        registerSettings(gap, active, radius,disconnect);
        disconnect.addSubSettings(dishealth);
    }
    //setting
    private final NumberSetting<Integer> gap = new NumberSetting<>("Gap","In blocks",8,1,64);
    private final NumberSetting<Integer> radius = new NumberSetting<>("Radius","in gaps",4,1,32);
    private final BooleanSetting active = new BooleanSetting("Active",false);

    private final BooleanSetting disconnect = new BooleanSetting("LogOnArrival",false);
    private final NumberSetting<Integer> dishealth = new NumberSetting<>("LogOnhealth",10,1,36).setVisibility(disconnect::getValue);


    //the varrssss
    List<BlockPos> kek = blocks( gap.getValue()/16, radius.getValue());
    BlockPos cen;
    int i;
    @Override
    public void onEnable(){
        assert mc.player != null;
        cen = mc.player.blockPosition();
    }

    @Override
    public void onDisable(){
        cen=null;
        i=1;
    }
    @Subscribe
    public void onUpdate(EventUpdate event)
    {
        if (cen == null){
            assert mc.player != null;
            cen = mc.player.blockPosition();
        }
        if (kek==null) kek = blocks( gap.getValue()/16, radius.getValue());
        if (mc.player!=null && active.getValue() && cen!=null){
            lookXZ(kek.get(i));
            if(distxz2block(kek.get(i))<=5&&i!=radius.getValue()*4+2){
                i++;
            } else if((i >= kek.size()-1)) {
                active.setValue(false);
                getNotificationManager().chat("Done");
                if (disconnect.getValue()||(dishealth.getValue()>mc.player.getHealth()&&disconnect.getValue())) disconnect();
            }
            // String = String.valueOf(chunks(radius.getValue()));
            // getNotificationManager().chat(string);
        }
        //if(mc.player.getHealth()<10 ||mc.player.getInventory().getArmor(2).getDamageValue()<20){
        //    discnnect();
        //};
    }

    private ArrayList<BlockPos> blocks(int gap,int radius)
    {
        ArrayList<BlockPos> blockies = new ArrayList<>();
        //gen offsets
        for (int i = 2; i <= radius; i++){
            blockies.add(new BlockPos(i,0,i));
            blockies.add(new BlockPos(i,0,-i));
            blockies.add(new BlockPos(-i,0,-i));
            blockies.add(new BlockPos(-i,0,i));
            blockies.add(new BlockPos(i,0,i));
        }
        //add gaps
        blockies.replaceAll(blockPos -> new BlockPos(blockPos.getX()*gap,0,blockPos.getZ()*gap));
        //center
        blockies.replaceAll(blockPos -> new BlockPos(blockPos.getX()+cen.getX(),0,blockPos.getZ()+cen.getZ()));
        return blockies;
    }

    //boilerplate shit
    private void lookXZ(BlockPos b)
    {
        Player p = mc.player;
        double x = p.getX();
        double y = p.getY() + 1;
        double z = p.getZ();
        double dirx = x - b.getX();
        double diry = y - b.getY();
        double dirz = z - b.getZ();
        double len = Math.sqrt(dirx * dirx + diry * diry + dirz * dirz);
        dirx /= len;
        dirz /= len;
        double yaw = Math.atan2(dirz, dirx);
        // to mc degree
        yaw = yaw * 180.0 / Math.PI;
        yaw += 90f;
        p.setYRot((float) yaw);
    }

    private double distxz2block(BlockPos b){
        assert mc.player != null;
        return mc.player.distanceToSqr( b.getX(),mc.player.getY(),b.getZ());
    }

    private void disconnect(){
        Objects.requireNonNull(mc.getConnection()).close();}
}
