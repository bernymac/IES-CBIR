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

import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;


public final class RC4 {

//	private byte[] key; 
//	private int[] sbox;
//	private static final int KEY_MIN_LENGTH = 5;
	private static final int SBOX_LENGTH = 256;


//	public RC4(byte[] key) {
//		this.key = key;
//	}
	
//	public void setKey(byte[] key) throws InvalidKeyException {
//		if (!(key.length >= KEY_MIN_LENGTH && key.length < SBOX_LENGTH)) {
//			throw new InvalidKeyException("Key length has to be between "
//					+ KEY_MIN_LENGTH + " and " + (SBOX_LENGTH - 1));
//		}
//		this.key = key;
//	}

	
	private int[] initSBox(byte[] key) {
		int[] sbox = new int[SBOX_LENGTH];
		int j = 0;

		for (int i = 0; i < SBOX_LENGTH; i++) {
			sbox[i] = i;
		}

		for (int i = 0; i < SBOX_LENGTH; i++) {
			j = (j + sbox[i] + (key[i % key.length] & 0xff)) % SBOX_LENGTH;
			swap(i, j, sbox);
		}
		return sbox;
	}


	public byte[] encrypt(final byte[] key, final byte[] msg) {
		int[] sbox = initSBox(key);
		byte[] code = new byte[msg.length];
		int i = 0;
		int j = 0;
		for (int n = 0; n < msg.length; n++) {
			i = (i + 1) % SBOX_LENGTH;
			j = (j + sbox[i]) % SBOX_LENGTH;
			swap(i, j, sbox);
			int rand = sbox[(sbox[i] + sbox[j]) % SBOX_LENGTH];
			code[n] =  (byte) (rand ^  msg[n]);
		}
		return code;
	}
	
	public int[] generateRandomsVector(final byte[] key, final int size) {
		int[] sbox = initSBox(key);
		int[] result = new int[size];
		int i = 0;
		int j = 0;
		for (int n = 0; n < size; n++) {
			byte[] rndBytes = new byte[4];
			for (int k = 0; k < 4; k++) { 	//get 4 bytes = 1 integer
				i = (i + 1) % SBOX_LENGTH;
				j = (j + sbox[i]) % SBOX_LENGTH;
				swap(i, j, sbox);
				rndBytes[k] = (byte) sbox[(sbox[i] + sbox[j]) % SBOX_LENGTH];
			}
			result[n] = ImgUtils.byteArrayToInt(rndBytes);
		}
		return result;
	}
	
	public int[] generateUniqueRandomsVector(final byte[] key, final int size) {
		int[] sbox = initSBox(key);
		int[] result = new int[size];
		List<Integer> sack = new LinkedList<Integer>();
		for (int n = 0; n < size; n++)
			sack.add(n);
		int i = 0;
		int j = 0;
		for (int n = 0; n < size; n++) {
			byte[] rndBytes = new byte[4];
			for (int k = 0; k < 4; k++) { 	//get 4 bytes = 1 integer
				i = (i + 1) % SBOX_LENGTH;
				j = (j + sbox[i]) % SBOX_LENGTH;
				swap(i, j, sbox);
				rndBytes[k] = (byte) sbox[(sbox[i] + sbox[j]) % SBOX_LENGTH];
			}
			int rnd = Math.abs(ImgUtils.byteArrayToInt(rndBytes))%sack.size();
			result[n] = sack.remove(rnd);
		}
		return result;
	}
	
	private void swap(int i, int j, int[] sbox) {
		int temp = sbox[i];
		sbox[i] = sbox[j];
		sbox[j] = temp;
	}

	
	public byte[] decrypt(final byte[] key, final byte[] msg) {
		return encrypt(key, msg);
	}
	
	
	public class InvalidKeyException extends Exception {

		private static final long serialVersionUID = 1L;

		public InvalidKeyException(String message) {
			super(message);
		}
	}
	
	
	public int[] secureRandom(final byte[] key, final int size) {
		SecureRandom prng = new SecureRandom(key);
		List<Integer> sack = new LinkedList<Integer>();
		int[] result = new int[size];
		for (int n = 0; n < size; n++)
			sack.add(n);
		for (int n = 0; n < size; n++) {
			int rnd = prng.nextInt(sack.size());
			result[n] = sack.remove(rnd);
		}
		return result;
	}
	
	public static void main (String[] args) throws Exception{
		SecureRandom prng = new SecureRandom();
		System.out.println(prng.getProvider().getInfo());
//		KeyGenerator kgen = KeyGenerator.getInstance("RC4");
//		kgen.init(128);
//		RC4 rc4 = new RC4(kgen.generateKey().getEncoded());
//		int[] index = new int[100];
//		for (int i = 0; i < 100; i++)
//			index[i] = i;
//		ByteBuffer byteBuffer = ByteBuffer.allocate(index.length * 4);
//		byteBuffer.asIntBuffer().put(index);
//		
//		byte[] ciphertext = rc4.encrypt(byteBuffer.array());
//		ByteBuffer buffer = ByteBuffer.allocate(ciphertext.length);
//		buffer.put(ciphertext);
//		buffer.flip();
//		IntBuffer encryptedValues = buffer.asIntBuffer();
//		while (encryptedValues.hasRemaining())
//			System.out.println((encryptedValues.get()&0xff)%100);
//		System.out.println();
//		byte[] plaintext = rc4.decrypt(ciphertext);
//		ByteBuffer buffer2 = ByteBuffer.allocate(plaintext.length);
//		buffer2.put(plaintext);
//		buffer2.flip();
//		IntBuffer plaintextValues = buffer2.asIntBuffer();
//		while (plaintextValues.hasRemaining())
//			System.out.println((plaintextValues.get()));
//			byte[] result = rc4.encrypt("hello there".getBytes());
//			System.out.println("encrypted string:\n" + new String(result));
//			System.out.println("decrypted string:\n"
//					+ new String(rc4.decrypt(result)));
	}
}
