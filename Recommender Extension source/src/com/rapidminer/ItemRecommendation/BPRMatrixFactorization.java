package com.rapidminer.ItemRecommendation;

import java.util.List;

import com.rapidminer.data.EntityMapping;
import com.rapidminer.data.IEntityMapping;
import com.rapidminer.data.IPosOnlyFeedback;
import com.rapidminer.data.PosOnlyFeedback;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.ExampleSetPassThroughRule;
import com.rapidminer.operator.ports.metadata.ExampleSetPrecondition;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.operator.ports.metadata.SetRelation;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.Ontology;


/**
 * Biased Matrix Factorization operator for Item Recomendation
 * 
 * @see com.rapidminer.ItemRecommendation.BPRMatrixFactorization
 * @see com.rapidminer.ItemRecommendation.BPRMF
 * 
 * @author Matej Mihelcic (Ruðer Boškoviæ Institute)
 */

public class BPRMatrixFactorization extends Operator {

	private InputPort exampleSetInput = getInputPorts().createPort("example set");
	private OutputPort exampleSetOutput1 = getOutputPorts().createPort("Model");
	private OutputPort exampleSetOutput = getOutputPorts().createPort("example set");

	
	
	public static final String PARAMETER_NUM_FACTORS = "Num Factors";
	public static final String PARAMETER_BIAS_REG="Bias";
	public static final String PARAMETER_REG_U="User regularization";
	public static final String PARAMETER_REG_I="Item regularization";
	public static final String PARAMETER_REG_J="NegItem regularization";
	public static final String PARAMETER_NUM_ITER="Iteration number";
	public static final String PARAMETER_LEARN_RATE="Learn rate";
	public static final String PARAMETER_BOLD_DRIVER="Bold driver";
	public static final String PARAMETER_FAST_SAMPLING="Fast sampling memory limit";
	public static final String PARAMETER_INIT_MEAN="Initial mean";
	public static final String PARAMETER_INIT_STDEV="Initial stdev";
	
	public List<ParameterType> getParameterTypes() {
		 List<ParameterType> types = super.getParameterTypes();
	
		 types.add(new ParameterTypeInt(PARAMETER_NUM_FACTORS, "Number of latent factors. Range: integer; 1-+?; default: 10", 1, Integer.MAX_VALUE, 10, true));
		 types.add(new ParameterTypeDouble(PARAMETER_BIAS_REG, "Bias regularization parameter.  Range: double; 0-+?; default: 0", 0, Double.MAX_VALUE, 0, true));
		 types.add(new ParameterTypeDouble(PARAMETER_REG_U, "User regularization parameter.  Range: double; 0-+?; default: 0.025", 0, Double.MAX_VALUE, 0.025, true));
		 types.add(new ParameterTypeDouble(PARAMETER_REG_I, "Item regularization parameter.  Range: double; 0-+?; default: 0.025", 0, Double.MAX_VALUE, 0.025, true));
		 types.add(new ParameterTypeDouble(PARAMETER_REG_J, "Negative item regularization parameter.  Range: double; 0-+?; default: 0.025", 0, Double.MAX_VALUE, 0.025, true));
		 types.add(new ParameterTypeInt(PARAMETER_NUM_ITER, "Number of iterations.  Range: integer; 1-+?; default: 30", 1, Integer.MAX_VALUE, 30, false));
		 types.add(new ParameterTypeDouble(PARAMETER_LEARN_RATE, "Learning rate of algorithm.  Range: double; 0-+?; default: 0.05", 0, Double.MAX_VALUE, 0.05, false));
		 types.add(new ParameterTypeDouble(PARAMETER_INIT_MEAN, "Initial mean.  Range: double; 0-+?; default: 0", 0, Double.MAX_VALUE, 0, true));
		 types.add(new ParameterTypeDouble(PARAMETER_INIT_STDEV, "Initial stdev.  Range: double; 0-+?; default: 0.1", 0, Double.MAX_VALUE, 0.1, true));
		 types.add(new ParameterTypeInt(PARAMETER_FAST_SAMPLING, "Fast sampling memory limit, in MiB. Range: integer; 1-+?; default: 1024", 1, Integer.MAX_VALUE, 1024, true));
		 types.add(new ParameterTypeBoolean(PARAMETER_BOLD_DRIVER, "Use bold driver heuristics for learning rate adaption.  Range: boolean; default: false", false, true));
		
		
		 return types;
		 }
	
	/**
	 * Constructor
	 */
	public BPRMatrixFactorization(OperatorDescription description) {
		super(description);
		
		exampleSetInput.addPrecondition(new ExampleSetPrecondition(exampleSetInput, "user identification", Ontology.ATTRIBUTE_VALUE));
		exampleSetInput.addPrecondition(new ExampleSetPrecondition(exampleSetInput, "item identification", Ontology.ATTRIBUTE_VALUE));

		getTransformer().addRule(new ExampleSetPassThroughRule(exampleSetInput, exampleSetOutput, SetRelation.EQUAL) {
		});
		
		getTransformer().addRule(new GenerateNewMDRule(exampleSetOutput1, new MetaData(ItemRecommender.class)) {
		 });
		
		
	}

	@Override
	public void doWork() throws OperatorException {
		
		ExampleSet exampleSet = exampleSetInput.getData();
		
				
				 IPosOnlyFeedback training_data=new PosOnlyFeedback();
				 IEntityMapping user_mapping=new EntityMapping();
				 IEntityMapping item_mapping=new EntityMapping();
				
				 
				 if (exampleSet.getAttributes().getSpecial("user identification") == null) {
			            throw new UserError(this,105);
			        }
					
				 if (exampleSet.getAttributes().getSpecial("item identification") == null) {
			            throw new UserError(this, 105);
			        }
				 
				Attributes Att = exampleSet.getAttributes();
				AttributeRole ur=Att.getRole("user identification");
				Attribute u=ur.getAttribute();
				AttributeRole ir=Att.getRole("item identification");
				Attribute i=ir.getAttribute();

				
				for (Example example : exampleSet) {
					
					double j=example.getValue(u);
					int uid=(int) j;

					j=example.getValue(i);
					int iid=(int) j;
				
					training_data.Add(user_mapping.ToInternalID(uid), item_mapping.ToInternalID(iid));
					 checkForStop();
				}
				
				
				BPRMF recommendAlg=new BPRMF();
				 
				 recommendAlg.num_factors=getParameterAsInt("Num Factors");
				 recommendAlg.NumIter=getParameterAsInt("Iteration number");
				 recommendAlg.InitMean=getParameterAsDouble("Initial mean");
				 recommendAlg.InitStdev=getParameterAsDouble("Initial stdev");
				 recommendAlg.BiasReg=getParameterAsDouble("Bias");
				 recommendAlg.learn_rate=getParameterAsDouble("Learn rate");
				 recommendAlg.reg_i=getParameterAsDouble("Item regularization");
				 recommendAlg.reg_u=getParameterAsDouble("User regularization");
				 recommendAlg.reg_j=getParameterAsDouble("NegItem regularization");
				 recommendAlg.BoldDriver=getParameterAsBoolean("Bold driver");
				 recommendAlg.fast_sampling_memory_limit=getParameterAsInt("Fast sampling memory limit");
				
				 recommendAlg.SetFeedback(training_data);
				 
				 checkForStop();
				 recommendAlg.Train();
				 checkForStop();
				 
				 exampleSetOutput.deliver(exampleSet);
				 exampleSetOutput1.deliver(recommendAlg);
	}
}

