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

package NovaSYS.IES_CBIR.data;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ImgHistograms implements Serializable {

	private static final long serialVersionUID = -2875596858975211578L;

	public static final int bins = 101;
	
//	private static final int maxBins = 101;
//	private static final int binSize = maxBins/bins;
	
	private int id;
	private int[] hue, saturation, brightness;

	public ImgHistograms(int id) {
		setId(id);
		hue = new int[bins];
		saturation = new int[bins];
		brightness = new int[bins];
	}

	public int[] getHue() {
		return hue;
	}

	public void setHue(int[] hue) {
		this.hue = hue;
	}
	
	public int[] getSaturation() {
		return saturation;
	}
	
	public void setSaturation(int[] saturation) {
		this.saturation = saturation;
	}

	public int[] getBrightness() {
		return brightness;
	}
	
	public void setBrightness(int[] brightness) {
		this.brightness = brightness;
	}
	
	public byte[] getHueBytes() {
		ByteBuffer buffer = ByteBuffer.allocate(4*bins);
		for (int i: hue)
			buffer.putInt(i);
		return buffer.array();
	}
 
	public byte[] getSaturationBytes() {
		ByteBuffer buffer = ByteBuffer.allocate(4*bins);
		for (int i: saturation)
			buffer.putInt(i);
		return buffer.array();
	}
	
	public byte[] getBrightnessBytes() {
		ByteBuffer buffer = ByteBuffer.allocate(4*bins);
		for (int i: brightness)
			buffer.putInt(i);
		return buffer.array();
	}

	public void addHueValue (int value) {
		hue[value]++;
//		for (int i = binSize; i <= maxBins; i+=binSize) {
//			if (value < i) {
//				hue[(i/binSize)-1]++;
//				return;
//			}
//		}
	}
	
	public void addSaturationValue (int value) {
		saturation[value]++;
//		for (int i = binSize; i <= maxBins; i+=binSize) {
//			if (value < i) {
//				saturation[(i/binSize)-1]++;
//				return;
//			}
//		}
	}
	
	public void addBrightnessValue (int value) {
		brightness[value]++;
//		for (int i = binSize; i <= maxBins; i+=binSize) {
//			if (value < i) {
//				brightness[(i/binSize)-1]++;
//				return;
//			}
//		}
	}
	
	public int getHueValue(int index) {
		return hue[index];
//		for (int i = binSize; i <= maxBins; i+=binSize)
//			if (index < i) 
//				return hue[(i/binSize)-1];
//		return -1;
	}
	
	public int getSaturationValue(int index) {
		return saturation[index];
//		for (int i = binSize; i <= maxBins; i+=binSize)
//			if (index < i) 
//				return saturation[(i/binSize)-1];
//		return -1;
	}
	
	public int getBrightnessValue(int index) {
		return brightness[index];
//		for (int i = binSize; i <= maxBins; i+=binSize)
//			if (index < i) 
//				return brightness[(i/binSize)-1];
//		return -1;
	}
	
	public void normalize(double factor) {
		for (int i = 0; i < bins; i++) {
			hue[i] /= factor;
			saturation[i] /= factor;
			brightness[i] /= factor;
		}
	}

	public void acumulate() {
		for (int i = 1; i < bins; i++) {
			hue[i] += hue[i-1];
			saturation[i] += saturation[i-1];
			brightness[i] += brightness[i-1];
		}
	}

	public void print() {
		System.out.println("Histogram:");
		for (int i = 1; i < bins; i++)
			System.out.println(hue[i]+" "+saturation[i]+" "+brightness[i]);
		System.out.println();
	}
	
	public static final int numberOfBins() {
		return bins;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public int[] getCondensed() {
		int[] condensed = new int[3*bins];
		for (int i = 0; i < bins; i++) {
			condensed[i] = hue[i];
			condensed[bins+i] = saturation[i];
			condensed[2*bins+i] = brightness[i];
		}
		return condensed;
	}
	
	public List<Integer> getCondensedAsList() {
		List<Integer> condensed = new ArrayList<Integer>(3*bins);
		for (int i = 0; i < bins; i++) {
			condensed.add(hue[i]);
		}
		for (int i = 0; i < bins; i++) {
			condensed.add(saturation[i]);
		}
		for (int i = 0; i < bins; i++) {
			condensed.add(brightness[i]);
		}
		return condensed;
	}
	
	public void setCondensed(int[] condensed) {
		for (int i = 0; i < bins; i++) {
			hue[i] = condensed[i];
			saturation[i] = condensed[bins+i];
			brightness[i] = condensed[2*bins+i];
		}
	}
	
}
