package de.graphplot.tasks;

import java.util.ArrayList;

import org.nfunk.jep.JEP;

import de.graphplot.MultitouchPlot;
import de.graphplot.activities.PlotActivity;
import android.os.AsyncTask;

public class CalcSiValuesTask extends AsyncTask<Integer, Integer, Long> {

	private MultitouchPlot multitouchPlot;
	private PlotActivity plotActivity;

	private double minX;
	private double maxX;
	private double resolution;
	private double samplingRate;
	private String equation;

	private JEP parser;

	private ArrayList<Double> seriesX;
	private ArrayList<Double> seriesY;


	public CalcSiValuesTask(double minX, double maxX, MultitouchPlot multitouchPlot, PlotActivity plotActivity) {
	
		this.plotActivity = plotActivity;
		this.multitouchPlot = multitouchPlot;
		this.minX = minX;
		this.maxX = maxX;
		this.resolution = plotActivity.getResolution();
		this.samplingRate = plotActivity.getSamplingRate();
		this.equation = plotActivity.getEquation();

		parser = plotActivity.getParser();

	}


	protected Long doInBackground(Integer... kMin_kMax) {
		
		plotActivity.runOnUiThread(new Runnable() {
             @Override
             public void run() {
         		plotActivity.setProgressBarIndeterminateVisibility(true);

             }
         });
		 
		seriesX = new ArrayList<Double>();
		seriesY = new ArrayList<Double>();

		
		double T = 1 / samplingRate;

		for(double i = minX - (maxX - minX) / 2;!this.isCancelled() && i < maxX + (maxX - minX) / 2;i += (maxX - minX) / resolution) {
		
			double val = 0;
			for (int k = kMin_kMax[0]; !this.isCancelled() && k <= kMin_kMax[1]; k++) {
				val += calcSiValue(i, T, k);
			}
			
			seriesX.add(i);
			seriesY.add(val);	

		}

		return null;
	}

	protected void onProgressUpdate(Integer... progress) {
	}

	protected void onPostExecute(Long result) {
		multitouchPlot.addSeriesAndRedraw(seriesX, seriesY, plotActivity.getInterpolatedSeries(), plotActivity.getInterpolatedSeriesFormat());
		
		plotActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
        		plotActivity.setProgressBarIndeterminateVisibility(false);

            }
        });

	}

	private double calcSiValue(double t, double T, int k) {

		if (t - k * T == 0) {
			return 1; // si(0) = 1
		}

		return (getValue(equation, k * T) * (Math.sin(Math.PI * (t - k * T) / T) / (Math.PI * (t - k * T) / T)));
	}

	private double getValue(String equation, double x) {

		parser.addVariable("t", x);
		parser.parseExpression(equation);
		Double result = parser.getValue();

		return result;
	}

}