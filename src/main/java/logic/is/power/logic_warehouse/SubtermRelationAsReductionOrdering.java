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


/** Simplest model of the abstraction 
 *  {@link logic.is.power.logic_warehouse.ReductionOrdering}:
 *  term <code>s</code> is smaller than term 
 *  <code>t</code> iff <code>s</code> is a subterm
 *  of <code>t</code>.
 */
public class SubtermRelationAsReductionOrdering 
extends ReductionOrdering {

    public SubtermRelationAsReductionOrdering() {
    }

    public final int compare(Term term1,Term term2) {
	if (term1.equals(term2)) return ComparisonValue.Equivalent;
	if (term1.containsAsProperSubterm(term2))
	    return ComparisonValue.Greater;
	if (term2.containsAsProperSubterm(term1))
	    return ComparisonValue.Smaller;
	return ComparisonValue.Incomparable;
    }


    public final int compare(Flatterm term1,Flatterm term2) {
	if (term1.wholeTermEquals(term2)) return ComparisonValue.Equivalent;
	if (term1.containsAsProperSubterm(term2))
	    return ComparisonValue.Greater;
	if (term2.containsAsProperSubterm(term1))
	    return ComparisonValue.Smaller;
	return ComparisonValue.Incomparable;
    }


    /** Compares the instances of the terms modulo global substitution 2. */
    public
	final int compareModuloSubst2(Term term1,Term term2) {
	if (term1.equalsModuloSubst2(term2)) return ComparisonValue.Equivalent;
	if (term1.containsAsProperSubtermModuloSubst2(term2))
	    return ComparisonValue.Greater;
	if (term2.containsAsProperSubtermModuloSubst2(term1))
	    return ComparisonValue.Smaller;
	return ComparisonValue.Incomparable;
    }


    /** Checks if the instance of <code>term1</code> wrt global substitution 2,
     *  is greater than the instance of <code>term2</code>.
     */
    public final boolean greaterModuloSubst2(Term term1,Term term2) {
	return term1.containsAsProperSubtermModuloSubst2(term2);
    }
    
    /** Checks if the instance of <code>term1</code> wrt global substitution 3,
     *  is greater than the instance of <code>term2</code>.
     */
    public final boolean greaterModuloSubst3(Term term1,Term term2) {
	return term1.containsAsProperSubtermModuloSubst3(term2);
    }
    


    /** Checks if there may be a substitution instantiating only variables 
     *  from <code>term1</code>, that would make <code>term1</code> greater 
     *  than <code>term2</code>;
     *  false positives are possible, false negatives are not.
     */
    public final boolean canBeGreaterModuloSubst(Term term1,Term term2) {

	if (term1.isVariable())
	    return !term2.containsFreeVariables();

	// Check that all variables from term2 are present in term1:

	TreeSet<Variable> term2Variables = 
	    new TreeSet<Variable>();
	for (Variable var : term2Variables)
	    if (!term1.containsVariableAsProperSubterm(var))
		return false;
	

	// Check that term2 is unifiable with a proper subterm
	// of term1:
	
	Term.LeanIterator iter = new Term.LeanIterator(term1);
	
	iter.next();

	while (iter.hasNext())
	    {
		if (Unification.possiblyUnify(iter.next(),term2))
		    return true;
	    };
	
	return false;
	
    } // canBeGreaterModuloSubst(Term term1,Term term2)


} // class SubtermRelationAsReductionOrdering