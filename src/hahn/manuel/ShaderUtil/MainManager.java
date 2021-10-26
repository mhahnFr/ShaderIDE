package hahn.manuel.ShaderUtil;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.UIManager;

/**
 * Diese Klasse kümmert sich um sämtliche Verwaltungsaufgaben.
 * 
 * @author Manuel Hahn
 * @since 12.05.2017
 */
public class MainManager implements ActionListener {
	private boolean verbose;
	private ArrayList<MainWindow> windows;
	private FileManager manager;
	/**
	 * Der Indikator, um das Programm zu beenden.
	 */
	public static String EXIT = "exit";
	/**
	 * Der Indikator, um ein neues Fenster zu öffnen.
	 */
	public static String NEW_WINDOW = "newWindow";
	/**
	 * Der Indikator, dass eine neue Datei erzeugt werden soll.
	 */
	public static String NEW_FILE = "newFile";
	
	/**
	 * Ermittelt aus den übergebenen Argumenten, was zu tun ist.
	 * 
	 * @param args die Argumente
	 */
	public MainManager(String[] args) {
		manager = new FileManager();
		windows = new ArrayList<>();
		MainWindow w;
		for(int i = 0; i < args.length; i++) {
			String argument = args[i];
			switch(argument) {
			case "-o":
				w = new MainWindow(manager, this, verbose);
				w.openFile(args[++i]);
				windows.add(w);
				break;
			case "-v":
				verbose = true;
				manager.setVerbosity(verbose);
				break;
			case "-k":
				convertFile(args[++i]);
				System.exit(0);
				break;
			case "-kn":
				i++;
				convertFile(args[i], args[++i]);
				System.exit(0);
				break;
			case "-h":
			default:
				printHelp();
				System.exit(0);
				break;
			}
		}
		if(windows.isEmpty()) {
			w = new MainWindow(manager, this, verbose);
			windows.add(w);
		} else if(verbose) {
			for(MainWindow mw : windows) {
				mw.setVerbosity(verbose);
			}
		}
	}
	
	/**
	 * Öffnet ein weiteres Fenster.
	 */
	private void openNewWindow() {
		MainWindow w = new MainWindow(manager, this, verbose);
		windows.add(w);
	}
	
	/**
	 * Gibt zurück, ob der Verbosemodus aktiviert ist oder nicht.
	 * 
	 * @return ob der Verbose-Modus aktiviert ist oder nicht
	 */
	public boolean getVerbosity() {
		return verbose;
	}
	
	/**
	 * Beendet das Programm. Fragt, ob ungesicherte Änderungen gespeichert werden sollen.
	 */
	private void exit() {
		if(verbose) {
			System.out.println("Programm wird beendet...");
		}
		for(MainWindow w : windows) {
			if(w != null) {
				if(!w.disposing()) {
					if(verbose) {
						System.out.println("Beenden abgebrochen.");
					}
					return;
				}
			}
		}
		System.exit(0);
	}
	
	/**
	 * Haupteingangspunkt für die Shader-IDE. Setzt das Look and Feel auf das vom Betriebssystem.
	 * 
	 * @param args ein Array mit Argumenten
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		new MainManager(args);
	}
	
	/**
	 * Konvertiert die angegebene Datei in das Dateiformat dieser IDE. Die Datei wird im angegebenen Ordner mit dem 
	 * evtl. angegebenen Namen gespeichert.
	 * 
	 * @param fileName die zu konvertirende Datei
	 * @param convertedFileName der Pfad, in den die konvertierte Datei gespeichert werden soll
	 */
	private void convertFile(String fileName, String convertedFileName) {
		manager.convertTextFile(new File(fileName), new File(convertedFileName));
	}
	
	/**
	 * Konvertiert eine Textdatei in eine *.shad-Datei. Die neue Datei wird 
	 * im selben Ordner liegen und genauso wie die alte heißen.
	 * 
	 * @param fileName der Pfad zu der zu konvertierenden Datei
	 */
	private void convertFile(String fileName) {
		manager.convertTextFile(new File(fileName), null);
	}
	
	/**
	 * Gibt den Hilfetext für die Benutzung per Kommandozeile aus.
	 */
	private void printHelp() {
		System.out.printf("Hinweise zur Benutzung des ShaderUtils:\n"
						+ "java -jar ShaderUtil [OPTIONS]\n\n"
						+ "-k path/to/file Konvertiert die angegebene Datei\n"
						+ "-kn path/to/file path/to/new/directory Konvertiert die angegebene Datei und speichert sie an dem angegebenen Ort\n"
						+ "-o path/to/file Öffnet die angegebene Datei\n"
						+ "-v Aktiviert den Verbose-Modus\n"
						+ "-h zeigt diese Hilfe an\n");
	}
	
	public void actionPerformed(ActionEvent e) {
		switch(e.getActionCommand()) {
		case "exit":
			exit();
			break;
		case "newWindow":
			//openNewWindow();
			//break;
		case "newFile":
			openNewWindow();
			break;
		}
	}
}