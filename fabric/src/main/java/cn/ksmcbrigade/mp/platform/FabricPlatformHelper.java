package cn.ksmcbrigade.mp.platform;

import cn.ksmcbrigade.mp.Constants;
import cn.ksmcbrigade.mp.platform.services.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;

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
}
