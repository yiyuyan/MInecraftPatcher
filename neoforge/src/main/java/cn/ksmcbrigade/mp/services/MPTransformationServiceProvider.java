package cn.ksmcbrigade.mp.services;

import cn.ksmcbrigade.mp.CommonClass;
import cpw.mods.modlauncher.api.NamedPath;
import cpw.mods.modlauncher.serviceapi.ITransformerDiscoveryService;

import java.nio.file.Path;
import java.util.List;

public class MPTransformationServiceProvider implements ITransformerDiscoveryService {
    @Override
    public List<NamedPath> candidates(Path gameDirectory) {
        return List.of();
    }

    @Override
    public void earlyInitialization(String launchTarget, String[] arguments) {
        CommonClass.init();
    }
}
