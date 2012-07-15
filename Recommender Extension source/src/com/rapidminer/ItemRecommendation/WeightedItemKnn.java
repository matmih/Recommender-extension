package com.rapidminer.ItemRecommendation;

/**
*Copyright (C) 2010  Steffen Rendle, Zeno Gantner
*Copyright (C) 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 02.08.2011
*/

public class WeightedItemKnn extends _itemKnn {

		///
	/// <summary>Weighted k-nearest neighbor item-based collaborative filtering using cosine similarity</summary>
	   static final long serialVersionUID=3453434;
	   
		public double Predict(int user_id, int item_id)
		{
			if ((user_id < 0) || (user_id > MaxUserID))
				return 0;
			if ((item_id < 0) || (item_id >= nearest_neighbors.length))
				return 0;

			if(k==Integer.MAX_VALUE)
				return correlation.SumUp(item_id, GetFeedback().GetUserMatrix().getLocation(user_id));
			else
			{
				double result = 0;
				for(int i1=0;i1<nearest_neighbors[item_id].length;i1++){
					int neighbor=nearest_neighbors[item_id][i1];
					if (GetFeedback().GetItemMatrix().getLocation(neighbor, user_id))
						result += correlation.getLocation(item_id, neighbor);
				}
				
				return result;				
			}
		}
				

			public String ToString()
			{
				return String.format("WeightedItemKNN k={0}" , k == Integer.MAX_VALUE ? "inf" : k);
			}
	
}
