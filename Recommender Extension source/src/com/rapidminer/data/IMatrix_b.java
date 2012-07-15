package com.rapidminer.data;
import java.io.Serializable;

public interface IMatrix_b extends Serializable {

	               boolean getLocation(int x, int y);
	                void setLocation(int x, int y, boolean value);
			int NumberOfRows();

			/// <summary>The number of columns of the matrix</summary>
			/// <value>The number of columns of the matrix</value>
			int NumberOfColumns();

			/// <summary>True if the matrix is symmetric, false otherwise</summary>
			/// <value>True if the matrix is symmetric, false otherwise</value>
			boolean IsSymmetric();

			/// <summary>Get the transpose of the matrix, i.e. a matrix where rows and columns are interchanged</summary>
			/// <returns>the transpose of the matrix (copy)</returns>
			IMatrix_b Transpose();

			/// <summary>Create a matrix with a given number of rows and columns</summary>
			/// <param name="num_rows">the number of rows</param>
			/// <param name="num_columns">the number of columns</param>
			/// <returns>A matrix with num_rows rows and num_column columns</returns>
			public IMatrix_b CreateMatrix(int num_rows, int num_columns);
	
}
