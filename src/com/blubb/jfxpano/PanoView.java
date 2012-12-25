package com.blubb.jfxpano;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class PanoView {
	int croppedLeft = 0;
	int croppedTop = 0;
	int croppedWidth = 0;
	int croppedHeight = 0;
	int fullHeight = 0;
	int fullWidth = 0;
	int width;
	int height;
	int distance;
	int heading = 0;
	double headingangle = 0;
	int tilt = 0;
	double tiltangle = 0;
	int pixels[];
	int origpixels[];

	WritableImage view;
	
	public PanoView(InputStream is, int width, int height) {
		this.width = width;
		this.height = height;
		ImageInputStream input = new MemoryCacheImageInputStream(
				is);
		ImageReader reader = ImageIO.getImageReaders(input).next();
		reader.setInput(input);
		DOMImplementationRegistry registry;
		try {
			registry = DOMImplementationRegistry
					.newInstance();
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | ClassCastException e) {
			e.printStackTrace();
			return;
		}
		DOMImplementationLS impl = (DOMImplementationLS) registry
				.getDOMImplementation("LS");
		LSSerializer lsSerializer = impl.createLSSerializer();
		Node n;
		try {
			n = reader.getImageMetadata(0).getAsTree(
					"javax_imageio_jpeg_image_1.0");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		String s = lsSerializer.writeToString(n);
		System.out.println(s);
		NodeList nl = ((Element) n).getElementsByTagName("unknown");
		for (int i = 0; i < nl.getLength(); i++) {
			IIOMetadataNode mn = (IIOMetadataNode) nl.item(i);
			byte data[] = (byte[]) mn.getUserObject();
			if (data.length > 30) {
				String header = new String(data, 0, 28);
				if (header.equals("http://ns.adobe.com/xap/1.0/")) {
					System.out.println("parsing xmp");
					String xmp = new String(data, 29, data.length - 29);
					System.out.println(xmp);
					croppedLeft = findAttribute(xmp,
							"CroppedAreaLeftPixels");
					croppedTop = findAttribute(xmp,
							"CroppedAreaTopPixels");
					croppedHeight = findAttribute(xmp,
							"CroppedAreaImageHeightPixels");
					croppedWidth = findAttribute(xmp,
							"CroppedAreaImageWidthPixels");
					fullHeight = findAttribute(xmp,
							"FullPanoHeightPixels");
					fullWidth = findAttribute(xmp,
							"FullPanoWidthPixels");
					System.out.println("Cropped Left " + croppedLeft);
				}
			}
		}

		view = new WritableImage(width, height);
		BufferedImage src;
		try {
			src = reader.read(0);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		pixels = new int[width*height];
		origpixels = new int[fullHeight*fullWidth];
		Arrays.fill(origpixels, 0xFF000000);
		src.getRGB(0, 0, croppedWidth, croppedHeight, origpixels, croppedLeft, fullWidth);
		setDistance((int)(width));
	}

	public Image getImage() {
		return view;
	}
	
	public void setDistance(int distance) {
		this.distance = distance;
		redraw();
	}
	
	private void redraw() {
		final int midx = width/2;
		final int midy = height/2;
		final double wpi = fullWidth/(2.0*Math.PI);
		final double hpi = fullHeight/(Math.PI);
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				// find the panorama pixel
				double origxa = Math.atan((double)(x-midx)/(double)distance);
				double origya = Math.atan((double)(y-midy)/(double)distance);
				double origx = wpi*origxa+heading;
				double origy = hpi*origya+tilt;
				if(origx >= fullWidth) origx-= fullWidth;
				if(origy >= fullHeight) origy-= fullHeight;
				if(origx < 0) origx = fullWidth+origx;
				if(origy < 0) origy = fullHeight+origy;
				try {
				pixels[width*y+x] = origpixels[(int)origy*fullWidth+(int)origx];
				} catch(Throwable t) {
					System.out.println(origx+" "+origy+" "+fullWidth+" "+fullHeight);
					System.exit(0);
				}
			}
		}
		PixelWriter pw = view.getPixelWriter();
		pw.setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
	}
	
	private static final int findAttribute(String xmp, String name) {
		int idx = xmp.indexOf(name + "=\"");
		if (idx < 0)
			return -1;
		idx += name.length() + 2;
		int endidx = xmp.indexOf('"', idx);
		return Integer.parseInt(xmp.substring(idx, endidx));
	}

	public void setHeading(double fh) {
		while(fh < 0) fh = Math.PI*2.0 + fh;
		while(fh >= Math.PI*2.0) fh -= Math.PI*2.0;
		headingangle = fh;
		System.out.println("heading "+headingangle);
		heading = (int)(fullWidth/(Math.PI*2)*fh);
		redraw();
	}
	
	public double getHeading() {
		return headingangle;
	}
	
	public void setTilt(double fh) {
		while(fh < 0) fh = Math.PI + fh;
		while(fh >= Math.PI) fh -= Math.PI;
		tiltangle = fh;
		System.out.println("tilt "+tiltangle);
		tilt = (int)(fullHeight/(Math.PI)*fh);
		redraw();
	}
	
	public double getTilt() {
		return tiltangle;
	}
}
