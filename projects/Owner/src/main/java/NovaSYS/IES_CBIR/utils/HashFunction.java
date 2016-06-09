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

import java.util.Random;

public class HashFunction {

	private int a;
	private int b;
	private int c;

	public HashFunction(int universeSize) {
		Random r = new Random();
		a = r.nextInt(universeSize);
		b = r.nextInt(universeSize);
		c = r.nextInt(universeSize);
	}

	public int hash(int x) {
		int hashValue = (int)((a * (x >> 4) + b * x + c) & 131071);
		return Math.abs(hashValue);
	}
	
}

