package com.rapidminer.operator.RatingPrediction;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.rapidminer.data.SkewSymmetricSparseMatrix;
import com.rapidminer.data.SymetricSparseMatrix_i;
import com.rapidminer.operator.Annotations;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.ProcessingStep;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.LoggingHandler;

/**
Copyright (C) 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 08.08.2011
*/

public class _slopeOne extends RatingPredictor {

	   static final long serialVersionUID=3232342;
  		private SkewSymmetricSparseMatrix diff_matrix;
  		private SymetricSparseMatrix_i freq_matrix;

		// TODO one more way to save memory: use short instead of int internally in the SparseMatrix datatypes

		private double global_average;

		///
		protected void InitModel()
		{
			super.InitModel();
			// create data structure
			diff_matrix = new SkewSymmetricSparseMatrix(MaxItemID + 1);
			freq_matrix = new SymetricSparseMatrix_i(MaxItemID + 1);
		}

		///
		public boolean CanPredict(int user_id, int item_id)
		{
			if (user_id > MaxUserID || item_id > MaxItemID)
				return false;

			for(int i=0;i<GetRatings().ByUser().get(user_id).size();i++){
			
				int index=GetRatings().ByUser().get(user_id).get(i);
			
				if (freq_matrix.getLocation(item_id, GetRatings().GetItems().get(index)) != 0)
					return true;
			}
			return false;
	}

		///
		public double Predict(int user_id, int item_id)
		{
			if (item_id > MaxItemID || user_id > MaxUserID)
				return global_average;

			double prediction = 0.0;
			int frequency = 0;

			
			for(int i=0;i<GetRatings().ByUser().get(user_id).size();i++){
				
				int index=GetRatings().ByUser().get(user_id).get(i);
				
				int other_item_id = GetRatings().GetItems().get(index);
				int f = freq_matrix.getLocation(item_id, other_item_id);
				if (f != 0)
				{
					prediction += ( diff_matrix.getLocation(item_id, other_item_id)+GetRatings().GetValues(index)) * f;
					frequency += f;
				}
			}

			if (frequency == 0){
				return global_average;
			}
			
			if(((double) prediction / frequency)>this.max_rating)
				return max_rating;
			else if(((double) prediction / frequency)<this.min_rating)
				return min_rating;
			else
			return (double) prediction / frequency;
		}

		///
		public void Train()
		{
			InitModel();

			// default value if no prediction can be made
			global_average = GetRatings().Average();

			// compute difference sums and frequencies
			
			//memory drain

			for(int i=0;i<GetRatings().ByUser().size();i++){
				
				ArrayList<Integer> by_user_indices=GetRatings().ByUser().get(i);
				
				for(int j=0;j<by_user_indices.size();j++){
					
					int index1=by_user_indices.get(j);
					
					for(int k=j+1;k<by_user_indices.size();k++){
						int index2 = by_user_indices.get(k);
						freq_matrix.setLocation(GetRatings().GetItems().get(index1), GetRatings().GetItems().get(index2), freq_matrix.getLocation(GetRatings().GetItems().get(index1), GetRatings().GetItems().get(index2))+1);
			  			diff_matrix.setLocation(GetRatings().GetItems().get(index1), GetRatings().GetItems().get(index2), diff_matrix.getLocation(GetRatings().GetItems().get(index1), GetRatings().GetItems().get(index2))+(float)(GetRatings().GetValues(index1)-GetRatings().GetValues(index2)));
					}
					
				}
					
				
			}
			

			// compute average differences
			for (int i = 0; i <= MaxItemID; i++){
				
				Set<Integer> s=freq_matrix.Get(i).keySet();
				Iterator<Integer> it=s.iterator();
				
				while(it.hasNext()){
					
				int ind=it.next();
					diff_matrix.setLocation(i, ind, diff_matrix.getLocation(i,ind)/freq_matrix.getLocation(i, ind));
				}
		}	
	}

		
		public void AddUsers(List<Integer> users){
			super.AddUsers(users);
			
		}
		
		public void AddItems(List<Integer> items){
			super.AddItems(items);
			
		}
		
		public int AddRatings(List<Integer> users, List<Integer> items, List<Double> ratings){
			
			if(users==null)
				return 1;
			
			super.AddRatings(users, items, ratings);
			
			global_average = GetRatings().Average();
			
			
			for (int i = 0; i <= MaxItemID; i++){
				
				Set<Integer> s=freq_matrix.Get(i).keySet();
				Iterator<Integer> it=s.iterator();
				
				while(it.hasNext()){
					
				int ind=it.next();
					diff_matrix.setLocation(i, ind, diff_matrix.getLocation(i,ind)*freq_matrix.getLocation(i, ind));
				}
			}	

			 for(int i=0;i<users.size();i++){
			ArrayList<Integer> by_item_indices=GetRatings().ByUser().get(users.get(i));

			for(int j=0;j<by_item_indices.size();j++){
				
				int index1=by_item_indices.get(j);
					if(GetRatings().GetItems().get(index1)==items.get(i))
				for(int k=j+1;k<by_item_indices.size();k++){
					int index2 = by_item_indices.get(k);
					//if(GetRatings().GetItems().get(index2)==items.get(i)){
					freq_matrix.setLocation(GetRatings().GetItems().get(index1), GetRatings().GetItems().get(index2), freq_matrix.getLocation(GetRatings().GetItems().get(index1), GetRatings().GetItems().get(index2))+1);
		  			diff_matrix.setLocation(GetRatings().GetItems().get(index1), GetRatings().GetItems().get(index2), diff_matrix.getLocation(GetRatings().GetItems().get(index1), GetRatings().GetItems().get(index2))+(float)(GetRatings().GetValues(index1)-GetRatings().GetValues(index2)));
		  			//break;
				//	}	
			}
		}
	}
			
			for (int i = 0; i <= MaxItemID; i++){
				
				Set<Integer> s=freq_matrix.Get(i).keySet();
				Iterator<Integer> it=s.iterator();
				
				while(it.hasNext()){
					
				int ind=it.next();
					diff_matrix.setLocation(i, ind, diff_matrix.getLocation(i,ind)/freq_matrix.getLocation(i, ind));
				}
			}	
			
			return 1;
}
		
		public void RetrainUsers(List<Integer> users){
			super.RetrainUsers(users);
		}
		
		public void RetrainItems(List<Integer> items){
			super.RetrainItems(items);
	}
		
		///
		public void LoadModel(String file)
		{
			//not needed
		}

		///
		public void SaveModel(String file)
		{
			//not needed
		}

		///
		public String ToString()
		{
			 return String.format("SlopeOne");
		}
		
		  private String source = null;
		    
		    /** The current working operator. */
		    private transient LoggingHandler loggingHandler;
		    
		    private transient LinkedList<ProcessingStep> processingHistory = new LinkedList<ProcessingStep>();
		    
		    /** Sets the source of this IOObject. */
		    public void setSource(String sourceName) {
		        this.source = sourceName;
		    }

		    /** Returns the source of this IOObject (might return null if the source is unknown). */
		    public String getSource() {
		        return source;
		    }
		    
		    @Override
		    public void appendOperatorToHistory(Operator operator, OutputPort port) {
		    	if (processingHistory == null) {
		    		processingHistory = new LinkedList<ProcessingStep>();
		    	if (operator.getProcess() != null)
		    		processingHistory.add(new ProcessingStep(operator, port));
		    }
		    	ProcessingStep newStep = new ProcessingStep(operator, port);
		    	if (operator.getProcess() != null && (processingHistory.isEmpty() || !processingHistory.getLast().equals(newStep))) {
		    		processingHistory.add(newStep);
		    	}
		    }
		    
		    @Override
		    public List<ProcessingStep> getProcessingHistory() {
		    	if (processingHistory == null)
		    		processingHistory = new LinkedList<ProcessingStep>();
		    	return processingHistory;
		    }
		    
		    /** Gets the logging associated with the operator currently working on this 
		     *  IOObject or the global log service if no operator was set. */
		    public LoggingHandler getLog() {
		        if (this.loggingHandler != null) {
		            return this.loggingHandler;
		        } else {
		            return LogService.getGlobal();
		        }
		    }
		    
		    /** Sets the current working operator, i.e. the operator which is currently 
		     *  working on this IOObject. This might be used for example for logging. */
		    public void setLoggingHandler(LoggingHandler loggingHandler) {
		        this.loggingHandler = loggingHandler;
		    }
		    
			/**
			 * Returns not a copy but the very same object. This is ok for IOObjects
			 * which cannot be altered after creation. However, IOObjects which might be
			 * changed (e.g. {@link com.rapidminer.example.ExampleSet}s) should
			 * overwrite this method and return a proper copy.
			 */
			public IOObject copy() {
				return this;
			}
			
			protected void initWriting() {}
		
			public Annotations getAnnotations(){
				Annotations temp=new Annotations();
				return temp;
			}
		
	}
