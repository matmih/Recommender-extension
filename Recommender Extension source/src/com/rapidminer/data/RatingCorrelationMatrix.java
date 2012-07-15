package com.rapidminer.data;

/**
*Copyright (C) 2010, 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 27.07.2011
*/

public class RatingCorrelationMatrix extends CorrelationMatrix
{
	/// <summary>Constructor</summary>
	/// <param name="num_entities">the number of entities</param>
	static final long serialVersionUID=3453435;
	public RatingCorrelationMatrix(int num_entities) {super(num_entities); }

	/// <summary>Compute the correlations for a given entity type from a rating dataset</summary>
	/// <param name="ratings">the rating data</param>
	/// <param name="entity_type">the EntityType - either USER or ITEM</param>
	public void ComputeCorrelations(IRatings ratings, Integer entity_type)
	{
	
	}
}