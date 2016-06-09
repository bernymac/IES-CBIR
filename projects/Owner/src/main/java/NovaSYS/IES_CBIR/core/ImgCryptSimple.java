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

package NovaSYS.IES_CBIR.core;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import NovaSYS.IES_CBIR.data.ImgCiphertext;
import NovaSYS.IES_CBIR.utils.Paillier;
import NovaSYS.IES_CBIR.utils.RC4;
import NovaSYS.IES_CBIR.utils.OPE.OPE;


public class ImgCryptSimple {

	private int[] tk1;									//trapdoor key tk1
//	private int[] tk2;									//trapdoor key tk2
	private Map<Integer,byte[]> dks;		//decryption keys dk
	private KeyGenerator kgen;
	private RC4 prng;
	private OPE ope;
	private Paillier paillier;
	
	private Cipher aes;
	private SecretKeySpec aeskey; 
	private Map<Integer,byte[]> ivs;
	
	public ImgCryptSimple() throws Exception {
		aes = Cipher.getInstance("AES/CBC/PKCS5Padding");
		kgen = KeyGenerator.getInstance("AES");
//		kgen.init(256);
		kgen.init(128);
		aeskey = new SecretKeySpec(kgen.generateKey().getEncoded(), "AES");
		ivs = new HashMap<Integer, byte[]>();
		ope = new OPE("my key materials", 32, 64);
		paillier = new Paillier();
		
		prng = new RC4();
		kgen = KeyGenerator.getInstance("RC4");
		kgen.init(128);
		tk1 = new int[]{52,56,51,57,54,90,74,19,8,96,24,83,39,20,2,13,66,68,18,7,
				42,21,58,4,89,62,17,64,82,23,45,3,95,80,84,15,50,27,79,97,67,75,
				63,37,30,31,46,61,36,16,6,81,78,94,65,9,70,35,1,87,93,34,38,47,
				88,10,60,12,92,48,28,99,85,49,33,55,69,44,41,91,98,25,71,73,
				100,32,11,14,53,77,72,86,40,29,26,0,59,43,76,22,5};
//		tk1 = prng.generateUniqueRandomsVector(kgen.generateKey().getEncoded(),101);
//		tk2 = prng.generateUniqueRandomsVector(kgen.generateKey().getEncoded(),
		dks = new HashMap<Integer, byte[]>();
	}
	
//	private byte[] generateKeyHSB() {
//		List<Byte> l = new LinkedList<Byte>();
//		for (int i = 0; i < 101; i++) {
//			l.add((byte)i);
//		}
//		byte[] key = new byte[101];
//		for (int i = 100; i >= 0; i--) {
//			int rnd = prng.nextInt(i+1);
//			key[i] = l.remove(rnd);
//		}
//		return key;
//	}

	public ImgCiphertext encrypt (ImgCiphertext img) {
		if(img.isEncrypted())
			return img;
		try {
			final int[][] pixels = img.getImg();
			int width = img.getWidth();
			int height = img.getHeight();
			byte[] imgKey = kgen.generateKey().getEncoded();
			dks.put(img.getId(), imgKey);
			int[] randoms = prng.generateRandomsVector(imgKey,width+height);
			
			for (int i = 0; i < width; i++) 								//encrypt color values
				for (int j = 0; j < height; j++)
//					pixels[i][j] = encryptPixel3DES(pixels[i][j]);
					pixels[i][j] = encryptPixel1Key(pixels[i][j],tk1);
					
			for (int i = 0; i < width; i++) {							//shuffle width
				int rnd = Math.abs(randoms[i])%height;
				int[] newCol = new int[height];
				for (int j = 0; j < height; j++) 
					newCol[j] = pixels[i][(j+rnd)%height];
				pixels[i] = newCol;
			}

			for (int i = 0; i < height; i++) {							//shuffle columns
				int rnd = Math.abs(randoms[width+i])%width;
				int[] newRow = new int[width];
				for (int j = 0; j < width; j++) 
					newRow[j] = pixels[(j+rnd)%width][i];
				for (int j = 0; j < width; j++)
					pixels[j][i] = newRow[j];
			}

//			final int[] randoms = prng.generateUniqueRandomsVector(imgKey,width*height);
//			final int[][] newImg = new int[pixels.length][pixels[0].length];
//			for (int i = 0; i < width; i++) {
//				for (int j = 0; j < height; j++) {
//					final int rand = randoms[j+i*height];
//					final int x = rand / height;
//					final int y = rand % height;
//					newImg[i][j] = pixels[x][y]; 
//				}
//			}
//			img.setImg(newImg);
			
			img.setEncrypted(true);
			return img;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	
	public ImgCiphertext decrypt (ImgCiphertext encImg) {
		if(!encImg.isEncrypted())
			return encImg;
		try {
			int[][] img = encImg.getImg();
			int width = encImg.getWidth();
			int height = encImg.getHeight();
			byte[] imgKey = dks.get(encImg.getId());
			int[] randoms = prng.generateRandomsVector(imgKey,width+height);
			
			//shuffle back height first
			for (int i = 0; i < height; i++) {
				int rnd = Math.abs(randoms[width+i])%width;
				int[] newRow = new int[width];
				for (int j = 0; j < width; j++) 
					newRow[(j+rnd)%width] = img[j][i];
				for (int j = 0; j < width; j++)
					img[j][i] = newRow[j];
			}
			
			//then shuffle back width
			for (int i = 0; i < width; i++) {
				int rnd = Math.abs(randoms[i])%height;
				int[] newCol = new int[height];
				for (int j = 0; j < height; j++) 
					newCol[(j+rnd)%height] = img[i][j];
				img[i] = newCol;
			}
				
			//decrypt pixel values
			for (int i = 0; i < width; i++) 
				for (int j = 0; j < height; j++) 
//					img[i][j] = decryptPixel3DES(img[i][j]);
					img[i][j] = decryptPixel1Key(img[i][j],tk1);
				
			encImg.setEncrypted(false);
			return encImg;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
//	private int encryptPixel3DES(int pixel) {
//		return encryptPixel1Key(decryptPixel1Key(encryptPixel1Key(pixel, tk1), tk2), tk1);
//	}
//	
//	private int decryptPixel3DES(int pixel) {
//		return decryptPixel1Key(encryptPixel1Key(decryptPixel1Key(pixel, tk1), tk2), tk1);
//	}

//	private int encryptPixelDiffKeys(int pixel) {
//		return (((alphaKey[((pixel >> 24) & 0xff)] & 0xff) << 24)|
//				((redKey[((pixel >> 16) & 0xff)] & 0xff) << 16)|
//				((blueKey[((pixel >> 8) & 0xff)] & 0xff) << 8)|
//				(greenKey[((pixel) & 0xff)] & 0xff));
//	}
//	
//	private int decryptPixelDiffKeys(int pixel) {
//		byte eA = (byte)((pixel>>24)&0xff);
//		byte eR = (byte)((pixel>>16)&0xff);
//		byte eG = (byte)((pixel>>8)&0xff);
//		byte eB = (byte)(pixel&0xff);
//		byte a = 0, r = 0, g = 0, b = 0;
//		for (short i = 0; i < 256; i++) {
//			if (alphaKey[i] == eA)
//				a = (byte)i;
//			if (redKey[i] == eR)
//				r = (byte)i;
//			if (blueKey[i] == eG)
//				g = (byte)i;
//			if (greenKey[i] == eB)
//				b = (byte)i;
//		}
//		return ((0xff & a) << 24 | (0xff & r) << 16 | (0xff & g) << 8 | (0xff & b) << 0);
//	}
	
	private int encryptPixel1Key(int pixel, int[] key) {
		return 	(((key[((pixel >> 16) & 0xff)] & 0xff) << 16)|
				((key[((pixel >> 8) & 0xff)] & 0xff) << 8)|
				(key[((pixel) & 0xff)] & 0xff));
	}
	
	private int decryptPixel1Key(int pixel, int[] key) {
		int eH = (pixel>>16)&0xff;
		int eS = (pixel>>8)&0xff;
		int eB = pixel&0xff;
		int h = 0, s = 0, b = 0;
		for (int i = 0; i < 101; i++) {
			if (key[i] == eH)
				h = i;
			if (key[i] == eS)
				s = i;
			if (key[i] == eB)
				b = i;
		}
		return ((0xff & h) << 16 | (0xff & s) << 8 | (0xff & b) << 0);
	}
	
	private int decryptVal1Key(int val, int[] key) {
		for (int i = 0; i < 101; i++) 
			if (key[i] == val)
				return i;
		return 0;
	}
	
//	public ImgHistograms encryptHistograms (ImgHistograms hist) {
//		int[] newH = new int[101];
//		int[] newS = new int[101];
//		int[] newB = new int[101];
//		for (int i = 0; i < 101; i++) {				//E k1
//			int encVal = tk1[i];
//			newH[encVal] = hist.getHue()[i];
//			newS[encVal] = hist.getSaturation()[i];
//			newB[encVal] = hist.getBrightness()[i];
//		}
//		for (int i = 0; i < 101; i++) {				//D k2
//			int encVal = decryptVal1Key(i, tk2);
//			newH[encVal] = hist.getHue()[i];
//			newS[encVal] = hist.getSaturation()[i];
//			newB[encVal] = hist.getBrightness()[i];
//		}
//		for (int i = 0; i < 101; i++) {				//E k1
//			int encVal = tk1[i];
//			newH[encVal] = hist.getHue()[i];
//			newS[encVal] = hist.getSaturation()[i];
//			newB[encVal] = hist.getBrightness()[i];
//		}
//		hist.setHue(newH);
//		hist.setSaturation(newS);
//		hist.setBrightness(newB);
//		return hist;
//	}
	
	public byte[] encryptAES (int id, byte[] data) {
		try {
			byte[] iv = ivs.get(id);
			if (iv == null) {
				iv = kgen.generateKey().getEncoded();
				ivs.put(id, iv);
			}
			aes.init(Cipher.ENCRYPT_MODE, aeskey, new IvParameterSpec(iv));
			return aes.doFinal(data);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public byte[] decryptAES (int id, byte[] data) {
		try {
			byte[] iv = ivs.get(id);
			aes.init(Cipher.DECRYPT_MODE, aeskey, new IvParameterSpec(iv));
			return aes.doFinal(data);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public BigInteger encryptOPE (BigInteger data) {
		return ope.encrypt(data);
	}
	
	public BigInteger decryptOPE (BigInteger data) {
		return ope.decrypt(data);
	}
	
	public BigInteger encryptPaillier (BigInteger data) {
		return paillier.Encryption(data);
	}
	
//    public static void main(String[] str) throws Exception {
//		Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
//		KeyGenerator kgen = KeyGenerator.getInstance("AES");
//		kgen.init(128);
//		SecretKeySpec aeskey = new SecretKeySpec(kgen.generateKey().getEncoded(), "AES");
//		aes.init(Cipher.ENCRYPT_MODE, aeskey);
//		byte[] c1 = aes.doFinal("hweiodhfkjhjdfhzalsjfhlkjdhfldhslfhldsafhsjlaswxkhfasadjhsadkjhasdjklasfhjksafkhgaskgdhasbdhhdujahdkjahs".getBytes());
//		byte[] c2 = aes.doFinal("hweiodhfkjhjdfhdalsjfhlkjdhfldhslfhldsafhsjlaswxkhfasadjhsadkjhasdjklasfhjksafkhgaskgdhasbdhhdujahdkjahs".getBytes());
//		System.out.println(Arrays.equals(c1, c2));
//    }
	
}
