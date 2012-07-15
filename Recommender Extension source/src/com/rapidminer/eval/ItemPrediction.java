package com.rapidminer.eval;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.rapidminer.ItemRecommendation.ItemRecommender;
import com.rapidminer.data.WeightedItem;


/**
*Copyright (C) 2010 Steffen Rendle, Zeno Gantner
*Copyright (C) 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 29.07.2011
*/

public class ItemPrediction
{

	/// <summary>predict items for a specific users</summary>
	/// <param name="recommender">the <see cref="IRecommender"/> object to use for the predictions</param>
	/// <param name="user_id">the user ID</param>
	/// <param name="max_item_id">the maximum item ID</param>
	/// <returns>a list sorted list of item IDs</returns>
	static public int[] PredictItems(ItemRecommender recommender, int user_id, int max_item_id)
	{
		List<WeightedItem> result = new ArrayList<WeightedItem>();
		for (int item_id = 0; item_id <= max_item_id; item_id++)
			result.add( new WeightedItem(item_id, recommender.Predict(user_id, item_id)));

		Collections.sort(result);
		Collections.reverse(result);

		int[] return_array = new int[max_item_id + 1];
		for (int i = 0; i < return_array.length; i++)
			return_array[i] = result.get(i).item_id;

		return return_array;
	}

	/// <summary>Predict items for a given user</summary>
	/// <param name="recommender">the recommender to use</param>
	/// <param name="user_id">the numerical ID of the user</param>
	/// <param name="relevant_items">a collection of numerical IDs of relevant items</param>
	/// <returns>an ordered list of items, the most likely item first</returns>
	static public int[] PredictItems(ItemRecommender recommender, int user_id, List<Integer> relevant_items)
	{
		List<WeightedItem> result = new ArrayList<WeightedItem>();
	
		for(int i1=0;i1<relevant_items.size();i1++){
			int item_id=relevant_items.get(i1);
			result.add( new WeightedItem(item_id, recommender.Predict(user_id, item_id)));
		}
			
		Collections.sort(result);
		Collections.reverse(result);
		
		int[] return_array = new int[result.size()];
		for (int i = 0; i < return_array.length; i++)
			return_array[i] = result.get(i).item_id;
		return return_array;

	}
	
	static public List<WeightedItem> PredictItems1(ItemRecommender recommender, int user_id, List<Integer> relevant_items)
	{
		List<WeightedItem> result = new ArrayList<WeightedItem>();
	
		
		for(int i1=0;i1<relevant_items.size();i1++){
			int item_id=relevant_items.get(i1); 
			result.add( new WeightedItem(item_id, recommender.Predict(user_id, item_id)));
		}
		
			
		Collections.sort(result);
		Collections.reverse(result);
		
		return result;
	}
	
}