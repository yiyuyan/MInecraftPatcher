package cn.ksmcbrigade.mp;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class MinecraftPatcher implements PreLaunchEntrypoint {

    @Override
    public void onPreLaunch() {
        CommonClass.init();
    }
}
