package com.rapidminer.operator.ansamble;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.RatingPrediction.GroupRecommender;
import com.rapidminer.operator.RatingPrediction.RatingPredictor;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.InputPortExtender;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.operator.ports.metadata.Precondition;
import com.rapidminer.operator.ports.metadata.SimplePrecondition;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeList;
import com.rapidminer.parameter.ParameterTypeString;



/**
 * ModelCombiner operator for RatingPrediction
 * 
 * @see com.rapidminer.operator.ansamble.ModelCombiner
 * 
 * @author Matej Mihelcic (Ruðer Boškoviæ Institute)
 */


public class ModelCombiner extends Operator{
	
	public static final String PARAMETER_DEFAULT_WEIGHT = "default_weight";
	public static final String PARAMETER_MODEL_WEIGHTS = "model_weights";


	private final InputPortExtender inputPort=new InputPortExtender("model",getInputPorts()){
		@Override
		protected Precondition makePrecondition(InputPort port) {
			int index = inputPort.getManagedPorts().size();
			return new SimplePrecondition(port, new MetaData(RatingPredictor.class), index < 2);
		};
	};
	private OutputPort exampleSetOutput = getOutputPorts().createPort("grouped model");

	
	public ModelCombiner(OperatorDescription description) {
		super(description);
		inputPort.start();
		inputPort.ensureMinimumNumberOfPorts(2);
		
		MetaData met=new MetaData(RatingPredictor.class);
		
		inputPort.getManagedPorts().get(0).addPrecondition(new SimplePrecondition(inputPort.getManagedPorts().get(0), met));
		
		inputPort.getManagedPorts().get(1).addPrecondition(new SimplePrecondition(inputPort.getManagedPorts().get(1), met));
		
		getTransformer().addRule(new GenerateNewMDRule(exampleSetOutput, new MetaData(RatingPredictor.class)) {
		 });
	}
	
	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeDouble(PARAMETER_DEFAULT_WEIGHT, "The default weight for all models not specified in the list 'model_weights'.", 0.0d, Double.POSITIVE_INFINITY, 1.0d));
		types.add(new ParameterTypeList(PARAMETER_MODEL_WEIGHTS, "The weights for several models. Criteria weights not defined in this list are set to 'default_weight'.", 
				new ParameterTypeString("operator_name", "The name of the operator."),
				new ParameterTypeDouble("model_weight", "The weight for this model.", 0.0d,
						Double.POSITIVE_INFINITY, 1.0d)));
		return types;
	}
	
	@Override
	public void doWork() throws OperatorException{
		
		List<RatingPredictor> ansambl=inputPort.getData(true);
		List<Double> weights=new ArrayList<Double>();
		
		List<String[]> weightList = getParameterList(PARAMETER_MODEL_WEIGHTS);
		Iterator<String[]> i = weightList.iterator();
		while (i.hasNext()) {
			String[] entry = i.next();
			Double criterionWeight = Double.valueOf(entry[1]);
			weights.add(criterionWeight);
		}
		
		
	      GroupRecommender recommendAlg=new GroupRecommender();
	      
	      recommendAlg.SetRatings(ansambl.get(0).GetRatings());
	      recommendAlg.SetMaxRating(ansambl.get(0).GetMaxRating());
	      recommendAlg.SetMinRating(ansambl.get(0).GetMinRating());
	      recommendAlg.SetRecommenders(ansambl);
	      recommendAlg.item_mapping=ansambl.get(0).item_mapping;
	      recommendAlg.user_mapping=ansambl.get(0).user_mapping;
	      recommendAlg.SetWeights(weights);
	      recommendAlg.SetDWeight(getParameterAsDouble("default_weight"));
	      exampleSetOutput.deliver(recommendAlg);
		
	}

}
	