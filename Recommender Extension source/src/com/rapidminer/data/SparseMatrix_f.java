package com.rapidminer.data;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.rapidminer.tools.container.Tupel;

public class SparseMatrix_f implements IMatrix_f {

	
	protected List<com.rapidminer.improved.HashMap<Integer, Float>> row_list = new ArrayList<com.rapidminer.improved.HashMap<Integer, Float>>();
	int num_cols;

	static final long serialVersionUID=3453435;
	
	public boolean IsSymmetric()
	{
		
			if (NumberOfRows() != NumberOfColumns())
				return false;
			
			for (int i = 0; i < row_list.size(); i++)
				for(int j=0;j<row_list.get(i).keySet().size();j++){
					double temp=row_list.get(i).get(j);

					if (i > temp)
						continue; // check every pair only onc
					if (! (this.getLocation(i, j)==this.getLocation(j, i)))
						return false;
				}
			return true;
		
	}

	///
	public int NumberOfRows() { return row_list.size(); }

	///
	public int NumberOfColumns() { return num_cols; }
	
	
	
	public com.rapidminer.improved.HashMap<Integer, Float> Get (int x)
			{

		            if (x >= row_list.size())
		                return new com.rapidminer.improved.HashMap<Integer, Float>();
		            else return row_list.get(x);

			}

			/// <summary>Access the elements of the sparse matrix</summary>
			/// <param name="x">the row ID</param>
			/// <param name="y">the column ID</param>
			public float getLocation(int x, int y)
			{
				
					//int result;
					if (x < row_list.size() && row_list.get(x).containsKey(y)){
						if(row_list.get(x).get(y)>0)
						return row_list.get(x).get(y);
						else
							return 0;
					}
					else{
							return 0;
					}
						
				}
			
			public double getLocation1(int x, int y)
			{
				
					//int result;
					if (x < row_list.size() && row_list.get(x).containsKey(y)){
						if(row_list.get(x).get(y)>0)
						return row_list.get(x).get(y);
						else
							return 0.0; //-1
					}
					else{
						return 0.0;
					}
						
				}
			
			public void setLocation(int x, int y, float value){
					if (x >= row_list.size())
						for (int i = row_list.size(); i <= x; i++)

							row_list.add( new com.rapidminer.improved.HashMap<Integer, Float>() );
	
					row_list.get(x).put(y, value);
				}
			

	/// <summary>Create a sparse matrix with a given number of rows</summary>
	/// <param name="num_rows">the number of rows</param>
	/// <param name="num_cols">the number of columns</param>
	public SparseMatrix_f(int num_rows, int _num_cols)
	{
		for (int i = 0; i < num_rows; i++)
			row_list.add( new com.rapidminer.improved.HashMap<Integer, Float>() );

		num_cols = _num_cols;
	}

	///
	public IMatrix_f CreateMatrix(int num_rows, int num_columns)
	{
		return new SparseMatrix_f(num_rows, num_columns);
	}

	///
	
	public  ArrayList<Tupel<Integer, com.rapidminer.improved.HashMap<Integer, Float>>>  NonEmptyRows()
	{
		
		ArrayList<Tupel<Integer,com.rapidminer.improved.HashMap<Integer,Float>>> return_list = new ArrayList<Tupel<Integer, com.rapidminer.improved.HashMap<Integer, Float>>>();
			
		for (int i = 0; i < row_list.size(); i++)
			if (row_list.get(i).size() > 0){
				return_list.add(new Tupel<Integer,com.rapidminer.improved.HashMap<Integer,Float>>(i,row_list.get(i)));
			}
		return return_list;
	}
	
	public List<Tupel<Integer, Integer>> NonEmptyEntryIDs()
	{
		
		
		List<Tupel<Integer,Integer>> return_list = new ArrayList<Tupel<Integer, Integer>>();
		

		ArrayList<Tupel<Integer,com.rapidminer.improved.HashMap<Integer,Float>>> temp=this.NonEmptyRows();
		
		for(int i=0;i<temp.size();i++){
			int rid;
			

			rid=temp.get(i).getFirst();
			
			   Iterator<Integer> it1=temp.get(i).getSecond().keySet().iterator();
			   
			   while(it1.hasNext()){
				   
				   int cid=it1.next();
				   return_list.add(new Tupel<Integer,Integer>(rid,cid));
			   }
			}
		
		return return_list;
	}
	
	public IMatrix_f Transpose()
	{
		SparseMatrix_f transpose = new SparseMatrix_f(NumberOfColumns(), NumberOfRows());
		
		
		for(int i=0;i<this.NonEmptyEntryIDs().size();i++){
			Tupel<Integer,Integer> index=this.NonEmptyEntryIDs().get(i);

			int rid=index.getFirst();
			
			int cid=index.getSecond();
			
			transpose.setLocation(cid, rid, transpose.getLocation(rid, cid));
		}
		
		return transpose;
	}
	
}

