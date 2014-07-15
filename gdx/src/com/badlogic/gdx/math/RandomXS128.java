/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.math;

import java.util.Random;

/** This class implements the xorshift128+ algorithm that is a very fast, top-quality 64-bit pseudo-random number generator. The
 * quality of this PRNG is much higher than {@link Random}'s, and its cycle length is 2<sup>128</sup>&nbsp;&minus;&nbsp;1, which
 * is more than enough for any single-thread application.
 * <p>
 * More details and algorithms can be found <a href="http://xorshift.di.unimi.it/">here</a>.
 * 
 * @author Inferno
 * @author davebaol */
public class RandomXS128 extends Random {

	/** Normalization constant for double. */
	private static final double NORM_DOUBLE = 1.0 / (1L << 53);

	/** Normalization constant for float. */
	private static final double NORM_FLOAT = 1.0 / (1L << 24);

	/** The internal state of pseudo-random number generator. */
	private long seed0, seed1;

	/** Creates a new random number generator. This constructor sets the seed of the random number generator to a value very likely
	 * to be distinct from any other invocation of this constructor.
	 * <p>
	 * This implementation creates a {@link Random} instance to generate the initial seed. */
	public RandomXS128 () {
		setSeed(new Random().nextLong());
	}

	/** Creates a new random number generator using a single {@code long} seed.
	 * @param seed the initial seed */
	public RandomXS128 (long seed) {
		setSeed(seed);
	}

	/** Creates a new random number generator using two {@code long} seeds.
	 * @param seed0 the first part of the initial seed
	 * @param seed1 the second part of the initial seed */
	public RandomXS128 (long seed0, long seed1) {
		setState(seed0, seed1);
	}

	@Override
	public long nextLong () {
		long s1 = this.seed0;
		final long s0 = this.seed1;
		this.seed0 = s0;
		s1 ^= s1 << 23;
		return (this.seed1 = (s1 ^ s0 ^ (s1 >>> 17) ^ (s0 >>> 26))) + s0;
	}

	@Override
	protected int next (int bits) {
		return (int)(nextLong() & ((1L << bits) - 1));
	}

	@Override
	public int nextInt () {
		return (int)nextLong();
	}

	@Override
	public int nextInt (final int n) {
		return (int)nextLong(n);
	}

	/** Returns a pseudo-random uniformly distributed {@code long} value between 0 (inclusive) and the specified value (exclusive),
	 * drawn from this random number generator's sequence. The algorithm used to generate the value guarantees that the result is
	 * uniform, provided that the sequence of 64-bit values produced by this generator is.
	 * @param n the positive bound on the random number to be returned.
	 * @return the next pseudo-random {@code long} value between {@code 0} (inclusive) and {@code n} (exclusive). */
	public long nextLong (final long n) {
		if (n <= 0) throw new IllegalArgumentException("n must be positive");
		for (;;) {
			final long bits = nextLong() >>> 1;
			final long value = bits % n;
			if (bits - value + (n - 1) >= 0) return value;
		}
	}

	@Override
	public double nextDouble () {
		return (nextLong() >>> 11) * NORM_DOUBLE;
	}

	@Override
	public float nextFloat () {
		return (float)((nextLong() >>> 40) * NORM_FLOAT);
	}

	@Override
	public boolean nextBoolean () {
		return (nextLong() & 1) != 0;
	}

	@Override
	public void nextBytes (final byte[] bytes) {
		int n = 0;
		int i = bytes.length;
		while (i != 0) {
			n = i < 8 ? i : 8; // min(i, 8);
			for (long bits = nextLong(); n-- != 0; bits >>= 8)
				bytes[--i] = (byte)bits;
		}
	}

	/** Sets the internal seed of this generator based on the given {@code long} value.
	 * <p>
	 * The given seed is passed twice through an hash function. This way, if the user passes a small value we avoid the short
	 * irregular transient associated with states having a very small number of bits set.
	 * @param seed a nonzero seed for this generator (if zero, the generator will be seeded with {@link Long#MIN_VALUE}). */
	@Override
	public void setSeed (final long seed) {
		long seed0 = murmurHash3(seed == 0 ? Long.MIN_VALUE : seed);
		setState(seed0, murmurHash3(seed0));
	}

	/** Sets the internal state of this generator.
	 * @param seed0 the first part of the initial state
	 * @param seed1 the second part of the initial state */
	public void setState (final long seed0, final long seed1) {
		this.seed0 = seed0;
		this.seed1 = seed1;
	}

	private final static long murmurHash3 (long x) {
		x ^= x >>> 33;
		x *= 0xff51afd7ed558ccdL;
		x ^= x >>> 33;
		x *= 0xc4ceb9fe1a85ec53L;
		x ^= x >>> 33;

		return x;
	}

}
