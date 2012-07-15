package com.rapidminer.data;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
*Copyright (C) 2010, 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 27.07.2011
*/

public class Ratings implements IRatings, Serializable
{
	static final long serialVersionUID=3453435;
	///
	public List<Integer> Users;
	///
	public List<Integer> Items;

	///
	protected List<Double> Values;

	///
	
	public double GetValues(int index){
		return Values.get(index);
	}
	

	 public List<Integer> GetUsers(){
	 
		 return Users;
	 
	 }
 	
	public List<Integer> GetItems(){
	  
		return Items;
	
	}
	
	///
	public int Count() { return Values.size(); } 

	

	/// <summary>Create a new Ratings object</summary>
	public Ratings()
	{
		Users  = new ArrayList<Integer>();
		Items  = new ArrayList<Integer>();
		Values = new ArrayList<Double>();
	}

	public int MaxUserID;
	public int MaxItemID;
	
	///
	public int GetMaxUserID(){
		
	return MaxUserID;
	}
	///
	public int GetMaxItemID(){
		return MaxItemID;
	}

	///
	public ArrayList<ArrayList<Integer>> ByUser()
	{
			if (by_user == null)
				BuildUserIndices();
			return by_user;
	}
	
	ArrayList<ArrayList<Integer>> by_user;

	///
	public void BuildUserIndices()
	{
		by_user = new ArrayList<ArrayList<Integer>>();
		for (int u = 0; u <= MaxUserID; u++)
			by_user.add(new ArrayList<Integer>());

		// one pass over the data
		for (int index = 0; index < Count(); index++)
			by_user.get(Users.get(index)).add(index);
	}

	///
	public ArrayList<ArrayList<Integer>> ByItem()
	{
			if (by_item == null)
				BuildItemIndices();
			return by_item;
	}
	ArrayList<ArrayList<Integer>> by_item;

	///
	public void BuildItemIndices()
	{
		by_item = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i <= MaxItemID; i++)
			by_item.add(new ArrayList<Integer>());

		// one pass over the data
		for (int index = 0; index < Count(); index++)
			by_item.get(Items.get(index)).add(index);

	}

	///
	public List<Integer> RandomIndex()
	{
			if (random_index == null || random_index.size() != Count())
				BuildRandomIndex();

			return random_index;
	}
	private List<Integer> random_index;

	///
	public void BuildRandomIndex()
	{
		random_index = new ArrayList<Integer>();
		for (int index = 0; index < Count(); index++)
			random_index.add(index, index);
		
		
		for (int i = random_index.size() - 1; i >= 0; i--)
		{
			//int r = random.Next(0, i + 1);
			int r = (int) Math.floor(Math.random() * (i+1));

			// swap position i with position r
			int tmp = random_index.get(i);
			random_index.set(i, random_index.get(r));
			random_index.set(r, tmp);
		}
		
	}

	///
	public int [] CountByUser()
	{

			if (count_by_user == null)
				BuildByUserCounts();
			return count_by_user;
	}
	int [] count_by_user;

	///
	public void BuildByUserCounts()
	{
		count_by_user = new int[MaxUserID + 1];
		for (int index = 0; index < Count(); index++)
			count_by_user[Users.get(index)]++;
	}

	///
	public int [] CountByItem()
	{
	
			if (count_by_item == null)
				BuildByItemCounts();
			return count_by_item;
		
	}
	int [] count_by_item;

	///
	public void BuildByItemCounts()
	{
		count_by_item = new int[MaxItemID + 1];
		for (int index = 0; index < Count(); index++)
			count_by_item[Items.get(index)]++;
	}

	// TODO speed up
	///
	public double Average()
	{
	
			double sum = 0;
			for (int index = 0; index < Count(); index++)
				sum += this.GetValues(index);
			return (double) sum / Count();
		
	}
	
	
	public void Add(int user_id, int item_id, float rating)
	{
		Add(user_id, item_id, (double) rating);
	}

	
	public void Add(int user_id, int item_id, byte rating)
	{
		Add(user_id, item_id, (double) rating);
	}

	///
	public  void Add(int user_id, int item_id, double rating)
	{
		Users.add(user_id);
		Items.add(item_id);
		Values.add(rating);

		int pos = Users.size() - 1;

		if (user_id > MaxUserID)
			MaxUserID = user_id;
		if (item_id > MaxItemID)
			MaxItemID = item_id;

		// update index data structures if necessary
		if (by_user != null)
		{
			for (int u = by_user.size(); u <= user_id; u++)
				by_user.add(new ArrayList<Integer>());
			by_user.get(user_id).add(pos);
		}
		if (by_item != null)
		{
			for (int i = by_item.size(); i <= item_id; i++)
				by_item.add(new ArrayList<Integer>());
			by_item.get(item_id).add(pos);
		}
	}
	
	
	public void RemoveUser(int user_id)
	{
		for (int index = 0; index < Count(); index++)
			if (Users.get(index) == user_id)
			{
			
				Users.remove(index);
				Items.remove(index);
				Values.remove(index);
			}

		if (MaxUserID == user_id)
			MaxUserID--;
	}

	///
	public void RemoveItem(int item_id)
	{
		for (int index = 0; index < Count(); index++)
			if (Items.get(index) == item_id)
			{
				Users.remove(index);
				Items.remove(index);
				Values.remove(index);
			}

		if (MaxItemID == item_id)
			MaxItemID--;
	}
	
	
	public  double Get(int user_id, int item_id, List<Integer> indexes)
	{
		// TODO speed up
		int index;
		for(int i=0;i<indexes.size();i++){
		   	index=indexes.get(i);
		
			if (Users.get(index) == user_id && Items.get(index) == item_id)
				return Values.get(index);
		}
                
		  return -1;
	}
	
	public CompactHashSet<Integer> GetUsers(List<Integer> indices)
	{
		CompactHashSet<Integer> result_set = new CompactHashSet<Integer>();
		
		for(int i=0;i<indices.size();i++){
			int index=indices.get(i);
			result_set.add(GetUsers().get(index));
		}
		return result_set;
	}

	///
	public CompactHashSet<Integer> GetItems(List<Integer> indices)
	{
		CompactHashSet<Integer> result_set = new CompactHashSet<Integer>();
		
		for(int i=0;i<indices.size();i++){
			int index=indices.get(i);
			result_set.add(GetItems().get(index));
		}
		return result_set;
	}

	// TODO think whether we want to have a set or a list here
	///
 public CompactHashSet<Integer> AllUsers()
	{
		
			CompactHashSet<Integer> result_set = new CompactHashSet<Integer>();
			for (int index = 0; index < Users.size(); index++)
				result_set.add(Users.get(index));
			return result_set;
	
	}
}
