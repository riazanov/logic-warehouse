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




/** Static methods for computing weights of terms in 
 *  various representations. Connective, quantifiers and
 *  abstractions are assumed to have weight 1. 
 *  Pair constructors are assumed to have weight 0.
 */
public class WeightComputation {

    public static WeightPolynomial computeWeight(Term term) {
	WeightPolynomial result = new WeightPolynomial();
	addWeight(term,result);
	return result;
    }
    

    /** Adds the weight of the term to <code>w</code>. */
    public 
	static void addWeight(Term term,WeightPolynomial w) {

	// Can be optimised by using recursion instead of Term.LeanIterator.

	Term.LeanIterator iter = new Term.LeanIterator(term);
	
	while (iter.hasNext())
	    {
		Term subterm = iter.next();
		
		switch (subterm.kind())
		    {
		    case Term.Kind.Variable:
			w.add((Variable)subterm);
			break;
		    case Term.Kind.CompoundTerm:       
			w.add(((CompoundTerm)subterm).function().weight());
			break;
		    case Term.Kind.IndividualConstant:
			w.add(((IndividualConstant)subterm).weight());
			break;
		    case Term.Kind.AtomicFormula: 
			w.add(((AtomicFormula)subterm).predicate().weight());
			break;   
		    case Term.Kind.ConnectiveApplication:
			w.add(1);
			break;   
		    case Term.Kind.QuantifierApplication:
			w.add(1);
			break;   
		    case Term.Kind.AbstractionTerm: 
			w.add(1);
			break;  
		    default:
			assert false;
			return;
		    }; // switch (subterm.kind())
		
	    }; // while (iter.hasNext())

	
    } // addWeight(Term term,WeightPolynomial w)
    
    public static WeightPolynomial computeWeight(Flatterm term) {
	WeightPolynomial result = new WeightPolynomial();
	addWeight(term,result);
	return result;
    }

    /** Adds the weight of the term to <code>w</code>. */
    public 
	static void addWeight(Flatterm term,WeightPolynomial w) {

	for (Flatterm subterm = term; 
	     subterm != term.after(); 
	     subterm = subterm.nextCell())
	    {
		switch (subterm.kind())
		    {
		    case Term.Kind.Variable:
			w.add(subterm.variable());
			break;
		    case Term.Kind.CompoundTerm:       
			w.add(subterm.function().weight());
			break;
		    case Term.Kind.IndividualConstant:
			w.add(subterm.individualConstant().weight());
			break;
		    case Term.Kind.AtomicFormula: 
			w.add(subterm.predicate().weight());
			break;   
		    case Term.Kind.ConnectiveApplication:
			w.add(1);
			break;   
		    case Term.Kind.QuantifierApplication:
			w.add(1);
			break;   
		    case Term.Kind.AbstractionTerm: 
			w.add(1);
			break;  
		    default:
			assert false;
			return;
		    }; // switch (subterm.kind())

	    }; // for (Flatterm subterm = term;


    } // addWeight(Flatterm term,WeightPolynomial w)


    /** Computes weight of the instance of the term wrt
     *  global substitution 2.
     */
    public 
	static 
	WeightPolynomial computeWeightModuloSubst2(Term term) {

	WeightPolynomial result = new WeightPolynomial();
	addWeightModuloSubst2(term,result);
	return result;
    }
	
    /** Computes weight of the instance of the term wrt
     *  global substitution 3.
     */
    public 
	static 
	WeightPolynomial computeWeightModuloSubst3(Term term) {

	WeightPolynomial result = new WeightPolynomial();
	addWeightModuloSubst3(term,result);
	return result;
    }
	

    /** Adds the weight of the instance of the term
     *  wrt grobal substitution 2 to <code>w</code>. */
    public 
	static 
	void addWeightModuloSubst2(Term term,WeightPolynomial w) {

	// Can be optimised by using recursion instead of Term.LeanIterator.

	Term.LeanIterator iter = new Term.LeanIterator(term);
	
	while (iter.hasNext())
	    {
		Term subterm = iter.next();
		
		switch (subterm.kind())
		    {
		    case Term.Kind.Variable:
			if (((Variable)subterm).isInstantiated2())
			    {
				addWeight(((Variable)subterm).instance2(),w);
			    }
			else
			    w.add((Variable)subterm);
			break;
		    case Term.Kind.CompoundTerm:       
			w.add(((CompoundTerm)subterm).function().weight());
			break;
		    case Term.Kind.IndividualConstant:
			w.add(((IndividualConstant)subterm).weight());
			break;
		    case Term.Kind.AtomicFormula: 
			w.add(((AtomicFormula)subterm).predicate().weight());
			break;   
		    case Term.Kind.ConnectiveApplication:
			w.add(1);
			break;   
		    case Term.Kind.QuantifierApplication:
			w.add(1);
			break;   
		    case Term.Kind.AbstractionTerm: 
			w.add(1);
			break;  
		    default:
			assert false;
			return;
		    }; // switch (subterm.kind())
		
	    }; // while (iter.hasNext())

    } // addWeightModuloSubst2(Term term,WeightPolynomial w)




    /** Adds the weight of the instance of the term
     *  wrt grobal substitution 3 to <code>w</code>. */
    public 
	static 
	void addWeightModuloSubst3(Term term,WeightPolynomial w) {

	// Can be optimised by using recursion instead of Term.LeanIterator.

	Term.LeanIterator iter = new Term.LeanIterator(term);
	
	while (iter.hasNext())
	    {
		Term subterm = iter.next();
		
		switch (subterm.kind())
		    {
		    case Term.Kind.Variable:
			if (((Variable)subterm).isInstantiated3())
			    {
				addWeight(((Variable)subterm).instance3(),w);
			    }
			else
			    w.add((Variable)subterm);
			break;
		    case Term.Kind.CompoundTerm:       
			w.add(((CompoundTerm)subterm).function().weight());
			break;
		    case Term.Kind.IndividualConstant:
			w.add(((IndividualConstant)subterm).weight());
			break;
		    case Term.Kind.AtomicFormula: 
			w.add(((AtomicFormula)subterm).predicate().weight());
			break;   
		    case Term.Kind.ConnectiveApplication:
			w.add(1);
			break;   
		    case Term.Kind.QuantifierApplication:
			w.add(1);
			break;   
		    case Term.Kind.AbstractionTerm: 
			w.add(1);
			break;  
		    default:
			assert false;
			return;
		    }; // switch (subterm.kind())
		
	    }; // while (iter.hasNext())

    } // addWeightModuloSubst3(Term term,WeightPolynomial w)


} // class WeightComputation 