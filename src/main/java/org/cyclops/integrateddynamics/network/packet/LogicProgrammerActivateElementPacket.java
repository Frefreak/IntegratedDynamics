package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * Packet for sending a button packet for the exalted crafting.
 * @author rubensworks
 *
 */
public class LogicProgrammerActivateElementPacket extends PacketCodec {

	@CodecField
	private String typeId;
    @CodecField
    private String elementId;

    public LogicProgrammerActivateElementPacket() {

    }

    public LogicProgrammerActivateElementPacket(String typeId, String elementId) {
		this.typeId = typeId;
        this.elementId = elementId;
    }

	@Override
	public boolean isAsync() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void actionClient(World world, EntityPlayer player) {
		
	}

	@Override
	public void actionServer(World world, EntityPlayerMP player) {
		if(player.openContainer instanceof ContainerLogicProgrammerBase) {
			((ContainerLogicProgrammerBase) player.openContainer).setActiveElementById(typeId, elementId);
		}
	}
	
}