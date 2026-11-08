package com.Dramora9879.mods.infinite_ocean_generator.worldgen;

final class OceanNoise {
	private OceanNoise() {
	}

	static double hash01(int x, int z) {
		long n = x * 374761393L + z * 668265263L;
		n = (n ^ (n >> 13)) * 1274126177L;
		return ((n ^ (n >> 16)) & 0xFFFFFF) / (double) 0xFFFFFF;
	}

	static double smoothNoise(double x, double z) {
		int x0 = (int) Math.floor(x);
		int z0 = (int) Math.floor(z);
		double fx = fade(x - x0);
		double fz = fade(z - z0);
		double v00 = hash01(x0, z0);
		double v10 = hash01(x0 + 1, z0);
		double v01 = hash01(x0, z0 + 1);
		double v11 = hash01(x0 + 1, z0 + 1);
		double i0 = lerp(v00, v10, fx);
		double i1 = lerp(v01, v11, fx);
		return lerp(i0, i1, fz);
	}

	static double octaveNoise(double x, double z) {
		return smoothNoise(x, z) * 0.65
				+ smoothNoise(x * 2.17, z * 2.17) * 0.25
				+ smoothNoise(x * 4.31, z * 4.31) * 0.10;
	}

	private static double fade(double t) {
		return t * t * (3.0 - 2.0 * t);
	}

	private static double lerp(double a, double b, double t) {
		return a + (b - a) * t;
	}
}
