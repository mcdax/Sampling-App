package de.graphplot.activities;

import java.text.DecimalFormat;
import java.util.ArrayList;

import org.nfunk.jep.JEP;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.androidplot.ui.AnchorPosition;
import com.androidplot.ui.PositionMetrics;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.ui.XLayoutStyle;
import com.androidplot.ui.YLayoutStyle;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYSeries;

import de.graphplot.MultitouchPlot;
import de.graphplot.R;
import de.graphplot.tasks.CalcAllTask;
import de.graphplot.tasks.CalcEquationValuesTask;
import de.graphplot.tasks.CalcErrorValuesTask;
import de.graphplot.tasks.CalcInterpolation0ValuesTask;
import de.graphplot.tasks.CalcInterpolation1ValuesTask;
import de.graphplot.tasks.CalcSiValuesTask;


public class PlotActivity extends Activity {

	public static CalcAllTask calcAllTask;
	public static CalcEquationValuesTask calcEquationValuesTask;
	public static CalcInterpolation0ValuesTask calcInterpolation0ValuesTask; 
	public static CalcInterpolation1ValuesTask calcInterpolation1ValuesTask;
	public static CalcSiValuesTask calcSiValuesTask;
	public static CalcErrorValuesTask calcErrorValuesTask;
	
	public static boolean fft;
	
	private PlotActivity plotActivity = this;
	
	private String equation;
	private double resolution = 350, lowerStartBound, upperStartBound;
	private ArrayList<Double> samplingX, samplingY;
	private int samplingRate;

	private MultitouchPlot multitouchPlot;
	private XYSeries originalSeries, interpolatedSeries, errorSeries, samplingPoints;
	private LineAndPointFormatter originalSeriesFormat, interpolatedSeriesFormat, errorSeriesSeriesFormat, samplingPointFormat;
	private JEP parser;
	
	private EditText edit_kMin, edit_kMax, edit_sampleRate;
	private int k_Min, k_Max;
	private boolean isShowOrig = true, isShowError = false, isShowInter = true, isShow0Inter = false, isShow1Inter = true, isShowSiInter = false; 
	private String k_Min_String, k_Max_String, sampleString;
	
	private RadioButton radioBtn_0_Ordnung, radioBtn_1_Ordnung, radioBtn_si;
	private CheckBox checkBox_original, checkBox_interpolation, checkBox_error;
	
	//Alert Dialog Box
	private View newView, newView2;
	private RadioButton btn_alterBereich;
	
	private boolean show_fft_dialog;
	private boolean show_moreOptions_dialog;

	
	protected void onResume(){
    	super.onResume();
    	fft = false;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.plot, menu);
		MenuItem fft = menu.findItem(R.id.fft);
		fft.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				if(!show_fft_dialog && !show_moreOptions_dialog){
					show_fft_dialog = true;
					fftShow();
				}
				return true;
			}
		});
		
		MenuItem moreOptions = menu.findItem(R.id.moreOptions);
		moreOptions.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				if(!show_moreOptions_dialog && !show_fft_dialog){
					show_moreOptions_dialog = true;
					moreOptionsShow(true);
				}
				return true;
			}
		});
		
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.activity_plot);
		
	//	View view = this.getWindow().getDecorView();
	//	 view.setBackgroundColor(Color.BLACK);

		Intent intent = getIntent();
		equation = intent.getStringExtra("inputEquation");
		samplingRate = intent.getIntExtra("samplingRate", 1);
		lowerStartBound = intent.getDoubleExtra("lowerBound", -1);
		upperStartBound = intent.getDoubleExtra("upperBound", 1);
		
		parser = new JEP();
		parser.addStandardFunctions();
		parser.addStandardConstants();
		parser.setImplicitMul(true);
		
		multitouchPlot = (MultitouchPlot) findViewById(R.id.multitouchPlot);
		multitouchPlot.centerOnDomainOrigin(0);
		multitouchPlot.centerOnRangeOrigin(0);
		multitouchPlot.setRangeBoundaries(-3, 3, BoundaryMode.AUTO);
		multitouchPlot.setDomainBoundaries(lowerStartBound, upperStartBound, BoundaryMode.FIXED);
		multitouchPlot.setPlotActivity(this);
		multitouchPlot.getGraphWidget().setMarginLeft(PixelUtils.dpToPix(20));
		multitouchPlot.getGraphWidget().getBackgroundPaint().setColor(Color.BLACK);
		multitouchPlot.getBackgroundPaint().setColor(Color.BLACK);
		multitouchPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.BLACK);
		
		originalSeriesFormat = new LineAndPointFormatter(Color.BLUE, null, null, null);
		interpolatedSeriesFormat = new LineAndPointFormatter(Color.GREEN, null, null, null);
		errorSeriesSeriesFormat = new LineAndPointFormatter(Color.MAGENTA, null, null, null);
		samplingPointFormat = new LineAndPointFormatter(null, Color.RED, null, null, null);
		
		originalSeries = new SimpleXYSeries("Original");
		interpolatedSeries = new SimpleXYSeries("Interpoliert");
		errorSeries = new SimpleXYSeries("Fehler"); 
		samplingPoints = new SimpleXYSeries("Abtastpunkte"); 
		
		
		multitouchPlot.getGraphWidget().getRangeLabelPaint().setTextSize(PixelUtils.dpToPix(10));
		multitouchPlot.getGraphWidget().getDomainLabelPaint().setTextSize(PixelUtils.dpToPix(13));
		multitouchPlot.getGraphWidget().setMarginBottom(30f);
		multitouchPlot.setPadding(0, 0, 0, 10);
		multitouchPlot.getGraphWidget().setPositionMetrics(new PositionMetrics(5, XLayoutStyle.ABSOLUTE_FROM_LEFT, 5, YLayoutStyle.ABSOLUTE_FROM_TOP, AnchorPosition.LEFT_TOP));
		multitouchPlot.getGraphWidget().setSize(new SizeMetrics( 50 , SizeLayoutType.FILL, 10, SizeLayoutType.FILL));

		multitouchPlot.getLegendWidget().setSize(new SizeMetrics( 60 , SizeLayoutType.ABSOLUTE, 0, SizeLayoutType.FILL));
		multitouchPlot.getLegendWidget().setPositionMetrics(new PositionMetrics(-50f, XLayoutStyle.ABSOLUTE_FROM_RIGHT, -5, YLayoutStyle.ABSOLUTE_FROM_BOTTOM, AnchorPosition.RIGHT_BOTTOM));
		multitouchPlot.getLegendWidget().setIconSizeMetrics(new SizeMetrics( 25 , SizeLayoutType.ABSOLUTE, 25, SizeLayoutType.ABSOLUTE));
		
		multitouchPlot.getLegendWidget().getTextPaint().setTextSize(PixelUtils.dpToPix(13));

		multitouchPlot.setDomainValueFormat(new DecimalFormat("#.###"));;
		multitouchPlot.setRangeValueFormat(new DecimalFormat("#.###"));;

		// reduce the number of range labels
		multitouchPlot.setTicksPerRangeLabel(2);

		if(calcAllTask!= null){
			calcAllTask.cancel();
		}
		
		calcAllTask = new CalcAllTask(lowerStartBound, upperStartBound, multitouchPlot, plotActivity);
		calcAllTask.execute(samplingRate);
		
		moreOptionsShow(false);
	}
	
	public void onRadioButtonClicked(View view){
		if(calcAllTask!= null){
			calcAllTask.cancel();
		}
		
		calcAllTask = new CalcAllTask(lowerStartBound, upperStartBound, multitouchPlot, plotActivity);
		calcAllTask.execute(samplingRate);
		int id = view.getId();
		switch (id) {
		case R.id.radioBtn_0_Ordnung:
			isShow0Inter = true;
			isShow1Inter = false;
			isShowSiInter = false;
			break;
		case R.id.radioBtn_1_Ordnung:
			isShow1Inter = true;
			isShow0Inter = false;
			isShowSiInter = false;
			break;
		case R.id.radioBtn_si:
			isShowSiInter = true;
			isShow1Inter = false;
			isShow0Inter = false;
			break;
		}
	}
	
	public void moreOptionsShow(boolean show)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();
		newView2 = inflater.inflate(R.layout.more_options_alert, null);
		builder.setView(newView2);
		builder.setTitle(getResources().getString(R.string.options));
		builder.setPositiveButton(getResources().getString(R.string.ok), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(calcAllTask!= null){
					calcAllTask.cancel();
				}
				
				calcAllTask = new CalcAllTask(lowerStartBound, upperStartBound, multitouchPlot, plotActivity);
				calcAllTask.execute(samplingRate);
			}
		}).setNegativeButton(getResources().getString(R.string.cancel), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
	
			}
		});
		AlertDialog dialog = builder.create();
		dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
		dialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				show_moreOptions_dialog = false;
			}
		});
		if(show)
		{
		dialog.show();
		}
		edit_kMin = ((EditText) newView2.findViewById(R.id.k_Min));
		edit_kMax = ((EditText) newView2.findViewById(R.id.k_Max));
		edit_sampleRate = ((EditText) newView2.findViewById(R.id.edit_sampleText));
		edit_sampleRate.setText(samplingRate + "");
		
		if(k_Min_String != null)
		{
			edit_kMin.setText(k_Min_String);
		}
		
		if(k_Max_String != null)
		{
			edit_kMax.setText(k_Max_String);
		}
		
		edit_sampleRate.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			if(!edit_sampleRate.getText().toString().equals("") && Integer.valueOf(edit_sampleRate.getText().toString()) > 0)
			{
				sampleString = edit_sampleRate.getText().toString();
				samplingRate = Integer.valueOf(edit_sampleRate.getText().toString());
				if(calcAllTask!= null){
					calcAllTask.cancel();
				}
				
				MainActivity.samplingRate_static = samplingRate;
				calcAllTask = new CalcAllTask(lowerStartBound, upperStartBound, multitouchPlot, plotActivity);
				calcAllTask.execute(samplingRate);
				
			}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});
		edit_kMin.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(!edit_kMin.getText().toString().equals("-") && !edit_kMin.getText().toString().isEmpty())
				{
				k_Min = Integer.valueOf(edit_kMin.getText().toString());
				k_Min_String = edit_kMin.getText().toString();

				if(plotActivity.getRadioBtn_si().isChecked())
				{
					if(calcSiValuesTask != null)
					{
						calcSiValuesTask.cancel(true);
					}
					if(!edit_kMax.getText().toString().isEmpty())
					{
					calcSiValuesTask = new CalcSiValuesTask(multitouchPlot.getMinX(lowerStartBound), multitouchPlot.getMaxX(upperStartBound), multitouchPlot, plotActivity);
					calcSiValuesTask.execute(k_Min, k_Max);
					}
				}
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});
		
		edit_kMax.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
				if(!edit_kMax.getText().toString().equals("-") && !edit_kMax.getText().toString().isEmpty())
				{	
				k_Max = Integer.valueOf(edit_kMax.getText().toString());	
				k_Max_String = edit_kMax.getText().toString();
 
				if(plotActivity.getRadioBtn_si().isChecked())
				{
					if(calcSiValuesTask != null)
					{
						calcSiValuesTask.cancel(true);
					}
					
					if(!edit_kMin.getText().toString().isEmpty())
					{

					calcSiValuesTask = new CalcSiValuesTask(multitouchPlot.getMinX(lowerStartBound), multitouchPlot.getMaxX(upperStartBound), multitouchPlot, plotActivity);
					calcSiValuesTask.execute(k_Min, k_Max);
					}
				}
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});
		
		checkBox_error = (CheckBox) newView2.findViewById(R.id.checkBox_Fehler);
		checkBox_error.setChecked(isShowError);
		checkBox_error.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				isShowError = isChecked;
				if(isChecked)
				{
					if(calcErrorValuesTask != null)
					{
						calcErrorValuesTask.cancel(true);
					}
					
					calcErrorValuesTask = new CalcErrorValuesTask(multitouchPlot.getMinX(lowerStartBound), multitouchPlot.getMaxX(upperStartBound), multitouchPlot, plotActivity);
					calcErrorValuesTask.execute(0);
				}
				else
				{
					if(calcErrorValuesTask != null)
					{
						calcErrorValuesTask.cancel(true);
						plotActivity.setProgressBarIndeterminateVisibility(false);
					}
	         	
					multitouchPlot.removeSeries(errorSeries);
					multitouchPlot.redraw();
				}
			}
		});
		
		checkBox_interpolation = (CheckBox) newView2.findViewById(R.id.checkBox1_Interpolation);
		checkBox_interpolation.setChecked(isShowInter);
		checkBox_interpolation.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				isShowInter = isChecked;
				if(isChecked)
				{
					if(plotActivity.getRadioBtn_0_Ordnung().isChecked())
					{
						if(calcInterpolation0ValuesTask != null)
						{
							plotActivity.setProgressBarIndeterminateVisibility(false);
							calcInterpolation0ValuesTask.cancel(true);
						}
						calcInterpolation0ValuesTask = new CalcInterpolation0ValuesTask(multitouchPlot, plotActivity);
						calcInterpolation0ValuesTask.execute(0);
					}
					
					else if(plotActivity.getRadioBtn_1_Ordnung().isChecked())
					{
						if(calcInterpolation1ValuesTask != null)
						{
							plotActivity.setProgressBarIndeterminateVisibility(false);
							calcInterpolation1ValuesTask.cancel(true);
						}
						calcInterpolation1ValuesTask = new CalcInterpolation1ValuesTask(multitouchPlot, plotActivity);
						calcInterpolation1ValuesTask.execute(0);
					}
					
					else if(plotActivity.getRadioBtn_si().isChecked())
					{
						if(!edit_kMax.getText().toString().isEmpty() && !edit_kMin.getText().toString().isEmpty())
						{
						if(calcSiValuesTask != null)
						{
							plotActivity.setProgressBarIndeterminateVisibility(false);

							calcSiValuesTask.cancel(true);
						}
						calcSiValuesTask = new CalcSiValuesTask(multitouchPlot.getMinX(lowerStartBound), multitouchPlot.getMaxX(upperStartBound), multitouchPlot, plotActivity);
						calcSiValuesTask.execute(k_Min, k_Max);
					}
					else
					{
						showToast(getResources().getString(R.string.bothLimitsHaveToBeThere));
					}
					}
				}
				else
				{
					multitouchPlot.removeSeries(interpolatedSeries);
					multitouchPlot.redraw();
				}
			}
		});
		
		checkBox_original = (CheckBox) newView2.findViewById(R.id.CheckBox_Original);
		checkBox_original.setChecked(isShowOrig);
		checkBox_original.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				isShowOrig = isChecked;
				if(isChecked){
					calcEquationValuesTask = new CalcEquationValuesTask(multitouchPlot.getMinX(lowerStartBound), multitouchPlot.getMaxX(upperStartBound), multitouchPlot, plotActivity);
					calcEquationValuesTask.execute(equation);
				}else{
					
					if(calcEquationValuesTask != null)
					{
						calcEquationValuesTask.cancel(true);
					}
					
					multitouchPlot.removeSeries(originalSeries);
					multitouchPlot.redraw();
				}
			}
		});
		
		radioBtn_0_Ordnung = (RadioButton) newView2.findViewById(R.id.radioBtn_0_Ordnung);
		radioBtn_0_Ordnung.setChecked(isShow0Inter);
		radioBtn_1_Ordnung = (RadioButton) newView2.findViewById(R.id.radioBtn_1_Ordnung);
		radioBtn_1_Ordnung.setChecked(isShow1Inter);
		radioBtn_si = (RadioButton) newView2.findViewById(R.id.radioBtn_si);
		radioBtn_si.setChecked(isShowSiInter);
		
	}
	
	public void fftShow()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();
		newView = inflater.inflate(R.layout.fft_alert, null);
		builder.setView(newView);
		btn_alterBereich = (RadioButton)(newView.findViewById(R.id.radio_btn_alterBereich));
		btn_alterBereich.setChecked(true);
		builder.setTitle("Frequenzbild");
		builder.setPositiveButton("Erstellen", new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				Intent fftIntent = new Intent(getApplicationContext(), FftPlotActivity.class);
				fftIntent.putExtra("equation", equation);
				fftIntent.putExtra("samplingRate", samplingRate);
				
				if(btn_alterBereich.isChecked()){
					fft = true;
					fftIntent.putExtra("lowerBound", multitouchPlot.getMinX(lowerStartBound));
		    		fftIntent.putExtra("upperBound", multitouchPlot.getMaxX(upperStartBound));
		    		startActivity(fftIntent);
				}else{
					EditText min = (EditText)(newView.findViewById(R.id.editFFTMin));
					EditText max = (EditText)(newView.findViewById(R.id.editFFTMax));
					double minValue=0, maxValue=0;
					if(!max.getText().toString().equalsIgnoreCase("") && !min.getText().toString().equalsIgnoreCase("")){
						String minValueString = min.getText().toString();
						String maxValueString = max.getText().toString();
						if(minValueString.contains(",")) minValueString = minValueString.replace(',', '.'); // Ersetze , durch . (untere Grenze)
						if(maxValueString.contains(",")) maxValueString = maxValueString.replace(',', '.'); // Ersetze , durch . (obere Grenze)
						minValue = Double.parseDouble(minValueString);
						maxValue = Double.parseDouble(maxValueString);
					}else{
						showToast(getResources().getString(R.string.bothLimitsHaveToBeThere));
						return;
					}
					
					if(minValue == maxValue){ 
						showToast(getResources().getString(R.string.bothLimitsMustNotBeSame));
					}else if(minValue > maxValue){
						showToast(getResources().getString(R.string.lowerLimitHasToBeLowerThanTheUpper));
					}else{
						fft = true;
						fftIntent.putExtra("lowerBound", minValue);
						fftIntent.putExtra("upperBound", maxValue);
						startActivity(fftIntent);
					}
				}
			}}).setNegativeButton(getResources().getString(R.string.cancel), new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				
			}
		});
		AlertDialog dialog = builder.create();
		dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
		dialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				show_fft_dialog = false;
			}
		});
		dialog.show();

	}
	

	public XYSeries getOriginalSeries() {
		return originalSeries;
	}

	public void setOriginalSeries(XYSeries originalSeries) {
		this.originalSeries = originalSeries;
	}

	public XYSeries getInterpolatedSeries() {
		return interpolatedSeries;
	}

	public void setInterpolatedSeries(XYSeries interpolatedSeries) {
		this.interpolatedSeries = interpolatedSeries;
	}

	public XYSeries getSamplingPoints() {
		return samplingPoints;
	}

	public void setSamplingPoints(XYSeries samplingPoints) {
		this.samplingPoints = samplingPoints;
	}

	public PlotActivity getPlotActivity() {
		return plotActivity;
	}

	public String getEquation() {
		return equation;
	}

	public double getResolution() {
		return resolution;
	}

	public int getSamplingRate() {
		return samplingRate;
	}

	public LineAndPointFormatter getOriginalSeriesFormat() {
		return originalSeriesFormat;
	}

	public LineAndPointFormatter getInterpolatedSeriesFormat() {
		return interpolatedSeriesFormat;
	}

	public LineAndPointFormatter getSamplingPointFormat() {
		return samplingPointFormat;
	}

	public JEP getParser() {
		return parser;
	}
	
	public int getK_Min() {
		return k_Min;
	}

	public int getK_Max() {
		return k_Max;
	}

	public RadioButton getRadioBtn_0_Ordnung() {
		return radioBtn_0_Ordnung;
	}

	public RadioButton getRadioBtn_1_Ordnung() {
		return radioBtn_1_Ordnung;
	}

	public RadioButton getRadioBtn_si() {
		return radioBtn_si;
	}

	public CheckBox getCheckBox_original() {
		return checkBox_original;
	}

	public CheckBox getCheckBox_interpolation() {
		return checkBox_interpolation;
	}

	public CheckBox getCheckBox_error() {
		return checkBox_error;
	}

	public ArrayList<Double> getSamplingX() {
		return samplingX;
	}
	
	public ArrayList<Double> getSamplingY() {
		return samplingY;
	}

	public void setSamplingY(ArrayList<Double> samplingY) {
		this.samplingY = samplingY;
	}

	public void setSamplingX(ArrayList<Double> samplingX) {
		this.samplingX = samplingX;
	}

	public XYSeries getErrorSeries() {
		return errorSeries;
	}

	public void setErrorSeries(XYSeries errorSeries) {
		this.errorSeries = errorSeries;
	}

	public LineAndPointFormatter getErrorSeriesSeriesFormat() {
		return errorSeriesSeriesFormat;
	}

	public double getLowerStartBound() {
		return lowerStartBound;
	}

	public double getUpperStartBound() {
		return upperStartBound;
	}
	public void showToast(String message)
	{
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}
	
	
}