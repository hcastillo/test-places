package es.david.targetplaces.datos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

/**
 * Adaptador para la base de datos que guarda los elementos de la clase Lugar()
 * en Lugares.db
 */
public class LugaresSQLHelper extends SQLiteOpenHelper {

	// Versión y nombre del fichero donde se guardarán los datos en el dispositivo
	static String dbNombre = "Lugares.db";
	static int dbVersion   = 1;

	// Campos que contendrá la base de datos
	static private String[] camposDb = { 
		
			"_id", 
			"nombre", 
			"descripcion", 
			"imagen", 
			"latitud", 
			"longitud",
			"valoracion"
	};

	// Constructor de la clase
	public LugaresSQLHelper(Context context) {
		super(context, dbNombre, null, dbVersion);
	}

	// Creamos la base de datos si no existía:
	@Override
	public void onCreate(SQLiteDatabase db) {

		if (db.isReadOnly()) {
			db = getWritableDatabase();
		}

		// Sentencia SQL para la creación de una tabla
		db.execSQL("CREATE TABLE lugares ("
				
				+ "  _id integer PRIMARY KEY autoincrement," + "  nombre TEXT, "
				+ "  descripcion TEXT, "
				+ "  imagen TEXT, latitud DOUBLE, longitud DOUBLE,valoracion INTEGER );");
	}
	
	// No está previsto actualizar la tabla de la BBDD
	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		
		/* Al actualizar la BBDD remover la antigua tabla si existe
	    db.execSQL("DROP TABLE IF EXISTS lugares;");
	    onCreate(db);*/
	}

	// Insertamos en la base de datos un nuevo lugar
	public long insertarLugar(Lugar lugar) {
		
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("nombre", lugar.getNombre());
		values.put("descripcion", lugar.getDescripcion());
		values.put("imagen", lugar.getImage().toString());
		values.put("latitud", lugar.getPosicion().latitude);
		values.put("longitud", lugar.getPosicion().longitude);
		values.put("valoracion", lugar.getValoracion());
		return db.insert("lugares", null, values);	 
	}

	// Cambiamos los datos de un lugar dado su id
	public int actualizarLugar(long id, String nombre, String descripcion, String imagen,int valoracion) {

		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues valoresAActualizar = new ContentValues();
		valoresAActualizar.put("nombre", nombre);
		valoresAActualizar.put("descripcion", descripcion);
		valoresAActualizar.put("imagen", imagen);
		valoresAActualizar.put("valoracion", valoracion);	
		return db.update("lugares", valoresAActualizar, "_id=?", new String[] { Long.toString(id) });	
	}

	// Borramos un lugar dado el id:
		public int deleteById(long id) {
			SQLiteDatabase db = this.getWritableDatabase();
			return db.delete("lugares", "_id=?", new String[] { Long.toString(id) });	
		}

	// Borramos todos los lugares:
	public void deleteAll() {
		
	    SQLiteDatabase db = this.getWritableDatabase();
	    db.delete("lugares",null,null);
	    db.close();
	}

	// Recibimos un id y devolvemos ese lugar:
	public Lugar getLugarById(long id) {

		SQLiteDatabase db = this.getWritableDatabase();		
		Cursor cursor = db.query("Lugares", camposDb, "_id=" + id, null, null, null, "nombre ASC");
		cursor.moveToFirst();
		Lugar lugar = new Lugar(cursor.getString(1), cursor.getString(2));
		lugar.setImage(Uri.parse(cursor.getString(3)));
		lugar.setPosicion(cursor.getDouble(4), cursor.getDouble(5));
		lugar.setValoracion(cursor.getInt(6));
		cursor.close();
		return lugar;
	}

	// Devolvemos un cursor con todos los lugares:
	public Cursor getAllLugares(boolean OrdenaPorNombre) {
		SQLiteDatabase db = this.getWritableDatabase();		
		String orden;
		if (OrdenaPorNombre)
			orden = "nombre ASC";
		else 
			orden = "_id DESC";
				
		Cursor cursor = db.query("Lugares", camposDb, null, null, null, null, orden );
		return cursor;
	}
}