package com.rapidminer.data;
import com.rapidminer.utils.Random;

/**
* Copyright (C) 2010 Steffen Rendle, Zeno Gantner
* Copyright (C) 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 01.08.2011
*/

public class WeightedItem implements Comparable<WeightedItem>
{
	/// <summary>Item ID</summary>
	public int item_id;
	/// <summary>Weight</summary>
	public double weight;

	/// <summary>Default constructor</summary>
	public WeightedItem() {}
	static public Random r=new Random();
	/// <summary>Constructor</summary>
	/// <param name="item_id">the item ID</param>
	/// <param name="weight">the weight</param>
	public WeightedItem(int item_id, double weight)
	{
		this.item_id = item_id;
		this.weight  = weight;
	}

	///
	public int compareTo(WeightedItem o)
	{
		if(this==o)
			return 0;
		else if(this!=o && (this.weight==o.weight)){
			/*if(r.nextInt(5)<=3)
			return 1;
			else return -1;*/
			return 0;
		}
		else if(this!=o && (this.weight>o.weight))
			return 1; 
		else 
			return -1; 
	}
	
	///
	public boolean Equals(WeightedItem otherItem)
	{
		if (otherItem == null)
			return false;

		return Math.abs(this.weight - otherItem.weight) < 0.000001;
	}
}