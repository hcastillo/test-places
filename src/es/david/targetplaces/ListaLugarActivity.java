package es.david.targetplaces;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;
import es.david.targetplaces.datos.LugaresSQLHelper;

/**
 * Actividad que permite listar los lugares
 * 
 * @author David
 */
public class ListaLugarActivity extends Activity implements OnClickListener {
  
	// Declaramos los objetos y variables
	public static final String PREFS_NAME = "MisPreferencias";
	public ObjetoParaListaAdapter[] ArrayTodosLugares;
	private Cursor cursor;
	boolean ordenandoPorNombre;
	int posicionScroll = 0;
	
	// Iniciamos la Activity y enlazamos con el Layout
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lista_lugar);
		
	    // Leemos las preferencias
	    SharedPreferences preferencias = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	    ordenandoPorNombre = preferencias.getBoolean("ordenarPor", false); 	    	        
	}
	
	// Guardamos la posicion del scroll al pausar la actividad
	public void onPause() {
				
		ListView lv = (ListView) findViewById(R.id.lista);	    
	    this.posicionScroll = lv.getFirstVisiblePosition();
	    super.onPause();	    
	}

	// Sobreescibimos el método "onResume"
	// De esta forma refrescamos los campos al recuperar el foco
	@Override
	protected void onResume() {
	
		 // Accedemos a la BBDD
 		LugaresSQLHelper dbHelper = new LugaresSQLHelper(ListaLugarActivity.this);

 		// Creamos el cursor
 		cursor = dbHelper.getAllLugares(ordenandoPorNombre);
 		
 		// Recorremos todos los lugares
 		ArrayTodosLugares = new ObjetoParaListaAdapter[cursor.getCount()];
 		int posicionArray = 0;
 		cursor.moveToFirst();
 		while (!cursor.isAfterLast()) {
				
			ArrayTodosLugares[posicionArray++] = new ObjetoParaListaAdapter(
					cursor.getString(1), cursor.getString(2), cursor.getInt(0),cursor.getString(3));
			cursor.moveToNext();
 		}
 		
 		// Si hay lugares enlazamos con el ListView
 		if (posicionArray >0) {
 			ListView lv = (ListView) findViewById(R.id.lista);
 			
 			ListaAdapter adapter = new ListaAdapter(this, ArrayTodosLugares);
 			lv.setAdapter(adapter);
 			
 			// Recuperamos la posicion del scroll
 			lv.setSelectionFromTop(this.posicionScroll,0);
 					
 		// Si no hay lugares mostramos un Toast
 		} else 
 			Toast.makeText(this, R.string.toast_lista_vacia,
 					Toast.LENGTH_SHORT).show();
   			
		super.onResume();	
	}
	
	// Enlazamos y añadimos funcionalidad al menú
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Accedemos a la BBDD
		LugaresSQLHelper dbHelper = new LugaresSQLHelper(
				ListaLugarActivity.this);

		// Creamos el cursor
		cursor = dbHelper.getAllLugares(ordenandoPorNombre);

		// Recorremos todos los lugares
		ArrayTodosLugares = new ObjetoParaListaAdapter[cursor.getCount()];
		int posicionArray = 0;
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			
			ArrayTodosLugares[posicionArray++] = new ObjetoParaListaAdapter(
					cursor.getString(1), cursor.getString(2), cursor.getInt(0), cursor.getString(3));
			cursor.moveToNext();
		}

		// Si hay lugares hacemos visible el menú
		if (posicionArray > 0) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.lista_lugar, menu);

		// Si no hay lugares ocultamos el menú
		} else 			
			menu.setGroupVisible(posicionArray, false);
		
		return true;
	}
	
	// Añadimos las opciones del menú
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menuOrdenarLista:

			// Construimos el dialog
			AlertDialog.Builder adb1 = new AlertDialog.Builder(this);

			adb1.setTitle(R.string.titulo_dialog_ordenar);

			// Array de opciones
			String items[] = getResources().getStringArray(
					R.array.opcion_dialog_tipo_ordenar);

			if (ordenandoPorNombre)
				adb1.setSingleChoiceItems(items, 0, this);
			else
				adb1.setSingleChoiceItems(items, 1, this);

			// Comportamiento del botón "Cancelar"
			adb1.setNeutralButton(R.string.opcion_cancelar, null);

			// Mostramos el dialog
			adb1.show();

			return true;
		
		case R.id.menuEliminarLista:
			
			// Accedemos a la base de datos
			final LugaresSQLHelper lugarHelper = new LugaresSQLHelper(getBaseContext());

			// Construimos el dialog "Eliminar Lista"
		    AlertDialog.Builder adb2 = new AlertDialog.Builder(this);
			
			adb2.setIcon(android.R.drawable.ic_dialog_alert);
			adb2.setTitle(R.string.titulo_dialog_eliminar_lista);
			adb2.setMessage(R.string.mensaje_dialog_eliminar_lista);
			
			// Comportamiento botón "Si"
			adb2.setPositiveButton(R.string.opcion_dialog_si, new DialogInterface.OnClickListener() {
					    	   
				@Override
				public void onClick(DialogInterface dialog, int which) {

					lugarHelper.deleteAll();
					finish();
					startActivity(getIntent());
					Toast.makeText(getApplicationContext(), R.string.toast_eliminar_lista, Toast.LENGTH_SHORT).show();
				}
			});
			     
			// Comportamiento botón "No"
	        adb2.setNegativeButton(R.string.opcion_dialog_no, new DialogInterface.OnClickListener() {
		    	public void onClick(DialogInterface dialog, int which) {
		    		dialog.cancel(); 
		        }
		    });
	        
			// Mostramos el dialog
			adb2.show();
			
		return true;
		default:
		return ordenandoPorNombre;
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int n) {
		if (n == 0) {
			// ordenar alfabéticamente
			ordenandoPorNombre = true;

		} else {
			// ordenar por fecha
			ordenandoPorNombre = false;
		}
		
		 // Guardamos las preferencias
        SharedPreferences preferencias = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();
        editor.putBoolean("ordenarPor", ordenandoPorNombre);
        editor.commit();

        // Accedemos a la BBDD
		LugaresSQLHelper dbHelper = new LugaresSQLHelper(ListaLugarActivity.this);

		cursor = dbHelper.getAllLugares(ordenandoPorNombre);
		
		// Recorremos todos los lugares
		ObjetoParaListaAdapter[] ArrayTodosLugares = new ObjetoParaListaAdapter[cursor
				.getCount()];
		int posicionArray = 0;
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			 
			ArrayTodosLugares[posicionArray++] = new ObjetoParaListaAdapter(
					cursor.getString(1), cursor.getString(2), cursor.getInt(0), cursor.getString(3));
			cursor.moveToNext();
		}
		
		// Enlazamos con la vista
		ListView lv = (ListView) findViewById(R.id.lista);
		ListaAdapter adapter = new ListaAdapter(this,ArrayTodosLugares);
		lv.setAdapter(adapter);
		
		// Cerramos el dialog al elegir una opción
		dialog.cancel(); 
	}	
}