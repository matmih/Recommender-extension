package com.rapidminer.data;
import java.util.List;
import java.util.Set;

/**
*Copyright (C) 2010, 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 29.07.2011
*/

public interface IEntityMapping
{
	/// <summary>all original (external) entity IDs</summary>
	/// <value>all original (external) entity IDs</value>
	Set<Integer> GetOriginalIDs();

	/// <summary>all internal entity IDs</summary>
	/// <value>all internal entity IDs</value>
	Set<Integer> GetInternalIDs();

	/// <summary>Get original (external) ID of a given entity, if the given internal ID is unknown, throw an exception.</summary>
	/// <param name="internal_id">the internal ID of the entity</param>
	/// <returns>the original (external) ID of the entitiy</returns>
	int ToOriginalID(int internal_id);

	/// <summary>Get internal ID of a given entity. If the given external ID is unknown, create a new internal ID for it and store the mapping.</summary>
	/// <param name="original_id">the original (external) ID of the entity</param>
	/// <returns>the internal ID of the entitiy</returns>
	int ToInternalID(int original_id);

	/// <summary>Get original (external) IDs of a list of given entities</summary>
	/// <param name="internal_id_list">the list of internal IDs</param>
	/// <returns>the list of original (external) IDs</returns>
	List<Integer> ToOriginalID(List<Integer> internal_id_list);

	/// <summary>Get internal IDs of a list of given entities</summary>
	/// <param name="original_id_list">the list of original (external) IDs</param>
	/// <returns>a list of internal IDs</returns>
	List<Integer> ToInternalID(List<Integer> original_id_list);
}
