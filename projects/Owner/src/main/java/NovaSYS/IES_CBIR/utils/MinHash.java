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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MinHash<T> {

	private HashFunction[] hashFunctions;
    
	public MinHash(int numHashes, int universeSize) {
		hashFunctions = new HashFunction[numHashes];
		for (int i = 0; i < numHashes; i ++)
			hashFunctions[i] = new HashFunction(universeSize);
	}
	

	public double[][] similarities (int[][] hashSets) {
		double[][] allScores = new double[hashSets.length][hashSets.length];
		for (int i = 0; i < hashSets.length; i++) 
			for (int j = 0; j < hashSets.length; j++) 
				allScores[i][j] = similarity(hashSets[i], hashSets[j]);
		
		return allScores;
	}
	
	
	public double similarity (int[] hashSet1, int[] hashSet2) {
		return computeSimilarityFromSignatures(hashSet1, hashSet2);
	}
	
	
    public double similarity(List<T> set1, List<T> set2){
    		Set<T> bitArray = new HashSet<T>();
        int numSets = 2;
        buildBitMap(set1, bitArray);
        buildBitMap(set2, bitArray);
        int[][] minHashValues = initializeHashBuckets(numSets);
        computeMinHashForSet(set1, 0, minHashValues, bitArray);
        computeMinHashForSet(set2, 1, minHashValues, bitArray);
        return similarity(minHashValues[0], minHashValues[1]);
    }
    
    
    public int[][] minHashSets (List<List<T>> sets) {
		Set<T> bitArray = new HashSet<T>();
		for (List<T> set: sets)
			 buildBitMap(set, bitArray);
		int[][] minHashValues = initializeHashBuckets(sets.size());
		int i = 0;
		for (List<T> set: sets) {
			computeMinHashForSet(set, i, minHashValues, bitArray);
			i++;
		}
		return minHashValues;
	}
    
    
	private void computeMinHashForSet(List<T> set, int setIndex, int[][] minHashValues, Set<T> bitArray){
		int index = 0;
		for (T element: bitArray) {
			for (int i = 0; i < hashFunctions.length; i++) {
				if(set.contains(element)) {
					int hindex = hashFunctions[i].hash(index);
					if (hindex < minHashValues[setIndex][i])
						minHashValues[setIndex][i] = hindex;
				}
			}
			index++;
		}
	}
	
    
	private int[][] initializeHashBuckets(int numSets) {
		int[][] minHashValues = new int[numSets][hashFunctions.length];
		
        for (int i = 0; i < numSets; i++)
        		for (int j = 0; j < hashFunctions.length; j++)
        			minHashValues[i][j] = Integer.MAX_VALUE;
        
        return minHashValues;
    }
	
	
	private void buildBitMap(List<T> set, Set<T> bitArray) {
		for(T t: set){
			bitArray.add(t);
		}
	}
	
	
	private double computeSimilarityFromSignatures(int[] minHashSet1, int[] minHashSet2) {
		int identicalMinHashes = 0;
        for (int i = 0; i < hashFunctions.length; i++){
            if (minHashSet1[i] == minHashSet2[i]) {
                identicalMinHashes++;
            }
        }
        return (1.0 * identicalMinHashes) / hashFunctions.length;
    }
	
		
//	public static void main(String[] args){
//		List<String> set1 = new ArrayList<String>(3);
//		set1.add("FRANCISCO");
//		set1.add("MISSION");
//		set1.add("SAN");
//		
//		List<String> set2 = new ArrayList<String>(4);
//		set2.add("FRANCISCO");
//		set2.add("MIION");
//		set2.add("SAN");
//		set2.add("USA");
//
//		MinHash<String> minHash = new MinHash<String>(set1.size()+set2.size());
//		System.out.println(minHash.similarity(set1, set2));
//	}
}
