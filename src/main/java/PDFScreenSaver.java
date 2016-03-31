import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

import tv.frackie.pdfscreensaver.PDFScreenSaverSettings;
import tv.frackie.pdfscreensaver.ScreenSaver;

public class PDFScreenSaver {

	public static void main(String[] args) {

		Preferences prefs = Preferences.userNodeForPackage(ScreenSaver.class);
		List<String> filenames = new ArrayList<>(Arrays.asList((prefs.get("pdfFiles", "").split("\\|"))));
		List<File> files = new ArrayList<>();
		for (String filename : filenames) {
			files.add(new File(filename));
		}
		if (filenames.get(0).isEmpty()) {
			filenames.clear();
		}
		int documentIndex = prefs.getInt("currentFile", 0);
		int pageIndex = prefs.getInt("currentPage", 0);
		long delay = prefs.getLong("delay", 2500);

		if (0 == args.length) {
			new PDFScreenSaverSettings().show();
		} else if (1 == args.length) {
			if ("/c".equals(args[0].toLowerCase())) {
				new PDFScreenSaverSettings().show();
			} else if ("/s".equals(args[0].toLowerCase())) {
				new ScreenSaver(files, documentIndex, pageIndex, delay).show(0);
			} else if ("/h".equals(args[0].toLowerCase())) {
				usage("");
			} else if ("/?".equals(args[0].toLowerCase())) {
				usage("");
			} else {
				usage("Invalid option \"" + args[0] + "\"!");
			}
		} else if (2 == args.length) {
			if ("/p".equals(args[0].toLowerCase())) {
				try {
					long hwnd = Long.parseLong(args[1]);
					new ScreenSaver(files, documentIndex, pageIndex, delay).show(hwnd);
				} catch (NumberFormatException nfe) {
					usage("Argument \"" + args[1] + "\" is not a valid HWND!");
				}
			} else {
				usage("Invalid option \"" + args[0] + "\"!");
			}
		} else {
			usage("Invalid syntax!");
		}
	}

	public static void usage(String message) {
		if (!message.isEmpty()) {
			System.err.println("Error: " + message);
		}
		System.err.println("Syntax:");
		System.err.println("PDFScreenSaver           - Show the settings dialog box.");
		System.err.println("PDFScreenSaver /c        - Show the settings dialog box, modal to");
		System.err.println("                           the foreground window.");
		System.err.println("PDFScreenSaver /p <HWND> - Preview the screen saver as child of window HWND.");
		System.err.println("PDFScreenSaver /s        - Run the screen saver.");
		System.err.println("");
		System.exit(message.isEmpty() ? 0 : 1);
	}
}
