package ru.stash.hunter;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import org.rusherhack.client.api.events.client.EventUpdate;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.BooleanSetting;
import org.rusherhack.core.setting.NumberSetting;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.rusherhack.client.api.RusherHackAPI.*;

public class StashHunterModule extends ToggleableModule {

    private final NumberSetting<Integer> gap = new NumberSetting<>("Gap", "In blocks", 8, 1, 64);
    private final NumberSetting<Integer> radius = new NumberSetting<>("Radius", "in gaps", 4, 1, 32);
    private final BooleanSetting active = new BooleanSetting("Active", false);

    private final BooleanSetting disconnect = new BooleanSetting("LogOnArrival", false);
    private final NumberSetting<Integer> dishealth = new NumberSetting<>("LogOnHealth", 10, 1, 36)
            .setVisibility(disconnect::getValue);

    public StashHunterModule() {
        super("StashHunter", "Auromatically scans territory", ModuleCategory.PLAYER);
        registerSettings(gap, active, radius, disconnect);
        disconnect.addSubSettings(dishealth);
    }

    private List<BlockPos> kek;
    private BlockPos center;
    private int index;

    @Override
    public void onEnable() {
        assert mc.player != null : "Player is null";
        center = mc.player.blockPosition();
        resetScan();
    }

    @Override
    public void onDisable() {
        center = null;
        resetScan();
    }

    @Subscribe
    public void onUpdate(EventUpdate event) {
        if (center == null) {
            assert mc.player != null : "Player is null";
            center = mc.player.blockPosition();
        }
        updateScan();
    }

    private void resetScan() {
        kek = blocks(gap.getValue() / 16, radius.getValue());
        index = 0;
    }

    private void updateScan() {
        if (mc.player != null && active.getValue() && center != null) {
            lookAt(kek.get(index));
            if (distanceToBlock(kek.get(index)) <= 5 && index < kek.size()) {
                index++;
            } else if (index >= kek.size()) {
                finishScan();
            }
        }
    }

    private void finishScan() {
        active.setValue(false);
        getNotificationManager().chat("Done");
        if (shouldDisconnect()) {
            disconnect();
        }
    }

    private boolean shouldDisconnect() {
        return disconnect.getValue() || (mc.player.getHealth() <= dishealth.getValue() && disconnect.getValue());
    }

    private ArrayList<BlockPos> blocks(int gap, int radius) {
        ArrayList<BlockPos> blockies = new ArrayList<>();
        for (int i = 2; i <= radius; i++) {
            addBlockPositions(blockies, i);
        }
        applyGap(blockies, gap);
        centerBlocks(blockies);
        return blockies;
    }

    private void addBlockPositions(ArrayList<BlockPos> blockies, int distance) {
        blockies.add(new BlockPos(distance, 0, distance));
        blockies.add(new BlockPos(distance, 0, -distance));
        blockies.add(new BlockPos(-distance, 0, -distance));
        blockies.add(new BlockPos(-distance, 0, distance));
    }

    private void applyGap(ArrayList<BlockPos> blockies, int gap) {
        blockies.replaceAll(blockPos -> new BlockPos(blockPos.getX() * gap, 0, blockPos.getZ() * gap));
    }

    private void centerBlocks(ArrayList<BlockPos> blockies) {
        blockies.replaceAll(blockPos -> new BlockPos(blockPos.getX() + center.getX(), 0, blockPos.getZ() + center.getZ()));
    }

    private void lookAt(BlockPos block) {
        double yaw = calculateYaw(block);
        mc.player.setYRot((float) yaw);
    }

    private double calculateYaw(BlockPos block) {
        Player player = mc.player;
        double dx = block.getX() - player.getX();
        double dz = block.getZ() - player.getZ();
        return (float) Math.toDegrees(Math.atan2(dz, dx)) + 90f;
    }

    private double distanceToBlock(BlockPos block) {
        assert mc.player != null : "Player is null";
        return mc.player.distanceToSqr(block.getX(), mc.player.getY(), block.getZ());
    }

    private void disconnect() {
        Objects.requireNonNull(mc.getConnection()).close();
    }
}