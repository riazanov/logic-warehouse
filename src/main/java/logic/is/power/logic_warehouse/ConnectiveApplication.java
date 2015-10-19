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

import java.util.Collection;

import java.util.Iterator;

/**
 * Data structure for representing formulas that are applications of 
 * connectives.
 */
public final class ConnectiveApplication extends Formula {

    /** <b>pre:</b> <code>(con.arity() != 1 || arg.isFormula()) &&
     *                    (con.arity() == 1 || arg.isPair())</code>.
     */
    public ConnectiveApplication(Connective con,Term arg) {
	assert arg != null;
	assert con.arity() != 1 || arg.isFormula();
	assert con.arity() == 1 || arg.isPair();
	_connective = con;
	_argument = arg;
    }
    

    public final Connective connective() { return _connective; }

    public final Term argument() { return _argument; }

    public final int hashCode() {
	// Must correspond to Flatterm.hashCode()
	return _connective.hashCode() * 5 + _argument.hashCode();
    }

    //                Methods prescribed by the interface Term:

    public final java.util.Iterator<Term> iterator() {
	return new Term.Iterator(this);
    }
    
    public final int kind() { return Term.Kind.ConnectiveApplication; }
    

    /** @return <code>connective()</code> */
    public final Symbol topSymbol() { return _connective; }


    /** @return connective().id() == Connective.Id.Not */
    public final boolean isNegative() {
	return _connective.id() == Connective.Id.Not;
    }

    /** @return (isNegative())? (Formula)argument() : this */
    public final Formula atom() {
	return (isNegative())? (Formula)argument() : this;
    }


    public final boolean equals(Term term) {
	return term.kind() == Term.Kind.ConnectiveApplication &&
	    _connective == ((ConnectiveApplication)term)._connective &&
	    _argument.equals(((ConnectiveApplication)term)._argument);
    }
    
    public final boolean equalsModuloSubst2(Term term) {
	return term.kind() == Term.Kind.ConnectiveApplication &&
	    _connective == ((ConnectiveApplication)term)._connective &&
	    _argument.
	    equalsModuloSubst2(((ConnectiveApplication)term)._argument);
    }


    public final boolean equalsModuloSubst3(Term term) {
	return term.kind() == Term.Kind.ConnectiveApplication &&
	    _connective == ((ConnectiveApplication)term)._connective &&
	    _argument.
	    equalsModuloSubst3(((ConnectiveApplication)term)._argument);
    }


    public final boolean equals(Flatterm flatterm) {
	return flatterm.isConnectiveApplication() &&
	    _connective == flatterm.connective() &&
	    _argument.equals(flatterm.nextCell());
    }

    public 
	final 
	boolean 
	containsVariableAsProperSubterm(Variable var) {
	return _argument.containsVariableAsProperSubterm(var);
    }

    public final boolean containsAsProperSubterm(Term term) {
	return _argument.equals(term) ||
	    _argument.containsAsProperSubterm(term);
    }

    public final boolean containsAsProperSubtermModuloSubst2(Term term) {
	return _argument.equalsModuloSubst2(term) ||
	    _argument.containsAsProperSubtermModuloSubst2(term);
    }

    public final boolean containsAsProperSubtermModuloSubst3(Term term) {
	return _argument.equalsModuloSubst3(term) ||
	    _argument.containsAsProperSubtermModuloSubst3(term);
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
	_argument.collectFreeVariables(result);
    }
    
    public final boolean containsFreeVariables() {
	return _argument.containsFreeVariables();
    }

    public final boolean containsVariables() {
	return _argument.containsVariables();
    }

    public 
	final 
	boolean containsFreeVariables(Collection<Variable> exclusions) {
	return _argument.containsFreeVariables(exclusions);
    }
    
    public final int depth() {
	return 1 + argument().depth();
    }

    public final int numberOfSymbols() {
	return 1 + argument().numberOfSymbols();
    }

    public final int numberOfSymbolsAfterSubst1() {
	return 1 + argument().numberOfSymbolsAfterSubst1();
    }


    /** Counts all nonvariable symbols <code>sym</code>, 
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
	    argument().
	    numberOfNonvariableSymbolsFromCategory(category,
						   modulus) +
	    ((connective().category(modulus) == category)? 1 : 0);
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
	result[connective().category(modulus)] += 1;
	
	argument().
	    addNumberOfNonvariableSymbolsFromCategories(modulus,
							result);
	
    } // addNumberOfNonvariableSymbolsFromCategories(int modulus,
    

    public final String toString(boolean closed) {
	if (connective() == Connective.getNot())
	{
	    assert _argument.isFormula();
	    
	    if (_argument.kind() == Term.Kind.AtomicFormula &&
		((AtomicFormula)_argument).predicate().isEquality())
	    {
		// Special case - negation of equality.

		assert ((AtomicFormula)_argument).argument().isPair();

		String result = 
		    ((TermPair)(((AtomicFormula)_argument).argument())).first() +
		    " != " +
		    ((TermPair)(((AtomicFormula)_argument).argument())).second();
		
		if (closed) result = "(" + result + ")";
	    
		return result;
	    }
	    else
		return "~" + _argument.toString(true);
	}
	else if (connective().isAssociative()) 
	{
	    assert _argument.isPair();

	    String result = ((TermPair)_argument).first().toString(true);

	    Term tail;

	    for (tail = ((TermPair)_argument).second();
		 tail.isPair();
		 tail = ((TermPair)tail).second())
	    {
		result += connective() + " " + ((TermPair)tail).first();
	    };

	    result += connective() + " " + tail;
	    
	    if (closed) result = "(" + result + ")";

	    return result;	    
	}
	else // non-associative binary connective
	{
	    assert _argument.isPair();

	    String result = 
		((TermPair)_argument).first().toString(true) +
		" " + _connective + " " +
		((TermPair)_argument).second().toString(true);

	    if (closed) result = "(" + result + ")";

	    return result;
	}
    }

    //                     More public methods:
    
    public final String toString() { return toString(false); }




    //                  Data:

    private final Connective _connective;

    private final Term _argument;

}; // class ConnectiveApplication
