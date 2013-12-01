package es.david.targetplaces;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * Actividad que permite editar un lugar
 * 
 * @author David
 */
public class MainActivity extends Activity {

	// Iniciamos la Activity y enlazamos con el Layout
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Localizamos los botones en el layout
		Button btnLista = (Button) findViewById(R.id.btnLista);
		Button btnMapa = (Button) findViewById(R.id.btnMapa);

		// Asignamos el comportamiento al botón "Lista"
		btnLista.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				// Lanzamos un intent para irnos a "ListaLugarActivity"
				Intent i = new Intent();
				i.setClass(MainActivity.this, ListaLugarActivity.class);
				startActivity(i);
			}
		});
		
		// Asignamos el comportamiento al botón "Mapa"
		btnMapa.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				// Lanzamos un intent para irnos a "MapaLugarActivity"
				Intent i = new Intent();
				i.setClass(MainActivity.this, MapaLugarActivity.class);
				startActivity(i);
			}
		});
	}

	// Enlazamos con el menú
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
	    getMenuInflater().inflate(R.menu.main, menu);
	    return true;
	}

	// Añadimos las opciones del menú
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.menuAcercaDe:
	        	
	        	AcercaDe customizeDialog = new AcercaDe(this);
	        	customizeDialog.show();
	            return true;

	        case R.id.menuSalir:
	        	Intent i = new Intent(Intent.ACTION_MAIN);
	        	i.addCategory(Intent.CATEGORY_HOME);
	        	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        	startActivity(i);
				finish();

	            return true;	     
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	// Volvemos a Home al apretar el botón atrás
	public void onBackPressed() {

		Intent i = new Intent(Intent.ACTION_MAIN);
		i.addCategory(Intent.CATEGORY_HOME);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
		startActivity(i); 
		finish();
	}
}