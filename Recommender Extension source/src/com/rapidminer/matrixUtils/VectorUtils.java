package com.rapidminer.matrixUtils;
import java.util.List;


/**
*Copyright (C) 2010, 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 01.08.2011
*/


public class VectorUtils
{

	/// <summary>Compute the Euclidean norm of a collection of doubles</summary>
	/// <param name="vector">the vector to compute the norm for</param>
	/// <returns>the Euclidean norm of the vector</returns>
	public static double EuclideanNorm(double[] vector)
	{
		double sum = 0;
		
		for(int i=0;i<vector.length;i++){
			double v=vector[i];
			sum += Math.pow(v, 2);
		}
		
		return Math.sqrt(sum);
	}

	/// <summary>Compute the L1 norm of a collection of doubles</summary>
	/// <param name="vector">the vector to compute the norm for</param>
	/// <returns>the L1 norm of the vector</returns>
	public static double L1Norm(List<Double> vector)
	{
		double sum = 0;
		for(int i=0;i<vector.size();i++){
			double v=vector.get(i);
			sum += Math.abs(v);
		}
		return sum;
	}

	/// <summary>Initialize a collection of doubles with values from a normal distribution</summary>
	/// <param name="vector">the vector to initialize</param>
	/// <param name="mean">the mean of the normal distribution</param>
	/// <param name="stdev">the standard deviation of the normal distribution</param>
	static public void InitNormal(List<Double> vector, double mean, double stdev)
	{
		com.rapidminer.utils.Random rnd = com.rapidminer.utils.Random.GetInstance();
		for (int i = 0; i < vector.size(); i++)
			vector.set(i, rnd.NextNormal(mean, stdev));
	}
}