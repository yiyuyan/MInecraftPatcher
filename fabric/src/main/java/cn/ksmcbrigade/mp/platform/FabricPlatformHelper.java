package cn.ksmcbrigade.mp.platform;

import cn.ksmcbrigade.mp.Constants;
import cn.ksmcbrigade.mp.platform.services.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.discovery.RuntimeModRemapper;
import net.fabricmc.loader.impl.launch.FabricLauncher;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.fabricmc.loader.impl.lib.mappingio.tree.MappingTree;

import java.util.Objects;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public String getModVersion() {
        return FabricLoader.getInstance().getModContainer(Constants.MOD_ID).get().getMetadata().getVersion().getFriendlyString();
    }

    @Override
    public String getGameVersion() {
        return FabricLoader.getInstance().getModContainer("minecraft").get().getMetadata().getVersion().getFriendlyString();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public String getObfName(String name) {
        return name;
    }
}
