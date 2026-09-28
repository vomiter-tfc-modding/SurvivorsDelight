package com.vomiter.survivorsdelight.adapter.skillet;

import net.minecraft.world.item.ItemStack;

public interface ISkilletItemCookingData {
    ItemStack getCooking();
    void setCooking(ItemStack stack);
    float getTargetTemp();
    void setTargetTemp(float f);

    default void clear(){
        setCooking(ItemStack.EMPTY);
        setTargetTemp(0);
        if ((Object)this instanceof ItemStack itemStack){
            var tag = itemStack.getTag();
            if (tag != null && tag.contains("Cooking")) {
                tag.remove("Cooking");
                tag.remove("CookTimeHandheld");
            }
        }
    }
}
