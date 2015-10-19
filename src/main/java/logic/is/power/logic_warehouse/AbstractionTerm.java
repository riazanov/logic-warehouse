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


/**
 * Data structure for terms that are used as arguments of quantifiers,
 * which are essentially lambda abstractions: in <code>! [X] p(X)</code>, 
 * the part <code>[X] p(X)</code>
 * is an abstraction; we only allow abstractions with one variable.
 */
public final class AbstractionTerm implements Term {

    /** <b> pre: </b> <code>matrix.isFormula()</code>. */
    public AbstractionTerm(Variable var,Term matrix) {
	
	assert matrix.isFormula() || matrix.isAbstraction();
	_variable = var;
	_matrix = matrix;
    }
      
    public final Variable variable() { return _variable; }

    public final Term matrix() { return _matrix; }
    
    /** See also {@link logic.is.power.logic_warehouse.Flatterm#hashCode()}. */
    public final int hashCode() {
	// Must correspond to Flatterm.hashCode()
	return _variable.hashCode() * 5 + _matrix.hashCode();
    }


    //                Methods prescribed by the interface Term:

    public final java.util.Iterator<Term> iterator() {
	return new Term.Iterator(this);
    }
    
    public final int kind() { return Term.Kind.AbstractionTerm; }
    
    public final boolean isVariable() { return false; }
        
    public final boolean isIndividualConstant() { return false; }
	
    public final boolean isIndividualValued() { return false; }
	    
    public final boolean isPair() { return false; }

    public final boolean isFormula() { return false; }

    public final boolean isAbstraction() { return true; }

    /** Just throws an {@link java.lang.Error} exception. */
    public final Symbol topSymbol() {
	throw 
	    new Error("topSymbol() is undefined for logic_warehouse_je.AbstractionTerm");
    }

    public final boolean equals(Term term) {

	return term.kind() == Term.Kind.AbstractionTerm &&
	    _variable == ((AbstractionTerm)term).variable() &&
	    _matrix.equals(((AbstractionTerm)term).matrix());

    } // equals(Term term)

    public final boolean equalsModuloSubst2(Term term) {
	
	return 
	    (term.kind() == Term.Kind.AbstractionTerm &&
	     _variable == ((AbstractionTerm)term).variable() &&
	     _matrix.equalsModuloSubst2(((AbstractionTerm)term).matrix())) ||
	    (term.isVariable() &&
	     ((Variable)term).isInstantiated2() &&
	     ((Variable)term).instance2().equalsModuloSubst2(this));

    } // equalsModuloSubst2(Term term)

    public final boolean equalsModuloSubst3(Term term) {
	
	return 
	    term.kind() == Term.Kind.AbstractionTerm &&
	    _variable == ((AbstractionTerm)term).variable() &&
	    _matrix.equalsModuloSubst3(((AbstractionTerm)term).matrix());

    } // equalsModuloSubst3(Term term)

    public final boolean equals(Flatterm flatterm) {
	
	return flatterm.isAbstraction() &&
	    _variable == flatterm.variable() && 
	    _matrix.equals(flatterm.nextCell());

    } // equals(Flatterm flatterm)


    
    public 
	final 
	boolean 
	containsVariableAsProperSubterm(Variable var) {
	return _matrix.containsVariableAsProperSubterm(var);
    }

    
    public final boolean containsAsProperSubterm(Term term) {
	return _matrix.equals(term) || 
	    _matrix.containsAsProperSubterm(term);
    }

    public final boolean containsAsProperSubtermModuloSubst2(Term term) {
	return _matrix.equalsModuloSubst2(term) || 
	    _matrix.containsAsProperSubtermModuloSubst2(term);
    }

    public final boolean containsAsProperSubtermModuloSubst3(Term term) {
	return _matrix.equalsModuloSubst3(term) || 
	    _matrix.containsAsProperSubtermModuloSubst3(term);
    }

    public final Term subtermInPosition(int n) {
	assert n >= 0;
	assert n < numberOfSymbols();
	if (n == 0) return this;
	return _matrix.subtermInPosition(n - 1);
    }

    
    public final int depthOfPosition(int n) {
	assert n >= 0;
	assert n < numberOfSymbols();
	if (n == 0) return 0;
	return _matrix.depthOfPosition(n - 1) + 1;
    }

    public final int positionOfSubterm(Term term) {
	assert !term.isPair();
	if (this == term) return 0;
	int result = _matrix.positionOfSubterm(term);
	if (result < 0) return -1;
	return result + 1;
    }

    public final int mapPositionWithSubst1(int n) {
	assert n >= 0;
	assert n < numberOfSymbols();
	if (n == 0) return 0;
	return 1 + _matrix.mapPositionWithSubst1(n - 1);
    }
    


    public final void collectFreeVariables(Collection<Variable> result) {

	if (result.contains(_variable))
	{
	    _matrix.collectFreeVariables(result);
	}
	else
	{
	    _matrix.collectFreeVariables(result);
	    result.remove(_variable);
	};

    } // collectFreeVariables(Collection<Variable> result)

    public final boolean containsFreeVariables() {
	TreeSet<Variable> exclusions = new TreeSet<Variable>();
	exclusions.add(_variable);
	return containsFreeVariables(exclusions);
    }

    public final boolean containsVariables() {
	return true;
    }

    public 
	final 
	boolean containsFreeVariables(Collection<Variable> exclusions) {
	
	if (exclusions.contains(_variable))
	    return _matrix.containsFreeVariables(exclusions);
	exclusions.add(_variable);
	boolean result = _matrix.containsFreeVariables(exclusions);
	exclusions.remove(_variable);
	return result;
    }


    public final int depth() {
	return _matrix.depth() + 1;
    }

    public final int numberOfSymbols() {
	return _matrix.numberOfSymbols() + 1;
    }

    public final int numberOfSymbolsAfterSubst1() {
	return _matrix.numberOfSymbolsAfterSubst1() + 1;
    }

    public 
	final 
	int 
	numberOfNonvariableSymbolsFromCategory(int category,
					       int modulus) {
	return 
	    _matrix.
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
	
	_matrix.
	    addNumberOfNonvariableSymbolsFromCategories(modulus,
							result);
	
    } // addNumberOfNonvariableSymbolsFromCategories(int modulus,
    
    public String toString(boolean closed) {
	return toString();
    }

    //                     More public methods:
    
    public String toString() {
	return "[" + _variable + "] : " + _matrix.toString(true);
    }


    //                     Data:


    private final Variable _variable;

    private final Term _matrix;

}; // class AbstractionTerm 
