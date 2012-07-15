package com.rapidminer.operator.RatingPrediction;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.RatingPrediction.IIterativeModel;
import com.rapidminer.data.Matrix;
import com.rapidminer.eval.RatingEval;
import com.rapidminer.matrixUtils.MatrixUtils;
import com.rapidminer.matrixUtils.VectorUtils;
import com.rapidminer.operator.Annotations;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.ProcessingStep;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.LoggingHandler;


/**
*Copyright (C) 2010 Zeno Gantner, Steffen Rendle, Christoph Freudenthaler
*Copyright (C) 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 28.07.2011
*/

public class MatrixFactorization  extends RatingPredictor implements IIterativeModel{

	  static final long serialVersionUID=3453434;
		/// <summary>Matrix containing the latent user factors</summary>
		protected Matrix user_factors;

		/// <summary>Matrix containing the latent item factors</summary>
		protected Matrix item_factors;

		/// <summary>The bias (global average)</summary>
		protected double global_bias;

		/// <summary>Mean of the normal distribution used to initialize the factors</summary>
		public double InitMean;

		/// <summary>Standard deviation of the normal distribution used to initialize the factors</summary>
		public double InitStdev;

		/// <summary>Number of latent factors</summary>
		public int NumFactors;

		/// <summary>Learn rate</summary>
		public double LearnRate;

		/// <summary>Regularization parameter</summary>
		public double Regularization;

		/// <summary>Number of iterations over the training data</summary>
		public int NumIter;

		
		public int GetNumIter(){
			return NumIter;
		}
		
		/// <summary>Default constructor</summary>
		public MatrixFactorization()
		{
			// set default values
			Regularization = 0.015;
			LearnRate = 0.01;
			NumIter = 30;
			InitStdev = 0.1;//0.4
			NumFactors = 10;
		}

		/// <summary>Initialize the model data structure</summary>
		protected void InitModel()
		{
			super.InitModel();

			// init factor matrices
			user_factors = new Matrix(GetRatings().GetMaxUserID() + 1, NumFactors);
			item_factors = new Matrix(GetRatings().GetMaxItemID() + 1, NumFactors);
			MatrixUtils.RowInitNormal(user_factors, InitMean, InitStdev);
			MatrixUtils.RowInitNormal(item_factors, InitMean, InitStdev);	
		}

		///
		public void Train()
		{
			InitModel();

			// learn model parameters
			global_bias = GetRatings().Average();
			LearnFactors(GetRatings().RandomIndex(), true, true);
		}

		///
		public void Iterate()
		{
			
			Iterate(GetRatings().RandomIndex(), true, true);
		}

		/// <summary>Updates the latent factors on a user</summary>
		/// <param name="user_id">the user ID</param>
		public void RetrainUser(int user_id)
		{
			if (UpdateUsers)
			{
				MatrixUtils.RowInitNormal(user_factors, InitMean, InitStdev, user_id);
				
				LearnFactors(GetRatings().ByUser().get((int)user_id), true, false);
			}
		}

		/// <summary>Updates the latent factors of an item</summary>
		/// <param name="item_id">the item ID</param>
		public  void RetrainItem(int item_id)
		{
			if (UpdateItems)
			{
				MatrixUtils.RowInitNormal(item_factors, InitMean, InitStdev, item_id);
				LearnFactors(GetRatings().ByItem().get((int)item_id), false, true);
			}
		}

public int AddRatings(List<Integer> users, List<Integer> items, List<Double> ratings){
			
			if(users==null)
				return 1;
			
			super.AddRatings(users, items, ratings);
			
			return 1;
}
		
		public void RetrainUsers(List<Integer> users){
			
			for(int i=0;i<users.size();i++)
				RetrainUser(users.get(i));		
		}

		public void RetrainItems(List<Integer> items){
			
			for(int i=0;i<items.size();i++)
				RetrainItem(items.get(i));
		}
		
		/// <summary>Iterate once over rating data and adjust corresponding factors (stochastic gradient descent)</summary>
		/// <param name="rating_indices">a list of indices pointing to the ratings to iterate over</param>
		/// <param name="update_user">true if user factors to be updated</param>
		/// <param name="update_item">true if item factors to be updated</param>
		protected void Iterate(List<Integer> rating_indices, boolean update_user, boolean update_item)
		{
			
			for(int i1=0;i1<rating_indices.size();i1++){
			
				int index=rating_indices.get(i1);
			
				int u = ratings.GetUsers().get(index);
				int i = ratings.GetItems().get(index);

				double p = Predict(u, i, false);
				double err = ratings.GetValues(index) - p;

				 // Adjust factors
				 for (int f = 0; f < NumFactors; f++)
				 {
					double u_f = user_factors.getLocation(u, f);
					double i_f = item_factors.getLocation(i, f);

					// compute factor updates
					double delta_u = err * i_f - Regularization * u_f;
					double delta_i = err * u_f - Regularization * i_f;
					
					// if necessary, apply updates
					if (update_user)
						MatrixUtils.Inc(user_factors, u, f, LearnRate * delta_u);
					if (update_item)
						MatrixUtils.Inc(item_factors, i, f, LearnRate * delta_i);
				 }	 
			}
		}

		private void LearnFactors(List<Integer> rating_indices, boolean update_user, boolean update_item)
		{
			for (int current_iter = 0; current_iter < NumIter; current_iter++)
				Iterate(rating_indices, update_user, update_item);
		}

		///
		protected double Predict(int user_id, int item_id, boolean bound)
		{
			double result = global_bias + MatrixUtils.RowScalarProduct(user_factors, user_id, item_factors, item_id);

			if (bound)
			{
				if (result > GetMaxRating())
					return GetMaxRating();
				if (result < GetMinRating())
					return GetMinRating();
			}
			return result;
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

			return Predict(user_id, item_id, true);
		}

		///
		public void AddRating(int user_id, int item_id, double rating)
		{
			super.AddRating(user_id, item_id, rating);
		}

		///
		public void UpdateRating(int user_id, int item_id, double rating)
		{
			super.UpdateRating(user_id, item_id, rating);
			RetrainUser(user_id);
			RetrainItem(item_id);
		}

		///
		public void RemoveRating(int user_id, int item_id)
		{
			super.RemoveRating(user_id, item_id);
			RetrainUser(user_id);
			RetrainItem(item_id);
		}

		///
		protected  void AddUser(int user_id)
		{
			super.AddUser(user_id);
			user_factors.AddRows(user_id + 1);
		}

		///
		protected void AddItem(int item_id)
		{
			super.AddItem(item_id);
			item_factors.AddRows(item_id + 1);
		}

		public void AddUsers(List<Integer> users){
			
					super.AddUsers(users);
			
					user_factors.AddRows(users.get(users.size()-1)+1);
			
			
		}
		
		public void AddItems(List<Integer> items){
			
				super.AddItems(items);
				
				item_factors.AddRows(items.get(items.size()-1)+1);
			
		}
		
		///
		public void RemoveUser(int user_id)
		{
			super.RemoveUser(user_id);

			// set user factors to zero
			user_factors.SetRowToOneValue(user_id, 0);
		}

		///
		public void RemoveItem(int item_id)
		{
			super.RemoveItem(item_id);

			// set item factors to zero
			item_factors.SetRowToOneValue(item_id, 0);
		}

		///
		public void SaveModel(String filename)
		{

		}

		///
		public void LoadModel(String filename)
		{

		}

		///
		public double ComputeFit()
		{
			return RatingEval.Evaluate(this, ratings).get("RMSE");
		}

		/// <summary>Compute the regularized loss</summary>
		/// <returns>the regularized loss</returns>
		public double ComputeLoss()
		{
			double loss = 0;
			for (int i = 0; i < ratings.Count(); i++)
			{
				int user_id = ratings.GetUsers().get(i);
				int item_id = ratings.GetItems().get(i);
				loss += Math.pow(Predict(user_id, item_id) - ratings.GetValues(i), 2);
			}

			for (int u = 0; u <= MaxUserID; u++)
				loss += ratings.CountByUser()[u] * Regularization * Math.pow(VectorUtils.EuclideanNorm(user_factors.GetRow(u)), 2);

			for (int i = 0; i <= MaxItemID; i++)
				loss += ratings.CountByItem()[i] * Regularization * Math.pow(VectorUtils.EuclideanNorm(item_factors.GetRow(i)), 2);

			return loss;
		}

		///
		public String ToString()
		{
			return String.format(/*CultureInfo.InvariantCulture,*/
								 "MatrixFactorization num_factors={0} regularization={1} learn_rate={2} num_iter={3} init_mean={4} init_stdev={5}",
								 NumFactors, Regularization, LearnRate, NumIter, InitMean, InitStdev);
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
	
