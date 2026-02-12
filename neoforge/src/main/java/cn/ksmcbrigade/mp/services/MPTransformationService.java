package cn.ksmcbrigade.mp.services;

import cn.ksmcbrigade.mp.CommonClass;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class MPTransformationService implements ITransformationService {

    static {
        CommonClass.init();
    }

    @Override
    public @NotNull String name() {
        return "MPAgentLoader";
    }

    @Override
    public void initialize(IEnvironment iEnvironment) {}

    @Override
    public void onLoad(IEnvironment iEnvironment, Set<String> set) {

    }

    @Override
    public @NotNull List<? extends ITransformer<?>> transformers() {
        return List.of();
    }
}
