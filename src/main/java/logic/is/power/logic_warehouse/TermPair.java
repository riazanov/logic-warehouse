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

public final class TermPair implements Term {

    /** <b>pre:</b> <code>!first.isPair()</code>. */
    public TermPair(Term first,Term second) {
	_first = first;
	_second = second;
    }

    public final int hashCode() {
	// Must correspond to Flatterm::hashCodeOfTuple(unsigned int arity)
	return _first.hashCode() * 5 + _second.hashCode();
    }

    public final Term first() { return _first; }

    public final Term second() { return _second; }


    //                Methods prescribed by the interface Term:

    public final java.util.Iterator<Term> iterator() {
	return new Term.Iterator(this);
    }
    
    public final int kind() { return Term.Kind.TermPair; }
    
    public final boolean isVariable() { return false; }
        
    public final boolean isIndividualConstant() { return false; }
	
    public final boolean isIndividualValued() { return false; }
	    
    public final boolean isPair() { return true; }

    public final boolean isFormula() { return false; }

    public final boolean isAbstraction() { return false; }

    /** Just throws an {@link java.lang.Error} exception. */
    public final Symbol topSymbol() {
	throw 
	    new Error("topSymbol() is undefined for logic_warehouse_je.AbstractionTerm");
    }

    public final boolean equals(Term term) {
	return term.kind() == Term.Kind.TermPair &&
	    _first.equals(((TermPair)term)._first) &&
	    _second.equals(((TermPair)term)._second);

    }

    public final boolean equalsModuloSubst2(Term term) {
	return term.kind() == Term.Kind.TermPair &&
	    _first.equalsModuloSubst2(((TermPair)term)._first) &&
	    _second.equalsModuloSubst2(((TermPair)term)._second);

    }

    public final boolean equalsModuloSubst3(Term term) {
	return term.kind() == Term.Kind.TermPair &&
	    _first.equalsModuloSubst3(((TermPair)term)._first) &&
	    _second.equalsModuloSubst3(((TermPair)term)._second);

    }
	


    public final boolean equals(Flatterm flatterm) {
	return _first.equals(flatterm) && 
	    flatterm.after() != null &&
	    _second.equals(flatterm.after());

    } // equals(Flatterm flatterm)


    public 
	final 
	boolean 
	containsVariableAsProperSubterm(Variable var) {
	return 
	    _first == (Term)var ||
	    _second == (Term)var ||
	    _first.containsVariableAsProperSubterm(var) ||
	    _second.containsVariableAsProperSubterm(var);
	    
    }




    public final boolean containsAsProperSubterm(Term term) {
	return 
	    _first.equals(term) ||
	    _second.equals(term) ||
	    _first.containsAsProperSubterm(term) ||
	    _second.containsAsProperSubterm(term);
    }


    public final boolean containsAsProperSubtermModuloSubst2(Term term) {
	return 
	    _first.equalsModuloSubst2(term) ||
	    _second.equalsModuloSubst2(term) ||
	    _first.containsAsProperSubtermModuloSubst2(term) ||
	    _second.containsAsProperSubtermModuloSubst2(term);
    }

    public final boolean containsAsProperSubtermModuloSubst3(Term term) {
	return 
	    _first.equalsModuloSubst3(term) ||
	    _second.equalsModuloSubst3(term) ||
	    _first.containsAsProperSubtermModuloSubst3(term) ||
	    _second.containsAsProperSubtermModuloSubst3(term);
    }

    public final Term subtermInPosition(int n) {
	assert n >= 0;
	assert n < numberOfSymbols();
	if (n < _first.numberOfSymbols())
	    return _first.subtermInPosition(n);
	return _second.subtermInPosition(n - _first.numberOfSymbols());
    }

    
    public final int depthOfPosition(int n) {
	assert n >= 0;
	assert n < numberOfSymbols();
	if (n == 0) return 0;

	if (n < _first.numberOfSymbols())
	    return _first.depthOfPosition(n);
	
	return _second.depthOfPosition(n - _first.numberOfSymbols());
    }


    public final int positionOfSubterm(Term term) {
	assert !term.isPair();
	int result = _first.positionOfSubterm(term);
	if (result >= 0) return result;
	result = _second.positionOfSubterm(term);
	if (result < 0) return -1;
	return result + _first.numberOfSymbols();
    }

    public final int mapPositionWithSubst1(int n) {
	assert n >= 0;
	assert n < numberOfSymbols();
	if (n < _first.numberOfSymbols())
	    return _first.mapPositionWithSubst1(n);
	return _first.numberOfSymbolsAfterSubst1() + 
	    _second.mapPositionWithSubst1(n - _first.numberOfSymbols());
    }


    public final void collectFreeVariables(Collection<Variable> result) {
	
	_first.collectFreeVariables(result);
	_second.collectFreeVariables(result);
    } 

    public final boolean containsFreeVariables() {
	return _first.containsFreeVariables() ||
	    _second.containsFreeVariables();
    }

    public final boolean containsVariables() {
	return _first.containsVariables() ||
	    _second.containsVariables();
    }

    public 
	final 
	boolean containsFreeVariables(Collection<Variable> exclusions) {
	return _first.containsFreeVariables(exclusions) ||
	    _second.containsFreeVariables(exclusions);
    }
    


    public final int depth() {
	return Math.max(_first.depth(),_second.depth());
    }

    public final int numberOfSymbols() {
	return _first.numberOfSymbols() + _second.numberOfSymbols();
    }

    public final int numberOfSymbolsAfterSubst1() {
	return _first.numberOfSymbolsAfterSubst1() + 
	    _second.numberOfSymbolsAfterSubst1();
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
	    _first.
	    numberOfNonvariableSymbolsFromCategory(category,
						   modulus) +
	    _second.
	    numberOfNonvariableSymbolsFromCategory(category,
						   modulus);
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
	
	_first.
	    addNumberOfNonvariableSymbolsFromCategories(modulus,
							result);
	_second.
	    addNumberOfNonvariableSymbolsFromCategories(modulus,
							result);
	
    } // addNumberOfNonvariableSymbolsFromCategories(int modulus,


    public String toString(boolean closed) {
	return (closed)? "(" + toString() + ")" : toString();
    }


    //                     More public methods:
    
    

    public String toString() {
	assert !_first.isPair();
	return _first + "," + _second;
    }


    //                     Package access methods:

    /** @return (_second.isPair())? 1 + ((TermPair)_second).dimension() : 2 */
    /* package */ int dimension() {
	return 
	    (_second.isPair())? 
	    (1 + ((TermPair)_second).dimension())
	    : 
	    2;
    } 


    //                     Data:

    private final Term _first;

    private final Term _second;

}; // class TermPair
