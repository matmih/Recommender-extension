package com.rapidminer.ItemRecommendation;
import com.rapidminer.data.SparseBooleanMatrix;

/**
*Copyright (C) 2010 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 02.08.2011
*/

public interface IUserAttributeAwareRecommender
{
	/// <value>Number of binary user attributes</value>
	/// <remarks></remarks>
	int GetNumUserAttributes();
	void SetNumUserAttributes(int value);

	/// <value>The binary user attributes</value>
	/// <remarks></remarks>
	SparseBooleanMatrix GetUserAttributes(); 
	void SetUserAttributes(SparseBooleanMatrix value);
}
