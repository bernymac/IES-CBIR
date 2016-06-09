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
//
//import java.io.File;
//import java.util.TreeSet;
//
//import org.grire.BagOfVisualWords.BOVWSystem;
//import org.grire.BagOfVisualWords.CustomDescriptor;
//import org.grire.BagOfVisualWords.EuclidianStemmer;
//import org.grire.BagOfVisualWords.SMARTModel;
//import org.grire.BagOfVisualWords.Functions.ClusteringCodebookFactory;
//import org.grire.BagOfVisualWords.Functions.Importer;
//import org.grire.BagOfVisualWords.Functions.IndexFactory;
//import org.grire.BagOfVisualWords.Functions.PoolFeatureExtractor;
//import org.grire.BagOfVisualWords.Functions.QueryPerformer;
//import org.grire.BagOfVisualWords.Interfaces.VisualWordDescriptor;
//import org.grire.BagOfVisualWords.Interfaces.WeightingScheme;
//import org.grire.BagOfVisualWords.Structures.Codebook;
//import org.grire.BagOfVisualWords.Structures.ImagePool;
//import org.grire.BagOfVisualWords.Structures.Index;
//import org.grire.BagOfVisualWords.Structures.PoolFeatures;
//import org.grire.GeneralUtilities.CosineSimilarity;
//import org.grire.GeneralUtilities.KMeans;
//import org.grire.GeneralUtilities.SIFTParser;
//import org.grire.GeneralUtilities.GeneralStorers.GeneralMapDBStorer;
//import org.grire.GeneralUtilities.GeneralStorers.GeneralStorer;
//import org.grire.GeneralUtilities.Interfaces.ClusteringAlgorithm;
//import org.grire.GeneralUtilities.Interfaces.FeatureExtractor;
//import org.grire.GeneralUtilities.Interfaces.SimilarityMeasure;
//
public class Grire {
//
//	public static void main(String[] args) throws Exception{
//		//Create a new database file
//		GeneralStorer storer=new GeneralMapDBStorer("/Users/bernardo/database1");
//		long start = System.nanoTime();
//		//Create a new image pool and import the images from two folders.
//		ImagePool imagePool;
//		imagePool = new ImagePool(storer,"UKBench",true);
//		Importer importer=new Importer(
//				new String[]{
//						"/Users/bernardo/Dropbox/WorkspacePHD/CryptoHealthcareCBIR/datasets/wang_dataset"},
//				imagePool);
//		importer.run();
//		long images = System.nanoTime();
//		//Extract the features of the pool using
//		//the SURF local feature extractor and descriptor.
//		PoolFeatures poolFeatures=new PoolFeatures(storer,"UKBench_SURF",true);
//		FeatureExtractor extractor=new SIFTParser();
//		PoolFeatureExtractor pfe=new PoolFeatureExtractor(imagePool,
//				extractor,
//				poolFeatures);
//		//Add a listener that prints the progress at console.
//		//The same listener can be used to all the Function classes of GRire
//		//that implements the Listened class.
////		pfe.addProgressListener(new ProgressMadeListener() {
////			@Override
////			public void OnProgressMade(int progress, int total, String message) {
////				System.out.println(message);
////			}
////		});
//		pfe.run();
//		long features = System.nanoTime();
//		//Create a codebook using KMeans with 40% of the set as training set
//		//and producing 400 classes.
//		Codebook cb=new Codebook(storer,"SURF_400",true);
//		ClusteringAlgorithm kmeans=new KMeans(1000f,0f);
//		ClusteringCodebookFactory ccf=new ClusteringCodebookFactory(
//				poolFeatures,
//				kmeans,
//				cb);
//		ccf.setPercentageOfData(1f);
//		ccf.run();
//		
//		//Create the Index of the database using a Custom Descriptor
//		//with SURF features(of course) and Euclidian Stemmer.
//		Index in=new Index(storer,"UKBench_SURF_400",true);
//		VisualWordDescriptor desc=new CustomDescriptor(
//				extractor,
//				new EuclidianStemmer(),
//				cb);
//		IndexFactory indexFactory=new IndexFactory(poolFeatures,
//				desc,
//				in);
//		indexFactory.run();
//		
//		//Create a complete BOVW System with these components.
//		BOVWSystem system=new BOVWSystem(imagePool,
//				desc,
//				in);
//		long index = System.nanoTime();
////		System.out.println("search");
//		//Initialize a Query Performer over the system.
//		//Use the weighting scheme smart with notation ntc
//		//for query and indexed vector.
//		//and Cosine similarity to compare the vectors.
//		WeightingScheme scheme=new SMARTModel("ntc:ntc");
//		SimilarityMeasure sim=new CosineSimilarity();
//		QueryPerformer qp=new QueryPerformer(system,scheme,sim);
////
////		//Make a simple query to the database.
////		===>substituir
//		TreeSet<QueryPerformer.setItem> results = qp.NewQuery(new File("/Users/bernardo/Dropbox/WorkspacePHD/CryptoHealthcareCBIR/datasets/new/0.jpg"),12);
//		for (QueryPerformer.setItem r: results) {
//			System.out.println(r.id+" "+r.score);
//		}
//		
////		Collection<float[]> invIndex = storer.GetMap(in.getDBName()).values();
////		List<List<Float>> invIndex2 = new ArrayList<List<Float>>(invIndex.size());
////		for (float[] entry: invIndex) {
////			List<Float> set = new ArrayList<Float>(entry.length); 
////			for (float f: entry)
////				set.add(f);
////			invIndex2.add(set);
////		}
////		MinHash<Float> minHash = new MinHash<Float>(1024,10);
////		int[][] encIndex = minHash.minHashSets(invIndex2);
////		long encrypt = System.nanoTime();
////		double[][] queries = minHash.similarities(encIndex);
////		long search = System.nanoTime();
////		for (double[] query: queries)
////			for (double score: query)
////				System.out.println(score);
//		
////		System.out.println("LoadImages: "+(images-start)
////				+" ExtractFeatures: "+(features-images)
////				+" Index: "+(index-features)
////				+" Encrypt: "+(encrypt-index)
////				+" Search: "+(search-encrypt));
//		
//		//Make a multiple query from file and output a TREC file
//		//with 100 images per query.
//		//This function returns a Listened object so it can run asynchronously
////		Listened listened = qp.NewTRECQuery("TestExperiment",
////				new FileInputStream("c:/queries.txt"),
////				new FileOutputStream("c:/trecresults.txt"),
////				100);
////		listened.run();
//	}
}
