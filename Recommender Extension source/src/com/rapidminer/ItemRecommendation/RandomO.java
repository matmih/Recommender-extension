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
import com.rapidminer.tools.Ontology;



/**
 *Random item recommender
 * 
 * @see com.rapidminer.ItemRecommendation.RandomO
 * @see com.rapidminer.ItemRecommendation.Random
 * 
 * @author Matej Mihelcic (Ruðer Boškoviæ Institute)
 */

public class RandomO extends Operator {
		
		private InputPort exampleSetInput = getInputPorts().createPort("example set");
		private OutputPort exampleSetOutput1 = getOutputPorts().createPort("Model");
		private OutputPort exampleSetOutput = getOutputPorts().createPort("example set");

		
		public List<ParameterType> getParameterTypes() {
			 List<ParameterType> types = super.getParameterTypes();
			 return types;
			 }
		
		/**
		 * Constructor
		 */
		public RandomO(OperatorDescription description) {
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
					
				
					 System.out.println(training_data.GetMaxItemID()+" "+training_data.GetMaxUserID());
					
					 
					Random recommendAlg=new Random();
					 recommendAlg.SetFeedback(training_data);
					 recommendAlg.user_mapping=user_mapping;
					 recommendAlg.item_mapping=item_mapping;
					 recommendAlg.Train();
					 
					exampleSetOutput.deliver(exampleSet);
					exampleSetOutput1.deliver(recommendAlg);
					}
		}
