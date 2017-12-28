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

package org.blockartistry.DynSurround.asm;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.RETURN;

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.lib.asm.Transmorgrifier;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class PatchEntityRenderer extends Transmorgrifier {

	// 1.10.x
	private static final String[] classNames = { "net.minecraft.client.renderer.EntityRenderer", "bnz", "bnd" };
	
	// 1.11.x
	//private static final String[] classNames = { "net.minecraft.client.renderer.EntityRenderer", "bqc" };
	
	// 1.12.x
	//private static final String[] classNames = { "net.minecraft.client.renderer.EntityRenderer", "buo" };

	public PatchEntityRenderer() {
		super(classNames);
	}

	@Override
	public String name() {
		return "addRainParticles";
	}

	@Override
	public boolean isEnabled() {
		return ModOptions.enableWeatherASM;
	}

	@Override
	public boolean transmorgrify(final ClassNode cn) {

		final String names[] = { "func_78484_h", "addRainParticles" };
		final String sigs[] = { "()V", "()V" };

		final MethodNode m = findMethod(cn, names, sigs);
		if (m != null) {
			this.logMethod(Transformer.log(), m, "Found!");

			m.localVariables = null;
			final InsnList list = new InsnList();
			list.add(new VarInsnNode(ALOAD, 0));

			final String owner = "org/blockartistry/DynSurround/client/weather/RenderWeather";
			final String targetName = "addRainParticles";
			final String sig = "(Lnet/minecraft/client/renderer/EntityRenderer;)V";

			list.add(new MethodInsnNode(INVOKESTATIC, owner, targetName, sig, false));
			list.add(new InsnNode(RETURN));
			m.instructions = list;
			return true;
		}

		Transformer.log().info("Unable to patch [net.minecraft.client.renderer.EntityRenderer]!");

		return false;
	}

}