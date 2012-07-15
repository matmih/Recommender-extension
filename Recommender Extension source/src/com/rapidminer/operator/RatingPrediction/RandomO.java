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
 * Random rating predictor operator for Rating Prediction
 * 
 * @see com.rapidminer.operator.RatingPrediction.RandomO
 * @see com.rapidminer.RatingPrediction.Random
 * 
 * @author Matej Mihelcic (Ruðer Boškoviæ Institute)
 */



public class RandomO extends Operator{

	public static final String PARAMETER_Min="Min Rating";
	public static final String PARAMETER_Range="Range";
	public static final String PARAMETER_NORMAL="normal";
	public static final String PARAMETER_INIT_MEAN="Initial mean";
	public static final String PARAMETER_INIT_STDEV="Initial stdev";
	
	private InputPort exampleSetInput = getInputPorts().createPort("example set");
	private OutputPort exampleSetOutput1 = getOutputPorts().createPort("Model");
	private OutputPort exampleSetOutput = getOutputPorts().createPort("example set");

	
	public List<ParameterType> getParameterTypes() {
		 List<ParameterType> types = super.getParameterTypes();
		 types.add(new ParameterTypeInt(PARAMETER_Min, "Value of minimal rating value. Range: integer; 0-+?; default: 1", 0, Integer.MAX_VALUE, 1, false));
		 types.add(new ParameterTypeInt(PARAMETER_Range, "Range of possible rating values.  Range: integer; 1-+?; default: 4 ; Max Rating=Min Rating+Range;", 1, Integer.MAX_VALUE, 4, false));
		 types.add(new ParameterTypeBoolean(PARAMETER_NORMAL, "Use random generator from normal distribution.  Range: boolean; default: false", false, false));
		 types.add(new ParameterTypeDouble(PARAMETER_INIT_MEAN, "Initial mean, used in normal distribution mode only.  Range: double; 0-+?; default: 0.5", 0, Double.MAX_VALUE, 0.5, true));
		 types.add(new ParameterTypeDouble(PARAMETER_INIT_STDEV, "Initial stdev, used in normal distribution mode only.  Range: double; 0-+?; default: 0.0010", 0, Double.MAX_VALUE, 0.0010, true));
		 return types;
		 }
	
	/**
	 * Constructor
	 */
	public RandomO(OperatorDescription description) {
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
				
			
				 System.out.println(training_data.GetMaxItemID()+" "+training_data.GetMaxUserID());
				
				 
				Random recommendAlg=new Random();
	
				 recommendAlg.user_mapping=user_mapping;
				 recommendAlg.item_mapping=item_mapping;

				 recommendAlg.SetMinRating(getParameterAsInt("Min Rating"));
				 recommendAlg.SetMaxRating(recommendAlg.GetMinRating()+getParameterAsInt("Range"));
				 
				 recommendAlg.SetRatings(training_data);
				 
				 boolean norm = getParameterAsBoolean("normal");
				 
				 if(norm==true){
					 recommendAlg.use_normal=true;
					 recommendAlg.mean=getParameterAsDouble("Initial mean");
					 recommendAlg.stdev=getParameterAsDouble("Initial stdev");
				 }
				 else{ recommendAlg.use_normal=false;
				 }
				 recommendAlg.Train();
				 
				exampleSetOutput.deliver(exampleSet);
				exampleSetOutput1.deliver(recommendAlg);
				}
	}
