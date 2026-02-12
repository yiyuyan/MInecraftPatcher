package cn.ksmcbrigade.mp.platform;

import cn.ksmcbrigade.mp.Constants;
import cn.ksmcbrigade.mp.platform.services.IPlatformHelper;

import java.io.File;
import java.util.ServiceLoader;

// Service loaders are a built-in Java feature that allow us to locate implementations of an interface that vary from one
// environment to another. In the context of MultiLoader we use this feature to access a mock API in the common code that
// is swapped out for the platform specific implementation at runtime.
public class Services {

    // In this example we provide a platform helper which provides information about what platform the mod is running on.
    // For example this can be used to check if the code is running on Forge vs Fabric, or to ask the modloader if another
    // mod is loaded.
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    // This code is used to load a service for the current environment. Your implementation of the service must be defined
    // manually by including a text file in META-INF/services named with the fully qualified class name of the service.
    // Inside the file you should write the fully qualified class name of the implementation to load for the platform. For
    // example our file on Forge points to ForgePlatformHelper while Fabric points to FabricPlatformHelper.
    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz).findFirst().orElse((T) new IPlatformHelper(){

            public String checkPlatform(File dir){
                File[] files = dir.listFiles();
                if(files!=null){
                    for (File file1 : files) {
                        if(file1.getName().toLowerCase().contains("fabric")) return "Fabric";
                        if(file1.getName().toLowerCase().contains("neoforge")) return "NeoForge";
                        if(file1.getName().toLowerCase().contains("forge")) return "Forge";
                        if(file1.isDirectory()){
                            return checkPlatform(file1);
                        }
                    }
                }
                return "None";
            }

            @Override
            public String getPlatformName() {
                return checkPlatform(new File(System.getProperty("user.dir")));
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
                return false;
            }

            @Override
            public boolean isDevelopmentEnvironment() {
                File file = new File(System.getProperty("user.dir"));
                if(file.getName().equals("run")) return true;
                return new File(System.getProperty("user.dir")).getParentFile().getParentFile().getName().equals("run");
            }

        });
        Constants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}
