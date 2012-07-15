package com.rapidminer.operator.RatingPrediction;

import com.rapidminer.data.IRatings;
import com.rapidminer.data.SparseBooleanMatrix;
import java.util.List;

/**
Copyright (C) 2010, 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 23.07.2011
*/

public abstract class _itemKnn extends rKnn{
	static final long serialVersionUID=3232342;
		/// <summary>Matrix indicating which item was rated by which user</summary>
		protected SparseBooleanMatrix data_item;

		
		public _itemKnn() {super(); }
		
		///
		public  void SetRatings(IRatings value)
		{
				super.SetRatings(value);
				
				data_item = new SparseBooleanMatrix();
				for (int index = 0; index < super.ratings.Count(); index++)
					data_item.setLocation(ratings.GetItems().get(index), ratings.GetUsers().get(index), true);
		}

		///

		/// <summary>Get positively correlated entities</summary>
		//protected Func<Integer, List<Integer>> GetPositivelyCorrelatedEntities;

		/// <summary>Predict the rating of a given user for a given item</summary>
		/// <remarks>
		/// If the user or the item are not known to the recommender, a suitable average is returned.
		/// To avoid this behavior for unknown entities, use CanPredict() to check before.
		/// </remarks>
		/// <param name="user_id">the user ID</param>
		/// <param name="item_id">the item ID</param>
		/// <returns>the predicted rating</returns>
		public double Predict(int user_id, int item_id)
		{
			if (user_id < 0)
				throw new IllegalArgumentException("user is unknown: " + user_id);
			if (item_id < 0)
				throw new IllegalArgumentException("item is unknown: " + item_id);

			if ((user_id > MaxUserID) || (item_id > correlation.NumberOfRows() - 1))
				return super.Predict(user_id, item_id);

			//List<Integer> relevant_items = correlation.GetPositivelyCorrelatedEntities(item_id);
			Integer[] relevant_items=correlation.GetPositivelyCorrelatedEntities(item_id);

			double sum = 0;
			double weight_sum = 0;
			int neighbors = K;
			int item_id2;

			for (int i=0;i<relevant_items.length;i++){
				   	item_id2=relevant_items[i];
			
				if (data_item.getLocation(item_id2, user_id))
				{
					double rating = ratings.Get(user_id, item_id2, ratings.ByItem().get(item_id2));
					double weight = correlation.getLocation(item_id, item_id2);
					weight_sum += weight;
					sum += weight * (rating - super.Predict(user_id, item_id2));

					if (--neighbors == 0)
						break;
				}
			}

			double result = super.Predict(user_id, item_id);
			
			if (weight_sum != 0)
				result += sum / weight_sum;

			if (result > GetMaxRating())
				result = GetMaxRating();
			if (result < GetMinRating())
				result = GetMinRating();
			return result;
		}

		///
		public void AddRating(int user_id, int item_id, double rating)
		{
			super.AddRating(user_id, item_id, rating);
			data_item.setLocation(item_id, user_id, true);
			//RetrainItem(item_id);
		}

		///
		public void UpdateRating(int user_id, int item_id, double rating)
		{
			super.UpdateRating(user_id, item_id, rating);
			RetrainItem(item_id);
		}

		///
		public void RemoveRating(int user_id, int item_id)
		{
			super.RemoveRating(user_id, item_id);
			data_item.setLocation(item_id, user_id, false);
			RetrainItem(user_id);
		}

		///
		protected  void AddItem(int item_id)
		{
			super.AddItem(item_id);//addUser?
			//correlation.AddEntity(item_id);
		}
		
		public void AddItems(List<Integer> items){
			super.AddItems(items);
			
			correlation.AddEntity(items.get(items.size()-1));
		}
		
		
		///
		public void LoadModel(String filename)
		{
			super.LoadModel(filename);
		}
		
		public void setSchrinkage(float value){
		
		}
		
		public float getSchrinkage(){
			return 0;
		}
		
	}
	
