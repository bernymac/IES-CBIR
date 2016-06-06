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

package NovaSYS.IES_CBIR.core;

import java.math.BigInteger;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

import NovaSYS.IES_CBIR.data.QueryResult;
import NovaSYS.IES_CBIR.data.BOVW.BigIntegerIndex;
import NovaSYS.IES_CBIR.data.BOVW.BigIntegerPostingList;

public interface StoreRemote extends Remote {

//	public void storeNewImage(ImgCiphertext img) throws RemoteException;
	
//	public void storeSEMinHash(List<byte[]> encImgs, int[][] encIndex) throws RemoteException;
//
//	public void storeSE(List<byte[]> encImgs, byte[] encHists, int[][] encIndex) throws RemoteException;
//
//	public byte[] getFeatures() throws RemoteException;
//
//	void storeIES_CBIR(List<int[][]> imgs) throws RemoteException;
	
	
	
	void storeIES_CBIR(int id, int[][] img) throws RemoteException;
	
	void storeIES_CBIR(int id, byte[] img) throws RemoteException;
	
//	void storeAllIES_CBIR(Map<Integer, byte[]> imgs) throws RemoteException;
	void storeAllIES_CBIR(byte[][] imgs) throws RemoteException;

	public void storeSE(Map<Integer, byte[]> documents, byte[] encIndex,	BigIntegerIndex imgIndex) throws RemoteException;
	
	public byte[] getSEIndex() throws RemoteException;

	void storePaillier(int id, BigInteger[][] img) throws RemoteException;

	QueryResult[] searchIES_CBIR (int[][] img, int resultSize) throws RemoteException;
	
	QueryResult[] searchIES_CBIR (byte[] compressedImg, int resultSize) throws RemoteException;
	
	BigIntegerPostingList[] searchSE (int[] vws) throws RemoteException;
}
