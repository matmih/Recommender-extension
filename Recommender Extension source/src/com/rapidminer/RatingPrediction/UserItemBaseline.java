package com.rapidminer.RatingPrediction;

import java.util.LinkedList;
import java.util.List;

import com.rapidminer.eval.RatingEval;
import com.rapidminer.operator.Annotations;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.RatingPrediction.RatingPredictor;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.ProcessingStep;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.LoggingHandler;


/**
*Copyright (C) 2010, 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 22.07.2011
*/

public class UserItemBaseline extends RatingPredictor
{
	/// <summary>Regularization parameter for the user biases</summary>
	/// <remarks>If not set, the recommender will try to find suitable values.</remarks>
	public double RegU;
	static final long serialVersionUID=1942342342;
	/// <summary>Regularization parameter for the item biases</summary>
	/// <remarks>If not set, the recommender will try to find suitable values.</remarks>
	public double RegI;

	///
	public int NumIter;

	private double global_average;
	private double[] user_biases;
	private double[] item_biases;

	/// <summary>Default constructor</summary>
	public UserItemBaseline()
	{
		RegU = 15;
		RegI = 10;
		NumIter = 10;
	}

	///
	protected void InitModel()
	{
		super.InitModel();

		user_biases = new double[MaxUserID + 1];
		item_biases = new double[MaxItemID + 1];
	}

	///
	public  void Train()
	{
		InitModel();

		global_average = GetRatings().Average();

		for (int i = 0; i < NumIter; i++)
			Iterate();
	}

	///
	public void Iterate()
	{
		OptimizeItemBiases();
		OptimizeUserBiases();
	}

	void OptimizeUserBiases()
	{
		int[] user_ratings_count = new int[MaxUserID + 1];

		for (int index = 0; index < GetRatings().Count(); index++)
		{

			user_biases[GetRatings().GetUsers().get(index)] += GetRatings().GetValues(index) - global_average - item_biases[GetRatings().GetItems().get(index)];
			user_ratings_count[GetRatings().GetUsers().get(index)]++;
		}
		for (int u = 0; u < user_biases.length; u++)
			if (user_ratings_count[u] != 0)
				user_biases[u] = user_biases[u] / (RegU + user_ratings_count[u]);
	}

	void OptimizeItemBiases()
	{
		int[] item_ratings_count = new int[MaxItemID + 1];

		// compute item biases
		for (int index = 0; index < GetRatings().Count(); index++)
		{
			item_biases[GetRatings().GetItems().get(index)] += GetRatings().GetValues(index) - global_average - user_biases[GetRatings().GetUsers().get(index)];
			item_ratings_count[GetRatings().GetItems().get(index)]++;
		}
		for (int i = 0; i < item_biases.length; i++)
			if (item_ratings_count[i] != 0)
				item_biases[i] = item_biases[i] / (RegI + item_ratings_count[i]);
	}
	

	///
	public double Predict(int user_id, int item_id)
	{
		double user_bias = (user_id < user_biases.length && user_id >= 0) ? user_biases[user_id] : 0;
		double item_bias = (item_id < item_biases.length && item_id >= 0) ? item_biases[item_id] : 0; //user_biases
		double result = global_average + user_bias + item_bias;

		if (result > GetMaxRating())
			result = GetMaxRating();
		if (result < GetMinRating())
			result = GetMinRating();

		return result;
	}

	///
	protected void RetrainUser(int user_id)
	{
		if(UpdateUsers){
			
			for(int i=0;i<ratings.ByUser().get(user_id).size();i++)
				user_biases[user_id]+=GetRatings().GetValues(ratings.ByUser().get(user_id).get(i))-global_average-item_biases[GetRatings().GetItems().get(ratings.ByUser().get(user_id).get(i))];
				
			if (ratings.ByUser().get(user_id).size()!= 0)
				user_biases[user_id] = user_biases[user_id] / (RegU + ratings.ByUser().get(user_id).size());
		}
	}

	///
	protected void RetrainItem(int item_id)
	{
		if (UpdateItems)
		{
			for(int i=0;i<ratings.ByItem().get(item_id).size();i++)
				item_biases[item_id]+=GetRatings().GetValues(ratings.ByItem().get(item_id).get(i))-global_average;
			
			if (ratings.ByItem().get(item_id).size()!=0)
				item_biases[item_id] = item_biases[item_id] / (RegI + ratings.ByItem().get(item_id).size());
		}
	}
	
	public void RetrainUsers(List<Integer> users){
		
		for(int i=0;i<users.size();i++)
			RetrainUser(users.get(i));		
		
		OptimizeUserBiases();
		
	}

	public void RetrainItems(List<Integer> items){
		
		for(int i=0;i<items.size();i++)
			RetrainItem(items.get(i));
		
		OptimizeItemBiases();
			
	}

	///
	public  void AddRating(int user_id, int item_id, double rating)
	{
		super.AddRating(user_id, item_id, rating);
		RetrainItem(item_id);
		RetrainUser(user_id);
	}

	///
	public  void UpdateRating(int user_id, int item_id, double rating)
	{
		super.UpdateRating(user_id, item_id, rating);
		RetrainItem(item_id);
		RetrainUser(user_id);
	}

	///
	public void RemoveRating(int user_id, int item_id)
	{
		super.RemoveRating(user_id, item_id);
		RetrainItem(item_id);
		RetrainUser(user_id);
	}

	///
	protected  void AddUser(int user_id)
	{
		super.AddUser(user_id);
	}
	
	public int AddRatings(List<Integer> users, List<Integer> items, List<Double> ratings){
		
		if(users==null)
			return 1;
		
		
		for(int i=0;i<users.size();i++){
			this.AddRating(users.get(i), items.get(i), ratings.get(i));
		}
		
		global_average=GetRatings().Average();	
		
		return 1;
	}

	public void AddUsers(List<Integer> users){
		
		super.AddUsers(users);
		
		double[] user_biases1 = new double[users.get(users.size()-1) + 1];
		System.arraycopy(this.user_biases,0, user_biases1,0, this.user_biases.length);
		this.user_biases = user_biases1;
	}
	
	public void AddItems(List<Integer> items){
		super.AddItems(items);
		double[] item_biases1 = new double[items.get(items.size()-1) + 1];
		System.arraycopy(this.item_biases, 0, item_biases1, 0, this.item_biases.length);
		this.item_biases = item_biases1;
	}
	
	///
	protected void AddItem(int item_id)
	{
		super.AddItem(item_id);
	}
	
	public void LoadModel(String filename){
		
		
	}
	
	
	public void SaveModel(String filename){
		
	}

	///
	public double ComputeFit()
	{
		String s= RatingEval.Evaluate(this, ratings).get("RMSE").toString();
		double temp=Double.valueOf(s);
		return temp;
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