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

package NovaSYS.IES_CBIR.data.BOVW;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Index implements Serializable {

	private static final long serialVersionUID = -5053380674002721281L;

	private PostingList[] scores;


	public Index(int sizeCodebook) {
		scores = new PostingList[sizeCodebook];
		for (int i = 0; i < sizeCodebook; i++)
			scores[i] = new PostingList();
	}

	public void addTermFreq(int vw, int imgId) {
		PostingList postingList = scores[vw];
		postingList.addPosting(imgId,1);
	}


	public PostingList scores (int vw) {
		return scores[vw];
	}


	public double df (int vw) {
		return scores[vw].getDf();
	}


	public int size() {
		return scores.length;
	}

	public void makeFinal(double nDocs) {
		for (PostingList postings: scores) {
			postings.makeFinal(nDocs); 
		}
	}
	
	public PostingList[] allScores () {
		return scores;
	}
	
	public List<List<Posting>> allScoresAsList () {
		List<List<Posting>> result = new ArrayList<List<Posting>>(scores.length);
		for (PostingList postings: scores) {
			result.add(new ArrayList<Posting>(postings.getPostings()));
		}
		return result;
	}


	//	public List<List<Integer>> getScores() {
		//		List<List<Integer>> scores = new ArrayList<List<Integer>>(tf.size());
	//		for (List<Posting> postingLists: tf) {
	//			for (Posting p: postingLists) {
	//				p.setScore()
	//			}
	//		}
	//	}




}