
package de.graphplot.activities;

import de.graphplot.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity
{
	private Intent intent;
	private EditText inputEquation, samplingRate, lowerBound, upperBound;
	
	private static String inputEquation_static;
	private static double lowerBound_static=-1, upperBound_static=-1;
	static int samplingRate_static=-1;
    
	
	@Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intent = new Intent(this, PlotActivity.class);
        inputEquation  = (EditText)(findViewById(R.id.input_equation));
        samplingRate = (EditText)(findViewById(R.id.sampleRate));
        lowerBound = (EditText)(findViewById(R.id.lower_limit));
        upperBound = (EditText)(findViewById(R.id.upper_limit));
        
        if(samplingRate_static > 0) samplingRate.setText(""+samplingRate_static);
        if(lowerBound_static > -1) lowerBound.setText(""+lowerBound_static);
        if(upperBound_static > -1) upperBound.setText(""+upperBound_static);
    }
    
    @Override
    protected void onResume(){
    	super.onResume();
    	if(inputEquation_static != null && inputEquation != null){
    		if(inputEquation_static.contains("pi")) inputEquation_static = inputEquation_static.replace("pi", "\u03c0"); // Ersetze pi wieder mit dem griech. Zeichen für die Anzeige
    		inputEquation.setText(inputEquation_static);
    	}
    }
    
     
    public void startPlot(View view){
		if(inputEquation.getText().toString().equalsIgnoreCase("")){ // Fall 1: Nichts wurde für die Abtastfrequenz eingegeben 
			Toast.makeText(this, getResources().getString(R.string.insertFunction), Toast.LENGTH_SHORT).show();
			return;
		}else if(!inputEquation.getText().toString().contains("t")){
			Toast.makeText(this, getResources().getString(R.string.missT), Toast.LENGTH_SHORT).show();
			return;
		}else if(samplingRate.getText().toString().equalsIgnoreCase("")){ // Fall 2: Nichts wurde für die Abtastfrequenz eingegeben
			Toast.makeText(this, getResources().getString(R.string.missValueForSampleRate), Toast.LENGTH_SHORT).show();
			return;
		}

		
		double lowerBound_Double, upperBound_Double;
    	if(!lowerBound.getText().toString().equalsIgnoreCase("") && !upperBound.getText().toString().equalsIgnoreCase("")){
    		String lowerBoundString = lowerBound.getText().toString();
    		String upperBoundString = upperBound.getText().toString();
    		if(lowerBoundString.contains(",")) lowerBoundString = lowerBoundString.replace(',', '.'); // Ersetze , durch . (untere Grenze)
    		if(upperBoundString.contains(",")) upperBoundString = upperBoundString.replace(',', '.'); // Ersetze , durch . (obere Grenze)
    		lowerBound_Double = Double.parseDouble(lowerBoundString);
    		upperBound_Double = Double.parseDouble(upperBoundString);
    	}else{ // Fall 3: Nichts wurde für die Grenzen eingeben
    		Toast.makeText(this, getResources().getString(R.string.bothLimitsHaveToBeThere), Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
    	if(lowerBound_Double == upperBound_Double){ //Fall 4: untere Grenze ist gleich mit oberer Grenze 
    		Toast.makeText(this, getResources().getString(R.string.lowerLimitHasToBeLowerThanTheUpper), Toast.LENGTH_SHORT).show();
    	}else if(lowerBound_Double > upperBound_Double){ //Fall 5: untere Grenze ist größer als obere Grenze
    		Toast.makeText(this, getResources().getString(R.string.lowerLimitHasToBeLowerThanTheUpper), Toast.LENGTH_SHORT).show();
    	}else{
    		String inputEquationString = inputEquation.getText().toString();
    		String samplingRateString = samplingRate.getText().toString();
    		if(inputEquationString.contains(",")) inputEquationString = inputEquationString.replace(',', '.'); // Ersetze , durch . (Funktion)
    		if(samplingRateString.contains(",")) samplingRateString = samplingRateString.replace(',', '.'); // Ersetze , durch . (Abtastrate)
    		if(inputEquationString.contains("\u03c0") || inputEquationString.contains("t")){
    			inputEquationString = insertMultiplicationSigns(inputEquationString);
    		}
    		if(inputEquationString.contains("\u03c0")) inputEquationString = inputEquationString.replace("\u03c0", "pi"); // Ersetze \u03c0 durch pi zum parsen der Formel
    		intent.putExtra("inputEquation", inputEquationString);
    		intent.putExtra("samplingRate", Integer.valueOf(samplingRateString));
    		intent.putExtra("lowerBound", lowerBound_Double);
    		intent.putExtra("upperBound", upperBound_Double);

    		inputEquation_static = inputEquationString;
    		samplingRate_static = Integer.valueOf(samplingRateString);
    		lowerBound_static = lowerBound_Double;
    		upperBound_static = upperBound_Double;
    		startActivity(intent);
    	}
    }

    
    public void onButtonClicked(View view){
    	int id = view.getId();
    	EditText e = ((EditText)findViewById(R.id.input_equation));
    	String text = "";
		switch (id) {
		case R.id.btn_sin:
			text = "sin(";
			break;
		case R.id.btn_cos:
			text = "cos(";
			break;
		case R.id.btn_tan:
			text = "tan(";
			break;
		case R.id.btn_exp:
			text = "exp(";
			break;
		case R.id.btn_ln:
			text = "ln(";
			break;
		case R.id.btn_t:
			text = "t";
			break;
		case R.id.btn_Pi:
			text = "\u03c0";
			break;
		case R.id.btn_openBrackets:
			text = "(";
			break;
		case R.id.btn_closeBrackets:
			text = ")";
			break;
		case R.id.btn_exponent:
			text = "^";
			break;
		}
    	e.getText().insert(e.getSelectionStart(), text);
    	
    	if(e != null && e.getText().length() != 0){
    		e.setSelection(e.getText().length());
    	}
    }
    
    
    /**
     * Prüft, ob Multiplikationszeichen eingefügt werden müssen und fügt diese ein, wenn nötig
     * @param inputEquationString
     * @return changed String inputEquationString
     */
    private String insertMultiplicationSigns(String inputEquationString){
    	for(int i=0; i<inputEquationString.length(); i++){
			switch(inputEquationString.charAt(i)){
				case '\u03c0': // Stelle mit dem 'pi' finden
					if(i>0 && inputEquationString.charAt(i-1) != '*'){ // '*' davor einfügen?
						if(Character.isDigit(inputEquationString.charAt(i-1)) || inputEquationString.charAt(i-1) == 't' || inputEquationString.charAt(i-1) == '\u03c0'){ // VOR dem 'pi-Zeichen' steht eine Zahl, ein 't' oder ein 'pi-Zeichen'
							inputEquationString = inputEquationString.substring(0,i)+ "*"+inputEquationString.substring(i, inputEquationString.length()); // Füge '*' VOR dem 'pi-Zeichen' hinzu
						}
					} 
					break;
				case 't': // Stelle mit dem 't' finden
					if(i>0 && inputEquationString.charAt(i-1) != '*'){ // '*' davor einfügen?
						if(Character.isDigit(inputEquationString.charAt(i-1)) || inputEquationString.charAt(i-1) == '\u03c0' || inputEquationString.charAt(i-1) == 't'){ // VOR dem 't' steht eine Zahl, ein 'pi-Zeichen' oder ein t
							inputEquationString = inputEquationString.substring(0,i)+ "*"+inputEquationString.substring(i, inputEquationString.length()); // Füge '*' VOR dem 't' hinzu
						}
					} 
					break;
			} 
		} 
		for(int i=0; i<inputEquationString.length(); i++){
			switch(inputEquationString.charAt(i)){
			case '\u03c0': // Stelle mit dem 'pi' finden
				if(i<inputEquationString.length()-1 && inputEquationString.charAt(i+1) != '*'){ // '*' dahinter einfügen?
					if(Character.isDigit(inputEquationString.charAt(i+1)) || inputEquationString.charAt(i+1) == 't' || inputEquationString.charAt(i+1) == '\u03c0'){ // HINTER dem 'pi-Zeichen' steht eine Zahl, ein 't' oder ein 'pi-Zeichen'
						inputEquationString = inputEquationString.substring(0,i+1)+"*"+inputEquationString.substring(i+1, inputEquationString.length()); // Füge '*' NACH dem 'pi-Zeichen' hinzu
					}
				}
				break;
			case 't': // Stelle mit dem 't' finden
				if(i<inputEquationString.length()-1 && inputEquationString.charAt(i+1) != '*'){ // '*' dahinter einfügen?
					if(Character.isDigit(inputEquationString.charAt(i+1)) || inputEquationString.charAt(i+1) == '\u03c0' || inputEquationString.charAt(i+1) == 't'){ // HINTER dem 't' steht eine Zahl, ein 'pi-Zeichen' oder ein t
						inputEquationString = inputEquationString.substring(0,i+1)+"*"+inputEquationString.substring(i+1, inputEquationString.length()); // Füge '*' NACH dem 't' hinzu
					}
				}
				break;
			}
		} 
		return inputEquationString;
    }
    
}