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

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import NovaSYS.IES_CBIR.data.ImgCiphertext;
import NovaSYS.IES_CBIR.data.ImgHistograms;
import NovaSYS.IES_CBIR.data.QueryResult;
import NovaSYS.IES_CBIR.utils.ImgUtils;


public class ImgLoaderPlaintext {

	private Map<Integer,ImgHistograms> featureVectors;
	private Map<Integer,ImgCiphertext> images;
	
	public ImgLoaderPlaintext() throws Exception{
		featureVectors = new HashMap<Integer, ImgHistograms>();
		images = new HashMap<Integer, ImgCiphertext>();
	}
	
	
	public void loadImages (String path) throws IOException {
		//Data Owner
		File dir = new File(path);
		for (File f: dir.listFiles()) {
			if (f.getName().contains(".jpg")) {
				BufferedImage bufferedImg = ImageIO.read(f);
				int[][] img = new int[bufferedImg.getWidth()][bufferedImg.getHeight()];
				for (int i = 0; i < bufferedImg.getWidth(); i++) {
					for (int j = 0; j < bufferedImg.getHeight(); j++) {
						img[i][j] = ImgUtils.RGBtoHSB(bufferedImg.getRGB(i, j));
					}
				}
				int id = Integer.parseInt(f.getName().substring(0, f.getName().indexOf('.')));
				images.put(id, new ImgCiphertext(id, img, false));
			}
		}
	}
	
	
	public void encryptImages() {
	}
	
	
	public void decryptImages() {
	}
	
	
	public void buildImageHistograms() {
		for (ImgCiphertext c: images.values()) 
			featureVectors.put(c.getId(), buildImageHistogram(c));
	}
	
	
	public ImgHistograms buildImageHistogram (ImgCiphertext img) {
		int[][] pixels = img.getImg();
		int width = img.getWidth();
		int height = img.getHeight();
		ImgHistograms histograms = new ImgHistograms(img.getId());
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) { 
				int pixel = pixels[i][j];
				histograms.addHueValue(ImgUtils.redOrHueValueFromPixel(pixel));
				histograms.addSaturationValue(ImgUtils.greenOrSaturationValueFromPixel(pixel));
				histograms.addBrightnessValue(ImgUtils.blueOrBrightnessValueFromPixel(pixel));
			}
		}
//		histograms.normalize(rows*cols);
//		histograms.acumulate();
		return histograms;
	}
	
	
	public QueryResult[] searchImage (int imgID, int resultSize) {
		QueryResult[] distanceVectors = new QueryResult[featureVectors.size()];
		ImgHistograms q = (ImgHistograms)featureVectors.get(imgID);
		for (int i = 0; i < featureVectors.size(); i++)
			distanceVectors[i] = new QueryResult(i,ImgUtils.LmDistanceHSB(q, (ImgHistograms)featureVectors.get(i),1.0));
//			distanceVectors[i] = new QueryResult(i,ImgUtils.hammingDistanceHSB(q, (ImgHistogramsHSB)featureVectors.get(i)));
		
		Arrays.sort(distanceVectors);
		if (resultSize == -1 || resultSize/*+1*/ > distanceVectors.length)
			return Arrays.copyOfRange(distanceVectors, 0/*+1*/, distanceVectors.length);
		else
			return Arrays.copyOfRange(distanceVectors, 0/*+1*/, resultSize/*+1*/);
	}

	
	public ImgCiphertext retrieveImage(int imgID) {
		return images.get(imgID);
	}

	
	public ImgCiphertext[] retrieveImages() {
		ImgCiphertext[] result = new ImgCiphertext[images.size()];
		int i = 0;
		for (ImgCiphertext c: images.values()) {
			result[i] = c;
			i++;
		}
		return result;
	}
	
	
	public void storeDataset(String path) throws IOException {
		ImgCiphertext[] dataset = retrieveImages();
		for (int w = 0; w < dataset.length; w++) {
			int[][] pixels = dataset[w].getImg();
			BufferedImage result = new BufferedImage( dataset[w].getWidth(), dataset[w].getHeight(), BufferedImage.TYPE_3BYTE_BGR);
			for (int i = 0; i <  dataset[w].getWidth(); i++) {
				for (int j = 0; j <  dataset[w].getHeight(); j++) { 
					result.setRGB(i, j, ImgUtils.HSBtoRGB(pixels[i][j]));
				}
			}
			ImageIO.write(result, "jpg", new File(path+w+".jpg"));
		}
	}
	
	
	public void storeEncryptedDataset(String fileName) throws IOException {
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
		for (ImgCiphertext img: images.values()) {
			dos.writeInt(img.getWidth());
			dos.writeInt(img.getHeight());
			int[][] pixels = img.getImg();
			for (int i = 0; i < img.getWidth(); i++)
				for (int j = 0; j < img.getHeight(); j++)
					dos.writeInt(pixels[i][j]);
		}
		dos.close();
	}

}
