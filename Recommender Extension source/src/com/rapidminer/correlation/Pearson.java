package com.rapidminer.correlation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.rapidminer.data.CompactHashSet;
import com.rapidminer.data.CorrelationMatrix;
import com.rapidminer.data.IRatings;
import com.rapidminer.data.RatingCorrelationMatrix;
import com.rapidminer.data.SparseMatrix;
import com.rapidminer.data.SparseMatrix_d;
import com.rapidminer.tools.container.Tupel;

/**
*Copyright (C) 2010, 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 21.07.2011
*/


public class Pearson extends RatingCorrelationMatrix
{
	
	static final long serialVersionUID=3453435;
	/// <summary>shrinkage parameter</summary>
	public float shrinkage = 10;

	/// <summary>Constructor. Create a Pearson correlation matrix</summary>
	/// <param name="num_entities">the number of entities</param>
	public Pearson(int num_entities) {super(num_entities); }

	/// <summary>Create a Pearson correlation matrix from given data</summary>
	/// <param name="ratings">the ratings data</param>
	/// <param name="entity_type">the entity type, either USER or ITEM</param>
	/// <param name="shrinkage">a shrinkage parameter</param>
	/// <returns>the complete Pearson correlation matrix</returns>
	static public CorrelationMatrix Create(IRatings ratings, Integer entity_type, float shrinkage)
	{
		Pearson cm;
		int num_entities = 0;
		
		if(entity_type==0)
			num_entities = ratings.GetMaxUserID() + 1;
		else if (entity_type==1)
			num_entities = ratings.GetMaxItemID() + 1;
		else
			throw new IllegalArgumentException("Unknown entity type: " + entity_type);

			cm = new Pearson(num_entities);

		cm.shrinkage = shrinkage;
		cm.ComputeCorrelations(ratings, entity_type);
		return cm;
	}

	/// <summary>Compute correlations between two entities for given ratings</summary>
	/// <param name="ratings">the rating data</param>
	/// <param name="entity_type">the entity type, either USER or ITEM</param>
	/// <param name="i">the ID of first entity</param>
	/// <param name="j">the ID of second entity</param>
	/// <param name="shrinkage">the shrinkage parameter</param>
	public static float ComputeCorrelation(IRatings ratings, Integer entity_type, int i, int j, float shrinkage)
	{
		if (i == j)
			return 1;

		List<Integer> ratings1 = (entity_type == 0) ? ratings.ByUser().get(i) : ratings.ByItem().get(i);
		List<Integer> ratings2 = (entity_type == 1) ? ratings.ByUser().get(j) : ratings.ByItem().get(j);

		// get common ratings for the two entities
		CompactHashSet<Integer> e1 = (entity_type == 0) ? ratings.GetItems(ratings1) : ratings.GetUsers(ratings1);
		CompactHashSet<Integer> e2 = (entity_type == 0) ? ratings.GetItems(ratings2) : ratings.GetUsers(ratings2);

		e1.retainAll(e2);
		
		
		int n = e1.size();
		if (n < 2)
			return 0;

		// single-pass variant
		double i_sum = 0;
		double j_sum = 0;
		double ij_sum = 0;
		double ii_sum = 0;
		double jj_sum = 0;
		
			
		 Iterator<Integer> itr = e1.iterator();
		
		while(itr.hasNext()){
		
		   String s=itr.next().toString();
		   int other_entity_id=Integer.parseInt(s);
			// get ratings
			double r1 = 0;
			double r2 = 0;
			if (entity_type == 0)
			{
				r1 = ratings.Get(i, other_entity_id, ratings1);
				r2 = ratings.Get(j, other_entity_id, ratings2);
			}
			else
			{
				r1 = ratings.Get(other_entity_id, i, ratings1);
				r2 = ratings.Get(other_entity_id, j, ratings2);
			}

			// update sums
			i_sum  += r1;
			j_sum  += r2;
			ij_sum += r1 * r2;
			ii_sum += r1 * r1;
			jj_sum += r2 * r2;
		}

		double denominator = Math.sqrt( (n * ii_sum - i_sum * i_sum) * (n * jj_sum - j_sum * j_sum) );

		if (denominator == 0)
			return 0;
		double pmcc = (n * ij_sum - i_sum * j_sum) / denominator;

		return (float) pmcc * (n / (n + shrinkage));
	}

	/// <summary>Compute correlations for given ratings</summary>
	/// <param name="ratings">the rating data</param>
	/// <param name="entity_type">the entity type, either USER or ITEM</param>
	public void ComputeCorrelations(IRatings ratings, Integer entity_type)
	{
		if (entity_type !=0 && entity_type != 1)
			throw new IllegalArgumentException("entity type must be either USER or ITEM, not " + entity_type);

		ArrayList<ArrayList<Integer>> ratings_by_other_entity = (entity_type == 0) ? ratings.ByItem() : ratings.ByUser();

		SparseMatrix freqs   = new SparseMatrix(num_entities, num_entities);
		SparseMatrix_d i_sums  = new SparseMatrix_d(num_entities, num_entities);
		SparseMatrix_d j_sums  = new SparseMatrix_d(num_entities, num_entities);
		SparseMatrix_d ij_sums = new SparseMatrix_d(num_entities, num_entities);
		SparseMatrix_d ii_sums = new SparseMatrix_d(num_entities, num_entities);
		SparseMatrix_d jj_sums = new SparseMatrix_d(num_entities, num_entities);

		for(int i1=0;i1<ratings_by_other_entity.size();i1++){
			ArrayList<Integer> other_entity_ratings = ratings_by_other_entity.get(i1);
			for (int i = 0; i < other_entity_ratings.size(); i++)
			{
				Integer index1 = other_entity_ratings.get(i);
				int x = (entity_type == 0) ? ratings.GetUsers().get(index1) : ratings.GetItems().get(index1);

				// update pairwise scalar product and frequency
        		for (int j = i + 1; j < other_entity_ratings.size(); j++)
				{
					Integer index2 = other_entity_ratings.get(j);
					int y = (entity_type == 0) ? ratings.GetUsers().get(index2) : ratings.GetItems().get(index2);

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
		
		 
		 for (int i = 0; i < num_entities; i++)
				this.setLocation(i, i, 1);
		
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
				this.setLocation(i, j, 0);
				continue;
			}

			double numerator = ij_sums.getLocation(i, j) * n - i_sums.getLocation(i, j) * j_sums.getLocation(i, j);

			double denominator = Math.sqrt( (n * ii_sums.getLocation(i, j) - i_sums.getLocation(i, j) * i_sums.getLocation(i, j)) * (n * jj_sums.getLocation(i, j) - j_sums.getLocation(i, j) * j_sums.getLocation(i, j)) );
			
			
			if (denominator == 0)
			{
				this.setLocation(i, j, 0);
				continue;
			}

			double pmcc = numerator / denominator;
			
			
			this.setLocation(i, j, (float) (pmcc * (n / (n + shrinkage))));
		}
		
	}
}