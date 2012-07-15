package com.rapidminer.operator.RatingPrediction;

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

public class BiPolarSlopeOne extends RatingPredictor {

	   static final long serialVersionUID=3453434;
	   
  		private SkewSymmetricSparseMatrix  diff_matrix_like;
  		private SymetricSparseMatrix_i freq_matrix_like;
  		private SkewSymmetricSparseMatrix  diff_matrix_dislike;
  		private SymetricSparseMatrix_i freq_matrix_dislike;

		private double global_average;
		private double[] user_average;

		///
		public boolean CanPredict(int user_id, int item_id)
		{
			if (user_id > MaxUserID || item_id > MaxItemID)
				return false;

			
			for(int i=0;i<GetRatings().ByUser().get(user_id).size();i++){
				int index=GetRatings().ByUser().get(user_id).get(i);

				if (freq_matrix_like.getLocation(item_id, GetRatings().GetItems().get(index)) != 0)
					return true;
				if (freq_matrix_dislike.getLocation(item_id, GetRatings().GetItems().get(index)) != 0)
					return true;
			}
			return false;
		}

		///
		public double Predict(int user_id, int item_id)
		{
			if (item_id > MaxItemID || user_id > MaxUserID){
				return global_average;
			}

			double prediction = 0.0;
			int frequencies = 0;

			
			for(int i=0;i<GetRatings().ByUser().get(user_id).size();i++){
			int index=GetRatings().ByUser().get(user_id).get(i);
				

					if (GetRatings().GetValues(index) > user_average[user_id])
					{
						int f = freq_matrix_like.getLocation(item_id, GetRatings().GetItems().get(index));
						if (f != 0)
						{
							prediction  += ( diff_matrix_like.getLocation(item_id, GetRatings().GetItems().get(index))+ GetRatings().GetValues(index) ) * f;
							frequencies += f;
						}
					}
					else
					{
						int f = freq_matrix_dislike.getLocation(item_id, GetRatings().GetItems().get(index));
						if (f != 0)
						{
							prediction  += ( diff_matrix_dislike.getLocation(item_id, GetRatings().GetItems().get(index))+ GetRatings().GetValues(index) ) * f;
							frequencies += f;
						}
					}
				}
			
			if (frequencies == 0){
				return global_average;
			}
			double result = (double) (prediction / frequencies);

			if (result > GetMaxRating()){
				return GetMaxRating();
			}
			
			if (result < GetMinRating()){
				return GetMinRating();
			}
			return result;
		}

		///
		public void Train()
		{
			InitModel();

			// default value if no prediction can be made
			global_average = GetRatings().Average();

			// compute difference sums and frequencies
				
			Iterator<Integer> it=GetRatings().AllUsers().iterator();
			
			
			while(it.hasNext()){
			int user_id=it.next();
					
				double user_avg = 0;
				
				for(int j=0;j<GetRatings().ByUser().get(user_id).size();j++){
					
					int index=GetRatings().ByUser().get(user_id).get(j);
					user_avg+=GetRatings().GetValues(index);
					
				}
				
				user_avg /= GetRatings().ByUser().get(user_id).size();

				// store for later use
				user_average[user_id] = user_avg;

				
				for(int j=0;j<GetRatings().ByUser().get(user_id).size();j++){
					int index=GetRatings().ByUser().get(user_id).get(j);
					
					for(int k=0;k<GetRatings().ByUser().get(user_id).size();k++){
						int index2=GetRatings().ByUser().get(user_id).get(k);
						
						
						if (GetRatings().GetValues(index) > user_avg && GetRatings().GetValues(index2) > user_avg)
						{
							freq_matrix_like.setLocation(GetRatings().GetItems().get(index), GetRatings().GetItems().get(index2), freq_matrix_like.getLocation(GetRatings().GetItems().get(index), GetRatings().GetItems().get(index2))+1);
							diff_matrix_like.setLocation(GetRatings().GetItems().get(index), GetRatings().GetItems().get(index2), diff_matrix_like.getLocation(GetRatings().GetItems().get(index), GetRatings().GetItems().get(index2))+(float) (GetRatings().GetValues(index) - GetRatings().GetValues(index2)));
						}
						else if (GetRatings().GetValues(index) < user_avg && GetRatings().GetValues(index2) < user_avg)
						{
							freq_matrix_dislike.setLocation(GetRatings().GetItems().get(index), GetRatings().GetItems().get(index2), freq_matrix_dislike.getLocation(GetRatings().GetItems().get(index), GetRatings().GetItems().get(index2))+1);
							diff_matrix_dislike.setLocation(GetRatings().GetItems().get(index), GetRatings().GetItems().get(index2), diff_matrix_dislike.getLocation(GetRatings().GetItems().get(index), GetRatings().GetItems().get(index2))+(float) (GetRatings().GetValues(index) - GetRatings().GetValues(index2)));
						}
					}
				}
			}

			// compute average differences
			
			for (int i = 0; i <= MaxItemID; i++){
				
				Set<Integer> s=freq_matrix_like.Get(i).keySet();
				Iterator<Integer> it1=s.iterator();
				
				while(it1.hasNext()){
					
				int ind=it1.next();
					diff_matrix_like.setLocation(i, ind, diff_matrix_like.getLocation(i,ind)/freq_matrix_like.getLocation(i, ind));
				}
				
				s=freq_matrix_dislike.Get(i).keySet();
				it1=s.iterator();
				
				
				while(it1.hasNext()){
					
					int ind=it1.next();
						diff_matrix_dislike.setLocation(i, ind, diff_matrix_dislike.getLocation(i,ind)/freq_matrix_dislike.getLocation(i, ind));
					}
				
			}
		}

		///
		protected void InitModel()
		{
			super.InitModel();

			// create data structure
			diff_matrix_like = new SkewSymmetricSparseMatrix(MaxItemID + 1);
			freq_matrix_like = new SymetricSparseMatrix_i(MaxItemID + 1);
			diff_matrix_dislike = new SkewSymmetricSparseMatrix(MaxItemID + 1);
			freq_matrix_dislike = new SymetricSparseMatrix_i(MaxItemID + 1);
			user_average = new double[MaxUserID + 1];
		}

		
		public void AddUsers(List<Integer> users){
			super.AddUsers(users);
			
			double[] user_average_new = new double[users.get(users.size()-1)+ 1];
			
			for(int i=0;i<user_average.length;i++)
				user_average_new[i]=user_average[i];
			
			user_average=user_average_new;	
		}
		
		public void AddItems(List<Integer> items){
			super.AddItems(items);
		}
		
		
		public int AddRatings(List<Integer> users, List<Integer> items, List<Double> ratings){
		
			if(users==null)
				return 1;
			
			super.AddRatings(users, items, ratings);
			global_average = GetRatings().Average();

			// compute difference sums and frequencies
				
			for(int k1=0;k1<users.size();k1++){
			int user_id=users.get(k1);
					
				double user_avg = 0;
				
				for(int j=0;j<GetRatings().ByUser().get(user_id).size();j++){
					
					int index=GetRatings().ByUser().get(user_id).get(j);
					user_avg+=GetRatings().GetValues(index);
					
				}
				
				user_avg /= GetRatings().ByUser().get(user_id).size();

				// store for later use
				user_average[user_id] = user_avg;

				
				for(int j=0;j<GetRatings().ByUser().get(user_id).size();j++){
					int index=GetRatings().ByUser().get(user_id).get(j);
					
					 if(GetRatings().GetItems().get(index)==items.get(k1))
					for(int k=0;k<GetRatings().ByUser().get(user_id).size();k++){
						int index2=GetRatings().ByUser().get(user_id).get(k);
						
						if (GetRatings().GetValues(index) > user_avg && GetRatings().GetValues(index2) > user_avg)
						{
							freq_matrix_like.setLocation(GetRatings().GetItems().get(index), GetRatings().GetItems().get(index2), freq_matrix_like.getLocation(GetRatings().GetItems().get(index), GetRatings().GetItems().get(index2))+1);
							diff_matrix_like.setLocation(GetRatings().GetItems().get(index), GetRatings().GetItems().get(index2), diff_matrix_like.getLocation(GetRatings().GetItems().get(index), GetRatings().GetItems().get(index2))+(float) (GetRatings().GetValues(index) - GetRatings().GetValues(index2)));
						}
						else if (GetRatings().GetValues(index) < user_avg && GetRatings().GetValues(index2) < user_avg)
						{
							freq_matrix_dislike.setLocation(GetRatings().GetItems().get(index), GetRatings().GetItems().get(index2), freq_matrix_dislike.getLocation(GetRatings().GetItems().get(index), GetRatings().GetItems().get(index2))+1);
							diff_matrix_dislike.setLocation(GetRatings().GetItems().get(index), GetRatings().GetItems().get(index2), diff_matrix_dislike.getLocation(GetRatings().GetItems().get(index), GetRatings().GetItems().get(index2))+(float) (GetRatings().GetValues(index) - GetRatings().GetValues(index2)));
						}
					}
				}
			}

			// compute average differences
			
			for (int i = 0; i <= MaxItemID; i++){
				
				Set<Integer> s=freq_matrix_like.Get(i).keySet();
				Iterator<Integer> it1=s.iterator();
				
				while(it1.hasNext()){
					
				int ind=it1.next();
					diff_matrix_like.setLocation(i, ind, diff_matrix_like.getLocation(i,ind)/freq_matrix_like.getLocation(i, ind));
				}
				
				s=freq_matrix_dislike.Get(i).keySet();
				it1=s.iterator();
				
				
				while(it1.hasNext()){
					
					int ind=it1.next();
						diff_matrix_dislike.setLocation(i, ind, diff_matrix_dislike.getLocation(i,ind)/freq_matrix_dislike.getLocation(i, ind));
					}
				
			}
			
			return 1;
			
		}
		
		public void RetrainItems(List<Integer> items){
			super.RetrainItems(items);
			
		}
		
		public void RetrainUsers(List<Integer> users){
			super.RetrainUsers(users);
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
			 return "BipolarSlopeOne";
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
