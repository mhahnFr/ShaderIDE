package hahn.manuel.ShaderUtil;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Diese Klasse repräsentiert ein Fenster, in dem ein Shader geschreiben und bearbeitet werden kann.
 * 
 * @author Manuel Hahn
 * @since 12.05.2017
 */
public class MainWindow extends JFrame implements ActionListener {
	private static final long serialVersionUID = 561319106666932430L;
	private JTextArea source;
	private FileManager manager;
	private volatile boolean edited;
	private volatile boolean ignoreKey;
	private boolean verbose;
	private Shader opened;
	private SwingWorker<byte[], Void> convertFile = new SwingWorker<byte[], Void>() {
		protected byte[] doInBackground() {
			if(verbose) {
				System.out.println("Thread started...");
				System.out.println("Daten konvertieren...");
			}
			return opened.getRawFileData();
		}
		
		protected void done() {
			if(verbose) {
				System.out.println("Daten konvertiert!");
				System.out.println("Thread finished!");
			}
		}
	};
	
	/**
	 * Erzeugt ein Fenster, in welchem Shader programmiert werden können.
	 * 
	 * @param manager ein Dateimanager, mit welchem Dateien gelesen und geschrieben werden können
	 * @param al ein ActionListener, um Beenden und Neues Fenster zu behandeln
	 * @param verbose ob das Fenster genau beschreiben soll, was es tut
	 */
	public MainWindow(FileManager manager, ActionListener al, boolean verbose) {
		super("ShaderUtil");
		this.manager = manager;
		this.verbose = verbose;
		final int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		if(verbose) {
			System.out.printf("Menü erzeugen...");
		}
		JMenuBar bar = new JMenuBar();
		JMenu file = new JMenu("Datei");
		JMenuItem neu = new JMenuItem("Neu...");
		neu.addActionListener(al);
		neu.setActionCommand(MainManager.NEW_FILE);
		neu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, shortcut));
		file.add(neu);
		JMenuItem beenden = new JMenuItem("Beenden");
		beenden.setActionCommand(MainManager.EXIT);
		beenden.addActionListener(al);
		beenden.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, shortcut));
		JMenuItem open = new JMenuItem("Öffnen...");
		open.setActionCommand("openFile");
		open.addActionListener(this);
		open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, shortcut));
		file.add(open);
		file.addSeparator();
		JMenuItem save = new JMenuItem("Sichern");
		save.addActionListener(this);
		save.setActionCommand("save");
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, shortcut));
		file.add(save);
		JMenuItem saveUnder = new JMenuItem("Sichern unter...");
		saveUnder.addActionListener(this);
		saveUnder.setActionCommand("saveUnder");
		saveUnder.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, shortcut + KeyEvent.SHIFT_DOWN_MASK));
		file.add(saveUnder);
		file.addSeparator();
		file.add(beenden);
		bar.add(file);
		JMenu window = new JMenu("Fenster");
		JMenuItem close = new JMenuItem("Fenster schließen");
		close.addActionListener(this);
		close.setActionCommand("close");
		close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, shortcut));
		JMenuItem pack = new JMenuItem("Größe anpassen");
		pack.addActionListener(this);
		pack.setActionCommand("pack");
		pack.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.ALT_GRAPH_DOWN_MASK));
		window.add(pack);
		window.addSeparator();
		window.add(close);
		bar.add(window);
		setJMenuBar(bar);
		if(verbose) {
			System.out.println(" Fertig.");
			System.out.print("Fensterinhalt erzeugen...");
		}
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				disposing();
			}
		});
		source = new JTextArea();
		source.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if(ignoreKey) {
					ignoreKey = false;
				} else {
					if(!edited) {
						setTitle("*" + getTitle());
					}
					edited = true;
				}
			}
		});
		JScrollPane spane = new JScrollPane(source);
		getContentPane().add(spane);
		setSize(300, 200);
		setLocationRelativeTo(null);
		setVisible(true);
		if(verbose) {
			System.out.println(" Fertig.");
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		switch(e.getActionCommand()) {
		case "openFile":
			openFile();
			break;
		case "save":
			saveToFile(false);
			break;
		case "saveUnder":
			saveToFile(true);
			break;
		case "close":
			disposing();
			break;
		case "pack":
			pack();
			break;
		}
	}
	
	/**
	 * Öffnet eine Datei, die der Nutzer auswählt.
	 */
	private void openFile() {
		if(verbose) {
			System.out.print("Dateimanager wird erzeugt...");
		}
		if(edited) {
			switch(JOptionPane.showConfirmDialog(this, "Möchten Sie Ihre Änderungen an dem Shader speichern?",
					"Änderungen sichern", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)) {
			case JOptionPane.YES_OPTION:
				saveToFile(false);
				break;
			case JOptionPane.NO_OPTION:
				break;
			default:
				if(verbose) {
					System.out.println(" Abgebrochen.");
				}
				return;
			}
		}
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new FileNameExtensionFilter("Shaderquelltext (*.shad, *.glsl, *.vert, *.frag)", "shad", "glsl", "frag", "vert"));
		chooser.setMultiSelectionEnabled(false);
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		if(opened != null) {
			chooser.setCurrentDirectory(opened.getFile().getAbsoluteFile());
		}
		if(verbose) {
			System.out.println(" Fertig.");
		}
		switch(chooser.showOpenDialog(this)) {
		case JFileChooser.APPROVE_OPTION:
			openFile(chooser.getSelectedFile());
			break;
		default:
			return;
		}
	}
	
	/**
	 * Speichert die geöffnete Datei.
	 * 
	 * @param newName ob die Datei unter einem neuen Namen gespeichert werden soll
	 */
	private void saveToFile(boolean newName) {
		File toWrite = null;
		MimeType mime = null;
		String shaderSource = source.getText();
		if(opened != null) {
			opened.setSource(shaderSource);
			toWrite = opened.getFile();
			mime = opened.getMimeType();
		} else {
			newName = true;
		}
		if(newName) {
			convertFile.execute();
			toWrite = chooseFileToSave();
			if(toWrite == null) {
				return;
			}
			int mtbe = mimeTypeByExtension(toWrite);
			boolean addExtension = mtbe == -1;
			if(addExtension) {
				mtbe = chooseMimeType();
				if(mtbe == JOptionPane.CLOSED_OPTION) {
					return;
				}
			}
			if(opened == null) {
				boolean toCheck = true;
				if(!addExtension) {
					String exte = FileManager.getExtension(toWrite.getName());
					if(exte.equalsIgnoreCase("vert")) {
						opened = new VertexShader(shaderSource);
						toCheck = false;
					} else if(exte.equalsIgnoreCase("frag")) {
						opened = new FragmentShader(shaderSource);
						toCheck = false;
					} else {
						toCheck = true;
					}
				}
				if(toCheck) {
					switch(chooseShaderType("Shadertyp des zu sichernden Shaders wählen:\n")) {
					case 0:
						opened = new VertexShader(shaderSource);
						break;
					case 1:
						opened = new FragmentShader(shaderSource);
						break;
					default:
						return;
					}
				}
			}
			if(mtbe == 0) {
				if(addExtension) {
					String ext;
					if(opened instanceof VertexShader) {
						ext = ".vert";
					} else {
						ext = ".frag";
					}
					toWrite = new File(toWrite.getAbsolutePath() + ext);
				}
				mime = MimeType.TEXT;
			} else {
				if(addExtension) {
					toWrite = new File(toWrite.getAbsolutePath() + ".shad");
				}
				mime = MimeType.SHADER;
			}
			if(toWrite.exists()) {
				switch(JOptionPane.showConfirmDialog(this, "Soll die existierende Datei überschrieben werden?",
						"Datei existiert bereits!", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE)) {
				case JOptionPane.CANCEL_OPTION:
				case JOptionPane.CLOSED_OPTION:
					return;
				}
			}
			if(opened.getFile() == null) {
				opened.setFile(toWrite);
				setTitle("*GLSL - " + toWrite.getAbsolutePath() + " - ShaderUtil");
			}
		}
		edited = false;
		ignoreKey = true;
		String title = getTitle();
		if(title.startsWith("*")) {
			setTitle(getTitle().substring(1));
		}
		switch(mime) {
		case SHADER:
			byte[] data;
			try {
				data = convertFile.get();
			} catch(Exception e) {
				System.err.print("Fehler aufgetreten: ");
				if(verbose) {
					System.err.println();
					e.printStackTrace();
				} else {
					System.err.println(e.getMessage());
				}
				data = opened.getRawFileData();
			}
			manager.writeShadFile(toWrite, data);
			break;
		case TEXT:
			manager.writeTextFile(toWrite, opened);
			break;
		}
	}
	
	/**
	 * Gibt {@code 1} zurück, wenn die Datei das Format der IDE verwendet, {@code 0} wenn es eine Textdatei ist
	 * und {@code -1}, wenn keine bekannte Endung verwendet wird.
	 * 
	 * @param file die Datei dessen Endung erkannt werden soll
	 * @return den MimeType, wie oben beschrieben
	 */
	private int mimeTypeByExtension(File file) {
		if(file != null) {
			String extension = FileManager.getExtension(file.getName());
			if(extension.equalsIgnoreCase("shad")) {
				return 1;
			} else if(extension.equalsIgnoreCase("glsl") || 
					extension.equalsIgnoreCase("vert") || extension.equalsIgnoreCase("frag")) {
				return 0;
			}
		}
		return -1;
	}
	
	/**
	 * Fragt den Nutzer, welches Dateiformat er verwenden möchte.
	 * 
	 * @return {@code 0}, wenn eine Textdatei erzeugt werden soll, {@code 1} wenn das Dateiformat dieser IDE
	 * 			verwendet werden soll, oder {@link JOptionPane#CLOSED_OPTION} wenn der Nutzer den Dialog weggeklickt hat
	 */
	private int chooseMimeType() {
		Object[] options = new Object[] {
				"Textdatei (*.glsl, *.vert, *.frag)", 
				"Shader (*.shad)"
		};
		return JOptionPane.showOptionDialog(this, "Dateiformat auswählen:", "Speichern als",
				JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
	}
	
	/**
	 * Befragt den Nutzer nach einer Datei, in die der geöffnete Shader gespeichert werden soll.
	 * 
	 * @return die Datei, die der Nutzer ausgewählt hat
	 */
	private File chooseFileToSave() {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		chooser.setMultiSelectionEnabled(false);
		if(chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		} else {
			return null;
		}
	}
	
	/**
	 * Befragt den Nutzer, was für einen Shader er geschrieben hat.
	 * 
	 * @param text der Text, der im Dialog angezeigt werden soll
	 * @return {@code 0}, wenn VertexShader ausgewählt wurde, {@code 1} für FragmentShader, oder 
	 * 			{@link JOptionPane#CLOSED_OPTION}, wenn der Nutzer den Dialog weggeklickt hat
	 */
	private int chooseShaderType(String text) {
		Object[] options = new Object[] {
				"VertexShader",
				"FragmentShader"
		};
		return JOptionPane.showOptionDialog(this, text, "Shadertyp",
				JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null);
	}
	
	/**
	 * Öffnet die angegebene Datei.
	 * 
	 * @param toOpen die zu öffnende Datei
	 */
	public void openFile(File toOpen) {
		String ap = toOpen.getAbsolutePath();
		if(verbose) {
			System.out.printf("Zu öffnende Datei: " + ap + "\n");
		}
		switch(FileManager.getExtension(toOpen.getName()).toLowerCase()) {
		case "shad":
			opened = manager.openShadFile(toOpen);
			break;
		case "vert":
			opened = new VertexShader(manager.openTextFile(toOpen), toOpen);
			break;
		case "frag":
			opened = new FragmentShader(manager.openTextFile(toOpen), toOpen);
			break;
		default:
			String[] content = manager.openTextFile(toOpen);
			if(chooseShaderType("Was für ein Shader ist \"" + toOpen.getName() + "\"?") == 0) {
				opened = new VertexShader(content, toOpen);
			} else {
				opened = new FragmentShader(content, toOpen);
			}
		}
		source.setEditable(toOpen.canWrite());
		source.setText(opened.getSource());
		if(opened.getSource() == null) {
			JOptionPane.showMessageDialog(this, "Datei existiert nicht oder Lesefehler aufgetreten!",
					"ShaderUtil: Fehler", JOptionPane.ERROR_MESSAGE);
			if(verbose) {
				System.out.println("Fenster wird geschlossen.");
			}
			dispose();
			return;
		}
		setTitle("GLSL - " + ap + " - ShaderUtil");
		edited = false;
		ignoreKey = false;
		if(verbose) {
			System.out.printf("Datei geöffnet.\n");
		}
	}

	/**
	 * Öffnet die angegebene Datei.
	 * 
	 * @param file die zu öffnende Datei
	 */
	public void openFile(String file) {
		openFile(new File(file));
	}
	
	public boolean disposing() {
		if(edited) {
			int choice = JOptionPane.showConfirmDialog(this, "Möchten Sie Ihre Änderungen an dem Shader sichern?",
					"Änderungen sichern", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			switch(choice) {
			case JOptionPane.YES_OPTION:
				saveToFile(false);
				break;
			case JOptionPane.CANCEL_OPTION:
			case JOptionPane.CLOSED_OPTION:
				return false;
			}
		}
		manager = null;
		super.dispose();
		return true;
	}
	
	/**
	 * Gibt zurück, ob der Shader bearbeitet wurde oder nicht.
	 * 
	 * @return ob die Datei bearbeitet wurde oder nicht
	 */
	public boolean isEdited() {
		return edited;
	}
	
	/**
	 * Aktiviert den Verbose-Modus in diesem Fenster.
	 * 
	 * @param verbose ob der Verbose-Modus aktiviert werden soll oder nicht
	 */
	public void setVerbosity(boolean verbose) {
		this.verbose = verbose;
	}
}