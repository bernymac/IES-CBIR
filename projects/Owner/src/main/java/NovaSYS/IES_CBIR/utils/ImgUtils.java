/**
 *    Copyright 2015 Bernardo Luís da Silva Ferreira
 
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package NovaSYS.IES_CBIR.utils;

import java.awt.Color;
import java.util.Arrays;

import NovaSYS.IES_CBIR.data.ImgHistograms;
import NovaSYS.IES_CBIR.data.BOVW.CodebookNode;
import NovaSYS.IES_CBIR.data.BOVW.FloatCodebookNode;

public class ImgUtils {

	public static final int blockWidth = 16;
	public static final int blockHeight = 16;
	
	public static final int hueBins = 101;//8;
	public static final int satBins = 101;//4;
	public static final int brightBins = 101;//4;
	public static final double nanoSeconds = 1000000000.0;
	
	public static byte[] intToByteArray (int i) {
		return new byte[] {
				(byte)((i >> 24) & 0xff),
				(byte)((i >> 16) & 0xff),
				(byte)((i >> 8) & 0xff),
				(byte)((i >> 0) & 0xff)
		};
	}

	public static int byteArrayToInt (byte[] b) {
		return 	((0xff & b[0]) << 24 |
				(0xff & b[1]) << 16 |
				(0xff & b[2]) << 8 |
				(0xff & b[3]) << 0
				);
	}
	
	public static short alphaValueFromPixel (int pixel) {
		return (short)((pixel>>24)&0xff);
	}
	
	public static short redOrHueValueFromPixel (int pixel) {
		return (short)((pixel>>16)&0xff);
	}
	
	public static short greenOrSaturationValueFromPixel (int pixel) {
		return (short)((pixel>>8)&0xff);
	}
	
	public static short blueOrBrightnessValueFromPixel (int pixel) {
		return (short)(pixel&0xff);
	}
	
	public static int packPixel (int a, int r, int g, int b) {
		return 	((0xff & (byte)a) << 24 |
				(0xff & (byte)r) << 16 |
				(0xff & (byte)g) << 8 |
				(0xff & (byte)b) << 0
				);
	}
	
	public static double precision (double relevantDocumentsRetrieved, double totalDocumentsRetrieved) {
		return relevantDocumentsRetrieved / totalDocumentsRetrieved;
	}
	
	public static double recall (double relevantDocumentsRetrieved, double totalRelevantDocuments) {
		return relevantDocumentsRetrieved / totalRelevantDocuments;
	}
	
	public static double HistIntersectionAdd (ImgHistograms img1, ImgHistograms img2) {
		double distance = 0, minH = 0, sumH1 = 0, sumH2 = 0;
		for (int i = 0; i < 101; i++) {
			sumH1 += img1.getHueValue(i);
			sumH2 += img2.getHueValue(i);
			minH += Math.min(img1.getHueValue(i), img2.getHueValue(i));
		}
		distance += minH / Math.min(sumH1,sumH2);

		double minS = 0, sumS1 = 0, sumS2 = 0;
		for (int i = 0; i < 101; i++) {
			sumS1 += img1.getSaturationValue(i);
			sumS2 += img2.getSaturationValue(i);
			minS += Math.min(img1.getSaturationValue(i), img2.getSaturationValue(i));
		}
		distance += minS / Math.min(sumS1,sumS2);
		
		double minV = 0, sumV1 = 0, sumV2 = 0;
		for (int i = 0; i < 101; i++) {
			sumV1 += img1.getBrightnessValue(i);
			sumV2 += img2.getBrightnessValue(i);
			minV += Math.min(img1.getBrightnessValue(i), img2.getBrightnessValue(i));
		}
		distance += minV / Math.min(sumV1,sumV2);
		return 1-distance;
	}
	
	public static double HistIntersectionAll (ImgHistograms img1, ImgHistograms img2) {
		double sumMin = 0, sum1 = 0, sum2 = 0;
		for (int i = 0; i < 101; i++) {
			sum1 += img1.getHueValue(i)+img1.getSaturationValue(i)+img1.getBrightnessValue(i);
			sum2 += img2.getHueValue(i)+img2.getSaturationValue(i)+img2.getBrightnessValue(i);
			sumMin += Math.min(img1.getHueValue(i), img2.getHueValue(i)) +
					Math.min(img1.getSaturationValue(i), img2.getSaturationValue(i)) +
					Math.min(img1.getBrightnessValue(i), img2.getBrightnessValue(i));
		}
		return 1- (sumMin / Math.min(sum1,sum2));
	}
	
	public static double LmDistanceHSB (ImgHistograms img1, ImgHistograms img2, double m) {
		double total = 0.0;
		for (int i = 0; i < ImgHistograms.numberOfBins(); i++) {
			total += Math.pow(Math.abs(img1.getHueValue(i)-img2.getHueValue(i)), m);
			total += Math.pow(Math.abs(img1.getSaturationValue(i)-img2.getSaturationValue(i)), m);
			total += Math.pow(Math.abs(img1.getBrightnessValue(i)-img2.getBrightnessValue(i)), m);
		}
		return Math.pow(total,1.0/m);
	}
	
	public static int hammingDistanceHSB (ImgHistograms img1, ImgHistograms img2) {
		int distance = 0;
		for (int i = 0; i < ImgHistograms.numberOfBins(); i++) {
			distance += hamingdistancePixel(img1.getHueValue(i), img2.getHueValue(i));
			distance += hamingdistancePixel(img1.getSaturationValue(i), img2.getSaturationValue(i));
			distance += hamingdistancePixel(img1.getBrightnessValue(i), img2.getBrightnessValue(i));
		}
		return distance;
	}
	
	public static double LmDistance (int[] array1, int[] array2, double m) {
		assert array1.length == array2.length;
		double total = 0.0;
		for (int i = 0; i < array1.length; i++) {
			total += Math.pow(Math.abs(array1[i]-array2[i]), m);
		}
		return Math.pow(total,1.0/m);
	}
	
	public static double floatLmDistance (float[] array1, float[] array2, double m) {
		assert array1.length == array2.length;
		double total = 0.0;
		for (int i = 0; i < array1.length; i++) {
			total += Math.pow(Math.abs(array1[i]-array2[i]), m);
		}
		return Math.pow(total,1.0/m);
	}
	
	public static double LmDistance (double[] array1, double[] array2, double m) {
		assert array1.length == array2.length;
		double total = 0.0;
		for (int i = 0; i < array1.length; i++) {
			total += Math.pow(Math.abs(array1[i]-array2[i]), m);
		}
		return Math.pow(total,1.0/m);
	}
	
//	public static double LmDistance (byte[] array1, byte[] array2, double m) {
//		assert array1.length == array2.length;
//		double total = 0.0;
//		for (int i = 0; i < array1.length; i++) {
//			total += Math.pow(Math.abs(array1[i]-array2[i]), m);
//		}
//		return Math.pow(total,1.0/m);
//	}
	
	public static float normalizedHammingDistance(float[] array1, float[] array2) {
		assert array1.length == array2.length;
		float total = 0f;
		for (int i = 0; i < array1.length; i++) {
			if((array1[i] == 1 && array2[i]==0) || (array1[i] == 0 && array2[i]==1))
				total ++;				
		}
		total /= array1.length;
		return total; 
//		return (float) (floatLmDistance(array1, array2, 1)/array1.length);
	}
	
	public static float hammingDistance(byte[] array1, byte[] array2) {
	assert array1.length == array2.length;
	float dist = 0f;
	for (int i = 0; i < array1.length; i++)
		if (array1[i] != array2[i])
			dist++;
	return dist;
}
	
//	public static float hammingDistance(byte[] array1, byte[] array2) {
//		assert array1.length == array2.length;
//		float dist = 0f;
//		for (int i = 0; i < array1.length; i++) {
////		for (int i = 0; i < array1.length; i+=4) {
////			int x = byteArrayToInt(Arrays.copyOfRange(array1, i, i+4));
////			int y = byteArrayToInt(Arrays.copyOfRange(array2, i, i+4));
////			int  val =x ^ y;
//			int val = array1[i] ^ array2[i];
//			while (val != 0)	{
//				dist++;
//				val &= val - 1;
//			}
//		}
//		return dist;
//	}
	
//	public static double mahalanobisDistance(RealMatrix m1, double v1[], double v2[]) {
//		double det = Math.pow((new LUDecomposition(m1).getDeterminant()), 1/(v1.length));
//		double[] tempSub = new double[v1.length];
//		for(int i=0; i < v1.length; i++){
//			tempSub[i] = (v1[i]-v2[i]);
//		}
//		double[] temp = new double[v1.length];
//		for(int i=0; i < temp.length; i++){
//			temp[i] = tempSub[i]*det;
//		}
//		RealMatrix m2 = new Array2DRowRealMatrix(new double[][] { temp });
//		RealMatrix m3 = m2.multiply(new LUDecomposition(m1).getSolver().getInverse());
//		RealMatrix m4 = m3.multiply((new Array2DRowRealMatrix(new double[][] { temp })).transpose());
//		return Math.sqrt(m4.getEntry(0, 0));
//}
	
	public static int hierchStemHist(CodebookNode node, int[] hist) {
		//recursive method - we found the most similar leaf
		if (node.isLeaf())
			return node.getNodeId();
		
		//find the most similar node between the childs of this node
		CodebookNode minNode = null;
		double minValue = Double.MAX_VALUE;
		for (CodebookNode child: node.getChilds()) {
			double curValue = ImgUtils.LmDistance(child.getNode(),hist,1);
			if (curValue < minValue) {
				minValue = curValue;
				minNode=child;
			}
		}
		return hierchStemHist(minNode, hist);
	}
	
	public static int stemHist(int[][] codebook, int[] hist) {
		int minId=0;
		double minValue = Double.MAX_VALUE;
		for (int i=0;i<codebook.length;i++){
			double curValue = ImgUtils.LmDistance(codebook[i],hist,1);
			if (curValue < minValue) {
				minValue = curValue;
				minId=i;
			}
		}
		return minId;
	}
	
	public static int floatHierchStemHist(FloatCodebookNode node, float[] hist) {
		//recursive method - we found the most similar leaf
		if (node.isLeaf())
			return node.getNodeId();
		
		//find the most similar node between the childs of this node
		FloatCodebookNode minNode = null;
		double minValue = Double.MAX_VALUE;
		for (FloatCodebookNode child: node.getChilds()) {
			double curValue = ImgUtils.floatLmDistance(child.getNode(),hist,1);
			if (curValue < minValue) {
				minValue = curValue;
				minNode=child;
			}
		}
		return floatHierchStemHist(minNode, hist);
	}
	
	public static int floatStemHist(float[][] codebook, float[] hist) {
		int minId=0;
		double minValue = Double.MAX_VALUE;
		for (int i=0;i<codebook.length;i++){
			double curValue = ImgUtils.floatLmDistance(codebook[i],hist,1);
			if (curValue < minValue) {
				minValue = curValue;
				minId=i;
			}
		}
		return minId;
	}
	
	public static int hamingdistancePixel(int x, int y)
	{
	  int dist = 0, val = x ^ y; // XOR
	 
	  // Count the number of set bits
	  while(val>0)
	  {
	    ++dist; 
	    val &= (val - 1);
	  }
	 
	  return dist;
	}

	public static int packHSB(float[] hsb) {
		return 	((0xff & (byte)(Math.round(hsb[0]*100))) << 16 |
				(0xff & (byte)(Math.round(hsb[1]*100))) << 8 |
				(0xff & (byte)(Math.round(hsb[2]*100))) << 0
				);
	}

	public static int RGBtoHSB(int rgb) {
		return packHSB(Color.RGBtoHSB(ImgUtils.redOrHueValueFromPixel(rgb), 
				ImgUtils.greenOrSaturationValueFromPixel(rgb), ImgUtils.blueOrBrightnessValueFromPixel(rgb), null));
	}

	public static int HSBtoRGB(int pixel) {
		return Color.HSBtoRGB(((float)ImgUtils.redOrHueValueFromPixel(pixel))/(float)100,
				((float)ImgUtils.greenOrSaturationValueFromPixel(pixel))/(float)100, 
				((float)ImgUtils.blueOrBrightnessValueFromPixel(pixel))/(float)100);
	}
	
	public static float[] RGB_TO_LAB(int pixel, boolean norm) {
		final double epsilon = 0.008856; // actual CIE standard
		final double kappa = 903.3; // actual CIE standard

		final double Xr = 0.950456; // reference white
		final double Yr = 1.0; // reference white
		final double Zr = 1.088754; // reference white
		
		final float Lscale = norm ? 1f / 100f : 1;
		final float ascale = norm ? 1f / 256f : 1;
		final float bscale = norm ? 1f / 256f : 1;
		final float abdelta = norm ? 127 : 0;

		//get pixel RGB
		final float R = redOrHueValueFromPixel(pixel);
		final float G = greenOrSaturationValueFromPixel(pixel);
		final float Bl = blueOrBrightnessValueFromPixel(pixel);

		// inverse sRGB companding
		final double r = (R <= 0.04045) ? (R / 12.92) : (Math.pow((R + 0.055) / 1.055, 2.4));
		final double g = (G <= 0.04045) ? (G / 12.92) : (Math.pow((G + 0.055) / 1.055, 2.4));
		final double b = (Bl <= 0.04045) ? (Bl / 12.92) : (Math.pow((Bl + 0.055) / 1.055, 2.4));

		// XYZ linear transform
		final float X = (float) (r * 0.4124564 + g * 0.3575761 + b * 0.1804375);
		final float Y = (float) (r * 0.2126729 + g * 0.7151522 + b * 0.0721750);
		final float Z = (float) (r * 0.0193339 + g * 0.1191920 + b * 0.9503041);

		//XYZ to LAB
		final double xr = X / Xr;
		final double yr = Y / Yr;
		final double zr = Z / Zr;

		final double fx = (xr > epsilon) ? (Math.pow(xr, 1.0 / 3.0)) : ((kappa * xr + 16.0) / 116.0);
		final double fy = (yr > epsilon) ? (Math.pow(yr, 1.0 / 3.0)) : ((kappa * yr + 16.0) / 116.0);
		final double fz = (zr > epsilon) ? (Math.pow(zr, 1.0 / 3.0)) : ((kappa * zr + 16.0) / 116.0);

		final float L = ((float) (116.0 * fy - 16.0)) * Lscale;
		final float A = ((float) (500.0 * (fx - fy)) + abdelta) * ascale;
		final float B = ((float) (200.0 * (fy - fz)) + abdelta) * bscale;

//		return packPixel(0, Math.round(L), Math.round(A), Math.round(B));
		return new float[] {L,A,B};
	}

	public static double scaledTfIdf (double tf, double idf) {
		if (tf != 0)
			return (1+Math.log10(tf))* idf;
		else
			return 0;
		//return tf*idf;
	}
	
	public static double idfScaledTfIdf (double nDocs, double df) {
		return Math.log10(nDocs / df);
	}
	
	public static double bm25(double n, int df, int nDocs, int docLength, int sumDocLengths) {
		final double k1 = 1.2;
		final double b = 0.75;
		final double avgDocLength = (double) sumDocLengths / nDocs;
		final double tf = (1+Math.log10(n));//(double) n;// / docLength;
		final double idf = Math.log10((double) nDocs / df);
		return idf * (((k1+1)*tf) / (k1*((1-b)+b*(docLength/avgDocLength))+tf));
	}
	
	public static void main (String[] args) {
//		Set<Float> L = new HashSet<Float>();
//		Set<Float> A = new HashSet<Float>();
//		Set<Float> B = new HashSet<Float>();
		float maxL = Float.MIN_VALUE;
		float maxA = Float.MIN_VALUE;
		float maxB = Float.MIN_VALUE;
		float minL = Float.MAX_VALUE;
		float minA = Float.MAX_VALUE;
		float minB = Float.MAX_VALUE;
		for (int r = 0; r < 256; ++r)
		    for (int g = 0; g < 256; ++g)
		        for (int b = 0; b < 256; ++b) {
		        		final float[] lab = Color.RGBtoHSB(r, g, b,null); 
//		        		L.add(lab[0]);
//		        		A.add(lab[1]);
//		        		B.add(lab[2]);
		        		 maxL = Math.max(maxL, lab[0]);
		        		 maxA = Math.max(maxA, lab[1]);
		        		 maxB = Math.max(maxB, lab[2]);
		        		 minL = Math.min(minL, lab[0]);
		        		 minA = Math.min(minA, lab[1]);
		        		 minB = Math.min(minB, lab[2]);
		        		
//		        		System.out.println(r+" "+g+" "+b+" => "+lab[0]+" "+lab[1]+" "+lab[2]);
		        }
//		System.out.println("L:"+L.size()+" A:"+A.size()+" B:"+B.size());
		System.out.println("maxL = " + maxL + ", maxA = " + maxA + ", maxB = " + maxB);
		System.out.println("minL = " + minL + ", minA = " + minA + ", minB = " + minB);
	}
	
}
