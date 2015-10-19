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


/** Compares two ordinary literals by the
 *  <a href="{@docRoot}/resources/glossary.html#shallow_weight">shallow weights</a>
 *  of their atoms; if the weights are equal, numeric ids of the predicates are
 *  compared; if they coincide too, polarities are compared: negative polarity
 *  is greater;
 *  this ordering is stable wrt
 *  <a href="{@docRoot}/resources/glossary.html#gamma_substitution">gamma-substitution.</a>.
 */
public class LitOrdByShallowWeight implements LiteralOrdering {
       
    /** Will compare literals by the
     *  <a href="{@docRoot}/resources/glossary.html#shallow_weight">shallow weights</a>
     *  of their atoms, where <code>maxDepth</code> specifies the depth limit
     *  for the computation of shallow weights.
     */
    public LitOrdByShallowWeight(int maxDepth) {
	_shallowWeightComputation = new ShallowWeightComputation(maxDepth);
    }

    /** <b>pre:</b> <code>!lit1.isGeneral() && !lit2.isGeneral()</code> */
    public
	final
	int 
	compare(Literal lit1,Literal lit2) {

	WeightPolynomial w1 = _shallowWeightComputation.computeWeight(lit1.atom());
	WeightPolynomial w2 = _shallowWeightComputation.computeWeight(lit1.atom());

	int numericIdDiff;

	switch (w1.compare(w2))
	    {
		
	    case FunctionComparisonValue.AlwaysSmaller: return ComparisonValue.Smaller;

	    case FunctionComparisonValue.CanBeSmallerOrEquivalent: 
		// Compare the predicate priorities:
		numericIdDiff = 
		    ((AtomicFormula)lit1.atom()).predicate().numericId() - 
		    ((AtomicFormula)lit2.atom()).predicate().numericId();
		if (numericIdDiff < 0) return ComparisonValue.Smaller;
		if (numericIdDiff > 0) return ComparisonValue.Incomparable;
		
		// Compare the polarities
		if (lit1.isPositive() && lit2.isNegative())
		    return ComparisonValue.Smaller;

		return ComparisonValue.Incomparable;

	    case FunctionComparisonValue.AlwaysEquivalent: 
		// Compare the predicate priorities:
		numericIdDiff = 
		    ((AtomicFormula)lit1.atom()).predicate().numericId() - 
		    ((AtomicFormula)lit2.atom()).predicate().numericId();
		if (numericIdDiff < 0) return ComparisonValue.Smaller;
		if (numericIdDiff > 0) return ComparisonValue.Greater;

		// Compare the polarities
		if (lit1.isPositive() != lit2.isPositive())
		    if (lit1.isPositive())
			{
			    return ComparisonValue.Smaller;
			}
		    else
			return ComparisonValue.Greater;

		return ComparisonValue.Equivalent;

	    case FunctionComparisonValue.CanBeGreaterOrEquivalent: 
		// Compare the predicate priorities:
		numericIdDiff = 
		    ((AtomicFormula)lit1.atom()).predicate().numericId() - 
		    ((AtomicFormula)lit2.atom()).predicate().numericId();
		if (numericIdDiff > 0) return ComparisonValue.Greater;
		if (numericIdDiff < 0) return ComparisonValue.Incomparable;
		
		// Compare the polarities
		if (lit1.isNegative() && lit2.isPositive())
		    return ComparisonValue.Greater;

		return ComparisonValue.Incomparable;

	    
	    case FunctionComparisonValue.AlwaysGreater: 
		return ComparisonValue.Greater;
	    
	    case FunctionComparisonValue.Volatile: return ComparisonValue.Incomparable;

	    }; // switch (w1.compare(w2))

	
	assert false;

	return -1000;

    } // compare(Literal lit1,Literal lit2)


    
    /** <b>pre:</b> <code>!lit1.isGeneral() && !lit2.isGeneral()</code> */
    public 
	final
	int 
	compare(FlattermLiteral lit1,FlattermLiteral lit2) {
		
		
	WeightPolynomial w1 = _shallowWeightComputation.computeWeight(lit1.atom());
	WeightPolynomial w2 = _shallowWeightComputation.computeWeight(lit1.atom());

	int numericIdDiff;

	switch (w1.compare(w2))
	    {
		
	    case FunctionComparisonValue.AlwaysSmaller: return ComparisonValue.Smaller;

	    case FunctionComparisonValue.CanBeSmallerOrEquivalent: 
		// Compare the predicate priorities:
		numericIdDiff = 
		    lit1.atom().predicate().numericId() - 
		    lit2.atom().predicate().numericId();
		if (numericIdDiff < 0) return ComparisonValue.Smaller;
		if (numericIdDiff > 0) return ComparisonValue.Incomparable;
		
		// Compare the polarities
		if (lit1.isPositive() && lit2.isNegative())
		    return ComparisonValue.Smaller;

		return ComparisonValue.Incomparable;

	    case FunctionComparisonValue.AlwaysEquivalent: 
		// Compare the predicate priorities:
		numericIdDiff = 
		    lit1.atom().predicate().numericId() - 
		    lit2.atom().predicate().numericId();
		if (numericIdDiff < 0) return ComparisonValue.Smaller;
		if (numericIdDiff > 0) return ComparisonValue.Greater;

		// Compare the polarities
		if (lit1.isPositive() != lit2.isPositive())
		    if (lit1.isPositive())
			{
			    return ComparisonValue.Smaller;
			}
		    else
			return ComparisonValue.Greater;

		return ComparisonValue.Equivalent;

	    case FunctionComparisonValue.CanBeGreaterOrEquivalent: 
		// Compare the predicate priorities:
		numericIdDiff = 
		    lit1.atom().predicate().numericId() - 
		    lit2.atom().predicate().numericId();
		if (numericIdDiff > 0) return ComparisonValue.Greater;
		if (numericIdDiff < 0) return ComparisonValue.Incomparable;
		
		// Compare the polarities
		if (lit1.isNegative() && lit2.isPositive())
		    return ComparisonValue.Greater;

		return ComparisonValue.Incomparable;

	    
	    case FunctionComparisonValue.AlwaysGreater: 
		return ComparisonValue.Greater;
	    
	    case FunctionComparisonValue.Volatile: return ComparisonValue.Incomparable;

	    }; // switch (w1.compare(w2))

	
	assert false;

	return -1000;

    } // compare(FlattermLiteral lit1,FlattermLiteral lit2)



    private final ShallowWeightComputation _shallowWeightComputation;

} // class LitOrdByShallowWeight