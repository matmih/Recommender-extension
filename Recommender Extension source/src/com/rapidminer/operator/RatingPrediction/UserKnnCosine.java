package com.rapidminer.operator.RatingPrediction;
import com.rapidminer.data.BinaryCosine;
import com.rapidminer.data.IBooleanMatrix;
import com.rapidminer.data.IMatrix_b;
import com.rapidminer.data.SparseMatrix;
import com.rapidminer.tools.container.Tupel;

import java.util.List;

/**
Copyright (C) 2010, 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 27.07.2011
*/

public class UserKnnCosine extends _userKnn {

	static final long serialVersionUID=3232342;
		///
		public UserKnnCosine(){super(); }

		///
		public void Train()
		{
			super.Train();
			this.correlation = BinaryCosine.Create(data_user);
		}

		///
		protected void RetrainUser(int user_id)
		{
		}
		
		
		public void AddUsers(List<Integer> users){
		super.AddUsers(users);
			
		}
		
		public void RetrainUsers(List<Integer> users){
			super.RetrainUsers(users);
			
		}
		
		public int AddRatings(List<Integer> users, List<Integer> items, List<Double> ratings){
			
			if(users==null)
				return 1;
			
			super.AddRatings(users, items, ratings);
			correlation.Grow(correlation.NumberOfRows()+users.size(), correlation.NumberOfColumns()+users.size());
			correlation.Setnum_entities(MaxUserID+1);
			
			IMatrix_b transpose = data_user.Transpose();
			
			SparseMatrix overlap = new SparseMatrix(data_user.NumberOfRows(), data_user.NumberOfRows());
			com.rapidminer.data.CompactHashSet<Integer> viewed=new com.rapidminer.data.CompactHashSet<Integer>();
			int prevus=-1;
			
			for (int row_id = 0; row_id <items.size(); row_id++) //should be item sorted
			{
				List<Integer> row = ((IBooleanMatrix) transpose).GetEntriesByRow(items.get(row_id));
				if(prevus!=users.get(row_id))
					 viewed.clear();
				

					int x=users.get(row_id);
		
					for (int j = 0; j < row.size(); j++)
					{
						int y = row.get(j);
						if(viewed.contains(y))
							continue;
						
						if (x < y){
							
							int t=overlap.getLocation1(x, y);
							t++;
							overlap.setLocation(x, y, t);
						}
						else{
							
							int t=overlap.getLocation1(y, x);
							t++;
							overlap.setLocation(y, x, t);
						}

					}
					prevus=users.get(row_id);
			}
			
			List<Tupel<Integer,Integer>> temp=overlap.NonEmptyEntryIDs();

			
			for(int i=0;i<temp.size();i++){
				
				int x=temp.get(i).getFirst();
				
				int y=temp.get(i).getSecond();
				 
				 float value= (float) (overlap.getLocation(x, y) / Math.sqrt(data_user.NumEntriesByRow(x) * data_user.NumEntriesByRow(y) ));

				 this.correlation.setLocation(x, y, this.correlation.getLocation(x, y)*0.9999f+value);
			}
			
			return 1;
		}

		///
		public String ToString()
		{
			return String.format(
								 "UserKNNCosine k={0} reg_u={1} reg_i={2}",
								 K == Integer.MAX_VALUE ? "inf" : K, RegU, RegI);
		}
	}
	
