package com.rapidminer.operator.RatingPrediction;

import com.rapidminer.RatingPrediction.UserItemBaseline;
import com.rapidminer.data.CorrelationMatrix;


/**
*Copyright (C) 2010, 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 22.07.2011
*/

public abstract class rKnn extends UserItemBaseline
{
	 static final long serialVersionUID=3453434;
	/// <summary>Number of neighbors to take into account for predictions</summary>
	public int K;
	
	public int GetK(){
		
	return K;
	}
	
	public void SetK(int value){
		
	K=value;
	}


	/// <summary>Correlation matrix over some kind of entity</summary>
	protected CorrelationMatrix correlation;

	/// <summary>Create a new KNN recommender</summary>
	public rKnn()
	{
		RegU = 10;
		RegI = 5;
	}

	///
	public  void SaveModel(String filename)
	{
		//not needed
	}

	///
	public void LoadModel(String filename)
	{
		//not needed
	}
}