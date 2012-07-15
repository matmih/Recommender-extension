package com.rapidminer.data;

import java.io.Serializable;
import java.util.List;

/**
*Copyright (C) 2010, 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 29.07.2011
*/

public interface IPosOnlyFeedback extends Serializable
{
	/// <summary>By-user access, users are stored in the rows, items in the culumns</summary>
	IBooleanMatrix GetUserMatrix();

	/// <summary>By-item access, items are stored in the rows, users in the culumns</summary>
	IBooleanMatrix GetItemMatrix();

	/// <summary>the maximum user ID</summary>
	int GetMaxUserID();

	/// <summary>the maximum item ID</summary>
	int GetMaxItemID();

	/// <summary>the number of feedback events</summary>
	int Count();
	
	/// <summary>Add a user-item event to the data structure</summary>
	/// <param name="user_id">the user ID</param>
	/// <param name="item_id">the item ID</param>
	void Add(int user_id, int item_id);
	
	public List<Integer> GetAllItems();
	public List<Integer> GetAllUsers();
	
	/// <summary>Remove a user-item event from the data structure</summary>
	/// <param name="user_id">the user ID</param>
	/// <param name="item_id">the item ID</param>
	void Remove(int user_id, int item_id);
	
	/// <summary>Remove all feedback by a given user</summary>
	/// <param name="user_id">the user id</param>
	void RemoveUser(int user_id);

	/// <summary>Remove all feedback about a given item</summary>
	/// <param name="item_id">the item ID</param>
	void RemoveItem(int item_id);

	/// <summary>Compute the number of overlapping events in two feedback datasets</summary>
	/// <param name="s">the feedback dataset to compare to</param>
	/// <returns>the number of overlapping events, i.e. events that have the same user and item ID</returns>
	int Overlap(IPosOnlyFeedback s);
}
