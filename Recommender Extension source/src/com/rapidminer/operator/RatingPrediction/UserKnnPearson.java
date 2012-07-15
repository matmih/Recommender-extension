package com.rapidminer.operator.RatingPrediction;
import com.rapidminer.correlation.Pearson;
import com.rapidminer.data.SparseMatrix;
import com.rapidminer.data.SparseMatrix_d;
import com.rapidminer.tools.container.Tupel;

import java.util.ArrayList;
import java.util.List;
/**
Copyright (C) 2010, 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 27.07.2011
*/

public class UserKnnPearson extends _userKnn{

	   static final long serialVersionUID=3232342;
		/// <summary>shrinkage (regularization) parameter</summary>
		public float GetShrinkage() { return shrinkage; } 
		
		
		public void SetShrinkage(float value){
		 shrinkage = value; } 
		
		private float shrinkage = 10;
		

		///
		public UserKnnPearson(){super();}

		///
		public void Train()
		{
			super.Train();
			this.correlation = Pearson.Create(ratings, 0, GetShrinkage());
		}

		///
		protected void RetrainUser(int user_id)
		{
		
		}
		
		public void RetrainUsers(List<Integer> users){
			super.RetrainUsers(users);
			
			  ArrayList<ArrayList<Integer>> ratings_by_other_entity = ratings.ByUser();

				SparseMatrix freqs   = new SparseMatrix(this.correlation.Getnum_entities(), this.correlation.Getnum_entities());
				SparseMatrix_d i_sums  = new SparseMatrix_d(this.correlation.Getnum_entities(), this.correlation.Getnum_entities());
				SparseMatrix_d j_sums  = new SparseMatrix_d(this.correlation.Getnum_entities(), this.correlation.Getnum_entities());
				SparseMatrix_d ij_sums = new SparseMatrix_d(this.correlation.Getnum_entities(), this.correlation.Getnum_entities());
				SparseMatrix_d ii_sums = new SparseMatrix_d(this.correlation.Getnum_entities(), this.correlation.Getnum_entities());
				SparseMatrix_d jj_sums = new SparseMatrix_d(this.correlation.Getnum_entities(), this.correlation.Getnum_entities());

				for(int i1=0;i1<users.size();i1++){
					ArrayList<Integer> other_entity_ratings = ratings_by_other_entity.get(users.get(i1));
					for (int i = 0; i < other_entity_ratings.size(); i++)
					{
						Integer index1 = other_entity_ratings.get(i);
						int x =  ratings.GetUsers().get(index1);

						// update pairwise scalar product and frequency
		       		for (int j = i + 1; j < other_entity_ratings.size(); j++)
						{
							Integer index2 = other_entity_ratings.get(j);
							int y = ratings.GetUsers().get(index2);

							double rating1 = ratings.GetValues(index1);
							double rating2 = ratings.GetValues(index2);
							

							// update sums
							if (x < y)
							{
								freqs.setLocation(x, y, freqs.getLocation1(x, y)+1);
								i_sums.setLocation(x, y,i_sums.getLocation1(x, y)+rating1);
								j_sums.setLocation(x, y, j_sums.getLocation1(x, y)+rating2);
								ij_sums.setLocation(x, y,ij_sums.getLocation1(x, y)+rating1*rating2);
								ii_sums.setLocation(x, y, ii_sums.getLocation1(x, y)+rating1*rating1);
								jj_sums.setLocation(x, y, jj_sums.getLocation1(x, y)+rating2*rating2);
							}
							else
							{
								freqs.setLocation(y, x, freqs.getLocation1(y, x)+1);
								i_sums.setLocation(y, x, i_sums.getLocation1(y, x)+rating1);
								j_sums.setLocation(y, x, j_sums.getLocation1(y, x)+rating2);
								ij_sums.setLocation(y, x, ij_sums.getLocation1(y, x)+rating1*rating2);
								ii_sums.setLocation(y, x, ii_sums.getLocation1(y, x)+rating1*rating1);
								jj_sums.setLocation(y, x, jj_sums.getLocation1(y, x)+rating2*rating2);
							}
		       		}
					}
				}
				
				 List<Tupel<Integer,Integer>> elementi=freqs.NonEmptyEntryIDs();
				
				// fill the entries with interactions
				for (int i1=0;i1<elementi.size();i1++)
				{
					
					Tupel<Integer,Integer> par=elementi.get(i1);
					
					int i=par.getFirst();
					int j=par.getSecond();
					int n = freqs.getLocation(i, j);
					
					
					if (n < 2)
					{
						continue;
					}

					double numerator = ij_sums.getLocation(i, j) * n - i_sums.getLocation(i, j) * j_sums.getLocation(i, j);

					double denominator = Math.sqrt( (n * ii_sums.getLocation(i, j) - i_sums.getLocation(i, j) * i_sums.getLocation(i, j)) * (n * jj_sums.getLocation(i, j) - j_sums.getLocation(i, j) * j_sums.getLocation(i, j)) );
					
					
					if (denominator == 0)
					{
						continue;
					}

					double pmcc = numerator / denominator;
					
					
					this.correlation.setLocation(i, j, this.correlation.getLocation(i, j)+(float) (pmcc * (n / (n + shrinkage))));
				}
			
		}

		///
		public String ToString()
		{
			return String.format(
								 "UserKNNPearson k={0} shrinkage={1} reg_u={2} reg_i={3}",
								 K == Integer.MAX_VALUE ? "inf" : K, GetShrinkage(), RegU, RegI);
		}
	}
	
	
