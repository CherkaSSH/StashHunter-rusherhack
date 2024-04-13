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
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static org.rusherhack.client.api.RusherHackAPI.*;

public class StashHunterModule extends ToggleableModule {
    public StashHunterModule(){
        super("StashHunter","Auromatically scans territory", ModuleCategory.PLAYER);
        //settings
        registerSettings(gap, active, radius);
    }
    //setting
    private final NumberSetting<Integer> gap = new NumberSetting<>("Gap","In blocks",128,0,1024).incremental(16);
    private final NumberSetting<Integer> radius = new NumberSetting<>("Radius","in gaps",4,1,32);
    private final BooleanSetting active = new BooleanSetting("Active",false);

    private BlockPos cen;
    private int i;

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
    public void onUpdate(EventUpdate event){
        if (cen == null){
            assert mc.player != null;
            cen = mc.player.blockPosition();
        }
        if (mc.player!=null && active.getValue() && cen!=null){
            List<BlockPos> kek = blocks(radius.getValue()*16);
            lookXZ(kek.get(i));
            getNotificationManager().chat("going to "+kek.get(i));
            if(disxz2block(kek.get(i))<=5&&i!=radius.getValue()*4+2){
                i++;
            } else if((i >= kek.size())) {
                active.setValue(false);
                getNotificationManager().chat("Done");
            }
            // String string = String.valueOf(chunks(radius.getValue()));
            // getNotificationManager().chat(string);
        }
        //if(mc.player.getHealth()<10 ||mc.player.getInventory().getArmor(2).getDamageValue()<20){
        //    discnnect();
        //};
    }

    private ArrayList<BlockPos> blocks(int gap){
        ArrayList<BlockPos> blockies= new ArrayList<>();
        //doing 1st circle manually cuz why not
        blockies.add(new BlockPos(0,0,0));
        blockies.add(new BlockPos(0,0,-1));
        blockies.add(new BlockPos(-1,0,-1));
        blockies.add(new BlockPos(-1,0,1));
        blockies.add(new BlockPos(2,0,1));
        //everything else is being done automatically
        for (int i = 2; i <= radius.getValue(); i++){
            blockies.add(new BlockPos(i,0,-i));
            blockies.add(new BlockPos(-i,0,-i));
            blockies.add(new BlockPos(-i,0,i));
            blockies.add(new BlockPos(i++,0,i));
        }
        //add gaps
        blockies.replaceAll(blockPos -> new BlockPos(blockPos.getX()*gap,0,blockPos.getZ()*gap));
        //center to cen
        blockies.replaceAll(blockPos -> new BlockPos(blockPos.getX()+cen.getX(),0,blockPos.getZ()+cen.getZ()));
        return blockies;
    }

    //boilerplate shit
    private void lookXZ(BlockPos b) {
        Player p = mc.player;
        int x = (int) p.getX();
        int y = (int) p.getY() + 1;
        int z = (int) p.getZ();
        double dirx = x - b.getX();
        double diry = y - b.getY();
        double dirz = z - b.getZ();
        double len = Math.sqrt(dirx * dirx + diry * diry + dirz * dirz);
        dirx /= len;
        dirz /= len;
        double yaw = Math.atan2(dirz, dirx);
        // to degree
        yaw = yaw * 180.0 / Math.PI;
        yaw += 90f;
        p.setYRot((float) yaw);
    }

    private double disxz2block(BlockPos b){
        assert mc.player != null;
        return mc.player.distanceToSqr( b.getX(),mc.player.getY(),b.getZ());
    }

    //private void discnnect(){
    //    Socket socket = new Socket();
    //    try {
    //        socket.close();
    //    } catch (IOException ignored) {}
    //}
}
