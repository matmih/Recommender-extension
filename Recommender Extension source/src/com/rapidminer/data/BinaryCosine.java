package com.rapidminer.data;

import java.io.Serializable;
import java.util.List;

import com.rapidminer.tools.container.Tupel;

/**
*Copyright (C) 2010 Steffen Rendle, Zeno Gantner
*Copyright (C) 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ institute) 21.07.2011
*/


public class BinaryCosine extends BinaryDataCorrelationMatrix implements Serializable
{
	
	 static final long serialVersionUID=3453435;
	/// <summary>Creates an object of type Cosine</summary>
	/// <param name="num_entities">the number of entities</param>
	public BinaryCosine(int num_entities) {super(num_entities); }

	/// <summary>Copy constructor. Creates an object of type Cosine from an existing correlation matrix</summary>
	/// <param name ="correlation_matrix">the correlation matrix to copy</param>
	public BinaryCosine(CorrelationMatrix correlation_matrix)
	{
		super(correlation_matrix.NumberOfRows());
		this.data = correlation_matrix.data;
	}

	public BinaryCosine(){
	
	}
	
	/// <summary>Creates a Cosine similarity matrix from given data</summary>
	/// <param name="vectors">the boolean data</param>
	/// <returns>the similarity matrix based on the data</returns>
	
	public void ComputeCorrelations(IBooleanMatrix entity_data)
	{
		
		IMatrix_b transpose = entity_data.Transpose();

		
		SparseMatrix overlap = new SparseMatrix(entity_data.NumberOfRows(), entity_data.NumberOfRows());
		// go over all (other) entities
		

		for (int row_id = 0; row_id < transpose.NumberOfRows(); row_id++)
		{
			List<Integer> row = ((IBooleanMatrix) transpose).GetEntriesByRow(row_id);
			
			for (int i = 0; i < row.size(); i++)
			{
				int x = row.get(i);

				for (int j = i + 1; j < row.size(); j++)
				{
					int y = row.get(j);

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
			}
		}
		
		// the diagonal of the correlation matrix
		for (int i = 0; i < num_entities; i++)
			this.setLocation(i, i, 1);
		
		List<Tupel<Integer,Integer>> temp=overlap.NonEmptyEntryIDs();

		
		for(int i=0;i<temp.size();i++){
			
			int x=temp.get(i).getFirst();
			
			int y=temp.get(i).getSecond();
			 
			 float value= (float) (overlap.getLocation(x, y) / Math.sqrt(entity_data.NumEntriesByRow(x) * entity_data.NumEntriesByRow(y) ));

			 this.setLocation(x, y, value);
		}
	}
	
	 public static CorrelationMatrix Create(IBooleanMatrix vectors)
	{
		BinaryDataCorrelationMatrix cm;
		int num_entities = vectors.NumberOfRows();
			cm = new BinaryCosine(num_entities);
		cm.ComputeCorrelations(vectors);
		return cm;
	}

	/// <summary>Computes the cosine similarity of two binary vectors</summary>
	/// <param name="vector_i">the first vector</param>
	/// <param name="vector_j">the second vector</param>
	/// <returns>the cosine similarity between the two vectors</returns>
	public static float ComputeCorrelation(CompactHashSet<Integer> vector_i, CompactHashSet<Integer> vector_j)
	{
		int cntr = 0;
		for(Object obj : vector_j)
			if(vector_i.contains(obj))
				cntr++;
		return (float) cntr / (float) Math.sqrt(vector_i.size() * vector_j.size());
	}
}