package com.rapidminer.data;
import java.util.Iterator;
import java.util.Set;

/**
*Copyright (C) 2010, 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 08.08.2011
*/

public class SkewSymmetricSparseMatrix extends SymmetricSparseMatrix_f {

	
	static final long serialVersionUID=3453435;
	// TODO default should be double, create a specific FloatSkewSymmetricSparseMatrix
	
		/// <summary>a skew symmetric (anti-symmetric) sparse matrix; consumes less memory</summary>
		/// <remarks>
		/// Be careful when accessing the matrix via the NonEmptyEntryIDs and
		/// NonEmptyRows properties: these contain only the entries with x &gt; y,
		/// but not their antisymmetric counterparts.
		/// </remarks>

			/// <summary>Access the elements of the sparse matrix</summary>
			/// <param name="x">the row ID</param>
			/// <param name="y">the column ID</param>
			public float getLocation(int x,int y)
			{
					float result = 0f;
					
					if (x < y)
					{
						if (x < row_list.size() && row_list.get(x).containsKey(y)){
						result=row_list.get(x).get(y);	
							return result;
						}
					}
					else if (x > y)
					{
						if (y < row_list.size() && row_list.get(y).containsKey(x)){
							result=row_list.get(y).get(x);
							return -result; // minus for anti-symmetry
						}
					}

					return result;
				}
			
			public void setLocation(int x,int y, float value)
				{
				
					if (x < y)
					{
						if (x >= row_list.size())
							for (int i = row_list.size(); i <= x; i++)
								row_list.add( new com.rapidminer.improved.HashMap<Integer, Float>() );
						
						row_list.get(x).put(y, value);					
					}
					else if (x > y)
					{
						if (y >= row_list.size())
							for (int i = row_list.size(); i <= y; i++)
								row_list.add( new com.rapidminer.improved.HashMap<Integer, Float>() );
						
						row_list.get(y).put(x, -value);
					}
					else
					{
						// all elements on the diagonal must be zero
						if (value != 0)
							row_list.get(x).put(y, 0.0f);
						//throw new IllegalArgumentException("Elements of the diagonal of a skew symmetric matrix must equal 0");
					}
				}
			

			/// <summary>Only true if all entries are zero</summary>
			/// <value>Only true if all entries are zero</value>
			public boolean IsSymmetric()
			{
	
					for (int i = 0; i < row_list.size(); i++){
						
						Set<Integer> s=row_list.get(i).keySet();
						Iterator<Integer> it=s.iterator();
						
						while(it.hasNext()){
							
							if(this.getLocation(i, it.next())!=0)
								return false;
						}
					}
					return true;
				
			}

			/// <summary>Create a skew symmetric sparse matrix with a given dimension</summary>
			/// <param name="dimension">the dimension (number of rows/columns)</param>
			public SkewSymmetricSparseMatrix(int dimension){ super(dimension);}

			///
			public IMatrix_f CreateMatrix(int num_rows, int num_columns)
			{
				if (num_rows != num_columns)
					throw new IllegalArgumentException("Skew symmetric matrices must have the same number of rows and columns.");
				return new SkewSymmetricSparseMatrix(num_rows);
			}
		}
