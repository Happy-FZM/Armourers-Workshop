package moe.plushie.armourers_workshop.core.skin.part.advanced;

import moe.plushie.armourers_workshop.core.math.Rectangle3i;
import moe.plushie.armourers_workshop.core.math.Vector3i;
import moe.plushie.armourers_workshop.core.skin.part.SkinPartType;

public class AdvancedPartType extends SkinPartType {

    public AdvancedPartType() {
        super();
        this.buildingSpace = new Rectangle3i(-32, -32, -32, 64, 64, 64);
        this.guideSpace = new Rectangle3i(0, 0, 0, 0, 0, 0);
        this.offset = new Vector3i(0, 0, 0);
    }

    @Override
    public int getMinimumMarkersNeeded() {
        return 0;
    }

    @Override
    public int getMaximumMarkersNeeded() {
        return 0;
    }
}
