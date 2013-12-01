package es.david.targetplaces;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import es.david.targetplaces.datos.Lugar;
import es.david.targetplaces.datos.LugaresSQLHelper;

/**
 * Actividad que permite crear y mostrar lugares en un mapa
 * 
 * @author David
 */
public class MapaLugarActivity extends FragmentActivity implements
		LocationListener, OnClickListener, OnMapLongClickListener,
		OnMapClickListener, OnInfoWindowClickListener {
	
	// Declaramos los objetos y variables	
	public static final String PREFS_NAME = "MisPreferencias";
	public static boolean mensajeYaMostrado = false;
	public CheckBox noVolverAMostrar;
	public Vibrator vibrador;
	private GoogleMap mapa;
	private LocationManager locationManager;
    private static CameraPosition cp;
	private EditText txtBuscar;
	private ImageButton btnBuscar;
	
	// Asociamos los id de los marker con el id del lugar
	// Guardamos la equivalencia para cuando hagan click en el snippet, saber que lugar hemos de mostrar
	Dictionary<String, Long> markerAlugar = new Hashtable<String, Long>();
	long HeDeMostrarSoloUno = 0;
	
	// Iniciamos la Activity y enlazamos con el Layout
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_mapa_lugar);

		// Llamamos al servicio del vibrador
		vibrador = (Vibrator) MapaLugarActivity.this.getSystemService(Context.VIBRATOR_SERVICE);

		// Enlazamos con los campos de la opción de busqueda
		txtBuscar = (EditText) findViewById(R.id.txtBuscar);
		btnBuscar = (ImageButton) findViewById(R.id.btnBuscar);
		
		// Llamamos al método que permite escuchar los eventos de click en el botón
		btnBuscar.setOnClickListener(this);
		
		 // Comprobamos la disponibilidad de Google Play Servicies
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
 
        // Google Play Services no disponible
        if(status!=ConnectionResult.SUCCESS){ 
 
            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();
        
        // Google Play Services disponible   
        }else { 
        	 
            // Referenciamos SupportMapFragment en el layout
            SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fmMapa);
    
            // Obtenemos el objeto GoogleMap desde el fragment
            mapa = fm.getMap();
            
            if (mapa==null) {
            	Toast.makeText(getApplicationContext(), R.string.toast_obtener_mapa, 
            			Toast.LENGTH_SHORT).show();
            }
 
            // Activamos la localización en el mapa
            mapa.setMyLocationEnabled(true);
            
            // Llamamos a los métodos que permiten escuchar los eventos de click en el mapa
            mapa.setOnMapLongClickListener(this);
            mapa.setOnMapClickListener(this);            
            
            // Llamamos al método que permite escuchar los eventos de click en el snippet
            mapa.setOnInfoWindowClickListener(this);
            
            // Leemos las preferencias y establecemos el tipo de mapa
            SharedPreferences preferencias = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);  
            mapa.setMapType(preferencias.getInt("tipoMapa",GoogleMap.MAP_TYPE_NORMAL));
            
            // Obtenemos el objeto LocationManager desde el servicio del sistema LOCATION_SERVICE
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
 
            // Creamos un objeto criteria para elegir el mejor proveedor de servicios de ubicación
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
     
            // Obtenemos el nombre del mejor proveedor (gps o network)
            String provider = locationManager.getBestProvider(criteria, true);
            
            // Configuramos los proveedores de ubicaciones disponibles
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, this);
            
            // Si no obtenemos ningun proveedor mostramos un AlertDialog para ir a la configuración de ubicaciones del dispositivo           
            if (provider == null) {
            	
            	serviciosUbicacionApagados1();
            	
            	// Centramos el mapa en España
        		LatLng posicionApagado = new LatLng(40.413496, -3.705139);
        		mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(posicionApagado, 5));

            // En caso contrario mostramos la última ubicación conocida mientras se obtiene la ubicación actual	
            } else { 
      	
            	// Obtenemos la última ubicación conocida
            	Location location = locationManager.getLastKnownLocation(provider);
            	
            	// Si la última ubicación está disponible la mostramos en el mapa
            	if (location!=null) {
            	
                // Obtenemos la latitud de la última ubicación conocida
                double latitude = location.getLatitude();
         
                // Obtenemos la longitud de la última ubicación conocida
                double longitude = location.getLongitude();
         
                // Creamos un objeto LatLng
                LatLng latLng = new LatLng(latitude, longitude);

                // Movemos la camara hasta la posición en el mapa
                mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
            	}
            	
            	// Si la última ubicación no está disponible (por ejemplo porque se ha reiniciado el sistema)
            	// Centramos el mapa en España
            	else {
            		
            		LatLng posicionApagado = new LatLng(40.413496, -3.705139);
            		mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(posicionApagado, 5));          		
            	}
            }
        }
                    
	    
	}

	// Geocoder
	@Override
	public void onClick(View v) {
		
		// Ocultamos el teclado al pulsar el botón buscar		
		InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE); 

		inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                   InputMethodManager.HIDE_NOT_ALWAYS);
		
		switch (v.getId()) {
			case R.id.btnBuscar:
				
				// Sólo haremos la búsqueda si hay texto introducido
				if (txtBuscar.getText().toString().trim().length() > 0) {

					try {
						// Obtenemos el objeto Geocoder y le solicitamos la localizacion del texto introducido
						Geocoder geocoder = new Geocoder(this);
						List<Address> results = geocoder.getFromLocationName(txtBuscar.getText()
								.toString().trim(), 1);

						// Sólo leeremos el resultado si no está vacío
						if (results.size() > 0) {

							// Creamos un marcador
							MarkerOptions marker = new MarkerOptions();

							// Le asignamos la localizacion del marcador obteniendo los datos del
							// primer punto devuelto por el Geocoder
							marker.position(new LatLng(results.get(0).getLatitude(), results.get(0)
									.getLongitude()));

							// Localizamos el icono para el marcador
							marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_amarillo));
		
							// Como titulo del marcador le pasamos un String
							// Como snippet ponemos el texto introducido por el usuario
							marker.title(getResources().getString(R.string.marker_nuevo_lugar)).snippet(txtBuscar.getText().toString().trim());

							// Limpiamos el mapa de otros markers y añadimos el marcador de busqueda al mapa
							mapa.clear();
							mapa.addMarker(marker);
							
							// Creamos una actualizacion de la cámara para indicarle donde debe
							// centrarse y que zoom debe mostrar
							CameraUpdate cam = CameraUpdateFactory.newLatLngZoom(new LatLng(results
									.get(0).getLatitude(), results.get(0).getLongitude()),10);

							// Animamos el mapa para que se centre donde le indicamos a la
							// actualizacion de la camara
							mapa.animateCamera(cam);
						}

					// Capturamos la excepción
					} catch (IOException e) {
						
						// Mostramos mensaje de error en caso de haber fallado el Geocoder
						Toast.makeText(this, R.string.toast_error_busqueda,
								Toast.LENGTH_SHORT).show();
					}
				}

			break;
		}		
	}

	// Al hacer click largo en el mapa lanzamos un Intent a NuevoLugarActivity pasando las coordenadas
	@Override
	public void onMapLongClick(LatLng posicion) {
		
		// Activamos el vibrador (35ms)
		vibrador.vibrate(35);
		
		Intent i = new Intent();
		i.putExtra("latitud", posicion.latitude);
		i.putExtra("longitud", posicion.longitude);
		int resultado = 0;
		i.setClass(MapaLugarActivity.this, NuevoLugarActivity.class);
		startActivityForResult(i, resultado);
	}
	
	// Mostramos un Toast la primera vez cuando se hace click corto al mapa 
	// para informar de cómo crear un lugar 
	@Override
	public void onMapClick(LatLng posicion) {
		
		if (!mensajeYaMostrado) {
  		  Toast.makeText(this, R.string.toast_nuevo_lugar,
  				  Toast.LENGTH_SHORT).show();
		}
		mensajeYaMostrado=true;		
	}

	// Definimos el método "serviciosUbicacionApagados"
	private void serviciosUbicacionApagados1() {
		
		// Construimos el dialog
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
        LayoutInflater adbInflater = LayoutInflater.from(this);
        View checkbox = adbInflater.inflate(R.layout.checkbox, null);
        noVolverAMostrar = (CheckBox) checkbox.findViewById(R.id.checkbox);
        adb.setView(checkbox);
        adb.setTitle(R.string.titulo_dialog_servicios_ubicacion1);
        adb.setMessage(R.string.mensaje_dialog_servicios_ubicacion1);
        
        // Comportamiento botón "Configuración"
        adb.setPositiveButton(R.string.opcion_dialog_configuracion, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String checkBoxResult = "NOT checked";
                if (noVolverAMostrar.isChecked())checkBoxResult = "checked";
                
                // Guardamos las preferencias
                SharedPreferences preferencias = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferencias.edit();
                editor.putString("omitirMensaje", checkBoxResult);
                editor.commit();
                
                // Lanzamos un Intent para ir a configuración
                Intent i = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);               
            }
        });

        // Comportamiento botón "Omitir"
        adb.setNegativeButton(R.string.opcion_dialog_omitir, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String checkBoxResult = "NOT checked";
                if (noVolverAMostrar.isChecked())checkBoxResult = "checked";
                
                // Guardamos las preferencias
                SharedPreferences preferencias = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferencias.edit();
                editor.putString("omitirMensaje", checkBoxResult);
                editor.commit();
            }
        });
        
        // Leemos las preferencias
        SharedPreferences preferencias = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String omitirMensaje = preferencias.getString("omitirMensaje", "NOT checked");
        if (!omitirMensaje.equals("checked")) adb.show();
	}
	
	// Al hacer click en el snippet reaccionamos según el tipo de market
	@Override
	public void onInfoWindowClick(Marker marker) {
		
		long aux = 0;
		try {
			 aux = markerAlugar.get(marker.getId());			 
		} catch (Exception e) {			
		}
		
		// Si aux==0 (Marker amarillo del Geocoder) lanzamos un Intent a NuevoLugarActivity
		if (aux==0) {
			Intent i = new Intent();
			i.putExtra("latitud", marker.getPosition().latitude);
			i.putExtra("longitud", marker.getPosition().longitude);
			i.setClass(MapaLugarActivity.this, NuevoLugarActivity.class);
			startActivity(i);
		
		// Si venimos del marker verde (Mostrar un solo lugar) salimos de la pila 
		// para volver a MostrarLugarActivity
		} else if (HeDeMostrarSoloUno > 0) {			
			finish();
			
		// Si venimos del marker rojo (todos los lugares) lanzamos un intent a MostrarLugarActivity
		} else {
			Intent i = new Intent();
			i.putExtra("id",aux);
			i.setClass(MapaLugarActivity.this, MostrarLugarActivity.class);
			startActivity(i);
		}
	}

	// Métodos no implementados
	@Override
    public void onLocationChanged(Location location) {
    }
	
	@Override
	public void onProviderDisabled(String provider) {
	}
	
	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
	
	// Pausamos el Listener cuando la aplicación pierde el foco
	@Override
	protected void onPause() {

	    cp = mapa.getCameraPosition();	
	    locationManager.removeUpdates(this);
	    
	    super.onPause();
	}
	       	
	
	@Override
	protected void onResume() {
		    		
		// Accedemos a la BBDD
				LugaresSQLHelper lugarHelper = new LugaresSQLHelper(getBaseContext());

				// Al hacer click en "ver en el mapa" mostramos sólo ese lugar
				Intent i = getIntent();
				HeDeMostrarSoloUno = i.getLongExtra("soloMuestraEste", 0);
			
				if (HeDeMostrarSoloUno > 0) {
					
					// Mostramos el marker con los datos de ese lugar
					Lugar lugarQueHeDeMostrar = lugarHelper.getLugarById(HeDeMostrarSoloUno);
					LatLng posicionAMostrar = new LatLng(lugarQueHeDeMostrar.getPosicion().latitude,lugarQueHeDeMostrar.getPosicion().longitude);
					cp = CameraPosition.fromLatLngZoom(posicionAMostrar,10);
					MarkerOptions marker = new MarkerOptions();
					marker.title(lugarQueHeDeMostrar.getNombre());
					marker.position(new LatLng(lugarQueHeDeMostrar.getPosicion().latitude,lugarQueHeDeMostrar.getPosicion().longitude));
					marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_verde));
					
					//Dictionary
					Marker miMarker = mapa.addMarker(marker);
					markerAlugar.put(miMarker.getId(), HeDeMostrarSoloUno);

				} else {
					
					// Mostramos todos los lugares
					Cursor listadoLugares = lugarHelper.getAllLugares(false);
					if (listadoLugares.moveToFirst()) {
						
						do {
							// Obtenemos los datos del marker
							Long id = listadoLugares.getLong(listadoLugares.getColumnIndex("_id"));
							String nombre = listadoLugares.getString(listadoLugares.getColumnIndex("nombre"));
							double longitud = listadoLugares.getDouble(listadoLugares.getColumnIndex("longitud"));
							double latitud = listadoLugares.getDouble(listadoLugares.getColumnIndex("latitud"));
							
							// Mostramos el marker
							MarkerOptions marker = new MarkerOptions();
							marker.title(nombre);
							marker.position(new LatLng(latitud, longitud));
							marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_rojo));
							Marker miMarker = mapa.addMarker(marker);
							markerAlugar.put(miMarker.getId(), id);

						} while (listadoLugares.moveToNext());
					}
					
					// Cerramos el cursor
					listadoLugares.close();
				}	
				
				// Reactivamos el Listener cuando la Activity vuelve a ser visible
				if (cp !=null) {
			    	
					mapa.moveCamera(CameraUpdateFactory.newCameraPosition(cp)) ;
		        
					// Configuramos los proveedores de ubicaciones disponibles 
					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, this);
					
			    }
		super.onResume();
	}

	// Enlazamos con el menú
	@Override
	 public boolean onCreateOptionsMenu(Menu menu) {
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mapa_lugar, menu);
		return true;
	}
		
	// Añadimos funcionalidad a las opciones de menú
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menuCapas:
			
			// Construimos el dialog	
			AlertDialog.Builder adb = new AlertDialog.Builder(this);
			adb.setTitle(R.string.titulo_dialog_seleccionar_capa);
			
			// Array de opciones
			String items[] = getResources().getStringArray(R.array.opcion_dialog_tipo_mapa);
			int actual = 0;
			switch (mapa.getMapType()) {
				case GoogleMap.MAP_TYPE_HYBRID : actual=1; break;
				case GoogleMap.MAP_TYPE_TERRAIN : actual=2; break;
			}

			adb.setSingleChoiceItems(items, actual, new DialogInterface.OnClickListener() {
				
				@Override
			    public void onClick(DialogInterface dialog, int n) {
					
					if (n==0) mapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			        if (n==1) mapa.setMapType(GoogleMap.MAP_TYPE_HYBRID );
			        if (n==2) mapa.setMapType(GoogleMap.MAP_TYPE_TERRAIN );
			        			        
			        // Guardamos las preferencias
			        SharedPreferences preferencias = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
			        SharedPreferences.Editor editor = preferencias.edit();
			        editor.putInt("tipoMapa", mapa.getMapType());
			        editor.commit();			        
			        dialog.cancel(); 
			    }
			});
			
			// Comportamiento del botón "Cancelar"
			adb.setNeutralButton(R.string.opcion_cancelar, new DialogInterface.OnClickListener() {
		    	public void onClick(DialogInterface dialog, int which) {
		    		dialog.cancel(); 
		        }
		    });
			
			// Mostramos el dialog
			adb.show();
					
            return true;
		
			case R.id.menuNuevoLugar:
			
			// Llamamos a los servicios (fuentes) de localizacion y conectividad
			final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			final ConnectivityManager conMan = ((ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE));

			// Comprobamos el estado de los servicios (fuentes)
			boolean isWifiEnabled = conMan.getNetworkInfo(
					ConnectivityManager.TYPE_WIFI).isAvailable();
			boolean is3GEnabled = (conMan.getNetworkInfo(
					ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.DISCONNECTED && conMan
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
					.getReason().equals("dataDisabled"));

			// Si estan desconectados llamamos al método "fuentesDeUbicacionApagados"
			if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
					&& (!isWifiEnabled) && (!is3GEnabled)) {

				fuentesDeUbicacionApagados();

			// Si estan conectados creamos el nuevo lugar
			} else {
				
				// Lanzamos un intent para ir a "NuevoLugarActivity"
				Intent i = new Intent();
				i.setClass(MapaLugarActivity.this, NuevoLugarActivity.class);

				// Creamos un objeto criteria para elegir el mejor proveedor de servicios de ubicación
				Criteria criteria = new Criteria();
				LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				
				// Obtenemos el nombre del mejor proveedor (gps o network)
				String provider = locationManager.getBestProvider(criteria,true);

				// Si provider (servicios de ubicación) NO es null iniciamos la localización
				if (provider != null) {
					Location location = mapa.getMyLocation();
					criteria.setAccuracy(Criteria.ACCURACY_FINE);

					// Si tenemos una localización válida inferior a 100 metros obtenemos sus coordenadas
					if ((location != null) && (location.getAccuracy() < 100)
							&& (location.getAccuracy() > 0)) {
						i.putExtra("latitud", location.getLatitude());
						i.putExtra("longitud", location.getLongitude());
						
						// Lanzamos la Activity
						startActivity(i);
						
					// Si la localización NO es válida o es superior a 100 metros mostramos un Toast	
					} else

						Toast.makeText(getApplicationContext(),R.string.toast_error_ubicacion,
								Toast.LENGTH_SHORT).show();
					
				// Si provider (servicios de ubicación) es null llamamos al método "serviciosUbicacionApagados2"
				} else

					serviciosUbicacionApagados2();

				return true;
			}
			default:
			return super.onOptionsItemSelected(item);
		}
	}

	// Definimos el método "fuentesDeUbicacionApagados"
	private void fuentesDeUbicacionApagados() {
		
		// Construimos el dialog
		AlertDialog.Builder adb = new AlertDialog.Builder(this); 
	    adb.setTitle(R.string.titulo_dialog_fuentes_ubicacion);
	    adb.setMessage(R.string.mensaje_dialog_fuentes_ubicacion);
	    
	    // Comportamiento botón "Wi-Fi - 3G"
	    adb.setPositiveButton(R.string.opcion_dialog_WiFi_3G, new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int which) {
	                            
	    		// Lanzamos un Intent para ir a configuración del sistema
	            Intent configuracionProviderIntent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
	            startActivity(configuracionProviderIntent);               
	        }
	    });

	    // Comoportamiento botón "GPS"
	    adb.setNegativeButton(R.string.opcion_dialog_GPS, new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int which) {
	            Intent configuracionProviderIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	            startActivity(configuracionProviderIntent);  
	            	 
	        }
	    });
	    
	    // Mostramos el dialog
	    adb.show();			
	}

	private void serviciosUbicacionApagados2() {
		
		// Construimos el dialog
		AlertDialog.Builder adb = new AlertDialog.Builder(this); 
	    adb.setTitle(R.string.titulo_dialog_servicios_ubicacion2);
	    adb.setMessage(R.string.mensaje_dialog_servicios_ubicacion2); 
	    
	    // Comportamiento botón "Configuración"
	    adb.setPositiveButton(R.string.opcion_dialog_configuracion, new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int which) {
	                            
	    		// Lanzamos un Intent para ir a configuración
	            Intent configuracionProviderIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	            startActivity(configuracionProviderIntent);               
	        }
	    });

	    // Comportamiento botón "Omitir"
	    adb.setNegativeButton(R.string.opcion_dialog_omitir, new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int which) {
	    		dialog.cancel();     	 
	        }
	    });
	    
	    // Mostramos el dialog
	    adb.show();	
	}
}