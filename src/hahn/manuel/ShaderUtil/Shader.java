package hahn.manuel.ShaderUtil;

import hahn.manuel.Utils.ByteHelper;
import hahn.manuel.Utils.StringPro;

import java.io.File;
import java.util.ArrayList;

/**
 * Diese Klasse repr�sentiert einen Shader. Kompilieren unter Android / iOS sollte ebenfalls hierhin.
 * 
 * @author Manuel Hahn
 * @since 16.05.2017
 */
public abstract class Shader {
	/**
	 * Der Quelltext dieses Shaders.
	 */
	protected String[] source;
	
	/**
	 * Die in diesem Shader deklarierten attributes.
	 */
	protected String[] attributes;
	
	/**
	 * Die in diesem Shader deklarierten uniforms.
	 */
	protected String[] uniforms;
	
	/**
	 * Die Datei, in welcher dieser Shader gespeichert ist.
	 */
	protected File file;
	
	/**
	 * Das verwendete Dateiformat.
	 */
	protected MimeType mtype; 
	
	/**
	 * Erzeugt einen Basisshader.
	 * 
	 * @param source der Quelltext des Shaders
	 */
	protected Shader(String[] source, File file) {
		this.source = source;
		this.file = file;
		if(this.file != null) {
			generateMimeType();
		}
		if(this.source != null) {
			generateBindingValues();
		}
	}
	
	/**
	 * Erzeugt einen Basisshader.
	 * 
	 * @param uniforms die Uniforms, die in dem Quelltext definiert wurden
	 * @param attributes die Attributes, die in dem Quelltext definiert wurden
	 * @param source der Quelltext dieses Shaders
	 * @param file die Datei, in welcher dieser Shader gespeichert wurde
	 */
	protected Shader(String[] uniforms, String[] attributes, String source, File file) {
		this.uniforms = uniforms;
		this.attributes = attributes;
		this.file = file;
		this.mtype = MimeType.SHADER;
		setSource(source);
	}
	
	/**
	 * Erkennt das verwendete Dateiformat.
	 */
	private void generateMimeType() {
		switch(FileManager.getExtension(file.getName()).toLowerCase()) {
		case "shad":
			mtype = MimeType.SHADER;
			break;
		default:
			mtype = MimeType.TEXT;
			break;
		}
	}
	
	/**
	 * Gibt den Quelltext dieses Shaders zur�ck.
	 * 
	 * @return den Quelltext als einzelnen String
	 */
	public String[] getSourceLines() {
		return source;
	}
	
	/**
	 * Gibt die im Shader deklarierten attributes zur�ck.
	 * 
	 * @return die attributes in einem Array
	 */
	public String[] getAttributes() {
		return attributes;
	}
	
	/**
	 * Gibt die in diesem Shader deklarierten uniforms zur�ck.
	 * 
	 * @return die uniforms in einem String-Array
	 */
	public String[] getUnifomrs() {
		return uniforms;
	}
	
	/**
	 * Entfernt die Kommentare im Quelltext. Leere Zeilen bleiben allerdings erhalten.
	 */
	private void generateRawSource() {
		StringBuilder builder = new StringBuilder();
		for(String line : source) {
			if(line.contains("//")) {
				line = line.substring(0, line.indexOf("//"));
			}
			builder.append(line + '\n');
		}
		String source = builder.toString();
		if(source.contains("/*") && source.contains("*/")) {
			int begin, end = 0, bLast = source.lastIndexOf("/*"), eLast = source.lastIndexOf("*/");
			do {
				StringPro sp = new StringPro(source);
				begin = source.indexOf("/*", end);
				if(begin < 0) {
					break;
				}
				end = source.indexOf("*/", end + 1);
				if(end < 0) {
					break;
				}
				source = sp.delete(begin, end + 2);
			} while(begin < bLast && end < eLast);
		}
	}
	
	/**
	 * Erkennt die in diesem Shader deklarierten uniforms und attributes und gibt sie zur�ck.
	 */
	public void generateBindingValues() {
		ArrayList<String> uniforms = new ArrayList<>();
		ArrayList<String> attributes = new ArrayList<>();
		generateRawSource();
		StringPro proLine;
		for(String line : source) {
			proLine = new StringPro(line);
			String pot;
			try {
				pot = line.substring(line.lastIndexOf(' ') + 1, line.lastIndexOf(';'));
			} catch(Exception e) {
				pot = line;
			}
			if(proLine.containsWord("uniform")) {
				if(!pot.contains("struct")) {
					uniforms.add(pot.trim());
				}
			} else if(proLine.containsWord("attribute")) {
				attributes.add(pot.trim());
			}
		}
		this.uniforms = uniforms.toArray(new String[uniforms.size()]);
		this.attributes = attributes.toArray(new String[attributes.size()]);
	}
	
	/**
	 * Gibt die Daten dieses Shader auf byte-Basis zur�ck.
	 * 
	 * @return ein byte-Array mit den Informationen dieses Shaders, um ihn zu speichern
	 */
	public byte[] getRawFileData() {
		generateBindingValues();
		/*
		 * Format:
		 * int L�ngeShaderType (bytes)
		 * ShaderType
		 * int L�ngeUniforms (bytes)
		 * int L�ngeEinzelnUniform (bytes)
		 * uniforms
		 * ...
		 * int L�ngeAttributes (bytes)
		 * int L�ngeAttribute (bytes)
		 * attributes
		 * ...
		 * Rest: Quelltext
		 */
		ArrayList<Byte> bytes = new ArrayList<>();
		ByteHelper bh = new ByteHelper();
		// Bytes des Shadertyps holen
		byte[] byMi = getShaderTypeBytes();
		// Bytel�nge des Shadertyps hinzuf�gen
		for(byte b : bh.intToBytes(byMi.length)) {
			bytes.add(b);
		}
		// Shadertyp hinzuf�gen
		for(byte b : byMi) {
			bytes.add(b);
		}
		bytes = addStringArray(uniforms, bytes);
		/*byte[][] attBytes = new byte[attributes.length][];
		int attByteLength = 0;
		for(int i = 0; i < attributes.length; i++) {
			// Bytes des Attributes holen
			byte[] aB = attributes[i].getBytes();
			// Bytes zwischenspeichern
			attBytes[i] = aB;
			// Gesamtl�nge der bytes z�hlen
			attByteLength += aB.length + 4;
		}
		// byte-Gesamtl�nge der attributes hinzuf�gen
		for(byte b : bh.intToBytes(attByteLength)) {
			bytes.add(b);
		}
		for(byte[] bts : attBytes) {
			// byte-L�nge des Attributes hinzuf�gen
			for(byte b : bh.intToBytes(bts.length)) {
				bytes.add(b);
			}
			// attribute-Bytes hinzuf�gen
			for(byte b : bts) {
				bytes.add(b);
			}
		}*/
		bytes = addStringArray(attributes, bytes);
		// Der Rest der Datei wird der Quelltext
		for(byte b : getSource().getBytes()) {
			bytes.add(b);
		}
		return new ByteHelper().castToByte(bytes.toArray(new Byte[bytes.size()]));
	}
	
	/**
	 * F�gt ein String-Array der Liste mit den bytes hinzu.
	 * 
	 * @param array das hinzuzuf�gende String-Array
	 * @param list die Liste, zu welcher die bytes hinzugef�gt werden sollen
	 * @return die angegebene Liste, um die bytes des String-Arrays erweitert
	 */
	protected ArrayList<Byte> addStringArray(String[] array, ArrayList<Byte> list) {
		ByteHelper bh = new ByteHelper();
		byte[][] uniBytes = new byte[array.length][];
		int byteLength = 0;
		for(int i = 0; i < array.length; i++) {
			// Bytes des Uniforms holen
			byte[] uB = array[i].getBytes();
			// Bytes zwischenspeichern
			uniBytes[i] = uB;
			// Gesamtl�nge der bytes z�hlen
			byteLength += uB.length + 4;
		}
		// byte-Gesamtl�nge der Uniforms hinzuf�gen
		for(byte b : bh.intToBytes(byteLength)) {
			list.add(b);
		}
		for(byte[] bts : uniBytes) {
			// byte-L�nge des Uniforms hinzuf�gen
			for(byte b : bh.intToBytes(bts.length)) {
				list.add(b);
			}
			// uniform-Bytes hinzuf�gen
			for(byte b : bts) {
				list.add(b);
			}
		}
		return list;
	}
	
	/**
	 * Gibt den Typ des Shaders als byte[] zur�ck.
	 * 
	 * @return den ShaderTyp in bin�rer Form
	 */
	protected abstract byte[] getShaderTypeBytes();
	
	/**
	 * Ersetzt den verkn�pften Quelltext mit dem angegebenen.
	 * 
	 * @param source der ge�nderte Quelltext dieses Shaders
	 */
	public void setSourceLines(String[] source) {
		this.source = source;
	}
	
	/**
	 * Gibt die Datei, in welcher dieser Shader gespeichert ist, zur�ck.
	 * 
	 * @return die Datei dieses Shaders
	 */
	public File getFile() {
		return file;
	}
	
	/**
	 * Setzt die Datei, in welche dieser Shader gespeichert werden wird. Kann nur einmal aufgerufen werden!
	 * Generiert au�erdem das verwendete Dateiformat.
	 * 
	 * @param newFile die Datei dieses Shaders
	 */
	public void setFile(File file) {
		if(this.file == null) {
			this.file = file;
			generateMimeType();
		} else {
			throw new IllegalStateException("Datei wurde bereits hinzugef�gt oder neue "
					+ "Datei existiert nicht oder neue Datei ist keine Datei!");
		}
	}
	
	/**
	 * Gibt den Quelltext dieses Shaders zur�ck.
	 * 
	 * @return den Quelltext dieses Shaders
	 */
	public String getSource() {
		StringBuilder builder = new StringBuilder();
		for(String line : source) {
			builder.append(line + '\n');
		}
		builder.deleteCharAt(builder.length() - 1);
		return builder.toString();
	}
	
	/**
	 * Ersetzt den Quelltext dieses Shaders.
	 * 
	 * @param source der neue Quelltext dieses Shaders
	 */
	public void setSource(String source) {
		ArrayList<String> list = new ArrayList<>();
		int oldIndex, sourceLength = source.length(), index = source.indexOf('\n');
		list.add(source.substring(0, index));
		for(index++; index < sourceLength; index++) {
			oldIndex = index;
			index = source.indexOf('\n', index);
			if(index < 0 || index > sourceLength) {
				list.add(source.substring(oldIndex));
				break;
			}
			list.add(source.substring(oldIndex, index));
		}
		this.source = list.toArray(new String[list.size()]);
	}
	
	/**
	 * Gibt das Dateiformat zur�ck, mit welcher dieser Shader gespeichert wurde.
	 * 
	 * @return das Dateiformat dieses Shaders
	 */
	public MimeType getMimeType() {
		return mtype;
	}
}