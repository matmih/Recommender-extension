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
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.Ontology;

/**
 * UserKnn operator for Rating Prediction
 * 
 * @see com.rapidminer.operator.RatingPrediction.UserKnn
 * @see com.rapidminer.operator.RatingPrediction._userKnn
 * 
 * @author Matej Mihelcic (Ruðer Boškoviæ Institute)
 */

public class UserKnn extends Operator{
	private InputPort exampleSetInput = getInputPorts().createPort("example set");
	private OutputPort exampleSetOutput1 = getOutputPorts().createPort("Model");
	private OutputPort exampleSetOutput = getOutputPorts().createPort("example set");

	public static final String PARAMETER_K = "k";
	public static final String PARAMETER_Min="Min Rating";
	public static final String PARAMETER_Range="Range";
	public static final String PARAMETER_CORRELATION_MODE="Correlation mode";
	public static final String[] CORRELATION_MODES = { "pearson" , "cosine" };
	public static final int CORRELATION_MODE_COSINE = 1;
	public static final int CORRELATION_MODE_PEARSON = 0;
	public static final String PARAMETER_REGU="reg_u";
	public static final String PARAMETER_REGI="reg_i";
	public static final String PARAMETER_schrink="schrinkage";
	
	
	public List<ParameterType> getParameterTypes() {
		 List<ParameterType> types = super.getParameterTypes();
		 types.add(new ParameterTypeInt(PARAMETER_K, "The used number of nearest neighbors. Range: integer; 1-+?; default: 80", 1, Integer.MAX_VALUE, 80, false));
		 types.add(new ParameterTypeInt(PARAMETER_Min, "Value of minimal rating value. Range: integer; 0-+?; default: 1", 0, Integer.MAX_VALUE, 1, false));
		 types.add(new ParameterTypeInt(PARAMETER_Range, "Range of possible rating values.  Range: integer; 1-+?; default: 4 ; Max Rating=Min Rating+Range;", 1, Integer.MAX_VALUE, 4, false));
		 ParameterType type = new ParameterTypeCategory(PARAMETER_CORRELATION_MODE, "Tipe of correlation used to calculate prediction.", CORRELATION_MODES, CORRELATION_MODE_COSINE);
			type.setExpert(false);
			types.add(type);
		 types.add(new ParameterTypeDouble(PARAMETER_REGU, "Regularization parameter for user biases.  Range: double; 0-+?; default: 10 ;", 0, Double.MAX_VALUE, 10, true));
		 types.add(new ParameterTypeDouble(PARAMETER_REGI, "Regularization parameter for item biases.  Range: double; 0-+?; default: 5 ;", 0, Double.MAX_VALUE, 5, true));
		 types.add(new ParameterTypeDouble(PARAMETER_schrink, "Schrinkage regularization parameter.  Range: float; 0-+?; default: 10 ; used only in Pearson mode", 0, Float.MAX_VALUE, 10, true));
		 return types;
		 }
	
	/**
	 * Constructor
	 */
	public UserKnn(OperatorDescription description) {
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
				
				
				 int correlationMode = getParameterAsInt("Correlation mode");
				 _userKnn recommendAlg;
				 
				 
				 if(correlationMode==0){
					 recommendAlg=new UserKnnPearson();
				 double schrinkage=getParameterAsDouble("schrinkage");
				 recommendAlg.setSchrinkage((float)schrinkage);
				 }
				 else recommendAlg=new UserKnnCosine();
				
				recommendAlg.user_mapping=user_mapping;
				recommendAlg.item_mapping=item_mapping;
				 
				 int K=getParameterAsInt("k");
				 recommendAlg.SetK(K);
		
				 double regU=getParameterAsDouble("reg_u");
				 recommendAlg.RegU=regU;
				 double regI=getParameterAsDouble("reg_i");
				 recommendAlg.RegI=regI;
				 
				 recommendAlg.SetMinRating(getParameterAsInt("Min Rating"));
				 recommendAlg.SetMaxRating(recommendAlg.GetMinRating()+getParameterAsInt("Range"));
				 recommendAlg.SetRatings(training_data);
			
				 recommendAlg.Train();

				exampleSetOutput.deliver(exampleSet);
				exampleSetOutput1.deliver(recommendAlg);
				}
}
