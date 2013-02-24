package com.rapidminer.ItemRecommendation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//import com.rapidminer.data.EntityMapping;
import com.rapidminer.data.IEntityMapping;
import com.rapidminer.data.IPosOnlyFeedback;
import com.rapidminer.data.PosOnlyFeedback;
import com.rapidminer.eval.ItemPredictionEval;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
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
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.operator.ports.metadata.SetRelation;
import com.rapidminer.operator.ports.metadata.SimplePrecondition;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.Ontology;

/**
 * Evaluation operator for ItemRecommender operators
 * 
 * @see com.rapidminer.ItemRecommendation.Eval
 * 
 * @author Matej Mihelcic (Ruðer Boškoviæ Institute)
 */

public class Eval extends Operator {

	
	private InputPort exampleSetInput = getInputPorts().createPort("train set");
	private InputPort exampleSetInput1 = getInputPorts().createPort("test set");
	private InputPort exampleSetInput2 = getInputPorts().createPort("Model");
	private OutputPort performanceOutput = getOutputPorts().createPort("performance");
	private OutputPort exampleSetOutput = getOutputPorts().createPort("evaluation measures");

	
	
	public List<ParameterType> getParameterTypes() {
		 List<ParameterType> types = super.getParameterTypes();
		
		 return types;
		 }
	
	/**
	 * Constructor
	 */
	public Eval(OperatorDescription description) {
		super(description);
		MetaData met=new MetaData(ItemRecommender.class);
		exampleSetInput.addPrecondition(new ExampleSetPrecondition(exampleSetInput, "user identification", Ontology.ATTRIBUTE_VALUE));
		exampleSetInput.addPrecondition(new ExampleSetPrecondition(exampleSetInput, "item identification", Ontology.ATTRIBUTE_VALUE));
		exampleSetInput1.addPrecondition(new ExampleSetPrecondition(exampleSetInput1, "user identification", Ontology.ATTRIBUTE_VALUE));
		exampleSetInput1.addPrecondition(new ExampleSetPrecondition(exampleSetInput1, "item identification", Ontology.ATTRIBUTE_VALUE));
		exampleSetInput2.addPrecondition(new SimplePrecondition(exampleSetInput2, met));
		getTransformer().addRule(new GenerateNewMDRule(performanceOutput, PerformanceVector.class));
		getTransformer().addRule(new ExampleSetPassThroughRule(exampleSetInput1, exampleSetOutput, SetRelation.UNKNOWN) {
		});
		
		getTransformer().addRule(new ExampleSetPassThroughRule(exampleSetInput1, exampleSetOutput, SetRelation.UNKNOWN) {
			@Override
			public ExampleSetMetaData modifyExampleSet(ExampleSetMetaData metaData) throws UndefinedParameterError {
			
				AttributeMetaData attribute=new AttributeMetaData("AUC",4);
				metaData.removeAllAttributes();
				metaData.addAttribute(attribute);
				attribute=new AttributeMetaData("prec@5",4);
				metaData.addAttribute(attribute);
				attribute=new AttributeMetaData("prec@10",4);
				metaData.addAttribute(attribute);
				attribute=new AttributeMetaData("prec@15",4);
				metaData.addAttribute(attribute);
				attribute=new AttributeMetaData("NDCG",4);
				metaData.addAttribute(attribute);
				attribute=new AttributeMetaData("MAP",4);
				metaData.addAttribute(attribute);
				return metaData;
			}
		});
	}

	@Override
	public void doWork() throws OperatorException {
				
		ExampleSet exampleSet = exampleSetInput.getData();
		ExampleSet exampleSet1=exampleSetInput1.getData();
		Attributes attributes1=exampleSet1.getAttributes();
				
		 if (exampleSet.getAttributes().getSpecial("user identification") == null) {
	            throw new UserError(this,105);
	        }
		 
		 if (exampleSet.getAttributes().getSpecial("item identification") == null) {
	            throw new UserError(this,105);
	        }
			
		if (exampleSet1.getAttributes().getSpecial("item identification") == null) {
	            throw new UserError(this, 105);
	        }
		
		if (exampleSet1.getAttributes().getSpecial("user identification") == null) {
         throw new UserError(this, 105);
			}
				
				Attributes Att = exampleSet.getAttributes();
				AttributeRole ur=Att.getRole("user identification");
				Attribute u=ur.getAttribute();
				AttributeRole ir=Att.getRole("item identification");
				Attribute i=ir.getAttribute();
				
				AttributeRole tur=attributes1.getRole("user identification");
				Attribute tu=tur.getAttribute();

				AttributeRole tir=attributes1.getRole("item identification");
				Attribute ti=tir.getAttribute();

		
				ItemRecommender model = exampleSetInput2.getData();
		
					 IPosOnlyFeedback train_data=new PosOnlyFeedback();
					 IPosOnlyFeedback test_data=new PosOnlyFeedback();
					 IEntityMapping user_mapping=model.user_mapping;//new EntityMapping();
					 IEntityMapping item_mapping=model.item_mapping;//new EntityMapping();
					

					for (Example example : exampleSet) {
						double j=example.getValue(u);
						int uid=(int) j;

						j=example.getValue(i);
						int iid=(int) j;
						train_data.Add(user_mapping.ToInternalID(uid), item_mapping.ToInternalID(iid));
				}
					
					for (Example example : exampleSet1) {
						double j=example.getValue(tu);
						int uid=(int) j;

						j=example.getValue(ti);
						int iid=(int) j;
						test_data.Add(user_mapping.ToInternalID(uid), item_mapping.ToInternalID(iid));
				}	
					
					
					 Map<String,Double> result= ItemPredictionEval.Evaluate(model, test_data,train_data,test_data.GetAllUsers(),train_data.GetAllItems()); //train_data.GetAllUsers(),train_data.GetAllItems()

						   Attribute m1 = AttributeFactory.createAttribute("AUC", Ontology.REAL);
						   Attribute m2 = AttributeFactory.createAttribute("prec@5", Ontology.REAL);
						   Attribute m3 = AttributeFactory.createAttribute("prec@10", Ontology.REAL);
						   Attribute m4 = AttributeFactory.createAttribute("prec@15", Ontology.REAL);
						   Attribute m5 = AttributeFactory.createAttribute("NDCG", Ontology.REAL);
						   Attribute m6 = AttributeFactory.createAttribute("MAP", Ontology.REAL);
						   
							List<Attribute> attr=new ArrayList<Attribute>();
							attr.add(m1); attr.add(m2); attr.add(m3); attr.add(m4); attr.add(m5); attr.add(m6);
							MemoryExampleTable a=new MemoryExampleTable(attr);
							
							
							double [] a1={result.get("AUC"),result.get("prec@5"),result.get("prec@10"), result.get("prec@15"),result.get("NDCG"),result.get("MAP")};
							DoubleArrayDataRow row=new DoubleArrayDataRow(a1);
							a.addDataRow(row);
							
						PerformanceVector result1 = new PerformanceVector();
						EstimatedPerformance performance = new EstimatedPerformance("AUC", a1[0],1,false);
						EstimatedPerformance performance1 = new EstimatedPerformance("prec@5", a1[1],1,false);
						EstimatedPerformance performance2 = new EstimatedPerformance("prec@10", a1[2],1,false);
						EstimatedPerformance performance3 = new EstimatedPerformance("prec@15", a1[3],1,false);
						EstimatedPerformance performance4 = new EstimatedPerformance("NDCG", a1[4],1,false);
						EstimatedPerformance performance5 = new EstimatedPerformance("MAP", a1[5],1,false);
						result1.addCriterion(performance);
						result1.addCriterion(performance1);
						result1.addCriterion(performance2);
						result1.addCriterion(performance3);
						result1.addCriterion(performance4);
						result1.addCriterion(performance5);
						
						
						performanceOutput.deliver(result1);
						exampleSetOutput.deliver(a.createExampleSet());	
				}
	}
