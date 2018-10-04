package de.graphplot.tasks;

import java.util.ArrayList;

import org.nfunk.jep.JEP;

import de.graphplot.MultitouchPlot;
import de.graphplot.activities.PlotActivity;
import android.os.AsyncTask;

public class CalcEquationValuesTask extends AsyncTask<String, Integer, Long> {
	
	private double minX;
	private double maxX;
	private double resolution;
	private JEP parser;
	private ArrayList<Double> seriesX;
	private ArrayList<Double> seriesY;
	private MultitouchPlot multitouchPlot;
	private PlotActivity plotActivity;
	
	public CalcEquationValuesTask(double minX, double maxX, MultitouchPlot multitouchPlot, PlotActivity plotActivity)
	{
		this.minX = minX;
		this.maxX = maxX;
		this.plotActivity = plotActivity;
		this.resolution = plotActivity.getResolution();
		this.multitouchPlot = multitouchPlot;

		parser = plotActivity.getParser();
	}
	protected Long doInBackground(String... equations) {

		seriesX = new ArrayList<Double>();
		seriesY = new ArrayList<Double>();

		double i = minX-(maxX-minX)/2;
		while(!this.isCancelled() && i<maxX+(maxX-minX)/2)
		{
			seriesX.add(i);
			seriesY.add(getValue(equations[0], i));
			i+=(maxX-minX)/resolution;
		}
		
		return null;
	}
	 protected void onProgressUpdate(Integer... progress) {
	     }
	 protected void onPostExecute(Long result) {
		 multitouchPlot.addSeriesAndRedraw(seriesX, seriesY, plotActivity.getOriginalSeries(), plotActivity.getOriginalSeriesFormat());
	     }
	 
	    private double getValue(String equation, double value)
	    {
	    	parser.addVariable("t", value);
	    	parser.parseExpression(equation);
	    	return parser.getValue();
	    }
	    
	}