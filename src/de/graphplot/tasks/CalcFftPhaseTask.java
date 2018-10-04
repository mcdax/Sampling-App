package de.graphplot.tasks;

import java.util.ArrayList;

import org.jtransforms.fft.DoubleFFT_1D;
import org.nfunk.jep.JEP;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import de.graphplot.MultitouchPlot;
import de.graphplot.activities.FftPlotActivity;
import android.os.AsyncTask;

public class CalcFftPhaseTask extends AsyncTask<Double, Integer, Long> {
	
	private MultitouchPlot multitouchPlot;
	private JEP parser;
	private int samplingRate;
	private String equation, name;
	private ArrayList<Double> seriesX;
	private ArrayList<Double> seriesY;
	private LineAndPointFormatter sampleFormat;
	private FftPlotActivity fftPlotActivity;
	
	public CalcFftPhaseTask(MultitouchPlot multitouchPlot, FftPlotActivity fftPlotActivity, int sampleRate, LineAndPointFormatter sampleFormat, String name)
	{
		
		this.multitouchPlot = multitouchPlot;
		this.fftPlotActivity = fftPlotActivity;
		this.equation = fftPlotActivity.getEquation();
		this.samplingRate = sampleRate;
		this.sampleFormat = sampleFormat;
		this.name = name;
		
		parser = new JEP();
		parser.addStandardFunctions();
		parser.addStandardConstants();
		parser.setImplicitMul(true);

	}
	protected Long doInBackground(Double ...lower_higher_Bound) {

		seriesX = new ArrayList<Double>();
		seriesY = new ArrayList<Double>();
	
		
		ArrayList<Double> values = new ArrayList<Double>();
        double i = lower_higher_Bound[0];
        while(!this.isCancelled() && i<lower_higher_Bound[1])
		{
        	values.add(getValue(equation, i));
			i+=1.0/samplingRate;
		}
    	int size = values.size();
        DoubleFFT_1D fft = new DoubleFFT_1D(size);
        
        // Buffer zum laden der Daten (Jede zweite Stelle: Komplex)
        double[] fftData = new double[size * 2];
        for (int j = 0; j < size; j++) {
            // Imaginärteil = 0
            fftData[2 * j] = values.get(j);
            fftData[2 * j + 1] = 0;
        }

 
        fft.complexForward(fftData);
		
        
        double max_phase = -1;
        int length = fftData.length;
      
        if(name.equals("Original"))
        {
        	length /=2;
        }
        seriesX.add(0.0);
        seriesY.add(0.0);
        for (int j = 0; j < length; j += 2) {
            double hz = ((j / 1.0) / fftData.length) * (1.0*samplingRate);

            double phase;
            if((Math.abs((fftData[j + 1])) > 0.1 ? fftData[j + 1] : 0) == 0 && ((Math.abs(fftData[j])) > 0.1 ? fftData[j] : 0) == 0)
            {
            	phase = 0;
            } else if((Math.abs((fftData[j + 1])) > 0.1 ? fftData[j + 1] : 0) > 0 && ((Math.abs(fftData[j])) > 0.1 ? fftData[j] : 0) == 0)
            {
            	phase = Math.PI/2;
            } else if((Math.abs((fftData[j + 1])) > 0.1 ? fftData[j + 1] : 0) < 0 && ((Math.abs(fftData[j])) > 0.1 ? fftData[j] : 0) == 0)
                {
                	phase = -(Math.PI/2);
                }
            else
            {
             phase = Math.atan2((Math.abs(fftData[j + 1]) > 0.1 ? fftData[j + 1] : 0), (Math.abs(fftData[j]) > 0.1 ? fftData[j] : 0));
            }
            
            seriesX.add(hz);
            seriesY.add(0.0);
            
            seriesX.add(hz);
            seriesY.add(phase);
            
            seriesX.add(hz);
            seriesY.add(0.0);
            

            if (max_phase < phase) {
                max_phase = phase;
            }
        }
       
        
        if(name.equals("Original"))
        {
        	for(int j = seriesX.size()-1; fftPlotActivity.getMax_x_orig() < seriesX.get(j) && j>0; j--)
        	{
        		seriesX.remove(j);
        		seriesY.remove(j);
        	}
        }
        
        seriesX.add(seriesX.get(seriesX.size()-1));
		seriesY.add(0.0);
        
        seriesX.add(seriesX.get(seriesX.size()-1)+1);
		seriesY.add(0.0);
        
        if(fftPlotActivity.getMaxval() < max_phase)
        {
        multitouchPlot.setRangeBoundaries(-(max_phase+0.25*max_phase), max_phase+0.25*max_phase, BoundaryMode.FIXED);
        fftPlotActivity.setMaxval(max_phase+0.25*max_phase);
        }
        
		return null;
		
	}
	 protected void onProgressUpdate(Integer... progress) {
	     }
	 
	 protected void onPostExecute(Long result) {
		 multitouchPlot.addFftAndRedraw(seriesX, seriesY, sampleFormat, name);
	     }
	 
		private double getValue(String equation, double x) {

			parser.addVariable("t", x);
			parser.parseExpression(equation);
			Double result = parser.getValue();

			return result;
		}
	    
	}