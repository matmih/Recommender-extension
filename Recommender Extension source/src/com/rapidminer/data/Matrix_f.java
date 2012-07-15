package com.rapidminer.data;

import java.util.List;

public class Matrix_f implements IMatrix_f {

   /// <summary>Data array: data is stored in columns.</summary>
		public float[] data;
		/// <summary>Dimension 1, the number of rows</summary>
		public int dim1;
		/// <summary>Dimension 2, the number of columns</summary>
		public int dim2;
		static final long serialVersionUID=3453435;
		
		public Matrix_f(){
		
		}

               public  float getLocation(int i, int j){

                   if(i>=this.dim1)
                       throw new IllegalArgumentException("i too big: " + i + ", dim1 is " + this.dim1);
                   if (j >= this.dim2)
					throw new IllegalArgumentException("j too big: " + j + ", dim2 is " + this.dim2);
				return data[i * dim2 + j];
               }

              public void setLocation(int i, int j, float value){

                    if(i>=this.dim1)
                       throw new IllegalArgumentException("i too big: " + i + ", dim1 is " + this.dim1);
                   if (j >= this.dim2)
					throw new IllegalArgumentException("j too big: " + j + ", dim2 is " + this.dim2);

                         data[i * dim2 + j]=value;
                }

		///
		public boolean IsSymmetric()
		{
				if (dim1 != dim2)
					return false;
				for (int i = 0; i < dim1; i++)
					for (int j = i + 1; j < dim2; j++)
						if (getLocation(i, j)==(getLocation(j, i)))
							return false;
				return true;

		}

		///
		public int NumberOfRows() { return dim1;  }

		///
		public int NumberOfColumns() {  return dim2; }

		/// <summary>Initializes a new instance of the Matrix class</summary>
		/// <param name="dim1">the number of rows</param>
		/// <param name="dim2">the number of columns</param>
		public Matrix_f(int dim1, int dim2)
		{
			if (dim1 < 0)
				throw new IllegalArgumentException("dim1 must be at least 0");
			if (dim2 < 0)
				throw new IllegalArgumentException("dim2 must be at least 0");

			this.dim1 = dim1;
			this.dim2 = dim2;
                       this.data =new float[dim1*dim2];
		}


		/// <summary>Copy constructor. Creates a deep copy of the given matrix.</summary>
		/// <param name="matrix">the matrix to be copied</param>
		public Matrix_f(Matrix_f matrix)
		{
			this.dim1 = matrix.dim1;
			this.dim2 = matrix.dim2;
			this.data = new float[this.dim1 * this.dim2];
			System.arraycopy(matrix.data, 0, data, 0, matrix.data.length);
		}

		/// <summary>Constructor that takes a list of lists to initialize the matrix</summary>
		/// <param name="data">a list of lists of T</param>
		public Matrix_f(List<List<Float>> _data)
		{
			this.dim1 = _data.size();
			this.dim2 = _data.get(0).size();
			this.data = new float[this.dim1 * this.dim2];
			for (int i = 0; i < dim1; i++)
				for (int j = 0; j < dim2; j++)
					this.data[i * dim2 + j] = _data.get(i).get(j);
		}
		
		///
		public Matrix_f CreateMatrix(int num_rows, int num_columns)
		{
			return new Matrix_f(num_rows, num_columns);
		}

		///
		public Matrix_f Transpose()
		{
			Matrix_f transpose = new Matrix_f(NumberOfColumns(), NumberOfRows());
			for (int i = 0; i < dim1; i++)
				for (int j = 0; j < dim2; j++)
					transpose.data[j * dim1 + i] = data[i * dim2 + j];
			return transpose;
		}

		///

		/// <summary>Returns a copy of the i-th row of the matrix</summary>
		/// <param name="i">the row ID</param>
		/// <returns>a list of T containing the row data</returns>
		public double[] GetRow(int i)
		{
			double[] row = new double[this.dim2];
                       System.arraycopy(data, i*dim2, row, 0, dim2);
			return row;
		}

		/// <summary>Returns a copy of the j-th column of the matrix</summary>
		/// <param name="j">the column ID</param>
		/// <returns>a list of T containing the column data</returns>
		public double[] GetColumn(int j)
		{
			double[] column =new double[this.dim1];
			for (int x = 0; x < this.dim1; x++)
				column[x] = getLocation(x, j);
			return column;
		}

		/// <summary>Sets the values of the i-th row to the values in a given array</summary>
		/// <param name="i">the row ID</param>
		/// <param name="row">a list of T of length dim1</param>
		public void SetRow(int i, double[] row)
		{
			if (row.length != this.dim2)
				throw new IllegalArgumentException(String.format("Array length ({0}) must equal number of columns ({1}",
														  row.length, this.dim2));
                       System.arraycopy(row, 0, data, i*dim2, row.length);
		}

		/// <summary>Sets the values of the j-th column to the values in a given array</summary>
		/// <param name="j">the column ID</param>
		/// <param name="column">a list of T of length dim2</param>
		public void SetColumn(int j, float[] column)
		{
			if (column.length != this.dim1)
				throw new IllegalArgumentException(String.format("Array length ({0}) must equal number of rows ({1}",
														  column.length, this.dim1));

			for (int i = 0; i < this.dim1; i++)
				setLocation(i,j,column[i]);
		}

		/// <summary>Init the matrix with a default value</summary>
		/// <param name="d">the default value</param>
		public void Init(float d)
		{
			for (int i = 0; i < dim1 * dim2; i++)
				data[i] = d;
		}

		/// <summary>Enlarges the matrix to num_rows rows</summary>
		/// <remarks>
		/// Do nothing if num_rows is less than dim1.
		/// The new entries are filled with zeros.
		/// </remarks>
		/// <param name="num_rows">the minimum number of rows</param>
		public void AddRows(int num_rows)
		{
			if (num_rows > dim1)
			{
				// create new data structure
				float[] data_new =  new float[num_rows * dim2];
                               System.arraycopy(data, 0, data_new, 0, data.length);

				// replace old data structure
				this.dim1 = num_rows;
				this.data = data_new;
			}
		}

		/// <summary>Grows the matrix to the requested size, if necessary</summary>
		/// <remarks>
		/// The new entries are filled with zeros.
		/// </remarks>
		/// <param name="num_rows">the minimum number of rows</param>
		/// <param name="num_cols">the minimum number of columns</param>
		public void Grow(int num_rows, int num_cols)
		{
			if (num_rows > dim1 || num_cols > dim2)
			{
				// create new data structure
				float[] new_data =new float[num_rows * num_cols];
				for (int i = 0; i < dim1; i++)
					for (int j = 0; j < dim2; j++)
						new_data[i * num_cols + j] = getLocation(i,j);

				// replace old data structure
				this.dim1 = num_rows;
				this.dim2 = num_cols;
				this.data = new_data;
				
				//System.out.println("Dimenzije: "+this.dim1+" "+this.dim2);
			}
		}

		/// <summary>Sets an entire row to a specified value</summary>
		/// <param name="v">the value to be used</param>
		/// <param name="i">the row ID</param>
		public void SetRowToOneValue(int i, float v)
		{
			for (int j = 0; j < dim2; j++)
                       setLocation(i,j,v);
		}

		/// <summary>
		/// Sets an entire column to a specified value
		/// </summary>
		/// <param name="v">the value to be used</param>
		/// <param name="j">the column ID</param>
		public void SetColumnToOneValue(int j, float v)
		{
			for (int i = 0; i < dim1; i++)
                           setLocation(i,j,v);
		}
}
