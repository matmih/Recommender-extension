package com.rapidminer.ItemRecommendation;
import com.rapidminer.data.CorrelationMatrix;

/**
*Copyright (C) 2010, 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 22.07.2011
*/

public abstract class Knn extends ItemRecommender 
{
	 static final long serialVersionUID=3453434;
	/// <summary>The number of neighbors to take into account for prediction</summary>
	public int GetK() {return k;}
	public void setK(int value){k=value;}
	
	
	/// <summary>The number of neighbors to take into account for prediction</summary>
	protected int k = 80;

	/// <summary>Precomputed nearest neighbors</summary>
	protected Integer[][] nearest_neighbors;

	/// <summary>Correlation matrix over some kind of entity</summary>
	protected CorrelationMatrix correlation;

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