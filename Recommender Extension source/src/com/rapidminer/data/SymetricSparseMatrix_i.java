package com.rapidminer.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.rapidminer.tools.container.Tupel;

/**
*Copyright (C) 2010, 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 8.08.2011
*/

public class SymetricSparseMatrix_i extends SparseMatrix_i{

		static final long serialVersionUID=3453435;
		/// <summary>Access the elements of the sparse matrix</summary>
		/// <param name="x">the row ID</param>
		/// <param name="y">the column ID</param>
		public int getLocation(int x,int y)
		{
				// ensure x <= y
				if (x > y)
				{
					int tmp = x;
					x = y;
					y = tmp;
				}

				int result;
				if (x < row_list.size() && row_list.get(x).containsKey(y)){
					result=row_list.get(x).get(y);
					return result;
				}
				else
					return 0;
			}
		
		public void setLocation(int x, int y, int value)
			{
				// ensure x <= y
				if (x > y)
				{
					int tmp = x;
					x = y;
					y = tmp;
				}

				if (x >= row_list.size())
					for (int i = row_list.size(); i <= x; i++)
						row_list.add( new com.rapidminer.improved.HashMap<Integer, Integer>() );

				row_list.get(x).put(y, value);
			}
		

		/// <summary>Always true because the data type is symmetric</summary>
		/// <value>Always true because the data type is symmetric</value>
		public boolean IsSymmetric() {  return true;  }

		/// <summary>Create a symmetric sparse matrix with a given dimension</summary>
		/// <param name="dimension">the dimension (number of rows/columns)</param>
		public SymetricSparseMatrix_i(int dimension) {super(dimension,dimension); }

		///
		public IMatrix_i CreateMatrix(int num_rows, int num_columns)
		{
			if (num_rows != num_columns)
				throw new IllegalArgumentException("Symmetric matrices must have the same number of rows and columns.");
			return new SymetricSparseMatrix_i(num_rows);
		}
		
		///
		public List<Tupel<Integer, Integer>> NonEmptyEntryIDs()
		{

				List<Tupel<Integer,Integer>> return_list = new ArrayList<Tupel<Integer, Integer>>();
				
				ArrayList<Tupel<Integer,com.rapidminer.improved.HashMap<Integer,Integer>>> temp=this.NonEmptyRows();
				for(int i=0;i<temp.size();i++){
					int rid;
					
		
					rid=temp.get(i).getFirst();
					
					   Iterator<Integer> it1=temp.get(i).getSecond().keySet().iterator();
					   
					   while(it1.hasNext()){
						   
						   int cid=it1.next();
						   return_list.add(new Tupel<Integer,Integer>(rid,cid));
						   
						   if(rid!=cid)
								return_list.add(new Tupel<Integer,Integer>(cid,rid));
						   
					   }
					}
				
				return return_list;
			
		}
		
		///
		public int NumberOfNonEmptyEntries()
		{
				int counter = 0;
				for (int i = 0; i < row_list.size(); i++)
				{
					counter += 2 * row_list.get(i).size();
					
					// adjust for diagonal elements
					if (row_list.get(i).containsKey(i))
						counter--;
				}
				
				return counter;
		}		

}
