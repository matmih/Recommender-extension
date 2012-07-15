package com.rapidminer.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
*Copyright (C) 2010, 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 22.07.2011
*/

public class SparseBooleanMatrix implements IBooleanMatrix 
{
	private List<CompactHashSet<Integer>> row_list = new ArrayList<CompactHashSet<Integer>>();
	static final long serialVersionUID=3453435;
	/// <summary>Indexer to access the elements of the matrix</summary>
	/// <param name="x">the row ID</param>
	/// <param name="y">the column ID</param>
	
	public boolean getLocation(int x, int y){
		

			if (x < row_list.size())
				return row_list.get(x).contains(y);
			else
				return false;
	}
	
	
	public CompactHashSet<Integer> getL(int x){
		
		if (x >= row_list.size())
			for (int i = row_list.size(); i <= x; i++)
				row_list.add(new CompactHashSet<Integer>());
		
		
		return row_list.get(x);
	}
	
	public void setLocation(int x, int y, boolean value)
		{
			if (value)
				this.getL(x).add(y);
			else
				this.getL(x).remove(y);
		}


	public List<Integer> getLocation(int x)
	{	
		if (x >= row_list.size())
			for (int i = row_list.size(); i <= x; i++)
				row_list.add(new CompactHashSet<Integer>());
		
		ArrayList<Integer> t1=new ArrayList<Integer>();
		
		
        Iterator<Integer> it=row_list.get(x).iterator();
		
		while(it.hasNext()){
			t1.add(it.next());
		}
		
		return t1;
	}
	

	///
	public boolean IsSymmetric()
	{
		
			for (int i = 0; i < row_list.size(); i++)
				for(int j=0;j<row_list.get(i).size();j++)
				{
					if (i > j)
						continue; // check every pair only once

					if (!this.getLocation(j,i))
						return false;
				}
			return true;
	}

	///
	public SparseBooleanMatrix CreateMatrix(int x, int y)
	{
		return new SparseBooleanMatrix();
	}

	///
	public List<Integer> GetEntriesByRow(int row_id)
	{
		
		ArrayList<Integer> ret=new ArrayList<Integer>(row_list.get(row_id));
		Collections.sort(ret);
		return ret;
	}

	///
	public int NumEntriesByRow(int row_id)
	{
		return row_list.get(row_id).size();
	}		
	
	
	public int NumberOfRows()	{  return row_list.size();  }
	
	/// <remarks>Takes O(N) worst-case time, where N is the number of rows, if the internal hash table can be queried in constant time.</remarks>
	public List<Integer> GetEntriesByColumn(int column_id)
	{
		List<Integer> list = new ArrayList<Integer>();

		for (int row_id = 0; row_id < NumberOfRows(); row_id++)
			if (row_list.get(row_id).contains(column_id))
				list.add(row_id);
		return list;
	}

	///
	public int NumEntriesByColumn(int column_id)
	{
		int count = 0;

		for (int row_id = 0; row_id < NumberOfRows(); row_id++)
			if (row_list.get(row_id).contains(column_id))
				count++;
		return count;
	}		
	
	public int NumberOfEntries()
	{
			int n = 0;
			
			for(int i=0;i<row_list.size();i++){
				CompactHashSet<Integer> temp=row_list.get(i);
				
				n+=temp.size();
			}
			return n;
	}
			
	
	public List<Integer> NonEmptyColumnIDs()
	{

			List<Integer> col_ids = new ArrayList<Integer>();

			Iterator<Integer> it;
			// iterate over the complete data structure to find column IDs
			for (int i = 0; i < row_list.size(); i++){
					it=row_list.get(i).iterator();
					
					while(it.hasNext()){
						int id=it.next();
						if(!col_ids.contains(id))
							col_ids.add(id);
					}
				}
				
			return col_ids;
		
	}

	
	public int NumberOfColumns() {

			int max_column_id = -1;
			int max_r=0,max=0;
			Iterator<Integer> it;
			
			for(int i=0;i<row_list.size();i++){
			 it=row_list.get(i).iterator();
			 
			 while(it.hasNext()){
				 int col=it.next();
				 if(col>max)
					 max_r=col;
			 }
			 
			 if(max_r>max)
					max=max_r;
				max_r=0;
			 
				if(max_r>max)
					max=max_r;
				max_r=0;
			}
			
			max_column_id=max;
			return max_column_id + 1;
		
	}
	
	
	public int Overlap(IBooleanMatrix s)
	{
		int c = 0;
		Iterator<Integer> it;
		
		for (int i = 0; i < row_list.size(); i++){
			
			it=row_list.get(i).iterator();
			
			while(it.hasNext()){
				int col=it.next();
				if(s.getLocation(i, col))
					c++;
			}
		}
		
		return c;
	}
	
	
	public List<Integer> NonEmptyRowIDs()
	{
		 List<Integer> row_ids = new ArrayList<Integer>();

			for (int i = 0; i < row_list.size(); i++)
				if (row_list.get(i).size() > 0)
					row_ids.add(i);

			return row_ids;
	}
	
	
	public IMatrix_b Transpose()
	{
		IMatrix_b transpose = new SparseBooleanMatrix();
		for (int i = 0; i < row_list.size(); i++){
		     List<Integer> col= this.getLocation(i);
		     
		     for(int j=0;j<col.size();j++){
		    	 int cid=col.get(j);
		    	 transpose.setLocation(cid, i, true);
		     }
		}
		
		return transpose;
	}
}