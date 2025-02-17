package moe.plushie.armourers_workshop.core.client.other;

import com.mojang.blaze3d.platform.NativeImage;
import moe.plushie.armourers_workshop.api.core.IResourceLocation;
import moe.plushie.armourers_workshop.core.data.PlayerTexture;
import moe.plushie.armourers_workshop.core.skin.texture.SkinPaintColor;
import moe.plushie.armourers_workshop.core.skin.texture.SkinPaintData;
import moe.plushie.armourers_workshop.core.utils.Objects;
import moe.plushie.armourers_workshop.utils.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public class SkinDynamicTexture extends DynamicTexture {

    private final TextureManager textureManager;
    private SkinPaintData paintData;
    private NativeImage downloadedImage;

    private IResourceLocation refer;
    private AbstractTexture referTexture;

    private boolean needsUpdate = true;

    public SkinDynamicTexture() {
        super(PlayerTexture.TEXTURE_WIDTH, PlayerTexture.TEXTURE_HEIGHT, true);
        this.textureManager = Minecraft.getInstance().getTextureManager();
    }

    public IResourceLocation getRefer() {
        return refer;
    }

    public void setRefer(IResourceLocation refer) {
        if (!Objects.equals(this.refer, refer)) {
            this.refer = refer;
            this.referTexture = Objects.flatMap(refer, it -> textureManager.getTexture(it.toLocation()));
            this.downloadedImage = null;
            this.setNeedsUpdate();
        }
    }

    public SkinPaintData getPaintData() {
        return paintData;
    }

    public void setPaintData(SkinPaintData paintData) {
        if (this.paintData != paintData) {
            this.paintData = paintData;
            this.setNeedsUpdate();
        }
    }

    @Override
    public void upload() {
        var downloadedImage = getDownloadedImage();
        var mergedImage = getPixels();
        if (mergedImage == null || downloadedImage == null) {
            return;
        }
        mergedImage.copyFrom(downloadedImage);
        if (paintData != null) {
            applyPaintColor(mergedImage);
        }
        super.upload();
    }

    private void setNeedsUpdate() {
        this.needsUpdate = true;
        RenderSystem.recordRenderCall(() -> {
            if (this.needsUpdate) {
                this.needsUpdate = false;
                this.upload();
            }
        });
    }

    private void applyPaintColor(NativeImage mergedImage) {
        for (var iy = 0; iy < paintData.getHeight(); ++iy) {
            for (var ix = 0; ix < paintData.getWidth(); ++ix) {
                var color = paintData.getColor(ix, iy);
                if (SkinPaintColor.isOpaque(color)) {
                    var r = color >> 16 & 0xff;
                    var g = color >> 8 & 0xff;
                    var b = color & 0xff;
                    var fixed = b << 16 | g << 8 | r;  // ARGB => ABGR
                    mergedImage.setPixelRGBA(ix, iy, 0xff000000 | fixed);
                }
            }
        }
    }

    private NativeImage getDownloadedImage() {
        if (downloadedImage != null) {
            return downloadedImage;
        }
        if (referTexture != null) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, referTexture.getId());
            downloadedImage = new NativeImage(PlayerTexture.TEXTURE_WIDTH, PlayerTexture.TEXTURE_HEIGHT, true);
            downloadedImage.downloadTexture(0, false);
        }
        return downloadedImage;
    }

    // TODO: @SAGESSE replace to new impl.
//    @Override
//    protected void finalize() throws Throwable {
//        close();
//        super.finalize();
//    }
}
