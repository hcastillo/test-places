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
 * Actividad que permite editar un lugar
 * 
 * @author David
 */
public class EditarLugarActivity extends Activity implements OnRatingBarChangeListener {

	// Declaramos los objetos y variables
	protected static final int camara = 0;
	protected static final int galeria = 1;
	private Intent imagenActionIntent = null;
	private static int valoracionAGuardar = 0;
	private ImageView seleccionarImagen;
	public static Bitmap bitmap = null;
	static String imagenAGuardar = null;
	
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
	
	// Capturamos los cambios en la valoración 
	public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
		 
	       EditarLugarActivity.valoracionAGuardar = (int) Math.round(rating);
    }
	 
	// Iniciamos la Activity y enlazamos con el Layout
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_editar_lugar);
		
		// Ocultamos el teclado al iniciar la Activity
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		       
		// Obtenemos el id que recibimos desde MostrarLugarActivity
		Intent i = getIntent();
		long cualHeDeMostrar = i.getLongExtra("id", 1);
		
	    // Accedemos a la BBDD
		LugaresSQLHelper lugarHelper = new LugaresSQLHelper(getBaseContext());
		
		// Recuperamos de la BBDD los datos del lugar cuyo identificador es cualHeDeMostrar
		Lugar lugar = lugarHelper.getLugarById(cualHeDeMostrar);
		
		// Recuperamos el nombre a editar
	    EditText nombre = new EditText(this);
	    nombre = (EditText) findViewById(R.id.nombreEditarLugar);
	    nombre.setText(lugar.getNombre());
		
		// Recuperamos la descripción a editar
		EditText descripcion = new EditText(this);
		descripcion = (EditText) findViewById(R.id.descripcionEditarLugar);
		descripcion.setText(lugar.getDescripcion());
		
		// Recuperamos la imagen a editar (usando la uri)
		ImageView imagen = new ImageView(this);
		imagen = (ImageView) findViewById(R.id.imagenEditarLugar);
		Uri imagenSeleccionada = lugar.getImage();
		String path = getRealPathFromURI(imagenSeleccionada);
		
		// Pasamos 300px de altura (200dp hdpi) como requerimiento de reescalado
		Bitmap resizedBitmap = decodeSampledBitmapFromFile(path, 300);
		imagen.setImageBitmap(resizedBitmap);
		
		// Almacenamos el path de la imagen
		imagenAGuardar = getRealPathFromURI(imagenSeleccionada);
				
		// Mostramos la valoración actual:
		RatingBar ratingbar = (RatingBar) findViewById(R.id.ratingbarEditarLugar);
		ratingbar.setOnRatingBarChangeListener(this);
		ratingbar.setNumStars(5);
		ratingbar.setRating( lugar.getValoracion());
		ratingbar.setStepSize(1);
  	
		// Enlazamos con el botón "Cancelar"
		Button btnCancelar = (Button) findViewById(R.id.btnCancelarEditarLugar);

		// Asignamos comportamiento al botón "Cancelar"
		btnCancelar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				// Limpiamos la variable valoracionAGuardar para evitar falso positivo en la comprobación
				// de datos introducidos al salir
				valoracionAGuardar = 0;
			
				// Finalizamos la Activity
				finish();
				
				// Mostramos un Toast de cancelación
				Toast.makeText(getApplicationContext(), R.string.toast_cancelado, 
						Toast.LENGTH_SHORT).show();			
			}
		});

		// Enlazamos con el botón "Guardar"
		Button btnGuardar = (Button) findViewById(R.id.btnGuardarEditarLugar);

		// Asignamos comportamiento al botón "Guardar"
		btnGuardar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				// Recuperamos el id del lugar a guardar
				Intent i1 = getIntent();
				long cualHeDeGuardar = i1.getLongExtra("id", 0);
				
				// Enlazamos con los campos
				String nombreAGuardar = ((EditText) findViewById(R.id.nombreEditarLugar)).getText().toString().trim();
				EditText descripcionAGuardar = (EditText) findViewById(R.id.descripcionEditarLugar);
				
				// Controlamos que no se quede el campo "Nombre" en blanco
				boolean ok = true;
				
				if (nombreAGuardar.isEmpty()) {
					Toast.makeText(getApplicationContext(), R.string.toast_insertar_nombre, 
							Toast.LENGTH_SHORT).show();
					ok = false;
				}

				if (ok) {
					
					// Llamanos al metodo para crear el thumbnail y pasamos el Id
					String Idthumb = String.valueOf(cualHeDeGuardar +".jpg");
					crearDirectorioYThumbnails(bitmap, Idthumb);
					
					// Accedemos a la BBDD
					LugaresSQLHelper lugarHelper = new LugaresSQLHelper(getBaseContext());
				
					// Actualizamos el lugar
					lugarHelper.actualizarLugar(cualHeDeGuardar, nombreAGuardar, descripcionAGuardar.getText()
							.toString(), imagenAGuardar, valoracionAGuardar);
				
					// Mostramos un toast de lugar editado correctamente
					Toast.makeText(getApplicationContext(),R.string.toast_lugar_editado, 
							Toast.LENGTH_SHORT).show();
				
					// Quitamos esta activity de la pila
					finish();
				}
			}});
		
		// Enlazamos con el campo de selección de imagen
		seleccionarImagen = (ImageView) findViewById(R.id.imagenEditarLugar);
		seleccionarImagen.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				startDialog();
			}
		});
	}
	
	// Definimos el método startDialog
	private void startDialog() {
		
		// Construimos el dialog "Seleccionar imágen"
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setTitle(R.string.titulo_dialog_seleccionar_imagen);
		adb.setMessage(R.string.mensaje_dialog_elegir_opcion);
		
		// Comportamiento botón "Galería"
		adb.setPositiveButton(R.string.opcion_dialog_galeria, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				imagenActionIntent = new Intent(Intent.ACTION_GET_CONTENT, null);
				imagenActionIntent.setType("image/*");
				imagenActionIntent.putExtra("return-data", true);
				startActivityForResult(imagenActionIntent, galeria);
			}
		});
		
		// Comportamiento botón "Cámara"
		adb.setNegativeButton(R.string.opcion_dialog_camara, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				imagenActionIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
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

					BitmapDrawable thumbnail = new BitmapDrawable(getResources(), data.getData().getPath());
					
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
	
	// Guardamos la imagen a guardar para evitar que se pierda al rotar la pantalla
	@Override
	protected void onSaveInstanceState(Bundle outState){
	    super.onSaveInstanceState(outState);
	    outState.putString("Imagen", imagenAGuardar);
	}
	
	// Recuperamos la imagen 
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState){
	    super.onRestoreInstanceState(savedInstanceState);
	    imagenAGuardar = savedInstanceState.getString("Imagen");
	    
		// Pasamos 300px de altura (200dp hdpi) como requerimiento de reescalado
	    Bitmap resizedBitmap = decodeSampledBitmapFromFile(imagenAGuardar, 300);
	    seleccionarImagen.setImageBitmap(resizedBitmap);
	}
	
	// Evitamos salidas accidentales al pulsar el botón "Atrás"
	public void onBackPressed() {

		// Obtenemos el id que recibimos desde MostrarLugarActivity
		Intent i = getIntent();
		long cualHeDeMostrar = i.getLongExtra("id", 1);
		
	    // Accedemos a la BBDD
		LugaresSQLHelper lugarHelper = new LugaresSQLHelper(getBaseContext());
		
		// Recuperamos de la BBDD los datos del lugar cuyo identificador es cualHeDeMostrar
		Lugar lugar = lugarHelper.getLugarById(cualHeDeMostrar);
		
		// Recuperamos el texto introduciodo en nombre y descripción
		String nombreAGuardar = ((EditText) findViewById(R.id.nombreEditarLugar)).getText().toString().trim();
		String descripcionAGuardar = ((EditText) findViewById(R.id.descripcionEditarLugar)).getText().toString().trim();
		
		// Comparamos los datos recuperados de la BBDD con los nuevos datos introducidos
		// Si ha habido cambios...
	    if (!lugar.getNombre().equals(nombreAGuardar) 
	    		|| (!lugar.getDescripcion().equals(descripcionAGuardar)	
	    		|| (!lugar.getImage().toString().equals(imagenAGuardar) 
	    	    || (lugar.getValoracion() != (valoracionAGuardar))))) {
			
		// Construimos el dialog "Atención"
	    AlertDialog.Builder adb = new AlertDialog.Builder(this);
	    adb.setIcon(android.R.drawable.ic_dialog_alert);
	    adb.setTitle(R.string.titulo_dialog_atencion);
        adb.setMessage(R.string.mensaje_dialog_cambios_realizados);
        
        // Comportamiento botón "Si"
        adb.setPositiveButton(R.string.opcion_dialog_si, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface arg0, int arg1) {
            	
            	// Limpiamos la variable valoracionAGuardar para evitar falso positivo en la comprobación
            	// de datos introducidos al salir				
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
        
        // Si no ha habido cambios...
	    }else {
	    	finish();
	    }
	}
}