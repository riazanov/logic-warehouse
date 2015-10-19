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
 * quantifiers.
 */
public final class QuantifierApplication extends Formula {

    /** <b> pre: </b> abs.kind() == Term.Kind.AbstractionTerm. */
    public QuantifierApplication(Quantifier quant,Term abs) {
	_quantifier = quant;
	_abstraction = abs;
    }
    

    public final Quantifier quantifier() { return _quantifier; }

    public final Term abstraction() { return _abstraction; }

    public final int hashCode() {
	// Must correspond to Flatterm.hashCode()
	return _quantifier.hashCode() * 5 + _abstraction.hashCode();
    }


    //                Methods prescribed by the interface Term:

    public final java.util.Iterator<Term> iterator() {
	return new Term.Iterator(this);
    }

    public final int kind() { return Term.Kind.QuantifierApplication; }
    

    /** @return <code>quantifier()</code> */
    public final Symbol topSymbol() { return _quantifier; }
	
    /** @return false */
    public final boolean isNegative() { return false; }

    /** @return this */
    public final Formula atom() { return this; }


    public final boolean equals(Term term) {
	return term.kind() == Term.Kind.QuantifierApplication &&
	    _quantifier == ((QuantifierApplication)term)._quantifier &&
	    _abstraction.equals(((QuantifierApplication)term)._abstraction);
    }

    public final boolean equalsModuloSubst2(Term term) {
	return term.kind() == Term.Kind.QuantifierApplication &&
	    _quantifier == ((QuantifierApplication)term)._quantifier &&
	    _abstraction.
	    equalsModuloSubst2(((QuantifierApplication)term)._abstraction);
    }

    public final boolean equalsModuloSubst3(Term term) {
	return term.kind() == Term.Kind.QuantifierApplication &&
	    _quantifier == ((QuantifierApplication)term)._quantifier &&
	    _abstraction.
	    equalsModuloSubst3(((QuantifierApplication)term)._abstraction);
    }

	

    public final boolean equals(Flatterm flatterm) {
	return flatterm.isQuantifierApplication() &&
	    _quantifier == flatterm.quantifier() &&
	    _abstraction.equals(flatterm.nextCell());
    }

    public 
	final 
	boolean containsVariableAsProperSubterm(Variable var) {
	return _abstraction.containsVariableAsProperSubterm(var);
    }

    public final boolean containsAsProperSubterm(Term term) {
	return _abstraction.containsAsProperSubterm(term);
    }


    public final boolean containsAsProperSubtermModuloSubst2(Term term){
	return _abstraction.containsAsProperSubtermModuloSubst2(term);
    }

    public final boolean containsAsProperSubtermModuloSubst3(Term term){
	return _abstraction.containsAsProperSubtermModuloSubst3(term);
    }

    public final Term subtermInPosition(int n) {
	assert n >= 0;
	assert n < numberOfSymbols();
	if (n == 0) return this;
	return _abstraction.subtermInPosition(n - 1);
    }

    
    public final int depthOfPosition(int n) {
	assert n >= 0;
	assert n < numberOfSymbols();
	if (n == 0) return 0;
	return _abstraction.depthOfPosition(n - 1) + 1;
    }

    public final int positionOfSubterm(Term term) {
	assert !term.isPair();
	if (this == term) return 0;
	int result = _abstraction.positionOfSubterm(term);
	if (result < 0) return -1;
	return result + 1;
    }

    public final int mapPositionWithSubst1(int n) {
	assert n >= 0;
	assert n < numberOfSymbols();
	if (n == 0) return 0;
	return 1 + _abstraction.mapPositionWithSubst1(n - 1);
    }

    public final void collectFreeVariables(Collection<Variable> result) {
	_abstraction.collectFreeVariables(result);
    }

    public final boolean containsFreeVariables() {
	return _abstraction.containsFreeVariables();
    }
    public final boolean containsVariables() {
	return true;
    }

    public 
	final 
	boolean containsFreeVariables(Collection<Variable> exclusions) {
	return _abstraction.containsFreeVariables(exclusions);
    }


    public final int depth() {
	return 1 + abstraction().depth();
    }

    public final int numberOfSymbols() {
	return 1 + abstraction().numberOfSymbols();
    }

    public final int numberOfSymbolsAfterSubst1() {
	return 1 + abstraction().numberOfSymbolsAfterSubst1();
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
	    abstraction().
	    numberOfNonvariableSymbolsFromCategory(category,
						   modulus) +
	    ((quantifier().category(modulus) == category)? 1 : 0);
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
	
	result[quantifier().category(modulus)] += 1;

	abstraction().
	    addNumberOfNonvariableSymbolsFromCategories(modulus,
							result);
	
    } // addNumberOfNonvariableSymbolsFromCategories(int modulus,
    

    

    public final String toString(boolean closed) {
	return toString();
    }



    //                      More public methods:

    public final String toString() {
	return _quantifier.toString() + _abstraction;
    }


    //                  Data:

    private final Quantifier _quantifier;

    private final Term _abstraction;
    // The type should probably be changed to AbstractionTerm.

}; // class QuantifierApplication
