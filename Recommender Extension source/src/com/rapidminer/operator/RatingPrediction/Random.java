package com.rapidminer.operator.RatingPrediction;


import java.util.LinkedList;
import java.util.List;

import com.rapidminer.operator.Annotations;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.ProcessingStep;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.LoggingHandler;

/**
*Created by Matej Mihelcic (Ruðer Boškoviæ Institute) 25.08.2011
*
*Random rating predictor, use for comparison
*/

public class Random extends RatingPredictor {

	
	
	static final long serialVersionUID=1942342347;
	
	
	public boolean use_normal=false;
	public double stdev=0.1;
	public double mean=0;
	
	public void Train(){
		
	}
	
	
	public double Predict(int user_id, int item_id){
		
		java.util.Random pred1=new java.util.Random();
		com.rapidminer.utils.Random pred=new com.rapidminer.utils.Random();
		
		if(use_normal==false)
		return pred1.nextDouble()*max_rating;
		else{ 
			
			double temp=Math.abs(pred.NextNormal(mean,stdev))*max_rating;
			
			while(temp>max_rating)
				temp/=10;
			
			return temp;
		}
	}
	
	
	
	
	public void LoadModel(String filename){
		
		
	}
	
	
	public void SaveModel(String filename){
		
	}
	
	
	
	
	///
	public  String ToString()
	{
		return "";
	}
	
	

	   private String source = null;
	    
	    /** The current working operator. */
	    private transient LoggingHandler loggingHandler;
	    
	    private transient LinkedList<ProcessingStep> processingHistory = new LinkedList<ProcessingStep>();
	    
	    /** Sets the source of this IOObject. */
	    public void setSource(String sourceName) {
	        this.source = sourceName;
	    }

	    /** Returns the source of this IOObject (might return null if the source is unknown). */
	    public String getSource() {
	        return source;
	    }
	    
	    @Override
	    public void appendOperatorToHistory(Operator operator, OutputPort port) {
	    	if (processingHistory == null) {
	    		processingHistory = new LinkedList<ProcessingStep>();
	    	if (operator.getProcess() != null)
	    		processingHistory.add(new ProcessingStep(operator, port));
	    }
	    	ProcessingStep newStep = new ProcessingStep(operator, port);
	    	if (operator.getProcess() != null && (processingHistory.isEmpty() || !processingHistory.getLast().equals(newStep))) {
	    		processingHistory.add(newStep);
	    	}
	    }
	    
	    @Override
	    public List<ProcessingStep> getProcessingHistory() {
	    	if (processingHistory == null)
	    		processingHistory = new LinkedList<ProcessingStep>();
	    	return processingHistory;
	    }
	    
	    /** Gets the logging associated with the operator currently working on this 
	     *  IOObject or the global log service if no operator was set. */
	    public LoggingHandler getLog() {
	        if (this.loggingHandler != null) {
	            return this.loggingHandler;
	        } else {
	            return LogService.getGlobal();
	        }
	    }
	    
	    /** Sets the current working operator, i.e. the operator which is currently 
	     *  working on this IOObject. This might be used for example for logging. */
	    public void setLoggingHandler(LoggingHandler loggingHandler) {
	        this.loggingHandler = loggingHandler;
	    }
	    
		/**
		 * Returns not a copy but the very same object. This is ok for IOObjects
		 * which cannot be altered after creation. However, IOObjects which might be
		 * changed (e.g. {@link com.rapidminer.example.ExampleSet}s) should
		 * overwrite this method and return a proper copy.
		 */
		public IOObject copy() {
			return this;
		}
		
		protected void initWriting() {}

	
		public Annotations getAnnotations(){
			Annotations temp=new Annotations();
			return temp;
		}
}
