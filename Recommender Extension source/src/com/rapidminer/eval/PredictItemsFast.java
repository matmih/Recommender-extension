package com.rapidminer.eval;
import com.rapidminer.ItemRecommendation.ItemRecommender;
import com.rapidminer.data.WeightedItem;
import com.rapidminer.utils.Random;

import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.List;
import java.util.ArrayList;

/**
@autor Nino-Antulov Fantulin (Ruðer Boškoviæ Institute) 05.02.2012.
@edited Matej Mihelèiæ (Ruðer Boškoviæ Institute) 1.09.2012.
 */

public class PredictItemsFast {

	static Random r=new Random();
	
	static public List<WeightedItem> PredictItemsFast1(ItemRecommender recommender, int user_id, List<Integer> relevant_items, com.rapidminer.data.CompactHashSet<Integer> consumedItems, int topk)
		{
			List<WeightedItem> result = new ArrayList<WeightedItem>();
		
			PriorityQueue<WeightedItem> pq = new PriorityQueue<WeightedItem>(topk);
			
			int cnt=0;
			for (int i=0; i < relevant_items.size(); ++i){
				int item_id=relevant_items.get(i); 
				if(consumedItems.contains(item_id)==false){
				WeightedItem wi = new WeightedItem(item_id, recommender.Predict(user_id, item_id));
				pq.add(wi);
				cnt++;
				if(cnt==topk)
					break;
				}
			}
			
			double minScore = pq.element().weight;
			for(int i1 = topk; i1 < relevant_items.size(); i1++){
				int item_id=relevant_items.get(i1); 
				WeightedItem wi = new WeightedItem(item_id, recommender.Predict(user_id, item_id));
				
				if (wi.weight < minScore){
					continue; 
					// This item is not candidate for topk items
				}
				else if(wi.weight==minScore){
					if(consumedItems.contains(item_id)==false){
						if(r.nextInt(5)<=3){
							// insert instead of item with min score in priority queue
							pq.poll(); // deletes minimum item O( log(topK) )
							pq.add(wi); // inserts O( log(topK) )
							minScore = pq.element().weight; // O(1)
						}
					}
				}
					else{
					if (consumedItems.contains(item_id) == false){ 
						// insert instead of item with min score in priority queue
						pq.poll(); // deletes minimum item O( log(topK) )
						pq.add(wi); // inserts O( log(topK) )
						minScore = pq.element().weight; // O(1)
					}
				}
			}
			
			WeightedItem [] itemsArray  = (WeightedItem []) pq.toArray(new WeightedItem [0]);
			Arrays.sort(itemsArray);
			for (int i = itemsArray.length-1; i >= 0 ; i--){
				result.add(itemsArray[i]);
			}
			
			return result;
		}
	
	
}
