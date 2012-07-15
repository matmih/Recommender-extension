package com.rapidminer.operator.RatingPrediction;
import java.util.List;

import com.rapidminer.data.IRatings;
import com.rapidminer.data.SparseBooleanMatrix;

/**
Copyright (C) 2010, 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 27.07.2011
*/

public abstract class _userKnn extends rKnn {
	static final long serialVersionUID=3232342;

		/// <summary>boolean matrix indicating which user rated which item</summary>
		protected SparseBooleanMatrix data_user;

		///
		public _userKnn() {super(); }

		///
		public void SetRatings(IRatings value)
		{

				super.SetRatings(value);
				data_user = new SparseBooleanMatrix();
				for (int index = 0; index < GetRatings().Count(); index++){
					data_user.setLocation(ratings.GetUsers().get(index), ratings.GetItems().get(index), true);
				}
			
		}

		/// <summary>Predict the rating of a given user for a given item</summary>
		/// <remarks>
		/// If the user or the item are not known to the recommender, a suitable average rating is returned.
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

			if ((user_id > correlation.NumberOfRows() - 1) || (item_id > MaxItemID))
				return super.Predict(user_id, item_id);

			Integer[] relevant_users=correlation.GetPositivelyCorrelatedEntities(user_id);
		
			double sum = 0;
			double weight_sum = 0;
			int neighbors = K;
			int user_id2;

				for(int i=0;i<relevant_users.length;i++){
					user_id2=relevant_users[i];
			
				if (data_user.getLocation(user_id2, item_id))
				{
					double rating = ratings.Get(user_id2, item_id, ratings.ByUser().get(user_id2));
					
					double weight = correlation.getLocation(user_id, user_id2);
					weight_sum += weight;
					sum += weight * (rating - super.Predict(user_id2, item_id));

					if (--neighbors == 0)
						break;
				}
			}

			double result = super.Predict(user_id, item_id);
			
			if (weight_sum != 0)
			{
				double modification = sum / weight_sum;
				result += modification;
			}

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
			data_user.setLocation(user_id, item_id, true);
			//RetrainUser(user_id);
		}

		///
		public void UpdateRating(int user_id, int item_id, double rating)
		{
			super.UpdateRating(user_id, item_id, rating);
			RetrainUser(user_id);
		}

		///
		public void RemoveRating(int user_id, int item_id)
		{
			super.RemoveRating(user_id, item_id);
			data_user.setLocation(user_id, item_id, false);
			RetrainUser(user_id);
		}

		///
		protected void AddUser(int user_id)
		{
			super.AddUser(user_id);
			//correlation.AddEntity(user_id);
		}
		
		public void AddUsers(List<Integer> users){
			super.AddUsers(users);
			
			correlation.AddEntity(users.get(users.size()-1));
		}
		
		
		public void setSchrinkage(float value){
			
		}
		
		public float getSchrinkage(){
			return 0;
		}
		
	}
