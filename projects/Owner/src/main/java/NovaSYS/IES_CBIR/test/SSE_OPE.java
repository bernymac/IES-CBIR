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

package NovaSYS.IES_CBIR.test;

import static NovaSYS.IES_CBIR.utils.ImgUtils.HSBtoRGB;
import static NovaSYS.IES_CBIR.utils.ImgUtils.RGBtoHSB;
import static NovaSYS.IES_CBIR.utils.ImgUtils.blueOrBrightnessValueFromPixel;
import static NovaSYS.IES_CBIR.utils.ImgUtils.brightBins;
import static NovaSYS.IES_CBIR.utils.ImgUtils.greenOrSaturationValueFromPixel;
import static NovaSYS.IES_CBIR.utils.ImgUtils.hueBins;
import static NovaSYS.IES_CBIR.utils.ImgUtils.nanoSeconds;
import static NovaSYS.IES_CBIR.utils.ImgUtils.redOrHueValueFromPixel;
import static NovaSYS.IES_CBIR.utils.ImgUtils.satBins;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.imageio.ImageIO;

import NovaSYS.IES_CBIR.data.ImgCiphertext;
import NovaSYS.IES_CBIR.data.QueryResult;
import NovaSYS.IES_CBIR.data.BOVW.CodebookNode;
import NovaSYS.IES_CBIR.data.BOVW.Index;
import NovaSYS.IES_CBIR.data.BOVW.Posting;
import NovaSYS.IES_CBIR.data.BOVW.PostingList;
import NovaSYS.IES_CBIR.utils.HierarchicalIntKMeans;
import NovaSYS.IES_CBIR.utils.ImgUtils;


public class SSE_OPE {
	
	public static final String mainPath = "/Users/bernardo/Dropbox/WorkspacePHD/IES_CBIR/";
//	public static final String mainPath = "/local/a28300/";
	
	public static final String dataset = "datasets/wang/";
//	public static final String dataset = "datasets/jpg/";
	
	
	public static final int nBlocks = 256;	
	public static final int nBlockBySide = (int) Math.sqrt(nBlocks); //assert it gives int value!
	
	private ImgCryptSSE_OPE imgCrypt;
	private Map<Integer,int[][]> featureVectors;
	private Map<Integer,ImgCiphertext> images;
	
	private CodebookNode codebookRoot;
	private Index index;
	
	public SSE_OPE() throws Exception{
		imgCrypt = new ImgCryptSSE_OPE();
		featureVectors = new HashMap<Integer, int[][]>();
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
					img[i][j] = RGBtoHSB(bufferedImg.getRGB(i, j));
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
	
	
	
	public int[][] buildImageHistogram (ImgCiphertext img) {
		int[][] pixels = img.getImg();
		final int width = img.getWidth();
		final int height = img.getHeight();
		final int blockWidth = (int)Math.ceil((double)width/(double)nBlockBySide);
		final int blockHeight = (int)Math.ceil((double)height/(double)nBlockBySide);
		
//		int[][] imgHists = new int[nBlocks][hueBins*satBins*brightBins];
		int[][] imgHists = new int[nBlocks][hueBins+satBins+brightBins];
		for (int w=0; w<width; w+=blockWidth) {
			for (int h=0; h<height; h+=blockHeight) {
				final int nBlock = (h/blockHeight)+(w/blockWidth)*nBlockBySide;
				final double widthLimit = w+blockWidth < width ? w+blockWidth : width;
				final double heightLimit = h+blockHeight < height ? h+blockHeight : height;
				for (int i = w; i < widthLimit; i++) {
					for (int j = h; j < heightLimit; j++) { 
						final int hue = redOrHueValueFromPixel(pixels[i][j])%hueBins;
						final int sat = greenOrSaturationValueFromPixel(pixels[i][j])%satBins;
						final int bright = blueOrBrightnessValueFromPixel(pixels[i][j])%brightBins;
//						final int nBin = hue+hueBins*sat+hueBins*satBins*bright;
//						imgHists[nBlock][nBin]++;
						imgHists[nBlock][hue]++;
						imgHists[nBlock][hueBins+sat]++;
						imgHists[nBlock][hueBins+satBins+bright]++;
					}
				}
			}
		}
		return imgHists;
	}
	
	
//	public QueryResult[] searchImage (int imgID, int resultSize) {
//		QueryResult[] distanceVectors = new QueryResult[featureVectors.size()];
//		int[][] q = featureVectors.get(imgID);
//		for (int i = 0; i < featureVectors.size(); i++)
//			distanceVectors[i] = new QueryResult(i,ImgUtils.LmDistanceHSB(q, (ImgHistograms)featureVectors.get(i),1.0));
//		
//		Arrays.sort(distanceVectors);
//		if (resultSize == -1 || resultSize > distanceVectors.length)
//			return Arrays.copyOfRange(distanceVectors, 0, distanceVectors.length);
//		else
//			return Arrays.copyOfRange(distanceVectors, 0, resultSize);
//	}

	
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
					result.setRGB(i, j, HSBtoRGB(pixels[i][j]));
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
		try {
			File f = new File(mainPath+"data/imgCodebook");
			if (f.exists()) {	//read from disk if already built
				ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f)));
				codebookRoot = (CodebookNode) ois.readObject();
				ois.close();
			} else {		//build and then store in disk
				HierarchicalIntKMeans classifier = new HierarchicalIntKMeans(height, classesPerNode);
				classifier.clusterMultipleFeatures(featureVectors.values());
				codebookRoot = classifier.getRoot();
				ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
				oos.writeObject(codebookRoot);
				oos.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void index() {
		try {
			File f = new File(mainPath+"data/imgIndex");
			if (f.exists()) {	//read from disk if already built
				ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f)));
				index = (Index) ois.readObject();
				ois.close();
			} else {		//build and then store in disk
				index = new Index(codebookRoot.codebookSize());
				for (Entry<Integer,int[][]> imgHists: featureVectors.entrySet()) {
					for (int[] fv: imgHists.getValue()) {
						int vw= ImgUtils.hierchStemHist(codebookRoot,fv); //convert feature to visual word
						index.addTermFreq(vw,imgHists.getKey());
					}
				}
				ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
				oos.writeObject(index);
				oos.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	public QueryResult[] searchIndex (int imgID, int resultSize) {
		//get query feature vectors and visual words
		Map<Integer,QueryResult> distanceVectors = new HashMap<Integer,QueryResult>();
		final int[][] q = featureVectors.get(imgID);
		int[] vws = new int[q.length];
		for (int i = 0; i < q.length; i++)
			vws[i] = ImgUtils.hierchStemHist(codebookRoot,q[i]);
		
		//access index with VWs
		for (int vw: vws) {
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
		}
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
	
	
	protected static QueryResult[][] queryDatasetTotal (SSE_OPE test) {
		QueryResult[][] searches = new QueryResult[1000][];
		for (int i = 0; i < 1000; i++) 									
//			searches[i] = loader.searchImage(i, 1000);
			searches[i] = test.searchIndex(i, 1000);
		return searches;
	}
	
	protected static void mapQueryHolidayDataset (String path, SSE_OPE test) throws Exception {
		List<QueryResult[]> searches = new ArrayList<QueryResult[]>(500);
		for (int i = 100000; i <= 149900; i+=100) 									
			searches.add(test.searchIndex(i, -1));
		PrintWriter pw = new PrintWriter(new File(path));
		int i = 100000; 
		for (QueryResult[] result: searches) {
			pw.print(i+".jpg");
			for (int j = 0; j < result.length; j++)
				pw.print(" "+j+" "+result[j].getId()+".jpg");
			pw.println();
			i+=100;
		}
		pw.close();
	}
	
	protected static Map<Integer,Collection<Integer>> holidaysQueries() throws FileNotFoundException {
		Map<Integer,Collection<Integer>> queries = new HashMap<Integer,Collection<Integer>>(500);
		Scanner sc = new Scanner(new File(mainPath+"data/perfect_result.dat"),"ISO-8859-1");
		while (sc.hasNext()) {
			final String[] line = sc.nextLine().split(" ");
			int queryID = Integer.parseInt(line[0].substring(0, line[0].indexOf('.')));
			Collection<Integer> relevanceSet = new ArrayList<Integer>((line.length-1)/2);
			for (int i = 2; i < line.length; i+=2) 
				relevanceSet.add(Integer.parseInt(line[i].substring(0, line[i].indexOf('.'))));
			queries.put(queryID, relevanceSet);
		}
		sc.close();
		return queries;
	}
	
	protected static void PrecRecQueryHolidayDataset (SSE_OPE loader, Map<Integer,Collection<Integer>> queries) throws Exception {
		final double[][] precisions = new double[500][1491];
		final double[][] recalls = new double[500][1491];
		int nQueries = 0;
		for (Entry<Integer,Collection<Integer>> entry: queries.entrySet()) {
			final QueryResult[] qr = loader.searchIndex(entry.getKey(), -1);
			final Collection<Integer> classe = entry.getValue();
			double relevantDocumentsRetrieved = 0.0;
			for (int i = 0; i < qr.length; i++) {
				if (classe.contains(qr[i].getId()))
					relevantDocumentsRetrieved++;
				final double recall = ImgUtils.recall(relevantDocumentsRetrieved, classe.size());
					precisions[nQueries][i] = ImgUtils.precision(relevantDocumentsRetrieved, i+1);
					recalls[nQueries][i] = recall;
			}
			nQueries++;
		}
		double[] avgPrecisions = new double[1491];
		double[] avgRecalls = new double[1491];
		for (int level = 0; level < 1491; level++) {
			for (int query = 0; query < nQueries; query++) {
				avgPrecisions[level] += precisions[query][level];
				avgRecalls[level] += recalls[query][level];
			}
			avgPrecisions[level] /= nQueries;
			avgRecalls[level] /= nQueries;
		}
		System.out.println("Recall:");
		for (double d: avgRecalls)
			System.out.println(d);
		System.out.println("Precision:");
		for (double d: avgPrecisions)
			System.out.println(d);
	}
	
	public static void main (String[] args) throws Exception {
		SSE_OPE test = new SSE_OPE();
		long startTime = System.nanoTime();
		test.loadImages(mainPath+dataset);
		System.out.println("loaded images");
		long loadTime = System.nanoTime();
//		test.encryptImages();
//		test.storeDataset(homeFolder+"datasets/test_retrieved/");
//		System.out.println("encrypted images");
		long encryptTime = System.nanoTime();
		test.buildImageHistograms();
		System.out.println("extracted features");
		test.createCodebook(3,10);
		System.out.println("created codebook");
		test.index();
		System.out.println("indexed");
		long indexTime = System.nanoTime();
//		PrecRecQueryHolidayDataset(test, holidaysQueries());
//		mapQueryHolidayDataset(mainPath+"data/SSE-OPE-Holiday.dat",test);
		QueryResult[][] queries = queryDatasetTotal(test);
		System.out.println("queried");
		long queryAndCalcTime = System.nanoTime();
		ImgTest.calculateAvgPrcRecTotal(queries);
		
		System.out.println("LoadTime: "+(double)(loadTime-startTime)/nanoSeconds
				+" EncryptTime: "+(double)(encryptTime-loadTime)/nanoSeconds
				+" IndexTime: "+(double)(indexTime-encryptTime)/nanoSeconds
				+" QueryTime: "+(double)(queryAndCalcTime-indexTime)/nanoSeconds); 
	}
	
}
