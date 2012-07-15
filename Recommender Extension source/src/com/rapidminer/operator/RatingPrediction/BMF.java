package com.rapidminer.operator.RatingPrediction;

import java.util.List;

import com.rapidminer.data.EntityMapping;
import com.rapidminer.data.IEntityMapping;
import com.rapidminer.data.IRatings;
import com.rapidminer.data.Ratings;
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
 * Biased Matrix Factorization operator for Rating Prediction
 * 
 * @see com.rapidminer.operator.RatingPrediction.BMF
 * @see com.rapidminer.operator.RatingPrediction.BiasedMatrixFactorization
 * @see com.rapidminer.operator.RatingPrediction.BiasedMatrixFactorizationMAE
 * 
 * @author Matej Mihelcic (Ruðer Boškoviæ Institute)
 */



public class BMF extends Operator{
	private InputPort exampleSetInput = getInputPorts().createPort("example set");
	private OutputPort exampleSetOutput1 = getOutputPorts().createPort("Model");
	private OutputPort exampleSetOutput = getOutputPorts().createPort("example set");

	
	public static final String PARAMETER_NUM_FACTORS = "Num Factors";
	public static final String PARAMETER_REGULARIZATION="Regularization";
	public static final String PARAMETER_LEARN_RATE="Learn rate";
	public static final String PARAMETER_NUM_ITER="Iteration number";
	public static final String PARAMETER_INIT_MEAN="Initial mean";
	public static final String PARAMETER_INIT_STDEV="Initial stdev";
	public static final String PARAMETER_BIAS_REG="Bias";
	public static final String PARAMETER_REG_U="User regularization";
	public static final String PARAMETER_REG_I="Item regularization";
	public static final String PARAMETER_BOLD_DRIVER="Bold driver";
	public static final String PARAMETER_MAE_OPTIMIZED="MAE optimized";
	public static final String PARAMETER_Min="Min Rating";
	public static final String PARAMETER_Range="Range";

	
	public List<ParameterType> getParameterTypes() {
		 List<ParameterType> types = super.getParameterTypes();
		 types.add(new ParameterTypeInt(PARAMETER_Min, "Value of minimal rating value. Range: integer; 0-+?; default: 1", 0, Integer.MAX_VALUE, 1, false));
		 types.add(new ParameterTypeInt(PARAMETER_Range, "Range of possible rating values.  Range: integer; 1-+?; default: 4 ; Max Rating=Min Rating+Range;", 1, Integer.MAX_VALUE, 4, false));
		 types.add(new ParameterTypeInt(PARAMETER_NUM_FACTORS, "Number of latent factors. Range: integer; 1-+?; default: 10", 1, Integer.MAX_VALUE, 10, true));
		 types.add(new ParameterTypeDouble(PARAMETER_BIAS_REG, "Bias regularization parameter.  Range: double; 0-+?; default: 0.0001", 0, Double.MAX_VALUE, 0.0001, true));
		 types.add(new ParameterTypeDouble(PARAMETER_REG_U, "User regularization parameter.  Range: double; 0-+?; default: 0.015", 0, Double.MAX_VALUE, 0.015, true));
		 types.add(new ParameterTypeDouble(PARAMETER_REG_I, "Item regularization parameter.  Range: double; 0-+?; default: 0.015", 0, Double.MAX_VALUE, 0.015, true));
		 types.add(new ParameterTypeDouble(PARAMETER_LEARN_RATE, "Learning rate of algorithm.  Range: double; 0-+?; default: 0.01", 0, Double.MAX_VALUE, 0.01, false));
		 types.add(new ParameterTypeInt(PARAMETER_NUM_ITER, "Number of iterations.  Range: integer; 1-+?; default: 30", 1, Integer.MAX_VALUE, 30, false));
		 types.add(new ParameterTypeDouble(PARAMETER_REGULARIZATION, "Value of regularization parameter. Range: double; 0-+?; default: 0.015", 0, Double.MAX_VALUE, 0.015, true));
		 types.add(new ParameterTypeDouble(PARAMETER_INIT_MEAN, "Initial mean.  Range: double; 0-+?; default: 0", 0, Double.MAX_VALUE, 0, true));
		 types.add(new ParameterTypeDouble(PARAMETER_INIT_STDEV, "Initial stdev.  Range: double; 0-+?; default: 0.1", 0, Double.MAX_VALUE, 0.1, true));
		 types.add(new ParameterTypeBoolean(PARAMETER_BOLD_DRIVER, "Use bold driver heuristics for learning rate adaption.  Range: boolean; default: false", false, true));
		 types.add(new ParameterTypeBoolean(PARAMETER_MAE_OPTIMIZED, "Use biased matrix factorization optimized for mean average error (MAE).  Range: boolean; default: false", false, false));
		 return types;
		 }
	
	
	/**
	 * Constructor
	 */
	public BMF(OperatorDescription description) {
		super(description);

		exampleSetInput.addPrecondition(new ExampleSetPrecondition(exampleSetInput, "user identification", Ontology.ATTRIBUTE_VALUE));
		exampleSetInput.addPrecondition(new ExampleSetPrecondition(exampleSetInput, "item identification", Ontology.ATTRIBUTE_VALUE));
		exampleSetInput.addPrecondition(new ExampleSetPrecondition(exampleSetInput, "label", Ontology.ATTRIBUTE_VALUE));

		getTransformer().addRule(new ExampleSetPassThroughRule(exampleSetInput, exampleSetOutput, SetRelation.EQUAL) {
		});
		
		 getTransformer().addRule(new GenerateNewMDRule(exampleSetOutput1, new MetaData(RatingPredictor.class)) {
	           
		 });
	}

	@Override
	public void doWork() throws OperatorException {
		
		ExampleSet exampleSet = exampleSetInput.getData();
				
				IEntityMapping user_mapping=new EntityMapping();
				 IEntityMapping item_mapping=new EntityMapping();
				IRatings training_data=new Ratings();
				
				
			 if (exampleSet.getAttributes().getSpecial("user identification") == null) {
		            throw new UserError(this,105);
		        }
				
			 if (exampleSet.getAttributes().getSpecial("item identification") == null) {
		            throw new UserError(this, 105);
		        }
			 
			 if (exampleSet.getAttributes().getLabel() == null) {
		            throw new UserError(this, 105);
		        }
			 
			 Attributes Att = exampleSet.getAttributes();
			 AttributeRole ur=Att.getRole("user identification");
			 Attribute u=ur.getAttribute();
			 AttributeRole ir=Att.getRole("item identification");
			 Attribute i=ir.getAttribute();
			 Attribute ui=Att.getLabel();
				
				for (Example example : exampleSet) {
					
					double j=example.getValue(u);
					int uid=user_mapping.ToInternalID((int) j);

					j=example.getValue(i);
					int iid=item_mapping.ToInternalID((int) j);

					double r=example.getValue(ui);
					training_data.Add(uid, iid, r);
				}
				
				
				 boolean factorizationMode = getParameterAsBoolean("MAE optimized");
				 
				 BiasedMatrixFactorization recommendAlg;
				 
				 if(factorizationMode==false)
					 recommendAlg=new BiasedMatrixFactorization() ;
				 else recommendAlg=new BiasedMatrixFactorizationMAE();
			
				 recommendAlg.user_mapping=user_mapping;
				 recommendAlg.item_mapping=item_mapping;
				 recommendAlg.NumFactors=getParameterAsInt("Num Factors");
				 recommendAlg.Regularization=getParameterAsDouble("Regularization");
				 recommendAlg.NumIter=getParameterAsInt("Iteration number");
				 recommendAlg.InitMean=getParameterAsDouble("Initial mean");
				 recommendAlg.InitStdev=getParameterAsDouble("Initial stdev");
				 recommendAlg.BiasReg=getParameterAsDouble("Bias");
				 recommendAlg.LearnRate=getParameterAsDouble("Learn rate");
				 recommendAlg.RegI=getParameterAsDouble("Item regularization");
				 recommendAlg.RegU=getParameterAsDouble("User regularization");
				 recommendAlg.BoldDriver=getParameterAsBoolean("Bold driver");
				 
				 
				 recommendAlg.SetMinRating(getParameterAsInt("Min Rating"));
				 recommendAlg.SetMaxRating(recommendAlg.GetMinRating()+getParameterAsInt("Range"));
				
				 recommendAlg.SetRatings(training_data);
				
				 recommendAlg.Train();

				exampleSetOutput.deliver(exampleSet);
				exampleSetOutput1.deliver(recommendAlg);
				}
		
}
