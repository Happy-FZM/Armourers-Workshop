package moe.plushie.armourers_workshop.builder.network;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import moe.plushie.armourers_workshop.api.network.IFriendlyByteBuf;
import moe.plushie.armourers_workshop.api.network.IServerPacketHandler;
import moe.plushie.armourers_workshop.builder.blockentity.AdvancedBuilderBlockEntity;
import moe.plushie.armourers_workshop.builder.menu.AdvancedBuilderMenu;
import moe.plushie.armourers_workshop.core.network.CustomPacket;
import moe.plushie.armourers_workshop.core.skin.Skin;
import moe.plushie.armourers_workshop.core.skin.SkinLoader;
import moe.plushie.armourers_workshop.core.skin.serializer.SkinSerializer;
import moe.plushie.armourers_workshop.core.skin.serializer.document.SkinDocumentNode;
import moe.plushie.armourers_workshop.init.ModLog;
import moe.plushie.armourers_workshop.init.ModPermissions;
import moe.plushie.armourers_workshop.library.data.SkinLibraryManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class AdvancedImportPacket extends CustomPacket {

    private final BlockPos pos;
    private final Skin skin;
    private final String target;

    public AdvancedImportPacket(AdvancedBuilderBlockEntity blockEntity, Skin skin, String target) {
        this.pos = blockEntity.getBlockPos();
        this.skin = skin;
        this.target = target;
    }

    public AdvancedImportPacket(IFriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.target = buffer.readUtf();
        this.skin = decodeSkin(buffer);
    }

    @Override
    public void encode(IFriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeUtf(target);
        encodeSkin(buffer);
    }

    @Override
    public void accept(IServerPacketHandler packetHandler, ServerPlayer player) {
        // this is an unauthorized operation, ignore it
        var blockEntity = player.getLevel().getBlockEntity(pos);
        if (!(blockEntity instanceof AdvancedBuilderBlockEntity blockEntity1) || !(player.containerMenu instanceof AdvancedBuilderMenu) || skin == null) {
            abort(player, "unauthorized", "user status is incorrect or the skin is invalid");
            return;
        }
        var server = SkinLibraryManager.getServer();
        if (!server.shouldUploadFile(player)) {
            abort(player, "import", "uploading prohibited in the config file");
            return;
        }
        if (!ModPermissions.ADVANCED_SKIN_BUILDER_SKIN_IMPORT.accept(player)) {
            abort(player, "import", "prohibited by the config file");
            return;
        }
        if (!skin.getSettings().isEditable()) {
            abort(player, "import", "prohibited by the skin can't editing.");
            return;
        }
        SkinDocumentNode node = null;
        if (!target.isEmpty()) {
            node = blockEntity1.getDocument().nodeById(target);
            if (node == null) {
                abort(player, "import", "can't found node.");
                return;
            }
        }
        accept(player, "import");
        String identifier = SkinLoader.getInstance().saveSkin("", skin);
        if (node != null) {
            blockEntity1.importToNode(identifier, skin, node);
        } else {
            blockEntity1.importToDocument(identifier, skin);
        }
    }

    private void accept(Player player, String op) {
        ModLog.info("accept {} request of the '{}'", op, player.getScoreboardName());
    }

    private void abort(Player player, String op, String reason) {
        ModLog.info("abort {} request of the '{}', reason: '{}'", op, player.getScoreboardName(), reason);
    }

    private void encodeSkin(IFriendlyByteBuf buffer) {
        try (var outputStream = new GZIPOutputStream(new ByteBufOutputStream(buffer.asByteBuf()))) {
            SkinSerializer.writeToStream(skin, null, outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Skin decodeSkin(IFriendlyByteBuf buffer) {
        try (var inputStream = new GZIPInputStream(new ByteBufInputStream(buffer.asByteBuf()))) {
            return SkinSerializer.readFromStream(null, inputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
