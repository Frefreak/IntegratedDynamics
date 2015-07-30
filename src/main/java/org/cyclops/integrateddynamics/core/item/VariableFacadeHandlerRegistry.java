package org.cyclops.integrateddynamics.core.item;

import com.google.common.collect.Maps;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.cyclops.cyclopscore.helper.ItemStackHelpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.core.client.model.VariableModelBaked;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.network.Network;
import org.cyclops.integrateddynamics.core.part.write.IPartStateWriter;

import java.util.List;
import java.util.Map;

/**
 * The variable facade handler registry.
 * @author rubensworks
 */
public class VariableFacadeHandlerRegistry implements IVariableFacadeHandlerRegistry {

    private static VariableFacadeHandlerRegistry INSTANCE = new VariableFacadeHandlerRegistry();
    private static DummyVariableFacade DUMMY_FACADE = new DummyVariableFacade("variable.error.invalidItem");

    private final Map<String, IVariableFacadeHandler> handlers = Maps.newHashMap();

    private VariableFacadeHandlerRegistry() {

    }

    /**
     * @return The unique instance.
     */
    public static VariableFacadeHandlerRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public void registerHandler(IVariableFacadeHandler variableFacadeHandler) {
        handlers.put(variableFacadeHandler.getTypeId(), variableFacadeHandler);
    }

    @Override
    public IVariableFacade handle(NBTTagCompound tagCompound) {
        if(tagCompound == null) {
            return DUMMY_FACADE;
        }
        if(!tagCompound.hasKey("_type", MinecraftHelpers.NBTTag_Types.NBTTagString.ordinal())
                || !tagCompound.hasKey("_id", MinecraftHelpers.NBTTag_Types.NBTTagInt.ordinal())) {
            return DUMMY_FACADE;
        }
        String type = tagCompound.getString("_type");
        int id = tagCompound.getInteger("_id");
        IVariableFacadeHandler handler = handlers.get(type);
        if(handler != null) {
            return handler.getVariableFacade(id, tagCompound);
        }
        return DUMMY_FACADE;
    }

    @Override
    public <F extends IVariableFacade> void write(NBTTagCompound tagCompound, F variableFacade, IVariableFacadeHandler<F> handler) {
        tagCompound.setString("_type", handler.getTypeId());
        tagCompound.setInteger("_id", variableFacade.getId());
        handler.setVariableFacade(tagCompound, variableFacade);
    }

    @Override
    public <F extends IVariableFacade> ItemStack writeVariableFacade(boolean generateId, ItemStack itemStack, IVariableFacadeHandler<F> variableFacadeHandler, IVariableFacadeFactory<F> variableFacadeFactory) {
        if(itemStack == null) {
            return null;
        }
        itemStack = itemStack.copy();
        NBTTagCompound tag = ItemStackHelpers.getSafeTagCompound(itemStack);
        IVariableFacade previousVariableFacade = this.handle(tag);
        F variableFacade;
        if(generateId && previousVariableFacade.getId() > -1) {
            variableFacade = variableFacadeFactory.create(previousVariableFacade.getId());
        } else {
            variableFacade = variableFacadeFactory.create(generateId);
        }
        this.write(tag, variableFacade, variableFacadeHandler);
        return itemStack;
    }

    /**
     * Variable facade used for items that have no (valid) information on them.
     */
    public static class DummyVariableFacade extends VariableFacadeBase {

        private final String unlocalizedError;

        public DummyVariableFacade(String unlocalizedError) {
            super(false);
            this.unlocalizedError = unlocalizedError;
        }

        @Override
        public <V extends IValue> IVariable<V> getVariable(Network network) {
            return null;
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public void validate(Network network, IPartStateWriter validator) {
            validator.addError(validator.getActiveAspect(), new L10NHelpers.UnlocalizedString(unlocalizedError));
        }

        @Override
        public void addInformation(List<String> list, EntityPlayer entityPlayer) {

        }

        @Override
        public void addModelOverlay(VariableModelBaked variableModelBaked, List<BakedQuad> quads) {

        }
    }

}