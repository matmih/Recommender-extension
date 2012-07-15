package com.rapidminer.ItemRecommendation;
import com.rapidminer.RatingPrediction.IIterativeModel;
import com.rapidminer.data.Matrix;
import com.rapidminer.matrixUtils.MatrixUtils;

/**
*Copyright (C) 2010 Steffen Rendle, Zeno Gantner, Christoph Freudenthaler
*Copyright (C) 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 01.08.2011
*/

public abstract class MF extends ItemRecommender implements IIterativeModel
{
	 static final long serialVersionUID=3453434;
	/// <summary>Latent user factor matrix</summary>
	protected Matrix user_factors;
	/// <summary>Latent item factor matrix</summary>
	protected Matrix item_factors;

	/// <summary>Mean of the normal distribution used to initialize the latent factors</summary>
	public double InitMean;

	/// <summary>Standard deviation of the normal distribution used to initialize the latent factors</summary>
	public double InitStdev;

	/// <summary>Number of latent factors per user/item</summary>
	public int GetNumFactors()
	{  
		 return  num_factors; }
	
	public void SetNumFactors(int value){
	 num_factors =  value; } 
	/// <summary>Number of latent factors per user/item</summary>
	protected int num_factors = 10;

	/// <summary>Number of iterations over the training data</summary>
	public int NumIter; 

	/// <summary>Default constructor</summary>
	public MF()
	{
		NumIter = 30;
		InitMean = 0;
		InitStdev = 0.1;
	}

	// TODO push upwards in class hierarchy
	///
	protected void InitModel()
	{
		user_factors = new Matrix(MaxUserID + 1, GetNumFactors());
		item_factors = new Matrix(MaxItemID + 1, GetNumFactors());

		MatrixUtils.RowInitNormal(user_factors, InitMean, InitStdev);
		MatrixUtils.RowInitNormal(item_factors, InitMean, InitStdev);
	}

	///
	public void Train()
	{
		InitModel();

		for (int i = 0; i < NumIter; i++)
			Iterate();
	}

	/// <summary>Iterate once over the data</summary>
	public abstract void Iterate();

	/// <summary>Computes the fit (optimization criterion) on the training data</summary>
	/// <returns>a double representing the fit, lower is better</returns>
	public abstract double ComputeFit();

	/// <summary>Predict the weight for a given user-item combination</summary>
	/// <remarks>
	/// If the user or the item are not known to the recommender, zero is returned.
	/// To avoid this behavior for unknown entities, use CanPredict() to check before.
	/// </remarks>
	/// <param name="user_id">the user ID</param>
	/// <param name="item_id">the item ID</param>
	/// <returns>the predicted weight</returns>
	public double Predict(int user_id, int item_id)
	{
		if ((user_id < 0) || (user_id >= user_factors.dim1))
		{
			System.out.println("user is unknown: " + user_id);
			return 0;
		}
		if ((item_id < 0) || (item_id >= item_factors.dim1))
		{
			System.out.println("item is unknown: " + item_id);
			return 0;
		}

		return MatrixUtils.RowScalarProduct(user_factors, user_id, item_factors, item_id);
	}
}
