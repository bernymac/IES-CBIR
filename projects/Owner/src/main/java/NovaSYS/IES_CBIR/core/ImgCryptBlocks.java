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

import static NovaSYS.IES_CBIR.utils.ImgUtils.blockHeight;
import static NovaSYS.IES_CBIR.utils.ImgUtils.blockWidth;

import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import NovaSYS.IES_CBIR.data.ImgCiphertext;
import NovaSYS.IES_CBIR.data.ImgHistograms;
import NovaSYS.IES_CBIR.utils.RC4;


public class ImgCryptBlocks {

	private int[] tk1;									//trapdoor key tk1
	private int[] tk2;									//trapdoor key tk2
	private Map<Integer,byte[]> dks;		//decryption keys dk
	private KeyGenerator kgen;
	private RC4 prng;
	
	private Cipher aes;
	private SecretKeySpec aeskey; 
	private Map<Integer,byte[]> ivs;
	
	public ImgCryptBlocks() throws Exception {
		aes = Cipher.getInstance("AES/CBC/PKCS5Padding");
		kgen = KeyGenerator.getInstance("AES");
		kgen.init(256);
		aeskey = new SecretKeySpec(kgen.generateKey().getEncoded(), "AES");
		ivs = new HashMap<Integer, byte[]>();
		
		prng = new RC4();
		kgen = KeyGenerator.getInstance("RC4");
		kgen.init(128);
		tk1 = prng.generateUniqueRandomsVector(kgen.generateKey().getEncoded(),101);
		tk2 = prng.generateUniqueRandomsVector(kgen.generateKey().getEncoded(),101);
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
			int[][] pixels = img.getImg();
			final int width = img.getWidth();
			final int height = img.getHeight();
//			final int blockWidth = (int)Math.ceil((double)width/(double)nBlockBySide);
//			final int blockHeight = (int)Math.ceil((double)height/(double)nBlockBySide);
			final int nWidthBlocks = (int)Math.ceil((double)width/(double)blockWidth);
			final int nHeightBlocks = (int)Math.ceil((double)height/(double)blockHeight);
			
			byte[] imgKey = kgen.generateKey().getEncoded();
			if (img.getId() != -1)	//if id is -1, then it's a query and don't store the decryption key (for now)
				dks.put(img.getId(), imgKey);
			int[] randoms = prng.generateRandomsVector(imgKey, nWidthBlocks+nHeightBlocks+blockWidth+blockHeight);
			
			for (int i = 0; i < width; i++) 								//encrypt color values
				for (int j = 0; j < height; j++)
					pixels[i][j] = encryptPixel3DES(pixels[i][j]);
			
			for (int w=0; w<width; w+=blockWidth) {						//shuffle all blocks by width
				final int rnd = Math.abs(randoms[w/blockWidth])%height;
				final int limit = w+blockWidth < width ? w+blockWidth : width;
				for (int i = w; i < limit; i++) {
					int[] newCol = new int[height];
					for (int j = 0; j < height; j++) 
						newCol[j] = pixels[i][(j+rnd)%height];
					pixels[i] = newCol;
				}
			}
			
			for (int h=0; h<height; h+=blockHeight) {					//shuffle all blocks by columns
				final int rnd = Math.abs(randoms[nWidthBlocks+h/blockHeight])%width;
				final int limit = h+blockHeight < height ? h+blockHeight : height;
				for (int j = h; j < limit; j++) { 				
					int[] newRow = new int[width];
					for (int i = 0; i < width; i++) 
						newRow[i] = pixels[(i+rnd)%width][j];
					for (int i = 0; i < width; i++)
						pixels[i][j] = newRow[i];
				}
			}
			
			for (int w=0; w<width; w+=blockWidth) {					//shuffle rows and cols inside each block
				for (int h=0; h<height; h+=blockHeight) {
					final int widthLimit = w+blockWidth < width ? w+blockWidth : width;
					final int heightLimit = h+blockHeight < height ? h+blockHeight : height;
					for (int i = w; i < widthLimit; i++) {				//shuffle block cols
						final int rndCols = Math.abs(randoms[nWidthBlocks+nHeightBlocks+(i-w)])%(heightLimit-h);
						int[] newBlockCol = new int[blockHeight];
						for (int j = h; j < heightLimit; j++) {
							int index = (j+rndCols)%heightLimit;	//if value overflows module gives 0, so we need to add h 
							index += (index < h) ? h : 0; 
							newBlockCol[j-h] = pixels[i][index];
						}
						for (int j = h; j < heightLimit; j++)
							pixels[i][j] = newBlockCol[j-h];
					}
					for (int j = h; j < heightLimit; j++) { 			//shuffle block rows
						final int rndRows = Math.abs(randoms[nWidthBlocks+nHeightBlocks+blockWidth+(j-h)])%(widthLimit-w);
						int[] newBlockRow = new int[blockWidth];
						for (int i = w; i < widthLimit; i++) {
							int index = (i+rndRows)%widthLimit;
							index += (index < w) ? w : 0;
							newBlockRow[i-w] = pixels[index][j];
						}
						for (int i = w; i < widthLimit; i++)
							pixels[i][j] = newBlockRow[i-w];
					}
				}
			}
			
			img.setEncrypted(true);
			return img;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	
	public ImgCiphertext decrypt (ImgCiphertext encImg) { //TODO:INCOMPLETE!!!
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
				int[] newCol = new int[width];
				for (int j = 0; j < width; j++) 
					newCol[(j+rnd)%width] = img[j][i];
				for (int j = 0; j < width; j++)
					img[j][i] = newCol[j];
			}
			
			//then shuffle back width
			for (int i = 0; i < width; i++) {
				int rnd = Math.abs(randoms[i])%height;
				int[] newRow = new int[height];
				for (int j = 0; j < height; j++) 
					newRow[(j+rnd)%height] = img[i][j];
				img[i] = newRow;
			}
				
			//decrypt pixel values
			for (int i = 0; i < width; i++) 
				for (int j = 0; j < height; j++) 
					img[i][j] = decryptPixel3DES(img[i][j]);
				
			encImg.setEncrypted(false);
			return encImg;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private int encryptPixel3DES(int pixel) {
		return encryptPixel1Key(decryptPixel1Key(encryptPixel1Key(pixel, tk1), tk2), tk1);
	}
	
	private int decryptPixel3DES(int pixel) {
		return decryptPixel1Key(encryptPixel1Key(decryptPixel1Key(pixel, tk1), tk2), tk1);
	}

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
			byte[] iv = kgen.generateKey().getEncoded();
			ivs.put(id, iv);
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
	
}
