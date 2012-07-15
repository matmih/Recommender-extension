package com.rapidminer.operator.RatingPrediction;
//import java.io.IOException;
//import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.RatingPrediction.IIterativeModel;
import com.rapidminer.RatingPrediction.UserItemBaseline;
import com.rapidminer.data.Matrix;
import com.rapidminer.matrixUtils.MatrixUtils;
import com.rapidminer.operator.Annotations;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.ProcessingStep;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.LoggingHandler;

/**
*Copyright (C) 2010, 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic  (Ruðer Boškoviæ Institute) 03.08.2011
*/

public class FactorWisedMatrixFactorization extends RatingPredictor implements IIterativeModel
{
	
	   static final long serialVersionUID=3453434;
	// TODO have common base class with MatrixFactorization

	/// <summary>Matrix containing the latent user factors</summary>
	Matrix user_factors;

	/// <summary>Matrix containing the latent item factors</summary>
	Matrix item_factors;

	/// <summary>The bias (global average)</summary>
	double global_bias;
	int res_old_size;

	UserItemBaseline global_effects = new UserItemBaseline();

	int num_learned_factors;

	double[] residuals;

	/// <summary>Number of latent factors</summary>
	public int NumFactors;

	/// <summary>Number of iterations (in this case: number of latent factors)</summary>
	public int NumIter;
	
	
	public int GetNumIter(){
		return NumIter;
	}

	/// <summary>Shrinkage parameter</summary>
	/// <remarks>
	/// alpha in the Bell et al. paper
	/// </remarks>
	public double Shrinkage;

	/// <summary>Sensibility parameter (stopping criterion for parameter fitting)</summary>
	/// <remarks>
	/// epsilon in the Bell et al. paper
	/// </remarks>
	public double Sensibility;

	/// <summary>Mean of the normal distribution used to initialize the factors</summary>
	public double InitMean;

	/// <summary>Standard deviation of the normal distribution used to initialize the factors</summary>
	public double InitStdev;

	/// <summary>Default constructor</summary>
	public FactorWisedMatrixFactorization()
	{
		// set default values
		Shrinkage = 25;
		NumFactors = 10;
		NumIter = 10;
		Sensibility = 0.00001;
		InitStdev = 0.1;
	}

	/// <summary>Initialize the model data structure</summary>
	protected void InitModel()
	{
		super.InitModel();

		// init factor matrices
		user_factors = new Matrix(GetRatings().GetMaxUserID() + 1, NumFactors);
		item_factors = new Matrix(GetRatings().GetMaxItemID() + 1, NumFactors);

		// init global effects model
		global_effects.SetRatings(this.GetRatings());
		global_effects.SetMinRating(GetMinRating());
		global_effects.SetMaxRating(GetMaxRating());
	}

	///
	public void Train()
	{
		InitModel();

		global_effects.Train();
		global_bias = GetRatings().Average();

		// initialize learning data structure
		residuals = new double[GetRatings().Count()];

		// learn model parameters
		num_learned_factors = 0;
		for (int i = 0; i < NumIter; i++)
			Iterate();
	}

	///
	public void Iterate()
	{
		if (num_learned_factors >= NumFactors)
			return;

		// compute residuals
		for (int index = 0; index < GetRatings().Count(); index++)
		{
			int u = GetRatings().GetUsers().get(index);
			int i = GetRatings().GetItems().get(index);
			residuals[index] = GetRatings().GetValues(index) - Predict(u, i);
			int n_ui = Math.min(GetRatings().ByUser().get(u).size(), GetRatings().ByItem().get(i).size()); // TODO use less memory
			residuals[index] *= n_ui / (n_ui + Shrinkage);
		}

		// initialize new latent factors
		MatrixUtils.ColumnInitNormal(user_factors, InitMean, InitStdev, num_learned_factors);
		MatrixUtils.ColumnInitNormal(item_factors, InitMean, InitStdev, num_learned_factors); // TODO make configurable?

		// compute the next factor by solving many least squares problems with one variable each
		double err     = Double.MAX_VALUE / 2;
		double err_old = Double.MAX_VALUE;
		while (err / err_old < 1 - Sensibility)
		{
			{
				// TODO create only once?
				double [] user_factors_update_numerator   = new double[MaxUserID + 1];
				double [] user_factors_update_denominator = new double[MaxUserID + 1];

				// compute updates in one pass over the data
				for (int index = 0; index < GetRatings().Count(); index++)
				{
					int u = GetRatings().GetUsers().get(index);
					int i = GetRatings().GetItems().get(index);

					user_factors_update_numerator[u]   += residuals[index] * item_factors.getLocation(i, num_learned_factors);
					user_factors_update_denominator[u] += item_factors.getLocation(i, num_learned_factors) * item_factors.getLocation(i, num_learned_factors);
				}

				// update user factors
				for (int u = 0; u <= MaxUserID; u++)
					if (user_factors_update_numerator[u] != 0)
						user_factors.setLocation(u, num_learned_factors, user_factors_update_numerator[u] / user_factors_update_denominator[u]);
			}

			{
				double [] item_factors_update_numerator   = new double[MaxItemID + 1];
				double [] item_factors_update_denominator = new double[MaxItemID + 1];

				// compute updates in one pass over the data
				for (int index = 0; index < GetRatings().Count(); index++)
				{
					int u = GetRatings().GetUsers().get(index);
					int i = GetRatings().GetItems().get(index);

					item_factors_update_numerator[i]   += residuals[index] * user_factors.getLocation(u, num_learned_factors);
					item_factors_update_denominator[i] += user_factors.getLocation(u, num_learned_factors) * user_factors.getLocation(u, num_learned_factors);
				}

				// update item factors
				for (int i = 0; i <= MaxItemID; i++)
					if (item_factors_update_numerator[i] != 0)
						item_factors.setLocation(i, num_learned_factors, item_factors_update_numerator[i] / item_factors_update_denominator[i]);
			}

			err_old = err;
			err = ComputeFit();
		}

		num_learned_factors++;
	}

	/// <summary>Predict the rating of a given user for a given item</summary>
	/// <remarks>
	/// If the user or the item are not known to the recommender, the global average is returned.
	/// To avoid this behavior for unknown entities, use CanPredict() to check before.
	/// </remarks>
	/// <param name="user_id">the user ID</param>
	/// <param name="item_id">the item ID</param>
	/// <returns>the predicted rating</returns>
	public double Predict(int user_id, int item_id)
	{
		if (user_id >= user_factors.dim1)
			return global_bias;
		if (item_id >= item_factors.dim1)
			return global_bias;

		double result = global_effects.Predict(user_id, item_id) + MatrixUtils.RowScalarProduct(user_factors, user_id, item_factors, item_id);

		if (result > GetMaxRating())
			return GetMaxRating();
		if (result < GetMinRating())
			return GetMinRating();

		return result;
	}

	
	List<Integer> new_items;
	List<Integer> new_users;
	
	public void AddItems(List<Integer> items){
		super.AddItems(items);
		this.global_effects.AddItems(items);
	}
	
	public void AddUsers(List<Integer> users){
		super.AddUsers(users);
		this.global_effects.AddUsers(users);
		int old_num_rows=user_factors.dim1;
		user_factors.AddRows(MaxUserID+1);
		System.out.println(MaxUserID+1);
		System.out.println(old_num_rows);
		new_users=users;
		MatrixUtils.RowInitNormal(user_factors,old_num_rows,MaxUserID+1, InitMean, InitStdev);
	}
	
	public int AddRatings(List<Integer> users, List<Integer> items, List<Double> ratings){
		
		if(users==null)
			return 1;
		
		super.AddRatings(users, items, ratings);
		
		int old_num_rows=item_factors.dim1;
		new_items=items;
		
		item_factors.AddRows(GetRatings().GetMaxItemID()+1);
		res_old_size=residuals.length;
		// init global effects model
		global_effects.SetRatings(this.GetRatings());
		double[] residuals_new=new double[GetRatings().Count()];
		
		for(int i=0;i<residuals.length;i++)
			residuals_new[i]=residuals.length;

		residuals=residuals_new;

		
		MatrixUtils.RowInitNormal(item_factors,old_num_rows,GetRatings().GetMaxItemID()+1, InitMean, InitStdev);
		
		for (int index = GetRatings().Count()-res_old_size-1; index < GetRatings().Count(); index++)
		{
			int u = GetRatings().GetUsers().get(index);
			int i = GetRatings().GetItems().get(index);
			residuals[index] = GetRatings().GetValues(index) - Predict(u, i);
			int n_ui = Math.min(GetRatings().ByUser().get(u).size(), GetRatings().ByItem().get(i).size()); // TODO use less memory
			residuals[index] *= n_ui / (n_ui + Shrinkage);
		}
		
		return 1;
}
	
	public void RetrainItems(List<Integer> items){
		super.RetrainItems(items);
		this.global_effects.RetrainItems(items);
	}
	
	public void RetrainUsers(List<Integer> users){
		super.RetrainUsers(users);
		this.global_effects.RetrainUsers(users);
		global_bias = GetRatings().Average();
	}
	
	///
	public void SaveModel(String filename)
	{
		//not needed
	}

	///
	public void LoadModel(String filename)
	{
		//not needed
	}

	///
	public double ComputeFit()
	{
		return com.rapidminer.eval.RatingEval.Evaluate(this, ratings).get("RMSE");
	}

	///
	public String ToString()
	{
		return String.format(
							 "FactorWiseMatrixFactorization num_factors={0} shrinkage={1} sensibility={2}  init_mean={3} init_stdev={4} num_iter={5}",
							 NumFactors, Shrinkage, Sensibility, InitMean, InitStdev, NumIter);
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
