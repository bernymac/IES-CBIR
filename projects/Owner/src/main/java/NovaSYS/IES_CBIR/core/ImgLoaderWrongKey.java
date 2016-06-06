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

package NovaSYS.IES_CBIR.core;
//
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.Map;
//
//import javax.imageio.ImageIO;
//
//import NovaSYS.MIES_CBIR.data.ImgCiphertext;
//import NovaSYS.MIES_CBIR.data.ImgHistograms;
//import NovaSYS.MIES_CBIR.data.QueryResult;
//import NovaSYS.MIES_CBIR.utils.ImgUtils;
//
//
public class ImgLoaderWrongKey {
//
//	private ImgCrypt imgCrypt;
//	private ImgCrypt imgCryptWrongKey;
//	private Map<Integer,ImgHistograms> featureVectors;
//	private Map<Integer,ImgHistograms> featureVectorsWrongKey;
//	private Map<Integer,ImgCiphertext> images;
//	private int imgIDCounter;
//	
//	public ImgLoaderWrongKey() throws Exception{
//		imgCrypt = new ImgCrypt();
//		imgCryptWrongKey = new ImgCrypt();
//		featureVectors = new HashMap<Integer, ImgHistograms>();
//		featureVectorsWrongKey = new HashMap<Integer, ImgHistograms>();
//		images = new HashMap<Integer, ImgCiphertext>();
//		imgIDCounter = 0;
//	}
//	
//	
//	public void loadImage (File imgFile) throws IOException {
//		//Data Owner
//		BufferedImage img = ImageIO.read(imgFile);
//		ImgCiphertext encImg = imgCrypt.encrypt(img);
//		ImgCiphertext encImgWrongKey = imgCryptWrongKey.encrypt(img);
//		//Data Storer
//		int id = Integer.parseInt(imgFile.getName().substring(0, imgFile.getName().indexOf('.')));
//		images.put(id, encImg);
//		featureVectors.put(id, buildImageHistograms(encImg));
//		featureVectorsWrongKey.put(id, buildImageHistograms(encImgWrongKey));
//		imgIDCounter++;
//	}
//	
//	
//	public ImgCiphertext encryptImage (BufferedImage img) {
//		return imgCrypt.encrypt(img);
//	}
//	
//	
//	public BufferedImage decryptImage (ImgCiphertext img) {
//		return imgCrypt.decrypt(img);
//	}
//	
//	
//	public ImgHistograms buildImageHistograms (Object img) {
//		ImgCiphertextHSB encImg = (ImgCiphertextHSB)img;
//		int[][] pixels = encImg.getImg();
//		int rows = encImg.getRows();
//		int cols = encImg.getColumns();
//		ImgHistograms histograms = new ImgHistograms();
//		for (int i = 0; i < rows; i++) {
//			for (int j = 0; j < cols; j++) { 
//				int pixel = pixels[i][j];
//				histograms.addHueValue(ImgUtils.redOrHueValueFromPixel(pixel));
//				histograms.addSaturationValue(ImgUtils.greenOrSaturationValueFromPixel(pixel));
//				histograms.addBrightnessValue(ImgUtils.blueOrBrightnessValueFromPixel(pixel));
//			}
//		}
////		histograms.normalize(rows*cols);
////		histograms.acumulate();
//		return histograms;
//	}
//	
//	
//	
//	public QueryResult[] searchImage (int imgID, int resultSize) {
//		QueryResult[] distanceVectors = new QueryResult[imgIDCounter];
//		ImgHistograms q = (ImgHistograms)featureVectorsWrongKey.get(imgID);
//		for (int i = 0; i < imgIDCounter; i++)
//			distanceVectors[i] = new QueryResult(i,ImgUtils.LmDistanceHSB(q, (ImgHistograms)featureVectors.get(i),1.0));
////			distanceVectors[i] = new QueryResult(i,ImgUtils.hammingDistanceHSB(q, (ImgHistogramsHSB)featureVectors.get(i)));
//		
//		Arrays.sort(distanceVectors);
//		if (resultSize == -1 || resultSize/*+1*/ > distanceVectors.length)
//			return Arrays.copyOfRange(distanceVectors, 0/*+1*/, distanceVectors.length);
//		else
//			return Arrays.copyOfRange(distanceVectors, 0/*+1*/, resultSize/*+1*/);
//	}
//	
}
