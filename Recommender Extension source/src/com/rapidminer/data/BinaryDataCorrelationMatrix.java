package com.rapidminer.data;

/**
*Copyright (C) 2010, 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 21.07.2011
*/


public class BinaryDataCorrelationMatrix extends CorrelationMatrix
{
	static final long serialVersionUID=3453435;
	/// <summary>Constructor</summary>
	/// <param name="num_entities">the number of entities</param>
	public BinaryDataCorrelationMatrix(int num_entities) { super(num_entities);}
	
	public BinaryDataCorrelationMatrix(){}
	/// <summary>Compute the correlations from an implicit feedback, positive-only dataset</summary>
		/// <param name="entity_data">the implicit feedback set, rows contain the entities to correlate</param>
	
	public void ComputeCorrelations(IBooleanMatrix entity_data){}
	//currently unsupported
}