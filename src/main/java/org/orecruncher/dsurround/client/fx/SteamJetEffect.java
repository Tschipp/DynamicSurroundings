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

package org.orecruncher.dsurround.client.fx;

import java.util.Random;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.client.fx.particle.ParticleSteamCloud;
import org.orecruncher.dsurround.client.fx.particle.system.ParticleJet;
import org.orecruncher.dsurround.client.fx.particle.system.ParticleSteamJet;
import org.orecruncher.lib.chunk.IBlockAccessEx;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SteamJetEffect extends JetEffect {

	public SteamJetEffect(final int chance) {
		super(chance);
	}

	protected static int lavaCount(final IBlockAccessEx provider, final BlockPos pos, final boolean fastCheck) {
		int blockCount = 0;
		for (int i = -1; i <= 1; i++)
			for (int j = -1; j <= 1; j++)
				for (int k = -1; k <= 1; k++) {
					final IBlockState theBlock = provider.getBlockState(pos.getX() + i, pos.getY() + j, pos.getZ() + k);
					if (ParticleSteamCloud.isHotBlock(theBlock)) {
						if (fastCheck)
							return 1;
						blockCount++;
					}
				}
		return blockCount;
	}

	@Override
	@Nonnull
	public BlockEffectType getEffectType() {
		return BlockEffectType.STEAM_JET;
	}

	public static boolean isValidSpawnBlock(@Nonnull final IBlockAccessEx provider, @Nonnull final BlockPos pos) {
		return isValidSpawnBlock(provider.getBlockState(pos), provider, pos);
	}

	public static boolean isValidSpawnBlock(@Nonnull final IBlockState state, @Nonnull final IBlockAccessEx provider,
			@Nonnull final BlockPos pos) {
		if (!(state.getMaterial().isLiquid() && provider.isAirBlock(pos.up())))
			return false;
		return lavaCount(provider, pos, true) > 0;
	}

	@Override
	public boolean canTrigger(@Nonnull final IBlockAccessEx provider, @Nonnull final IBlockState state,
			@Nonnull final BlockPos pos, @Nonnull final Random random) {
		return isValidSpawnBlock(state, provider, pos) && super.canTrigger(provider, state, pos, random);
	}

	@Override
	public void doEffect(@Nonnull final IBlockAccessEx provider, @Nonnull final IBlockState state,
			@Nonnull final BlockPos pos, @Nonnull final Random random) {
		final int strength = lavaCount(provider, pos, false);
		if (strength > 0) {
			final float spawnHeight = BlockLiquid.getLiquidHeight(state, provider, pos);
			final ParticleJet effect = new ParticleSteamJet(strength, provider.getWorld(), pos.getX() + 0.5D,
					spawnHeight, pos.getZ() + 0.5D);
			addEffect(effect);
		}
	}
}
