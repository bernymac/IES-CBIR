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

import static NovaSYS.IES_CBIR.utils.ImgUtils.blueOrBrightnessValueFromPixel;
import static NovaSYS.IES_CBIR.utils.ImgUtils.brightBins;
import static NovaSYS.IES_CBIR.utils.ImgUtils.greenOrSaturationValueFromPixel;
import static NovaSYS.IES_CBIR.utils.ImgUtils.hueBins;
import static NovaSYS.IES_CBIR.utils.ImgUtils.redOrHueValueFromPixel;
import static NovaSYS.IES_CBIR.utils.ImgUtils.satBins;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import NovaSYS.IES_CBIR.core.ImgCryptSimple;
import NovaSYS.IES_CBIR.core.StoreRemote;
import NovaSYS.IES_CBIR.data.ImgCiphertext;
import NovaSYS.IES_CBIR.data.QueryResult;
import NovaSYS.IES_CBIR.data.BOVW.BigIntegerIndex;
import NovaSYS.IES_CBIR.data.BOVW.BigIntegerPosting;
import NovaSYS.IES_CBIR.data.BOVW.BigIntegerPostingList;
import NovaSYS.IES_CBIR.data.BOVW.CodebookNode;
import NovaSYS.IES_CBIR.data.BOVW.Index;
import NovaSYS.IES_CBIR.data.BOVW.Posting;
import NovaSYS.IES_CBIR.data.BOVW.PostingList;
import NovaSYS.IES_CBIR.utils.HierarchicalIntKMeans;
import NovaSYS.IES_CBIR.utils.ImgUtils;


public class OwnerRemote {

	private final static String host = "52.4.21.171";
	private final static String mainPath = "/home/ubuntu/";
//	private final static String mainPath = "/local/a28300/";
//	private final static String mainPath = "/Users/bernardo/Dropbox/WorkspacePHD/IES_CBIR/";
	
	private ImgCryptSimple imgCrypt;
	private StoreRemote store;
	
	private static final int start = 0, load=1, encrypt = 2, index = 3, cloud = 4;
	private long startTime, loadTime=0, indexTime=0, encryptTime=0, cloudTime=0;
	

	public OwnerRemote() throws Exception{
		imgCrypt = new ImgCryptSimple();
		store = (StoreRemote)Naming.lookup("//"+host+"/StoreBean");
	}
	
	public void loadImagesIES_CBIR (File[] files, int addToIDs, boolean compress) throws IOException {
		time(start);
		byte[][] imgs = new byte[files.length][];
		//load images to memory
		for (int f = 0; f < files.length; f++) {
			int[][] img = readImg(files[f]);
			int id = addToIDs+Integer.parseInt(files[f].getName().substring(0, files[f].getName().indexOf('.')));
			ImgCiphertext c = new ImgCiphertext(id, img, false);
			time(load);
			
			imgCrypt.encrypt(c);
			time(encrypt);
			if (compress) {
				final byte[] compressedImg = c.compress();
				imgs[f] = compressedImg;
				time(encrypt);
//				store.storeIES_CBIR(id, compressedImg);
//				time(cloud);
			} else {
				store.storeIES_CBIR(id, c.getImg());
				time(cloud);
			}
		}
		if (compress) {
			store.storeAllIES_CBIR(imgs);
			time(cloud);
		}
	}
	
	public void loadImagesPaillier (File[] files, int addToIDs) throws IOException {
		time(start);
		//load images to memory
		for (File f: files) {
			BufferedImage bufferedImg = ImageIO.read(f);
			BigInteger[][] img = new BigInteger[bufferedImg.getWidth()][bufferedImg.getHeight()];
			for (int i = 0; i < bufferedImg.getWidth(); i++) {
				for (int j = 0; j < bufferedImg.getHeight(); j++) {
					img[i][j] = imgCrypt.encryptPaillier(BigInteger.valueOf(ImgUtils.RGBtoHSB(bufferedImg.getRGB(i, j))));
				}
			}
			int id = addToIDs+Integer.parseInt(f.getName().substring(0, f.getName().indexOf('.')));
			time(load);

			store.storePaillier(id, img);
			time(cloud);
		}
	}
	
	
//	public void loadImagesSEMinHash (String path) throws IOException {
//		time(start);
//		List<ImgCiphertext> imgs = new ArrayList<ImgCiphertext>();
//		File dir = new File(path);
//		
//		//load images to memory
//		for (File f: dir.listFiles()) {
//			if (f.getName().contains(".jpg")) {
//				BufferedImage bufferedImg = ImageIO.read(f);
//				int[][] img = new int[bufferedImg.getWidth()][bufferedImg.getHeight()];
//				for (int i = 0; i < bufferedImg.getWidth(); i++) {
//					for (int j = 0; j < bufferedImg.getHeight(); j++) {
//						img[i][j] = ImgUtils.RGBtoHSB(bufferedImg.getRGB(i, j));
//					}
//				}
//				int id = Integer.parseInt(f.getName().substring(0, f.getName().indexOf('.')));
//				imgs.add(new ImgCiphertext(id, img, false));
//			}
//		}
//		time(load);
//		
//		//create histograms
//		List<List<Integer>> hists = new ArrayList<List<Integer>>(imgs.size());
//		for (ImgCiphertext img: imgs) {
//			hists.add(buildImageHistogram(img.getId(), img.getImg()).getCondensedAsList());
//		}
//		time(index);
//		
//		//encrypt images
//		List<byte[]> encImgs = new ArrayList<byte[]>(imgs.size());
//		for (ImgCiphertext img: imgs) {
//			ByteBuffer buffer = ByteBuffer.allocate(4+4*img.getWidth()*img.getHeight());
//			buffer.putInt(img.getId());
//			for (int i = 0; i < img.getWidth(); i++)
//				for (int j = 0; j < img.getHeight(); j++)
//					buffer.putInt(img.getImg()[i][j]);
//			encImgs.add(imgCrypt.encryptAES(img.getId(),buffer.array()));
//		}
//		//encrypt index/hists
//		MinHash<Integer> minHash = new MinHash<Integer>(256,1000);
//		int[][] encIndex = minHash.minHashSets(hists);
//		time(encrypt);
//		
//		//send images, histograms and index to cloud
//		store.storeSEMinHash(encImgs, encIndex);
//		
////		minHash.similarity(hashSet1, hashSet2)
//		
//		time(cloud);
//		print();
//	}
	
	public CodebookNode loadImagesSE (File[] files) throws Exception {
		CodebookNode codebook = buildCodeBook(files);
		time(start);
		Map<Integer,byte[]> documents = new HashMap<Integer,byte[]>();
		Index imgIndex = new Index(codebook.codebookSize());
		for (File f: files) {
			int[][] img = readImg(f);
			final int id = Integer.parseInt(f.getName().substring(0, f.getName().indexOf('.')));
			time(load);

			documents.put(id, imgCrypt.encryptAES(id, Files.readAllBytes(f.toPath())));
			time(encrypt);

			//index keywords
			int[][] hists = buildImageHistograms(img);
			for (int[] fv: hists)
				imgIndex.addTermFreq(ImgUtils.hierchStemHist(codebook,fv),id);
			time(index);
		}
		
		//backup index
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(baos));
		oos.writeObject(imgIndex);
		oos.close();
		final byte[] encIndex = imgCrypt.encryptAES(Integer.MAX_VALUE, baos.toByteArray());
		baos.close();
		time(encrypt);
		
		//teste
//		final ObjectInputStream ois2 = new ObjectInputStream(new BufferedInputStream(new ByteArrayInputStream(
//				imgCrypt.decryptAES(Integer.MAX_VALUE, encIndex))));
//		imgIndex = (Index)ois2.readObject();
//		ois.close();
		
		//index final scores
		for (PostingList postings: imgIndex.allScores()) {
			final double idf = ImgUtils.idfScaledTfIdf(documents.size(), postings.getDf());
			for (Posting p: postings.getPostings())
				p.setScore(ImgUtils.scaledTfIdf(p.getScore(), idf));
		}
		time(index);

		//encrypt final scores
		BigIntegerIndex opeIndex = new BigIntegerIndex(imgIndex.size());
		for (int i = 0; i < imgIndex.size(); i++) {
			final PostingList postings = imgIndex.scores(i);
			final BigIntegerPostingList opeScores = new BigIntegerPostingList();
			for (Posting p: postings.getPostings()) {
				opeScores.addPosting(p.getDocId(), imgCrypt.encryptOPE(BigInteger.valueOf((int)(p.getScore()*1E3))));
//				p.setScore(imgCrypt.encryptOPE(BigInteger.valueOf((int)(p.getScore()*1E3))).intValue());
			}
			opeIndex.setScores(i, opeScores);
		}
		time(encrypt);

		store.storeSE(documents, encIndex, opeIndex);
//		store.storeSE(documents, encIndex, imgIndex);
		time(cloud);
		return codebook;
	}
	
	
	public void loadImagesSE2 (File[] files) throws Exception {
		CodebookNode codebook = loadImagesSE(files);
		
//MANDAR MAIS 1485, 99 a 99
//		for (int i = 0; i < 15; i++)
//			addImages(i*99, 99+i*99, files, codebook);
		
//MANDAR MAIS 1500, 10 a 10
		for (int i = 0; i < 150; i++)
			addImages(i*10, 10+i*10, files, codebook);
	}
	
	
	private void addImages(int startId, int endId, File[] files, CodebookNode codebook) throws Exception {
		//get and decrypt index backup from cloud
		time(start);
		byte[] encIndex = store.getSEIndex();
		time(cloud);
		
		final ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new ByteArrayInputStream(
				imgCrypt.decryptAES(Integer.MAX_VALUE, encIndex))));
		final Index imgIndex = (Index)ois.readObject();
		ois.close();
		time(encrypt);
		
		//index new imgs
		final Map<Integer,byte[]> documents = new HashMap<Integer,byte[]>();
		for (int f = startId; f < endId; f++) {
			int[][] img = readImg(files[f]);
			final int id = files.length+Integer.parseInt(files[f].getName().substring(0, files[f].getName().indexOf('.')));
			time(load);

			documents.put(id, imgCrypt.encryptAES(id, Files.readAllBytes(files[f].toPath())));
			time(encrypt);

			int[][] hists = buildImageHistograms(img);
			for (int[] fv: hists)
				imgIndex.addTermFreq(ImgUtils.hierchStemHist(codebook,fv),id);
			time(index);
		}
		
		//backup index
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(baos));
		oos.writeObject(imgIndex);
		oos.close();
		encIndex = imgCrypt.encryptAES(Integer.MAX_VALUE, baos.toByteArray());
		baos.close();
		time(encrypt);

		//index final scores
		for (PostingList postings: imgIndex.allScores()) {
			final double idf = ImgUtils.idfScaledTfIdf(documents.size(), postings.getDf());
			for (Posting p: postings.getPostings())
				p.setScore(ImgUtils.scaledTfIdf(p.getScore(), idf));
		}
		time(index);

		//encrypt final scores
		BigIntegerIndex opeIndex = new BigIntegerIndex(imgIndex.size());
		for (int i = 0; i < imgIndex.size(); i++) {
			final PostingList postings = imgIndex.scores(i);
			final BigIntegerPostingList opeScores = new BigIntegerPostingList();
			for (Posting p: postings.getPostings()) {
				opeScores.addPosting(p.getDocId(), imgCrypt.encryptOPE(BigInteger.valueOf((int)(p.getScore()*1E3))));
//				p.setScore(imgCrypt.encryptOPE(BigInteger.valueOf((int)(p.getScore()*1E3))).intValue());
			}
			opeIndex.setScores(i, opeScores);
		}
		time(encrypt);

		store.storeSE(documents, encIndex, opeIndex);
//		store.storeSE(documents, encIndex, imgIndex);
		time(cloud);
	}
	
	
//	private ImgHistograms buildImageHistogram (int id, int[][] img) {
//		int rows = img.length;
//		int cols = img[0].length;
//		ImgHistograms histograms = new ImgHistograms(id);
//		for (int i = 0; i < rows; i++) {
//			for (int j = 0; j < cols; j++) { 
//				int pixel = img[i][j];
//				histograms.addHueValue(ImgUtils.redOrHueValueFromPixel(pixel));
//				histograms.addSaturationValue(ImgUtils.greenOrSaturationValueFromPixel(pixel));
//				histograms.addBrightnessValue(ImgUtils.blueOrBrightnessValueFromPixel(pixel));
//			}
//		}
//		return histograms;
//	}	
	
	public CodebookNode buildCodeBook(File[] files) throws Exception {
		File codebookFile = new File(mainPath+"data/imgCodebook");
		if (codebookFile.exists()) {
			final ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream
					(new FileInputStream(codebookFile)));
			CodebookNode codebook = (CodebookNode) ois.readObject();
			ois.close();
			System.out.println("read codebook");
			return codebook;
		} else {
			time(start);
			List<int[][]> features = new ArrayList<int[][]>(files.length);
			for (File f: files) {
				BufferedImage bufferedImg = ImageIO.read(f);
				int[][] img = new int[bufferedImg.getWidth()][bufferedImg.getHeight()];
				for (int i = 0; i < bufferedImg.getWidth(); i++) {
					for (int j = 0; j < bufferedImg.getHeight(); j++) {
						img[i][j] = ImgUtils.RGBtoHSB(bufferedImg.getRGB(i, j));
					}
				}
				time(load);
				int[][] hists = buildImageHistograms(img);
				features.add(hists);
				time(index);
			}
			HierarchicalIntKMeans classifier = new HierarchicalIntKMeans(3, 10);
			classifier.clusterMultipleFeatures(features);
			CodebookNode codebook = classifier.getRoot();
			time(index);
			ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(
					new FileOutputStream(codebookFile)));
			oos.writeObject(codebook);
			oos.close();
			System.out.println("wrote codebook");
			return codebook;
		}
	}
	
	private int[][] readImg (File f) throws IOException {
		BufferedImage bufferedImg = ImageIO.read(f);
		int[][] img = new int[bufferedImg.getWidth()][bufferedImg.getHeight()];
		for (int i = 0; i < bufferedImg.getWidth(); i++) {
			for (int j = 0; j < bufferedImg.getHeight(); j++) {
				img[i][j] = ImgUtils.RGBtoHSB(bufferedImg.getRGB(i, j));
			}
		}
		return img;
	}
	
	private int[][] buildImageHistograms (int[][] img) {
		final int nBlockBySide = 16;
		final int nBlocks = 256;
		final int width = img.length;
		final int height = img[0].length;
		final int blockWidth = (int)Math.ceil((double)width/(double)nBlockBySide);
		final int blockHeight = (int)Math.ceil((double)height/(double)nBlockBySide);
		
		int[][] imgHists = new int[nBlocks][hueBins+satBins+brightBins];
		for (int w=0; w<width; w+=blockWidth) {
			for (int h=0; h<height; h+=blockHeight) {
				final int nBlock = (h/blockHeight)+(w/blockWidth)*nBlockBySide;
				final double widthLimit = w+blockWidth < width ? w+blockWidth : width;
				final double heightLimit = h+blockHeight < height ? h+blockHeight : height;
				for (int i = w; i < widthLimit; i++) {
					for (int j = h; j < heightLimit; j++) { 
						final int hue = redOrHueValueFromPixel(img[i][j])%hueBins;
						final int sat = greenOrSaturationValueFromPixel(img[i][j])%satBins;
						final int bright = blueOrBrightnessValueFromPixel(img[i][j])%brightBins;
						imgHists[nBlock][hue]++;
						imgHists[nBlock][hueBins+sat]++;
						imgHists[nBlock][hueBins+satBins+bright]++;
					}
				}
			}
		}
		return imgHists;
	}
	
	public QueryResult[] searchIES_CBIR (File f, boolean compress) throws Exception {
		clearTime();
		QueryResult[] results = null;
		int[][] img = readImg(f);
		ImgCiphertext c = new ImgCiphertext(-1, img, false);
		time(load);
		
		imgCrypt.encrypt(c);
		time(encrypt);
		if (compress) {
			final byte[] compressedImg = c.compress();
			time(encrypt);
			results = store.searchIES_CBIR(compressedImg,-1);
			time(cloud);
		} else {
			results = store.searchIES_CBIR(c.getImg(),-1);
			time(cloud);
		}
		return results;
	}
	
	public QueryResult[] searchSE (File f, CodebookNode codebook, int resultSize ) throws Exception {
		clearTime();
		int[][] img = readImg(f);
		time(load);
		
		int[][] hists = buildImageHistograms(img);
		int[] vws = new int[hists.length];
		for (int i = 0; i < hists.length; i++)
			vws[i] = ImgUtils.hierchStemHist(codebook,hists[i]);
		time(index);
		
		BigIntegerPostingList[] pls = store.searchSE(vws);
		time(cloud);
		
		for (BigIntegerPostingList pl: pls) {
			for (BigIntegerPosting p: pl.getPostings())
				p.setScore(imgCrypt.decryptOPE(p.getScore()));
		}
		time(encrypt);
		
		Map<Integer,QueryResult> distanceVectors = new HashMap<Integer,QueryResult>();
		for (BigIntegerPostingList pl: pls) {
			final double idf = ImgUtils.idfScaledTfIdf(1491, pl.getDf());
			for (BigIntegerPosting p: pl.getPostings()) {
				final int imgId = p.getDocId();
				final double score = ImgUtils.scaledTfIdf(p.getScore().doubleValue(), idf);
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
		time(index);
		if (resultSize == -1 || resultSize > distanceVectors.size())
			return results;
		else
			return Arrays.copyOfRange(results, 0, resultSize);
	}
	
	private void print () {
		System.out.println("LoadTime: "+((double)loadTime)/ImgUtils.nanoSeconds
				+" EncryptTime: "+((double)encryptTime)/ImgUtils.nanoSeconds
				+" IndexTime: "+((double)indexTime)/ImgUtils.nanoSeconds
				+" CloudTime: "+((double)cloudTime)/ImgUtils.nanoSeconds
				);
	}
	
	private void time(int type) {
		switch (type) {
		case (load):
			loadTime += System.nanoTime() - startTime;
		break;
		case (encrypt):
			encryptTime += System.nanoTime() - startTime;
		break;
		case (index):
			indexTime += System.nanoTime() - startTime;
		break;
		case (cloud):
			cloudTime += System.nanoTime() - startTime;
		break;
		case(start):
		}
		startTime = System.nanoTime();
	}
	
	private void clearTime() {
		loadTime = 0;
		encryptTime = 0;
		indexTime = 0;
		cloudTime = 0;
		time(start);
	}
	
	public static void main (String[] args) throws Exception {
		final String path = mainPath+"datasets/jpg/";
		final File[] files = new File(path).listFiles(new FilenameFilter() { 
			@Override public boolean accept(File dir, String name) {
				return name.contains(".jpg");
		} } );
		final OwnerRemote owner = new OwnerRemote();
		System.out.println("Starting...");
		
		final boolean compress = true;
		owner.loadImagesIES_CBIR(files,0,compress);											//IES-CBIR Holidays
		
//		final boolean compress = false;									//IES-CBIR Holidays 2x
//		owner.loadImagesIES_CBIR(files,0,compress);						//just the lines single-commented
////		ExecutorService threads = Executors.newCachedThreadPool();		
////		for (int i=0; i < 150; i++) {												
//			final int userID = 0;
////			threads.execute(new Runnable() { @Override public void run() {
////				try {
//					owner.loadImagesIES_CBIR(Arrays.copyOfRange(files, userID*10, 10+userID*10),
//							files.length,compress);
////				} catch (IOException e) {
////					e.printStackTrace();
////				}		
////			} });
////		}
////		threads.shutdown();
////		while (!threads.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS))
////			;
		
//		CodebookNode codebook = owner.buildCodeBook(files);													//clustering time
		
//		CodebookNode codebook = owner.loadImagesSE(files);													//SSE-OPE Holidays
		
//		owner.loadImagesSE2(files);													//SSE-OPE Holidays 2x
		
//		owner.loadImagesPaillier(files,0);											//Paillier Holidays
		
//		owner.searchSE(files[0], codebook, -1);
		
		owner.searchIES_CBIR(files[0], compress);
		
		owner.print();
	}

}
