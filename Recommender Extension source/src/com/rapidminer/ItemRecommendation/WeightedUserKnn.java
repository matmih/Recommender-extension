package com.rapidminer.ItemRecommendation;

/**
*Copyright (C) 2010  Steffen Rendle, Zeno Gantner
*Copyright (C) 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 02.08.2011
*/

public class WeightedUserKnn extends _userKnn {
	/// <summary>Weighted k-nearest neighbor user-based collaborative filtering using cosine-similarity</summary>
	   static final long serialVersionUID=3453434;
	   
		public double Predict(int user_id, int item_id)
		{
			if ((user_id < 0) || (user_id >= nearest_neighbors.length))
				return 0;
			if ((item_id < 0) || (item_id > MaxItemID))
				return 0;

			if(k==Integer.MAX_VALUE){
				return correlation.SumUp(user_id, GetFeedback().GetItemMatrix().getLocation(item_id));
			}
			else
			{
				double result=0;
				
				for(int i=0;i<nearest_neighbors[user_id].length;i++){
					
					int neighbor=nearest_neighbors[user_id][i];
					
						if (GetFeedback().GetUserMatrix().getLocation(neighbor, item_id)) 
							result += correlation.getLocation(user_id, neighbor);
					}
				return result;
			}
			
			
			
		}

		///
		public String ToString()
		{
			return String.format("WeightedUserKNN k={0}",
								 k == Integer.MAX_VALUE ? "inf" : k);
		}
	}
	
