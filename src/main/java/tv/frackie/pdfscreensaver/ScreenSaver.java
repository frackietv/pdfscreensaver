package tv.frackie.pdfscreensaver;

import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

public class ScreenSaver {

	private List<File> pdfFiles;
	private int currentPdfFile;
	private int currentPage;
	private PDDocument currentDocument = null;
	private PDFRenderer currentRenderer = null;
	private long delay;
	private Color nextColor;
	private List<JFrame> frames;
	private Map<JFrame, JLabel> labels;

	private boolean closed = false;

	public ScreenSaver(List<File> pdfFiles, int currentPdfFile, int currentPage, long delay) {
		this.pdfFiles = new ArrayList<>(pdfFiles);
		this.currentPage = currentPage;
		this.currentPdfFile = currentPdfFile;
		this.delay = delay;
	}

	public ScreenSaver(List<File> pdfFiles, long delay) {
		this(pdfFiles, 0, 0, delay);
	}

	public ScreenSaver(List<File> pdfFiles) {
		this(pdfFiles, 3000);
	}

	public void addPdf(File pdfFile) {
		this.pdfFiles.add(pdfFile);
	}

	private void close(Window w) {
		if (!closed) {
			closed = true;
			w.setVisible(false);
			w.dispose();
			if (null != currentDocument) {
				try {
					currentDocument.close();
				} catch (IOException e) {
					// Ignore
				}
			}
			System.exit(1);
		}
	}

	public void show(long parentWindowId) {
		frames = new ArrayList<>();
		labels = new HashMap<>();

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		for (GraphicsDevice gd : ge.getScreenDevices()) {
			JFrame frame = new JFrame();
			frame.setUndecorated(true);
			gd.setFullScreenWindow(frame);
			frames.add(frame);

			if (0 != parentWindowId) {

				com.sun.jna.platform.win32.User32.INSTANCE.SetParent(
						new com.sun.jna.platform.win32.WinDef.HWND(com.sun.jna.Native.getWindowPointer(frame)),
						new com.sun.jna.platform.win32.WinDef.HWND(new com.sun.jna.Pointer(parentWindowId)));
			}

			frame.getContentPane().setBackground(Color.BLACK);
			frame.setLayout(new GridBagLayout());
			JLabel label = new JLabel();
			frame.add(label);
			labels.put(frame, label);
			frame.addWindowListener(new WindowListener() {

				@Override
				public void windowClosing(WindowEvent e) {
					close(e.getWindow());
				}

				@Override
				public void windowClosed(WindowEvent e) {
					close(e.getWindow());
				}

				@Override
				public void windowDeactivated(WindowEvent e) {
					close(e.getWindow());
				}

				@Override
				public void windowOpened(WindowEvent e) {
				}

				@Override
				public void windowIconified(WindowEvent e) {
				}

				@Override
				public void windowDeiconified(WindowEvent e) {
				}

				@Override
				public void windowActivated(WindowEvent e) {
				}

			});
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			frame.setVisible(true);
		}
		showSlideShow();
	}

	private void nextDocument() {

		if (null != currentDocument) {
			try {
				currentDocument.close();
			} catch (IOException e) {
				// Ignore
			}
			currentDocument = null;
		}
		currentRenderer = null;

		currentPage = 0;
		currentPdfFile++;

		int startingDocument = currentPdfFile;

		File currentFile = null;
		if (currentPdfFile < pdfFiles.size()) {
			currentFile = pdfFiles.get(currentPdfFile);
		}
		while (null == currentFile || !currentFile.canRead()) {
			currentPdfFile += 1;
			if (currentPdfFile >= pdfFiles.size()) {
				currentPdfFile = 0;
			}
			if (startingDocument == currentPdfFile) {
				return;
			}
			currentFile = pdfFiles.get(currentPdfFile);
		}

		try {
			currentDocument = PDDocument.load(currentFile, "");
		} catch (IOException e) {
			// Ignore
		}
		if (null != currentDocument) {
			currentRenderer = new PDFRenderer(currentDocument);
		}
	}

	private Color getAverageColor(int[]... pixels_array) {
		int count = 0;
		int r = 0, g = 0, b = 0;
		for (int[] pixels : pixels_array) {
			count += pixels.length;
			for (int i = 0; i < pixels.length; i += 4) {
				r += (255 * 255 + pixels[i + 0] * pixels[i + 3] - 255 * pixels[i + 3]) / 255;
				g += (255 * 255 + pixels[i + 1] * pixels[i + 3] - 255 * pixels[i + 3]) / 255;
				b += (255 * 255 + pixels[i + 2] * pixels[i + 3] - 255 * pixels[i + 3]) / 255;
			}
		}
		r /= count;
		g /= count;
		b /= count;
		return new Color(r, g, b);
	}

	private Image getScaledImage(JFrame frame, BufferedImage original) {
		int fw = frame.getWidth();
		int fh = frame.getHeight();

		int iw = original.getWidth();
		int ih = original.getHeight();

		if (ih * fw / iw > fh) {
			return original.getScaledInstance(-1, frame.getHeight(), Image.SCALE_SMOOTH);
		} else {
			return original.getScaledInstance(frame.getWidth(), -1, Image.SCALE_SMOOTH);
		}
	}

	private Map<JFrame, ImageIcon> getNextPage() {
		if (currentDocument == null && currentPdfFile < pdfFiles.size()) {
			try {
				currentDocument = PDDocument.load(pdfFiles.get(currentPdfFile));
				currentRenderer = new PDFRenderer(currentDocument);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			currentPage++;
		}
		if (currentDocument == null || currentRenderer == null || currentPage >= currentDocument.getNumberOfPages()) {
			nextDocument();
		}
		if (currentRenderer == null) {
			return null;
		}
		try {
			BufferedImage image = currentRenderer.renderImageWithDPI(currentPage, 96, ImageType.ARGB);
			Raster data = image.getData();
			nextColor = getAverageColor(data.getPixels(10, 0, 1, image.getHeight(), (int[]) null),
					data.getPixels(0, 0, image.getWidth(), 1, (int[]) null),
					data.getPixels(image.getWidth() - 1, 0, 1, image.getHeight(), (int[]) null),
					data.getPixels(0, image.getHeight() - 1, image.getWidth(), 1, (int[]) null));
			Map<JFrame, ImageIcon> ret = new HashMap<>();
			for (JFrame frame : frames) {
				ret.put(frame, new ImageIcon(getScaledImage(frame, image)));
			}
			Preferences prefs = Preferences.userNodeForPackage(ScreenSaver.class);
			prefs.putInt("currentFile", currentPdfFile);
			prefs.putInt("currentPage", currentPage);
			try {
				prefs.flush();
			} catch (BackingStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return ret;
		} catch (IOException e) {
			return null;
		}
	}

	private void showSlideShow() {
		boolean first = true;
		while (!closed) {
			Map<JFrame, ImageIcon> images = getNextPage();
			if (first) {
				first = false;
			} else {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					// Ignore
				}
			}
			for (JFrame frame : frames) {
				frame.getContentPane().setBackground(nextColor);
				labels.get(frame).setIcon(images.get(frame));
			}
		}
	}

}
