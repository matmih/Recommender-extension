package com.rapidminer.ItemRecommendation;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.data.IBooleanMatrix;
import com.rapidminer.data.Matrix;
import com.rapidminer.matrixUtils.MatrixUtils;
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

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 02.08.2011
*/

public class WRMF extends MF{
	
	/// <summary>Weighted matrix factorization method proposed by Hu et al. and Pan et al.</summary>
		/// <remarks>
		///   Y. Hu Y. Koren C. Volinsky: Collaborative filtering for implicit feedback datasets,
		///   IEEE International Conference on Data Mining (ICDM), 2008
		///
		///   R. Pan, Y. Zhou, B. Cao, N. N. Liu, R. M. Lukose, M. Scholz, Q. Yang:
		///   One-class collaborative filtering,
		///   IEEE International Conference on Data Mining (ICDM), 2008
		///
		/// We use the fast computation method proposed by Hu et al. and we use a global
		/// weight to penalize observed/unobserved values.
	
	 static final long serialVersionUID=3453434;
	
/// <summary>C position: the weight/confidence that is put on positive observations</summary>
		/// <remarks>The alpha value in Hu et al.</remarks>
		public double GetCPos() {  return c_pos; } 
		
		public void SetCPos(double value)
		{ c_pos = value;	} 
		double c_pos = 1;

		/// <summary>Regularization parameter</summary>
		public double GetRegularization() {  return regularization;	} 
		
		public void SetRegularization(double value){
		regularization = value;	}
		
		double regularization = 0.015;

		///
		public void Iterate()
		{
			// perform alternating parameter fitting
			Optimize(GetFeedback().GetUserMatrix(), user_factors, item_factors);
			Optimize(GetFeedback().GetItemMatrix(), item_factors, user_factors); // TODO create different formulation to save 50% memory
		}
		
		public int GetNumIter(){
			return NumIter;
		}
 
		/// <summary>Optimizes the specified data</summary>
		/// <param name="data">data</param>
		/// <param name="W">W</param>
		/// <param name="H">H</param>
		protected void Optimize(IBooleanMatrix data, Matrix W, Matrix H)
		{
			Matrix HH          = new Matrix(num_factors, num_factors);
			Matrix HC_minus_IH = new Matrix(num_factors, num_factors);
			double[] HCp         = new double[num_factors];
			
			cern.colt.matrix.DoubleMatrix2D m=new cern.colt.matrix.impl.DenseDoubleMatrix2D(num_factors,num_factors); 
			
			cern.colt.matrix.DoubleMatrix2D m_inv;
			// TODO speed up using more parts of that library

			// source code comments are in terms of computing the user factors
			// works the same with users and items exchanged

			// (1) create HH in O(f^2|Items|)
			// HH is symmetric
			for (int f_1 = 0; f_1 < num_factors; f_1++)
				for (int f_2 = 0; f_2 < num_factors; f_2++)
				{
					double d = 0;
					for (int i = 0; i < H.dim1; i++)
						d += H.getLocation(i, f_1) * H.getLocation(i, f_2);
					HH.setLocation(f_1, f_2,d);
				}
			// (2) optimize all U
			// HC_minus_IH is symmetric
			for (int u = 0; u < W.dim1; u++)
			{
				List<Integer> row = data.GetEntriesByRow(u);
				// create HC_minus_IH in O(f^2|S_u|)
				for (int f_1 = 0; f_1 < num_factors; f_1++)
					for (int f_2 = 0; f_2 < num_factors; f_2++)
					{
						double d = 0;
						for(int i1=0;i1<row.size();i1++){
							int i=row.get(i1);
							d += H.getLocation(i, f_1) * H.getLocation(i, f_2) * c_pos;
						}
						HC_minus_IH.setLocation(f_1, f_2, d);
					}
				// create HCp in O(f|S_u|)
				for (int f = 0; f < num_factors; f++)
				{
					double d = 0;
					
					for(int i1=0;i1<row.size();i1++){
						int i=row.get(i1);
						d += H.getLocation(i, f) * (1 + c_pos);
					}
					HCp[f] = d;
				}
				// create m = HH + HC_minus_IH + reg*I
				// m is symmetric
				// the inverse m_inv is symmetric
				
				cern.colt.matrix.linalg.Algebra a=new cern.colt.matrix.linalg.Algebra();
				
				for (int f_1 = 0; f_1 < num_factors; f_1++)
					for (int f_2 = 0; f_2 < num_factors; f_2++)
					{
						double d = HH.getLocation(f_1, f_2) + HC_minus_IH.getLocation(f_1, f_2);
						if (f_1 == f_2)
							d += regularization;
						m.set(f_1, f_2, d);
					}
				m_inv = a.inverse(m); 
				// write back optimal W
				for (int f = 0; f < num_factors; f++)
				{
					double d = 0;
					for (int f_2 = 0; f_2 < num_factors; f_2++)
						d += m_inv.get(f, f_2) * HCp[f_2];
					W.setLocation(u, f, d);
				}
			}
		}
		
		
		
		
		protected void OptimizeUpdate(IBooleanMatrix data,List<Integer> entities, Matrix W, Matrix H)
		{
			Matrix HH          = new Matrix(num_factors, num_factors);
			Matrix HC_minus_IH = new Matrix(num_factors, num_factors);
			double[] HCp         = new double[num_factors];
			
			cern.colt.matrix.DoubleMatrix2D m=new cern.colt.matrix.impl.DenseDoubleMatrix2D(num_factors,num_factors); 
			
			cern.colt.matrix.DoubleMatrix2D m_inv;
			// TODO speed up using more parts of that library

			// source code comments are in terms of computing the user factors
			// works the same with users and items exchanged

			// (1) create HH in O(f^2|Items|)
			// HH is symmetric
			for (int f_1 = 0; f_1 < num_factors; f_1++)
				for (int f_2 = 0; f_2 < num_factors; f_2++)
				{
					double d = 0;
					for (int i = 0; i < H.dim1; i++)
						d += H.getLocation(i, f_1) * H.getLocation(i, f_2);
					HH.setLocation(f_1, f_2,d);
				}
			// (2) optimize all U
			// HC_minus_IH is symmetric
			for (int u = W.dim1-entities.size(); u < W.dim1; u++)
			{
				List<Integer> row = data.GetEntriesByRow(u);
				// create HC_minus_IH in O(f^2|S_u|)
				for (int f_1 = 0; f_1 < num_factors; f_1++)
					for (int f_2 = 0; f_2 < num_factors; f_2++)
					{
						double d = 0;
						for(int i1=0;i1<row.size();i1++){
							int i=row.get(i1);
							d += H.getLocation(i, f_1) * H.getLocation(i, f_2) * c_pos;
						}
						HC_minus_IH.setLocation(f_1, f_2, d);
					}
				// create HCp in O(f|S_u|)
				for (int f = 0; f < num_factors; f++)
				{
					double d = 0;
					
					for(int i1=0;i1<row.size();i1++){
						int i=row.get(i1);
						d += H.getLocation(i, f) * (1 + c_pos);
					}
					HCp[f] = d;
				}
				// create m = HH + HC_minus_IH + reg*I
				// m is symmetric
				// the inverse m_inv is symmetric
				
				cern.colt.matrix.linalg.Algebra a=new cern.colt.matrix.linalg.Algebra();
				
				for (int f_1 = 0; f_1 < num_factors; f_1++)
					for (int f_2 = 0; f_2 < num_factors; f_2++)
					{
						double d = HH.getLocation(f_1, f_2) + HC_minus_IH.getLocation(f_1, f_2);
						if (f_1 == f_2)
							d += regularization;
						m.set(f_1, f_2, d);
					}
				m_inv = a.inverse(m); 
				// write back optimal W
				for (int f = 0; f < num_factors; f++)
				{
					double d = 0;
					for (int f_2 = 0; f_2 < num_factors; f_2++)
						d += m_inv.get(f, f_2) * HCp[f_2];
					W.setLocation(u, f, d);
				}
			}
		}
		
		public void AddUsers(List<Integer> users)
		{
			if(users.size()!=0){
			super.AddUsers(users);
			Matrix um=new Matrix(MaxUserID + 1, GetNumFactors());
			MatrixUtils.RowInitNormal(user_factors, InitMean, InitStdev);
			
			for(int i=0;i<this.user_factors.dim1;i++)
					for(int j=0;j<this.user_factors.dim2;j++)
							um.setLocation(i, j, this.user_factors.getLocation(i, j));		

			this.user_factors=new Matrix(MaxUserID+1,GetNumFactors());
			
			for(int i=0;i<this.user_factors.dim1;i++)
				for(int j=0;j<this.user_factors.dim2;j++)
						this.user_factors.setLocation(i, j, um.getLocation(i, j));	
			}
			
		}
		
		public void AddItems(List<Integer> items)
		{
			if(items.size()!=0){
			super.AddUsers(items);
			Matrix im=new Matrix(MaxItemID + 1, GetNumFactors());
			MatrixUtils.RowInitNormal(item_factors, InitMean, InitStdev);
			
			for(int i=0;i<this.item_factors.dim1;i++)
					for(int j=0;j<this.item_factors.dim2;j++)
							im.setLocation(i, j, this.item_factors.getLocation(i, j));		
			
			this.item_factors=new Matrix(MaxItemID+1,GetNumFactors());
			
			for(int i=0;i<this.item_factors.dim1;i++)
				for(int j=0;j<this.item_factors.dim2;j++)
						this.item_factors.setLocation(i, j, im.getLocation(i, j));	
			}
		}

		
		public void RetrainUsers(List<Integer> users)
		{
			

				OptimizeUpdate(GetFeedback().GetUserMatrix(),users,user_factors, item_factors);
				Optimize(GetFeedback().GetItemMatrix(),item_factors,user_factors);

		}
		

		public void RetrainItems(List<Integer> items)
		{
			
				OptimizeUpdate(GetFeedback().GetItemMatrix(),items, item_factors, user_factors);
				Optimize(GetFeedback().GetUserMatrix(),user_factors,item_factors);
		}
		
		///
		public double ComputeFit()
		{
			return -1;
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
		
		

		///
		public String ToString()
		{
			return String.format("WRMF num_factors={0} regularization={1} c_pos={2} num_iter={3} init_mean={4} init_stdev={5}",
								 GetNumFactors(), GetRegularization(), GetCPos(), NumIter, InitMean, InitStdev);
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
