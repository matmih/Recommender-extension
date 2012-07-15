package com.rapidminer.ItemRecommendation;

import java.util.List;

import com.rapidminer.data.BinaryCosine;
import com.rapidminer.data.SparseBooleanMatrix;

/**
*Copyright (C) 2010, 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 02.08.2011
*/

public class ItemAttributeKnn extends _itemKnn implements IItemAttributeAwareRecommender
{
	 static final long serialVersionUID=3453434;
	///
	public SparseBooleanMatrix GetItemAttributes()
	{
		return this.item_attributes; }
	
	
		public void SetItemAttributes(SparseBooleanMatrix value){
			this.item_attributes = value;
			this.NumItemAttributes = item_attributes.NumberOfColumns();
			this.MaxItemID = Math.max(MaxItemID, item_attributes.NumberOfRows() - 1);
		}
	

private SparseBooleanMatrix item_attributes;

	///
	public int NumItemAttributes;
	
	
	public int GetNumItemAttributes(){
		return NumItemAttributes;
	}
	
	public void SetNumItemAttributes(int value){
		NumItemAttributes=value;
	}
	

	///
	public void Train()
	{
		this.correlation = BinaryCosine.Create(GetItemAttributes());

		int num_items = MaxItemID + 1;

		this.nearest_neighbors=new Integer[num_items][];
		for (int i = 0; i < num_items; i++){
			nearest_neighbors[i]=correlation.GetNearestNeighbors(i, k);
		}
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
	
	public void RetrainItems(List<Integer> items)
	{
		

	}

	///
	public String ToString()
	{
		return String.format("ItemAttributeKNN k={0}", k == Integer.MAX_VALUE ? "inf" : k);
	}
}
