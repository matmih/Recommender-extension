package com.rapidminer.data;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
*Copyright (C) 2010, 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 27.07.2011
*/

public class CorrelationMatrix extends Matrix_f {

	public CorrelationMatrix(){}

		/// <summary>Number of entities, e.g. users or items</summary>
		protected int num_entities;
		static final long serialVersionUID=3453435;
		/// <value>returns true if the matrix is symmetric, which is generally the case for similarity matrices</value>
		public  boolean IsSymmetric (){ 
			return true;
			}

		///
		
		public void Setnum_entities(int new_num_entities){
			num_entities=new_num_entities;
		}
		
		public int Getnum_entities(){
			return num_entities;
		}

         public void setLocation(int i, int j, float value){

              if(i>=this.dim1)
                 throw new IllegalArgumentException("i too big: " + i + ", dim1 is " + this.dim1);
             if (j >= this.dim2)
				throw new IllegalArgumentException("j too big: " + j + ", dim2 is " + this.dim2);

                   data[i * dim2 + j]=value;
                   data[j * dim2 + i] = value;
          }
		

		/// <summary>Creates a CorrelationMatrix object for a given number of entities</summary>
		/// <param name="num_entities">number of entities</param>
		public CorrelationMatrix(int num_entities)
		{
			super(num_entities,num_entities);
			this.num_entities = num_entities;
		}

		/// <summary>Creates a correlation matrix</summary>
		/// <remarks>Gives out a useful warning if there is not enough memory</remarks>
		/// <param name="num_entities">the number of entities</param>
		/// <returns>the correlation matrix</returns>
		static public CorrelationMatrix Create(int num_entities)
		{
			CorrelationMatrix cm;
				cm = new CorrelationMatrix(num_entities);	
			return cm;
		}

		/// <summary>Add an entity to the CorrelationMatrix by growing it to the requested size.</summary>
		/// <remarks>
		/// Note that you still have to correctly compute and set the entity's correlation values
		/// </remarks>
		/// <param name="entity_id">the numerical ID of the entity</param>
		public void AddEntity(int entity_id)
		{
			this.Grow(entity_id + 1, entity_id + 1);
		}

		/// <summary>Sum up the correlations between a given entity and the entities in a collection</summary>
		/// <param name="entity_id">the numerical ID of the entity</param>
		/// <param name="entities">a collection containing the numerical IDs of the entities to compare to</param>
		/// <returns>the correlation sum</returns>
		public double SumUp(int entity_id, List<Integer> entities)
		{
			if (entity_id < 0 || entity_id >= num_entities)
				throw new IllegalArgumentException("Invalid entity ID: " + entity_id);
			
			double result = 0;
			for(int i=0;i<entities.size();i++)
				if (entities.get(i) >= 0 && entities.get(i) < num_entities)
                	result +=this.getLocation(entity_id, entities.get(i));
			return result;
		}

		public class EmpComparator implements Comparator<Integer> {
			public int entity_id;
			  public int compare(Integer obj1, Integer obj2) {
			   
				  if(getLocation(obj1, entity_id)>getLocation(obj2,entity_id))
					  return -1;
				  else if(getLocation(obj1, entity_id)<getLocation(obj2,entity_id))
					  return 1;
				  else return 0;

		
			  }
		}
		
		/// <summary>Get all entities that are positively correlated to an entity, sorted by correlation</summary>
		/// <param name="entity_id">the entity ID</param>
		/// <returns>a sorted list of all entities that are positively correlated to entitiy_id</returns>
		public Integer [] GetPositivelyCorrelatedEntities(int entity_id)
		{
			
			int cnt=0;
			
			Integer[] temp=new Integer[num_entities];
			for (int i = 0; i < num_entities; i++)
				if ((this.getLocation(i, entity_id)>0) && (i!=entity_id) )
					temp[cnt++]=i;
			
			Integer[] result=new Integer[cnt];
			
			for(int i=0;i<cnt;i++)
				result[i]=temp[i];
				
			EmpComparator com=new EmpComparator();
			com.entity_id=entity_id;
			
			Arrays.sort(result,com);
			
			return result;
		}

		/// <summary>Get the k nearest neighbors of a given entity</summary>
		/// <param name="entity_id">the numerical ID of the entity</param>
		/// <param name="k">the neighborhood size</param>
		/// <returns>an array containing the numerical IDs of the k nearest neighbors</returns>
		//public List<Integer> GetNearestNeighbors(int entity_id, int k)
		public Integer[] GetNearestNeighbors(int entity_id, int k)
		{
			Integer [] entities=new Integer[num_entities-1]; 
			int cnt=0;
				for(int i=0;i<num_entities;i++)
						if(i!=entity_id)
							entities[cnt++]=i;
			
			EmpComparator com=new EmpComparator();
			com.entity_id=entity_id;

			Arrays.sort(entities,com);
			Integer[] returnA;
			
			if(k<entities.length){
				returnA=new Integer[k];
				for(int i=0;i<k;i++)
					returnA[i]=entities[i];
			}
			
			else
				returnA=entities;
			return returnA;
		}
	}
	