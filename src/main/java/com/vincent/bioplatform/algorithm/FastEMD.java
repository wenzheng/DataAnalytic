package com.vincent.bioplatform.algorithm;

/**
 *
 * @author Wenzheng Zhu
 */
public class FastEMD {

	private EmdData emd;
	
	private double[] signal;

	public FastEMD(int order, int iterations, int locality, double[] signal) {
		emd = new EmdData();
		emd.iterations = iterations;
		emd.order = order;
		emd.locality = locality;
		emd.size = signal.length;
		emd.imfs = new double[emd.order][emd.size];
		emd.imfs[0] = signal;
		emd.max = new double[emd.size];
		emd.min = new double[emd.size];
		emd.residue = new double[emd.size];
		emd.minPoints = new int[emd.size/2];
		emd.maxPoints = new int[emd.size/2];
		this.signal = signal;
	}

	public void emdDecompose() {
		int i, j;
		emd.residue = signal.clone();
		for (i = 0; i < emd.order - 1; i++) {
			double[] curImf = emd.imfs[i].clone();
			for (j = 0; j < emd.iterations; j++) {
				emdMakeExtrema(curImf);
				if (emd.minSize < 4 || emd.maxSize < 4)
					break; // can't fit splines
				emdInterpolate(curImf, emd.min, emd.minPoints, emd.minSize);
				emdInterpolate(curImf, emd.max, emd.maxPoints, emd.maxSize);
				emdUpdateImf(curImf);
			}
			emdMakeResidue(curImf);
			emd.imfs[i + 1] = emd.residue.clone();
		}
	}

	void emdMakeResidue(double[] cur) {
		int i;
		for (i = 0; i < emd.size; i++)
			emd.residue[i] -= cur[i];
	}

	// Currently, extrema within (locality) of the boundaries are not allowed.
	// A better algorithm might be to collect all the extrema, and then assume
	// that extrema near the boundaries are valid, working toward the center.

	void emdMakeExtrema(double[] curImf) {
		int i, lastMin = 0, lastMax = 0;
		emd.minSize = 0;
		emd.maxSize = 0;
		for (i = 1; i < emd.size - 1; i++) {
			if (curImf[i - 1] < curImf[i]) {
				if (curImf[i] > curImf[i + 1] && (i - lastMax) > emd.locality) {
					emd.maxPoints[emd.maxSize++] = i;
					lastMax = i;
				}
			} else {
				if (curImf[i] < curImf[i + 1] && (i - lastMin) > emd.locality) {
					emd.minPoints[emd.minSize++] = i;
					lastMin = i;
				}
			}
		}
	}

	void emdInterpolate(double[] in, double[] out, int[] points, int pointsSize) {
		int size = emd.size;
		int i, j, i0, i1, i2, i3, start, end;
		double a0, a1, a2, a3;
		double y0, y1, y2, y3, muScale, mu;
		for (i = -1; i < pointsSize; i++) {
			i0 = points[mirrorIndex(i - 1, pointsSize)];
			i1 = points[mirrorIndex(i, pointsSize)];
			i2 = points[mirrorIndex(i + 1, pointsSize)];
			i3 = points[mirrorIndex(i + 2, pointsSize)];

			y0 = in[i0];
			y1 = in[i1];
			y2 = in[i2];
			y3 = in[i3];

			a0 = y3 - y2 - y0 + y1;
			a1 = y0 - y1 - a0;
			a2 = y2 - y0;
			a3 = y1;

			// left boundary
			if (i == -1) {
				start = 0;
				i1 = -i1;
			} else
				start = i1;

			// right boundary
			if (i == pointsSize - 1) {
				end = size;
				i2 = size + size - i2;
			} else
				end = i2;

			muScale = 1.f / (i2 - i1);
			for (j = start; j < end; j++) {
				mu = (j - i1) * muScale;
				out[j] = ((a0 * mu + a1) * mu + a2) * mu + a3;
			}
		}
	}

	void emdUpdateImf(double[] imf) {
		int i;
		for (i = 0; i < emd.size; i++)
			imf[i] -= (emd.min[i] + emd.max[i]) * 0.5f;
	}

	public double[][] getImfs() {
		return this.emd.imfs;
	}

	int mirrorIndex(int i, int size) {
		if (i < size) {
			if (i < 0)
				return -i - 1;
			return i;
		}
		return (size - 1) + (size - i);
	}
}
