package com.rapidminer.operator.RatingPrediction;

import com.rapidminer.data.IEntityMapping;
import com.rapidminer.data.IRatings;
import com.rapidminer.operator.ResultObjectAdapter;
import java.util.List;


/**
*Copyright (C) 2010 Steffen Rendle,Zeno Gantner
*Copyright (C) 2011 Zeno Gantner
*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 22.07.2011
*/

public abstract class RatingPredictor extends ResultObjectAdapter implements IRatingPredictor/*, IOObject*/
{
	
	static final long serialVersionUID=1942342342;
	/// <summary>Maximum user ID</summary>
	public int MaxUserID;

	/// <summary>Maximum item ID</summary>
	public int MaxItemID;

	/// <summary>The max rating value</summary>
	public double GetMaxRating() {  return max_rating; }
	
	public IEntityMapping user_mapping, item_mapping;
	

	public void SetMaxRating(double value){
	max_rating = value; 
	} 
	/// <summary>The max rating value</summary>
	protected double max_rating;

	/// <summary>The min rating value</summary>
	public  double GetMinRating() {
		return min_rating; } 
		
	public void SetMinRating(double value){
		 min_rating = value; 
		 }
	/// <summary>The min rating value</summary>
	protected double min_rating;

	/// <summary>true if users shall be updated when doing incremental updates</summary>
	/// <remarks>
	/// Default is true.
	/// Set to false if you do not want any updates to the user model parameters when doing incremental updates.
	/// </remarks>
	public boolean UpdateUsers;

	/// <summary>true if items shall be updated when doing incremental updates</summary>
	/// <remarks>
	/// Default is true.
	/// Set to false if you do not want any updates to the item model parameters when doing incremental updates.
	/// </remarks>
	public boolean UpdateItems;

	/// <summary>The rating data</summary>
	public IRatings GetRatings() { return ratings;} 
	
	public void SetRatings(IRatings value){

	ratings = value; } 

	/// <summary>rating data</summary>
	protected IRatings ratings;

	/// <summary>Default constructor</summary>
	public RatingPredictor()
	{
		UpdateUsers = true;
		UpdateItems = true;
	}

	/// <summary>create a shallow copy of the object</summary>
	/*public Object Clone()
	{
		return this.MemberwiseClone();
	}*/

	///
	public abstract double Predict(int user_id, int item_id);

	/// <summary>Inits the recommender model</summary>
	/// <remarks>
	/// This method is called by the Train() method.
	/// When overriding, please call base.InitModel() to get the functions performed in the base class.
	/// </remarks>
	protected  void InitModel()
	{
		MaxUserID = GetRatings().GetMaxUserID();
		MaxItemID = GetRatings().GetMaxItemID();
	}

	///
	public abstract void Train();

	///
	public abstract void SaveModel(String filename);

	///
	public abstract void LoadModel(String filename);

	///
	public boolean CanPredict(int user_id, int item_id)
	{
		return (user_id <= MaxUserID && user_id >= 0 && item_id <= MaxItemID && item_id >= 0);
	}

	///
	public void AddRating(int user_id, int item_id, double rating)
	{
		if (user_id > MaxUserID)
			AddUser(user_id);
		if (item_id > MaxItemID)
			AddItem(item_id);

		ratings.Add(user_id, item_id, rating);
	}


	///
	protected  void AddUser(int user_id)
	{
		MaxUserID = Math.max(MaxUserID, user_id);
	}
	
	public void AddUsers(List<Integer> users)
	{
		MaxUserID=users.get(users.size()-1);	
	}
	
	public void RetrainUsers(List<Integer> users){
		
	}
	
	public void RetrainItems(List<Integer> items){
		
	}
	
	public void AddItems(List<Integer> items)
	{
	MaxItemID=items.get(items.size()-1);		
	}

	///
	protected  void AddItem(int item_id)
	{
		MaxItemID = Math.max(MaxItemID, item_id);
	}

	///
	public void RemoveUser(int user_id)
	{
		if (user_id == MaxUserID)
			MaxUserID--;
		ratings.RemoveUser(user_id);
	}

	///
	public void RemoveItem(int item_id)
	{
		if (item_id == MaxItemID)
			MaxItemID--;
		ratings.RemoveItem(item_id);
	}
	
	
	public int AddRatings(List<Integer> users, List<Integer> items, List<Double> ratings){
		
		if(users==null)
			return 0;
		
		for(int i=0;i<users.size();i++)
			this.ratings.Add(users.get(i), items.get(i), ratings.get(i));
			
		return 0;
	}
	
	public  void UpdateRating(int user_id, int item_id, double rating)
	{

	}

	///
	public  void RemoveRating(int user_id, int item_id)
	{
	
	}

}
