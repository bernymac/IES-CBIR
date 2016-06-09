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

package NovaSYS.IES_CBIR.test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;

import javax.imageio.ImageIO;

import NovaSYS.IES_CBIR.core.ImgLoaderSimple;
import NovaSYS.IES_CBIR.data.ImgCiphertext;
import NovaSYS.IES_CBIR.data.QueryResult;
import NovaSYS.IES_CBIR.utils.ImgUtils;
import NovaSYS.IES_CBIR.utils.Paillier;

public class ImgTest {

//	public static final String mainPath = "/Users/bernardo/Desktop/";//Dropbox/WorkspacePHD/IES_CBIR/";
	public static final String mainPath = "/local/a28300/";
	
//	public static final String dataset = "datasets/wang_dataset/";
	public static final String dataset = "datasets/jpg/";
	
	private static ImgLoaderSimple loader;
	
	protected static void loadDataset (String path) throws IOException {
		loader.loadImages(path);
	}
	
	protected static void encryptDataset () throws IOException {
		loader.encryptImages();
	}
	
	protected static void decryptDataset () throws IOException {
		loader.decryptImages();
	}
	
	protected static void indexDataset () throws IOException {
		loader.buildImageHistograms();
//		loader.createCodebook(3, 10);
//		loader.index();
	}
	
	protected static ImgCiphertext[] retrieveDataset() {
		ImgCiphertext[] images = loader.retrieveImages();
		return images;
	}

	protected static void writeDatasetToDisk(String path) throws IOException {
		loader.storeDataset(path);
	}
	
	protected static void writeEncryptedDatasetToDisk(String fileName) throws IOException {
		loader.storeEncryptedDataset(fileName);
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
	
	protected static void PrecRecQueryHolidayDataset (Map<Integer,Collection<Integer>> queries) throws Exception {
		final double[][] precisions = new double[500][1491];
		final double[][] recalls = new double[500][1491];
		int nQueries = 0;
		for (Entry<Integer,Collection<Integer>> entry: queries.entrySet()) {
			final QueryResult[] qr = loader.searchImage(entry.getKey(), -1);
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
	
	protected static void mapQueryHolidayDataset (String path) throws Exception {
		List<QueryResult[]> searches = new ArrayList<QueryResult[]>(500);
		for (int i = 100000; i <= 149900; i+=100) 									
			searches.add(loader.searchImage(i, -1));
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
	
	protected static QueryResult[][] queryDataset5Rnd () {
		Random gen = new Random();
		QueryResult[][] searches = new QueryResult[50][];
		
		for (int i = 0; i < 10; i++) 										//for each of the 10 classes
			for (int j = 0; j < 5; j++) {									//choose 5 random images
				int rnd = gen.nextInt(100);
				searches[(i*5)+j] = loader.searchImage((i*100)+rnd, 160);	//from their 100 images per class
			}
		return searches;
	}
	
	protected static QueryResult[][] queryDatasetTotal () {
		QueryResult[][] searches = new QueryResult[1000][];
		for (int i = 0; i < 1000; i++) 									
			searches[i] = loader.searchImage(i, 1000);
//			searches[i] = loader.searchIndex(i, 1000);
		return searches;
	}
	
	protected static void calculateAvgPrcRec5Rnd (QueryResult[][] searches){
		List<Double> avgPrecVals = new ArrayList<Double>(10);
		List<Double> avgRecVals = new ArrayList<Double>(10);
		for (int querySet = 16; querySet <= 160; querySet+=16) {
			double sumPrec = 0.0, sumRec = 0.0;
			for (int classID = 0; classID < 10; classID++) {
				for (int imgID = 0; imgID < 5; imgID++) {
					double relevantDocumentsRetrieved = 0.0;
					for (int resultID = 0; resultID < querySet; resultID++) {
						int retrievedImgID = searches[(classID*5)+imgID][resultID].getId();
						if (retrievedImgID >= classID*100 && retrievedImgID < classID*100+100) 
							relevantDocumentsRetrieved++;
					}
					sumPrec += ImgUtils.precision(relevantDocumentsRetrieved, querySet);
					sumRec += ImgUtils.recall(relevantDocumentsRetrieved, 100);
				}
			}
			avgPrecVals.add(sumPrec/50.0);
			avgRecVals.add(sumRec/50.0);
		}
		renderGraphic(avgPrecVals,avgRecVals);
	}
	
	public static void calculateAvgPrcRecTotal (QueryResult[][] searches){
		List<Double> avgPrecVals = new ArrayList<Double>(1000);
		List<Double> avgRecVals = new ArrayList<Double>(1000);
		for (int querySet = 1; querySet <= 1000; querySet++) {
//		for (int querySet = 16; querySet <= 160; querySet+=16) {
			double sumPrec = 0.0, sumRec = 0.0;
			for (int imgID = 0; imgID < 1000; imgID++) {
				double relevantDocumentsRetrieved = 0.0;
				for (int resultID = 0; resultID < querySet; resultID++) {
					if (searches[imgID].length == resultID)
						break;
					int retrievedImgID = searches[imgID][resultID].getId();
					if (isRelevant(imgID, retrievedImgID)) 
						relevantDocumentsRetrieved++;
				}
				sumPrec += ImgUtils.precision(relevantDocumentsRetrieved, querySet);
				sumRec += ImgUtils.recall(relevantDocumentsRetrieved, 100);
			}
			avgPrecVals.add(sumPrec/1000.0);
			avgRecVals.add(sumRec/1000.0);
		}
		renderGraphic(avgPrecVals,avgRecVals);
	}
	
	private static void renderGraphic(List<Double> avgPrecVals, List<Double> avgRecVals) {
		System.out.println("Recall:");
		for (Double d: avgRecVals)
			System.out.println(d);
		System.out.println("Precision:");
		for (Double d: avgPrecVals)
			System.out.println(d);
		
//		class Charter extends Frame {
//			private static final long serialVersionUID = 1L;
//			public Charter() {
//				super("Precision/Recall");
//				setSize(100,100);
//				setLocation(100, 100);
//				addWindowListener(new WindowAdapter() {
//					public void windowClosing(WindowEvent e) {
//						dispose();
//						System.exit(0);
//					}
//				});
//			}
//			public void paint(Graphics g) {
//				Graphics2D g2 = (Graphics2D)g;
//				g2.setPaint(Color.black);
//				for (int i = 0; i < 9; i++)
//					g2.fill(new Line2D.Double(avgRecVals.get(i)*100,avgPrecVals.get(i)*100,avgRecVals.get(i+1)*100,avgPrecVals.get(i+1)*100));
//			}
//		}
//		Charter charter = new Charter();
//		charter.setVisible(true);
	}
	
	public static boolean isRelevant(int queryID, int resultID) {
		if (queryID >= 0 && queryID < 100) {
			if (resultID >= 0 && resultID < 100)
				return true;
		} else if (queryID >= 100 && queryID < 200) {
			if (resultID >= 100 && resultID < 200)
				return true;
		} else if (queryID >= 200 && queryID < 300) {
			if (resultID >= 200 && resultID < 300)
				return true;
		} else if (queryID >= 300 && queryID < 400) {
			if (resultID >= 300 && resultID < 400)
				return true;
		} else if (queryID >= 400 && queryID < 500) {
			if (resultID >= 400 && resultID < 500)
				return true;
		} else if (queryID >= 500 && queryID < 600) {
			if (resultID >= 500 && resultID < 600)
				return true;
		} else if (queryID >= 600 && queryID < 700) {
			if (resultID >= 600 && resultID < 700)
				return true;
		} else if (queryID >= 700 && queryID < 800) {
			if (resultID >= 700 && resultID < 800)
				return true;
		} else if (queryID >= 800 && queryID < 900) {
			if (resultID >= 800 && resultID < 900)
				return true;
		} else if (queryID >= 900 && queryID < 1000) {
			if (resultID >= 900 && resultID < 1000)
				return true;
		}
		return false;
	}
	
	protected static void testPaillier () throws IOException {
		Paillier hom = new Paillier();
		BufferedImage img = ImageIO.read(new File((mainPath+"datasets/wang_dataset/0.jpg")));
		BigInteger[][] encImg = new BigInteger[img.getWidth()][img.getHeight()];
		
		for (int i = 0; i < img.getWidth(); i++) 
			for (int j = 0; j < img.getHeight(); j++) 
				encImg[i][j] = new BigInteger(""+ImgUtils.RGBtoHSB(img.getRGB(i, j)));
		long startEncrypt = System.nanoTime();
		for (int i = 0; i < img.getWidth(); i++) 
			for (int j = 0; j < img.getHeight(); j++) 
				encImg[i][j] = hom.Encryption(encImg[i][j]);
		System.out.println(System.nanoTime()-startEncrypt);
//		log.info("encrypt done. Beginnign decrypt:");
//		for (int i = 0; i < img.getWidth(); i++) 
//			for (int j = 0; j < img.getHeight(); j++) 
//				plainImg[i][j] = hom.Decryption(encImg[i][j]);
//		log.info("decrypt done");
		
//		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(("/home/bernardo/Dropbox/WorkspacePHD/CryptoHealthcareCBIR/datasets/ciphertexts/Paillier"))));
//		dos.writeInt( img.getWidth());
//		dos.writeInt(img.getHeight());
//		for (int i = 0; i < img.getWidth(); i++) {
//			for (int j = 0; j < img.getHeight(); j++) {
//				dos.write(encImg[i][j].toByteArray());
//			}
//		}
//		dos.close();
	}
	
	public static void main (String[] args) throws Exception {
		loader = new ImgLoaderSimple();
//		loader = new ImgLoaderHSBWrongKey();
//		loader = new ImgLoaderHSBPlaintext();
		long startTime = System.nanoTime();
		loadDataset(mainPath+dataset);
		long loadTime = System.nanoTime();
		encryptDataset();
		long encryptTime = System.nanoTime();
//		indexDataset();
		long indexTime = System.nanoTime();
//		mapQueryHolidayDataset(mainPath+"data/IES-Holiday.dat");
//		PrecRecQueryHolidayDataset(holidaysQueries());
//		QueryResult[][] queries = queryDatasetTotal();
		long queryAndCalcTime = System.nanoTime();
//		calculateAvgPrcRecTotal(queries);
		
		
//		decryptDataset();
//		long decryptTime = System.nanoTime();
		writeDatasetToDisk("/Users/bernardo/Desktop/test_retrieved/");
//		long writeDiskTime = System.nanoTime();
//		writeEncryptedDatasetToDisk("/home/bernardo/Dropbox/WorkspacePHD/CryptoHealthcareCBIR/datasets/ciphertexts/dataset_encrypted");
//		testPaillier();
		
		System.out.println("LoadTime: "+(double)(loadTime-startTime)/ImgUtils.nanoSeconds
				+" EncryptTime: "+(double)(encryptTime-loadTime)/ImgUtils.nanoSeconds
				+" IndexTime: "+(double)(indexTime-encryptTime)/ImgUtils.nanoSeconds
				+" QueryTime: "+(double)(queryAndCalcTime-indexTime)/ImgUtils.nanoSeconds); 
//				+" DecryptTime: "+(double)(decryptTime-queryAndCalcTime)/nanoSeconds
//				" WriteDiskTime: "+(double)(writeDiskTime-decryptTime)/nanoSeconds);
		
		
		
//		File dir = new File("/home/bernardo/Dropbox/WorkspacePHD/CryptoHealthcareCBIR/datasets/wang_dataset2/");
//		int i = 0;
//		for (File f: dir.listFiles()) {
//			if (f.getName().contains("(copy)")) {
//				int id = 1000+i;
//				i++;
//				f.renameTo(new File(dir.getAbsolutePath()+"/"+id+".jpg"));
//			}
//		}
	}
	
}
