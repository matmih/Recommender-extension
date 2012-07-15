package com.rapidminer.operator.RatingPrediction;



import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.rapidminer.eval.RatingEval;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.performance.EstimatedPerformance;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.AttributeMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetPassThroughRule;
import com.rapidminer.operator.ports.metadata.ExampleSetPrecondition;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.operator.ports.metadata.SetRelation;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.Ontology;


/**
 * Evaluation operator for Rating Prediction operators
 * 
 * @see com.rapidminer.operator.RatingPrediction.Evaluate
 * 
 * @author Matej Mihelcic (Ruðer Boškoviæ Institute)
 */

public class Evaluate extends Operator{

	private InputPort exampleSetInput1 = getInputPorts().createPort("predictions");
	private OutputPort performanceOutput = getOutputPorts().createPort("performance");
	private OutputPort exampleSetOutput = getOutputPorts().createPort("evaluation measures");

	public static final String PARAMETER_Min="Min Rating";
	public static final String PARAMETER_Range="Range";
	
	public List<ParameterType> getParameterTypes() {
		 List<ParameterType> types = super.getParameterTypes();
		 types.add(new ParameterTypeInt(PARAMETER_Min, "Value of minimal rating value. Range: integer; 0-+?; default: 1", 0, Integer.MAX_VALUE, 1, false));
		 types.add(new ParameterTypeInt(PARAMETER_Range, "Range of possible rating values.  Range: integer; 1-+?; default: 4 ; Max Rating=Min Rating+Range;", 1, Integer.MAX_VALUE, 4, false));
		 return types;
		 }
	
	/**
	 * Constructor
	 */
	public Evaluate(OperatorDescription description) {
		super(description);
		
		exampleSetInput1.addPrecondition(new ExampleSetPrecondition(exampleSetInput1, "user identification", Ontology.ATTRIBUTE_VALUE));
		exampleSetInput1.addPrecondition(new ExampleSetPrecondition(exampleSetInput1, "item identification", Ontology.ATTRIBUTE_VALUE));
		exampleSetInput1.addPrecondition(new ExampleSetPrecondition(exampleSetInput1, "label", Ontology.ATTRIBUTE_VALUE));
		getTransformer().addRule(new GenerateNewMDRule(performanceOutput, PerformanceVector.class));
		getTransformer().addRule(new ExampleSetPassThroughRule(exampleSetInput1, exampleSetOutput, SetRelation.UNKNOWN) {
		});
		
		
		
		getTransformer().addRule(new ExampleSetPassThroughRule(exampleSetInput1, exampleSetOutput, SetRelation.UNKNOWN) {
			@Override
			public ExampleSetMetaData modifyExampleSet(ExampleSetMetaData metaData) throws UndefinedParameterError {
			
				AttributeMetaData attribute=new AttributeMetaData("RMSE",4);
				metaData.removeAllAttributes();
				metaData.addAttribute(attribute);
				attribute=new AttributeMetaData("MAE",4);
				metaData.addAttribute(attribute);
				attribute=new AttributeMetaData("NMAE",4);
				metaData.addAttribute(attribute);
				
				return metaData;
			}
		});
	}

	@Override
	public void doWork() throws OperatorException {
				
				ExampleSet exampleSet = exampleSetInput1.getData();
				Attributes attributes = exampleSet.getAttributes();
				
				for(Attribute a : attributes)
					System.out.println(a.toString());
						
				System.out.println(exampleSet.size());
						System.out.println(attributes.size());
						
						
						ExampleSet  predicted= exampleSetInput1.getData();
						
						
						if (exampleSet.getAttributes().getSpecial("user identification") == null) {
				            throw new UserError(this,105);
				        }
						
					 if (exampleSet.getAttributes().getSpecial("item identification") == null) {
				            throw new UserError(this, 105);
				        }
					 
					 if (exampleSet.getAttributes().getLabel() == null) {
				            throw new UserError(this, 105);
				        }
							
						   Attribute m1 = AttributeFactory.createAttribute("RMSE", Ontology.REAL);
						   Attribute m2 = AttributeFactory.createAttribute("MAE", Ontology.REAL);
						   Attribute m3 = AttributeFactory.createAttribute("NMAE", Ontology.REAL);
							
							List<Attribute> attr=new ArrayList<Attribute>();
							attr.add(m1); attr.add(m2); attr.add(m3);
							MemoryExampleTable a=new MemoryExampleTable(attr);
							
							Map<String,Double> res;
							int minR=getParameterAsInt("Min Rating");
							int maxR=getParameterAsInt("Range")+minR;
							res=RatingEval.Evaluate(predicted,minR,maxR);
							double [] a1={res.get("RMSE"),res.get("MAE"),res.get("NMAE")};
							DoubleArrayDataRow row=new DoubleArrayDataRow(a1);
							a.addDataRow(row);
							
							PerformanceVector result1 = new PerformanceVector();
							EstimatedPerformance performance = new EstimatedPerformance("RMSE", a1[0],1,true);
							EstimatedPerformance performance1 = new EstimatedPerformance("MAE", a1[1],1,true);
							EstimatedPerformance performance2 = new EstimatedPerformance("NMAE", a1[2],1,true);

							result1.addCriterion(performance);
							result1.addCriterion(performance1);
							result1.addCriterion(performance2);

						performanceOutput.deliver(result1);
						exampleSetOutput.deliver(a.createExampleSet());	

				}
	}
