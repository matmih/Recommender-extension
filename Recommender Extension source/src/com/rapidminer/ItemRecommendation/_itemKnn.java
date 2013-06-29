package com.rapidminer.ItemRecommendation;

import java.util.LinkedList;
import java.util.List;

import com.rapidminer.data.BinaryCosine;
import com.rapidminer.data.IBooleanMatrix;
import com.rapidminer.data.IMatrix_b;
import com.rapidminer.data.SparseMatrix;
import com.rapidminer.operator.Annotations;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.ProcessingStep;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.LoggingHandler;
import com.rapidminer.tools.container.Tupel;

/**
*Copyright (C) 2010 Steffen Rendle, Zeno Gantner
*Copyright (C) 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 29.07.2011
*Iterative online updates created by Matej Mihelcic
*/

public class _itemKnn extends Knn {

	/// <summary>Unweighted k-nearest neighbor item-based collaborative filtering using cosine similarity</summary>
		/// <remarks>
		/// This recommender does NOT support incremental updates.
		/// </remarks>
	 static final long serialVersionUID=3453434;
		///
		public void Train()
		{
			correlation = BinaryCosine.Create(GetFeedback().GetItemMatrix());
			
			
			int num_items = MaxItemID + 1;
			
			this.nearest_neighbors=new Integer[num_items][];
			for (int i = 0; i < num_items; i++){
				nearest_neighbors[i]=correlation.GetNearestNeighbors(i, k);
			}
			
		}

		
		public void AddUsers(List<Integer> users)
		{
			
		if(users.size()!=0){
			super.AddUsers(users);
			
		correlation.Grow(MaxItemID+1, MaxItemID+1);
			
			 correlation.Setnum_entities(MaxItemID+1);
		}

		}
		
		public void AddItems(List<Integer> items)
		{
			
		if(items.size()!=0){
			super.AddItems(items);
			
			correlation.Grow(MaxItemID+1, MaxItemID+1);
			correlation.Setnum_entities(MaxItemID+1);
			
		}
		
	}
		
		int AddFeedbacks(List<Integer> users,List<Integer> items){
			
			if(users==null || items==null)
				return 1;
			
			if(users.size()!=0){
			IMatrix_b transpose = GetFeedback().GetItemMatrix().Transpose();
			SparseMatrix overlap = new SparseMatrix(GetFeedback().GetItemMatrix().NumberOfRows(), GetFeedback().GetItemMatrix().NumberOfRows());
			com.rapidminer.data.CompactHashSet<Integer> viewed=new com.rapidminer.data.CompactHashSet<Integer>();
			int prevus=-1;
			
			for(int i1=0;i1<users.size();i1++)//users list must be sorted
			{
				List<Integer> row = ((IBooleanMatrix) transpose).GetEntriesByRow(users.get(i1));
				if(prevus!=users.get(i1))
					 viewed.clear();
				
					int x=items.get(i1);
					viewed.add(x);
					
					for (int j = 0; j < row.size(); j++)
					{
						int y = row.get(j);
						
						if(viewed.contains(y))
							continue;
							
						if (x < y){
							
							int t=overlap.getLocation1(x, y);
							t++;
							overlap.setLocation(x, y, t);
						}
						else{
							
							int t=overlap.getLocation1(y, x);
							t++;
							overlap.setLocation(y, x, t);
						}

					}
					prevus=users.get(i1);
			}
			
			List<Tupel<Integer,Integer>> temp=overlap.NonEmptyEntryIDs();
			
			for(int i=0;i<temp.size();i++){
				
				int x=temp.get(i).getFirst();
				
				int y=temp.get(i).getSecond();
				 
				 float value= (float) (overlap.getLocation(x, y) / Math.sqrt(GetFeedback().GetItemMatrix().NumEntriesByRow(x) * GetFeedback().GetItemMatrix().NumEntriesByRow(y) ));
				 correlation.setLocation(x, y,correlation.getLocation(x, y)*0.9999f+value);
			}
		}
			return 1;
		}
		
		public void RetrainUsers(List<Integer> users)
		{
			
	}
		
		public void RetrainItems(List<Integer> items)
		{
			
		if(items.size()!=0){
			int num_items = MaxItemID + 1;
			
			Integer[][] nn=new Integer[num_items][];
			
			for(int i=0;i<this.nearest_neighbors.length;i++)//num_items-items.size()
					nn[i]=this.nearest_neighbors[i];
			
			
			this.nearest_neighbors=new Integer[num_items][];
			
			for(int i=0;i<num_items;i++)
				this.nearest_neighbors[i]=nn[i];
			
			for(int i=0;i<items.size();i++)
				this.nearest_neighbors[items.get(i)]=correlation.GetNearestNeighbors(items.get(i), k); //all query set items
			
		}
	}

		
		///
		public double Predict(int user_id, int item_id)
		{
			if ((user_id < 0) || (user_id > MaxUserID)){
				return 0;
			}
			if ((item_id < 0) || (item_id > MaxItemID)){
				return 0;
			}

			int count = 0;
			
			for(int i1=0;i1<this.nearest_neighbors[item_id].length;i1++){
				int neighbor=this.nearest_neighbors[item_id][i1];	
				if (GetFeedback().GetUserMatrix().getLocation(user_id, neighbor)){ //neighbour,user_id
					count++;
				}
			}
			return (double) count / k;
		}

		///
		public String ToString()
		{
			return String.format("ItemKNN k={0}" , k == Integer.MAX_VALUE ? "inf" : k);
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
