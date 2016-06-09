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

package NovaSYS.IES_CBIR.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import NovaSYS.IES_CBIR.data.ImgHistograms;
import NovaSYS.IES_CBIR.data.BOVW.CodebookNode;

public class HierarchicalIntKMeans {
	
	private int height;
	private int k;
	private int diff;
	private CodebookNode root;
	
	public HierarchicalIntKMeans (int height, int k) {
		this.setHeight(height);
		this.setK(k);
		setRoot(new CodebookNode(-1, null, 0, k));
		diff = 0;
		int aux = height;
		while (aux > 1) {
			aux--;
			diff += Math.pow(k,aux);
		}
	}
	
	public void clusterImgHistograms(Collection<ImgHistograms> imgHistograms) {
		List<int[]> features = new ArrayList<int[]>(imgHistograms.size());
		for (ImgHistograms imgHists: imgHistograms)
			features.add(imgHists.getCondensed());
		clusterNode(root, features);
	}
	
	public void clusterFeatures(Collection<int[]> features) {
		clusterNode(root, features);
	}
	
	public void clusterMultipleFeatures(Collection<int[][]> values) {
		   List<int[]> features = new LinkedList<int[]>();
		   for (int[][] imgHists: values)
			   for(int[] hist: imgHists)
				   features.add(hist);
		   clusterNode(root, features);
	   }
	
	private void clusterNode(CodebookNode node, Collection<int[]> features) {
		//recursive method - if we have reached a leaf, stop clustering
		if (node.isLeaf())
			return;
		
		//get representative cluster points with training data = features
		IntKMeans classifier = new IntKMeans(k,0f);
		classifier.setFeatures(features);
		classifier.setTrainingSize(features.size());
		classifier.run();
		int[][] medians = classifier.getMedians();
		node.setChilds(medians, diff);
		
		//prepare to divide features between the cluster points
		List<Collection<int[]>> newFeaturesPerClass = new ArrayList<Collection<int[]>>(k);
		for (int i=0; i<k; i++)
			newFeaturesPerClass.add(new LinkedList<int[]>());
		
		//divide features between the cluster points
		for (int[] feature: features)
			newFeaturesPerClass.get(ImgUtils.stemHist(medians, feature)).add(feature);
		
		//further cluster the new cluster points 
		for (int i=0; i<k; i++) 
			clusterNode(node.getChild(i),newFeaturesPerClass.get(i));
	}
	
	public CodebookNode getRoot() {
		return root;
	}

	public void setRoot(CodebookNode root) {
		this.root = root;
	}

	public int getK() {
		return k;
	}

	public void setK(int k) {
		this.k = k;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}
	
	
}