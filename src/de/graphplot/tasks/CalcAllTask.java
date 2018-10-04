package de.graphplot.tasks;

import java.util.ArrayList;

import org.nfunk.jep.JEP;

import de.graphplot.MultitouchPlot;
import de.graphplot.activities.PlotActivity;
import android.os.AsyncTask;

public class CalcAllTask extends AsyncTask<Integer, Integer, Long> {
	
	private double minX;
	private double maxX;
	
	private JEP parser;
	private PlotActivity plotActivity;

	private MultitouchPlot multitouchPlot;
	private ArrayList<Double> samplingX, samplingY;
	
	public CalcAllTask(double minX, double maxX, MultitouchPlot multitouchPlot, PlotActivity plotActivity)
	{
		this.minX = minX;
		this.maxX = maxX;
		this.multitouchPlot = multitouchPlot;
		this.plotActivity = plotActivity;
		this.parser = plotActivity.getParser();
		
		this.samplingX = new ArrayList<Double>();
		this.samplingY = new ArrayList<Double>();
	}
	
	protected Long doInBackground(Integer ...samplingrate) {

		double i = (minX-(maxX-minX)) - (((minX-(maxX-minX))/(1.0/samplingrate[0]))%1)*(1.0/samplingrate[0]); //Damit der Anfangsabtastwert bei t = 0 ist
		i -= 1.0/samplingrate[0];
		while (!this.isCancelled() && i < maxX+(maxX-minX)+1.0/samplingrate[0]) {
			samplingX.add(i);
			samplingY.add(getValue(plotActivity.getEquation(), i));
			i += 1.0 / samplingrate[0];
		}
		
		return null;
		
	}
	 protected void onProgressUpdate(Integer... progress) {
	     }
	 
	 protected void onPostExecute(Long result) {
		 
		 multitouchPlot.addSeriesAndRedraw(samplingX, samplingY, plotActivity.getSamplingPoints(), plotActivity.getSamplingPointFormat());
		 plotActivity.setSamplingX(samplingX);
		 plotActivity.setSamplingY(samplingY);
		 
		 if(plotActivity.getCheckBox_original().isChecked())
		 {
		 PlotActivity.calcEquationValuesTask = new CalcEquationValuesTask(minX, maxX, multitouchPlot, plotActivity);
		 PlotActivity.calcEquationValuesTask.execute(plotActivity.getEquation());
		 }	
		 
		 
				if(plotActivity.getRadioBtn_0_Ordnung().isChecked())
				{
					PlotActivity.calcInterpolation0ValuesTask = new CalcInterpolation0ValuesTask(multitouchPlot, plotActivity);
					PlotActivity.calcInterpolation0ValuesTask.execute(0);
				}
				
				else if(plotActivity.getRadioBtn_1_Ordnung().isChecked())
				{
					PlotActivity.calcInterpolation1ValuesTask = new CalcInterpolation1ValuesTask(multitouchPlot, plotActivity);
					PlotActivity.calcInterpolation1ValuesTask.execute(0);
				}
				
				else if(plotActivity.getRadioBtn_si().isChecked())
				{
					PlotActivity.calcSiValuesTask = new CalcSiValuesTask(minX, maxX,multitouchPlot, plotActivity);
					PlotActivity.calcSiValuesTask.execute(plotActivity.getK_Min(), plotActivity.getK_Max());
				}
	     
	 }
	 
	
	 public void cancel()
	 {
		 this.cancel(true);
		 if(PlotActivity.calcEquationValuesTask  != null)
		 {
			 PlotActivity.calcEquationValuesTask.cancel(true); 
		 }
		 if(PlotActivity.calcInterpolation0ValuesTask  != null)
		 {
			 PlotActivity.calcInterpolation0ValuesTask.cancel(true); 
		 }
		 if(PlotActivity.calcInterpolation1ValuesTask  != null)
		 {
			 PlotActivity.calcInterpolation1ValuesTask.cancel(true); 
		 }
		 if(PlotActivity.calcSiValuesTask  != null)
		 {
			 PlotActivity.calcSiValuesTask.cancel(true); 
		 } 
	 }

	 
		private double getValue(String equation, double x) {

			parser.addVariable("t", x);
			parser.parseExpression(equation);
			Double result = parser.getValue();

			return result;
		}
	    
	}