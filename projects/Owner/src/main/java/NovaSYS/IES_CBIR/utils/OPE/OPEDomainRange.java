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


public class OPEDomainRange {

	private BigInteger d, r_lo, r_hi;

	public OPEDomainRange(final BigInteger d_arg,
			final BigInteger r_lo_arg,
			final BigInteger r_hi_arg) {
		this.d = d_arg;
		this.r_lo = r_lo_arg;
		this.r_hi = r_hi_arg;
	}

	public BigInteger getD() {
		return d;
	}

	public BigInteger getR_lo() {
		return r_lo;
	}

	public BigInteger getR_hi() {
		return r_hi;
	}
	
}
