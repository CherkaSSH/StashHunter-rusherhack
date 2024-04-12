package ru.stash.hunter;

import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.plugin.Plugin;

public class StashHunterPlugin extends Plugin{

    @Override
    public void onLoad() {
        this.getLogger().info("StashHunter loaded");
        final StashHunterModule hunter = new StashHunterModule();
        //module
        RusherHackAPI.getModuleManager().registerFeature(hunter);
        //command
    }

    @Override
    public void onUnload() {
        this.getLogger().info("StashHunter unloaded");
    }

}