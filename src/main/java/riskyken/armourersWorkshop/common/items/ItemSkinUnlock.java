package riskyken.armourersWorkshop.common.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import riskyken.armourersWorkshop.api.common.skin.type.ISkinType;
import riskyken.armourersWorkshop.common.lib.LibItemNames;
import riskyken.armourersWorkshop.common.skin.ExPropsPlayerSkinData;
import riskyken.armourersWorkshop.common.skin.type.SkinTypeRegistry;

public class ItemSkinUnlock extends AbstractModItem {

    private final ISkinType[] VALID_SKINS = {
            SkinTypeRegistry.skinHead,
            SkinTypeRegistry.skinChest,
            SkinTypeRegistry.skinLegs,
            SkinTypeRegistry.skinFeet,
            SkinTypeRegistry.skinWings
            };
    
    public ItemSkinUnlock() {
        super(LibItemNames.SKIN_UNLOCK);
        setHasSubtypes(true);
        setSortPriority(7);
    }
    
    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            for (int i = 0; i < VALID_SKINS.length;i++) {
                items.add(new ItemStack(this, 1, i));
            } 
        }
    }
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack itemStack = playerIn.getHeldItem(handIn);
        if (worldIn.isRemote) {
            return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStack);
        }
        
        ISkinType skinType = getSkinTypeFormStack(playerIn.getHeldItem(handIn));
        
        ExPropsPlayerSkinData equipmentData = ExPropsPlayerSkinData.get(playerIn);
        int count = equipmentData.getEquipmentWardrobeData().getUnlockedSlotsForSkinType(skinType);
        count++;
        
        String localizedSkinName = SkinTypeRegistry.INSTANCE.getLocalizedSkinTypeName(skinType);
        
        if (count <= ExPropsPlayerSkinData.MAX_SLOTS_PER_SKIN_TYPE) {
            equipmentData.setSkinColumnCount(skinType, count);
            playerIn.sendMessage(new TextComponentTranslation("chat.armourersworkshop:slotUnlocked", localizedSkinName.toLowerCase(), Integer.toString(count)));
            itemStack.shrink(1);
        } else {
            playerIn.sendMessage(new TextComponentTranslation("chat.armourersworkshop:slotUnlockedFailed", localizedSkinName));
        }
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }
    
    private ISkinType getSkinTypeFormStack(ItemStack itemStack) {
        int damage = itemStack.getItemDamage();
        if (damage >= 0 & damage < VALID_SKINS.length) {
            return VALID_SKINS[damage];
        }
        return VALID_SKINS[0];
    }
}
