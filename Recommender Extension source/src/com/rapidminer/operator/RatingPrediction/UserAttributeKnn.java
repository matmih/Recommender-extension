package com.rapidminer.operator.RatingPrediction;
import com.rapidminer.ItemRecommendation.IUserAttributeAwareRecommender;
import com.rapidminer.data.BinaryCosine;
import com.rapidminer.data.IEntityMapping;
import com.rapidminer.data.SparseBooleanMatrix;


/**
*Copyright (C) 2010, 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 02.08.2011
*/

public class UserAttributeKnn extends _userKnn implements IUserAttributeAwareRecommender {

	    static final long serialVersionUID=3453434;
	    
	    public IEntityMapping attribute_mapping;
		///
		public SparseBooleanMatrix GetUserAttributes()
		{
			 return this.user_attributes; }
		
		
		public void SetUserAttributes(SparseBooleanMatrix value)
			{
				this.user_attributes = value;
				this.NumUserAttributes = user_attributes.NumberOfColumns();
				this.MaxUserID = Math.max(MaxUserID, user_attributes.NumberOfRows() - 1);
			}
		
		private SparseBooleanMatrix user_attributes;

		///
		public int NumUserAttributes;

		
		public int GetNumUserAttributes(){
			return NumUserAttributes;
		}
		
		public void SetNumUserAttributes(int value){
			NumUserAttributes=value;
		}
		
		///
		public UserAttributeKnn(){super(); }

		///
		public void Train()
		{
			super.Train();
			this.correlation = BinaryCosine.Create(user_attributes);
		}

		///
		public String ToString()
		{
			return String.format("UserAttributeKNN k={0} reg_u={1} reg_i={2}",
								 K == Integer.MAX_VALUE ? "inf" : K, RegU, RegI);
		}
	}
