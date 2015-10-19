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

import java.util.Collection;


import java.util.Iterator;


/**
 * Data structure for atomic formulas (= applications of predicates).
 */
public final class AtomicFormula extends Formula {

      /** <b> pre: </b> 
       *    <code>(arg == null || pred.arity() != 1 || arg.isIndividualValued()) &&
       *    (arg == null || pred.arity() == 1 || arg.isPair())</code>.
       */
    public AtomicFormula(Predicate pred,Term arg) {
	assert arg == null || pred.arity() != 1 || arg.isIndividualValued();
	assert arg == null || 
	    pred.arity() == 1 || 
	    (arg.isPair() &&
	     ((TermPair)arg).dimension() == pred.arity());
	_predicate = pred;
	_argument = arg;
    }

    public final Predicate predicate() { return _predicate; }

    public final boolean isEquality() { return _predicate.isEquality(); }

    /** @return <code>null</code> if the formula is just a propositional 
     *          variable. 
     */
    public final Term argument() { return _argument; }


    public final Term arg(int n) { 
	assert predicate().arity() > n;

	Term subterm = argument();

	while (n != 0) 
	    {
		subterm = ((TermPair)subterm).second();
		--n;
	    };

	if (subterm instanceof TermPair)
	    return ((TermPair)subterm).first();
	
	return subterm;

    } // arg(int n)
    

    /** <b>pre:</b> <code>predicate().arity() == 2</code> */
    public final Term firstArg() {
	assert predicate().arity() == 2;
	assert _argument != null;
	assert _argument.kind() == Term.Kind.TermPair;
	return ((TermPair)_argument).first();
    }

    /** <b>pre:</b> <code>predicate().arity() == 2</code> */
    public final int firstArgPosition() {
	assert predicate().arity() == 2;
	assert _argument != null;
	assert _argument.kind() == Term.Kind.TermPair;
	return 1;
    }

    /** <b>pre:</b> <code>predicate().arity() == 2</code> */
    public final Term secondArg() {
	assert predicate().arity() == 2;
	assert _argument != null;
	assert _argument.kind() == Term.Kind.TermPair;
	return ((TermPair)_argument).second();
    }

    /** <b>pre:</b> <code>predicate().arity() == 2</code> */
    public final int secondArgPosition() {
	assert predicate().arity() == 2;
	assert _argument != null;
	assert _argument.kind() == Term.Kind.TermPair;
	return 1 + ((TermPair)_argument).first().numberOfSymbols();
    }


    public final boolean isLinear() {

	LinkedList<Variable> variables = new LinkedList<Variable>();

	for (int n = 0; n < predicate().arity(); ++n)
	    {
		Term arg = arg(n);
		
		if (!arg.isVariable()) return false;
		
		Variable var = (Variable)arg;
		
		if (variables.contains(var)) return false;
		
		variables.add(var);
	    };
	
	return true;
	
    } // isLinear()



    public final int hashCode() {
	// Must correspond to Flatterm.hashCode()
	  return 
	    (_argument == null)? 
	    _predicate.hashCode()
	    :
	    (_predicate.hashCode() * 5 + _argument.hashCode());
    }
    

    //                Methods prescribed by the interface Term:

    public final java.util.Iterator<Term> iterator() {
	return new Term.Iterator(this);
    }

    public final int kind() { return Term.Kind.AtomicFormula; }
    

    /** @return <code>predicate()</code> */
    public final Symbol topSymbol() { return _predicate; }

    
    public final boolean isNegative() { return false; }

    public final Formula atom() { return this; }


    public final boolean equals(Term term) {
	return term.kind() == Term.Kind.AtomicFormula &&
	    _predicate.equals(((AtomicFormula)term)._predicate) &&
	    ((_argument == null &&
	      ((AtomicFormula)term)._argument == null) ||
	     (_argument != null &&
	      ((AtomicFormula)term)._argument != null &&
	      _argument.equals(((AtomicFormula)term)._argument)));
    }
	

    public final boolean equalsModuloSubst2(Term term) {
	return term.kind() == Term.Kind.AtomicFormula &&
	    _predicate.equals(((AtomicFormula)term)._predicate) &&
	    ((_argument == null &&
	      ((AtomicFormula)term)._argument == null) ||
	     (_argument != null &&
	      ((AtomicFormula)term)._argument != null &&
	      _argument.equalsModuloSubst2(((AtomicFormula)term)._argument)));
    }

    public final boolean equalsModuloSubst3(Term term) {
	return term.kind() == Term.Kind.AtomicFormula &&
	    _predicate.equals(((AtomicFormula)term)._predicate) &&
	    ((_argument == null &&
	      ((AtomicFormula)term)._argument == null) ||
	     (_argument != null &&
	      ((AtomicFormula)term)._argument != null &&
	      _argument.equalsModuloSubst3(((AtomicFormula)term)._argument)));
    }

    public final boolean equals(Flatterm flatterm) {
	
	return flatterm.isAtomicFormula() &&
	    _predicate == flatterm.predicate() &&
	    (_argument == null || _argument.equals(flatterm.nextCell()));
    }

    
    public 
	final 
	boolean 
	containsVariableAsProperSubterm(Variable var) {
	return _argument != null &&
	    (_argument == (Term)var ||
	     _argument.containsVariableAsProperSubterm(var));
    }
    
    public final boolean containsAsProperSubterm(Term term) {
	return _argument != null &&
	    (_argument.equals(term) || 
	     _argument.containsAsProperSubterm(term));
    }

    public final boolean containsAsProperSubtermModuloSubst2(Term term) {
	return _argument != null &&
	    (_argument.equalsModuloSubst2(term) || 
	     _argument.containsAsProperSubtermModuloSubst2(term));
    }

    public final boolean containsAsProperSubtermModuloSubst3(Term term) {
	return _argument != null &&
	    (_argument.equalsModuloSubst3(term) || 
	     _argument.containsAsProperSubtermModuloSubst3(term));
    }
    

    
    public final Term subtermInPosition(int n) {
	assert n >= 0;
	assert n < numberOfSymbols();
	if (n == 0) return this;
	return _argument.subtermInPosition(n - 1);
    }

    
    public final int depthOfPosition(int n) {
	assert n >= 0;
	assert n < numberOfSymbols();
	if (n == 0) return 0;
	return _argument.depthOfPosition(n - 1) + 1;
    }
    
    public final int positionOfSubterm(Term term) {
	assert !term.isPair();
	if (this == term) return 0;
	if (_argument == null) return -1;
	int result = _argument.positionOfSubterm(term);
	if (result < 0) return -1;
	return result + 1;
    }

    public final int mapPositionWithSubst1(int n) {
	assert n >= 0;
	assert n < numberOfSymbols();
	if (n == 0) return 0;
	return 1 + _argument.mapPositionWithSubst1(n - 1);
    }

    public final void collectFreeVariables(Collection<Variable> result) {
	if (_argument != null) _argument.collectFreeVariables(result);
    }

    
    public final boolean containsFreeVariables() {
	return _argument != null &&
	    _argument.containsFreeVariables();
    } 

    public final boolean containsVariables() { 
	return _argument != null &&
	    _argument.containsVariables();
    } 

    public 
	final 
	boolean containsFreeVariables(Collection<Variable> exclusions) {
	return _argument != null &&
	    _argument.containsFreeVariables(exclusions);
	
    }

    public final int depth() {
	return (argument() == null)? 0 : (1 + argument().depth());
    }

    public final int numberOfSymbols() {
	return (argument() == null)? 1 : (1 + argument().numberOfSymbols());
    }

    public final int numberOfSymbolsAfterSubst1() {
	return 
	    (argument() == null)? 
	    1 
	    : 
	    (1 + argument().numberOfSymbolsAfterSubst1());
    }
    
    /** Counts all nonvariablr symbols <code>sym</code>, 
     *  including logical symbols, but excluding pair constructors
     *  and abstraction operators, such that 
     *  <code>sym.category(modulus) == category</code>.
     */
    public 
	final
	int 
	numberOfNonvariableSymbolsFromCategory(int category,
					       int modulus) {
	return 
	    ((argument() == null)? 
	     0 
	     : 
	     argument().
	     numberOfNonvariableSymbolsFromCategory(category,
						    modulus)) +
	    ((predicate().category(modulus) == category)? 1 : 0);
    }

    
    
    
    /** Counts all nonvariable symbols <code>sym</code>, 
     *  including logical symbols, but excluding pair constructors
     *  and abstraction operators, such that 
     *  <code>sym.category(modulus) == category</code>,
     *  for every <code>category</code> in <code>[0,modulus - 1]</code>,
     *  and adds the numbers to the corresponding values in 
     *  <code>result[category]</code>.
     */
    public 
	final 
	void 
	addNumberOfNonvariableSymbolsFromCategories(int modulus,
						    int[] result) {
	
	result[predicate().category(modulus)] += 1;

	if (argument() != null)
	    argument().
		addNumberOfNonvariableSymbolsFromCategories(modulus,
							    result);
	
    } // addNumberOfNonvariableSymbolsFromCategories(int modulus,
    



    public final String toString(boolean closed) {
	String result;
	if (_predicate.isEquality() || _predicate.isInfix())
	{
	    assert _argument.isPair();
	    result = 
		((TermPair)_argument).first() + 
		" " + _predicate + " " +
		((TermPair)_argument).second();
	    if (closed) result = "(" + result + ")";
	}
	else
	{
	    result = _predicate.toString();
	    if (_argument != null)
		result += "(" + _argument +")";
	}
	return result;
    }

    //                     More public methods:
    
    public final String toString() { return toString(false); }


    //                  Data:

    private final Predicate _predicate;

    private final Term _argument;

}; // class AtomicFormula 
