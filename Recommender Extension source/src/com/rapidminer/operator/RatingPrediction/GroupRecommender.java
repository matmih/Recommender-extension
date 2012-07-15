package com.rapidminer.operator.RatingPrediction;

/**
 * Group model class for Rating Prediction algorithms
 * 
 * @see com.rapidminer.RatingPrediction.GroupRecommender
 * @author Matej Mihelcic (Ruðer Boškoviæ Institute)

*/

import java.util.List;

public class GroupRecommender extends RatingPredictor{

	static final long serialVersionUID=1942342392;
	
	List<RatingPredictor> recommenders;
	List<Double> weightList;
	double defaultWeight;
	
	public void SetDWeight(double value){
		defaultWeight=value;
	}
	
	public void SetWeights(List<Double> value){
		weightList=value;
	}
	
	public void SetRecommenders(List<RatingPredictor> value){
		recommenders=value;
	}
	
	public List<RatingPredictor> GetRecommenders(){
		return recommenders;
	}
	
	public void Train(){
		for(int i=0;i<recommenders.size();i++)
			recommenders.get(i).Train();
	}
	
	public void AddUsers(List<Integer> users)
	{
	
		for(int i=0;i<recommenders.size();i++)
			recommenders.get(i).AddUsers(users);
	}
	
	public void AddItems(List<Integer> items)
	{
		for(int i=0;i<recommenders.size();i++)
			recommenders.get(i).AddItems(items);
	}
	
	public void RetrainUsers(List<Integer> users)
	{
		for(int i=0;i<recommenders.size();i++)
			recommenders.get(i).RetrainUsers(users);
	}
	
	public void RetrainItems(List<Integer> items)
	{
		for(int i=0;i<recommenders.size();i++)
			recommenders.get(i).RetrainItems(items);
	}
	
	public double Predict(int user_id, int item_id){		
		double rating=0;
		double weightSum=0;
		
		if(weightList.size()<=recommenders.size()){
		for(int i=0;i<weightList.size();i++){
			rating+=recommenders.get(i).Predict(user_id, item_id)* Double.valueOf(weightList.get(i));
			weightSum += Double.valueOf(weightList.get(i));
		}
		
		for(int i=weightList.size();i<recommenders.size();i++){
				rating+=recommenders.get(i).Predict(user_id, item_id)*defaultWeight;
		weightSum+=defaultWeight;		
		}
	}
		else{
			
			for(int i=0;i<recommenders.size();i++){
				rating+=recommenders.get(i).Predict(user_id, item_id)* Double.valueOf(weightList.get(i));
				weightSum += Double.valueOf(weightList.get(i));		
		  }
	}
		
		return rating/weightSum;
		
	}
	
	public  void SaveModel(String file)
	{
		//not needed
	}

	///
	public void LoadModel(String file)
	{
		//not needed
	}
	
	public  String ToString()
	{
		
		return "Group Recommender";
	}
	
}
