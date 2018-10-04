package de.graphplot.tasks;

import java.util.ArrayList;

import org.nfunk.jep.JEP;

import de.graphplot.MultitouchPlot;
import de.graphplot.activities.PlotActivity;
import android.os.AsyncTask;

public class CalcErrorValuesTask extends AsyncTask<Integer, Integer, Long> {

	private MultitouchPlot multitouchPlot;
	private PlotActivity plotActivity;

	private double minX;
	private double maxX;
	private String equation;

	private JEP parser;

	private ArrayList<Double> seriesX;
	private ArrayList<Double> seriesY;


	public CalcErrorValuesTask(double minX, double maxX, MultitouchPlot multitouchPlot, PlotActivity plotActivity) {
	
		this.plotActivity = plotActivity;
		this.multitouchPlot = multitouchPlot;
		this.minX = minX;
		this.maxX = maxX;
		this.equation = plotActivity.getEquation();

		parser = plotActivity.getParser();

	}


	protected Long doInBackground(Integer... args) {
				 
		plotActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
        		plotActivity.setProgressBarIndeterminateVisibility(true);

            }
        });
		
		seriesX = new ArrayList<Double>();
		seriesY = new ArrayList<Double>();

		if(plotActivity.getRadioBtn_0_Ordnung().isChecked())
		{
			int k_old = 0;
			for(double i = minX - (maxX - minX)/2;!this.isCancelled() && i < maxX + (maxX - minX);i += (maxX - minX)/2 / 500) 
			{
				int k = 0;
				while(k < plotActivity.getInterpolatedSeries().size() && i > plotActivity.getInterpolatedSeries().getX(k).doubleValue())
				{
					k++;
				}
				
				if(k>k_old)
				{
					seriesX.add(plotActivity.getInterpolatedSeries().getX(k-1).doubleValue()); //Damit der Fehler unabhängig von der Auflösung an den Abtastpunkten bei y=0 dargestellt wird
					seriesY.add(0.0);
					k_old = k;
				}
				
				seriesX.add(i);
				seriesY.add(getValue(equation, i) - plotActivity.getInterpolatedSeries().getY(k).doubleValue());
			}		
		}
		
		else if(plotActivity.getRadioBtn_1_Ordnung().isChecked())
		{
			int k_old = 0;
			for(double i = minX - (maxX - minX)/2;!this.isCancelled() && i < maxX + (maxX - minX)/2;i += (maxX - minX) / 500) 
			{
				int k = 0;
				while(k < plotActivity.getInterpolatedSeries().size() && i > plotActivity.getInterpolatedSeries().getX(k).doubleValue())
				{
					k++;
				}
				
				if(k>k_old)
				{
					seriesX.add(plotActivity.getInterpolatedSeries().getX(k-1).doubleValue());
					seriesY.add(0.0);
					k_old = k;
				}
				
				try
				{
				seriesX.add(i);
				seriesY.add(getValue(equation, i) - (plotActivity.getInterpolatedSeries().getY(k-1).doubleValue()+(i-plotActivity.getInterpolatedSeries().getX(k-1).doubleValue())*((plotActivity.getInterpolatedSeries().getY(k).doubleValue()-plotActivity.getInterpolatedSeries().getY(k-1).doubleValue())/(plotActivity.getInterpolatedSeries().getX(k).doubleValue()-plotActivity.getInterpolatedSeries().getX(k-1).doubleValue()))));
				}
				catch(IndexOutOfBoundsException e)
				{
					System.err.println(e);
				}
				}	
		}
		
		else if(plotActivity.getRadioBtn_si().isChecked())
		{
			for(int k = 0; k < plotActivity.getInterpolatedSeries().size(); k++)
			{
			seriesX.add(plotActivity.getInterpolatedSeries().getX(k).doubleValue());
			seriesY.add(getValue(equation, plotActivity.getInterpolatedSeries().getX(k).doubleValue()) - plotActivity.getInterpolatedSeries().getY(k).doubleValue());
			}
		}
		

		return null;
	}

	protected void onProgressUpdate(Integer... progress) {
	}

	protected void onPostExecute(Long result) {
		multitouchPlot.addSeriesAndRedraw(seriesX, seriesY, plotActivity.getErrorSeries(), plotActivity.getErrorSeriesSeriesFormat());
		
		plotActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
        		plotActivity.setProgressBarIndeterminateVisibility(false);

            }
        });
	}


	private double getValue(String equation, double x) {

		parser.addVariable("t", x);
		parser.parseExpression(equation);
		Double result = parser.getValue();

		return result;
	}

}