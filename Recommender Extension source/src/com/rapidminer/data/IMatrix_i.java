package com.rapidminer.data;

import java.io.Serializable;

public interface IMatrix_i extends Serializable {

	              public int getLocation(int x, int y);
	               public void setLocation(int x, int y, int value);
		 public	int NumberOfRows();

			/// <summary>The number of columns of the matrix</summary>
			/// <value>The number of columns of the matrix</value>
		 public	int NumberOfColumns();

			/// <summary>True if the matrix is symmetric, false otherwise</summary>
			/// <value>True if the matrix is symmetric, false otherwise</value>
		public boolean IsSymmetric();

			/// <summary>Get the transpose of the matrix, i.e. a matrix where rows and columns are interchanged</summary>
			/// <returns>the transpose of the matrix (copy)</returns>
			public IMatrix_i Transpose();

			/// <summary>Create a matrix with a given number of rows and columns</summary>
			/// <param name="num_rows">the number of rows</param>
			/// <param name="num_columns">the number of columns</param>
			/// <returns>A matrix with num_rows rows and num_column columns</returns>
			public IMatrix_i CreateMatrix(int num_rows, int num_columns);
	
}
