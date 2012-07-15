package com.rapidminer.eval;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.rapidminer.data.IRatings;
import com.rapidminer.example.Attribute;
//import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
//import com.rapidminer.operator.UserError;
import com.rapidminer.operator.RatingPrediction.IRatingPredictor;


/**
*Copyright (C) 2010 Steffen Rendle, Zeno Gantner
*Copyright (C) 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 28.07.2011
*/

public class RatingEval
{
	/// <summary>the evaluation measures for rating prediction offered by the class</summary>
	static public HashSet<String> Measures()
	{
			HashSet<String> a= new HashSet<String>();
			a.add("RMSE"); a.add("MAE"); a.add("NMAE");
			return a;
	}

	/// <summary>Write rating prediction results to STDOUT</summary>
	/// <param name="result">the output of the Evaluate() method</param>
	static public void DisplayResults(Map<String, Double> result)
	{
     System.out.print("RMSE"+" "+ result.get("RMSE")+" "+"MAE"+" "+result.get("MAE")+" NMAE "+result.get("NMAE"));
	}

	/// <summary>Evaluates a rating predictor for RMSE, MAE, and NMAE</summary>
	/// <remarks>
	/// For NMAE, see "Eigentaste: A Constant Time Collaborative Filtering Algorithm" by Goldberg et al.
	/// </remarks>
	/// <param name="recommender">rating predictor</param>
	/// <param name="ratings">Test cases</param>
	/// <returns>a Dictionary containing the evaluation results</returns>
	static public Map<String,Double> Evaluate(IRatingPredictor recommender, IRatings ratings)
	{
		double rmse = 0;
		double mae  = 0;

		
		for (int index = 0; index < ratings.Count(); index++)
		{
			double error = (recommender.Predict(ratings.GetUsers().get(index), ratings.GetItems().get(index)) - ratings.GetValues(index));
			
			rmse += error * error;
			mae  += Math.abs(error);
		}
		mae  = mae / ratings.Count();
		rmse = Math.sqrt(rmse / ratings.Count());

		Map<String,Double> result = new java.util.HashMap<String,Double>();
		result.put("RMSE", rmse);
		result.put("MAE",  mae);
		result.put("NMAE", mae / (recommender.GetMaxRating() - recommender.GetMinRating()));
		return result;
	}
	
	
	static public Map<String,Double> Evaluate(List<Double> ratings, List<Double> prediction, double maxRating, double minRating)
	{
		double rmse = 0;
		double mae  = 0;

		
		if(ratings.size()!=prediction.size())
			throw new IllegalArgumentException("Rating and prediction vector must be of a same size!");
		
		for (int index = 0; index < ratings.size(); index++)
		{
			double error = (prediction.get(index) - ratings.get(index));

			rmse += error * error;
			mae  += Math.abs(error);
		}
		mae  = mae / ratings.size();
		rmse = Math.sqrt(rmse / ratings.size());

		Map<String,Double> result = new java.util.HashMap<String,Double>();
		result.put("RMSE", rmse);
		result.put("MAE",  mae);
		result.put("NMAE", mae / (maxRating - minRating));
		return result;
	}
	
	static public Map<String,Double> Evaluate(ExampleSet exampleSet, int min, int max)
	{
		double rmse = 0;
		double mae  = 0;
		
	 
		Attributes Att1=exampleSet.getAttributes();
		Attribute r=Att1.getLabel();
		Attribute pr=Att1.get("prediction");
		
		for (Example example : exampleSet) {
			
			double rating=example.getValue(r);
			double prediction=example.getValue(pr);
			
			double error = (prediction-rating);
			rmse += error * error;
			mae  += Math.abs(error);
		}
		
		mae  = mae / exampleSet.size();
		rmse = Math.sqrt(rmse / exampleSet.size());

		Map<String,Double> result = new java.util.HashMap<String,Double>();
		result.put("RMSE", rmse);
		result.put("MAE",  mae);
		result.put("NMAE", mae / (max - min));
		return result;
	}
}
