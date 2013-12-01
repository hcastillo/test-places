package es.david.targetplaces;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.Toast;
import es.david.targetplaces.datos.Lugar;
import es.david.targetplaces.datos.LugaresSQLHelper;

/**
 * Actividad que permite crear un lugar
 * 
 * @author David
 */
public class NuevoLugarActivity extends Activity implements OnRatingBarChangeListener {
	
	// Declaramos los objetos y variables
	protected static final int camara = 0;
	protected static final int galeria = 1;
	public static Bitmap bitmap = null;
	private static int valoracionAGuardar = 0;
	private Intent imagenActionIntent = null;
	private ImageView seleccionarImagen;
	static String imagenAGuardar = null;	
	double longitud;
	double latitud;

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
	 * Obtenemos Bitmap de una imagen dada la ruta
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
	 * Funcion auxiliar usada por decodeSampledBitmapFromFile
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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nuevo_lugar);
				
		// Ocultamos el teclado al iniciar la Activity
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		
		// Recibimos y guardamos la longitud y latitud que nos han pasado en el Intent
		Intent i = getIntent();
		longitud = i.getDoubleExtra("longitud", 0);
		latitud = i.getDoubleExtra("latitud", 0);
	
		// Localizamos los botones en el layout
		Button btnGuardar = (Button) findViewById(R.id.btnGuardarNuevoLugar);
		Button btnCancelar = (Button) findViewById(R.id.btnCancelarNuevoLugar);
		
		// Asignamos el comportamiento al botón "Guardar"		
		btnGuardar.setOnClickListener(new OnClickListener() {
					
			@Override
			public void onClick(View v) {
				
				// Almacenamos el texto introducido en Nombre
				EditText nuevoNombre = (EditText) findViewById(R.id.nombreNuevoLugar);
				String nombre = nuevoNombre.getText().toString().trim();

				// Almacenamos el texto introducido en Descripcion
				EditText nuevaDescripcion = (EditText) findViewById(R.id.descripcionNuevoLugar);
				String descripcion = nuevaDescripcion.getText().toString().trim();

				// Inicializamos la variable con null para evitar un warning mas abajo:
				Uri imagen = null;
				
				// Controlamos que no se queden los siguientes campos en blanco
				boolean ok = true;
				
				// Campo nombre
				if (nombre.isEmpty()) {
					Toast.makeText(getApplicationContext(),R.string.toast_insertar_nombre,
							Toast.LENGTH_SHORT).show();
					ok = false;
				
				// Campo imágen
				} else if (imagenAGuardar == null) {
					Toast.makeText(getApplicationContext(),R.string.toast_insertar_imagen,
							Toast.LENGTH_SHORT).show();
					ok = false;
					
				} else 
			        // Almacenamos el string de la url de la imagen en formato file://sdcard... 
					imagen = Uri.parse(imagenAGuardar);
							
				if (ok) {
					
					// Guardamos en la BBDD
					LugaresSQLHelper adapter = new LugaresSQLHelper(getApplicationContext());
					Lugar nuevoLugarAGuardar = new Lugar(nombre, descripcion,imagen);
					nuevoLugarAGuardar.setPosicion(latitud, longitud);
					nuevoLugarAGuardar.setValoracion(valoracionAGuardar);
					
					// Avisamos en caso de problemas al guardar				
					long elementoInsertado = adapter.insertarLugar(nuevoLugarAGuardar);
						
					if ( elementoInsertado == -1)
						Toast.makeText(getApplicationContext(),R.string.toast_error_crear_lugar, 
								Toast.LENGTH_SHORT).show();
			        
					else {
					
						// Llamanos al metodo para crear el thumbnail y pasamos el Id
						String Idthumb = String.valueOf(elementoInsertado +".jpg");
						crearDirectorioYThumbnails(bitmap, Idthumb);
						
					    // para que si crean una nuevo lugar sin valoración, no este en valoracionAGuardar el valor de 
						// la anterior valoración guardada, "limpiamos" esta variable:
						valoracionAGuardar = 0;

						// Lanzamos un Intent para irnos a MostrarLugarActivity
						Intent i = new Intent();
						i.setClass(NuevoLugarActivity.this, MostrarLugarActivity.class);
						
						// Pasamos el id del lugar a mostrar en el mapa
						i.putExtra("id",elementoInsertado);
						startActivity(i);
						finish();
						
						Toast.makeText(getApplicationContext(),R.string.toast_lugar_creado,
								Toast.LENGTH_SHORT).show();	
					}
				}
			}
		});
		
		// Asignamos el comportamiento al botón "Cancelar"
		btnCancelar.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				// Limpiamos la variable imagenAGuardar y valoracionAGuardar para evitar falso positivo en la comprobación
				// de datos introducidos al salir
				imagenAGuardar = null;
				valoracionAGuardar = 0;
			
				// Finalizamos la Activity
				finish();
				
				// Mostramos un Toast de cancelación
				Toast.makeText(getApplicationContext(), R.string.toast_cancelado, 
						Toast.LENGTH_SHORT).show();			
			}
		});

		// Enlazamos con el campo de selección de imagen
		seleccionarImagen = (ImageView) findViewById(R.id.imagenNuevoLugar);
		seleccionarImagen.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startDialog();
			}

		});		

		// Enlazamos con la valoración
		RatingBar ratingbar = (RatingBar) findViewById(R.id.ratingbarNuevoLugar);
	    ratingbar.setOnRatingBarChangeListener(this);
	    ratingbar.setNumStars(5);
	    ratingbar.setStepSize(1);
	}

	// Construimos el dialog "Seleccionar imágen"
	private void startDialog() {
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setTitle(R.string.titulo_dialog_seleccionar_imagen);
		adb.setMessage(R.string.mensaje_dialog_elegir_opcion);

		// Comportamiento botón "Galería"
		adb.setPositiveButton(R.string.opcion_dialog_galeria, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				imagenActionIntent = new Intent (Intent.ACTION_GET_CONTENT, null);
				imagenActionIntent.setType("image/*");
				imagenActionIntent.putExtra("return-data", true);
				startActivityForResult(imagenActionIntent, galeria);
			}
		});

		// Comportamiento botón "Cámara"
		adb.setNegativeButton("Camara", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				imagenActionIntent = new Intent (android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(imagenActionIntent, camara);
			}
		});
		
		// Mostramos el dialog
		adb.show();
	}

	// Capturamos el comportamiento para seleccionar imagen desde la galería o la cámara
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		// Desde la galería
		if (requestCode == galeria) {
			if (resultCode == RESULT_OK) {
				if (data != null) {

					Uri imagenSeleccionada = data.getData();
					imagenAGuardar = getRealPathFromURI(imagenSeleccionada);
					
					// Pasamos 300px de altura (200dp hdpi) como requerimiento de reescalado
					Bitmap resizedBitmap = decodeSampledBitmapFromFile(imagenAGuardar, 300);
					BitmapDrawable bmpDrawable = null;
					Cursor cursor = getContentResolver().query(data.getData(), null, null, null, null);
					
					if (cursor != null) {

						cursor.moveToFirst();

						int idx = cursor.getColumnIndex(ImageColumns.DATA);
						String fileSrc = cursor.getString(idx);
						
						bitmap = BitmapFactory.decodeFile(fileSrc); 
						seleccionarImagen.setImageBitmap(resizedBitmap);
									
					} else {
						bmpDrawable = new BitmapDrawable(getResources(), data.getData().getPath());
						seleccionarImagen.setImageDrawable(bmpDrawable);
					}
			    } 
		    }
			
		// Desde la cámara
		} else if (requestCode == camara) {
			if (resultCode == RESULT_OK) {
				Uri imagenSeleccionada = data.getData();
				imagenAGuardar = getRealPathFromURI(imagenSeleccionada);
				
				// Pasamos 300px de altura (200dp hdpi) como requerimiento de reescalado
				Bitmap resizedBitmap = decodeSampledBitmapFromFile(imagenAGuardar, 300);
				if (data.hasExtra("data")) {

					bitmap = (Bitmap) data.getExtras().get("data");
					seleccionarImagen.setImageBitmap(resizedBitmap);
					
				} else if (data.getExtras() == null) {

					BitmapDrawable thumbnail = new BitmapDrawable(
							getResources(), data.getData().getPath());

					// Actualizamos la imagen con el nuevo Drawable
					seleccionarImagen.setImageDrawable(thumbnail);
				}
			}
		}
	}
	
	// Crear directorio y Thumbnails
	private void crearDirectorioYThumbnails(Bitmap bitmap, String Idthumb) {

		File direct = new File(Environment.getExternalStorageDirectory() + "/DirName");

		if (!direct.exists()) {
			File wallpaperDirectory = new File("/sdcard/DirName/");
			wallpaperDirectory.mkdirs();
		}

		File file = new File(new File("/sdcard/DirName/"), Idthumb);

		if (file.exists())
			file.delete();

		try {

			bitmap = ThumbnailUtils.extractThumbnail(bitmap, 112, 112);
			FileOutputStream out = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Escuchamos los cambios en la valoración
	@Override
	public void onRatingChanged(RatingBar ratingBar, float rating,
			boolean fromUser) {
		valoracionAGuardar = (int) Math.round(rating);

	}
	
	// Salvamos la instancia de la activity para no perder la imágen
	// al rotar la pantalla
	@Override
	protected void onSaveInstanceState(Bundle outState){
	    super.onSaveInstanceState(outState);
	    outState.putString("Imagen", imagenAGuardar);
	}
	
	// Recuperamos la imágen al restaurar la instancia
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState){
	    super.onRestoreInstanceState(savedInstanceState);
	    if (imagenAGuardar!=null) {
	    imagenAGuardar = savedInstanceState.getString("Imagen");
	    
		// Pasamos 300px de altura (200dp hdpi) como requerimiento de reescalado
	    Bitmap resizedBitmap = decodeSampledBitmapFromFile(imagenAGuardar, 300);
		seleccionarImagen.setImageBitmap(resizedBitmap);
	    }
	}
	
	// Evitamos salidas accidentales al pulsar el botón "Atrás"
	public void onBackPressed() {
		
		// Recuperamos el texto introducido en "Nombre"
		EditText nuevoNombre = (EditText) findViewById(R.id.nombreNuevoLugar);
		String nombre = nuevoNombre.getText().toString().trim();

		// Recuperamos el texto introducido en "Descripcion"
		EditText nuevaDescripcion = (EditText) findViewById(R.id.descripcionNuevoLugar);
		String descripcion = nuevaDescripcion.getText().toString().trim();
		
		// Comprobamos si se han introducido datos
		// Si se han introducido datos...
		if (!nombre.isEmpty() 
				|| (!descripcion.isEmpty() 
				|| (imagenAGuardar != null) 
				|| (valoracionAGuardar >0))) {

		// Construimos el dialog "Atención"
	    AlertDialog.Builder adb = new AlertDialog.Builder(this);
	    adb.setIcon(android.R.drawable.ic_dialog_alert);
	    adb.setTitle(R.string.titulo_dialog_atencion);
        adb.setMessage(R.string.mensaje_dialog_datos_introducidos); 
        
        // Comportamiento botón "Si"
        adb.setPositiveButton(R.string.opcion_dialog_si, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface arg0, int arg1) {
            	
            	// Limpiamos la variable imagenAGuardar y valoracionAGuardar para evitar falso positivo en la comprobación
            	// de datos introducidos al salir
				imagenAGuardar = null;
				valoracionAGuardar = 0;
				
				// Finalizamos la Activity
				finish();
            }
        });
        
        // Comportamiento botón "No"
        adb.setNegativeButton(R.string.opcion_dialog_no, new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int which) {
	    		dialog.cancel(); 
	        }
	    });
        
        // Mostramos el dialog
        adb.show();
        
        // Si no se han introducido datos...
		} else {
			finish();
		}
	}
}