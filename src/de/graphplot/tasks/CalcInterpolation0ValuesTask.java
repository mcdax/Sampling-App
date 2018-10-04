package de.graphplot.tasks;

import java.util.ArrayList;

import de.graphplot.MultitouchPlot;
import de.graphplot.activities.PlotActivity;
import android.os.AsyncTask;

public class CalcInterpolation0ValuesTask extends AsyncTask<Integer, Integer, Long> {
	
	private ArrayList<Double> seriesX, seriesY, samplingX, samplingY;
	private MultitouchPlot multitouchPlot;
	private double samplingRate;	
	private PlotActivity plotActivity;
	
	public CalcInterpolation0ValuesTask(MultitouchPlot multitouchPlot, PlotActivity plotActivity)
	{
		this.multitouchPlot = multitouchPlot;
		this.samplingX = plotActivity.getSamplingX();
		this.samplingY = plotActivity.getSamplingY();
		this.plotActivity = plotActivity;
		this.samplingRate = plotActivity.getSamplingRate(); 
	}
	
	protected Long doInBackground(Integer ...args) {

		seriesX = new ArrayList<Double>();
		seriesY = new ArrayList<Double>();
		
		for(int i = 0;!this.isCancelled() && i < samplingX.size(); i++)
		{
			seriesX.add(samplingX.get(i));
			seriesY.add(samplingY.get(i));
			
			seriesX.add(samplingX.get(i)+(1/samplingRate));
			seriesY.add(samplingY.get(i));
		}
				
		return null;
	}
	
	 protected void onProgressUpdate(Integer... progress) {
	     }
	 
	 protected void onPostExecute(Long result) {
		 multitouchPlot.addSeriesAndRedraw(seriesX, seriesY, plotActivity.getInterpolatedSeries(), plotActivity.getInterpolatedSeriesFormat());
	     }
	 
	    
	}