package com.rapidminer.operator.RatingPrediction;

/**
*Copyright (C) 2010, 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 22.07.2011
*/

public interface IRecommender
{
	/// <summary>Predict rating or score for a given user-item combination</summary>
	/// <remarks></remarks>
	/// <param name="user_id">the user ID</param>
	/// <param name="item_id">the item ID</param>
	/// <returns>the predicted score/rating for the given user-item combination</returns>
	double Predict(int user_id, int item_id);

	/// <summary>Check whether a useful prediction can be made for a given user-item combination</summary>
	/// <remarks></remarks>
	/// <param name="user_id">the user ID</param>
	/// <param name="item_id">the item ID</param>
	/// <returns>true if a useful prediction can be made, false otherwise</returns>
	boolean CanPredict(int user_id, int item_id);

	/// <summary>Learn the model parameters of the recommender from the training data</summary>
	/// <remarks></remarks>
	void Train();

	/// <summary>Save the model parameters to a file</summary>
	/// <remarks></remarks>
	/// <param name="filename">the name of the file to write to</param>
	void SaveModel(String filename);

	/// <summary>Get the model parameters from a file</summary>
	/// <remarks></remarks>
	/// <param name="filename">the name of the file to read from</param>
	void LoadModel(String filename);

	/// <summary>Return a string representation of the recommender</summary>
	/// <remarks>
	/// The ToString() method of recommenders should list the class name and all hyperparameters, separated by space characters.
	/// </remarks>
	String ToString();
}
