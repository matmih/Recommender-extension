package com.rapidminer.data;
import java.util.ArrayList;
import java.util.List;

/**
*Copyright (C) 2010, 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 27.07.2011
*/

public interface IRatings
{
	/// <summary>the user entries</summary>
    public List<Integer> GetUsers();
    
	/// <summary>the item entries</summary>
	public List<Integer> GetItems();//s=new ArrayList<Integer>();

	/// <summary>the maximum user ID in the dataset</summary>
	int GetMaxUserID();
	/// <summary>the maximum item ID in the dataset</summary>
	int GetMaxItemID();

	/// <summary>indices by user</summary>
	/// <remarks>Should be implemented as a lazy data structure</remarks>
	ArrayList<ArrayList<Integer>> ByUser();
	/// <summary>indices by item</summary>
	/// <remarks>Should be implemented as a lazy data structure</remarks>
	ArrayList<ArrayList<Integer>> ByItem();
	/// <summary>get a randomly ordered list of all indices</summary>
	/// <remarks>Should be implemented as a lazy data structure</remarks>
	List<Integer> RandomIndex();
	// TODO add method to force refresh

	/// <summary>rating count by user</summary>
	/// <remarks>Should be implemented as a lazy data structure</remarks>
	int [] CountByUser();
	/// <summary>rating count by item</summary>
	/// <remarks>Should be implemented as a lazy data structure</remarks>
	int [] CountByItem();		
	public void BuildByUserCounts();
	public void BuildByItemCounts();
	// TODO think about getting rid of the interface
	/// <summary>Build the user indices</summary>
	void BuildUserIndices();
	/// <summary>Build the item indices</summary>
	void BuildItemIndices();
	/// <summary>Build the random index</summary>
	void BuildRandomIndex();

	/// <summary>average rating in the dataset</summary>
	double Average();
	
	void Add(int user_id, int item_id, byte rating);
	
	void Add(int user_id, int item_id, float rating);

	/// <summary>Add a new rating</summary>
	/// <param name="user_id">the user ID</param>
	/// <param name="item_id">the item ID</param>
	/// <param name="rating">the rating value</param>
	void Add(int user_id, int item_id, double rating);
	
	void RemoveUser(int user_id);

	/// <summary>Remove all ratings of a given item</summary>
	/// <param name="item_id">the item ID</param>
	void RemoveItem(int item_id);
	public int Count();
	public double GetValues(int index);
	public  double Get(int user_id, int item_id, List<Integer> indexes);
	
	
	CompactHashSet<Integer> GetUsers(List<Integer> indices);
	
	CompactHashSet<Integer> GetItems(List<Integer> indices);

	
	/// <summary>all user IDs in the dataset</summary>
	CompactHashSet<Integer> AllUsers();
}