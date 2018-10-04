package de.graphplot.tasks;

import java.util.ArrayList;

import de.graphplot.MultitouchPlot;
import de.graphplot.activities.PlotActivity;
import android.os.AsyncTask;

public class CalcInterpolation1ValuesTask extends AsyncTask<Integer, Integer, Long> {
	

	private PlotActivity plotActivity;
	private MultitouchPlot multitouchPlot;
	private ArrayList<Double> samplingX, samplingY;

	
	public CalcInterpolation1ValuesTask(MultitouchPlot multitouchPlot, PlotActivity plotActivity)
	{
		this.plotActivity = plotActivity;
		this.multitouchPlot = multitouchPlot;
		this.samplingX = plotActivity.getSamplingX();
		this.samplingY = plotActivity.getSamplingY();

	}
	protected Long doInBackground(Integer ...args) {

		return null;
		
	}
	 protected void onProgressUpdate(Integer... progress) {
	     }
	 
	 protected void onPostExecute(Long result) {
		 multitouchPlot.addSeriesAndRedraw(samplingX, samplingY, plotActivity.getInterpolatedSeries(), plotActivity.getInterpolatedSeriesFormat());
	     }
	    
	}