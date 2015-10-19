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
 * Data structure for compound terms (= applications of nonnullary functions).
 */
public final class CompoundTerm implements Term {

    /** <b>pre:</b> 
     *  <code>func.arity() > 0 **
     *  (func.arity() != 1 || arg.isIndividualValued()) &&
     *  (func.arity() == 1 || arg.isPair())</code>.
     */
    public CompoundTerm(Function func,Term arg) {
	assert arg != null;
	assert func.arity() > 0;
	assert func.arity() != 1 || arg.isIndividualValued();
	assert func.arity() == 1 || arg.isPair();
	_function = func;
	_argument = arg;
    }
      
    public final Function function() { return _function; }
      
    /** @return nonnull object */
    public final Term argument() { return _argument; }



    public final Term arg(int n) { 
	assert function().arity() > n;

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

    public final int hashCode() {
	// Must correspond to Flatterm.hashCode()!
	return _function.hashCode() * 5 + _argument.hashCode();
    }


    //                Methods prescribed by the interface Term:

    public final java.util.Iterator<Term> iterator() {
	return new Term.Iterator(this);
    }

    public final int kind() { return Term.Kind.CompoundTerm; }
    
    public final boolean isVariable() { return false; }
        
    public final boolean isIndividualConstant() { return false; }
	
    public final boolean isIndividualValued() { return true; }
	    
    public final boolean isPair() { return false; }

    public final boolean isFormula() { return false; }

    public final boolean isAbstraction() { return false; }

    /** @return <code>function()</code> */
    public final Symbol topSymbol() { return _function; }


    public final boolean equals(Term term) {
	return term.kind() == Term.Kind.CompoundTerm &&
	    _function.equals(((CompoundTerm)term)._function) &&
	    _argument.equals(((CompoundTerm)term)._argument);
    }

    public final boolean equalsModuloSubst2(Term term) {
	return 
	    (term.kind() == Term.Kind.CompoundTerm &&
	     _function.equals(((CompoundTerm)term)._function) &&
	     _argument.equalsModuloSubst2(((CompoundTerm)term)._argument)) ||
	    (term.isVariable() &&
	     ((Variable)term).isInstantiated2() &&
	     ((Variable)term).instance2().equalsModuloSubst2(this));
    }

    public final boolean equalsModuloSubst3(Term term) {
	return 
	    term.kind() == Term.Kind.CompoundTerm &&
	    _function.equals(((CompoundTerm)term)._function) &&
	    _argument.equalsModuloSubst3(((CompoundTerm)term)._argument);
    }


    public final boolean equals(Flatterm flatterm) {
	assert _argument != null;
	return flatterm.isCompound() &&
	    _function == flatterm.function() &&
	    _argument.equals(flatterm.nextCell());
    }
    

    public 
	final 
	boolean 
	containsVariableAsProperSubterm(Variable var) {
	return
	    _argument == (Term)var ||
	    _argument.containsVariableAsProperSubterm(var);
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
    

    public String toString(boolean closed) {
	return toString();
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
	    ((function().category(modulus) == category)? 1 : 0);
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
	result[function().category(modulus)] += 1;

	argument().
	    addNumberOfNonvariableSymbolsFromCategories(modulus,
							result);
	
    } // addNumberOfNonvariableSymbolsFromCategories(int modulus,
    

    //                     More public methods:
    
    public String toString() {
	return _function + "(" + _argument + ")";
    }

    

    //                  Data:


    private final Function _function;

    private final Term _argument;

}; // class CompoundTerm
