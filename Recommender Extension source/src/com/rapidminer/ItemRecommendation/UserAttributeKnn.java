package com.rapidminer.ItemRecommendation;

import java.util.List;

import com.rapidminer.data.BinaryCosine;
import com.rapidminer.data.SparseBooleanMatrix;

/**
*Copyright (C) 2010, 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 02.08.2011
*/

public class UserAttributeKnn extends _userKnn implements IUserAttributeAwareRecommender{
		///
	
	 static final long serialVersionUID=3453434;
	
		public SparseBooleanMatrix GetUserAttributes()
		{
			 return this.user_attributes; }
		
		
		public void SetUserAttributes(SparseBooleanMatrix value){
				this.user_attributes = value;
				this.NumUserAttributes = user_attributes.NumberOfColumns();
				this.MaxUserID = Math.max(MaxUserID, user_attributes.NumberOfRows() - 1);
			}
		
		private SparseBooleanMatrix user_attributes;

		///
		public int NumUserAttributes;

		public int GetNumUserAttributes(){
			return NumUserAttributes;
		}
		
		public void SetNumUserAttributes(int value){
			NumUserAttributes=value;
		}
		///
		public void Train()
		{
			correlation = BinaryCosine.Create(user_attributes);

			int num_users = user_attributes.NumberOfRows();
			this.nearest_neighbors=new Integer[num_users][];
			for (int u = 0; u < num_users; u++){
				nearest_neighbors[u]=correlation.GetNearestNeighbors(u, k);
			}
		}

		
		public void RetrainUsers(List<Integer> users)
		{
			
		
		}
		
		public void AddUsers(List<Integer> users)
		{
			
		if(users.size()!=0)
			super.AddItems(users);
		}
		
		public void AddItems(List<Integer> items)
		{
			
		if(items.size()!=0)
			super.AddUsers(items);
		}
		
		
		///
		public String ToString()
		{
			return String.format("UserAttributeKNN k={0}", k == Integer.MAX_VALUE ? "inf" : k);
		}
	}
