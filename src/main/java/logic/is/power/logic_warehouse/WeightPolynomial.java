/* Copyright (C) 2010 Alexandre Riazanov (Alexander Ryazanov)
 *
 * The copyright owner licenses this file to You under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package logic.is.power.logic_warehouse;

import java.util.*;


/** Representation of (linear) weight polynomials;
 *  for terminology, see Section 9.2.1 in 
 *  <a href="{@docRoot}/resources/references.html#Riazanov_PhD_thesis">[PhD thesis]</a>.
 */
public class WeightPolynomial {

    /** Constructs polynomial with all coefficients and the constant
     *  part equal to 0.
     */
    public WeightPolynomial() {
	_constantPart = 0; 
	_monomials = new HashMap<Variable,Integer>();
    }

    public final int constantPart() { return _constantPart; }

    /** Coefficient associated with the specified variable. */
    public final int coefficient(Variable var) { 
	Integer result = _monomials.get(var);
	if (result == null) return 0;
	return result.intValue();
    }

    /** Adds <code>coeff</code> to the coefficient of the variable;
     *  <b>pre:</b> <code>coeff > 0</code>.
     */
    public final void add(Variable var,int coeff) {
	assert coeff > 0;
	Integer coefficient = _monomials.get(var);
	if (coefficient == null)
	    {
		coefficient = new Integer(coeff);
	    }
	else
	    {
		coefficient += coeff;
	    }
	_monomials.put(var,coefficient);
    } 

    /** Same as <code>add(var,1)</code>. */
    public final void add(Variable var) { add(var,1); } 

    /** Adds <code>constant</code> to the constant part of the polynomial;
     *  <b>pre:</b> <code>constant > 0</code>.
     */
    public final void add(int constant) {  
	_constantPart += constant;
    } 
    
    /** Comparison of the polynomials.
     *  <code>compare(w) == AlwaysGreater</code> means that after 
     *  any instantiation of variables with positive integers,
     *  the value computed by <code>this</code> is greater than 
     *  the value computed by <code>w</code>.      
     *  For more details, see Section 9.2.1 in 
     *  <a href="{@docRoot}/resources/references.html#Riazanov_PhD_thesis">[PhD thesis]</a>.
     */
    public int compare(WeightPolynomial w) {
	
	// lft will accumulate the value of positive monomials
        // from (this - w), evaluated under the assumption 
	// that all variables are instantiated with 1.
	int lft = 0; 

	// rht will accumulate the value of positive monomials
        // from (w - this), evaluated under the assumption 
	// that all variables are instantiated with 1.
	int rht = 0;

	for (Map.Entry<Variable,Integer> monomial : _monomials.entrySet())
	    {
		int diff = 
		    monomial.getValue() - w.coefficient(monomial.getKey());

		if (diff > 0)
		    {
			lft += diff;
		    }
		else
		    rht -= diff;

		if (lft != 0 && rht != 0) 
		    return FunctionComparisonValue.Volatile;

	    }; // for (Map.Entry<Variable,Integer> monomial : _monomials.entrySet())

	// There may be some variables in w, not contained in this: 
	
	for (Map.Entry<Variable,Integer> monomial : w._monomials.entrySet())
	    if (!_monomials.containsKey(monomial.getKey()))
		{
		    if (lft != 0) return FunctionComparisonValue.Volatile;
		    rht += monomial.getValue();
		};

	if (lft != 0)
	    {
		assert rht == 0;
		int constDiff = w._constantPart - _constantPart;
		if (lft > constDiff) 
		    return FunctionComparisonValue.AlwaysGreater;
		if (lft == constDiff) 
		    return FunctionComparisonValue.CanBeGreaterOrEquivalent;
		return FunctionComparisonValue.Volatile;
	    }
	else if (rht != 0)
	    {
		int constDiff = _constantPart - w._constantPart;
		if (rht > constDiff) 
		    return FunctionComparisonValue.AlwaysSmaller;
		if (rht == constDiff) 
		    return FunctionComparisonValue.CanBeSmallerOrEquivalent;
		return FunctionComparisonValue.Volatile;
	    }
	else if (_constantPart > w._constantPart)
	    {
		return FunctionComparisonValue.AlwaysGreater;
	    }
	else if (_constantPart < w._constantPart)
	    {
		return FunctionComparisonValue.AlwaysSmaller;
	    }
	else 
	    return FunctionComparisonValue.AlwaysEquivalent;

    } // compare(WeightPolynomial w)


    public String toString() {
	String result = "";
	boolean someMonomialPrinted = false;
	for (Map.Entry<Variable,Integer> monomial : _monomials.entrySet())
	    {
		if (someMonomialPrinted)
		    {
			result += " + ";
		    };
		someMonomialPrinted = true;

		result += monomial.getValue() + "*" + monomial.getKey();
	    };
	if (someMonomialPrinted)
	    {
		result += " + ";
	    };
	return result += "" + _constantPart;
    } // toString() 




    private int _constantPart;

    private HashMap<Variable,Integer> _monomials;

} // class WeightPolynomial