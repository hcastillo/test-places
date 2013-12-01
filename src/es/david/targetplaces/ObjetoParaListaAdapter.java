package es.david.targetplaces;

import android.graphics.Bitmap;

/**
 * Objeto auxiliar para ListaAdapter
 * 
 * @author David
 * 
 */
public class ObjetoParaListaAdapter {
	public ObjetoParaListaAdapter(String nombre, String descripcion,
			int identificador, String imagen) {
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.identificador = identificador;
		this.imagen = imagen;
		this.bitmap = null;
	}

	public String nombre;
	public String descripcion;
	public int identificador;
	public String imagen;
	public Bitmap bitmap;
}