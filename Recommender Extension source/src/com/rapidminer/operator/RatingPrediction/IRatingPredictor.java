package com.rapidminer.operator.RatingPrediction;

import com.rapidminer.operator.IOObject;

/**
*Copyright (C) 2010 Steffen Rendle,Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 22.07.2011
*/

public interface IRatingPredictor extends IRecommender, /*Model*/IOObject
{
	/// <summary>The max rating value</summary>
	//double MaxRating=0;
	public double GetMaxRating();
	
	/// <summary>The min rating value</summary>
	//double MinRating=0;
	public double GetMinRating();

	///
	void AddRating(int user_id, int item_id, double rating);
	///
	void UpdateRating(int user_id, int item_id, double rating);
	///
	void RemoveRating(int user_id, int item_id);
	///
	void RemoveUser(int user_id);
	///
	void RemoveItem(int item_id);
}
