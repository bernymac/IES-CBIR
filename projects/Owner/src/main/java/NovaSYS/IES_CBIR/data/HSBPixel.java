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

package NovaSYS.IES_CBIR.data;

public class HSBPixel {

	private byte hue, saturation, brightness;

	public HSBPixel (float[] hsb) {
		this(hsb[0], hsb[1], hsb[2]);
	}
	
	public HSBPixel (float hue, float saturation, float brightness) {
		setHue((byte)(hue*100));
		setSaturation((byte)(saturation*100));
		setBrightness((byte)(brightness*100));
	}
	
	public byte getHue() {
		return hue;
	}
	
	public float getHueAsFloat() {
		return (float)hue/(float)100;
	}

	public void setHue(byte hue) {
		this.hue = hue;
	}

	public byte getSaturation() {
		return saturation;
	}
	
	public float getSaturationAsFloat() {
		return (float)saturation/(float)100;
	}

	public void setSaturation(byte saturation) {
		this.saturation = saturation;
	}

	public byte getBrightness() {
		return brightness;
	}
	
	public float getBrightnessAsFloat() {
		return (float)brightness/(float)100;
	}

	public void setBrightness(byte brightness) {
		this.brightness = brightness;
	}
	
}
