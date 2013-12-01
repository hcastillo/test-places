package es.david.targetplaces;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Clase que permite crear un adapter personalizado para la ListView
 * 
 * @author David
 *
 */
public class ListaAdapter extends ArrayAdapter<ObjetoParaListaAdapter>{
	
	// Declaramos los objetos y variables 
    private final Context context;
    private final ObjetoParaListaAdapter[] values;
    View convertView;
      
	// Constructor de la clase
	public ListaAdapter(Context context, ObjetoParaListaAdapter[] values) {
		super(context, R.layout.custom_listview, values);
		this.context = context;
		this.values = values;
	}

	// Declaramos un objeto ViewHolder que se encargará de reciclar cada elemento de la lista
	// para evitar usar findViewById reiteradamente optimizando así el scroll
	static class ViewHolder {
		TextView nombre;
		TextView descripcion;
		ImageView imagen;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {

		// Patrón ViewHolder
		ViewHolder holder = null;

		if (convertView == null) {

			holder = new ViewHolder();

			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.custom_listview, null);

			// Elemento nombre
			holder.nombre = (TextView) convertView.findViewById(R.id.nombreListaLugar);

			// Elemento descripción
			holder.descripcion = (TextView) convertView.findViewById(R.id.descripcionListaLugar);

			// Elemento imágen
			holder.imagen = (ImageView) convertView.findViewById(R.id.imagenListaLugar);

			convertView.setTag(holder);

		} else {

			holder = (ViewHolder) convertView.getTag();
		}

		holder.nombre.setText(values[position].nombre);
		holder.descripcion.setText(values[position].descripcion);

		if (values[position].bitmap == null) {

			values[position].bitmap = BitmapFactory.decodeFile("/sdcard/DirName/"
					+ String.valueOf(values[position].identificador) + ".jpg");
		}
		
		holder.imagen.setImageBitmap(values[position].bitmap);
			
		// Capturamos el evento click de cada elemento de la lista
		convertView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// Lanzamos un intent para irnos a "MostrarLugarActivity"
				Intent i = new Intent(v.getContext(),MostrarLugarActivity.class);
				i.putExtra("id", (long) values[position].identificador);
				v.getContext().startActivity(i);
			}
		});

		return convertView;
	}
}