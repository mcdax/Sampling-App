package de.graphplot;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYSeriesFormatter;

import de.graphplot.activities.FftPlotActivity;
import de.graphplot.activities.PlotActivity;
import de.graphplot.tasks.CalcAllTask;
import de.graphplot.tasks.CalcErrorValuesTask;


public class MultitouchPlot extends XYPlot implements OnTouchListener {

	// Touchzustände
	static final private int NONE = 0;
	static final private int ONE_FINGER_DRAG = 1;
	static final private int TWO_FINGERS_DRAG = 2;
	private int mode = NONE;

	private Number minXSeriesValue;
	private Number maxXSeriesValue;
	private Number minYSeriesValue;
	private Number maxYSeriesValue;

	private PointF firstFinger;
	private float lastScrolling;
	private float distBetweenFingers;

	private Number newMinX;
	private Number newMaxX;
	
	private PlotActivity plotActivity;
	private FftPlotActivity fftPlotActivity;

	public MultitouchPlot(Context context, String title) {
		super(context, title);
		initTouchHandling();
	}

	public MultitouchPlot(Context context, AttributeSet attributes) {
		super(context, attributes);
		initTouchHandling();
	}

	public MultitouchPlot(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initTouchHandling();
	}

	private void initTouchHandling() {
		this.setOnTouchListener(this);
	}

	public synchronized boolean addSeries(XYSeries series, XYSeriesFormatter formatter) {
		// Berechnen von min und max Werten
		for (int i = 0; i < series.size(); i++) {
			if (minXSeriesValue == null || minXSeriesValue.doubleValue() > series.getX(i).doubleValue())
				minXSeriesValue = series.getX(i);
			if (maxXSeriesValue == null || maxXSeriesValue.doubleValue() < series.getX(i).doubleValue())
				maxXSeriesValue = series.getX(i);
			if (minYSeriesValue == null || minYSeriesValue.doubleValue() > series.getY(i).doubleValue())
				minYSeriesValue = series.getY(i);
			if (maxYSeriesValue == null || maxYSeriesValue.doubleValue() < series.getX(i).doubleValue())
				maxYSeriesValue = series.getY(i);
		}
		return super.addSeries(series, formatter);
	}

	public boolean onTouch(View view, MotionEvent motionEvent) {
		switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			firstFinger = new PointF(motionEvent.getX(), motionEvent.getY());
			mode = ONE_FINGER_DRAG;
			break;
		case MotionEvent.ACTION_POINTER_DOWN: {
			distBetweenFingers = distance(motionEvent);
			if (distBetweenFingers > 5f || distBetweenFingers < -5f)
				mode = TWO_FINGERS_DRAG;
			break;
		}
		case MotionEvent.ACTION_POINTER_UP:
			mode = ONE_FINGER_DRAG;
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode == ONE_FINGER_DRAG) {
				calculateMinMaxVals();
				final PointF oldFirstFinger = firstFinger;
				firstFinger = new PointF(motionEvent.getX(), motionEvent.getY());
				lastScrolling = oldFirstFinger.x - firstFinger.x;
				scroll(lastScrolling);
				fixBoundariesForScroll();
				setDomainBoundaries(newMinX, newMaxX, BoundaryMode.FIXED);
				redraw();
			} else if (mode == TWO_FINGERS_DRAG) {
				calculateMinMaxVals();
				final float oldDist = distBetweenFingers;
				final float newDist = distance(motionEvent);
				if (oldDist > 0 && newDist < 0 || oldDist < 0 && newDist > 0) 
					break;

				distBetweenFingers = newDist;

				zoom(oldDist / distBetweenFingers);

				fixBoundariesForZoom();
				setDomainBoundaries(newMinX, newMaxX, BoundaryMode.FIXED);
				redraw();
			}
			break;
		}

		return true;
	}

	private void scroll(float pan) {
		float calculatedMinX = getCalculatedMinX().floatValue();
		float calculatedMaxX = getCalculatedMaxX().floatValue();
		final float domainSpan = calculatedMaxX - calculatedMinX;
		final float step = domainSpan / getWidth();
		final float offset = pan * step;

		newMinX = calculatedMinX + offset;
		newMaxX = calculatedMaxX + offset;
		
		if(!PlotActivity.fft)
		{
	
		if(PlotActivity.calcAllTask!= null)
		{
			PlotActivity.calcAllTask.cancel();
		}
		
		PlotActivity.calcAllTask = new CalcAllTask(newMinX.doubleValue(), newMaxX.doubleValue(), this, plotActivity);
		PlotActivity.calcAllTask.execute(plotActivity.getSamplingRate());
		}
	}

	private void fixBoundariesForScroll() {
		float diff = newMaxX.floatValue() - newMinX.floatValue();
		if (newMinX.floatValue() < minXSeriesValue.floatValue()) {
			newMinX = minXSeriesValue;
			newMaxX = newMinX.floatValue() + diff;
		}
		if (newMaxX.floatValue() > maxXSeriesValue.floatValue()) {
			newMaxX = maxXSeriesValue;
			newMinX = newMaxX.floatValue() - diff;
		}
	}

	private float distance(MotionEvent event) {
		final float x = event.getX(0) - event.getX(1);
		return x;
	}

	private void zoom(float scale) {
		if (Float.isInfinite(scale) || Float.isNaN(scale) || (scale > -0.001 && scale < 0.001)) // sanity
																								// check
			return;

		float calculatedMinX = getCalculatedMinX().floatValue();
		float calculatedMaxX = getCalculatedMaxX().floatValue();
		final float domainSpan = calculatedMaxX - calculatedMinX;
		final float domainMidPoint = calculatedMaxX - domainSpan / 2.0f;
		final float offset = domainSpan * scale / 2.0f;
		newMinX = domainMidPoint - offset;
		newMaxX = domainMidPoint + offset;

		if(!PlotActivity.fft)
		{
		if(PlotActivity.calcAllTask!= null)
		{
			PlotActivity.calcAllTask.cancel();
		}
		
		PlotActivity.calcAllTask = new CalcAllTask(newMinX.doubleValue(), newMaxX.doubleValue(), this, plotActivity);
		PlotActivity.calcAllTask.execute(plotActivity.getSamplingRate());
		}
	}

	private void fixBoundariesForZoom() {
		if (newMinX.floatValue() < minXSeriesValue.floatValue()) {
			newMinX = minXSeriesValue;
		}
		if (newMaxX.floatValue() > maxXSeriesValue.floatValue()) {
			newMaxX = maxXSeriesValue;
		}
	}

	public synchronized void addSeriesAndRedraw(ArrayList<Double> seriesX, ArrayList<Double> seriesY, XYSeries series, LineAndPointFormatter seriesFormat){
		
		if(seriesX.size() == seriesY.size())
		{
		this.removeSeries(series);

		if (series == plotActivity.getOriginalSeries()){ 
			series = new SimpleXYSeries(seriesX, seriesY, "Original");
			this.addSeries(series, seriesFormat);
			plotActivity.setOriginalSeries(series);
		}else if (series == plotActivity.getInterpolatedSeries()){
			series = new SimpleXYSeries(seriesX, seriesY, "Interpolation");
			plotActivity.setInterpolatedSeries(series);

			if(plotActivity.getCheckBox_interpolation().isChecked())
			{
			this.addSeries(series, seriesFormat);
			}
			
			if(PlotActivity.calcErrorValuesTask != null)
			{
				PlotActivity.calcErrorValuesTask.cancel(true);
			}
			
			if(plotActivity.getCheckBox_error().isChecked())
			{
				PlotActivity.calcErrorValuesTask = new CalcErrorValuesTask(getMinX(plotActivity.getLowerStartBound()), getMaxX(plotActivity.getUpperStartBound()), this, plotActivity);
				PlotActivity.calcErrorValuesTask.execute(0);
			}
			
			
		}else if (series == plotActivity.getErrorSeries()){
			series = new SimpleXYSeries(seriesX, seriesY, "Fehler");
			this.addSeries(series, seriesFormat);
			plotActivity.setErrorSeries(series);
		}if (series == plotActivity.getSamplingPoints()){
			series = new SimpleXYSeries(seriesX, seriesY, "Abtastpunkte");
			this.addSeries(series, seriesFormat);
			plotActivity.setSamplingPoints(series);
		}

		this.redraw();
	}
	}

	public double getMinX(double defaultMin) {
		
		if(newMinX == null)
		{
			return defaultMin;
		}
		
		return newMinX.doubleValue();
	}

	public double getMaxX(double defaultMax) {
		if(newMaxX == null)
		{
			return defaultMax;
		}
		
		return newMaxX.doubleValue();
	}
	
	public void setPlotActivity(PlotActivity plotActivity) {
		this.plotActivity = plotActivity;
	}
	
	public void setFFTPlotActivity(FftPlotActivity plotActivity) {
		this.fftPlotActivity = plotActivity;
	}

	public void addFftAndRedraw(ArrayList<Double> seriesX, ArrayList<Double> seriesY, LineAndPointFormatter seriesFormat, String name) {
	
		XYSeries series = new SimpleXYSeries(seriesX, seriesY, name);
		this.addSeries(series, seriesFormat);
		this.redraw();
		
	}
	
}