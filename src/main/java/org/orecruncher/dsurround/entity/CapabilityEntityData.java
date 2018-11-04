/*
 * This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.orecruncher.dsurround.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.network.Network;
import org.orecruncher.dsurround.network.PacketEntityData;
import org.orecruncher.lib.capability.CapabilityProviderSerializable;
import org.orecruncher.lib.capability.CapabilityUtils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CapabilityEntityData {

	@CapabilityInject(IEntityData.class)
	public static final Capability<IEntityData> ENTITY_DATA = null;
	public static final EnumFacing DEFAULT_FACING = null;
	public static final ResourceLocation CAPABILITY_ID = new ResourceLocation(ModBase.MOD_ID, "data");

	public static void register() {
		CapabilityManager.INSTANCE.register(IEntityData.class, new Capability.IStorage<IEntityData>() {
			@Override
			public NBTBase writeNBT(@Nonnull final Capability<IEntityData> capability,
					@Nonnull final IEntityData instance, @Nullable final EnumFacing side) {
				return ((INBTSerializable<NBTTagCompound>) instance).serializeNBT();
			}

			@Override
			public void readNBT(@Nonnull final Capability<IEntityData> capability, @Nonnull final IEntityData instance,
					@Nullable final EnumFacing side, @Nonnull final NBTBase nbt) {
				((INBTSerializable<NBTTagCompound>) instance).deserializeNBT((NBTTagCompound) nbt);
			}
		}, () -> new EntityData(null));
	}

	public static IEntityData getCapability(@Nonnull final Entity entity) {
		return CapabilityUtils.getCapability(entity, CapabilityEntityData.ENTITY_DATA, null);
	}

	@Nonnull
	public static ICapabilityProvider createProvider(final IEntityData data) {
		return new CapabilityProviderSerializable<>(ENTITY_DATA, DEFAULT_FACING, data);
	}

	@EventBusSubscriber(modid = ModBase.MOD_ID)
	public static class EventHandler {

		/*
		 * Attach the capability to the Entity when it is created.
		 */
		@SubscribeEvent
		public static void attachCapabilities(final AttachCapabilitiesEvent<Entity> event) {
			if (event.getObject() instanceof EntityLiving) {
				final EntityData emojiData = new EntityData(event.getObject());
				event.addCapability(CAPABILITY_ID, createProvider(emojiData));
			}
		}

		/*
		 * Event generated when a player starts tracking an Entity. Need to send an
		 * initial sync to the player.
		 */
		@SubscribeEvent
		public static void trackingEvent(@Nonnull final PlayerEvent.StartTracking event) {
			if (event.getTarget() instanceof EntityLiving) {
				final IEntityData data = event.getTarget().getCapability(ENTITY_DATA, DEFAULT_FACING);
				if (data != null) {
					Network.sendToPlayer((EntityPlayerMP) event.getEntityPlayer(), new PacketEntityData(data));
				}
			}
		}

		/*
		 * Called when an entity is being updated. Need to evaluate new states.
		 */
		@SubscribeEvent(receiveCanceled = false)
		public static void livingUpdate(@Nonnull final LivingUpdateEvent event) {
			final World world = event.getEntity().getEntityWorld();
			// Don't tick if this is the client thread. We only check 4 times a
			// second as if that is enough :)
			if (world.isRemote || (world.getTotalWorldTime() % 5) != 0)
				return;
			final IEntityDataSettable data = (IEntityDataSettable) getCapability(event.getEntity());
			if (data != null) {
				EntityDataTables.assess((EntityLiving) event.getEntity());
				if (data.isDirty())
					data.sync();
			}
		}
	}

}