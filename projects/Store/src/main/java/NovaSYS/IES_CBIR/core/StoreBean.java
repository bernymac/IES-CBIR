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

import static NovaSYS.IES_CBIR.utils.ImgUtils.blueOrBrightnessValueFromPixel;
import static NovaSYS.IES_CBIR.utils.ImgUtils.brightBins;
import static NovaSYS.IES_CBIR.utils.ImgUtils.greenOrSaturationValueFromPixel;
import static NovaSYS.IES_CBIR.utils.ImgUtils.hueBins;
import static NovaSYS.IES_CBIR.utils.ImgUtils.redOrHueValueFromPixel;
import static NovaSYS.IES_CBIR.utils.ImgUtils.satBins;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import NovaSYS.IES_CBIR.data.QueryResult;
import NovaSYS.IES_CBIR.data.BOVW.BigIntegerIndex;
import NovaSYS.IES_CBIR.data.BOVW.BigIntegerPostingList;
import NovaSYS.IES_CBIR.data.BOVW.CodebookNode;
import NovaSYS.IES_CBIR.data.BOVW.Index;
import NovaSYS.IES_CBIR.data.BOVW.Posting;
import NovaSYS.IES_CBIR.data.BOVW.PostingList;
import NovaSYS.IES_CBIR.utils.ImgUtils;

public class StoreBean extends UnicastRemoteObject implements StoreRemote {

	private static final long serialVersionUID = 2382241375698880902L;
	
	private final static String mainPath = "/home/ubuntu/";
	
//	private Map<Integer,byte[]> encImgs;
	private byte[] backupIndex;
	private BigIntegerIndex encIndex;
	
//	Map<Integer,int[][]> imgCiphertexts;
	private CodebookNode codebook;
	private Index index;
	
	protected StoreBean() throws Exception {
//		encImgs = new HashMap<Integer,byte[]>();
//		imgCiphertexts = new HashMap<Integer,int[][]>();
		File codebookFile = new File(mainPath+"data/imgCodebook");
		final ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream
				(new FileInputStream(codebookFile)));
		codebook = (CodebookNode) ois.readObject();
		ois.close();
		index = new Index(codebook.codebookSize());
	}

//	@Override
//	public void storeIES_CBIR(List<int[][]> imgs) throws RemoteException {
//		images.put(img.getId(), img);
//		featureVectors.put(img.getId(), buildImageHistogram(img));
//		return img.getId();
//		System.out.println("Store IES-CBIR");
//	}
	

//	@Override
//	public void storeSE(List<byte[]> encImgs, byte[] encHists, int[][] encIndex) throws RemoteException {
//		this.encImgs.addAll(encImgs);
//		this.encHists = encHists;
//		this.encIndex = encIndex;
//	}

//	@Override
//	public byte[] getFeatures() throws RemoteException {
//		return encHists;
//	}

//	@Override
//	public void storeSEMinHash(List<byte[]> encImgs, int[][] encIndex)
//			throws RemoteException {
//		this.encImgs.addAll(encImgs);
//		this.encIndex = encIndex;
//	}
	
	@Override
	public void storeIES_CBIR(int id, int[][] img) throws RemoteException {
		int[][] hists = buildImageHistograms(img);
		for (int[] fv: hists)
			index.addTermFreq(ImgUtils.hierchStemHist(codebook,fv),id);
	}
	
	@Override
	public void storeIES_CBIR(int id, byte[] compressedImg) throws RemoteException {
		try {
			BufferedImage bufferedImg = ImageIO.read(new ByteArrayInputStream(compressedImg));
			int[][] img = new int[bufferedImg.getWidth()][bufferedImg.getHeight()];
			for (int i = 0; i < bufferedImg.getWidth(); i++) {
				for (int j = 0; j < bufferedImg.getHeight(); j++) {
					img[i][j] = ImgUtils.RGBtoHSB(bufferedImg.getRGB(i, j));
				}
			}
			int[][] hists = buildImageHistograms(img);
			for (int[] fv: hists)
				index.addTermFreq(ImgUtils.hierchStemHist(codebook,fv),id);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void storeSE(Map<Integer, byte[]> documents, byte[] backupIndex,	BigIntegerIndex encIndex) throws RemoteException {
		this.backupIndex = backupIndex;
		this.encIndex = encIndex;
	}
	

	@Override
	public byte[] getSEIndex() throws RemoteException {
		return backupIndex;
	}

	@Override
	public void storePaillier(int id, BigInteger[][] img) throws RemoteException {
		// TODO Auto-generated method stub
	}

	@Override
//	public void storeAllIES_CBIR(Map<Integer, byte[]> imgs) throws RemoteException {
	public void storeAllIES_CBIR(byte[][] imgs) throws RemoteException {
	}

	@Override
	public QueryResult[] searchIES_CBIR(int[][] img, int resultSize) throws RemoteException {
		int[][] fvs = buildImageHistograms(img);
		
		Map<Integer,QueryResult> distanceVectors = new HashMap<Integer,QueryResult>();
		for (int[] fv: fvs) {
			int vw = ImgUtils.hierchStemHist(codebook,fv);
			PostingList postingList = index.scores(vw);
			final double idf = ImgUtils.idfScaledTfIdf(1491, postingList.getDf());
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

	@Override
	public QueryResult[] searchIES_CBIR(byte[] compressedImg, int resultSize) throws RemoteException {
		try {
			BufferedImage bufferedImg = ImageIO.read(new ByteArrayInputStream(compressedImg));
			int[][] img = new int[bufferedImg.getWidth()][bufferedImg.getHeight()];
			for (int i = 0; i < bufferedImg.getWidth(); i++) {
				for (int j = 0; j < bufferedImg.getHeight(); j++) {
					img[i][j] = ImgUtils.RGBtoHSB(bufferedImg.getRGB(i, j));
				}
			}
			return searchIES_CBIR(img,resultSize);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public BigIntegerPostingList[] searchSE(int[] vws) throws RemoteException {
		BigIntegerPostingList[] results = new BigIntegerPostingList[vws.length];
		for (int i = 0; i < vws.length; i++) 
			results[i] = encIndex.scores(vws[i]);
		return results;
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

}
