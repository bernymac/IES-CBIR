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

import NovaSYS.IES_CBIR.utils.ImgUtils;

public class FloatClusterPoint{
//    public float cluster;
//    HashSet<Integer> members = new HashSet<Integer>();
    public float[] nextLocation,Location;
    public float membersNumber;

    public FloatClusterPoint(float[] loc) {
        this.nextLocation=new float[loc.length];
        Location=loc;
        membersNumber=0;
    }
    
    public float GetMovement() {
        float sum=0;
        for (int i=0;i<nextLocation.length;i++) {
            sum += Math.abs(this.Location[i]-nextLocation[i]);//*(this.Location[i]-nextLocation[i]);
        }
        return sum;
    }
    
    @Override
    public String toString(){
        float ret=0;
        for (float d:this.Location) ret+=d;
        return String.valueOf(ret);
    }

    public float getL1Distance(float[] p) {
    		float sum=0;
         for (int i=0;i<p.length;i++) {
             sum+=Math.abs((p[i]-Location[i]));
         }
         return sum;
    }
    
    public float getNormHamDist(float[] p) {
    		return ImgUtils.normalizedHammingDistance(this.Location, p);
}
    
    public float getEuclidianDistance(float[] p) {
        return (float) Math.sqrt(getSquaredEuclidianDistance(p));
    }
    
    public float getSquaredEuclidianDistance(float[] p) {
        float sum=0;
        for (int i=0;i<p.length;i++) {
            sum+=(p[i]-Location[i])*(p[i]-Location[i]);
        }
        return sum;
    }
    
    public void swapLocations() {
        Location=nextLocation;
    }
}
