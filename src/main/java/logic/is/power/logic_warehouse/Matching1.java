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

/** Static methods for term matching. The current implementation fails
 *  to match terms containing abstraction terms or quantified formulas.
 */
public class Matching1 {


    /** Similar to <code>Unification.unify(term1,term2,subst)</code> 
     *  except that only variables from <code>term1</code> are allowed
     *  to get instantiated; only instance1() values are affected,
     *  ie, only the global substitution 1 is changed. 
     *  <b>pre:</b> neither <code>term1</code>, nor <code>term2</code> 
     *  contains abstractions or quantifiers; they are either both formulas,
     *  or both individual-valued; they also do not contain common variables;
     *  the current implementation fails
     *  to match terms containing abstraction terms or quantified formulas.
     *  <b>VERY IMPORTANT:>/b> the terms are considered modulo the current
     *             global substitution 1, ie, if the unification process meets
     *             an instantiated variable, it deepens into the instance. 
     */
    public 
	static 
	boolean match(Flatterm term1,Flatterm term2,Substitution1 subst) {

	int unifierSavepoint = subst.savepoint();
	
	if (tryToMatch(term1,term2,subst,unifierSavepoint)) return true;
	
	// Undo the changes to subst:
	subst.backtrackTo(unifierSavepoint);
	
	return false;

    } // match(Flatterm term1,Flatterm term2,Substitution1 subst)



    //                 Private methods:



    /** Tries to unify the terms so that only variables from <code>term1</code>
     *  get instantiated; increments <code>subst</code> with new 
     *  instantiation; does not cancel the instantiations even if it fails.
     */
    private static boolean tryToMatch(Flatterm term1,
				      Flatterm term2,
				      Substitution1 subst,
				      int substSavepoint) {
	
	assert term1.isFormula() == term2.isFormula();

	switch (term1.kind())
	    {
	    case Term.Kind.Variable: 
	    
		if (term1.variable().isInstantiated1()) 
		    {
			if (subst.
			    variableWasInstantiatedAfter(term1.variable(),
							 substSavepoint)) 
			    {
				// The variable was instantiated within the current
				// (whole) matching attempt. 
				return 
				    FlattermInstance.equals(term1.variable().instance1(),
							    term2);
			    }
			else
			    {
				// The variable was instantiated before the current
				// (whole) matching attempt. 
				return tryToMatch(term1.variable().instance1(),
						  term2,
						  subst,
						  substSavepoint);
			    }

		    }
		else // uninstantiated variable
		    {
			assert !FlattermInstance.contains(term2,term1.variable());
			subst.instantiate(term1.variable(),term2);
			return true;
		    }

	    case Term.Kind.CompoundTerm:
		{
	  
		    if (term2.isVariable()) 
			{
			    return term2.variable().isInstantiated1() &&
				tryToMatch(term1,
					   term2.variable().instance1(),
					   subst,
					   substSavepoint);
			};

		    if (!term2.isCompound() || 
			!term1.function().equals(term2.function()))
			return false;	    
   
		    Flatterm arg1 = term1.nextCell();
		    Flatterm arg2 = term2.nextCell();
		    for (int n = 0; n < term1.function().arity(); ++n)
			{
			    if (!tryToMatch(arg1,arg2,subst,substSavepoint))
				return false;
			    arg1 = arg1.after();
			    arg2 = arg2.after();
			};
		    return true;
		}

	    case Term.Kind.IndividualConstant:

		if (term2.isVariable()) 
		    {
			return term2.variable().isInstantiated1() &&
			    tryToMatch(term1,
				       term2.variable().instance1(),
				       subst,
				       substSavepoint);
		    };
	  
		return term2.isIndividualConstant() &&
		    term1.individualConstant().equals(term2.individualConstant());


	    case Term.Kind.AtomicFormula: 
		{
		    if (!term2.isAtomicFormula() || 
			!term1.predicate().equals(term2.predicate()))
			return true;
	  
		    Flatterm arg1 = term1.nextCell();
		    Flatterm arg2 = term2.nextCell();
		    for (int n = 0; n < term1.predicate().arity(); ++n)
			{
			    if (!tryToMatch(arg1,arg2,subst,substSavepoint)) 
				return false;
			    arg1 = arg1.after();
			    arg2 = arg2.after();
			};
		    return true;
		}


	    case Term.Kind.ConnectiveApplication:
		{
		    if (!term2.isConnectiveApplication() || 
			!term1.connective().equals(term2.connective()))
			return true;
	  
		    Flatterm arg1 = term1.nextCell();
		    Flatterm arg2 = term2.nextCell();
		    for (int n = 0; n < term1.connective().arity(); ++n)
			{
			    if (!tryToMatch(arg1,arg2,subst,substSavepoint)) 
				return false;
			    arg1 = arg1.after();
			    arg2 = arg2.after();
			};
		    return true;
		}
	  
	    case Term.Kind.QuantifierApplication: // as below
	    case Term.Kind.AbstractionTerm:
		// Current implementation does not try to match 
		// abstraction terms and quantified formulas.
		return false; 

	    }; // switch (term1.kind())
    
	assert false;
	return false;
    
    } // match(Flatterm term1,Flatterm term2,..)



} // class Matching1

    