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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import NovaSYS.IES_CBIR.data.ImgCiphertext;
import NovaSYS.IES_CBIR.data.ImgHistograms;
import NovaSYS.IES_CBIR.data.QueryResult;
import NovaSYS.IES_CBIR.data.BOVW.CodebookNode;
import NovaSYS.IES_CBIR.data.BOVW.Index;
import NovaSYS.IES_CBIR.data.BOVW.Posting;
import NovaSYS.IES_CBIR.data.BOVW.PostingList;
import NovaSYS.IES_CBIR.utils.HierarchicalIntKMeans;
import NovaSYS.IES_CBIR.utils.ImgUtils;


public class ImgLoaderSimple {

	private ImgCryptSimple imgCrypt;
	private Map<Integer,ImgHistograms> featureVectors;
	private Map<Integer,ImgCiphertext> images;
	
	private CodebookNode codebookRoot;
	private Index index;
	
	public ImgLoaderSimple() throws Exception{
		imgCrypt = new ImgCryptSimple();
		featureVectors = new HashMap<Integer, ImgHistograms>();
		images = new HashMap<Integer, ImgCiphertext>();
	}
	
	
	public void loadImages (String path) throws IOException {
		//Data Owner
		File[] files = new File(path).listFiles(	new FilenameFilter() { 
			 public boolean accept(File dir, String name) {
				return name.contains(".jpg");
			} } );
		for (File f: files) {
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
	
	
	public void encryptImages() {
		for (ImgCiphertext c: images.values()) 
			imgCrypt.encrypt(c);
	}
	
	
	public void decryptImages() {
		for (ImgCiphertext c: images.values()) 
			imgCrypt.decrypt(c);
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
		if (q == null)
			System.out.println(imgID);
		int i = 0;
		for (ImgHistograms hist: featureVectors.values()) {
			distanceVectors[i] = new QueryResult(hist.getId(),ImgUtils.LmDistanceHSB(q, hist,1.0));
			i++;
		}
		Arrays.sort(distanceVectors, new Comparator<QueryResult>() {
			@Override public int compare(QueryResult o1, QueryResult o2) {
				if (o1.getDistance() > o2.getDistance())
					return 1;
				if (o1.getDistance() < o2.getDistance())
					return -1;
				return 0;
		}});
		if (resultSize == -1 || resultSize > distanceVectors.length)
			return Arrays.copyOfRange(distanceVectors, 0, distanceVectors.length);
		else
			return Arrays.copyOfRange(distanceVectors, 0, resultSize);
	}

	
	public ImgCiphertext retrieveImage(int imgID) {
		return imgCrypt.decrypt(images.get(imgID));
	}

	
	public ImgCiphertext[] retrieveImages() {
		ImgCiphertext[] result = new ImgCiphertext[images.size()];
		int i = 0;
		for (ImgCiphertext c: images.values()) {
			result[i] = imgCrypt.decrypt(c);
			i++;
		}
		return result;
	}
	
	
	public void storeDataset(String path) throws IOException {
		for (ImgCiphertext img: images.values()) {
			int[][] pixels = img.getImg();
			BufferedImage result = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
			for (int i = 0; i <  img.getWidth(); i++) {
				for (int j = 0; j <  img.getHeight(); j++) { 
					result.setRGB(i, j, ImgUtils.HSBtoRGB(pixels[i][j]));
				}
			}
			ImageIO.write(result, "png", new File(path+img.getId()+".png"));
		}
	}
	
	
	public void storeEncryptedDataset(String fileName) throws IOException {
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
		for (ImgCiphertext img: images.values()) {
			dos.writeInt(img.getId());
			dos.writeBoolean(img.isEncrypted());
			dos.writeInt(img.getWidth());
			dos.writeInt(img.getHeight());
			int[][] pixels = img.getImg();
			for (int i = 0; i < img.getWidth(); i++)
				for (int j = 0; j < img.getHeight(); j++)
					dos.writeInt(pixels[i][j]);
		}
		dos.close();
	}
	
	
	public void createCodebook(int height, int classesPerNode) {
//		try {
//			File f = new File("/Users/bernardo/Dropbox/WorkspacePHD/IES_CBIR/data/imgCodebook");
//			if (f.exists()) {	//read from disk if already built
//				ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f)));
//				codebookRoot = (CodebookNode) ois.readObject();
//				ois.close();
//			} else {				//build and then store in disk
		HierarchicalIntKMeans classifier = new HierarchicalIntKMeans(height, classesPerNode);
		classifier.clusterImgHistograms(featureVectors.values());
		codebookRoot = classifier.getRoot();
//				ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
//				oos.writeObject(codebookRoot);
//				oos.close();
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
	
	
	public void index () {
		index = new Index(codebookRoot.codebookSize());
		for (ImgHistograms fv: featureVectors.values()) {
			int vw= ImgUtils.hierchStemHist(codebookRoot,fv.getCondensed()); //convert feature to visual word
			index.addTermFreq(vw,fv.getId());
        }
    }
	
	
	public QueryResult[] searchIndex (int imgID, int resultSize) {
		Map<Integer,QueryResult> distanceVectors = new HashMap<Integer,QueryResult>();
		ImgHistograms q = featureVectors.get(imgID);
		int vw= ImgUtils.hierchStemHist(codebookRoot,q.getCondensed());
		PostingList postingList = index.scores(vw);
		
		final double idf = ImgUtils.idfScaledTfIdf(featureVectors.size(), postingList.getDf());
		for (Posting p: postingList.getPostings()) {
			final int imgId = p.getDocId();
			final double score = ImgUtils.scaledTfIdf(p.getScore(), idf);
			final QueryResult qr = distanceVectors.get(imgId);
			if (qr == null)
				distanceVectors.put(imgId,new QueryResult(imgId,score));
			else 
				qr.addDistance(score);
		}
		
		//sort results and return
		QueryResult[] results = new QueryResult[distanceVectors.size()];
		results = distanceVectors.values().toArray(results);
		Arrays.sort(results, new Comparator<QueryResult>() {
			@Override public int compare(QueryResult o1, QueryResult o2) {
				if (o1.getDistance() < o2.getDistance())
					return 1;
				if (o1.getDistance() > o2.getDistance())
					return -1;
				return 0;
		}});
		if (resultSize == -1 || resultSize > distanceVectors.size())
			return results;
		else
			return Arrays.copyOfRange(results, 0, resultSize);
	}
	
}
