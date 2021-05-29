package BatteShip;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class Ship {
	public Image ship; // ảnh tàu nằm ngang

	public Ship(int length, int w, int h) {

		if (length == 3) {
			ship = loadImage("/img/3.png", w, h);
		}
		if (length == 2) {
			ship = loadImage("/img/2.png", w, h);
		}
		if (length == 1) {
			ship = loadImage("/img/1.png", w, h);
		}
	}

	public Image getShip() {
		return this.ship;
	}

	private Image loadImage(String s, int w, int h) {
		BufferedImage i = null; // doc anh duoi dang Buffered Image
		try {
			i = ImageIO.read(MainMenu.class.getResource(s));
		} catch (Exception e) {
			System.out.println("Duong dan anh k hop le!");
		}

		Image dimg = i.getScaledInstance(w, h, Image.SCALE_SMOOTH); // thay doi kich thuoc anh
		return dimg;

	}
}
