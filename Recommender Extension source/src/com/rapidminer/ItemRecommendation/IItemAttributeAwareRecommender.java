package com.rapidminer.ItemRecommendation;
import com.rapidminer.data.SparseBooleanMatrix;

/**
*Copyright (C) 2010 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 02.08.2011
*/


public interface IItemAttributeAwareRecommender {

		/// <value>an integer stating the number of attributes</value>
		/// <summary></summary>
		/// <remarks></remarks>
		int GetNumItemAttributes();
		void SetNumItemAttributes(int value);

		/// <value>The binary item attributes</value>
		/// <summary></summary>
		/// <remarks></remarks>
		SparseBooleanMatrix GetItemAttributes();
		void SetItemAttributes(SparseBooleanMatrix value);
}
