package moe.plushie.armourers_workshop.init.platform;

import moe.plushie.armourers_workshop.core.client.model.ItemModelManager;
import moe.plushie.armourers_workshop.core.client.skinrender.SkinRendererManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class RendererManager {

    public static void init() {
        ItemModelManager.init();
        SkinRendererManager.reload();
        //moe.plushie.armourers_workshop.core.skin.animation.molang.runtime.test.CompilerTest.main();
    }
}
