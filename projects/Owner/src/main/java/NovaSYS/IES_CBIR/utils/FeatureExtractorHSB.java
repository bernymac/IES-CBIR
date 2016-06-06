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

package NovaSYS.IES_CBIR.utils;
//
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.io.IOException;
//
//import javax.imageio.ImageIO;
//
//import org.grire.GeneralUtilities.Interfaces.FeatureExtractor;
//import org.grire.Helpers.Listeners.Listened;
//
//import NovaSYS.IES_CBIR.data.ImgHistograms;
//
///**
// * Created by Joao on 3/31/14.
// */
public class FeatureExtractorHSB {//extends FeatureExtractor {
//
//    private float[][] positions;
//
//    @Override
//    public float[][] extract(File image) {
//        try { return extract(ImageIO.read(image)); }
//        catch (IOException ex) {  return null; }
//    }
//
//    @Override
//    public float[][] extract(BufferedImage image) {
//
//        ImgHistograms histograms = new ImgHistograms(0);
//        for (int i = 0; i < image.getWidth(); i++) {
//            for (int j = 0; j < image.getHeight(); j++) {
//                int pixel = ImgUtils.RGBtoHSB(image.getRGB(i, j));
//
//                histograms.addHueValue(ImgUtils.redOrHueValueFromPixel(pixel));
//                histograms.addSaturationValue(ImgUtils.greenOrSaturationValueFromPixel(pixel));
//                histograms.addBrightnessValue(ImgUtils.blueOrBrightnessValueFromPixel(pixel));
//            }
//        }
//
//        int B[] = histograms.getBrightness();
//        int H[] = histograms.getHue();
//        int S[] = histograms.getSaturation();
//
//        float[][] ret = new float[3][B.length];
//        for(int i = 0; i < B.length; i++){
//            ret[0][i] = H[i];
//            ret[1][i] = S[i];
//            ret[2][i] = B[i];
//        }
//
//        return ret;
//    }
//
//    @Override
//    public float[][] getPositions() {
//        return this.positions;
//    }
//
//    @Override
//    public Listened setUp(Object... objects) throws Exception {
//        return null;
//    }
//
//    @Override
//    public Class[] getParameterTypes() {
//        return new Class[0];
//    }
//
//    @Override
//    public Class[] getSetUpParameterTypes() {
//        return new Class[0];
//    }
//
//    @Override
//    public String[] getParameterNames() {
//        return new String[0];
//    }
//
//    @Override
//    public String[] getSetUpParameterNames() {
//        return new String[0];
//    }
//
//    @Override
//    public String[] getDefaultParameterValues() {
//        return new String[0];
//    }
//
//    @Override
//    public String[] getDefaultSetUpParameterValues() {
//        return new String[0];
//    }
//
//    @Override
//    public boolean requiresSetUp() {
//        return false;
//    }
}
