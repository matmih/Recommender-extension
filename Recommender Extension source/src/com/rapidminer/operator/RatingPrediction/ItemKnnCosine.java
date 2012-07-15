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

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 25.07.2011
*/

public class ItemKnnCosine extends _itemKnn {

	///
	static final long serialVersionUID=3453434;
	public ItemKnnCosine(){super(); }

	///
	public void Train()
	{
		super.Train();
		this.correlation = BinaryCosine.Create(data_item);
	}

	///
	protected void RetrainItem(int item_id)
	{
	}

	public void AddItems(List<Integer> items){
		super.AddItems(items);
	}
	
	public int AddRatings(List<Integer> users, List<Integer> items, List<Double> ratings){
		
		if(users==null)
			return 1;
		
		super.AddRatings(users, items, ratings);
		
		correlation.Grow(MaxItemID+1, MaxItemID+1);
		 correlation.Setnum_entities(MaxItemID+1);
		 
		 IMatrix_b transpose = data_item.Transpose();

			
			SparseMatrix overlap = new SparseMatrix(data_item.NumberOfRows(), data_item.NumberOfRows());
			com.rapidminer.data.CompactHashSet<Integer> viewed=new com.rapidminer.data.CompactHashSet<Integer>();
			int prevus=-1;

			for (int row_id = 0; row_id < users.size(); row_id++)
			{
				List<Integer> row = ((IBooleanMatrix) transpose).GetEntriesByRow(users.get(row_id));
				if(prevus!=users.get(row_id))
					 viewed.clear();
				
					int x=items.get(row_id);
					viewed.add(x);
			
					for (int j = 0; j < row.size(); j++)
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
						prevus=users.get(row_id);
				}
			}
			
			List<Tupel<Integer,Integer>> temp=overlap.NonEmptyEntryIDs();

			
			for(int i=0;i<temp.size();i++){
				
				int x=temp.get(i).getFirst();
				
				int y=temp.get(i).getSecond();
				 
				 float value= (float) (overlap.getLocation(x, y) / Math.sqrt(data_item.NumEntriesByRow(x) * data_item.NumEntriesByRow(y) ));

				 this.correlation.setLocation(x, y, this.correlation.getLocation(x, y)*0.9999f+value);
			}
		 
		
		return 1;
	}
	
	public void RetrainItems(List<Integer> items){
		super.RetrainItems(items);
	}
	
	///
	public String ToString()
	{
		return String.format("ItemKNNCosine k={0} reg_u={1} reg_i={2}",
							 K == Integer.MAX_VALUE ? "inf" : GetK(), RegU, RegI);
	}
}