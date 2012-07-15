package com.rapidminer.ItemRecommendation;

//import java.io.IOException;
//import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.operator.Annotations;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.ProcessingStep;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.LoggingHandler;

/**
*Copyright (C) 2010 Steffen Rendle, Zeno Gantner
*Copyright (C) 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 25.08.2011
*/

	public class MostPopular extends ItemRecommender
	{
		
		static final long serialVersionUID=1942342637;
		/// <summary>View count</summary>
		protected List<Integer> view_count;

		///
		public void Train()
		{
			view_count = new ArrayList<Integer>(MaxItemID + 1);
			for (int i = 0; i <= MaxItemID; i++)
				view_count.add(0);

			for (int u = 0; u < GetFeedback().GetUserMatrix().NumberOfRows(); u++)
				for(int k=0;k<GetFeedback().GetUserMatrix().getLocation(u).size();k++){
					int i=GetFeedback().GetUserMatrix().getLocation(u).get(k);
					view_count.set(i, view_count.get(i)+1);
				}
		}

		///
		public double Predict(int user_id, int item_id)
		{
			if (item_id <= MaxItemID){
				return view_count.get(item_id);
			}
			else
				return 0;
		}

		///
		protected void AddItem(int item_id)
		{
			super.AddItem(item_id);
			while (view_count.size() <= MaxItemID)
				view_count.add(0);
		}

		///
		public void RemoveItem(int item_id)
		{
			super.RemoveItem(item_id);
			view_count.set(item_id, 0);
		}

		///
		public void RemoveUser(int user_id)
		{
			
			for(int k=0;k<GetFeedback().GetUserMatrix().getLocation(user_id).size();k++){
				int i=GetFeedback().GetUserMatrix().getLocation(user_id).get(k);
				view_count.set(i, view_count.get(i)-1);
			}
			super.RemoveUser(user_id);
		}

		public void AddItems(List<Integer> items){
			super.AddItems(items);	
			
			for(int i=0;i<items.size();i++)
				view_count.add(0);

			
		}
		
		public void AddUsers(List<Integer> users){
			super.AddUsers(users);
		}
		
		int AddFeedbacks(List<Integer> users,List<Integer> items){
			
			if(users==null || items==null)
				return 1;
			
			for(int i=0;i<items.size();i++)
				view_count.set(items.get(i), view_count.get(items.get(i))+1);
			return 1;
		}
		
		public void RetrainItems(List<Integer> items){
			super.RetrainItems(items);
		}
		
		public void RetrainUsers(List<Integer> users){
			super.RetrainUsers(users);
		}
		
		///
		public void AddFeedback(int user_id, int item_id)
		{
			super.AddFeedback(user_id, item_id);
			view_count.set(item_id, view_count.get(item_id)+1);
		}

		///
		public void RemoveFeedback(int user_id, int item_id)
		{
			super.RemoveFeedback(user_id, item_id);
			view_count.set(item_id, view_count.get(item_id)-1);
		}

		///
		public void SaveModel(String filename)
		{
			//not needed
		}

		///
		public void LoadModel(String filename)
		{
			//not needed
		}

		///
		public String ToString()
		{
			return "MostPopular";
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
