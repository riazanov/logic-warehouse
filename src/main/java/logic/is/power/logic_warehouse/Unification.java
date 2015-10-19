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


/** Static methods for unifying terms. */
public class Unification {

    
    /** Tries to unify the terms; if succeeds, makes a savepoint in 
     *  <code>subst</code> and registers the corresponding variable 
     *  instantiation in <code>subst</code>; if the unification attemt 
     *  fails, <code>subst</code> remains 
     *  unchanged and no variables get instantiated.
     *  <b>pre:</b> neither <code>term1</code>, nor <code>term2</code> 
     *  contains abstractions or quantifiers; they are either both formulas,
     *  or both individual-valued.
     *  <b>VERY IMPORTANT:</b> the terms are considered modulo the current
     *             global substitution 1, ie, if the unification process meets
     *             an instantiated variable, it deepens into the instance.
     */
    public static boolean unify(Flatterm term1,
				Flatterm term2,
				Substitution1 subst) {
	
	int unifierSavepoint = subst.savepoint();
	
	if (tryToUnify(term1,term2,subst)) return true;
	
	// Undo the changes to subst:
	subst.backtrackTo(unifierSavepoint);
	
	return false;
    } // unify(Flatterm term1,..)



    /** Tries to unify the terms; if succeeds, makes a savepoint in 
     *  <code>subst</code> and registers the corresponding variable 
     *  instantiation in <code>subst</code>; if the unification attemt 
     *  fails, <code>subst</code> remains 
     *  unchanged and no variables get instantiated.
     *  <b>pre:</b> neither <code>var.instance()</code>, 
     *  nor <code>term</code> contains abstractions or quantifiers;
     *  they are either both formulas, or both individual-valued.
     *  <b>VERY IMPORTANT:</b> the terms are considered modulo the current
     *             global substitution 1, ie, if the unification process meets
     *             an instantiated variable, it deepens into the instance.
     */
    public static boolean unify(Variable var,
				Flatterm term,
				Substitution1 subst) {

	int unifierSavepoint = subst.savepoint();
	
	if (tryToUnify(var,term,subst)) return true;
	
	// Undo the changes to subst:
	subst.backtrackTo(unifierSavepoint);
	
	return false;

    } // unify(Variable var,..)



    /** Cheap unifiability pre-test: if it returns <code>true</code>, 
     *  the terms are definitely non-unifiable; if it returns 
     *  <code>true</code>, the terms may still be non-unifiable.
     *  The method does not affect any instance values of variables
     *  and does not consider them when estimating the unifiability.
     *  <b>pre:</b> terms should not contain quantifiers or abstractions.
     */
    public static boolean possiblyUnify(Term term1,Term term2) {
	
	if (term1.isVariable())
	    return !term2.containsVariableAsProperSubterm((Variable)term1); 
	    
	if (term2.isVariable())
	    return !term1.containsVariableAsProperSubterm((Variable)term2);

	if (term1.kind() != term2.kind()) return false;

	switch (term1.kind())
	    {
		
	    case Term.Kind.CompoundTerm:
		return 
		    ((CompoundTerm)term1).
		    function().
		    equals(((CompoundTerm)term2).function()) &&
		    possiblyUnify(((CompoundTerm)term1).argument(),
				  ((CompoundTerm)term2).argument());

	    case Term.Kind.IndividualConstant:
		return 
		    ((IndividualConstant)term1).
		    equals((IndividualConstant)term2);

	    case Term.Kind.AtomicFormula:
		return 
		    ((AtomicFormula)term1).
		    predicate().
		    equals(((AtomicFormula)term2).predicate()) &&
		    possiblyUnify(((AtomicFormula)term1).argument(),
				  ((AtomicFormula)term2).argument());
		    
	    case Term.Kind.ConnectiveApplication:
		return 
		    ((ConnectiveApplication)term1).
		    connective().
		    equals(((ConnectiveApplication)term2).connective()) &&
		    possiblyUnify(((ConnectiveApplication)term1).argument(),
				  ((ConnectiveApplication)term2).argument());

	    case Term.Kind.TermPair:
		return 
		    possiblyUnify(((TermPair)term1).first(),
				  ((TermPair)term2).first()) &&
		    possiblyUnify(((TermPair)term1).second(),
				  ((TermPair)term2).second());
		    
		
	    }; // switch (_currentSubterm.kind())
	    	    

	assert false;
	return false;

    } // possiblyUnify(Term term1,Term term2) 




      
    /** Tries to unify the terms; increments <code>subst</code> with new 
     *  instantiation; does not cancel the instantiations even if it fails.
     */
    private static boolean tryToUnify(Flatterm term1,
				      Flatterm term2,
				      Substitution1 subst) {
	
	assert !term1.isFormula() || term2.isFormula();
	assert term1.isFormula() || !term2.isFormula();
	assert term1.kind() != Term.Kind.QuantifierApplication;
	assert term1.kind() != Term.Kind.AbstractionTerm;
	assert term2.kind() != Term.Kind.QuantifierApplication;
	assert term2.kind() != Term.Kind.AbstractionTerm;
	
	switch (term1.kind())
	    {
	    case Term.Kind.Variable: 
		return tryToUnify(term1.variable(),term2,subst);
	
	    case Term.Kind.CompoundTerm:
	    
		switch (term2.kind())
		    {     
		    case Term.Kind.Variable: 
			return tryToUnify(term2.variable(),term1,subst);

		    case Term.Kind.CompoundTerm:
			{
			    if (!term1.function().equals(term2.function())) 
				return false;
			    Flatterm arg1 = term1.nextCell();
			    Flatterm arg2 = term2.nextCell();
			    for (int n = 0; n < term1.function().arity(); ++n)
				{
				    if (!tryToUnify(arg1,arg2,subst)) return false;
				    arg1 = arg1.after();
				    arg2 = arg2.after();
				};
			    return true;
			}
		    
		    case Term.Kind.IndividualConstant: return false;

		    }; // switch (term2.kind())
		
		assert false;
		return false;
	    
   
	    case Term.Kind.IndividualConstant:
		switch (term2.kind())
		    {     
		    case Term.Kind.Variable: 
			return tryToUnify(term2.variable(),term1,subst);

		    case Term.Kind.CompoundTerm: return false;
		    
		    case Term.Kind.IndividualConstant: 
			return 
			    term1.individualConstant().
			    equals(term2.individualConstant());

		    }; // switch (term2.kind())

		assert false;
		return false;


	    case Term.Kind.AtomicFormula: 
		{
		    if (!term2.isAtomicFormula() || 
			!term1.predicate().equals(term2.predicate()))
			return false;
		
		    Flatterm arg1 = term1.nextCell();
		    Flatterm arg2 = term2.nextCell();
		    for (int n = 0; n < term1.predicate().arity(); ++n)
			{
			    if (!tryToUnify(arg1,arg2,subst)) return false;
			    arg1 = arg1.after();
			    arg2 = arg2.after();
			};
		    return true;
		}

	    case Term.Kind.ConnectiveApplication:
		{
		    if (!term2.isConnectiveApplication() || 
			!term1.connective().equals(term2.connective()))
			return false;

		    Flatterm arg1 = term1.nextCell();
		    Flatterm arg2 = term2.nextCell();
		    for (int n = 0; n < term1.connective().arity(); ++n)
			{
			    if (!tryToUnify(arg1,arg2,subst)) return false;
			    arg1 = arg1.after();
			    arg2 = arg2.after();
			};
		    return true;
		}

	    }; // switch (term1.kind())
	
	assert false;
	return false;

    } // tryToUnify(Flatterm term1,..)





    private static boolean tryToUnify(Variable var,
				      Flatterm term,
				      Substitution1 subst) {

	assert !term.isFormula();
	
	if (var.isInstantiated1()) 
	    {
		return tryToUnify(var.instance1(),term,subst);
	    }
	else
	    {
		if (term.isVariable())
		    {
			Variable var1 = term.variable();
			if (var == var1) return true;
			
			if (var1.isInstantiated1())
			    return tryToUnify(var,var1.instance1(),subst);
		    }
		else if (FlattermInstance.contains(term,var))
		    return false;
		subst.instantiate(var,term);
		return true;
	    }

    } // tryToUnify(Variable var,..)
      


	
} // class Unification