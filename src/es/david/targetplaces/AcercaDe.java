package es.david.targetplaces;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;

/** La clase debe extender de Dialog */
/**
 * Implementar onClickListener para descartar el dialog al pulsar el botón
 * "Aceptar"
 */
public class AcercaDe extends Dialog implements OnClickListener {
	Button Aceptar;

	public AcercaDe(Context context) {
		super(context);

		/** "Window.FEATURE_NO_TITLE" - Se usa para ocultar el título */
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		/** Enlazamos con el layout */
		setContentView(R.layout.custom_dialog_acercade);
		Aceptar = (Button) findViewById(R.id.btnAceptar);
		Aceptar.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {

		/** AL pulsar el botón aceptar, descartamos el dialog */
		if (v == Aceptar)
			dismiss();
	}
}
