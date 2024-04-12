package ru.stash.hunter;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import org.rusherhack.client.api.events.client.EventUpdate;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.BooleanSetting;
import org.rusherhack.core.setting.NumberSetting;

import java.util.ArrayList;
import java.util.List;

import static org.rusherhack.client.api.RusherHackAPI.getNotificationManager;
import static org.rusherhack.client.api.RusherHackAPI.getRotationManager;

public class StashHunterModule extends ToggleableModule {
    public StashHunterModule(){
        super("StashHunter","Auromatically scans territory", ModuleCategory.PLAYER);
        //settings
        registerSettings(gap, active, looky, radius);
    }
    //setting
    private final NumberSetting<Integer> gap = new NumberSetting<>("Gap",4,0,10);
    private final NumberSetting<Integer> radius = new NumberSetting<>("Radius in gaps",4,1,32);
    private final NumberSetting<Integer> looky = new NumberSetting<>("YLevel",200,-64,320);
    private final BooleanSetting active = new BooleanSetting("Active",false);

    private ChunkPos cen;
    private int i;

    @Override
    public void onEnable(){
        if (cen == null){
            assert mc.player != null;
            cen = mc.player.chunkPosition();
            System.out.println(chunks(radius.getValue()));
        };
    }

    @Override
    public void onDisable(){
        cen=null;
    }
    @Subscribe
    public void onUpdate(EventUpdate event){
        if (mc.player!=null &&active.getValue()){
            List<ChunkPos> kek = chunks(radius.getValue());
            lookXZ(kek.get(i));
            if(dis2chunk(kek.get(i))<=10){
                i++;
            }
            String string = String.valueOf(chunks(radius.getValue()));
            //getNotificationManager().chat(string);
        }
    }

    private ArrayList<ChunkPos> chunks(int gap){
        ArrayList<ChunkPos> chunkies = new ArrayList<>();
        chunkies.add(new ChunkPos(0,0));
        //create diagonal chunks
        for (int i = 1; i <= radius.getValue(); i++) {
            chunkies.add(new ChunkPos(i,-i));
            chunkies.add(new ChunkPos(-i,-i));
            chunkies.add(new ChunkPos(-i,i));
            chunkies.add(new ChunkPos(i,i));
        }
        //add gaps
        chunkies.replaceAll(chunkPos -> new ChunkPos(chunkPos.x*gap, chunkPos.z*gap));
        //move ++ dir by 1 gap
        for (int i = 0; i < chunkies.size(); i++) {
            if (i%4==0){
                chunkies.set(i,new ChunkPos(chunkies.get(i).x+gap,chunkies.get(i).z+gap));
            }
        }
        //apply offset to cen
        chunkies.replaceAll(chunkPos -> new ChunkPos(chunkPos.x+cen.x,chunkPos.z+cen.z));
        return chunkies;
    }

    private void lookXZ(ChunkPos c) {
        Player p = mc.player;
        int x = (int) p.getX();
        int y = (int) p.getY() + 1;
        int z = (int) p.getZ();
        double dirx = x - c.x*16;
        double diry = y - looky.getValue();
        double dirz = z - c.z*16;
        double len = Math.sqrt(dirx * dirx + diry * diry + dirz * dirz);
        dirx /= len;
        diry /= len;
        dirz /= len;
        double pitch = Math.asin(diry);
        double yaw = Math.atan2(dirz, dirx);
        // to degree
        pitch = pitch * 180.0 / Math.PI;
        yaw = yaw * 180.0 / Math.PI;
        yaw += 90f;
        p.setXRot((float) pitch);
        p.setYRot((float) yaw);
    }

    private double dis2chunk(ChunkPos c){
        return mc.player.distanceToSqr( c.x*16,mc.player.getY(),c.z*16);
    }
}
