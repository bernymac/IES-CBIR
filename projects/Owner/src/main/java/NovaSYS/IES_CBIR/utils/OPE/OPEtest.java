/**
*    Copyright 2015 João Miguel Cardia Melro Rodrigues
 
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

package NovaSYS.IES_CBIR.utils.OPE;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import NovaSYS.IES_CBIR.utils.ImgUtils;


public class OPEtest {

	public static void main(String args[]) {
		long startTime = System.nanoTime();
		test1();
		long loadTime = System.nanoTime();
		System.out.println("LoadTime: "+(double)(loadTime-startTime)/ImgUtils.nanoSeconds);
	}
	
	public static void test1() {
		OPE o = new OPE("my key materials", 8, 60);

		BigInteger init = new BigInteger(16, new Random());
		BigInteger prev = null;
		for( int i = 0; i < 100; i++) {
			BigInteger enc = o.encrypt(init);
//			if(prev != null)
//				System.err.print(enc.compareTo(prev) > 0 ? "[OK] " : "[FAIL] ");
//			else
//				System.err.print("[NN] ");
//			System.err.println(init + " -ENC> " + enc + " -DEC> " + o.decrypt(enc));
			init = init.add(BigInteger.ONE);
			prev = enc;
		}
	}
	
	
	public static void test2() {
		OPE o = new OPE("my key materials", 64, 128);

		BigInteger init = new BigInteger(16, new Random());
		List<BigInteger> plainValues = new LinkedList<BigInteger>();
		for( int i = 0; i < 10000; i++) {
			plainValues.add(init);
			init = init.add(BigInteger.ONE);
		}
		
		Collections.shuffle(plainValues);
		
		List<BigInteger> encryptedValues = new LinkedList<BigInteger>();
		
		for( BigInteger pValue: plainValues ) {
			BigInteger enc = o.encrypt(pValue);
			BigInteger dec = o.decrypt(enc);
			if( !dec.equals(pValue) )
				System.err.print("Ooops " + dec + " expected " + pValue);
			encryptedValues.add(enc);
		}
		
		Collections.sort(plainValues);
		Collections.sort(encryptedValues);
		
		if(plainValues.size() != encryptedValues.size())
			System.err.print("Ooops: plain and encrypted values size do not match! :(");
	

		Iterator<BigInteger> pV = plainValues.iterator();
		Iterator<BigInteger> eV = encryptedValues.iterator();
		
		while(pV.hasNext() && eV.hasNext()) {
			BigInteger pValue = pV.next();
			BigInteger dec = o.decrypt(eV.next());
			if( !pValue.equals(dec) )
				System.err.print("Ooops " + dec + " expected " + pValue + " WRONG ORDER?");
		}
		
		System.err.print("FINISH :)");
		
	}



}
