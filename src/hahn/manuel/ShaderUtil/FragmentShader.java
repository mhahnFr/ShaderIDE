package hahn.manuel.ShaderUtil;

import java.io.File;

/**
 * Diese Klasse repräsentiert einen FragmentShader.
 * 
 * @author Manuel Hahn
 * @since Unknown
 */
public class FragmentShader extends Shader {

	/**
	 * Erzeugt einen FragmentShader mit dem angegebenen Quelltext.
	 * 
	 * @param source der Quelltext dieses Shaders
	 * @param file die Datei, in welcher dieser Shader gespeichert ist
	 */
	public FragmentShader(String[] source, File file) {
		super(source, file);
	}
	
	/**
	 * Erzeugt einen FragmentShader mit dem angegebenen Quelltext.
	 * 
	 * @param source der Quelltext dieses Shaders
	 */
	public FragmentShader(String[] source) {
		super(source, null);
	}
		
	public FragmentShader(String source, File file) {
		super(null, file);
		setSource(source);
	}
	
	public FragmentShader(String source) {
		super(null, null);
		setSource(source);
	}
	
	public FragmentShader(String[] uniforms, String[] attributes, String source, File file) {
		super(uniforms, attributes, source, file);
	}

	@Override
	protected byte[] getShaderTypeBytes() {
		return ShaderType.FRAGMENT.name().getBytes();
	}
}