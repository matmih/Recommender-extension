package com.rapidminer.data;
import java.util.List;

/**
*Copyright (C) 2010, 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 29.07.2011
*/

public class PosOnlyFeedback implements IPosOnlyFeedback {

	 static final long serialVersionUID=3453435;
	
		/// <summary>By-user access, users are stored in the rows, items in the culumns</summary>
		public IBooleanMatrix UserMatrix;

		/// <summary>By-item access, items are stored in the rows, users in the culumns</summary>
		public IBooleanMatrix GetItemMatrix()
		{
				if (item_matrix == null)
					item_matrix = (IBooleanMatrix) UserMatrix.Transpose();

				return item_matrix;
		}
		
		public IBooleanMatrix GetUserMatrix(){
			return UserMatrix;
		}
		
		public IBooleanMatrix item_matrix;

		/// <summary>the maximum user ID</summary>
		public int MaxUserID;

		/// <summary>the maximum item ID</summary>
		public int MaxItemID;

		/// <summary>the number of feedback events</summary>
		public int Count() {return UserMatrix.NumberOfEntries(); }

		/// <summary>all users that have given feedback</summary>
		public List<Integer> GetAllUsers() { return UserMatrix.NonEmptyRowIDs(); }

		/// <summary>all items mentioned at least once</summary>
		public List<Integer> GetAllItems() {
			
				if (item_matrix == null)
					return UserMatrix.NonEmptyColumnIDs();
				else
					return GetItemMatrix().NonEmptyRowIDs();
			
		}
		
		public int GetMaxUserID(){
			return MaxUserID;
		}
		
		public int GetMaxItemID(){
			return MaxItemID;
		}
		

		/// <summary>Default constructor</summary>
		public PosOnlyFeedback()
		{
			UserMatrix = new SparseBooleanMatrix();
		}

		/// <summary>Create a PosOnlyFeedback object from an existing user-item matrix</summary>
		/// <param name="user_matrix">the user-item matrix</param>
		public PosOnlyFeedback(SparseBooleanMatrix user_matrix)
		{
			UserMatrix = user_matrix;
			MaxUserID = user_matrix.NumberOfRows();
			MaxItemID = user_matrix.NumberOfColumns();
		}

		/// <summary>Add a user-item event to the data structure</summary>
		/// <param name="user_id">the user ID</param>
		/// <param name="item_id">the item ID</param>
		public void Add(int user_id, int item_id)
		{
			UserMatrix.setLocation(user_id, item_id, true);
			if (item_matrix != null)
				item_matrix.setLocation(item_id, user_id, true);

			if (user_id > MaxUserID)
				MaxUserID = user_id;

			if (item_id > MaxItemID)
				MaxItemID = item_id;
		}

		/// <summary>Remove a user-item event from the data structure</summary>
		/// <param name="user_id">the user ID</param>
		/// <param name="item_id">the item ID</param>
		public void Remove(int user_id, int item_id)
		{
			UserMatrix.setLocation(user_id, item_id, false);
			if (item_matrix != null)
				item_matrix.setLocation(item_id, user_id, false);
		}

		/// <summary>Remove all feedback by a given user</summary>
		/// <param name="user_id">the user id</param>
		public void RemoveUser(int user_id)
		{
			UserMatrix.getLocation(user_id).clear();
			if (item_matrix != null)
				for (int i = 0; i < item_matrix.NumberOfRows(); i++)
					item_matrix.getLocation(i).remove(user_id);
		}

		/// <summary>Remove all feedback about a given item</summary>
		/// <param name="item_id">the item ID</param>
		public void RemoveItem(int item_id)
		{
			for (int u = 0; u < UserMatrix.NumberOfRows(); u++)
				UserMatrix.getLocation(u).remove(item_id);

			if (item_matrix != null)
				item_matrix.getLocation(item_id).clear();
		}

		/// <summary>Compute the number of overlapping events in two feedback datasets</summary>
		/// <param name="s">the feedback dataset to compare to</param>
		/// <returns>the number of overlapping events, i.e. events that have the same user and item ID</returns>
		public int Overlap(IPosOnlyFeedback s)
		{
			return UserMatrix.Overlap(s.GetUserMatrix());
		}
	}
