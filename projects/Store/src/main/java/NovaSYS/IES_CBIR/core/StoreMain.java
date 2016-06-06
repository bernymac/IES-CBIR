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


import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class StoreMain {
	
	private static String host = "52.4.21.171";
//	private static String host = "127.0.0.1";
	
	public static void main (String[] args) throws Exception{
		System.setProperty("java.rmi.server.hostname", host);
		System.getProperties().put("java.security.policy", "policy.all");
		if( System.getSecurityManager() == null ) 
			System.setSecurityManager( new RMISecurityManager()) ;
		try {
			LocateRegistry.createRegistry( 1099);
		} catch (RemoteException e) {
			//do nothing; already registered
		}
		try {
			StoreRemote store = new StoreBean();
			Naming.bind("StoreBean", store);
			System.out.println("Store Running...");

		} catch (AlreadyBoundException e) {
			System.out.println("ERROR: CloudCryptoSearch Server Already Running!");
			System.exit(0);
		}
	}
}
