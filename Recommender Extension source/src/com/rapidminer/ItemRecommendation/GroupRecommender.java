package com.rapidminer.ItemRecommendation;

/**
 * Group model class for Item Recommendation algorithms
 * 
 * @see com.rapidminer.ItemRecommendation.GroupRecommender
 * @author Matej Mihelcic (Ruðer Boškoviæ Institute)

*/

import java.util.LinkedList;
import java.util.List;
import com.rapidminer.operator.Annotations;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.ProcessingStep;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.LoggingHandler;

public class GroupRecommender extends ItemRecommender{

	static final long serialVersionUID=3453434;
	
	
	List<ItemRecommender> recommenders;
	List<Double> weightList;
	double defaultWeight;

	
	public void SetDWeight(double value){
		defaultWeight=value;
	}
	
	public void SetWeights(List<Double> value){
		weightList=value;
	}
	
	public void SetRecommenders(List<ItemRecommender> value){
		recommenders=value;
		user_mapping=recommenders.get(0).user_mapping;
		item_mapping=recommenders.get(0).item_mapping;
	}
	
	
	public List<ItemRecommender> GetRecommenders(){
		return recommenders;
	}
	
	public void Train(){
		for(int i=0;i<recommenders.size();i++)
			recommenders.get(i).Train();
	}
	
	public void AddUsers(List<Integer> users)
	{
	
		for(int i=0;i<recommenders.size();i++)
			recommenders.get(i).AddUsers(users);
	}
	
	public void AddItems(List<Integer> items)
	{
		for(int i=0;i<recommenders.size();i++)
			recommenders.get(i).AddItems(items);
	}
	
	public void RetrainUsers(List<Integer> users)
	{
		for(int i=0;i<recommenders.size();i++)
			recommenders.get(i).RetrainUsers(users);
	}
	
	public void RetrainItems(List<Integer> items)
	{
		for(int i=0;i<recommenders.size();i++)
			recommenders.get(i).RetrainItems(items);
	}
	
	public double Predict(int user_id, int item_id){
		
		double score=0;
		double weightSum=0;

		if(weightList.size()<=recommenders.size()){
		for(int i=0;i<weightList.size();i++){
			score+=recommenders.get(i).Predict(user_id, item_id)* Double.valueOf(weightList.get(i));
			weightSum += Double.valueOf(weightList.get(i));
		}
		
		for(int i=weightList.size();i<recommenders.size();i++){
				score+=recommenders.get(i).Predict(user_id, item_id)*defaultWeight;
		weightSum+=defaultWeight;		
		}
	}
		else{
			for(int i=0;i<recommenders.size();i++){
				score+=recommenders.get(i).Predict(user_id, item_id)* Double.valueOf(weightList.get(i));
				weightSum += Double.valueOf(weightList.get(i));
			}
			
		}
		
		return score/weightSum;
	}
	
	public  void SaveModel(String file)
	{
		//not needed
	}

	///
	public void LoadModel(String file)
	{
		//not needed
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
