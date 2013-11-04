/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

import com.google.common.base.Predicate;
import com.google.common.collect.*;

public class MatrixUtil {

	private MatrixUtil(){
	}

	/**
	 * @param row The row index. The index of the first row is <code>1</code>.
	 * @param column The column index. The index of the first column is <code>1</code>.
	 *
	 * @return The element at the specified location, or <code>null</code>.
	 *
	 * @throws IndexOutOfBoundsException If either the row or column index is out of range.
	 */
	static
	public Number getElementAt(Matrix matrix, int row, int column){
		List<Array> arrays = matrix.getArrays();
		List<MatCell> matCells = matrix.getMatCells();

		Matrix.Kind kind = matrix.getKind();
		switch(kind){
			case DIAGONAL:
				{
					// "The content is just one Array of numbers representing the diagonal values"
					if(arrays.size() == 1){
						Array array = arrays.get(0);

						List<? extends Number> elements = ArrayUtil.getNumberContent(array);

						// Diagonal element
						if(row == column){
							return elements.get(row - 1);
						} else

						// Off-diagonal element
						{
							int min = 1;
							int max = elements.size();

							if((row < min || row > max) || (column < min || column > max)){
								throw new IndexOutOfBoundsException();
							}

							return matrix.getOffDiagDefault();
						}
					}
				}
				break;
			case SYMMETRIC:
				{
					// "The content must be represented by Arrays"
					if(arrays.size() > 0){

						// Make sure the specified coordinates target the lower left triangle
						if(column > row){
							int temp = row;

							row = column;
							column = temp;
						}

						return getArrayValue(arrays, row, column);
					}
				}
				break;
			case ANY:
				{
					if(arrays.size() > 0){
						return getArrayValue(arrays, row, column);
					} // End if

					if(matCells.size() > 0){

						if(row < 1 || column < 1){
							throw new IndexOutOfBoundsException();
						}

						Number value = getMatCellValue(matCells, row, column);
						if(value == null){

							if(row == column){
								return matrix.getDiagDefault();
							}

							return matrix.getOffDiagDefault();
						}

						return value;
					}
				}
				break;
			default:
				throw new UnsupportedFeatureException(matrix, kind);
		}

		throw new InvalidFeatureException(matrix);
	}

	static
	private Number getArrayValue(List<Array> arrays, int row, int column){
		Array array = arrays.get(row - 1);

		List<? extends Number> elements = ArrayUtil.getNumberContent(array);

		return elements.get(column - 1);
	}

	static
	private Number getMatCellValue(List<MatCell> matCells, final int row, final int column){
		Predicate<MatCell> filter = new Predicate<MatCell>(){

			@Override
			public boolean apply(MatCell matCell){
				return (getRow(matCell) == row) && (getColumn(matCell) == column);
			}
		};

		MatCell matCell = Iterables.getFirst(Iterables.filter(matCells, filter), null);
		if(matCell != null){
			String value = matCell.getValue();

			return Double.valueOf(value);
		}

		return null;
	}

	/**
	 * @return The number of rows.
	 */
	static
	public int getRows(Matrix matrix){
		Integer nbRows = matrix.getNbRows();
		if(nbRows != null){
			return nbRows.intValue();
		}

		List<Array> arrays = matrix.getArrays();
		List<MatCell> matCells = matrix.getMatCells();

		Matrix.Kind kind = matrix.getKind();
		switch(kind){
			case DIAGONAL:
				{
					if(arrays.size() == 1){
						Array array = arrays.get(0);

						return ArrayUtil.getSize(array);
					}
				}
				break;
			case SYMMETRIC:
				{
					if(arrays.size() > 0){
						return arrays.size();
					}
				}
				break;
			case ANY:
				{
					if(arrays.size() > 0){
						return arrays.size();
					} // End if

					if(matCells.size() > 0){
						MatCell matCell = Collections.max(matCells, MatrixUtil.rowComparator);

						return getRow(matCell);
					}
				}
				break;
			default:
				throw new UnsupportedFeatureException(matrix, kind);
		}

		throw new InvalidFeatureException(matrix);
	}

	/**
	 * @return The number of columns.
	 */
	static
	public int getColumns(Matrix matrix){
		Integer nbCols = matrix.getNbCols();
		if(nbCols != null){
			return nbCols.intValue();
		}

		List<Array> arrays = matrix.getArrays();
		List<MatCell> matCells = matrix.getMatCells();

		Matrix.Kind kind = matrix.getKind();
		switch(kind){
			case DIAGONAL:
				{
					if(arrays.size() == 1){
						Array array = arrays.get(0);

						return ArrayUtil.getSize(array);
					}
				}
				break;
			case SYMMETRIC:
				{
					if(arrays.size() > 0){
						return arrays.size();
					}
				}
				break;
			case ANY:
				{
					if(arrays.size() > 0){
						Array array = arrays.get(arrays.size() - 1);

						return ArrayUtil.getSize(array);
					} // End if

					if(matCells.size() > 0){
						MatCell matCell = Collections.max(matCells, MatrixUtil.columnComparator);

						return getColumn(matCell);
					}
				}
				break;
			default:
				throw new UnsupportedFeatureException(matrix, kind);
		}

		throw new InvalidFeatureException(matrix);
	}

	static
	private int getRow(MatCell matCell){
		Integer row = matCell.getRow();
		if(row == null){
			throw new InvalidFeatureException(matCell);
		}

		return row.intValue();
	}

	static
	private int getColumn(MatCell matCell){
		Integer column = matCell.getCol();
		if(column == null){
			throw new InvalidFeatureException(matCell);
		}

		return column.intValue();
	}

	private static final Comparator<MatCell> rowComparator = new Comparator<MatCell>(){

		@Override
		public int compare(MatCell left, MatCell right){
			return (getRow(left) - getRow(right));
		}
	};

	private static final Comparator<MatCell> columnComparator = new Comparator<MatCell>(){

		@Override
		public int compare(MatCell left, MatCell right){
			return (getColumn(left) - getColumn(right));
		}
	};
}