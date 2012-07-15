package com.rapidminer.RatingPrediction;

/**
*Copyright (C) 2010, 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 23.07.2011
*/

public interface IIterativeModel
{
	/// <summary>Number of iterations to run the training</summary>
	int GetNumIter();

	/// <summary>Run one iteration (= pass over the training data)</summary>
	void Iterate();

	/// <summary>Compute the fit (RMSE) on the training data</summary>
	/// <returns>the fit (RMSE) on the training data according to the optimization criterion; -1 if not implemented</returns>
	double ComputeFit();
}
