package com.rapidminer.eval;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.rapidminer.ItemRecommendation.ItemRecommender;
import com.rapidminer.data.IPosOnlyFeedback;

/**
*Copyright (C) 2010 Zeno Gantner,Steffen Rendle
*Copyright (C) 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 29.07.2011
*/

public class ItemPredictionEval
{
	/// <summary>the evaluation measures for item prediction offered by the class</summary>
	static public HashSet<String> Measures()
	{
	
			HashSet<String> a=new HashSet<String>();
			a.add("AUC"); a.add("prec@5"); a.add("prec@10"); a.add("prec@15"); a.add("NDCG"); a.add("MAP");
			
			return a;
	}

	/// <summary>Display item prediction results</summary>
	/// <param name="result">the result dictionary</param>
	static public void DisplayResults(Map<String, Double> result)
	{
		System.out.print("AUC"+" "+ result.get("AUC")+" "+"prec@5"+" "+result.get("prec@5")+" prec@10 "+" "+result.get("prec@10")+" "+"prec@15"+" "+result.get("prec@15")+" "+"NDCG"+" "+result.get("NDCG")+" "+"MAP"+" "+result.get("MAP"));
	}

	/// <summary>Evaluation for rankings of items</summary>
	/// <remarks>
	/// User-item combinations that appear in both sets are ignored for the test set, and thus in the evaluation.
	/// The evaluation measures are listed in the ItemPredictionMeasures property.
	/// Additionally, 'num_users' and 'num_items' report the number of users that were used to compute the results
	/// and the number of items that were taken into account.
	///
	/// Literature:
	///   C. Manning, P. Raghavan, H. Schütze: Introduction to Information Retrieval, Cambridge University Press, 2008
	/// </remarks>
	/// <param name="recommender">item recommender</param>
	/// <param name="test">test cases</param>
	/// <param name="train">training data</param>
	/// <param name="relevant_users">a collection of integers with all relevant users</param>
	/// <param name="relevant_items">a collection of integers with all relevant items</param>
	/// <returns>a dictionary containing the evaluation results</returns>
	static public Map<String, Double> Evaluate(
		ItemRecommender recommender,
		IPosOnlyFeedback test,
		IPosOnlyFeedback train,
	    List<Integer> relevant_users,
		List<Integer> relevant_items)
	{
		if (train.Overlap(test) > 0)
			System.out.println("WARNING: Overlapping train and test data");

		// compute evaluation measures
		double auc_sum     = 0;
		double map_sum     = 0;
		double prec_5_sum  = 0;
		double prec_10_sum = 0;
		double prec_15_sum = 0;
		double ndcg_sum    = 0;
		int num_users      = 0;

		for(int i1=0;i1<relevant_users.size();i1++){
			
		int user_id=relevant_users.get(i1);
			com.rapidminer.data.CompactHashSet<Integer> correct_items = new com.rapidminer.data.CompactHashSet<Integer>(test.GetUserMatrix().getLocation(user_id));
			correct_items.retainAll(relevant_items);
			
			// the number of items that are really relevant for this user
			com.rapidminer.data.CompactHashSet<Integer> relevant_items_in_train = new com.rapidminer.data.CompactHashSet<Integer>(train.GetUserMatrix().getLocation(user_id));
			relevant_items_in_train.retainAll(relevant_items);
			int num_eval_items = relevant_items.size() - relevant_items_in_train.size();

			// skip all users that have 0 or #relevant_items test items
			if (correct_items.size() == 0)
				continue;
			if (num_eval_items - correct_items.size() == 0)
				continue;

			num_users++;
			int[] prediction = ItemPrediction.PredictItems(recommender, user_id, relevant_items);

			auc_sum     += AUC(prediction, correct_items, train.GetUserMatrix().getLocation(user_id));
			map_sum     += MAP(prediction, correct_items, train.GetUserMatrix().getLocation(user_id));
			ndcg_sum    += NDCG(prediction, correct_items, train.GetUserMatrix().getLocation(user_id));
			prec_5_sum  += PrecisionAt(prediction, correct_items, train.GetUserMatrix().getLocation(user_id),  5);
			prec_10_sum += PrecisionAt(prediction, correct_items, train.GetUserMatrix().getLocation(user_id), 10);
			prec_15_sum += PrecisionAt(prediction, correct_items, train.GetUserMatrix().getLocation(user_id), 15);

			if (prediction.length != relevant_items.size())
				throw new IllegalArgumentException("Not all items have been ranked.");
		}

		Map<String,Double> result = new java.util.HashMap<String, Double>();

		result.put("AUC",     auc_sum / num_users);
		result.put("MAP",     map_sum / num_users);
		result.put("NDCG",    ndcg_sum / num_users);
		result.put("prec@5",  prec_5_sum / num_users);
		result.put("prec@10", prec_10_sum / num_users);
		result.put("prec@15", prec_15_sum / num_users);
		result.put("num_users", (double)num_users);
		result.put("num_items", (double)relevant_items.size());

		return result;
	}
	
	public static double AUC(int[] ranked_items, com.rapidminer.data.CompactHashSet<Integer> correct_items)
	{
		return AUC(ranked_items, correct_items, new ArrayList<Integer>());
	}
	
	public static double AUC(int[] ranked_items, com.rapidminer.data.CompactHashSet<Integer> correct_items, List<Integer> ignore_items)
	{
	
        List<Integer> temp=new ArrayList<Integer>();
        
        for(int i=0;i<ignore_items.size();i++)
        	temp.add(ignore_items.get(i));
        
        List<Integer> temp1=new ArrayList<Integer>();
        
        for(int i1=0;i1<ranked_items.length;i1++)
        	temp1.add(ranked_items[i1]);
        
        
		  temp.retainAll(temp1);
		  
			int num_eval_items = ranked_items.length - temp.size();
			int num_eval_pairs = (num_eval_items - correct_items.size()) * correct_items.size();
			
			int num_correct_pairs = 0;
			int hit_count         = 0;
			
			for(int i=0;i<ranked_items.length;i++){
				
			int item_id=ranked_items[i];
			
				if (ignore_items.contains(item_id))
					continue;

				if (!correct_items.contains(item_id))
					num_correct_pairs += hit_count;
				else
					hit_count++;
			}
			
			return ((double) num_correct_pairs) / num_eval_pairs;
	}
	
	
	public static double MAP(int[] ranked_items, com.rapidminer.data.CompactHashSet<Integer> correct_items)
	{
		return MAP(ranked_items, correct_items, new ArrayList<Integer>());
	}
	
	public static double MAP(int[] ranked_items, com.rapidminer.data.CompactHashSet<Integer> correct_items, List<Integer> ignore_items)
	{
		int hit_count       = 0;
		double avg_prec_sum = 0;
		int left_out        = 0;

		for (int i = 0; i < ranked_items.length; i++)
		{
			int item_id = ranked_items[i];
			if (ignore_items.contains(item_id))
			{
				left_out++;
				continue;
			}

			if (!correct_items.contains(item_id))
				continue;

			hit_count++;

			avg_prec_sum += (double) hit_count / (i + 1 - left_out);
		}

		if (hit_count != 0)
			return avg_prec_sum / hit_count;
		else
			return 0;
	}
	
	
	
	
	public static double NDCG(int[] ranked_items, com.rapidminer.data.CompactHashSet<Integer> correct_items)
	{
		return NDCG(ranked_items, correct_items, new ArrayList<Integer>());
	}

	public static double NDCG(int[] ranked_items, com.rapidminer.data.CompactHashSet<Integer> correct_items, List<Integer> ignore_items)
	{
		double dcg   = 0;
		double idcg  = ComputeIDCG(correct_items.size());
		int left_out = 0;

		for (int i = 0; i < ranked_items.length; i++)
		{
			int item_id = ranked_items[i];
			if (ignore_items.contains(item_id))
			{
				left_out++;
				continue;
			}

			if (!correct_items.contains(item_id))
				continue;

			// compute NDCG part
			int rank = i + 1 - left_out;
			double log=Math.log10(rank+1)/Math.log10(2);
			dcg += 1 / log;
		}

		return dcg / idcg;
	}
	
	public static double PrecisionAt(int[] ranked_items, com.rapidminer.data.CompactHashSet<Integer> correct_items, int n)
	{
		return PrecisionAt(ranked_items, correct_items, new ArrayList<Integer>(), n);
	}
	
	
	public static double PrecisionAt(int[] ranked_items, com.rapidminer.data.CompactHashSet<Integer> correct_items, List<Integer> ignore_items, int n)
	{
		if (n < 1)
			throw new IllegalArgumentException("N must be at least 1.");

		int hit_count = 0;
		int left_out  = 0;

		for (int i = 0; i < ranked_items.length; i++)
		{
			int item_id = ranked_items[i];
			if (ignore_items.contains(item_id))
			{
				left_out++;
				continue;
			}

			if (!correct_items.contains(item_id))
				continue;

			if (i < n + left_out)
				hit_count++;
			else
				break;
		}

		return (double) hit_count / n;
	}
	
	static double ComputeIDCG(int n)
	{
		double idcg = 0;
		for (int i = 0; i < n; i++){
			double log=Math.log10(i+2)/Math.log10(2);
			idcg+=1/log;
		}
		return idcg;
	}
}
