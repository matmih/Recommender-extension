package com.rapidminer.ItemRecommendation;

import com.rapidminer.data.IPosOnlyFeedback;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.ResultObjectAdapter;
import java.util.List;
import com.rapidminer.data.IEntityMapping;

/**
*Copyright (C) 2010 Steffen Rendle, Zeno Gantner
*Copyright (C) 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 22.07.2011 
*/

public abstract class ItemRecommender extends ResultObjectAdapter implements IOObject
{
	/// <summary>Abstract item recommender class that loads the training data into memory</summary>
		/// <remarks>
		/// The data is stored in two sparse matrices:
		/// one column-wise and one row-wise
	
	 static final long serialVersionUID=3453434;
	/// <summary>Maximum user ID</summary>
	public int MaxUserID;

	/// <summary>Maximum item ID</summary>
	public int MaxItemID;
	
	public IEntityMapping user_mapping, item_mapping; //dodano

	/// <summary>the feedback data to be used for training</summary>
	public IPosOnlyFeedback GetFeedback()
	{
		 return this.feedback; 
	}
	
	public void SetFeedback(IPosOnlyFeedback feed){
		
			this.feedback = feed;
			MaxUserID = feedback.GetMaxUserID();
			MaxItemID = feedback.GetMaxItemID();

	}
	IPosOnlyFeedback feedback;

	///
	public abstract double Predict(int user_id, int item_id);

	///
	public boolean CanPredict(int user_id, int item_id)
	{
		return (user_id <= MaxUserID && user_id >= 0 && item_id <= MaxItemID && item_id >= 0);
	}

	///
	public abstract void Train();

	///
	public abstract void LoadModel(String filename);

	///
	public abstract void SaveModel(String filename);

	///
	public void AddFeedback(int user_id, int item_id)
	{
		if (user_id > MaxUserID)
			AddUser(user_id);
		if (item_id > MaxItemID)
			AddItem(item_id);

		GetFeedback().Add(user_id, item_id);
	}

	///
	public void RemoveFeedback(int user_id, int item_id)
	{
		if (user_id > MaxUserID)
			throw new IllegalArgumentException("Unknown user " + user_id);
		if (item_id > MaxItemID)
			throw new IllegalArgumentException("Unknown item " + item_id);

		GetFeedback().Remove(user_id, item_id);
	}

	///
	protected void AddUser(int user_id)
	{
		if (user_id > MaxUserID)
			MaxUserID = user_id;
	}
	
	public void AddUsers(List<Integer> users)
	{
				 MaxUserID=users.get(users.size()-1);//i
	}
	
	
	int  AddFeedbacks(List<Integer> users, List<Integer> items){
		return 0;
	}

	public void RetrainUsers(List<Integer> users){
	
	}
	
	public void RetrainItems(List<Integer> items){
		
	}

	///
	protected void AddItem(int item_id)
	{
		if (item_id > MaxItemID)
			MaxItemID = item_id;
	}

	public void AddItems(List<Integer> items)
	{
				 MaxItemID=items.get(items.size()-1);//i
	}
	
	///
	public void RemoveUser(int user_id)
	{
		GetFeedback().RemoveUser(user_id);

		if (user_id == MaxUserID)
			MaxUserID--;
	}

	///
	public void RemoveItem(int item_id)
	{
		GetFeedback().RemoveItem(item_id);

		if (item_id == MaxItemID)
			MaxItemID--;
	}
}