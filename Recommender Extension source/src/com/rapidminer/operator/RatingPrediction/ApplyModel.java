package com.rapidminer.operator.RatingPrediction;

import java.util.ArrayList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
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
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.Ontology;


/**
 * ApplyModel operator for Rating Prediction operators
 * 
 * @see com.rapidminer.operator.RatingPrediction.ApplyModel
 * 
 * @author Matej Mihelcic (Ruðer Boškoviæ Institute)
 */

public class ApplyModel extends Operator{

	
	private InputPort exampleSetInput = getInputPorts().createPort("query set");
	private InputPort exampleSetInput1 = getInputPorts().createPort("Model");
	private OutputPort exampleSetOutput = getOutputPorts().createPort("result set");
	private OutputPort exampleSetOutput1 = getOutputPorts().createPort("Model");

	public static final String PARAMETER_Updates="Online updates";
	
	public List<ParameterType> getParameterTypes() {
		 List<ParameterType> types = super.getParameterTypes();
		 types.add(new ParameterTypeBoolean(PARAMETER_Updates, "Use online model updates.  Range: boolean; default: false", false, false));
		 return types;
		 }
	
	/**
	 * Constructor
	 */
	public ApplyModel(OperatorDescription description) {
		super(description);

		MetaData met=new MetaData(RatingPredictor.class);
		
		exampleSetInput.addPrecondition(new ExampleSetPrecondition(exampleSetInput, "user identification", Ontology.ATTRIBUTE_VALUE));
		exampleSetInput.addPrecondition(new ExampleSetPrecondition(exampleSetInput, "item identification", Ontology.ATTRIBUTE_VALUE));

		exampleSetInput1.addPrecondition(new SimplePrecondition(exampleSetInput1, met));
		
		getTransformer().addRule(new GenerateNewMDRule(exampleSetOutput1, new MetaData(RatingPredictor.class)) {
	           
		 });
		
		getTransformer().addRule(new ExampleSetPassThroughRule(exampleSetInput, exampleSetOutput, SetRelation.SUPERSET) {
			@Override
			public ExampleSetMetaData modifyExampleSet(ExampleSetMetaData metaData) throws UndefinedParameterError {
			
				AttributeMetaData prediction=metaData.getAttributeByName("prediction");
				if (prediction != null) {
					prediction.setType(Ontology.REAL);
				}
				else{
					AttributeMetaData attribute=new AttributeMetaData("prediction",4);
					metaData.addAttribute(attribute);
				}
				
				return metaData;
			}
		});
		
		
	}

	@Override
	public void doWork() throws OperatorException {
		
				ExampleSet exampleSet = exampleSetInput.getData();
				Attributes attributes = exampleSet.getAttributes();	
				boolean OU = getParameterAsBoolean("Online updates");
				

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
						 Attribute label=null;
						 
							if(OU==true){
								if(Att.getLabel()!=null)
									label=Att.getLabel();
							}
				
						RatingPredictor model = exampleSetInput1.getData();
		
						ExampleSet out=exampleSet; 
						
						ArrayList<Integer> new_users=new ArrayList<Integer>();
						ArrayList<Integer> new_items=new ArrayList<Integer>();
						com.rapidminer.data.CompactHashSet<Integer> s2=new com.rapidminer.data.CompactHashSet<Integer>();
						com.rapidminer.data.CompactHashSet<Integer> s3=new com.rapidminer.data.CompactHashSet<Integer>();
						
						ArrayList<Integer> r_users=null;
						ArrayList<Integer> r_items=null;
						ArrayList<Double> ratings=null;
						
					   Attribute pred = AttributeFactory.createAttribute("prediction",
								Ontology.REAL);
					
					   if(!attributes.contains(pred)){
					  attributes.addRegular(pred);
					  ExampleTable a=exampleSet.getExampleTable();
					  a.addAttribute(pred);
					   }
					   else pred=attributes.get("prediction");
					
					  int useRatingUpdate=0;
					  
					  if(OU==true && Att.getLabel()!=null){
						  r_users=new ArrayList<Integer>();
						  r_items=new ArrayList<Integer>();
						  ratings=new ArrayList<Double>();
						  
						  for (Example example : out) {
							  int us=model.user_mapping.ToInternalID((int)example.getValue(u));
							  int it=model.item_mapping.ToInternalID((int)example.getValue(i));
							  if(us>model.MaxUserID){
								  new_users.add(us);
								  model.MaxUserID=us;
							  }
							  if(it>model.MaxItemID){
								  new_items.add(it);
								  model.MaxItemID=it;
							  }
							  s2.add(us);
							  s3.add(it);
							  double r=example.getValue(label);

							  if(model.AddRatings(null, null, null)==1){
							  r_users.add(us);
							  r_items.add(it);
							  ratings.add(r);
							  useRatingUpdate=1;
							  }
						  }
					  }
					  
					  ArrayList<Integer> query_users=new ArrayList<Integer>(s2);
						ArrayList<Integer> query_items=new ArrayList<Integer>(s3);
					  
						
					  if(OU==true && Att.getLabel()!=null){
						  if(new_items.size()!=0)
						  model.AddItems(new_items);
						  if(new_users.size()!=0)
						  model.AddUsers(new_users);
						  
						  if(useRatingUpdate==1)
							  model.AddRatings(r_users, r_items, ratings);
						  
						  model.RetrainUsers(query_users);
						  model.RetrainItems(query_items);
					  } 
					  
						for (Example example : out) {
								example.setValue(pred, model.Predict(model.user_mapping.ToInternalID((int)example.getValue(u)), model.item_mapping.ToInternalID((int)example.getValue(i))));
						}
						
						
						exampleSetOutput.deliver(out);	
						exampleSetOutput1.deliver(model);
				}
	}
