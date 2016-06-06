/**
 *    Copyright 2015 João Rodrigues

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
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


public class OPE {

	private String key;
	private int pbits, cbits;


	private SecretKeySpec aesk;
	private Map<BigInteger, BigInteger> dgap_cache;

	public OPE(final String keyarg, int plainbits, int cipherbits) {
		this.key = keyarg;
		this.cbits = cipherbits;
		this.pbits = plainbits;
		this.aesk = aeskey(key);
		this.dgap_cache = new HashMap<BigInteger, BigInteger>();
	}

	private static SecretKeySpec aeskey(final String key) {
		return new SecretKeySpec(new String(Arrays.copyOf(key.toCharArray(), 16)).getBytes(), "AES");
	}
	
	private BigInteger domain_gap(final BigInteger ndomain, final BigInteger nrange, final BigInteger rgap, Random prng) {
		//System.out.println("DOMAIN GAP: " + ndomain + " " + nrange + " " + rgap + " random");
	    return HGDist.HGD(rgap, ndomain, nrange.subtract(ndomain), prng);
	}

	private OPEDomainRange lazy_sample(final BigInteger d_lo, final BigInteger d_hi, final BigInteger r_lo, final BigInteger r_hi, BigInteger ptext, boolean encrypt, Random prng) {
	    BigInteger ndomain = d_hi.subtract(d_lo).add(BigInteger.ONE);
	    BigInteger nrange  = r_hi.subtract(r_lo).add(BigInteger.ONE);
	    
	    assert nrange.compareTo(ndomain) >= 0;

	    if (ndomain.equals(BigInteger.ONE))
	        return new OPEDomainRange(d_lo, r_lo, r_hi);

	    /*
	     * Deterministically reset the PRNG counter, regardless of
	     * whether we had to use it for HGD or not in previous round.
	     */
	    long v = MAC(	d_lo + "/" +
	                    d_hi + "/" +
	                    r_lo + "/" +
	                    r_hi, key);
	    
	    prng.setSeed(v);

	    BigInteger rgap = nrange.divide(BigInteger.valueOf(2));
	    BigInteger dgap;

	    BigInteger ci = dgap_cache.get(r_lo.add(rgap));
	    if (ci == null) {
	        dgap = domain_gap(ndomain, nrange, rgap, prng);
	        dgap_cache.put(r_lo.add(rgap), dgap);
	    } else {
	        dgap = ci;
	    }

	    //go_low(d_lo.add(dgap), r_lo.add(rgap))
	    if (ptext.compareTo(encrypt ? d_lo.add(dgap) : r_lo.add(rgap)) < 0)
	        return lazy_sample(d_lo, d_lo.add(dgap).subtract(BigInteger.ONE), r_lo, r_lo.add(rgap).subtract(BigInteger.ONE), ptext, encrypt, prng);
	    else
	        return lazy_sample(d_lo.add(dgap), d_hi, r_lo.add(rgap), r_hi, ptext, encrypt, prng);
	}
	
	private static String MASTERKEY = "MASTER";
	
    private static long MAC(String materials, String key) {
        SecretKeySpec keySpec = new SecretKeySpec(MASTERKEY.getBytes(), "HmacSHA1");
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(keySpec);
            mac.update(materials.getBytes());
            mac.update(key.getBytes());
            new SecureRandom(mac.doFinal()).nextLong();
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return 0L;
    }
    
    public OPEDomainRange search(BigInteger ptext, boolean encrypt) {
        
        Random rnd = new SecureRandom(this.aesk.getEncoded());

        return lazy_sample(	BigInteger.ZERO, BigInteger.ONE.shiftLeft(pbits),
        					BigInteger.ZERO, BigInteger.ONE.shiftLeft(cbits),
        					ptext, encrypt, rnd);
    }

    public BigInteger encrypt(final BigInteger ptext) {
    	OPEDomainRange dr = search(ptext, true);

        byte v[] = SHA256(ptext.toByteArray());
        v = Arrays.copyOf(v, 16);

        //blockrng<AES> aesrand(aesk);
        //aesrand.set_ctr(v);
        
        Random rnd = new SecureRandom(v);

        BigInteger nrange = dr.getR_hi().subtract(dr.getR_lo()).add(BigInteger.ONE);
        return dr.getR_lo().add(new BigInteger(nrange.bitCount(), rnd).mod(nrange));
    }

    public BigInteger decrypt(final BigInteger ctext) {
    	OPEDomainRange dr = search(ctext, false);
        return dr.getD();
    }
    
    public static byte[] SHA256(byte[] input) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        } 
        return md.digest(input);
    }

}
