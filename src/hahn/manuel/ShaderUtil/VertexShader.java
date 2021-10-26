package hahn.manuel.ShaderUtil;

import java.io.File;

/**
 * Diese Klasse repräsentiert einen VertexShader.
 * 
 * @author Manuel Hahn
 * @since Unknown
 */
public class VertexShader extends Shader {

	/**
	 * Erzeugt einen VertexShader mit dem angegebenen Quelltext und der angegebenen Datei.
	 * 
	 * @param source der Quelltext dieses Shaders
	 * @param file die Datei, in welcher dieser Shader gespeichert ist
	 */
	public VertexShader(String[] source, File file) {
		super(source, file);
	}
	
	/**
	 * Erzeugt einen VertexShader mit dem angegebenen Quelltext.
	 * 
	 * @param source der Quelltext dieses Shaders
	 */
	public VertexShader(String[] source) {
		super(source, null);
	}

	public VertexShader(String source, File file) {
		super(null, file);
		setSource(source);
	}
	
	public VertexShader(String source) {
		super(null, null);
		setSource(source);
	}
	
	public VertexShader(String[] uniforms, String[] attributes, String source, File file) {
		super(uniforms, attributes, source, file);
	}

	@Override
	protected byte[] getShaderTypeBytes() {
		return ShaderType.VERTEX.name().getBytes();
	}
}