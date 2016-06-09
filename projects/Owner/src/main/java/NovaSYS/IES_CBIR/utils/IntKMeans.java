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
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import NovaSYS.IES_CBIR.data.ImgHistograms;
import NovaSYS.IES_CBIR.data.BOVW.ClusterPoint;

public class IntKMeans {
   
   protected int numClusters = 256;
   protected int sizeOfSet=0;
   protected Collection<int[]> features;
   protected ClusterPoint[] clusters;
   protected float Threshold;

   public IntKMeans() {
   }
   
   public IntKMeans(int classes, float errorFactor) {
	   
       this.numClusters=classes;
//       Threshold=(float) Math.pow(10d,(-1*errorFactor));
       Threshold = 0.00000019209f;
   }
   
   protected void init() {
       // find first clusters:
       if (sizeOfSet==0) sizeOfSet=features.size();
       clusters = new ClusterPoint[numClusters];
       float probability= (float)numClusters / (float)sizeOfSet;
       int currCluster=0;
       big:
       while (true) {
           for (int[] h : features) {
               if (Math.random()<probability) {
                   clusters[currCluster++]=new ClusterPoint(h);
                   if (currCluster==numClusters) break big;
               }
           }
       }
   }

   /**
    * Do one step and return the overall stress (squared error). You should do this until
    * the error is below a threshold or doesn't change a lot in between two subsequent steps.
    * @return
    */
   protected float clusteringStep() {
       reOrganizeFeatures();
//       System.out.println("Reorganized!");
       recomputeMeans();
//       System.out.println("Recomputed!");
       float overallStress = overallStress();
       resetClusters();
       return overallStress;
   }

   /**
    * Re-shuffle all features.
    */
   protected void reOrganizeFeatures() {
	   for (int[] current : features) {
		   ClusterPoint best = clusters[0];
		   float minDistance = clusters[0].getL1Distance(current);
		   for (int i = 1; i < clusters.length; i++) {
			   float v = clusters[i].getL1Distance(current);
			   if (minDistance > v) {
				   best = clusters[i];
				   minDistance = v;
			   }
		   }
		   best.membersNumber++;
		   for (int i=0;i<current.length;i++) {
			   best.nextLocation[i]+=current[i];
           }
       }
   }

   /**
    * Computes the mean per cluster (averaged vector)
    */
   protected void recomputeMeans() {
       for (ClusterPoint cluster : clusters) {
           if (cluster.membersNumber>0) {
               for (int i=0;i<cluster.nextLocation.length;i++) {
                   cluster.nextLocation[i]/=cluster.membersNumber;
               }
           }
       }
   }
   
   protected void resetClusters() {
       for (ClusterPoint cluster : clusters) {
           if (cluster.membersNumber > 0)
               cluster.swapLocations();
           cluster.nextLocation=new int[cluster.Location.length];
           cluster.membersNumber=0;
       }
   }

   /**
    * Squared error in classification.
    * @return
    */
   protected float overallStress() {
       float stress=0;
       for (int i=0;i<clusters.length;i++) {
           stress+=clusters[i].GetMovement();
       }
       return stress/clusters.length;
   }

   public ClusterPoint[] getClusters() {
       return clusters;
   }
   
   public IntKMeans setThreshold(float a) {
       this.Threshold=a;
       return this;
   }

   
   public String toString() {
       return "KMeans{" + "Classes=" + numClusters + '}';
   }

   
   public void run() {
       try {
           if (features==null) throw new Exception("Features not set.");
           init();
//           System.out.println("KMeans: Initialized!");
           float curStress = this.clusteringStep(), lastStress = Float.MAX_VALUE;
           float goal=Float.MIN_VALUE, stress=Float.MAX_VALUE;
           int itters=0;
           while (stress > goal) {
               lastStress = curStress;
               curStress = this.clusteringStep();
               stress=Math.abs(curStress - lastStress);
               if (itters++==0)     {
                   goal=Threshold*stress;
               }
           }
       } catch (Exception ex) {
           Logger.getLogger(IntKMeans.class.getName()).log(Level.SEVERE, null, ex);
       }
   }

   
   public int[][] getMedians() {
       int[][] ret=new int[clusters.length][];
       int locSize=clusters[0].Location.length;
       for (int i=0;i<clusters.length;i++) {
           ret[i]=Arrays.copyOf(clusters[i].Location, locSize);
       }
       return ret;
   }

   
   public void setTrainingSize(Integer a) {
       sizeOfSet=a.intValue();
   }
   
   public int getNumClusters() {
       return numClusters;
   }
   

   public void setFeatures(Collection<int[]> features) {
       this.features = features;
   }

   public void addMultipleFeatures(Collection<int[][]> values) {
	   features = new ArrayList<int[]>(values.size());
	   for (int[][] imgHists: values)
		   for(int[] hist: imgHists)
			   features.add(hist);
   }
   
   public void addFeaturesFromImgHistograms(Collection<ImgHistograms> values) {
	   features = new ArrayList<int[]>(values.size());
	   for (ImgHistograms imgHists: values)
		   features.add(imgHists.getCondensed());
   }

}