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

/** Simple form of term matching; affects the global substitution 3,
 *  ie, instance3() values in variables. The current implementation fails
 *  to match terms containing abstraction terms or quantified formulas.
 */
public class Matching3 {

    /** Tries to augment the substitution so that the intance
     *  of <code>term1</code> becomes syntactically identical 
     *  to <code>term2</code>; instantiation is not treated as transitive,
     *  in particular, cyclic assignments are allowed;
     *  <code>term1<code> can be partially instantiated wrt  
     *  the global substitution 3; <code>term2<code> is treated as completely
     *  uninstantiated; the current implementation fails
     *  to match terms containing abstraction terms or quantified formulas.
     */
    public 
	static 
	boolean match(Term term1,Term term2,Substitution3 subst) {

	int unifierSavepoint = subst.savepoint();
	
	if (tryToMatch(term1,term2,subst)) 
	    {

		//System.out.println("MATCH " + term1 + "  VS " + term2 + "  SUBST= " + subst);

		return true;
	    };
	
	// Undo the changes to subst:
	subst.backtrackTo(unifierSavepoint);
	
	//System.out.println("MISMATCH " + term1 + "  VS " + term2);

	return false;

    } // match(Term term1,Term term2,Substitution3 subst)



    //                 Private methods:



    /** Tries to match the terms so that only variables from <code>term1</code>
     *  get instantiated; increments <code>subst</code> with new 
     *  instantiation; does not cancel the instantiations even if it fails.
     */
    private static boolean tryToMatch(Term term1,
				      Term term2,
				      Substitution3 subst) {

	assert term1.isFormula() == term2.isFormula();

	switch (term1.kind())
	    {
	    case Term.Kind.Variable:
		if (((Variable)term1).isInstantiated3())
		    {
			return 
			    ((Variable)term1).instance3().
			    equals(term2);
		    }
		else
		    {
			subst.instantiate((Variable)term1,term2);
			return true;
		    }
		
	    case Term.Kind.CompoundTerm:       
		return term2.kind() == Term.Kind.CompoundTerm &&
		    ((CompoundTerm)term1).function().
		    equals(((CompoundTerm)term2).function()) &&
		    tryToMatch(((CompoundTerm)term1).argument(),
			       ((CompoundTerm)term2).argument(),
			       subst);

	    case Term.Kind.IndividualConstant:
		return term2.isIndividualConstant() &&
		    ((IndividualConstant)term1).
		    equals((IndividualConstant)term2);
		
		
	    case Term.Kind.AtomicFormula:    
		return term2.kind() == Term.Kind.AtomicFormula &&
		    ((AtomicFormula)term1).predicate().
		    equals(((AtomicFormula)term2).predicate()) &&
		    (((AtomicFormula)term1).argument() == null ||
		     tryToMatch(((AtomicFormula)term1).argument(),
				((AtomicFormula)term2).argument(),
				subst));
		    
		
	    case Term.Kind.ConnectiveApplication:
		return term2.kind() == Term.Kind.ConnectiveApplication &&
		    ((ConnectiveApplication)term1).connective().
		    equals(((ConnectiveApplication)term2).connective()) &&
		    tryToMatch(((ConnectiveApplication)term1).argument(),
			       ((ConnectiveApplication)term2).argument(),
			       subst);
		
	    case Term.Kind.QuantifierApplication: // as below
	    case Term.Kind.AbstractionTerm:
		// Current implementation does not try to match 
		// abstraction terms and quantified formulas.
		return false; 


	    case Term.Kind.TermPair:
		assert term2.kind() == Term.Kind.TermPair;
		return  
		    tryToMatch(((TermPair)term1).first(),
			       ((TermPair)term2).first(),
			       subst) &&
		    tryToMatch(((TermPair)term1).second(),
			       ((TermPair)term2).second(),
			       subst);
		    

	  }; // switch (term1.kind())
	
	assert false;
	return false;

    } // match(Flatterm term1,Flatterm term2,..)



} // class Matching3

    