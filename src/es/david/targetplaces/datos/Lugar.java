package es.david.targetplaces.datos;

import com.google.android.gms.maps.model.LatLng;
import android.net.Uri;

/** Clase que representa un lugar (POJO)
 * 
 * @author David
 *
 */
/**
 * @author David
 *
 */
/**
 * @author David
 *
 */
/**
 * @author David
 *
 */
/**
 * @author David
 * 
 */
public class Lugar {

	/**
	 * Variables que contiene cada lugar
	 */
	private String nombre;
	private String descripcion;
	private Uri imagen;
	private double latitud, longitud;
	private int valoracion;

	/**
	 * Getters y Setters para recuperar los datos del lugar
	 * 
	 * @return LatLng
	 */
	public LatLng getPosicion() {
		return new LatLng(latitud, longitud);
	}

	/**
	 * @param latitud
	 * @param longitud
	 */
	public void setPosicion(double latitud, double longitud) {
		this.latitud = latitud;
		this.longitud = longitud;
	}

	/**
	 * @return imagen
	 */
	public Uri getImage() {
		return imagen;
	}

	/**
	 * @param imagen
	 */
	public void setImage(Uri imagen) {
		this.imagen = imagen;
	}

	/**
	 * @return nombre
	 */
	public String getNombre() {
		return nombre;
	}

	/**
	 * @param nombre
	 */
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	/**
	 * @return descripcion
	 */
	public String getDescripcion() {
		return descripcion;
	}

	/**
	 * @param descripcion
	 */
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	/**
	 * @return valoracion
	 */
	public int getValoracion() {
		return valoracion;
	}

	/**
	 * @param valoracion
	 */
	public void setValoracion(int valoracion) {
		this.valoracion = valoracion;
	}

	/**
	 * Constructores de la clase
	 */
	public Lugar(String nombre, String descripcion) {
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.imagen = null;
		this.latitud = 0;
		this.longitud = 0;
	}

	public Lugar(String nombre, String descripcion, Uri imagen) {
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.imagen = imagen;
		this.latitud = 0;
		this.longitud = 0;
	}

	public Lugar(String nombre, String descripcion, double latitud,
			double longitud) {
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.imagen = null;
		this.latitud = latitud;
		this.longitud = longitud;
	}
}