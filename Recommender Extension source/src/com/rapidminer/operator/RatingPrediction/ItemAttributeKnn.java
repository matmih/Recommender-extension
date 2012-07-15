package com.rapidminer.operator.RatingPrediction;

import com.rapidminer.ItemRecommendation.IItemAttributeAwareRecommender;
import com.rapidminer.data.BinaryCosine;
import com.rapidminer.data.SparseBooleanMatrix;

/**
*Copyright (C) 2010 Steffen Rendle,Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 02.08.2011
*/

public class ItemAttributeKnn extends _itemKnn implements IItemAttributeAwareRecommender
{
	///
	 static final long serialVersionUID=3453434;
	 
	public SparseBooleanMatrix GetItemAttributes()
	{
		return this.item_attributes; }
		
	
	public void SetItemAttributes(SparseBooleanMatrix value)
	{
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
	public ItemAttributeKnn(){super(); }

	///
	public void Train()
	{
		super.Train();
		this.correlation = BinaryCosine.Create(GetItemAttributes());
	}

	///
	public String ToString()
	{
		return String.format("ItemAttributeKNN k={0} reg_u={1} reg_i={2}",
							 K == Integer.MAX_VALUE ? "inf" : K, RegU, RegI);
	}
}
