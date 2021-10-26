package hahn.manuel.ShaderUtil;

import hahn.manuel.Utils.ByteHelper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Diese Klasse müsste eigentlich plattformunabhängig sein. 
 * Sie kümmert sich um alles, was mit Dateien lesen & schreiben zu tun hat.
 * 
 * @author Manuel Hahn
 * @since 12.05.2017
 */
public class FileManager {
	private boolean verbose;
	
	/**
	 * Öffnet die angegebene *.shad-Datei.
	 * 
	 * @param file die Datei im Format, das von diesem Programm geschrieben wird
	 * @return einen Shader mit all den Informationen, die in der Datei stehen
	 */
	public Shader openShadFile(File file) {
		Shader toReturn = null;
		byte[] bytes = new byte[(int) file.length()];
		try (BufferedInputStream is = new BufferedInputStream(new FileInputStream(file))) {
			is.read(bytes);
		} catch(FileNotFoundException e) {
			System.err.println("Datei existiert nicht!");
		} catch(IOException e) {
			System.err.print("Input/Outputfehler aufgetreten: ");
			if(verbose) {
				System.err.println();
				e.printStackTrace();
			} else {
				System.err.println(e.getMessage());
			}
		}
		ByteHelper bh = new ByteHelper();
		int oldCount = 0;
		int count = bh.bytesToInt(new byte[] {bytes[oldCount], bytes[++oldCount], bytes[++oldCount], bytes[++oldCount]}) + 4;
		ShaderType st = ShaderType.valueOf(new String(bh.subBytes(bytes, ++oldCount, count)));
		oldCount = count;
		count = bh.bytesToInt(new byte[] {bytes[oldCount], bytes[++oldCount], bytes[++oldCount], bytes[++oldCount]}) + oldCount;
		String[] uniforms = getStringArray(bh.subBytes(bytes, ++oldCount, ++count));
		oldCount = count;
		count = bh.bytesToInt(new byte[] {bytes[oldCount], bytes[++oldCount], bytes[++oldCount], bytes[++oldCount]}) + oldCount;
		String[] attributes = getStringArray(bh.subBytes(bytes, ++oldCount, ++count));
		String shaderSource = new String(bh.subBytes(bytes, count, bytes.length));
		switch(st) {
		case VERTEX:
			toReturn = new VertexShader(uniforms, attributes, shaderSource, file);
			break;
		case FRAGMENT:
			toReturn = new FragmentShader(uniforms, attributes, shaderSource, file);
			break;
		default:
			System.err.println("Shadertyp wird (noch) nicht unterstützt!");
			throw new IllegalArgumentException("Datei enthält nicht unterstützten Shadertyp!");
		}
		return toReturn;
	}
	
	/**
	 * Gibt ein String-Array zurück, das aus den angegebenen bytes erkannt wird.
	 * Es wird das proprietäre Format verwendet.
	 * 
	 * @param allBytes ein Array mit den bytes, aus welchen die Strings erkannt werden sollen
	 * @return ein Array mit Strings, die aus diesen bytes interpretiert wurden
	 */
	private String[] getStringArray(byte[] allBytes) {
		ByteHelper bh = new ByteHelper();
		ArrayList<String> strings = new ArrayList<>();
		for(int byteCount, index = 0; index < allBytes.length;) {
			byteCount = bh.bytesToInt(bh.subBytes(allBytes, index, (index += 4)));
			byte[] uniform = bh.subBytes(allBytes, index, (index += byteCount));
			strings.add(new String(uniform));
		}
		return strings.toArray(new String[strings.size()]);
	}
	
	/**
	 * Öffnet die angegebene Textdatei und gibt ihren Inhalt zurück.
	 * 
	 * @param file die Textdatei, die gelesen werden soll
	 * @return den Inhalt der Datei als einzelner String
	 */
	public String[] openTextFile(File file) {
		ArrayList<String> builder = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			while(reader.ready()) {
				String line = reader.readLine();
				builder.add(line);
			}
		} catch(FileNotFoundException e) {
			System.err.println("Datei existiert nicht!");
			return null;
		} catch (IOException e) {
			System.err.print("Input/Outputfehler aufgetreten: ");
			if(verbose) {
				System.err.println();
				e.printStackTrace();
			} else {
				System.err.println(e.getMessage());
			}
			return null;
		}
		return (String[]) builder.toArray(new String[builder.size()]);
	}
	
	/**
	 * Konvertiert eine Textdatei in das Format dieses Programms.
	 * 
	 * @param file die zu konvertierende Textdatei
	 */
	public void convertTextFile(File file, File toSave) {
		if(verbose) {
			System.out.println("Datei überprüfen...");
		}
		if(toSave != null) {
			if(toSave.isDirectory()) {
				String fileName = file.getName();
				toSave = new File(toSave.getAbsolutePath() + fileName.substring(0, fileName.lastIndexOf('.')) + ".shad");
			}
			if(toSave.exists()) {
				toSave = userQuestion("Die Datei, in welche geschrieben werden soll,"
						+ " existiert bereits. Überschreiben?") ? toSave : null;
			}
			if(toSave != null && !getExtension(toSave.getName()).equalsIgnoreCase("shad")) {
				toSave = new File(toSave.getAbsolutePath() + toSave.getName() + ".shad");
			}
		}
		String extension = getExtension(file.getName());
		if(extension.equalsIgnoreCase("shad")) {
			System.err.println("Datei hat falsches Format! Sie wurde bereits konvertiert.");
			return;
		}
		boolean vertex = false;
		switch(extension.toLowerCase()) {
		case "vert":
			vertex = true;
			break;
		case "frag":
			vertex = false;
			break;
		default:
			vertex = userQuestion("Ist die Datei ein VertexShader?");
			break;
		}
		if(verbose) {
			System.out.println("Datei ist kompatibel, sie wird konvertiert...");
		}
		Shader shader;
		String[] content = openTextFile(file);
		if(vertex) {
			shader = new VertexShader(content, null);
		} else {
			shader = new FragmentShader(content, null);
		}
		toSave = toSave == null ? createShadFile(file) : toSave;
		writeShadFile(toSave, shader.getRawFileData());
	}
	
	/**
	 * Erzeugt eine leere Datei mit der Endung *.shad, die in dem Ordner der angegebenen Datei ist.
	 * 
	 * @param oldFile die alte Datei, dessen Pfad und Name übernommen werden
	 * @return eine Datei, die im gleichen Ordner liegt und genauso wie die angegebene Datei heißt, mit der Endung *.shad
	 */
	private File createShadFile(File oldFile) {
		if(verbose) {
			System.out.println("*.shad-Datei wird erzeugt...");
		}
		String name = oldFile.getName(), aPath = oldFile.getAbsolutePath();
		aPath = aPath.substring(0, aPath.lastIndexOf(File.separatorChar)) + File.separatorChar + name.substring(0, name.lastIndexOf('.'));
		return new File(aPath + ".shad");
	}
	
	/**
	 * Lässt den Nutzer eine Aktion bestätigen.
	 * 
	 * @param text die Beschreibung der abzunickenden Aktion
	 * @return ob der Nutzer bestätigt hat oder nicht
	 */
	private boolean userQuestion(String text) {
		System.out.print(text + " j/n ");
		boolean is = false;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
			is = !reader.readLine().equalsIgnoreCase("n");
		} catch(IOException e) {
			System.err.print("Fehler aufgetreten: ");
			if(verbose) {
				System.err.println();
				e.printStackTrace();
			} else {
				System.err.println(e.getMessage());
			}
		}
		return is;
	}
	
	/**
	 * Schreibt eine Datei in dem *.shad-Format.
	 * 
	 * @param file die Datei, in welche geschrieben werden soll
	 * @param content der binäre Inhalt, der in die angegebene Datei geschrieben werden soll
	 */
	public void writeShadFile(File file, byte[] content) {
		if(verbose) {
			System.out.println("Daten schreiben...");
		}
		try (BufferedOutputStream fileOS = new BufferedOutputStream(new FileOutputStream(file))) {
			fileOS.write(content);
			fileOS.flush();
		} catch (IOException e) {
			System.err.print("Input/Outputfehler augetreten: ");
			if(verbose) {
				System.err.println();
				e.printStackTrace();
			} else {
				System.err.println(e.getMessage());
			}
			return;
		}
		if(verbose) {
			System.out.println("Datei geschrieben.");
		}
	}
	
	/**
	 * Stellt ein, ob genau beschrieben werden soll, was gemacht wird oder nicht.
	 * 
	 * @param verbose ob im erweitertem Logmodus gearbeitet wird oder nicht
	 */
	public void setVerbosity(boolean verbose) {
		this.verbose = verbose;
	}
	
	/**
	 * Schreibt den Shader in die angegebene Datei als Text.
	 * 
	 * @param file in die zu schreibende Datei
	 * @param shader der zu schreibende Shader
	 */
	public void writeTextFile(File file, Shader shader) {
		if(verbose) {
			System.out.println("Datei wird geschrieben: " + file.getAbsolutePath());
		}
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			writer.write(shader.getSource());
			writer.flush();
		} catch(IOException e) {
			System.err.print("Input/Outputfehler aufgetreten: ");
			if(verbose) {
				System.err.println();
				e.printStackTrace();
			} else {
				System.err.println(e.getMessage());
			}
		}
		if(verbose) {
			System.out.println("Fertig.");
		}
	}
	
	/**
	 * Gibt die Endung einer Datei ohne Punkt zurück.
	 * Beispiel: {@code getExtension("example.shad")} gibt {@code shad} zurück.
	 * 
	 * @param filename der Dateiname
	 * @return die Endung der Datei ohne Punkt
	 */
	public static String getExtension(String filename) {
		return filename.substring(filename.lastIndexOf('.') + 1);
	}
}