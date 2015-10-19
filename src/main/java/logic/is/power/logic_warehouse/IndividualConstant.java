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

public class IndividualConstant extends FunctionalSymbol implements Term {


    //          Abstract in Symbol:

    public final String name() { return _name; }
    
    //         Methods prescribed by the interface Term:

    public final java.util.Iterator<Term> iterator() {
	return new Term.Iterator(this);
    }
    
    public final int kind() { return Term.Kind.IndividualConstant; }
    
        
    public final boolean isIndividualConstant() { return true; }
	
    public final boolean isIndividualValued() { return true; }
	    
    public final boolean isPair() { return false; }

    public final boolean isFormula() { return false; }

    public final boolean isAbstraction() { return false; }

    /** @return <code>this</code> */
    public final Symbol topSymbol() { return this; }
   

    public final boolean equals(Term term) { 
	return term.kind() == Term.Kind.IndividualConstant &&
	    _name.equals(((IndividualConstant)term)._name);
    }

    public final boolean equalsModuloSubst2(Term term) { 
	return 
	    (term.kind() == Term.Kind.IndividualConstant &&
	     _name.equals(((IndividualConstant)term)._name)) ||
	    (term.isVariable() &&
	     ((Variable)term).isInstantiated2() &&
	     ((Variable)term).instance2().isIndividualConstant() &&
	     equals(((Variable)term).instance2().individualConstant()));
    }


    public final boolean equalsModuloSubst3(Term term) { 
	return 
	    term.kind() == Term.Kind.IndividualConstant &&
	    _name.equals(((IndividualConstant)term)._name);
    }



    public final boolean equals(Flatterm flatterm) {
	return flatterm.isIndividualConstant() &&
	    this == flatterm.individualConstant();
    }

    public
	final  
	boolean 
	containsVariableAsProperSubterm(Variable var) {
	return false;
    }

    public final boolean containsAsProperSubterm(Term term) {
	return false;
    }

    public final boolean containsAsProperSubtermModuloSubst2(Term term)  {
	return false;
    }

    public final boolean containsAsProperSubtermModuloSubst3(Term term)  {
	return false;
    }

    public final Term subtermInPosition(int n) {
	assert n == 0;
	return this;
    }

    
    public final int depthOfPosition(int n) {
	assert n == 0;
	return 0;
    }


    public final int positionOfSubterm(Term term) {
	assert !term.isPair();
	if (this == term) return 0;
	return -1;
    }

    public final int mapPositionWithSubst1(int n) {
	assert n == 0;
	return 0;
    }

    public final void collectFreeVariables(Collection<Variable> result) {
	// nothing here
    }

    public final boolean containsFreeVariables() { return false; }

    public final boolean containsVariables() { return false; }

    public 
	final 
	boolean containsFreeVariables(Collection<Variable> exclusions) {
	return false; 
    }

    public final int depth() { return 0; }

    public final int numberOfSymbols() { return 1; }

    public final int numberOfSymbolsAfterSubst1() { return 1; }

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
	    (category(modulus) == category)? 1 : 0;
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
	result[this.category(modulus)] += 1;
	
    } // addNumberOfNonvariableSymbolsFromCategories(int modulus,
    

    public String toString(boolean closed) { return _name; }

    //public final int hashCode() {
//	return _name.hashCode();
  //  } 
	

    //               Package access methods:


    /** Note that this constructor has package access; objects of this class
     *  can only be created/destroyed inside Signature.
     */
    IndividualConstant(String name) {
	super();
	_name = name;
    }
    
    //               Data:


    private final String _name;

}; // class IndividualConstant

