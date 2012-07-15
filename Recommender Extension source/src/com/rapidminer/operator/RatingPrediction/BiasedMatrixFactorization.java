package com.rapidminer.operator.RatingPrediction;
import java.util.List;

import com.rapidminer.matrixUtils.MatrixUtils;
import com.rapidminer.matrixUtils.VectorUtils;

/**
*Copyright (C) 2010  Steffen Rendle, Zeno Gantner
*Copyright (C) 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 28.07.2011
*/

public class BiasedMatrixFactorization extends MatrixFactorization
{
	   static final long serialVersionUID=3453434;
	/// <summary>regularization constant for the bias terms</summary>
	public double BiasReg;

	/// <summary>regularization constant for the user factors</summary>
	public double RegU;

	/// <summary>regularization constant for the item factors</summary>
	public double RegI;

	///
	public void SetRegularization(double value)
	{
			super.Regularization = value;
			RegU = value;
			RegI = value;
	}

	/// <summary>Use bold driver heuristics for learning rate adaption</summary>
	/// <remarks>
	/// See
	/// Rainer Gemulla, Peter J. Haas, Erik Nijkamp, Yannis Sismanis:
	/// Large-Scale Matrix Factorization with Distributed Stochastic Gradient Descent,
	/// 2011
	/// </remarks>
	public boolean BoldDriver;
	// TODO use for incremental updates as well

	/// <summary>Loss for the last iteration, used by bold driver heuristics</summary>
	double last_loss = Double.NEGATIVE_INFINITY;

	/// <summary>the user biases</summary>
	protected double[] user_bias;
	/// <summary>the item biases</summary>
	protected double[] item_bias;

	/// <summary>Default constructor</summary>
	public BiasedMatrixFactorization()
	{
		BiasReg = 0.0001;
	}

	///
	protected void InitModel()
	{
		super.InitModel();

		user_bias = new double[MaxUserID + 1];
		for (int u = 0; u <= MaxUserID; u++)
			user_bias[u] = 0;
		item_bias = new double[MaxItemID + 1];
		for (int i = 0; i <= MaxItemID; i++)
			item_bias[i] = 0;

		if (BoldDriver)
			last_loss = ComputeLoss();
	}

	///
	public void Train()
	{
		InitModel();

		// compute global average
		global_bias = ratings.Average();
		
		for (int current_iter = 0; current_iter < NumIter; current_iter++)
			Iterate();
	}

	///
	public void Iterate()
	{
		super.Iterate();

		if (BoldDriver)
		{
			double loss = ComputeLoss();

			if (loss > last_loss)
				LearnRate *= 0.5;
			else if (loss < last_loss)
				LearnRate *= 1.05;

			last_loss = loss;
		}
	}

	///
	protected  void Iterate(List<Integer> rating_indices, boolean update_user, boolean update_item)
	{
			
		double rating_range_size = GetMaxRating() - GetMinRating();

		for(int i1=0;i1<rating_indices.size();i1++){
		     int index=rating_indices.get(i1);	
		
			int u = ratings.GetUsers().get(index);
			int i = ratings.GetItems().get(index);

			double dot_product = user_bias[u] + item_bias[i] + MatrixUtils.RowScalarProduct(user_factors, u, item_factors, i);
			

			double sig_dot = 1 / (1 + Math.exp(-dot_product));
				
			double p = GetMinRating() + sig_dot * rating_range_size;
			double err = ratings.GetValues(index) - p;
			
			double gradient_common = err * sig_dot * (1 - sig_dot) * rating_range_size;
			
			// adjust biases
			if (update_user)
				user_bias[u] += LearnRate * (gradient_common - BiasReg * user_bias[u]);
			if (update_item)
				item_bias[i] += LearnRate * (gradient_common - BiasReg * item_bias[i]);

			// adjust latent factors
			for (int f = 0; f < NumFactors; f++)
			{
			 	double u_f = user_factors.getLocation(u, f);
				double i_f = item_factors.getLocation(i, f);

				if (update_user)
				{
					double delta_u = gradient_common * i_f - RegU * u_f;
					MatrixUtils.Inc(user_factors, u, f, LearnRate * delta_u);
					// this is faster (190 vs. 260 seconds per iteration on Netflix w/ k=30) than
					//    user_factors[u, f] += learn_rate * delta_u;
				}
				if (update_item)
				{
					double delta_i = gradient_common * u_f - RegI * i_f;
					MatrixUtils.Inc(item_factors, i, f, LearnRate * delta_i);
				}
			}
		}
	}

	///
	public double Predict(int user_id, int item_id)
	{
		if (user_id >= user_factors.dim1 || item_id >= item_factors.dim1)
			return global_bias;

		double score = user_bias[user_id] + item_bias[item_id] + MatrixUtils.RowScalarProduct(user_factors, user_id, item_factors, item_id);
		
		return GetMinRating() + ( 1 / (1 + Math.exp(-score)) ) * (GetMaxRating() - GetMinRating());
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
	protected void AddUser(int user_id)
	{
		super.AddUser(user_id);

		// create new user bias array
		double[] user_bias = new double[user_id + 1];
        System.arraycopy(this.user_bias, 0, user_bias, 0, this.user_bias.length);
		this.user_bias = user_bias;
	}
	
	public void AddUsers(List<Integer> users)
	{
		super.AddUsers(users);
		
		double[] user_bias=new double[users.get(users.size()-1)+1];//this.MaxUserID+1
		System.arraycopy(this.user_bias, 0, user_bias, 0, this.user_bias.length);
		this.user_bias=user_bias;
	}
	
	public void AddItems(List<Integer> items)
	{
		super.AddItems(items);
		
		double[] item_bias=new double[items.get(items.size()-1)+1];//this.MaxItemID+1
		System.arraycopy(this.item_bias, 0, item_bias, 0, this.item_bias.length);
		this.item_bias=item_bias;
	}

	///
	protected void AddItem(int item_id)
	{
		super.AddItem(item_id);

		// create new item bias array
		double[] item_bias = new double[item_id + 1];
	    System.arraycopy(this.item_bias, 0, item_bias, 0, this.item_bias.length);
		this.item_bias = item_bias;
	}

	///
	public void RetrainUser(int user_id)
	{
		user_bias[user_id] = 0;
		super.RetrainUser(user_id);
	}

	///
	public void RetrainItem(int item_id)
	{
		item_bias[item_id] = 0;
		super.RetrainItem(item_id);
	}
	
	public void RetrainUsers(List<Integer> users){
		
		for(int i=0;i<users.size();i++)
			RetrainUser(users.get(i));		
		//Iterate();
	}

	public void RetrainItems(List<Integer> items){
		
		for(int i=0;i<items.size();i++)
			RetrainItem(items.get(i));
	}
	
	public int AddRatings(List<Integer> users, List<Integer> items, List<Double> ratings){
		
		if(users==null)
			return 1;
		
		super.AddRatings(users, items, ratings);
		
		return 1;
	}
	
	///
	public void RemoveUser(int user_id)
	{
		super.RemoveUser(user_id);

		user_bias[user_id] = 0;
	}

	///
	public void RemoveItem(int item_id)
	{
		super.RemoveItem(item_id);

		item_bias[item_id] = 0;
	}

	///
	public double ComputeLoss()
	{
		double square_loss = 0;
		for (int i = 0; i < ratings.Count(); i++)
		{
			int user_id = ratings.GetUsers().get(i);
			int item_id = ratings.GetItems().get(i);
			square_loss += Math.pow(Predict(user_id, item_id) - ratings.GetValues(i), 2);
		}

		double complexity = 0;
		for (int u = 0; u <= MaxUserID; u++)
		{
			complexity += ratings.CountByUser()[u] * RegU * Math.pow(VectorUtils.EuclideanNorm(user_factors.GetRow(u)), 2);
			complexity += ratings.CountByUser()[u] * BiasReg * Math.pow(user_bias[u], 2);
		}
		for (int i = 0; i <= MaxItemID; i++)
		{
			complexity += ratings.CountByItem()[i] * RegI * Math.pow(VectorUtils.EuclideanNorm(item_factors.GetRow(i)), 2);
			complexity += ratings.CountByItem()[i] * BiasReg * Math.pow(item_bias[i], 2);
		}

		return square_loss + complexity;
	}

	///
	public String ToString()
	{
		return String.format(
							 "BiasedMatrixFactorization num_factors={0} bias_reg={1} reg_u={2} reg_i={3} learn_rate={4} num_iter={5} bold_driver={6} init_mean={7} init_stdev={8}",
							 NumFactors, BiasReg, RegU, RegI, LearnRate, NumIter, BoldDriver, InitMean, InitStdev);
	}
}
