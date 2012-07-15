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
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.Ontology;

/**
 * FactorWised Matrix Factorization operator for Rating Prediction
 * 
 * @see com.rapidminer.operator.RatingPrediction.FWMF
 * @see com.rapidminer.operator.RatingPrediction.FactorWisedMatrixFactorization
 * 
 * @author Matej Mihelcic (Ruðer Boškoviæ Institute)
 */

public class CF_FWMF extends Operator{

		private InputPort exampleSetInput = getInputPorts().createPort("example set");
		private OutputPort exampleSetOutput1 = getOutputPorts().createPort("Model");
		private OutputPort exampleSetOutput = getOutputPorts().createPort("example set");

		
		public static final String PARAMETER_NUM_FACTORS = "Num Factors";
		public static final String PARAMETER_SCHRINKAGE = "Schrinkage";
		public static final String PARAMETER_SENSIBILITY = "Sensibility";
		public static final String PARAMETER_NUM_ITER="Iteration number";
		public static final String PARAMETER_INIT_MEAN="Initial mean";
		public static final String PARAMETER_INIT_STDEV="Initial stdev";
		public static final String PARAMETER_Min="Min Rating";
		public static final String PARAMETER_Range="Range";

		
		public List<ParameterType> getParameterTypes() {
			 List<ParameterType> types = super.getParameterTypes();
			 types.add(new ParameterTypeInt(PARAMETER_Min, "Value of minimal rating value. Range: integer; 0-+?; default: 1", 0, Integer.MAX_VALUE, 1, false));
			 types.add(new ParameterTypeInt(PARAMETER_Range, "Range of possible rating values.  Range: integer; 1-+?; default: 4 ; Max Rating=Min Rating+Range;", 1, Integer.MAX_VALUE, 4, false));
			 types.add(new ParameterTypeInt(PARAMETER_NUM_FACTORS, "Number of latent factors. Range: integer; 1-+?; default: 10", 1, Integer.MAX_VALUE, 10, true));
			 types.add(new ParameterTypeDouble(PARAMETER_SCHRINKAGE, "Schrinkage. Range: double; 0-+?; default: 25", 0, Integer.MAX_VALUE, 25, true));
			 types.add(new ParameterTypeDouble(PARAMETER_SENSIBILITY, "Sensibility. Range: double; 0-+?; default: 0.00001", 0, Integer.MAX_VALUE, 0.00001, true));
			 types.add(new ParameterTypeInt(PARAMETER_NUM_ITER, "Number of iterations.  Range: integer; 1-+?; default: 10", 1, Integer.MAX_VALUE, 10, false));
			 types.add(new ParameterTypeDouble(PARAMETER_INIT_MEAN, "Initial mean.  Range: double; 0-+?; default: 0", 0, Double.MAX_VALUE, 0, true));
			 types.add(new ParameterTypeDouble(PARAMETER_INIT_STDEV, "Initial stdev.  Range: double; 0-+?; default: 0.1", 0, Double.MAX_VALUE, 0.1, true));
			 return types;
			 }
		
		
		/**
		 * Constructor
		 */
		public CF_FWMF(OperatorDescription description) {
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
					
							
					 FactorWisedMatrixFactorization recommendAlg=new FactorWisedMatrixFactorization();
					
					 recommendAlg.user_mapping=user_mapping;
					 recommendAlg.item_mapping=item_mapping;
					 recommendAlg.NumFactors=getParameterAsInt("Num Factors");
					
					 recommendAlg.NumIter=getParameterAsInt("Iteration number");
					 recommendAlg.InitMean=getParameterAsDouble("Initial mean");
					 recommendAlg.InitStdev=getParameterAsDouble("Initial stdev");
					 recommendAlg.Shrinkage=getParameterAsDouble("Schrinkage");
					 recommendAlg.Sensibility=getParameterAsDouble("Sensibility");
					
					 recommendAlg.SetMinRating(getParameterAsInt("Min Rating"));
					 recommendAlg.SetMaxRating(recommendAlg.GetMinRating()+getParameterAsInt("Range"));
				
					 recommendAlg.SetRatings(training_data);
				
					 recommendAlg.Train();
					
					exampleSetOutput.deliver(exampleSet);
					exampleSetOutput1.deliver(recommendAlg);
					}
			
	}
