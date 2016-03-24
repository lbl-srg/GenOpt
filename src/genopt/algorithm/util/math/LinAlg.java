package genopt.algorithm.util.math;

/** Class with functions for linear algebra.
  *
  * <P><I>This project was carried out at:</I>
  * <UL><LI><A HREF="http://www.lbl.gov">
  * Lawrence Berkeley National Laboratory (LBNL)</A>,
  * <A HREF="http://simulationresearch.lbl.gov">
  * Simulation Research Group</A>,</LI></UL>
  * <I>and supported by</I><UL>
  * <LI>the <A HREF="http://www.energy.gov">
  * U.S. Department of Energy (DOE)</A>,
  * <LI>the <A HREF="http://www.satw.ch">
  * Swiss Academy of Engineering Sciences (SATW)</A>,
  * <LI>the Swiss National Energy Fund (NEFF), and
  * <LI>the <A HREF="http://www.snf.ch">
  * Swiss National Science Foundation (SNSF)</A></LI></UL><P>
  *
  * GenOpt Copyright (c) 1998-2016, The Regents of the University of
  * California, through Lawrence Berkeley National Laboratory (subject 
  * to receipt of any required approvals from the U.S. Dept. of Energy).  
  * All rights reserved.
  *
  * @author <A HREF="mailto:MWetter@lbl.gov">Michael Wetter</A>
  *
  * @version GenOpt(R) 3.1.1 (March 24, 2016)<P>
  */

/*
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  * 
  * (1) Redistributions of source code must retain the above copyright notice, 
  * this list of conditions and the following disclaimer.
  * 
  * (2) Redistributions in binary form must reproduce the above copyright 
  * notice, this list of conditions and the following disclaimer in the 
  * documentation and/or other materials provided with the distribution.
  * 
  * (3) Neither the name of the University of California, Lawrence Berkeley 
  * National Laboratory, U.S. Dept. of Energy nor the names of its 
  * contributors may be used to endorse or promote products derived from 
  * this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
  * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
  * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * 
  * You are under no obligation whatsoever to provide any bug fixes, 
  * patches, or upgrades to the features, functionality or performance of 
  * the source code ("Enhancements") to anyone; however, if you choose to 
  * make your Enhancements available either publicly, or directly to 
  * Lawrence Berkeley National Laboratory, without imposing a separate 
  * written license agreement for such Enhancements, then you hereby grant 
  * the following license: a non-exclusive, royalty-free perpetual license 
  * to install, use, modify, prepare derivative works, incorporate into 
  * other computer software, distribute, and sublicense such enhancements 
  * or derivative works thereof, in binary and source code form. 
 */

public class LinAlg
{
	/** gets the center point of several points
	  * @param M Matrix where the row are the points from which the
	  *          center has to be determined (the row has the first element
	  *          number, i.e. row i: A[i][0...dim])
	  * @return the center point
	 */
	public static double[] getCenter(double[][] M)
	{
		int n = M.length;
		int dim = M[0].length;
		double[] x = new double[dim];
		initialize(x, 0);
		
		for(int i = 0; i < n; i++)
			x = add(x, M[i]);

		return LinAlg.multiply( 1/((double)n), x);
	}

	/** calculates the maximum magnitude of the difference between
	  * corresponding matrix element, defined as
	  * maxDiff = max(|A[i][j]-A[i][j]|) for all i,j
	  * @param A rectangular matrix
	  * @param B rectangular matrix of the same dimension like A
	  * @return the maximum magnitude of the difference between
	  * corresponding matrix element
	  */
	public static double maxDiff(double[][] A, double[][] B)
	{
		double md = 0;
		for (int i = 0; i < A.length; i++)
		{
			for (int j = 0; j < A[0].length; j++)
			{
				md = Math.max(md, Math.abs(A[i][j]-B[i][j]));
			}
		}
		return md;
	}

	/** calculates the absoulte value of a vector
	  * @param x the vector
	  * @return the absolute value of <CODE>x = ((sum(x(i)**2)**(1/2), i = 0..N-1)</CODE>
	 */
	public static double abs(double[] x)
	{
		int N = x.length;
		double r = 0;
		for (int i = 0; i < N; i++)
			r += x[i]*x[i];
		return Math.pow(r, 0.5);
	}


	/** gets a part of a vector
	  * @param x vector
	  * @param startIndex index of first element that will be returned
	  * @param number number of elements that will be returned
	  * @return i-th column A[k][i] where k = 0...n
	  */
	public static double[] getSub(double[] x, int startIndex, int number)
	{
		int i, j;
		int endIndex = startIndex + number;
		double[] ret = new double[number];
		for (i = 0, j = startIndex; j < endIndex; i++, j++)
			ret[i] = x[j];
		return ret;
	}

	/** gets a column of a matrix
	  * @param A matrix
	  * @param i number of column
	  * @return i-th column A[k][i] where k = 0...n
	  */
	public static double[] getColumn(double[][] A, int i)
	{
		int n = A.length;
		double[] c = new double[n];
		for (int j = 0; j < n; j++)
			c[j] = A[j][i];
		return c;
	}

	/** initializes a vector
	  * @param A vector
	  * @param v value to be set for all elements
	  */
	public static void initialize(double[] A, double v)
	{
		for (int i = 0; i < A.length; i++)
			A[i] = v;
		return;
	}

	/** initializes a rectangular matrix
	  * @param A matrix
	  * @param v value to be set for all elements
	  */
	public static void initialize(double[][] A, double v)
	{
		for (int i = 0; i < A.length; i++)
			for (int j = 0; j < A[0].length; j++)
				A[i][j] = v;
		return;
	}


	/** sets a column of a matrix<BR>
	  * <b>Note:</b> the dimension of x can be smaller than the column
	  *              length of A
	  * @param A matrix
	  * @param x Vector to be set as the i-th column of A
	  * @param i column number where x has to be set
	  * @return matrix A where the i-th column is replaced by x
	  */
	public static double[][] setColumn(double[][] A, double[] x, int i)
	{
		int j;
		int n = x.length;
		int m = A[0].length;
		int o = A.length;
		double[][] B = new double[o][m];
		for (j = 0; j < o; j++)
			System.arraycopy(A[j], 0, B[j], 0, m);
		for (j = 0; j < n; j++)
			B[j][i] = x[j];
		return B;
	}

	/** sets a row of a matrix<BR>
	  * <b>Note:</b> the dimension of x can be smaller than the row
	  *              length of A
	  * @param A matrix
	  * @param x Vector to be set as the i-th row of A
	  * @param i row number where x has to be set
	  * @return matrix A where the i-th row is replaced by x
	  */
	public static double[][] setRow(double[][] A, double[] x, int i)
	{
		int j;
		int n = x.length;
		int m = A[0].length;
		int o = A.length;
		double[][] B = new double[o][m];
		for (j = 0; j < o; j++)
			System.arraycopy(A[j], 0, B[j], 0, m);
		for (j = 0; j < n; j++)
			B[i][j] = x[j];
		return B;
	}

	/** prints a matrix to the output stream
	  * @param A matrix to be printed
	  */
	public static void print(double[][] A)
	{
		int n = A.length;
		int m = A[0].length;
		String s;
		for (int i = 0; i < n; i++)
		{
			s = new String();
			if ( i == 0)
			{
				System.out.println();
				s = "  ; ";
				for (int j = 0; j < m; j++)
					s += j + " ;";
				System.out.println(s);
			}
			for (int j = 0; j < m; j++)
				{
					if ( j == 0)
					{
						s = new String();
						s = i + " ;";
					}
					s += A[i][j] + " ;";
			}
			System.out.println(s);
		}
		return ;
	}
	
	/** prints a vector to the output stream
	  * @param x Vector to be printed
	  */
	public static void print(double[] x)
	{
		int n = x.length;
		System.out.println();
		String s = "";
		for (int j = 0; j < n; j++)
			s += x[j] + " ;";
		System.out.println(s);
		return ;
	}

	/** prints a vector to the output stream
	  * @param x Vector to be printed
	  */
	public static void print(int[] x)
	{
		int n = x.length;
		System.out.println();
		String s = "";
		for (int j = 0; j < n; j++)
			s += x[j] + " ;";
		System.out.println(s);
		return ;
	}

	/** fills the diagonal matrix element and sets all other elements to zero.
	  * @param A square matrix to be filled
	  * @param left Element (i-1, j)
	  * @param diagonal Element (i, j)
	  * @param right Element (i+1, j)
	  * @return Filled matrix
	  */
	public static double[][] fillDiagonal(
		double[][] A, double left, double diagonal, double right)
	{
		int i, j;
		int m = A[0].length;
		int n = A.length;
		double[][] B = new double[n][m];

		int nM1 = n - 1;

		for (i = 0; i < (n-2); i++)
		{
			for (j = i+2; j < n; j++)
			{
				B[i][j]         = 0;
				B[nM1-i][nM1-j] = 0;
			}
		}

		for (i = 0; i < n; i++)
			B[i][i] = diagonal;

		for (i = 0; i < (n-1); i++)
		{
			B[i][i+1] = right;
			B[i+1][i] = left;
		}
		
		return B;
	}
	/** calculates L1 norm of a matrix<BR>
	  * The one-norm of a matrix is the maximum column sum, where the column sum
	  * is the sum of the magnitudes of the elements in a given column.
	  * @param A matrix
	  * @return L1
	  */
	public static double oneNorm(double[][] A)
	{
		double L1 = 0;
		double t;
		for (int j = 0; j < A[0].length; j++)
		{
			t = 0;
			for (int i = 0; i < A.length; i++)
			{
				t += Math.abs(A[i][j]);
			}
			L1 = Math.max(L1, t);
		}
		return L1;
	}

	/** calculates Lmax norm of a matrix<BR>
	  * The max norm of a matrix is the maximum row
	  * sum, where the row sum is the sum of the magnitudes of
	  * the elements in a given row
	  * @param A matrix
	  * @return Lmax
	  */
	public static double maxNorm(double[][] A)
	{
		double Lmax = 0;
		double t;
		for (int i = 0; i < A.length; i++)
		{
			t = 0;
			for (int j = 0; j < A[0].length; j++)
			{
				t += Math.abs(A[i][j]);
			}
			Lmax = Math.max(Lmax, t);
		}
		return Lmax;
	}



	/** calculates L1 norm of a vector, defined as
	  * L1(u) = (sum(u(i), i = 1..n)
	  * @param u Vector
	  * @return L1
	  */
	public static double oneNorm(double[] u)
	{
		int n = u.length;
		double L1 = 0;
		for (int i = 0; i < n; i++)
			L1 += u[i];
		return L1;
	}
	/** returns the sum of the elements in the i-th row
	  * @param A Matrix
	  * @param i Row number
	  * @return sum(A[i][j], j = 0..N-1)
	  */
	public static double sumRow(double[][] A, int i)
	{
		double s = 0;
		for (int j = 0; j < A[0].length; j++)
			s += A[i][j];
		return s;
	}


	/** returns the sum of the elements in the i-th column
	  * @param A Matrix
	  * @param i Column number
	  * @return sum(A[j][i], j = 0..N-1)
	  */
	public static double sumColumn(double[][] A, int i)
	{
		double s = 0;
		for (int j = 0; j < A.length; j++)
			s += A[j][i];
		return s;
	}

	/** calculates the L2 norm of a vector, defined as
	  * L2(u) = (sum(u(i)**2, i = 1..n) ^ 0.5
	  * @param u Vector
	  * @return L2
	  */
	public static double twoNorm(double[] u)
	{
		int n = u.length;
		double L2 = 0;
		for (int i = 0; i < n; i++)
			L2 += u[i] * u[i];
		return Math.pow(L2, 0.5);
	}

	/** calculates the L2 norm with a scaling factor of a vector,
	  * defined as L2(u) = (sum(h * u(i)**2, i = 1..n) ^ 0.5
	  * @param u Vector
	  * *param h Scaling factor
	  * @return L2
	  */
	public static double twoNorm(double[] u, double h)
	{
		int n = u.length;
		double L2 = 0;
		for (int i = 0; i < n; i++)
			L2 += h * u[i] * u[i];
		return Math.pow(L2, 0.5);
	}

	/** calculates the Lmax norm of a vector, defined as
	  * Lmax(u) = (max(|u(i)|, i = 1..n)
	  * @param u Vector
	  * @return Lmax
	  */
	public static double maxNorm(double[] u)
	{
		int n = u.length;
		double L = 0;
		for (int i = 0; i < n; i++)
			L = Math.max(Math.abs(u[i]), L);
		return L;
	}


	/** subtracts 2 vectors: z = y - x;<br>
	  * <b>Note:</b> If the dimension dx of x is bigger than the dimension
	  *              dy of y, then only dx elements are subtracted.
	  * @param x Vector of size dx
	  * @param y Vector of size dy (dy &ge; dx)
	  * @return z Vector of size dx
	  */
	public static double[] subtract(double[] y, double[] x)
	{
		int n = y.length;
		double[] z = new double[n];
		for (int i = 0; i < n; i++)
			z[i] = y[i] - x[i];
		return z;
	}

	/** subtracts 2 vectors: z = y - x;<br>
	  * <b>Note:</b> If the dimension dx of x is bigger than the dimension
	  *              dy of y, then only dx elements are subtracted.
	  * @param x Vector of size dx
	  * @param y Vector of size dy (dy &ge; dx)
	  * @return z Vector of size dx
	  */
	public static int[] subtract(int[] y, int[] x)
	{
		int n = y.length;
		int[] z = new int[n];
		for (int i = 0; i < n; i++)
			z[i] = y[i] - x[i];
		return z;
	}

	/** adds 2 vectors: z = y + x<br>
	  * <b>Note:</b>If the dimension dx of x is bigger than the dimension
	  *             dy of y, then only dx elements are added.
	  * @param x Vector of size dx
	  * @param y Vector of size dy (dy &ge; dx)
	  * @return z Vector of size dx
	  */
	public static double[] add(double[] y, double[] x)
	{
		int n = y.length;
		double[] z = new double[n];
		for (int i = 0; i < n; i++)
			z[i] = y[i] + x[i];
		return z;
	}

	/** adds 2 matrices: C = A + B;<br>
	  * <b>Note:</b>C has the dimension of A; B must not have the same
	  *               dimension as A (non existing element are considered
	  *               as 0 and elements that are not in A but in B are not
	  *               taken into account by the summation)
	  * @param A Matrix of any dimension
	  * @param B Matrix of any dimension
	  * @return C Matrix of dimension A
	  */
	public static double[][] add(double[][] A, double[][] B)
	{
		int i, j;
		int nA = A.length;
		int mA = A[0].length;
		int n = Math.min(A.length,    B.length);
		int m = Math.min(A[0].length, B[0].length);
		double[][] C = new double[nA][mA];
		for ( i = 0; i < nA; i++)
			for ( j = 0; j < mA; j++)
				C[i][j] = A[i][j];
		for ( i = 0; i < n; i++)
			for ( j = 0; j < m; j++)
				C[i][j] += B[i][j];
		return C;
	}

	/** subtracts 2 matrices: C = A - B;<br>
	  * <b>Note:</b>C has the dimension of A; B must not have the same
	  *               dimension as A (non existing element are considered
	  *               as 0 and elements that are not in A but in B are not
	  *               taken into account by the summation)
	  * @param A Matrix of any dimension
	  * @param B Matrix of any dimension
	  * @return C Matrix of dimension A
	  */
	public static double[][] subtract(double[][] A, double[][] B)
	{
		int i, j;
		int nA = A.length;
		int mA = A[0].length;
		int n = Math.min(A.length,    B.length);
		int m = Math.min(A[0].length, B[0].length);
		double[][] C = new double[nA][mA];
		for ( i = 0; i < nA; i++)
			for ( j = 0; j < mA; j++)
				C[i][j] = A[i][j];
		for ( i = 0; i < n; i++)
			for ( j = 0; j < m; j++)
				C[i][j] -= B[i][j];
		return C;
	}

	/** calculates the inner product (dot product):
	  *    c[i] = sum(a[i] * b[i] , i = 0..N-1)
	  * @param a Array
	  * @param b Array
	  * @return c Inner procuct
	  */
	public static double innerProduct(double a[], double b[])
	{
		double c = 0;
		for (int i = 0; i < a.length; i++)
			c += a[i] * b[i];
		return c;
	}

	/** calculates the outer product (Tensor product):
	  *        M[i][j] = a[i] * b[j]
	  * @param a Array
	  * @param b Array
	  * @return M Outer product of a and b
	  */
	public static double[][] outerProduct(double[] a, double[] b)
	{
		int n = a.length;
		double[][] M = new double[n][n];
		for (int i = 0; i < n; i++)
			for (int j = 0; j <n; j++)
				M[i][j] = a[i] * b[j];
		return M;
	}

	/** calculates S-multiplication : r = s * u;
	  * @param s Scalar
	  * @param u Vector
	  * @return r Vector r = s * u;
	  */
	public static double[] multiply(double s, double[] u)
	{
		int n = u.length;
		double[] r = new double[n];
		for (int i = 0; i < n; i++)
			r[i] = s * u[i];
		return r;
	}

	/** calculates S-multiplication : r = s * u;
         *
	 * The computations are done in <CODE>double</CODE>.
	 * @param s Scalar
	 * @param u Vector
	 * @return r Vector r = s * u;
	 */
	public static double[] multiply(double s, int[] u)
	{
		final int n = u.length;
		double[] r = new double[n];
		for (int i = 0; i < n; i++)
			r[i] = s * (double)(u[i]);
		return r;
	}

	/** calculates S-multiplication : B = s * A;
	  * @param s Scalar
	  * @param A Matrix
	  * @return B Matrix B = s * A;
	  */
	public static double[][] multiply(double s, double[][] A)
	{
		int n = A.length;
		int m = A[0].length;
		double[][] B = new double[n][m];
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++)
				B[i][j] = s * A[i][j];
		return B;
	}

	/** multiplicates a vector with a matrix: r = u^T * A; <br>
	  * (Returns the product of the row vector x and the rectangular
	  *  array A)
	  * @param u Vector
	  * @param A Matrix
	  * @return r vector r = u^T * A;
	  */
	public static double[] multiply( double[] u, double[][] A)
	{
		int i, j;
		int n = A[0].length;
		double[] r = new double[n];
		for (j = 0; j < n; j++)
			r[j] = 0;

		for (i = 0; i < n; i++)
			for (j = 0; j < u.length; j++)
				r[i] += A[j][i] * u[j];
		return r;
	}

	/** multiplicates a matrix with a vector: r = A * u; <br>
	  * (Returns the product of the row vector x and the rectangular
	  *  array A)
	  * @param A matrix
	  * @param u vector
	  * @return r vector r = A * u;
	  */
	public static double[] multiply(double[][] A, double[] u)
	{
		int i, j;
		int n = A.length;
		int m = u.length;
		double[] r = new double[n];
		for (j = 0; j < n; j++)
			r[j] = 0;

		for (i = 0; i < n; i++)
			for (j = 0; j < m; j++)
				r[i] += A[i][j] * u[j];
		return r;
	}

	/** multiplicates a matrix with a matrix: C = A * B;
	  * @param A n x m matrix
	  * @param B m x n matrix
	  * @return C m x m matrix
	  */
	public static double[][] multiply(double[][] A, double[][] B)
	{
		int i, j, k;
		int nA = A.length; // # of rows
		int nB = B.length; // # of rows
		double[][] C = new double[nA][nA];

		for (i = 0; i < nA; i++)
			for (j = 0; j < nA; j++)
			{
				C[i][j] = 0;
				for (k = 0; k < nB; k++)
					C[i][j] += A[i][k] * B[k][j];
			}
		return C;
	}


	/** solves a vector x by a Gauss elimination of a NxN matrix with
	  * normalization and interchange of rows.<br>
	  * Method solves the equation A*x=f for x.
	  * @param A Matrix
	  * @param f Array with solution of A*x=f
	  * @return x Array x = A**(-1) * f
	  */
	public static double[] gaussElimination(double[][] A, double[] f)
	{
		int i, j, k, piv, iMax, jMax;
		int dim = f.length;
		int dimP1 = dim + 1;
		double[]   r = new double[dim];
		double[][] B = new double[dim][dimP1];
		double[]   tempRow = new double[dimP1];
		double a, pivotElement;
		double aMax = -1;

		for (i = 0; i < dim; i++)
		{
			for (j = 0; j < dim; j++)
				B[i][j] = A[i][j];
			B[i][dim] = f[i];
		}

		for (piv = 0; piv < dim; piv++)
		{
		//interchange rows if necessary
			iMax = 0;
			jMax = 0;
			for (i = 0; i < dim; i++)
			{
				for(j = dim-1; j >= 0; j--)
				{
					if(Math.abs(B[i][j]) > aMax)
					{
						aMax = Math.abs(B[i][j]);
						iMax = i;
						jMax = j;
					}
				}
			}

			if ( iMax != jMax)
			{
				for (i = 0; i < dimP1; i++)
				{
					tempRow[i] = B[iMax][i];
					B[iMax][i] = B[jMax][i];
					B[jMax][i] = tempRow[i];
				}
			}


			pivotElement = B[piv][piv];

		// normalization of pivot row
			for (j = 0; j < dimP1; j++)
				B[piv][j] = B[piv][j]/pivotElement;

		// elimination
			for(k = 0; k < dim; k++)
			{
				if(piv!=k)
				{
					a = B[k][piv];
					for(j = 0 ; j < dimP1; j++) // set new row
					{
						B[k][j] =  B[k][j] - a * B[piv][j];
					}
				}
			}
		}


	for (i = 0; i < dim; i++)
			r[i] = B[i][dim];

	return r;
	}

	/** solves a vector x by a Gauss elimination of a tridiagonal NxN
	  * matrix with normalization and interchange of rows.<br>
	  * Method solves the equation A*x=f for x.<br>
	  * <b>Note:</b> A has to be a NxN tridiagonal matrix
	  * where the entries are zero expect on the main diagonal (i=j)
	  * and the diagonals just above and below. The gaussian elimination
	  * method solves the equation in O(N) operations.
	  * @param A Matrix
	  * @param f Array with solution of A*x=f
	  * @return x Array x = A**(-1) * f
	  */
	public static double[] gaussEliminationTridiagonal(double[][] A, double[] f)
	{
		int i, j, k, piv;
		int dim = f.length;
		int dimM1 = dim - 1;
		double[]   r = new double[dim];
		double[][] B = new double[dim][dim+1];
		double a, pivotElement;

		for (i = 0; i < dim; i++)
		{
			for (j = 0; j < dim; j++)
				B[i][j] = A[i][j];
			B[i][dim] = f[i];
		}

		// loop over B for isolating B[dim-1][dim-1]
		for (piv = 0; piv < dimM1; piv++)
		{
			k = piv+1;
			pivotElement = B[piv][piv];

		// normalization of pivot row
			B[piv][k]   /= pivotElement;
			B[piv][dim] /= pivotElement;

			a = B[k][piv];

		// elimination
			B[k][k]   -= a * B[piv][k];
			B[k][dim] -= a * B[piv][dim];
		}

		// normalize last row
		B[dimM1][dim] /= B[dimM1][dimM1] ;

		// loop over B for isolating all B[i][i] elements
		for (piv = dimM1; piv > 0; piv--)
		{
			k = piv - 1;
		// elimination
			B[k][dim] -= B[k][piv] * B[piv][dim];
		}

	for (i = 0; i < dim; i++)
			r[i] = B[i][dim];
			
		return r;
	}

	/** reports a scalar and a vector to the output stream, where the
	  * entries are separated by the given delimiter
	  * @param state a vector
	  * @param delimiter a delimiter
	  */
	public static void print(double[] state, String delimiter)
	{
		int i;
		int n = state.length;
		String stateString = "";
		for (i = 0; i < n; i++)
			stateString += String.valueOf(state[i]) + delimiter;
		System.out.println(stateString);
		return;
	}

	/** reports a scalar and a vector to the output stream, where the
	  * entries are separated by the given delimiter
	  * @param time a scalar
	  * @param state a vector
	  * @param delimiter a delimiter
	  */
	public static void print(double time, double[] state, String delimiter)
	{
		int i;
		int n = state.length;
		String stateString = "";
		for (i = 0; i < n; i++)
			stateString += String.valueOf(state[i]) + delimiter;
		System.out.println(delimiter + time + stateString);
		return;
	}

	/** reports a scalar and a vector to the output stream
	  * @param time A scalar
	  * @param state A vector
	  */
	public static void print(double time, double[] state)
	{
		int i;
		int n = state.length;
		String stateString = "";
		for (i = 0; i < n; i++)
			stateString += "; " + String.valueOf(state[i]);
		System.out.println("t, u ; " + time + stateString);
		return;
	}
}







