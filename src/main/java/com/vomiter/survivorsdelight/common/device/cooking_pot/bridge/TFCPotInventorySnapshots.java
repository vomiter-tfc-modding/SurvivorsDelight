package com.vomiter.survivorsdelight.common.device.cooking_pot.bridge;

import net.dries007.tfc.common.blockentities.PotBlockEntity;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public final class TFCPotInventorySnapshots {

    // 離線用的假鍋，放在 (0,0,0) + 預設鍋方塊態，僅用來做 Recipe 匹配/assemble

    static final class DetachedPot extends PotBlockEntity {

        public DetachedPot() {
            // 用一個不會真的被用到的位置就好，反正不綁 Level
            super(BlockPos.ZERO, TFCBlocks.POT.get().defaultBlockState());
        }

        // 不要綁定真正的 Level，避免任何 world/blockstate 檢查
        @Override
        public void setLevel(@org.jetbrains.annotations.Nullable Level level) {
            // 故意 NO-OP，不呼叫 super.setLevel(level)
        }

        @Override
        public @org.jetbrains.annotations.Nullable Level getLevel() {
            // 對 PotInventory / 配方計算而言通常用不到 level，直接回傳 null 即可
            return null;
        }

        // 避免有人呼叫 setChanged() 時去碰世界
        @Override
        public void setChanged() {
            // NO-OP：離線快照不需要 markDirty
        }

        @Override
        public void updateCachedRecipe(){}

        // 已經保留：快照上永遠視為「尚未開始料理」
        @Override
        public boolean hasRecipeStarted() {
            return false;
        }
    }

    /**
     * 從任意來源建 PotInventory 快照（複製物品&流體進去）。
     * @param items  來源物品欄（讀取前 5 格，對應 TFC POT 的食材欄位）
     * @param fluids 來源流體（會讀第 0 槽）
     * @return 可拿去給 PotRecipe.matches/assemble 使用的 PotInventory
     */
    public static PotBlockEntity.PotInventory snapshot(IItemHandler items, IFluidHandler fluids) {
        DetachedPot pot = new DetachedPot();
        PotBlockEntity.PotInventory inv = new PotBlockEntity.PotInventory(pot); // TFC 鍋會有這個取用器；若沒有，直接 new PotInventory(pot)

        // 1) 先清空快照鍋的物品
        IItemHandlerModifiable dstItems = inv.getItemHandler();
        for (int i = 0; i < dstItems.getSlots(); i++) {
            dstItems.setStackInSlot(i, ItemStack.EMPTY);
        }

        int ingredientSlotIndex = 4;
        for (int i = 0; i < 9; i++) {
            if(i < 4) dstItems.setStackInSlot(i, ItemStack.EMPTY);
            else{
                ItemStack src = items.getStackInSlot(i - 4);
                if (!src.isEmpty()) {
                    dstItems.setStackInSlot(ingredientSlotIndex, src.copy().split(1));
                    ++ingredientSlotIndex;
                }
            }
        }

        // 3) 複製流體（TFC 鍋是 1000 mB Tank，且只允許 USABLE_IN_POT）
        IFluidHandler dstFluid = inv.getFluidHandler();
        // 先清到 0
        FluidStack existing = dstFluid.getFluidInTank(0);
        if (!existing.isEmpty()) {
            dstFluid.drain(existing, IFluidHandler.FluidAction.EXECUTE);
        }
        // 塞入來源第 0 槽的內容
        if (fluids.getTanks() > 0) {
            FluidStack toCopy = fluids.getFluidInTank(0);
            if (!toCopy.isEmpty()) {
                dstFluid.fill(toCopy.copy(), IFluidHandler.FluidAction.EXECUTE);
            }
        }
        return inv;
    }


}
