package com.rapidminer.ItemRecommendation;

/**
 * ApplyModel operator for ItemRecommender operators
 * 
 * @see com.rapidminer.ItemRecommendation.ApplyModel
 * 
 * @author Matej Mihelcic (Ruðer Boškoviæ Institute)
 */

import java.util.ArrayList;
import java.util.List;

/*import com.rapidminer.data.EntityMapping;
import com.rapidminer.data.IEntityMapping;
import com.rapidminer.data.IPosOnlyFeedback;*/
//import com.rapidminer.data.PosOnlyFeedback;
import com.rapidminer.data.WeightedItem;
//import com.rapidminer.eval.ItemPrediction;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.IntArrayDataRow;
import com.rapidminer.example.table.MemoryExampleTable;
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
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.Ontology;
import com.rapidminer.eval.PredictItemsFast;

public class ApplyModel extends Operator{

	
	private InputPort exampleSetInput = getInputPorts().createPort("query set");
	private InputPort exampleSetInput1 = getInputPorts().createPort("Model");
	private OutputPort exampleSetOutput = getOutputPorts().createPort("result set");
	private OutputPort exampleSetOutput1 = getOutputPorts().createPort("Model");

	
	public static final String PARAMETER_N = "n";
	public static final String PARAMETER_Updates="Online updates";
	
	
	public List<ParameterType> getParameterTypes() {
		 List<ParameterType> types = super.getParameterTypes();
		 types.add(new ParameterTypeInt(PARAMETER_N, "Desplay first n ranked items for users. Range: integer; 1-+?; default: 100", 1, Integer.MAX_VALUE, 100, false));
		 types.add(new ParameterTypeBoolean(PARAMETER_Updates, "Use online model updates.  Range: boolean; default: false", false, false));
		 return types;
		 }
	
	/**
	 * Constructor
	 */
	
	
	public ApplyModel(OperatorDescription description) {
		super(description);

		MetaData met=new MetaData(ItemRecommender.class);
		
		exampleSetInput.addPrecondition(new ExampleSetPrecondition(exampleSetInput, "user identification", Ontology.ATTRIBUTE_VALUE));
		exampleSetInput1.addPrecondition(new SimplePrecondition(exampleSetInput1, met));
		
		getTransformer().addRule(new ExampleSetPassThroughRule(exampleSetInput, exampleSetOutput, SetRelation.UNKNOWN) {
			@Override
			public ExampleSetMetaData modifyExampleSet(ExampleSetMetaData metaData) throws UndefinedParameterError {
				metaData.removeAllAttributes();
				AttributeMetaData attribute2=new AttributeMetaData("user_id",Ontology.INTEGER);
				AttributeMetaData attribute1=new AttributeMetaData("item_id",Ontology.INTEGER);
				AttributeMetaData attribute=new AttributeMetaData("ranking",Ontology.INTEGER);
				metaData.addAttribute(attribute2);
				metaData.addAttribute(attribute1);
				metaData.addAttribute(attribute);
				
				return metaData;
			}
		});
		
		getTransformer().addRule(new GenerateNewMDRule(exampleSetOutput1, new MetaData(ItemRecommender.class)) {
	           
		 });
	}

	@Override
	public void doWork() throws OperatorException {
		
				ExampleSet exampleSet = exampleSetInput.getData();
						
						
						if (exampleSet.getAttributes().getSpecial("user identification") == null) {
					            throw new UserError(this,105);
					        }
						
						boolean OU = getParameterAsBoolean("Online updates");
						
						Attributes Att = exampleSet.getAttributes();
						AttributeRole ur=Att.getRole("user identification");
						Attribute u=ur.getAttribute();
						AttributeRole ir=null;
						Attribute i=null;
						
					if(OU==true){
						if(Att.getRole("item identification")!=null){
							ir=Att.getRole("item identification");
							i=ir.getAttribute();
						}
					}
				
						ItemRecommender model = exampleSetInput1.getData();
						 int N=getParameterAsInt("n");
						 
							List<Attribute> attr=new ArrayList<Attribute>();
				
							com.rapidminer.data.CompactHashSet<Integer> s1=new com.rapidminer.data.CompactHashSet<Integer>();
							
							
							ArrayList<Integer> new_users=new ArrayList<Integer>();
							ArrayList<Integer> new_items=new ArrayList<Integer>();
							
							
							ArrayList<Integer> feed_it=null;
							ArrayList<Integer> feed_us=null;
							int use_feedUpdate=0;
							if(model.AddFeedbacks(null, null)==1){
								feed_it=new ArrayList<Integer>();
								feed_us=new ArrayList<Integer>();
								use_feedUpdate=1;
							}
							
							 if(N>model.MaxItemID)
								 N=model.MaxItemID+1;
							
							com.rapidminer.data.CompactHashSet<Integer> s2=new com.rapidminer.data.CompactHashSet<Integer>();
							com.rapidminer.data.CompactHashSet<Integer> s3=new com.rapidminer.data.CompactHashSet<Integer>();
							
							for (Example example : exampleSet) {
								double j=example.getValue(u);
								int uid=model.user_mapping.ToInternalID((int) j);
								double j1=0;
								int iid=0;
	
						if(OU==true){
							
							if(uid>model.feedback.GetMaxUserID()){
								new_users.add(uid);
							}
							
							if(Att.getRole("item identification")!=null){
									j1=example.getValue(i);
									iid=model.item_mapping.ToInternalID((int) j1);
								
								if(iid>model.feedback.GetMaxItemID()){
										new_items.add(iid);
								}
								model.feedback.Add(uid, iid);
								s2.add(uid);
								s3.add(iid);
								
								if(use_feedUpdate==1){
									feed_it.add(iid);
									feed_us.add(uid);
								}
								
							}
		
						}

								s1.add(uid);
						}
						
							ArrayList<Integer> query_users=new ArrayList<Integer>(s2);
							ArrayList<Integer> query_items=new ArrayList<Integer>(s3);
							
						if(OU==true){

							if(new_users.size()!=0)
							model.AddUsers(new_users);
							if(new_items.size()!=0)
							model.AddItems(new_items);
							if(use_feedUpdate==1){
								model.AddFeedbacks(feed_us, feed_it);
							}
							model.RetrainUsers(query_users);
							model.RetrainItems(query_items);
						}
							
							List<Integer> testU=new ArrayList<Integer>(s1);
							
							List<WeightedItem> data1=null; //dodano
							
							List<Integer> relevant_items=model.feedback.GetAllItems();
							
							 Attribute a1 = AttributeFactory.createAttribute("rank", Ontology.INTEGER);
							 Attribute tu = AttributeFactory.createAttribute("user_id", Ontology.INTEGER);
							 Attribute ti = AttributeFactory.createAttribute("item_id", Ontology.INTEGER);
								attr.add(tu); attr.add(ti); attr.add(a1);
								
								MemoryExampleTable a=new MemoryExampleTable(attr);
								com.rapidminer.data.CompactHashSet<Integer> s=new com.rapidminer.data.CompactHashSet<Integer>();

							for(int i1=0;i1<testU.size();i1++){
								
								s=model.feedback.GetUserMatrix().getL(testU.get(i1));//trainU
								
								data1=PredictItemsFast.PredictItemsFast1(model, testU.get(i1), relevant_items,s, N);

								int cn=0;
								int tr=model.user_mapping.ToOriginalID(testU.get(i1));
										
									
								
								for(int i2=0;i2<data1.size();i2++){
										if(!s.contains(data1.get(i2).item_id)){
											int[] dat={tr,model.item_mapping.ToOriginalID(data1.get(i2).item_id),cn+1};
										IntArrayDataRow row=new IntArrayDataRow(dat);
											a.addDataRow(row);
											cn++;
											if(cn==N)
												break;
										}
								}
								data1.clear();

							}
							
							 exampleSetOutput.deliver(a.createExampleSet());	
							 exampleSetOutput1.deliver(model);
							
							}
	
				}