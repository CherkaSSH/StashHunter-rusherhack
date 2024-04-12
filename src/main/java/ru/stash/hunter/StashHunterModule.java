package ru.stash.hunter;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.rusherhack.client.api.events.client.EventUpdate;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.BooleanSetting;
import org.rusherhack.core.setting.NumberSetting;

import java.util.ArrayList;
import java.util.List;

public class StashHunterModule extends ToggleableModule {
    public StashHunterModule(){
        super("StashHunter","Auromatically scans territory", ModuleCategory.PLAYER);
        //settings
        registerSettings(gap, active);
    }
    //setting
    private final NumberSetting<Integer> gap = new NumberSetting<>("Gap",4,0,10);
    private final NumberSetting<Integer> radius = new NumberSetting<>("Radius in gaps",4,1,32);
    private final BooleanSetting active = new BooleanSetting("Active",false);

    private Vec3 cen;

    @Override
    public void onEnable(){
        if (cen == null){
            assert mc.player != null;
            cen = new Vec3(mc.player.getX(),mc.player.getY(),mc.player.getZ());
        };
    }
    @Subscribe
    public void onTick(EventUpdate event){
        if(active.getValue()){
            lookXZ(blocks().get(0));
        }
        if(new BlockPos((int) blocks().get(0).x, (int) blocks().get(0).y, (int) blocks().get(0).z)
                .distSqr(new BlockPos((int) blocks().get(0).x, (int) blocks().get(0).y, (int) blocks().get(0).z))>=10){
            blocks().remove(0);
        }
    }
    private @NotNull List<Vec3> genOffsets(){
        List<Vec3> list = new ArrayList<>();
        for(int i = 0; i<radius.getValue() ; i++){
            list.add(new Vec3(getSpiralX(i),200,getSpiralZ(i)));
        }
        return list;
    }

    private @NotNull List<Vec3> blocks(){
        List<Vec3> list = new ArrayList<>();
        for(int i = 0;genOffsets().size()>=i;i++){
            list.add(new Vec3(genOffsets().get(i).x+cen.x(),200,genOffsets().get(i).z+cen.z()));
        }
        return list;
    }
    private void lookXZ(Vec3 pos){
        if (!isPlayerNull()){
            mc.player.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(pos.x,pos.y,pos.y));
        }
    }

    private double getSpiralX(double x){
        return gap.getValue()*16*x*Math.cos(x);
    }
    private double getSpiralZ(double z){
        return gap.getValue()*16*z*Math.sin(z);
    }
    private boolean isPlayerNull(){
        return mc.player==null;
    }
}
