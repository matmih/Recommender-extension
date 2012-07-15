package com.rapidminer.ItemRecommendation;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import com.rapidminer.RatingPrediction.IIterativeModel;
import com.rapidminer.matrixUtils.MatrixUtils;
import com.rapidminer.matrixUtils.VectorUtils;
import com.rapidminer.operator.Annotations;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.ProcessingStep;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.LoggingHandler;

/**
*Copyright (C) 2010 Zeno Gantner, Christoph Freudenthaler
*Copyright (C) 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 01.08.2011
*/

public class BPRMF extends MF implements IIterativeModel
{
	/// Matrix factorization model for item prediction optimized using BPR-Opt
			/// </summary>
			/// <remarks>
			/// Steffen Rendle, Christoph Freudenthaler, Zeno Gantner, Lars Schmidt-Thieme:
			/// BPR: Bayesian Personalized Ranking from Implicit Feedback.
			/// Proceedings of the 25th Conference on Uncertainty in Artificial Intelligence (UAI 2009),
			/// Montreal, Canada, 2009.
			///
	
	static final long serialVersionUID=3232342;
	/// <summary>Fast, but memory-intensive sampling</summary>
	protected boolean fast_sampling = false;

	/// <summary>Item bias terms</summary>
	protected double[] item_bias;

	/// <summary>Fast sampling memory limit, in MiB</summary>
	public int GetFastSamplingMemoryLimit()
	{  return fast_sampling_memory_limit; }	
	
	public void SetFastSamplingMemoryLimit(int value){
	fast_sampling_memory_limit = value; } 
	/// <summary>Fast sampling memory limit, in MiB</summary>
	protected int fast_sampling_memory_limit = 1024;

	/// <summary>Regularization parameter for the bias term</summary>
	public double BiasReg;
	/// <summary>Learning rate alpha</summary>
	
	public double GetLearnRate(){
		return learn_rate; } 
	
	public void SetLearnRate(double value){
	learn_rate = value; } 
	/// <summary>Learning rate alpha</summary>
	protected double learn_rate = 0.05;

	/// <summary>Regularization parameter for user factors</summary>
	public double GetRegU() {
		return reg_u; } 
	
	public void SetRegU(double value) 
	{ reg_u = value; } 
	/// <summary>Regularization parameter for user factors</summary>
	protected double reg_u = 0.0025;

	/// <summary>Regularization parameter for positive item factors</summary>
	public double GetRegI() { 
		 return reg_i; }
	
	public void SetRegI(double value){
	     reg_i = value;	} 
	/// <summary>Regularization parameter for positive item factors</summary>
	protected double reg_i = 0.0025;

	/// <summary>Regularization parameter for negative item factors</summary>
	public double GetRegJ() { 
		return reg_j; } 
	
	public void SetRegJ(double value)
	{ reg_j = value; } 
	/// <summary>Regularization parameter for negative item factors</summary>
	protected double reg_j = 0.00025;

	/// <summary>support data structure for fast sampling</summary>
	protected List<List<Integer>> user_pos_items;
	/// <summary>support data structure for fast sampling</summary>
	protected List<List<Integer>> user_neg_items;

	/// <summary>Use bold driver heuristics for learning rate adaption</summary>
	/// <remarks>
	/// See
	/// Rainer Gemulla, Peter J. Haas, Erik Nijkamp, Yannis Sismanis:
	/// Large-Scale Matrix Factorization with Distributed Stochastic Gradient Descent
	/// 2011
	/// </remarks>
	public boolean BoldDriver;

	/// <summary>Loss for the last iteration, used by bold driver heuristics</summary>
	double last_loss = Double.NEGATIVE_INFINITY;

	/// <summary>array of user components of triples to use for approximate loss computation</summary>
	int[] loss_sample_u;
	/// <summary>array of positive item components of triples to use for approximate loss computation</summary>
	int[] loss_sample_i;
	/// <summary>array of negative item components of triples to use for approximate loss computation</summary>
	int[] loss_sample_j;

	
	/// <summary>Random number generator</summary>
	protected com.rapidminer.utils.Random random;

	///
	protected void InitModel()
	{
		super.InitModel();
		System.out.println("MaxItemID unutar inicijalizacije "+MaxItemID);
		item_bias = new double[MaxItemID + 1];
		CheckSampling(); // TODO rename
	}

	///
	public void Train()
	{
		InitModel();

		random = com.rapidminer.utils.Random.GetInstance(); // TODO move to training

		if (BoldDriver)
		{
			int num_sample_triples = (int) Math.sqrt(GetFeedback().GetMaxUserID()) * 100; // TODO make configurable

			loss_sample_u = new int[num_sample_triples];
			loss_sample_i = new int[num_sample_triples];
			loss_sample_j = new int[num_sample_triples];

			int u=0, i=0, j=0;
			for (int c = 0; c < num_sample_triples; c++)
			{
				SampleTriple(u, i, j);
				loss_sample_u[c] = u;
				loss_sample_i[c] = i;
				loss_sample_j[c] = j;
			}

			last_loss = ComputeLoss();
		}

		for (int i = 0; i < NumIter; i++)
			Iterate(); 
	}

	/// <summary>Perform one iteration of stochastic gradient ascent over the training data</summary>
	/// <remarks>
	/// One iteration is samples number of positive entries in the training matrix times
	/// </remarks>
	public void Iterate()
	{
		int num_pos_events = GetFeedback().Count();

		int user_id=0, item_id_1=0, item_id_2=0;
		int [] trojka;
		
		for (int i = 0; i < num_pos_events; i++)
		{
			trojka=SampleTriple( user_id, item_id_1,  item_id_2);
			user_id=trojka[0]; item_id_1=trojka[1]; item_id_2=trojka[2];
			UpdateFactors(trojka[0], trojka[1], trojka[2], true, true, true);
		}

		if (BoldDriver)
		{
			double loss = ComputeLoss();

			if (loss > last_loss)
				SetLearnRate(GetLearnRate()*0.5);
			else if (loss < last_loss)
				SetLearnRate(GetLearnRate()*1.1);

			last_loss = loss;
		}
	}

	/// <summary>Sample another item, given the first one and the user</summary>
	/// <param name="u">the user ID</param>
	/// <param name="i">the ID of the given item</param>
	/// <param name="j">the ID of the other item</param>
	/// <returns>true if the given item was already seen by user u</returns>
	protected boolean SampleOtherItem(int u, int i, int j) //popraviti
	{
		boolean item_is_positive = GetFeedback().GetUserMatrix().getLocation(u, i);

		if (fast_sampling)
		{
			if (item_is_positive) 
			{
				int rindex = random.nextInt(user_neg_items.get(u).size());
				j = user_neg_items.get(u).get(rindex);
			}
			else
			{
				int rindex = random.nextInt(user_pos_items.get(u).size());
				j = user_pos_items.get(u).get(rindex);
			}
		}
		else
		{
			do
				j = random.nextInt(MaxItemID + 1);
			while (GetFeedback().GetUserMatrix().getLocation(u, j) != item_is_positive);
		}

		return item_is_positive;
	}

	/// <summary>Sample a pair of items, given a user</summary>
	/// <param name="u">the user ID</param>
	/// <param name="i">the ID of the first item</param>
	/// <param name="j">the ID of the second item</param>
	protected int[] SampleItemPair(int u,  int i, int j)
	{
		
		int[] par=new int[2];
		
		if (fast_sampling)
		{
			int rindex;

			rindex = random.nextInt(user_pos_items.get(u).size());
			i = user_pos_items.get(u).get(rindex); // TODO use this also with slow sampling?

		
			rindex = random.nextInt(user_neg_items.get(u).size());
			j = user_neg_items.get(u).get(rindex);
		}
		else
		{
			List<Integer> user_items = GetFeedback().GetUserMatrix().getLocation(u);
			i = user_items.get(random.nextInt(user_items.size()));
			do
				j = random.nextInt(MaxItemID + 1);
			while (GetFeedback().GetUserMatrix().getLocation(u, j));
		}
		
		par[0]=i;
		par[1]=j;
		return par;
	}

	/// <summary>Sample a user that has viewed at least one and not all items</summary>
	/// <returns>the user ID</returns>
	protected int SampleUser()
	{
		while (true)
		{
			int u = random.nextInt(MaxUserID + 1);
			List<Integer> user_items = GetFeedback().GetUserMatrix().getLocation(u);
			if (user_items.size() == 0 || user_items.size() == MaxItemID + 1)
				continue;
			return u;
		}
	}

	/// <summary>Sample a triple for BPR learning</summary>
	/// <param name="u">the user ID</param>
	/// <param name="i">the ID of the first item</param>
	/// <param name="j">the ID of the second item</param>
	protected int[] SampleTriple(int u, int i, int j)
	{
		int[] trojka=new int[3];
		int [] par=new int[2];
		u = SampleUser();
		trojka[0]=u;
		par=SampleItemPair(u,i, j);
		trojka[1]=par[0];
		trojka[2]=par[1];
		return trojka;
	}

	/// <summary>Update latent factors according to the stochastic gradient descent update rule</summary>
	/// <param name="u">the user ID</param>
	/// <param name="i">the ID of the first item</param>
	/// <param name="j">the ID of the second item</param>
	/// <param name="update_u">if true, update the user latent factors</param>
	/// <param name="update_i">if true, update the latent factors of the first item</param>
	/// <param name="update_j">if true, update the latent factors of the second item</param>
	protected  void UpdateFactors(int u, int i, int j, boolean update_u, boolean update_i, boolean update_j)
	{
			
		double x_uij = Predict(u, i) - Predict(u, j);

		double one_over_one_plus_ex = 1 / (1 + Math.exp(x_uij));

		// adjust bias terms
		if (update_i)
		{
			double bias_update = one_over_one_plus_ex - BiasReg * item_bias[i];
			item_bias[i] += learn_rate * bias_update;
		}

		if (update_j)
		{
;
			double bias_update = -one_over_one_plus_ex - BiasReg * item_bias[j];
			item_bias[j] += learn_rate * bias_update;
		}

		// adjust factors
		for (int f = 0; f < num_factors; f++)
		{
			double w_uf = user_factors.getLocation(u, f);
			double h_if = item_factors.getLocation(i, f);
			double h_jf = item_factors.getLocation(j, f);

			if (update_u)
			{
				double uf_update = (h_if - h_jf) * one_over_one_plus_ex - reg_u * w_uf;
				user_factors.setLocation(u, f,w_uf + learn_rate * uf_update);
			}

			if (update_i)
			{
				double if_update = w_uf * one_over_one_plus_ex - reg_i * h_if;
				item_factors.setLocation(i, f,h_if + learn_rate * if_update);
			}

			if (update_j)
			{
				double jf_update = -w_uf  * one_over_one_plus_ex - reg_j * h_jf;
				item_factors.setLocation(j, f, h_jf + learn_rate * jf_update);
			}
		}
	}

	///
	public void AddFeedback(int user_id, int item_id)
	{
		super.AddFeedback(user_id, item_id);

		if (fast_sampling)
			CreateFastSamplingData(user_id);

		// retrain
		RetrainUser(user_id);
		RetrainItem(item_id);
	}

	///
	public void RemoveFeedback(int user_id, int item_id)
	{
		super.RemoveFeedback(user_id, item_id);

		if (fast_sampling)
			CreateFastSamplingData(user_id);

		// retrain
		RetrainUser(user_id);
		RetrainItem(item_id);
	}

	///
	protected void AddUser(int user_id)
	{
		/*super.AddUser(user_id);

		user_factors.AddRows(user_id + 1);
		MatrixUtils.RowInitNormal(user_factors, InitMean, InitStdev, user_id);*/
	}

	public void AddUsers(List<Integer> users)
	{

		super.AddUsers(users);

		user_factors.AddRows(users.get(users.size()-1) + 1);
		
		for(int i=0;i<users.size();i++){
		MatrixUtils.RowInitNormal(user_factors, InitMean, InitStdev, users.get(i));
		if (fast_sampling)
			CreateFastSamplingData(users.get(i));
		}
		
	}
	
	///
	protected  void AddItem(int item_id)
	{
		/*super.AddItem(item_id);

		item_factors.AddRows(item_id + 1);
		MatrixUtils.RowInitNormal(item_factors, InitMean, InitStdev, item_id);*/
	}
	
	public void AddItems(List<Integer> items)
	{

		super.AddItems(items);

		item_factors.AddRows(items.get(items.size()-1) + 1);
		
		for(int i=0;i<items.size();i++)
		MatrixUtils.RowInitNormal(item_factors, InitMean, InitStdev, items.get(i));
		
	}

	///
	public void RemoveUser(int user_id)
	{
		super.RemoveUser(user_id);

		if (fast_sampling)
		{
			user_pos_items.set(user_id, null);
			user_neg_items.set(user_id, null);
		}

		// set user latent factors to zero
		user_factors.SetRowToOneValue(user_id, 0);
	}

	///
	public void RemoveItem(int item_id)
	{
		super.RemoveItem(item_id);

		// TODO remove from fast sampling data structures
		//      (however: not needed if all feedback events have been removed properly before)

		// set item latent factors to zero
		item_factors.SetRowToOneValue(item_id, 0);
	}

	/// <summary>Retrain the latent factors of a given user</summary>
	/// <param name="user_id">the user ID</param>
	protected void RetrainUser(int user_id)
	{
		MatrixUtils.RowInitNormal(user_factors, InitMean, InitStdev, user_id);
		
		List<Integer> user_items = GetFeedback().GetUserMatrix().getLocation(user_id);
		for (int i = 0; i < user_items.size(); i++)
		{
			int item_id_1=0, item_id_2=0;
			SampleItemPair(user_id,  item_id_1, item_id_2);
			UpdateFactors(user_id, item_id_1, item_id_2, true, false, false);
		}
	}
	
	public void RetrainUsers(List<Integer> users)
	{
		
		for(int i1=0;i1<users.size();i1++){
		MatrixUtils.RowInitNormal(user_factors, InitMean, InitStdev, users.get(i1));
		
		List<Integer> user_items = GetFeedback().GetUserMatrix().getLocation(users.get(i1));
		for (int i = 0; i < user_items.size(); i++)
		{
			int item_id_1=0, item_id_2=0;
			SampleItemPair(users.get(i1),  item_id_1, item_id_2);
			UpdateFactors(users.get(i1), item_id_1, item_id_2, true, false, false);
		}
	}
}
	
	/// <summary>Retrain the latent factors of a given item</summary>
	/// <param name="item_id">the item ID</param>
	protected void RetrainItem(int item_id)
	{
		MatrixUtils.RowInitNormal(item_factors, InitMean, InitStdev, item_id);

		int num_pos_events = GetFeedback().GetUserMatrix().NumberOfEntries();
		int num_item_iterations = num_pos_events  / (MaxItemID + 1);
		for (int i = 0; i < num_item_iterations; i++) {
			// remark: the item may be updated more or less frequently than in the normal from-scratch training
			int user_id = SampleUser();
			int other_item_id=0;
			boolean item_is_positive = SampleOtherItem(user_id, item_id, other_item_id);

			if (item_is_positive)
				UpdateFactors(user_id, item_id, other_item_id, false, true, false);
			else
				UpdateFactors(user_id, other_item_id, item_id, false, false, true);
		}
	}
	
	public void RetrainItems(List<Integer> items)
	{
		
		double[] item_bias_new=new double[MaxItemID+1];
		
		for(int k=0;k<item_bias.length;k++)
			item_bias_new[k]=item_bias[k];
		
		for(int k=item_bias.length;k<item_bias_new.length;k++)
			item_bias_new[k]=0;
		
		item_bias=item_bias_new;
		
	for(int i1=0;i1<items.size();i1++){
		MatrixUtils.RowInitNormal(item_factors, InitMean, InitStdev, items.get(i1));

		int num_pos_events = GetFeedback().GetUserMatrix().NumberOfEntries();
		int num_item_iterations = num_pos_events  / (MaxItemID + 1);
		for (int i = 0; i < num_item_iterations; i++) {
			// remark: the item may be updated more or less frequently than in the normal from-scratch training
			int user_id = SampleUser();
			int other_item_id=0;
			boolean item_is_positive = SampleOtherItem(user_id, items.get(i1), other_item_id);

			if (item_is_positive)
				UpdateFactors(user_id, items.get(i1), other_item_id, false, true, false);
			else
				UpdateFactors(user_id, other_item_id, items.get(i1), false, false, true);
		}
	}
}

	/// <summary>Compute approximate loss</summary>
	/// <returns>the approximate loss</returns>
	public double ComputeLoss()
	{
		double ranking_loss = 0;
		for (int c = 0; c < loss_sample_u.length; c++)
		{
			double x_uij = Predict(loss_sample_u[c], loss_sample_i[c]) - Predict(loss_sample_u[c], loss_sample_j[c]);
			ranking_loss += 1 / (1 + Math.exp(x_uij));
		}

		double complexity = 0;
		for (int c = 0; c < loss_sample_u.length; c++)
		{
			complexity += GetRegU() * Math.pow(VectorUtils.EuclideanNorm(user_factors.GetRow(loss_sample_u[c])), 2);
			complexity += GetRegI() * Math.pow(VectorUtils.EuclideanNorm(item_factors.GetRow(loss_sample_i[c])), 2);
			complexity += GetRegJ() * Math.pow(VectorUtils.EuclideanNorm(item_factors.GetRow(loss_sample_j[c])), 2);
			complexity += BiasReg * Math.pow(item_bias[loss_sample_i[c]], 2);
			complexity += BiasReg * Math.pow(item_bias[loss_sample_j[c]], 2);
		}

		return ranking_loss + 0.5 * complexity;
	}

	/// <summary>Compute the fit (AUC on training data)</summary>
	/// <returns>the fit</returns>
	public double ComputeFit()
	{
		double sum_auc = 0;
		int num_user = 0;

		for (int user_id = 0; user_id < MaxUserID + 1; user_id++)
		{
			int num_test_items = GetFeedback().GetUserMatrix().getLocation(user_id).size();
			if (num_test_items == 0)
				continue;
			int[] prediction = com.rapidminer.eval.ItemPrediction.PredictItems(this, user_id, MaxItemID);

			int num_eval_items = MaxItemID + 1;
			int num_eval_pairs = (num_eval_items - num_test_items) * num_test_items;

			int num_correct_pairs = 0;
			int num_pos_above = 0;
			// start with the highest weighting item...
			for (int i = 0; i < prediction.length; i++)
			{
				int item_id = prediction[i];

				if (GetFeedback().GetUserMatrix().getLocation(user_id, item_id))
					num_pos_above++;
				else
					num_correct_pairs += num_pos_above;
			}
			double user_auc = (double) num_correct_pairs / num_eval_pairs;
			sum_auc += user_auc;
			num_user++;
		}

		double auc = sum_auc / num_user;
		return auc;
	}

	private void CreateFastSamplingData(int u)
	{
		while (u >= user_pos_items.size())
			user_pos_items.add(null);
		while (u >= user_neg_items.size())
			user_neg_items.add(null);

		List<Integer> pos_list = new ArrayList<Integer>(GetFeedback().GetUserMatrix().getLocation(u));
		user_pos_items.set(u,pos_list);
	    List<Integer> neg_list = new ArrayList<Integer>();
	    
		for (int i = 0; i <= MaxItemID; i++)
			if (! GetFeedback().GetUserMatrix().getLocation(u, i))
				neg_list.add(i);
		user_neg_items.set(u, neg_list);
	}

	///
	protected void CheckSampling()
	{

				int fast_sampling_memory_size = ((MaxUserID + 1) * (MaxItemID + 1) * 4) / (1024 * 1024);

				if (fast_sampling_memory_size <= fast_sampling_memory_limit && fast_sampling_memory_limit!=0)//modifikacija
				{
					fast_sampling = true;

					this.user_pos_items = new ArrayList<List<Integer>>(MaxUserID + 1);
					this.user_neg_items = new ArrayList<List<Integer>>(MaxUserID + 1);
					
					for (int u = 0; u < MaxUserID + 1; u++)
						CreateFastSamplingData(u);
				}
	}

	public int GetNumIter(){
		return NumIter;
	}
	
	///
	public double Predict(int user_id, int item_id)
	{
		if ((user_id < 0) || (user_id >= user_factors.dim1))
		{
			System.out.println("user is unknown: " + user_id);
			return 0;
		}
		if ((item_id < 0) || (item_id >= item_factors.dim1))
		{
			System.out.println("item is unknown: " + item_id);
			return 0;
		}
		return item_bias[item_id] + com.rapidminer.matrixUtils.MatrixUtils.RowScalarProduct(user_factors, user_id, item_factors, item_id);
	}

	///
	public  void SaveModel(String file)
	{
		//not implemented
	}

	///
	public void LoadModel(String file)
	{
		//not implemented
	}

	///
	public String ToString()
	{
		return String.format("BPRMF num_factors={0} bias_reg={1} reg_u={2} reg_i={3} reg_j={4} num_iter={5} learn_rate={6} bold_driver={7} fast_sampling_memory_limit={8} init_mean={9} init_stdev={10}",
							 num_factors, BiasReg, reg_u, reg_i, reg_j, NumIter, learn_rate, BoldDriver, fast_sampling_memory_limit, InitMean, InitStdev);
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
