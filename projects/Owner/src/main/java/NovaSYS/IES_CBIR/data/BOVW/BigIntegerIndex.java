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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class BigIntegerIndex implements Serializable {

	private static final long serialVersionUID = -5053380674002721281L;

	private BigIntegerPostingList[] scores;


	public BigIntegerIndex(int sizeCodebook) {
		scores = new BigIntegerPostingList[sizeCodebook];
		for (int i = 0; i < sizeCodebook; i++)
			scores[i] = new BigIntegerPostingList();
	}

	public void addTermFreq(int vw, int imgId) {
		BigIntegerPostingList postingList = scores[vw];
		postingList.addPosting(imgId,BigInteger.valueOf(1));
	}


	public BigIntegerPostingList scores (int vw) {
		return scores[vw];
	}
	
	public void setScores (int vw, BigIntegerPostingList scores) {
		this.scores[vw] = scores;
	}


	public double df (int vw) {
		return scores[vw].getDf();
	}


	public int size() {
		return scores.length;
	}

//	public void makeFinal(double nDocs) {
//		for (BigIntegerPostingList postings: scores) {
//			postings.makeFinal(nDocs); 
//		}
//	}
	
	public BigIntegerPostingList[] allScores () {
		return scores;
	}
	
	public List<List<BigIntegerPosting>> allScoresAsList () {
		List<List<BigIntegerPosting>> result = new ArrayList<List<BigIntegerPosting>>(scores.length);
		for (BigIntegerPostingList postings: scores) {
			result.add(new ArrayList<BigIntegerPosting>(postings.getPostings()));
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