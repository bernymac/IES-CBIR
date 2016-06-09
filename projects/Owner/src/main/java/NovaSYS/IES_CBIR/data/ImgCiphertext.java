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

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import javax.imageio.ImageIO;

import NovaSYS.IES_CBIR.utils.ImgUtils;


public class ImgCiphertext implements Serializable{

	private static final long serialVersionUID = -3268981277230240746L;
	
	private int id;
	private int[][] img;
	private boolean isEncrypted;

	public ImgCiphertext(int id, int[][] encImg, boolean isEncrypted) {
		setId(id);
		setImg(encImg);
		setEncrypted(isEncrypted);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	
	public int[][] getImg() {
		return img;
	}

	public void setImg(int[][] img) {
		this.img = img;
	}

	public int getHeight() {
		return img[0].length;
	}

	public int getWidth() {
		return img.length;
	}
	
	public boolean isEncrypted() {
		return isEncrypted;
	}

	public void setEncrypted(boolean isEncrypted) {
		this.isEncrypted = isEncrypted;
	}
	
	public byte[] compress() {
		BufferedImage result = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		for (int i = 0; i <  getWidth(); i++) {
			for (int j = 0; j < getHeight(); j++) { 
				result.setRGB(i, j, ImgUtils.HSBtoRGB(img[i][j]));
			}
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		final File f = new File(path+getId()+".png");
		try {
			ImageIO.write(result, "jpg", new BufferedOutputStream(baos));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}

}