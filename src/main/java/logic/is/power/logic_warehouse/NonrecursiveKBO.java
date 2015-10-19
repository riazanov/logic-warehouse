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


/** Simple, yet efficient, reduction ordering;
 *  for details, see Section 3.1.5 in 
 *  <a href="{@docRoot}/resources/references.html#Riazanov_PhD_thesis">[PhD thesis]</a>.
 */
public class NonrecursiveKBO 
extends ReductionOrdering {

    public NonrecursiveKBO() {
    }

    public final int compare(Term term1,Term term2) {

	WeightPolynomial w1 = WeightComputation.computeWeight(term1);
	WeightPolynomial w2 = WeightComputation.computeWeight(term2);


	int lexCmp;

	switch (w1.compare(w2))
	    {
		
	    case FunctionComparisonValue.AlwaysSmaller: return ComparisonValue.Smaller;

	    case FunctionComparisonValue.CanBeSmallerOrEquivalent: 
		lexCmp = compareLexicographically(term1,term2);
		if (lexCmp == ComparisonValue.Smaller) 
		    return ComparisonValue.Smaller;
		return ComparisonValue.Incomparable;
	    
	    case FunctionComparisonValue.AlwaysEquivalent: 
		return compareLexicographically(term1,term2);

	    case FunctionComparisonValue.CanBeGreaterOrEquivalent: 
		lexCmp = compareLexicographically(term1,term2);
		if (lexCmp == ComparisonValue.Greater) 
		    return ComparisonValue.Greater;
		return ComparisonValue.Incomparable;
	    
	    case FunctionComparisonValue.AlwaysGreater: 
		return ComparisonValue.Greater;
	    
	    case FunctionComparisonValue.Volatile: return ComparisonValue.Incomparable;

	    }; // switch (w1.compare(w2))

	
	assert false;

	return -1000;

    } // compare(Term term1,Term term2) 




    public final int compare(Flatterm term1,Flatterm term2) {

	WeightPolynomial w1 = WeightComputation.computeWeight(term1);
	WeightPolynomial w2 = WeightComputation.computeWeight(term2);


	int lexCmp;

	switch (w1.compare(w2))
	    {
		
	    case FunctionComparisonValue.AlwaysSmaller: return ComparisonValue.Smaller;

	    case FunctionComparisonValue.CanBeSmallerOrEquivalent: 
		lexCmp = compareLexicographically(term1,term2);
		if (lexCmp == ComparisonValue.Smaller) 
		    return ComparisonValue.Smaller;
		return ComparisonValue.Incomparable;
	    
	    case FunctionComparisonValue.AlwaysEquivalent: 
		return compareLexicographically(term1,term2);

	    case FunctionComparisonValue.CanBeGreaterOrEquivalent: 
		lexCmp = compareLexicographically(term1,term2);
		if (lexCmp == ComparisonValue.Greater) 
		    return ComparisonValue.Greater;
		return ComparisonValue.Incomparable;
	    
	    case FunctionComparisonValue.AlwaysGreater: 
		return ComparisonValue.Greater;
	    
	    case FunctionComparisonValue.Volatile: return ComparisonValue.Incomparable;

	    }; // switch (w1.compare(w2))

	
	assert false;

	return -1000;

    } // compare(Flatterm term1,Flatterm term2)


    /** Compares the instances of the terms modulo global substitution 2. */
    public
	final int compareModuloSubst2(Term term1,Term term2) {


	WeightPolynomial w1 = WeightComputation.computeWeightModuloSubst2(term1);
	WeightPolynomial w2 = WeightComputation.computeWeightModuloSubst2(term2);

	int lexCmp;

	switch (w1.compare(w2))
	    {
		
	    case FunctionComparisonValue.AlwaysSmaller: return ComparisonValue.Smaller;

	    case FunctionComparisonValue.CanBeSmallerOrEquivalent: 
		lexCmp = compareLexicographicallyModuloSubst2(term1,term2);
		if (lexCmp == ComparisonValue.Smaller) 
		    return ComparisonValue.Smaller;
		return ComparisonValue.Incomparable;
	    
	    case FunctionComparisonValue.AlwaysEquivalent: 
		return compareLexicographicallyModuloSubst2(term1,term2);

	    case FunctionComparisonValue.CanBeGreaterOrEquivalent: 
		lexCmp = compareLexicographicallyModuloSubst2(term1,term2);
		if (lexCmp == ComparisonValue.Greater) 
		    return ComparisonValue.Greater;
		return ComparisonValue.Incomparable;
	    
	    case FunctionComparisonValue.AlwaysGreater: 
		return ComparisonValue.Greater;
	    
	    case FunctionComparisonValue.Volatile: return ComparisonValue.Incomparable;

	    }; // switch (w1.compare(w2))

	
	assert false;

	return -1000;

    } // compareModuloSubst2(Term term1,Term term2)





    /** Compares the instances of the terms modulo global substitution 3. */
    public
	final int compareModuloSubst3(Term term1,Term term2) {


	WeightPolynomial w1 = WeightComputation.computeWeightModuloSubst3(term1);
	WeightPolynomial w2 = WeightComputation.computeWeightModuloSubst3(term2);

	int lexCmp;

	switch (w1.compare(w2))
	    {
		
	    case FunctionComparisonValue.AlwaysSmaller: return ComparisonValue.Smaller;

	    case FunctionComparisonValue.CanBeSmallerOrEquivalent:
		lexCmp = compareLexicographicallyModuloSubst3(term1,term2);
		if (lexCmp == ComparisonValue.Smaller) 
		    return ComparisonValue.Smaller;
		return ComparisonValue.Incomparable;
	    
	    case FunctionComparisonValue.AlwaysEquivalent: 
		return compareLexicographicallyModuloSubst3(term1,term2);

	    case FunctionComparisonValue.CanBeGreaterOrEquivalent: 
		lexCmp = compareLexicographicallyModuloSubst3(term1,term2);
		if (lexCmp == ComparisonValue.Greater) 
		    return ComparisonValue.Greater;
		return ComparisonValue.Incomparable;
	    
	    case FunctionComparisonValue.AlwaysGreater: 
		return ComparisonValue.Greater;
	    
	    case FunctionComparisonValue.Volatile: return ComparisonValue.Incomparable;

	    }; // switch (w1.compare(w2))

	
	assert false;

	return -1000;

    } // compareModuloSubst3(Term term1,Term term2)







    /** Checks if the instance of <code>term1</code> wrt global substitution 2,
     *  is greater than the instance of <code>term2</code>.
     */
    public final boolean greaterModuloSubst2(Term term1,Term term2) {

	// Can be optimised.
	
	return compareModuloSubst2(term1,term2) == ComparisonValue.Greater;
    }

    
    /** Checks if the instance of <code>term1</code> wrt global substitution 3,
     *  is greater than the instance of <code>term2</code>.
     */
    public final boolean greaterModuloSubst3(Term term1,Term term2) {

	// Can be optimised.
	
	return compareModuloSubst3(term1,term2) == ComparisonValue.Greater;
    }
    

    /** Checks if there may be a substitution instantiating only variables 
     *  from <code>term1</code>, that would make <code>term1</code> greater 
     *  than <code>term2</code>;
     *  false positives are possible, false negatives are not.
     */
    public final boolean canBeGreaterModuloSubst(Term term1,Term term2) {
	
	// Can be optimised.

	if (term1.isVariable())
	    return !term2.containsFreeVariables();

	// Check that all variables from term2 are present in term1:

	TreeSet<Variable> term2Variables = 
	    new TreeSet<Variable>();
	for (Variable var : term2Variables)
	    if (!term1.containsVariableAsProperSubterm(var))
		return false;
	
	int cmp = compare(term1,term2);

	return cmp != ComparisonValue.Smaller &&
	    cmp != ComparisonValue.Equivalent;

    } // canBeGreaterModuloSubst(Term term1,Term term2)



    /** Lexicographic comparison. */
    private 
	int 
	compareLexicographically(Term term1,Term term2) {

	Term.LeanIterator iter1 = new Term.LeanIterator(term1);
	Term.LeanIterator iter2 = new Term.LeanIterator(term2);

	while (iter1.hasNext())
	    {
		assert iter2.hasNext();

		Term subterm1 = iter1.next();
		Term subterm2 = iter2.next();
		
		if (subterm1.isVariable())
		    {
			if (subterm1 != subterm2) 
			    return ComparisonValue.Incomparable;
		    }
		else if (subterm2.isVariable())
		    {
			return ComparisonValue.Incomparable;
		    }
		else if (subterm1.isAbstraction())
		    {
			if (!subterm2.isAbstraction() ||
			    ((AbstractionTerm)subterm1).variable() !=
			    ((AbstractionTerm)subterm2).variable())
			return ComparisonValue.Incomparable;
		    }
		else if (subterm2.isAbstraction())
		    {
			return ComparisonValue.Incomparable;
		    }
		else if (!subterm1.topSymbol().equals(subterm2.topSymbol()))
		    {
			return
			    compareDifferentNonvariableSymbols(subterm1.topSymbol(),
							       subterm2.topSymbol());
		    };
		
	    }; // while (iter1.hasNext())

	return ComparisonValue.Equivalent;

    } // compareLexicographically(Term term1,Term term2)


    /** Lexicographic comparison. */
    private 
	int 
	compareLexicographically(Flatterm term1,Flatterm term2) {

	Flatterm subterm1 = term1;
	Flatterm subterm2 = term2;
	
	do
	    {
		assert subterm2 != term2.after();

		
		if (subterm1.isVariable())
		    {
			if (!subterm2.isVariable() ||
			    subterm1.variable() != subterm2.variable()) 
			    return ComparisonValue.Incomparable;
		    }
		else if (subterm2.isVariable())
		    {
			return ComparisonValue.Incomparable;
		    }
		else if (subterm1.isAbstraction())
		    {
			if (!subterm2.isAbstraction() ||
			    subterm1.variable() !=
			    subterm2.variable())
			return ComparisonValue.Incomparable;
		    }
		else if (subterm2.isAbstraction())
		    {
			return ComparisonValue.Incomparable;
		    }
		else if (!subterm1.symbol().equals(subterm2.symbol()))
		    {
			return
			    compareDifferentNonvariableSymbols(subterm1.symbol(),
							       subterm2.symbol());
		    };
		

		subterm1 = subterm1.nextCell();
		subterm2 = subterm2.nextCell();
	    }
	while (subterm1 != term1.after());


	return ComparisonValue.Equivalent;
	

    } // compareLexicographically(Flatterm term1,Flatterm term2)


    /** Lexicographic comparison of the instances of the terms
     *  wrt global substitution 2.
     */
    private 
	int 
	compareLexicographicallyModuloSubst2(Term term1,Term term2) {


	Term.LeanIterator iter1 = new Term.LeanIterator(term1);
	Term.LeanIterator iter2 = new Term.LeanIterator(term2);


	while (iter1.hasNext())
	    {
		assert iter2.hasNext();

		Term subterm1 = iter1.next();
		Term subterm2 = iter2.next();

		if (subterm1.isVariable())
		    {
			if (subterm1 != subterm2) 
			    if (((Variable)subterm1).isInstantiated2())
				{

				    int cmp =
					compareLexicographicallyModuloSubst2(subterm2,
									     ((Variable)subterm1).
									     instance2());
				    if (cmp != ComparisonValue.Equivalent)
					return ComparisonValue.flip(cmp);

				    iter2.skipSubtermRemainder();
				}
			    else if (subterm2.isVariable() &&
				     ((Variable)subterm2).isInstantiated2())
				{
				    int cmp =
					compareLexicographicallyModuloSubst2(subterm1,
									     ((Variable)subterm2).
									     instance2());
				    if (cmp != ComparisonValue.Equivalent)
					return cmp;
				}
			    else
				return ComparisonValue.Incomparable;
		    }
		else if (subterm2.isVariable())
		    {
			if (((Variable)subterm2).isInstantiated2())
			    {
				int cmp =
				    compareLexicographicallyModuloSubst2(subterm1,
									 ((Variable)subterm2).
									 instance2());
				    if (cmp != ComparisonValue.Equivalent)
					return cmp;

				    iter1.skipSubtermRemainder();
			    }
			else
			    return ComparisonValue.Incomparable;
		    }
		else if (subterm1.isAbstraction())
		    {
			if (!subterm2.isAbstraction() ||
			    ((AbstractionTerm)subterm1).variable() !=
			    ((AbstractionTerm)subterm2).variable())
			return ComparisonValue.Incomparable;
		    }
		else if (subterm2.isAbstraction())
		    {
			return ComparisonValue.Incomparable;
		    }
		else if (!subterm1.topSymbol().equals(subterm2.topSymbol()))
		    {
			return
			    compareDifferentNonvariableSymbols(subterm1.topSymbol(),
							       subterm2.topSymbol());
		    };
		
	    }; // while (iter1.hasNext())

	
	assert !iter2.hasNext();

	return ComparisonValue.Equivalent;


    } // compareLexicographicallyModuloSubst2(Term term1,Term term2)







    /** Lexicographic comparison of the instance of <code>term1</code>
     *  wrt global substitution 2, with <code>term2</code>.
     */
    private 
	int 
	compareLexicographicallyModuloSubst2(Term term1,Flatterm term2) {

	Term.LeanIterator iter1 = new Term.LeanIterator(term1);
	Flatterm subterm2 = term2;

	while (iter1.hasNext())
	    {
		assert subterm2 != term2.after();

		Term subterm1 = iter1.next();
	
		if (subterm1.isVariable())
		    {
			if (((Variable)subterm1).isInstantiated2())
			    {
				int cmp =
				    compareLexicographically(((Variable)subterm1).
							     instance2(),
							     subterm2);
				
				if (cmp != ComparisonValue.Equivalent)
				    return cmp;
				subterm2 = subterm2.after();
			    }
			else
			    {
				if (!subterm2.isVariable() ||
				    subterm1 != subterm2.variable())
				    return ComparisonValue.Incomparable;
				subterm2 = subterm2.nextCell();
			    };
		    }
		else if (subterm2.isVariable())
		    {
			return ComparisonValue.Incomparable;
		    }
		else if (subterm1.isAbstraction())
		    {
			if (!subterm2.isAbstraction() ||
			    ((AbstractionTerm)subterm1).variable() !=
			    subterm2.variable())
			return ComparisonValue.Incomparable;
			subterm2 = subterm2.nextCell();
		    }
		else if (subterm2.isAbstraction())
		    {
			return ComparisonValue.Incomparable;
		    }
		else if (!subterm1.topSymbol().equals(subterm2.symbol()))
		    {
			return
			    compareDifferentNonvariableSymbols(subterm1.topSymbol(),
							       subterm2.symbol());
		    }
		else
		    subterm2 = subterm2.nextCell();

	    }; // while (iter1.hasNext())

	return ComparisonValue.Equivalent;

    } // compareLexicographicallyModuloSubst2(Term term1,Flatterm term2) 





    /** Lexicographic comparison of the instances of the terms
     *  wrt global substitution 3.
     */
    private 
	int 
	compareLexicographicallyModuloSubst3(Term term1,Term term2) {

	Term.LeanIterator iter1 = new Term.LeanIterator(term1);
	Term.LeanIterator iter2 = new Term.LeanIterator(term2);


	while (iter1.hasNext())
	    {
		assert iter2.hasNext();

		Term subterm1 = iter1.next();
		Term subterm2 = iter2.next();

		if (subterm1.isVariable())
		    {
			if (subterm1 != subterm2) 
			    if (((Variable)subterm1).isInstantiated3())
				{

				    int cmp =
					compareLexModuloSubst3(subterm2,
							       ((Variable)subterm1).
							       instance3());
				    if (cmp != ComparisonValue.Equivalent)
					return ComparisonValue.flip(cmp);

				    iter2.skipSubtermRemainder();
				}
			    else if (subterm2.isVariable() &&
				     ((Variable)subterm2).isInstantiated3())
				{
				    int cmp =
					compareLexModuloSubst3(subterm1,
							       ((Variable)subterm2).
							       instance3());
				    if (cmp != ComparisonValue.Equivalent)
					return cmp;
				}
			    else
				return ComparisonValue.Incomparable;
		    }
		else if (subterm2.isVariable())
		    {
			if (((Variable)subterm2).isInstantiated3())
			    {
				int cmp =
				    compareLexModuloSubst3(subterm1,
							   ((Variable)subterm2).
							   instance3());
				    if (cmp != ComparisonValue.Equivalent)
					return cmp;

				    iter1.skipSubtermRemainder();
			    }
			else
			    return ComparisonValue.Incomparable;
		    }
		else if (subterm1.isAbstraction())
		    {
			if (!subterm2.isAbstraction() ||
			    ((AbstractionTerm)subterm1).variable() !=
			    ((AbstractionTerm)subterm2).variable())
			return ComparisonValue.Incomparable;
		    }
		else if (subterm2.isAbstraction())
		    {
			return ComparisonValue.Incomparable;
		    }
		else if (!subterm1.topSymbol().equals(subterm2.topSymbol()))
		    {
			return
			    compareDifferentNonvariableSymbols(subterm1.topSymbol(),
							       subterm2.topSymbol());
		    };
		
	    }; // while (iter1.hasNext())

	
	assert !iter2.hasNext();

	return ComparisonValue.Equivalent;


    } // compareLexicographicallyModuloSubst3(Term term1,Term term2)






    /** Lexicographic comparison of the instance of <code>term1</code>
     *  wrt global substitution 3, with <code>term2</code> to which 
     *  <em>no substitution is applied</em>.
     */
    private 
	int 
	compareLexModuloSubst3(Term term1,Term term2) {

	Term.LeanIterator iter1 = new Term.LeanIterator(term1);
	Term.LeanIterator iter2 = new Term.LeanIterator(term2);


	while (iter1.hasNext())
	    {
		assert iter2.hasNext();

		Term subterm1 = iter1.next();
		Term subterm2 = iter2.next();

		if (subterm1.isVariable())
		    {
			if (((Variable)subterm1).isInstantiated3())
			    {
				int cmp =
				    compareLexicographically(subterm2,
							     ((Variable)subterm1).
							     instance3());
				if (cmp != ComparisonValue.Equivalent)
				    return ComparisonValue.flip(cmp);
				
				iter2.skipSubtermRemainder();
			    }
			else if (subterm1 != subterm2)
			    return ComparisonValue.Incomparable;
		    }
		else if (subterm2.isVariable())
		    {
			return ComparisonValue.Incomparable;
		    }
		else if (subterm1.isAbstraction())
		    {
			if (!subterm2.isAbstraction() ||
			    ((AbstractionTerm)subterm1).variable() !=
			    ((AbstractionTerm)subterm2).variable())
			return ComparisonValue.Incomparable;
		    }
		else if (subterm2.isAbstraction())
		    {
			return ComparisonValue.Incomparable;
		    }
		else if (!subterm1.topSymbol().equals(subterm2.topSymbol()))
		    {
			return
			    compareDifferentNonvariableSymbols(subterm1.topSymbol(),
							       subterm2.topSymbol());
		    };
		
	    }; // while (iter1.hasNext())

	
	assert !iter2.hasNext();

	return ComparisonValue.Equivalent;


    } // compareLexModuloSubst3(Term term1,Term term2)








    private 
	int 
	compareDifferentNonvariableSymbols(Symbol sym1,Symbol sym2) {

	assert !sym1.equals(sym2);
	assert !sym1.isVariable();
	assert !sym2.isVariable();
	
	if (sym1.isConnective())
	    {
		if (sym2.isConnective())
		    {
			// Compare numeric ids:
			return Util.compare(sym1.numericId(),sym2.numericId());

		    }
		else if (sym2.isQuantifier())
		    {
			// Connectives are smaller than quantifiers:
			return ComparisonValue.Smaller;
		    }
		else
		    // Connectives are greater than signature symbols:
		    return ComparisonValue.Greater;
	    }
	else if (sym1.isQuantifier())
	    {
		if (sym2.isQuantifier())
		    {
			// Compare numeric ids:
			return Util.compare(sym1.numericId(),sym2.numericId());
		    }
		else 
		    // Quantifiers are greater than connectives and 
		    // signature symbols:
		    return ComparisonValue.Greater;	
	    }
	else if (sym2.isConnective() || sym2.isQuantifier())
	    {
		// Signature symbols are smaller than connectives
		// and quantifiers:
		return ComparisonValue.Smaller;
	    }
	else 
	    {
		// Both are signature symbols. 
		
		// Compare priorities first:

		int cmp = 
		    Util.compare(((SignatureSymbol)sym1).priority(),
				 ((SignatureSymbol)sym1).priority());

		if (cmp == ComparisonValue.Equivalent)
		    // compare numeric ids
		    return Util.compare(sym1.numericId(),sym2.numericId());
		
		return cmp;
		
	    } // if (sym1.isConnective())

    } // compareDifferentNonvariableSymbols(Symbol sym1,Symbol sym2)
    



} // class NonrecursiveKBO