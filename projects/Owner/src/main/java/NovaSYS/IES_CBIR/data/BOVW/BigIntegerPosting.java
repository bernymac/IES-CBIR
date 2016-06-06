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
import java.math.BigInteger;


public class BigIntegerPosting implements Cloneable, Serializable, Comparable<BigIntegerPosting>{

	private static final long serialVersionUID = 8880242050287844431L;

	private int docId;
	
	private BigInteger score;
	
	public BigIntegerPosting (int docId, BigInteger score) {
		this.docId = docId;
		this.score = score;
	}

	public int getDocId() {
		return docId;
	}

	public void setDocId(int docId) {
		this.docId = docId;
	}

	public BigInteger getScore() {
		return score;
	}

	public void setScore(BigInteger score) {
		this.score = score;
	}
	
	public void addScore(BigInteger score) {
		this.score.add(score);
	}

	@Override
	public int compareTo(BigIntegerPosting o) {
		return score.compareTo(o.score);
	}
	
	@Override
	public boolean equals(Object o) {
		return docId == ((BigIntegerPosting)o).docId && score == ((BigIntegerPosting)o).score;
	}
	
	@Override
	public BigIntegerPosting clone() {
		return new BigIntegerPosting(docId, score);
	}
}
