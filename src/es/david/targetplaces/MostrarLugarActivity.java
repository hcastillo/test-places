package es.david.targetplaces;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import es.david.targetplaces.datos.Lugar;
import es.david.targetplaces.datos.LugaresSQLHelper;

/**
 * Actividad que permite mostrar un lugar
 * 
 * @author David
 */
public class MostrarLugarActivity extends Activity  {
	
	// Declaramos los objetos y variables
    long cualHeDeMostrar=0;
	
	/**
	 * Obtenemos la ruta desde un content: //...
	 * 
	 * @param contentURI: URI en formato content: //... a transformar en String
	 * 
	 * @return path: Devuelve un String con el formato file://sdcard/...
	 */
	private String getRealPathFromURI(Uri contentURI) {
		contentURI = Uri.parse(contentURI.toString());
		String path;
		Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
		
		if (cursor == null) {
			path = contentURI.getPath();
		} else {
			cursor.moveToFirst();
			int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
			path = cursor.getString(index);
		}

		return path;
	}

	/**
	 * Reescalamos la imagen para no desbordar la memoria
	 * 
	 * @param path: Ruta de la imagen
	 * 
	 * @param reqHeight: Requerimos la altura
	 * 
	 * @return: Devuelve la ruta y las opciones
	 */
	public static Bitmap decodeSampledBitmapFromFile(String path, int reqHeight) {

		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		options.inSampleSize = calculateInSampleSize(options, reqHeight);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(path, options);
	}

	/**
	 * Factor de reescalado
	 * 
	 * @param options: Aplicamos las opciones de reescalado
	 * 
	 * @param reqHeight: El tamaño de salida requerido, en este caso de la altura
	 * 
	 * @return: Devuelve el factor de reescalado a aplicar
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqHeight) {
		final int height = options.outHeight;
		int inSampleSize = 1;
		
		if (height > reqHeight) {
			inSampleSize = Math.round((float) height / (float) reqHeight);
		}
		
		return inSampleSize + 2;
	}
	
	// Iniciamos la Activity y enlazamos con el Layout
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mostrar_lugar);

		// Enlazamos con los botones en el layout
		Button btnIrAlMapa = (Button) findViewById(R.id.btnVerEnElMapa);
		Button btnCoordenadas = (Button) findViewById(R.id.btnCoordenadas);

		// Llamamos al método que permite escuchar los eventos de click en el botón
		btnIrAlMapa.setOnClickListener(new OnClickListener() {

			// Asignamos comportamiento al botón "Ver en el mapa"
			@Override
			public void onClick(View v) {
			
				// Lanzamos un intent para ir a "MapaLugaresActivity"
				Intent i = new Intent();
			    //cualHeDeMostrar = i.getLongExtra("id", 0);
				i.setClass(MostrarLugarActivity.this, MapaLugarActivity.class);
				
				// Pasamos el id del lugar a mostrar en el mapa
			    i.putExtra("soloMuestraEste",cualHeDeMostrar );
				startActivity(i);
			}
		});
		
		// Llamamos al método que permite escuchar los eventos de click en el botón
		btnCoordenadas.setOnClickListener(new OnClickListener() {

			// Asignamos comportamiento al botón "Coordenadas"
			@Override
			public void onClick(View v) {
				
				// Recuperamos el id del lugar a mostrar
				Intent i = getIntent();
			    cualHeDeMostrar = i.getLongExtra("id", 0);
			 
			    // Accedemos a la BBDD
				LugaresSQLHelper lugarHelper = new LugaresSQLHelper(getBaseContext());
				
				// Recuperamos de la BBDD los datos del lugar cuyo identificador es cualHeDeMostrar
				final Lugar lugar = lugarHelper.getLugarById(cualHeDeMostrar);

				// Construimos el dialog y sobrecargamos sus métodos
				new AlertDialog.Builder(MostrarLugarActivity.this)
				
						.setTitle(R.string.titulo_dialog_coordenadas)
						.setMessage (getText(R.string.mensaje_dialog_latitud).toString() + lugar.getPosicion().latitude + 
								getText(R.string.mensaje_dialog_longitud)+ lugar.getPosicion().longitude)

						// Comportamiento del botón "Aceptar"
						.setNeutralButton(R.string.opcion_dialog_aceptar, null)

						// Mostramos el dialog
						.show();
			}
		});	
	}
	
	// Sobreescibimos el método "onResume"
	// De este modo podemos actualizar los campos al recuperar el foco despues de editar
	@Override
	protected void onResume() {

		// Recuperamos el id del lugar a mostrar
		Intent i = getIntent();
	    cualHeDeMostrar = i.getLongExtra("id", 0);
	 
	    // Accedemos a la BBDD
		LugaresSQLHelper lugarHelper = new LugaresSQLHelper(getBaseContext());
		
		// Recuperamos de la BBDD los datos del lugar cuyo identificador es cualHeDeMostrar
		final Lugar lugar = lugarHelper.getLugarById(cualHeDeMostrar);

		// Modificamos los campos del Textview con esos datos
		// Mostramos el nombre
		TextView nombre = new TextView(this);
		nombre = (TextView) findViewById(R.id.nombreMostrarLugar);
		nombre.setText(lugar.getNombre());
		nombre.setSelected(true);
		
		// Mostramos la descripción
		TextView descripcion = new TextView(this);
		descripcion = (TextView) findViewById(R.id.descripcionMostrarLugar);
		descripcion.setText(lugar.getDescripcion());
		descripcion.setMovementMethod(new ScrollingMovementMethod());
		
		// Mostramos la valoracion
	    RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingbarMostrarLugar);
	    ratingBar.setRating( lugar.getValoracion());
	    ratingBar.setNumStars(5);
	    ratingBar.setStepSize(1);
	    ratingBar.setIsIndicator(true);

		// Mostramos la imagen (usando la uri)
		ImageView imagen = new ImageView(this);
		imagen = (ImageView) findViewById(R.id.imagenMostrarLugar);

		// Obtenemos la ruta de la imagen seleccionada
		Uri selectedImage = lugar.getImage();
		String rutaGaleria = getRealPathFromURI(selectedImage);
		
		// Pasamos 300px de altura (200dp hdpi) como requerimiento de reescalado
		Bitmap resizedBitmap = decodeSampledBitmapFromFile(rutaGaleria, 300);
		imagen.setImageBitmap(resizedBitmap);
	    		
		super.onResume();
	}
	
	// Enlazamos con el menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.mostrar_lugar, menu);
		return true;
	}

	// Añadimos las opciones del menú
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		
		// Editar
		case R.id.menuEditarLugar:
		
			Intent myIntent = getIntent();
			long cualHeDeMostrar = myIntent.getLongExtra("id", 0);
		
			// Lanzamos un Intent para irnos a "EditarLugarActivity"
			Intent intent = new Intent(this, EditarLugarActivity.class);
			intent.putExtra( "id",cualHeDeMostrar );
			this.startActivity(intent);
			return true;
			
		// Eliminar
		case R.id.menuEliminarLugar:
			
			// Accedemos a la base de datos
			final LugaresSQLHelper lugarHelper = new LugaresSQLHelper(getBaseContext());

			// Construimos el dialog de confirmación de borrado
			// y sobrecargamos sus métodos
			new AlertDialog.Builder(MostrarLugarActivity.this)
			
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.titulo_dialog_eliminar_lugar)
				.setMessage(R.string.mensaje_dialog_eliminar_lugar)
			
				// Comportamiento botón "Si"
				.setPositiveButton(R.string.opcion_dialog_si, new DialogInterface.OnClickListener() {
					    	   
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent myIntent = getIntent();
						long cualHeDeBorrar = myIntent.getLongExtra("id", 0);
						lugarHelper.deleteById(cualHeDeBorrar);
					
						Toast.makeText(getApplicationContext(), R.string.toast_lugar_eliminado,
								Toast.LENGTH_SHORT).show();

						// Lanzamos un Intent para irnos a "ListaLugarActivity"
						/*Intent i = new Intent();
						i.setClass(MostrarLugarActivity.this, ListaLugarActivity.class);						
						startActivity(i);*/
						finish();
					
					}
				})
			   
				// Comportamiento botón "No"	
				.setNegativeButton(R.string.opcion_dialog_no, new DialogInterface.OnClickListener() {
			    	public void onClick(DialogInterface dialog, int which) {
			    		dialog.cancel();           	 
			        }
			    })

				// Mostramos el dialog
				.show();
			
			return true;
			default:
			return super.onOptionsItemSelected(item);	
		}
	}
}