package genopt.algorithm.util.optimality;

import genopt.lang.OptimizerException;
import genopt.algorithm.Optimizer;
import genopt.algorithm.util.math.Point;

/** Class for checking the optimality condition of a point.<BR>
  * The optimality condition is checked by testing in each 
  * orthogonal direction of xL whether the function value increases or not.
  * 
  * <P><I>This project was carried out at:</I>
  * <UL><LI><A HREF="http://www.lbl.gov">
  * Lawrence Berkeley National Laboratory (LBNL)</A>,
  * <A HREF="http://simulationresearch.lbl.gov">
  * Simulation Research Group</A>,</UL></LI>
  * <I>and supported by</I><UL>
  * <LI>the <A HREF="http://www.energy.gov">
  * U.S. Department of Energy (DOE)</A>,
  * <LI>the <A HREF="http://www.satw.ch">
  * Swiss Academy of Engineering Sciences (SATW)</A>,
  * <LI>the Swiss National Energy Fund (NEFF), and
  * <LI>the <A HREF="http://www.snf.ch">
  * Swiss National Science Foundation (SNSF)</A></UL></LI><P>
  *
  * GenOpt Copyright (c) 1998-2010, The Regents of the University of
  * California, through Lawrence Berkeley National Laboratory (subject 
  * to receipt of any required approvals from the U.S. Dept. of Energy).  
  * All rights reserved.
  *
  * @author <A HREF="mailto:MWetter@lbl.gov">Michael Wetter</A>
  *
  * @version GenOpt(R) 3.0.3 (April 26, 2010)<P>
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

public class Perturber
{
	/** System dependent line separator */
    protected final static String LS = System.getProperty("line.separator");
	/** counter how many times an expansion can be done */
	private static final int COUEXPMAX = 5; // exp(5)=148, exp(6)=403
	
	/** constructor for checking the optimality condition 
	  * @param opt reference to the Optimizer object
	  */
	public Perturber(Optimizer opt)
	{
		o = opt;
		dim = o.getDimensionX();
	}

	/** checks the optimality condition in the transformed space
	  * by testing in each orthogonal direction of <CODE>xL</CODE> whether 
	  * the function increases or not.<BR>
	  * @param x point with lowest known objective function value<BR>
	  *        <B>Note:</B> The independent variable of the point
          *        has to be in the same mode as the Optimizer Object
	  *        is while calling this function
	  * @param stepFactor factor with which the step size is multiplied for the
	  *        test of the optimality condition
  	  * @exception OptimizerException
	  * @exception Exception
	  */
	public void perturb(Point x, 
		double stepFactor) throws OptimizerException, Exception
	{
		int couExp;
		o.println("Check optimality condition.");
		xC = (Point)x.clone();
		optimalPoint = true;
		String comTOC = new String("test optimality condition");
		String comESS = new String("expand step size in testing optimality condition");
		String com; // the comment that is actually used

		double s; // the step size
		int i = 0;
		do
		{
			xL = (Point)xC.clone();
			s = stepFactor * o.getDx(i, xL.getX(i));
			com = comTOC;
			couExp = 0;
			do
			{
				xL.setX(i, xC.getX(i) + Math.exp(couExp) * s);
				xL.setComment(com);
				xL = o.getF(xL);
				o.report(xL, Optimizer.SUBITERATION);
				if (++couExp > COUEXPMAX)
				{
					String varNam = o.getVariableNameContinuous(i);
					double xCO;
					if (o.getMode() == Optimizer.TRANSFORMED)
					{
					    xCO = genopt.db.ContinuousParameter.transformValue(x.getX(i), 
											       o.getL(i), 
											       o.getU(i), 
											       o.getKindOfConstraint(i), 
											       1);
					}
					else
						xCO = x.getX(i);
			
						String em = "Value 'Step' of parameter '" + varNam + 
							"' too small or objective function has a" + LS +
							"  nullspace in the positive direction at " +
							varNam + " = " + xCO + " ." + LS +
						"  Tried to expand " + couExp + " times in positive direction.";
					throw new OptimizerException(em);
				}
				com = comESS;				
			}while (xL.getF(0) == xC.getF(0)); // expand step if we are in a nullspace

			if (xL.getF(0) > xC.getF(0))
			{   // try other direction
				com = comTOC;
				xL.setX(i, xC.getX(i));
				couExp = 0;
				do
				{
					xL.setX(i, xC.getX(i) - Math.exp(couExp) * s);
					xL.setComment(com);
					xL = o.getF(xL);
					o.report(xL, Optimizer.SUBITERATION);
					if (++couExp > COUEXPMAX)
					{
						String varNam = o.getVariableNameContinuous(i);
						double xCO;
						if (o.getMode() == Optimizer.TRANSFORMED)
						{
						    xCO = genopt.db.ContinuousParameter.transformValue(x.getX(i), 
												       o.getL(i), 
												       o.getU(i), 
												       o.getKindOfConstraint(i), 
												       1);
						}
						else
							xCO = x.getX(i);
						
						String em = "Value 'Step' of parameter '" + varNam + 
							"' too small or objective function has a" + LS +
							"  nullspace in the negative direction at " +
							varNam + " = " + xCO + " ." + LS +
							"  Tried to expand " + couExp + " times in negative direction.";
						throw new OptimizerException(em);
					}					
					com = comESS;
				}while (xL.getF(0) == xC.getF(0)); // expand step if we are in a nullspace
			}
			if  (xL.getF(0) < xC.getF(0))
			{   // we got an improvement: xC is not local optimum
				optimalPoint = false;
				o.println("Found better point in optimality check.");
			}
			i++;
		} while (i < dim && optimalPoint);
		
		// set the fields of the optimal point equal to the one of the checked
		// point since it is the local minimum
		if (optimalPoint)	xL = (Point)xC.clone();
	}

	/** gets the point with the lowest function value
	  *@return the optimal point
	  */
	public Point getOptimalPoint()
	{
		return (Point)xL.clone();
	}

        /** checks whether we have an optimum point
	  *@return <CODE>true</CODE> if the checked point is a local minimum point,<BR>
	  *        <CODE>false</CODE> otherwise
	  */
	public boolean gotOptimum() { return optimalPoint;}

	/** point to be checked */
	protected Point xC;
	/** lowest point in the neighborhood of xC */
	protected Point xL;
	/** reference to Optimizer Object */
	private Optimizer o;
	/** dimension of the independent variable */
	protected int dim;
	/** flag: <CODE>true</CODE> if xC is the optimal point, <CODE>false</CODE> otherwise */
	protected boolean optimalPoint;
}







