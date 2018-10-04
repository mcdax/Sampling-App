package de.graphplot.activities;


import java.text.DecimalFormat;

import com.androidplot.ui.AnchorPosition;
import com.androidplot.ui.PositionMetrics;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.ui.XLayoutStyle;
import com.androidplot.ui.YLayoutStyle;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYStepMode;

import de.graphplot.MultitouchPlot;
import de.graphplot.R;
import de.graphplot.tasks.CalcFftPhaseTask;
import de.graphplot.tasks.CalcFftTask;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class FftPlotActivity extends Activity {

	private String equation;
	private int samplingRate;
	private double lowerBound, upperBound, maxval = 0, max_x_orig;
	private MultitouchPlot multitouchPlotFFT, multitouchPlotFFTPhase;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fft_plot);
		
		Intent intent = getIntent();
		equation = intent.getStringExtra("equation");
		samplingRate = intent.getIntExtra("samplingRate", 1);
		lowerBound = intent.getDoubleExtra("lowerBound", -1);
		upperBound = intent.getDoubleExtra("upperBound", 1);
		
		multitouchPlotFFT = (MultitouchPlot) findViewById(R.id.multitouchPlotSampling);
		multitouchPlotFFT.centerOnDomainOrigin(0);
		multitouchPlotFFT.centerOnRangeOrigin(0);
		multitouchPlotFFT.setRangeBoundaries(-3, 3, BoundaryMode.AUTO);
		multitouchPlotFFT.setDomainBoundaries(lowerBound, upperBound, BoundaryMode.AUTO);
		multitouchPlotFFT.setFFTPlotActivity(this);
		multitouchPlotFFT.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);
		
		
		multitouchPlotFFT.getGraphWidget().getRangeLabelPaint().setTextSize(PixelUtils.dpToPix(9));
		multitouchPlotFFT.getGraphWidget().getDomainLabelPaint().setTextSize(PixelUtils.dpToPix(11));
		multitouchPlotFFT.getGraphWidget().setMarginBottom(30f);

		multitouchPlotFFT.setPadding(0, 0, 0, 10);
		multitouchPlotFFT.getGraphWidget().setPositionMetrics(new PositionMetrics(10, XLayoutStyle.ABSOLUTE_FROM_LEFT, 5, YLayoutStyle.ABSOLUTE_FROM_TOP, AnchorPosition.LEFT_TOP));
		multitouchPlotFFT.getGraphWidget().setSize(new SizeMetrics( 50 , SizeLayoutType.FILL, 30, SizeLayoutType.FILL));
		multitouchPlotFFT.getGraphWidget().setMarginLeft(PixelUtils.dpToPix(15));

		multitouchPlotFFT.getLegendWidget().setSize(new SizeMetrics( 60 , SizeLayoutType.ABSOLUTE, 0, SizeLayoutType.FILL));
		multitouchPlotFFT.getLegendWidget().setPositionMetrics(new PositionMetrics(-50f, XLayoutStyle.ABSOLUTE_FROM_RIGHT, -5, YLayoutStyle.ABSOLUTE_FROM_BOTTOM, AnchorPosition.RIGHT_BOTTOM));
		multitouchPlotFFT.getLegendWidget().setIconSizeMetrics(new SizeMetrics( 25 , SizeLayoutType.ABSOLUTE, 25, SizeLayoutType.ABSOLUTE));
		
		multitouchPlotFFT.getLegendWidget().getTextPaint().setTextSize(PixelUtils.dpToPix(13));
		multitouchPlotFFT.setDomainLabel("Hz");
		multitouchPlotFFT.getDomainLabelWidget().setSize(new SizeMetrics( 60 , SizeLayoutType.ABSOLUTE, PixelUtils.dpToPix(15), SizeLayoutType.ABSOLUTE));

		multitouchPlotFFT.getDomainLabelWidget().getLabelPaint().setTextSize(PixelUtils.dpToPix(13));
		multitouchPlotFFT.getDomainLabelWidget().setPositionMetrics(new PositionMetrics(PixelUtils.dpToPix(10), XLayoutStyle.ABSOLUTE_FROM_RIGHT, 0, YLayoutStyle.ABSOLUTE_FROM_BOTTOM, AnchorPosition.RIGHT_BOTTOM));
		
		multitouchPlotFFT.setRangeValueFormat(new DecimalFormat("#.##"));
		multitouchPlotFFT.setDomainValueFormat(new DecimalFormat("#.##"));
		
		multitouchPlotFFTPhase = (MultitouchPlot) findViewById(R.id.multitouchPlotSamplingPhase);
		
		multitouchPlotFFTPhase.centerOnDomainOrigin(0);
		multitouchPlotFFTPhase.centerOnRangeOrigin(0);
		multitouchPlotFFTPhase.setRangeBoundaries(-3, 3, BoundaryMode.AUTO);
		multitouchPlotFFTPhase.setDomainBoundaries(lowerBound, upperBound, BoundaryMode.AUTO);
		multitouchPlotFFTPhase.setFFTPlotActivity(this);
		multitouchPlotFFTPhase.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);
		multitouchPlotFFTPhase.getGraphWidget().setMarginBottom(30f);
		multitouchPlotFFTPhase.getGraphWidget().setMarginLeft(PixelUtils.dpToPix(15));
		
		multitouchPlotFFTPhase.getGraphWidget().getRangeLabelPaint().setTextSize(PixelUtils.dpToPix(9));

		
		multitouchPlotFFTPhase.getGraphWidget().getDomainLabelPaint().setTextSize(PixelUtils.dpToPix(11));
		multitouchPlotFFTPhase.setPadding(0, 0, 0, 10);
		multitouchPlotFFTPhase.getGraphWidget().setPositionMetrics(new PositionMetrics(10, XLayoutStyle.ABSOLUTE_FROM_LEFT, 5, YLayoutStyle.ABSOLUTE_FROM_TOP, AnchorPosition.LEFT_TOP));
		multitouchPlotFFTPhase.getGraphWidget().setSize(new SizeMetrics( 50 , SizeLayoutType.FILL, 30, SizeLayoutType.FILL));

		multitouchPlotFFTPhase.getLegendWidget().setSize(new SizeMetrics( 60 , SizeLayoutType.ABSOLUTE, 0, SizeLayoutType.FILL));
		multitouchPlotFFTPhase.getLegendWidget().setPositionMetrics(new PositionMetrics(-50f, XLayoutStyle.ABSOLUTE_FROM_RIGHT, -5, YLayoutStyle.ABSOLUTE_FROM_BOTTOM, AnchorPosition.RIGHT_BOTTOM));
		multitouchPlotFFTPhase.getLegendWidget().setIconSizeMetrics(new SizeMetrics( 25 , SizeLayoutType.ABSOLUTE, 25, SizeLayoutType.ABSOLUTE));
		
		multitouchPlotFFTPhase.getLegendWidget().getTextPaint().setTextSize(PixelUtils.dpToPix(13));
		multitouchPlotFFTPhase.setDomainLabel("Hz");
		multitouchPlotFFTPhase.getDomainLabelWidget().setSize(new SizeMetrics( 60 , SizeLayoutType.ABSOLUTE, PixelUtils.dpToPix(15), SizeLayoutType.ABSOLUTE));

		multitouchPlotFFTPhase.getDomainLabelWidget().getLabelPaint().setTextSize(PixelUtils.dpToPix(13));
		multitouchPlotFFTPhase.getDomainLabelWidget().setPositionMetrics(new PositionMetrics(PixelUtils.dpToPix(10), XLayoutStyle.ABSOLUTE_FROM_RIGHT, 0, YLayoutStyle.ABSOLUTE_FROM_BOTTOM, AnchorPosition.RIGHT_BOTTOM));
		
		
		multitouchPlotFFTPhase.setRangeLabel("\u03c6");
		multitouchPlotFFTPhase.getRangeLabelWidget().setSize(new SizeMetrics( 60 , SizeLayoutType.ABSOLUTE, PixelUtils.dpToPix(15), SizeLayoutType.ABSOLUTE));
		
		multitouchPlotFFTPhase.getRangeLabelWidget().getLabelPaint().setTextSize(PixelUtils.dpToPix(13));
		multitouchPlotFFTPhase.getRangeLabelWidget().setPositionMetrics(new PositionMetrics(PixelUtils.dpToPix(3), XLayoutStyle.ABSOLUTE_FROM_LEFT, 0, YLayoutStyle.ABSOLUTE_FROM_TOP, AnchorPosition.LEFT_TOP));
		
		multitouchPlotFFTPhase.setDomainValueFormat(new DecimalFormat("#.##"));
		
		multitouchPlotFFTPhase.setRangeValueFormat(new DecimalFormat("#.##"));
		
		

		
		multitouchPlotFFT.getGraphWidget().getBackgroundPaint().setColor(Color.BLACK);
		multitouchPlotFFT.getBackgroundPaint().setColor(Color.BLACK);
		multitouchPlotFFT.getGraphWidget().getGridBackgroundPaint().setColor(Color.BLACK);
		
		multitouchPlotFFTPhase.getGraphWidget().getBackgroundPaint().setColor(Color.BLACK);
		multitouchPlotFFTPhase.getBackgroundPaint().setColor(Color.BLACK);
		multitouchPlotFFTPhase.getGraphWidget().getGridBackgroundPaint().setColor(Color.BLACK);
		
		
		
		LineAndPointFormatter originalFormat = new LineAndPointFormatter(Color.GREEN, null, null, null);
		LineAndPointFormatter sampleFormat = new LineAndPointFormatter(Color.RED, null, null, null);
		
		CalcFftTask calcFftTask = new CalcFftTask(multitouchPlotFFT, this, samplingRate, sampleFormat, getResources().getString(R.string.sampled));
		calcFftTask.execute(lowerBound, upperBound);
		
		calcFftTask = new CalcFftTask(multitouchPlotFFT, this, (int)(1.0/((upperBound-lowerBound)/1500.0)), originalFormat, getResources().getString(R.string.original));
		calcFftTask.execute(lowerBound, upperBound);
		
		CalcFftPhaseTask calcFftPhaseTask = new CalcFftPhaseTask(multitouchPlotFFTPhase, this , samplingRate, sampleFormat, getResources().getString(R.string.sampled));
		calcFftPhaseTask.execute(lowerBound, upperBound);
		
		calcFftPhaseTask = new CalcFftPhaseTask(multitouchPlotFFTPhase, this, (int)(1.0/((upperBound-lowerBound)/1500.0)), originalFormat, getResources().getString(R.string.original));
		calcFftPhaseTask .execute(lowerBound, upperBound);
		
		originalFormat.getLinePaint().setAlpha(130);

		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.fft_plot, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {


		return super.onOptionsItemSelected(item);
	}

	public int getSamplingRate() {
		return samplingRate;
	}

	public void setSamplingRate(int samplingRate) {
		this.samplingRate = samplingRate;
	}

	public double getLowerBound() {
		return lowerBound;
	}

	public void setLowerBound(double lowerBound) {
		this.lowerBound = lowerBound;
	}

	public double getUpperBound() {
		return upperBound;
	}

	public void setUpperBound(double upperBound) {
		this.upperBound = upperBound;
	}

	public double getMaxval() {
		return maxval;
	}

	public void setMaxval(double maxval) {
		this.maxval = maxval;
	}

	public double getMax_x_orig() {
		return max_x_orig;
	}

	public void setMax_x_orig(double max_x_orig) {
		this.max_x_orig = max_x_orig;
	}

	public String getEquation() {
		return equation;
	}

	public void setEquation(String equation) {
		this.equation = equation;
	}
	


	
}
