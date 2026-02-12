package cn.ksmcbrigade.mp.platform;

import cn.ksmcbrigade.mp.Constants;
import cn.ksmcbrigade.mp.platform.services.IPlatformHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public String getModVersion() {
        return "1.0";
    }

    @Override
    public String getGameVersion() {
        return "1.21";
    }

    @Override
    public boolean isModLoaded(String modId) {
        try {
            Class<?> modListC = Class.forName("net.neoforged.fml.ModList");

            Method getM = modListC.getMethod("get");
            Method isLoadedM = modListC.getMethod("isLoaded", String.class);

            Object modList = getM.invoke(null);
            return (boolean) isLoadedM.invoke(modList,modId);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            Constants.LOG.info("Failed to invoke ModList::get or ModList::isLoaded.");
            return false;
        }
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        try {
            Class<?> loaderC = Class.forName("net.neoforged.fml.loading.FMLLoader");

            Method isProductionM = loaderC.getMethod("isProduction");
            return !((boolean) isProductionM.invoke(null));
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            Constants.LOG.info("Failed to invoke FMLLoader::isProtection");
            return false;
        }
    }
}
