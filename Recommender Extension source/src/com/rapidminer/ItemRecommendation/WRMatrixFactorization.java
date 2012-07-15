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
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.Ontology;


/**
 * Weighted Matrix Factorization operator for Item Recommendation
 * 
 * @see com.rapidminer.ItemRecommendation.WRMatrixFactorization
 * @see com.rapidminer.ItemRecommendation.WRMF
 * 
 * @author Matej Mihelcic (Ruðer Boškoviæ Institute)
 */

public class WRMatrixFactorization extends Operator {

	public static final String PARAMETER_K = "k";
	
	private InputPort exampleSetInput = getInputPorts().createPort("example set");
	private OutputPort exampleSetOutput1 = getOutputPorts().createPort("Model");
	private OutputPort exampleSetOutput = getOutputPorts().createPort("example set");

	
	public static final String PARAMETER_NUM_FACTORS = "Num Factors";
	public static final String PARAMETER_REGULARIZATION="Regularization";
	public static final String PARAMETER_CPOS="C position";
	public static final String PARAMETER_NUM_ITER="Iteration number";
	public static final String PARAMETER_INIT_MEAN="Initial mean";
	public static final String PARAMETER_INIT_STDEV="Initial stdev";
	
	public List<ParameterType> getParameterTypes() {
		 List<ParameterType> types = super.getParameterTypes();
	
		 types.add(new ParameterTypeInt(PARAMETER_NUM_FACTORS, "Number of latent factors. Range: integer; 1-+?; default: 10", 1, Integer.MAX_VALUE, 10, true));
		 types.add(new ParameterTypeDouble(PARAMETER_REGULARIZATION, "Value of regularization parameter. Range: double; 0-+?; default: 0.015", 0, Double.MAX_VALUE, 0.015, true));
		 types.add(new ParameterTypeDouble(PARAMETER_CPOS, "C position: the weight/confidence that is put on positive observations. Range: double; 0-+?; default: 1", 0, Double.MAX_VALUE, 1, true));
		 types.add(new ParameterTypeInt(PARAMETER_NUM_ITER, "Number of iterations.  Range: integer; 1-+?; default: 30", 1, Integer.MAX_VALUE, 30, false));
		 types.add(new ParameterTypeDouble(PARAMETER_INIT_MEAN, "Initial mean.  Range: double; 0-+?; default: 0", 0, Double.MAX_VALUE, 0, true));
		 types.add(new ParameterTypeDouble(PARAMETER_INIT_STDEV, "Initial stdev.  Range: double; 0-+?; default: 0.1", 0, Double.MAX_VALUE, 0.1, true));
		
		 return types;
		 }
	
	/**
	 * Constructor
	 */
	public WRMatrixFactorization(OperatorDescription description) {
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
			
				WRMF recommendAlg=new WRMF();
				 
				recommendAlg.num_factors=getParameterAsInt("Num Factors");
				 recommendAlg.NumIter=getParameterAsInt("Iteration number");
				 recommendAlg.InitMean=getParameterAsDouble("Initial mean");
				 recommendAlg.InitStdev=getParameterAsDouble("Initial stdev");
				recommendAlg.c_pos=getParameterAsDouble("C position");
				recommendAlg.regularization=getParameterAsDouble("Regularization");
				
				 recommendAlg.SetFeedback(training_data);
				 recommendAlg.user_mapping=user_mapping;
				 recommendAlg.item_mapping=item_mapping;
				 recommendAlg.Train();
				 exampleSetOutput.deliver(exampleSet);
				 exampleSetOutput1.deliver(recommendAlg);
	}
}


