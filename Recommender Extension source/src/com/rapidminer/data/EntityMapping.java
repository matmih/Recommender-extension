package com.rapidminer.data;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
*Copyright (C) 2010, 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 29.07.2011
*/

public class EntityMapping implements IEntityMapping, Serializable {
		/// <summary>Contains the mapping from the original (external) IDs to the internal IDs</summary>
		/// <remarks>
		/// Never, to repeat NEVER, delete entries from this dictionary!
		/// </remarks>
	    static final long serialVersionUID=3453435;
		com.rapidminer.improved.HashMap<Integer, Integer> original_to_internal = new com.rapidminer.improved.HashMap<Integer, Integer>();

		/// <summary>Contains the mapping from the internal IDs to the original (external) IDs</summary>
		/// <remarks>
		/// Never, to repeat NEVER, delete entries from this dictionary!
		/// </remarks>
		com.rapidminer.improved.HashMap<Integer, Integer> internal_to_original = new com.rapidminer.improved.HashMap<Integer, Integer>();

		/// <summary>all original (external) entity IDs</summary>
		/// <value>all original (external) entity IDs</value>
		public Set<Integer> GetOriginalIDs(){return original_to_internal.keySet(); }

		/// <summary>all internal entity IDs</summary>
		/// <value>all internal entity IDs</value>
		public Set<Integer> GetInternalIDs() {  return internal_to_original.keySet();	}

		/// <summary>Get original (external) ID of a given entity, if the given internal ID is unknown, throw an exception.</summary>
		/// <param name="internal_id">the internal ID of the entity</param>
		/// <returns>the original (external) ID of the entitiy</returns>
		public int ToOriginalID(int internal_id)
		{
			int original_id;
			if (internal_to_original.containsKey(internal_id)){
				original_id=internal_to_original.get(internal_id);
				return original_id;
			}
			else
				throw new IllegalArgumentException("Unknown internal ID: " + internal_id);
		}

		/// <summary>Get internal ID of a given entity. If the given external ID is unknown, create a new internal ID for it and store the mapping.</summary>
		/// <param name="original_id">the original (external) ID of the entity</param>
		/// <returns>the internal ID of the entitiy</returns>
		public int ToInternalID(int original_id)
		{
			int internal_id;
			if (original_to_internal.containsKey(original_id)){
					internal_id=original_to_internal.get(original_id);
				return internal_id;
			}
			internal_id = original_to_internal.size();
			original_to_internal.put(original_id, internal_id);
			internal_to_original.put(internal_id, original_id);
			return internal_id;
		}

		/// <summary>Get original (external) IDs of a list of given entities</summary>
		/// <param name="internal_id_list">the list of internal IDs</param>
		/// <returns>the list of original (external) IDs</returns>
		public List<Integer> ToOriginalID(List<Integer> internal_id_list)
		{
			List<Integer> result = new ArrayList<Integer>(internal_id_list.size());
			
			for(int i=0;i<internal_id_list.size();i++){
				int id=internal_id_list.get(i);
				result.add(id);
			}
			
			return result;
		}

		/// <summary>Get internal IDs of a list of given entities</summary>
		/// <param name="original_id_list">the list of original (external) IDs</param>
		/// <returns>a list of internal IDs</returns>
		public List<Integer> ToInternalID(List<Integer> original_id_list)
		{
			List<Integer> result = new ArrayList<Integer>(original_id_list.size());
			
			for(int i=0;i<original_id_list.size();i++){
				int id=original_id_list.get(i);
				result.add(id);
			}
			return result;
		}
	}
