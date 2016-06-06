/**
 *    Copyright 2015 Bernardo Ferreira

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Scanner;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;

public class SecureBinaryEmbedding {
	//if these parameters are changed, remember to delete key and other persisted structures from disk!
	private static final int mAdjust = 1;			//increase or decrease to output size, by multiplication of input size
	private static final float delta = 0.5f;//0.125f;
	
	private int m;								//output size
	private int k;									//input size
	private float[][] a;						//secret random matrix
	private float[] w;							//secret vector with unif dist between [0,delta]
	
	
	public SecureBinaryEmbedding(int dimensions) throws Exception {
//		File key = new File("/Users/bernardo/Data/Client/IES-CBIR/sbeKey.txt");
		File key = new File("/localssd/a28300/Data/Client/IES-CBIR/sbeKey");
		if (key.exists()) {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(key));
			k = ois.readInt();
			m = k*mAdjust;
			a = (float[][]) ois.readObject();
			w = (float[]) ois.readObject();
			ois.close();
		} else {
			k = dimensions;
			m = k*mAdjust;
			SecureRandom prng = new SecureRandom();
			a = new float[m][k];
			for (int i = 0; i < m; i++)
				for (int j = 0; j < k; j++)
					a[i][j] = prng.nextFloat();
			w = new float[m];
			for (int i = 0; i < m; i++)
				w[i] = prng.nextFloat()*delta;
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(key));
			oos.writeInt(k);
			oos.writeObject(a);
			oos.writeObject(w);
			oos.close();
		}
	}
	
	public float[] encode (float[] x) throws Exception{
		if (x.length != k) 
			throw new Exception("Wrong dimension! Expected "+k+", got "+x.length);
		
		float[] encoded = new float[m];
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < k; j++)
				encoded[i] += a[i][j]*x[j];			//multiply input with random matrix a
			encoded[i] += w[i];						//add dither
			encoded[i] /= delta;						// divide by delta
			encoded[i] = encoded[i]%2 < 1 ? 1 : 0;	//quantize
		}
		
		return encoded;
	}
	
//public static void main(String[] args) throws Exception {
//	Scanner sc = new Scanner(new File("/Users/bernardo/Desktop/features_edgehistogram/0/0.txt"),"UTF-8");
//	float[] features = new float[150];
//	for (int i = 0; i < 150; i++) 
//		features[i] = (float) Double.parseDouble(sc.nextLine());
//	sc.close();
//	sc = new Scanner(new File("/Users/bernardo/Desktop/features_edgehistogram/0/0-cópia.txt"),"UTF-8");
//	float[] features2 = new float[150];
//	for (int i = 0; i < 150; i++) 
//		features2[i] = (float) Double.parseDouble(sc.nextLine());
//	sc.close();
//	StandardDeviation std = new StandardDeviation();
//	for (double d: features)
//		std.increment(d);
//	for (double d: features2)
//		std.increment(d); 
//	System.out.println("std: "+std.getResult());
//	System.out.println("l2 distance: "+ ImgUtils.floatLmDistance(features, features2, 2) );
//	System.out.println("normalized hamming distance: "+ ImgUtils.normalizedHammingDistance(features, features2));
//	
////	Covariance cov = new Covariance();
////	System.out.println("covariance: "+cov.covariance(features, features));
////	double[][] matrix = new double[2][features.length];
////	matrix[0] = features;
////	matrix[1] = features2;
////	cov = new Covariance(matrix);
////	RealMatrix covMatrix =  cov.getCovarianceMatrix();
////	System.out.println("Mahalanobis distance: "+ImgUtils.mahalanobisDistance(covMatrix, features, features));
//}
	
	
	public static void main(String[] args) throws Exception {
//		int dim = 150;
//		Scanner sc = new Scanner(new File("/Users/bernardo/Desktop/features_edgehistogram/0/1.txt"),"UTF-8");
//		int dim = 303;
//		Scanner sc = new Scanner(new File("/Users/bernardo/Desktop/SseHist.txt"));
//		float[] features = new float[dim];
//		for (int i = 0; i < dim; i++) 
//			features[i] = Float.parseFloat(sc.nextLine());
//		sc.close();
//		SecureBinaryEmbedding sbe = new SecureBinaryEmbedding(dim);
//		System.out.println("Dimensionality: "+features.length);
//		float[] encoding = sbe.encode(features);
		
//		byte[] features = new byte[dim*4];
//		for (int i = 0; i < dim*4; i+=4) {
//			byte[] bytes = ByteBuffer.allocate(4).putFloat(Float.parseFloat(sc.nextLine())).array();
//			for (int j = 0; j <4;  j++)
//				features[i+j] = bytes[j];
//		}
//		sc.close();
		byte[] features = "qwertyuiopasdfghjklz".getBytes();
		byte[] features2 = "qcvbnmuiopasdfghjklz".getBytes();
		Mac hmac = Mac.getInstance("HmacSHA1");
		KeyGenerator kgen = KeyGenerator.getInstance("HmacSHA1");
		hmac.init(kgen.generateKey());
		byte[] encoding = hmac.doFinal(features);
		int z = encoding[0]-encoding[1];
		byte[] encoding2 = hmac.doFinal(features2);
		System.out.println("Hamming Distance between plaintexts:"+ImgUtils.hammingDistance(features,features2)/features.length);
		System.out.println("Hamming Distance between ciphertexts:"+ImgUtils.hammingDistance(encoding,encoding2)/encoding.length);
		
//		sc = new Scanner(new File("/Users/bernardo/Desktop/features_edgehistogram/0/0-cópia.txt"),"UTF-8");
//		sc = new Scanner(new File("/Users/bernardo/Desktop/SseHist2.txt"));
//		float[] features2 = new float[dim];
//		for (int i = 0; i < dim; i++) 
//			features2[i] = Float.parseFloat(sc.nextLine());
//		sc.close();
//		System.out.println("Dimensionality: "+features2.length);
//		float[] encoding2 = sbe.encode(features2);
		
//		System.out.println("Original L2 distance: "+ImgUtils.floatLmDistance(features, features2, 2));
//		System.out.println("Hashed l1 distance: "+ImgUtils.floatLmDistance(encoding, encoding2, 1)/encoding.length);
//		System.out.println("Hashed normalized hamming distance: "+ImgUtils.normalizedHammingDistance(encoding, encoding2));
//		System.out.println("Distance between plaintext and ciphertext:"+ImgUtils.floatLmDistance(features, encoding, 1)/encoding.length);

	}
	
}
