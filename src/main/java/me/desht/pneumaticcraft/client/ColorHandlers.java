package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.client.util.TintColor;
import me.desht.pneumaticcraft.common.block.BlockAphorismTile;
import me.desht.pneumaticcraft.common.block.BlockPneumaticCraftCamo;
import me.desht.pneumaticcraft.common.block.BlockUVLightBox;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.item.IColorableItem;
import me.desht.pneumaticcraft.common.tileentity.ICamouflageableTE;
import me.desht.pneumaticcraft.common.tileentity.IHeatTinted;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAbstractHopper;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAphorismTile;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.block.Block;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ColorHandlers {

    @SubscribeEvent
    public static void registerItemColorHandlers(ColorHandlerEvent.Item event) {
        for (Item item : ModItems.Registration.ALL_ITEMS) {
            if (item instanceof IColorableItem) {
                event.getItemColors().register(((IColorableItem) item)::getTintColor, item);
            }
        }

        event.getItemColors().register((stack, tintIndex) -> {
            int n = UpgradableItemUtils.getUpgrades(stack, EnumUpgrade.CREATIVE);
            return n > 0 ? 0xFFFF60FF : 0xFFFFFFFF;
        }, ModBlocks.OMNIDIRECTIONAL_HOPPER.asItem(), ModBlocks.LIQUID_HOPPER.asItem());

        event.getItemColors().register((stack, tintIndex) -> {
            switch (tintIndex) {
                case 0: // border
                    return DyeColor.byId(BlockAphorismTile.getBorderColor(stack)).getColorValue();
                case 1: // background
                    return desaturate(DyeColor.byId(BlockAphorismTile.getBackgroundColor(stack)).getColorValue());
                default:
                    return 0xFFFFFF;
            }
        }, ModBlocks.APHORISM_TILE.asItem());
    }

    @SubscribeEvent
    public static void registerBlockColorHandlers(ColorHandlerEvent.Block event) {
        event.getBlockColors().register((state, blockAccess, pos, tintIndex) -> {
            if (blockAccess != null && pos != null) {
                TileEntity te = blockAccess.getTileEntity(pos);
                int heatLevel = te instanceof IHeatTinted ? ((IHeatTinted) te).getHeatLevelForTintIndex(tintIndex) : 10;
                float[] color = HeatUtil.getColorForHeatLevel(heatLevel);
                return 0xFF000000 + ((int) (color[0] * 255) << 16) + ((int) (color[1] * 255) << 8) + (int) (color[2] * 255);
            }
            return 0xFFFFFFFF;
        }, ModBlocks.COMPRESSED_IRON_BLOCK, ModBlocks.HEAT_SINK, ModBlocks.VORTEX_TUBE, ModBlocks.THERMAL_COMPRESSOR);

        event.getBlockColors().register((state, blockAccess, pos, tintIndex) -> {
            if (blockAccess != null && pos != null) {
                boolean lightsOn = blockAccess.getBlockState(pos).get(BlockUVLightBox.LIT);
                return lightsOn ? 0xFF4000FF : 0xFFAFAFE4;
            }
            return 0xFFAFAFE4;
        }, ModBlocks.UV_LIGHT_BOX);

        event.getBlockColors().register((state, blockAccess, pos, tintIndex) -> {
            if (blockAccess != null && pos != null) {
                TileEntity te = blockAccess.getTileEntity(pos);
                if (te instanceof TileEntityAbstractHopper) {
                    return ((TileEntityAbstractHopper) te).isCreative ? 0xFFFF80FF : 0xFFFFFFFF;
                }
            }
            return 0xFFFFFFFF;
        }, ModBlocks.OMNIDIRECTIONAL_HOPPER, ModBlocks.LIQUID_HOPPER);

        for (Block b : ModBlocks.Registration.ALL_BLOCKS) {
            if (b instanceof BlockPneumaticCraftCamo) {
                event.getBlockColors().register((state, blockAccess, pos, tintIndex) -> {
                    if (blockAccess != null && pos != null) {
                        TileEntity te = blockAccess.getTileEntity(pos);
                        if (te instanceof ICamouflageableTE && ((ICamouflageableTE) te).getCamouflage() != null) {
                            return event.getBlockColors().getColor(((ICamouflageableTE) te).getCamouflage(), te.getWorld(), pos, tintIndex);
                        }
                    }
                    return 0xFFFFFFFF;
                }, b);
            }
        }

        event.getBlockColors().register((state, worldIn, pos, tintIndex) -> {
            if (worldIn != null && pos != null) {
                TileEntity te = worldIn.getTileEntity(pos);
                if (te instanceof TileEntityAphorismTile) {
                    switch (tintIndex) {
                        case 0: // border
                            return DyeColor.byId(((TileEntityAphorismTile) te).getBorderColor()).getColorValue();
                        case 1: // background
                            return desaturate(DyeColor.byId(((TileEntityAphorismTile) te).getBackgroundColor()).getColorValue());
                    }
                }
            }
            return 0xFFFFFFFF;
        }, ModBlocks.APHORISM_TILE);
    }

    private static int desaturate(int c) {
        float[] hsb = TintColor.RGBtoHSB((c & 0xFF0000) >> 16, (c & 0xFF00) >> 8, c & 0xFF, null);
        TintColor color = TintColor.getHSBColor(hsb[0], hsb[1] * 0.4f, hsb[2]);
        if (hsb[2] < 0.7) color = color.brighter();
        return color.getRGB();
    }
}