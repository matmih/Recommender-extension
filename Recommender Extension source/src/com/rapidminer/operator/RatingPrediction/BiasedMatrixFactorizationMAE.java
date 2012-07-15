package com.rapidminer.operator.RatingPrediction;
import java.util.List;

import com.rapidminer.matrixUtils.MatrixUtils;
import com.rapidminer.matrixUtils.VectorUtils;

/**
*Copyright (C) 2010, 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 28.07.2011
*/

public class BiasedMatrixFactorizationMAE extends BiasedMatrixFactorization
{
	   static final long serialVersionUID=3453434;
	///
	protected void Iterate(List<Integer> rating_indices, boolean update_user, boolean update_item)
	{
		double rating_range_size = GetMaxRating() -GetMinRating();

		
		for(int i1=0;i1<rating_indices.size();i1++){
			
		int index=rating_indices.get(i1);
		
			int u = ratings.GetUsers().get(index);
			int i = ratings.GetItems().get(index);

			double dot_product = user_bias[u] + item_bias[i] + MatrixUtils.RowScalarProduct(user_factors, u, item_factors, i);
			double sig_dot = 1 / (1 + Math.exp(-dot_product));

			double p = GetMinRating() + sig_dot * rating_range_size;
			double err = ratings.GetValues(index) - p;

			// the only difference to RMSE optimization is here:
			double gradient_common = Math.signum(err) * sig_dot * (1 - sig_dot) * rating_range_size;

			// adjust biases
			if (update_user)
				user_bias[u] += LearnRate * (user_bias[u] * gradient_common - BiasReg * user_bias[u]);
			if (update_item)
				item_bias[i] += LearnRate * (item_bias[i] * gradient_common - BiasReg * item_bias[i]);

			// adjust latent factors
			for (int f = 0; f < NumFactors; f++)
			{
			 	double u_f = user_factors.getLocation(u, f);
				double i_f = item_factors.getLocation(i, f);

				if (update_user)
				{
					double delta_u = i_f * gradient_common - RegU * u_f;
					MatrixUtils.Inc(user_factors, u, f, LearnRate * delta_u);
				}
				if (update_item)
				{
					double delta_i = u_f * gradient_common - RegI * i_f;
					MatrixUtils.Inc(item_factors, i, f, LearnRate * delta_i);
				}
			}
		}
	}

	///
	public double ComputeLoss()
	{
		double mae = 0;
		for (int i = 0; i < ratings.Count(); i++)
		{
			int user_id = ratings.GetUsers().get(i);
			int item_id = ratings.GetItems().get(i);
			mae += Math.abs(Predict(user_id, item_id) - ratings.GetValues(i));
		}

		double complexity = 0;
		for (int u = 0; u <= MaxUserID; u++)
		{
			complexity += ratings.CountByUser()[u] * RegU * Math.pow(VectorUtils.EuclideanNorm(user_factors.GetRow(u)), 2);
			complexity += ratings.CountByUser()[u] * BiasReg * Math.pow(user_bias[u], 2);
		}

		for (int i = 0; i <= MaxItemID; i++)
		{
			complexity += ratings.CountByItem()[i] * RegI * Math.pow(VectorUtils.EuclideanNorm(item_factors.GetRow(i)), 2);
			complexity += ratings.CountByItem()[i] * BiasReg * Math.pow(item_bias[i], 2);
		}

		return mae + complexity;
	}

	///
	public String ToString()
	{
		return String.format(
							 "BiasedMatrixFactorizationMAE num_factors={0} bias_reg={1} reg_u={2} reg_i={3} learn_rate={4} num_iter={5} bold_driver={6} init_mean={7} init_stdev={8}",
							 NumFactors, BiasReg, RegU, RegI, LearnRate, NumIter, BoldDriver, InitMean, InitStdev);
	}
}
