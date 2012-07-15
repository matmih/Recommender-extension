package com.rapidminer.data;


import java.util.List;
/**
*Copyright (C) 2010, 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 21.07.2011
*/
public interface IBooleanMatrix extends IMatrix_b {

    /// <summary>Indexer to access the rows of the matrix</summary>
		/// <param name="x">the row ID</param>

	public List<Integer> getLocation(int x);
	public CompactHashSet<Integer> getL(int x);

		/// <summary>The number of (true) entries</summary>

     public  int NumberOfEntries();

		/// <summary>The IDs of the non-empty rows in the matrix (the ones that contain at least one true entry)</summary>

    	public List<Integer> NonEmptyRowIDs();

		/// <summary>The IDs of the non-empty columns in the matrix (the ones that contain at least one true entry)</summary>

    public	List<Integer> NonEmptyColumnIDs();

		/// <summary>Get all true entries (column IDs) of a row</summary>
		/// <param name="row_id">the row ID</param>
		/// <returns>a list of column IDs</returns>

    public List<Integer> GetEntriesByRow(int row_id);

		/// <summary>Get all the number of entries in a row</summary>
		/// <param name="row_id">the row ID</param>
		/// <returns>the number of entries in row row_id</returns>
    public int NumEntriesByRow(int row_id);

		/// <summary>Get all true entries (row IDs) of a column</summary>
		/// <param name="column_id">the column ID</param>
		/// <returns>a list of row IDs</returns>

    public	List<Integer> GetEntriesByColumn(int column_id);

		/// <summary>Get all the number of entries in a column</summary>
		/// <param name="column_id">the column ID</param>
		/// <returns>the number of entries in column column_id</returns>
    public int NumEntriesByColumn(int column_id);

		/// <summary>Get the overlap of two matrices, i.e. the number of true entries where they agree</summary>
		/// <param name="s">the <see cref="IBooleanMatrix"/> to compare to</param>
		/// <returns>the number of entries that are true in both matrices</returns>
    public int Overlap(IBooleanMatrix s);


}