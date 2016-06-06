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

package NovaSYS.IES_CBIR.data.BOVW;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PostingList implements Serializable {

	private static final long serialVersionUID = -2674224848248079640L;

	private Map<Integer,Posting> postings;
	
	
	public PostingList() {
		postings = new HashMap<Integer,Posting>();
	}

	public Collection<Posting> getPostings() {
		return postings.values();
	}

	public void setPostings(Map<Integer,Posting> postings) {
		this.postings = postings;
	}
	
	public void removePosting(int docId) {
		this.postings.remove(docId);
	}
	
	public int getDf() {
		return postings.size();
	}
	
	public void addPosting(int imgId, int score) {
		Posting j = postings.get(imgId);
		if (j == null)
			postings.put(imgId,new Posting(imgId,score));
		else 
			j.addScore(score);
	}
	
	public void makeFinal(double nDocs) {
		if (getDf() == 0)
			return;
		double idf = Math.log10(nDocs / getDf());
		for (Posting p: postings.values())
			p.setScore(p.getScore()*idf);
	}

}
