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
 * Main representation of variables; suitable for unification, matching
 * and persistent storage; one global substitution is assumed that potentially
 * instantiates all variables.
 * <b>IMPORTANT:</b> Variable objects keep pointers to variable banks,
 *                   that keep pointers back to variables (directly
 *                   or via other variables). This creates cycles and
 *                   disables automatic garbage collection.
 *                   See logic.is.power.logic_warehouse.Variable.Bank.clear().
 */
public class Variable extends Symbol implements Term {

    /** Variable banks, ie, special clusters of variables. 
     *  <b>IMPORTANT:</b> Variable bank objects keep pointers to variables
     *                    that keep pointers back to the banks. This 
     *                    creates cycles and disables automatic garbage 
     *                    collection. See clear().
     */
    public static class Bank {
	
	/** <b>post:</b> the object can be garbage-collected until
	 *  the first call to reserveVariable().
	 */
	public Bank() {
	    _id = _nextId;
	    ++_nextId;
	    _allVariables = null;
	    _lastVariable = null;
	    _nextReservedVariable = null;
	    _nextVariableNumber = 0;
	}

	/** Unique numeric id of the bank object. */
	public final int id() { return _id; }

	/** Initiates a new round of variable reservations; all previous
	 *  reservations are annuled in the sense that the previously reserved 
	 *  variables become available for reservation again; however, 
	 *  the corresponding variable objects and the pointers to them 
	 *  remain valid.
	 *  <b>IMPORTANT:</b> <code>reset()</code> does not enable
	 *                    garbage collection on this object;
	 *                   see {@link #clear()}.
	 */
	public final void reset() { _nextReservedVariable = _allVariables; }

	/** Resets the object so that it can be garbage collected
	 *  before any new calls to {@link reserveVariable()}.
	 *  The method is relatively inefficient, so for a light-weight 
	 *  reset use {@link reset()}.
	 */
	public final void clear() {
	    _allVariables = null;
	    _lastVariable = null;
	    _nextReservedVariable = null;
	    _nextVariableNumber = 0;
	} // clear()

	/** Reserves a variable and returns a pointer to it. */
	public final Variable reserveVariable() {
	      
	    if (_nextReservedVariable == null) {
    
		if (_lastVariable == null) {

		    assert _allVariables == null;

		    _allVariables = new Variable(this,_nextVariableNumber);
      
		    ++_nextVariableNumber;
      
		    _lastVariable = _allVariables;

		    return _allVariables;
		}
		else // _lastVariable != null
		{
		    _lastVariable._nextVariableInBank = 
			new Variable(this,_nextVariableNumber);
      
		    ++_nextVariableNumber;

		    _lastVariable = _lastVariable._nextVariableInBank;

		    return _lastVariable;
		}

	    }
	    else // _nextReservedVariable != null
	    {
    
		Variable result = _nextReservedVariable;

		_nextReservedVariable = 
		    _nextReservedVariable._nextVariableInBank;
      
		return result;
	    }
    

	} // reserveVariable()  



	//                       Data:

	private static int _nextId = 0;

	private int _id;
    
	private Variable _allVariables;

	private Variable _lastVariable;
    
	private Variable _nextReservedVariable;

	private int _nextVariableNumber;

    } // class Bank




    //          Abstract in Symbol:

    public final String name() { return _name; }
    
    //         Methods prescribed by the interface Term:

    public final java.util.Iterator<Term> iterator() {
	return new Term.Iterator(this);
    }

    public final int kind() { return Term.Kind.Variable; }
        
    public final boolean isIndividualConstant() { return false; }
	
    public final boolean isIndividualValued() { return true; }
	    
    public final boolean isPair() { return false; }

    public final boolean isFormula() { return false; }

    public final boolean isAbstraction() { return false; }

    /** @return <code>this</code> */
    public final Symbol topSymbol() { return this; }
	

    public final boolean equals(Term term) {
	return this == term;
    }

    public final boolean equalsModuloSubst2(Term term) {
	return this == term ||
	    isInstantiated2() &&
	    instance2().equalsModuloSubst2(term);
	// Note that the substitution is not applied to instance2().
    }

    public final boolean equalsModuloSubst3(Term term) {
	return 
	    (isInstantiated3())? 
	    instance3().equals(term)
	    :
	    (this == term); 
    }

    public final boolean equals(Flatterm flatterm) { 
	return flatterm.isVariable() && 
	    this == flatterm.variable();
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


    public final boolean containsAsProperSubtermModuloSubst2(Term term) {
	
	return isInstantiated2() &&
	    instance2().containsAsProperSubtermModuloSubst2(term);
	// Note that the substitution is not applied to instance2().
    }

    public final boolean containsAsProperSubtermModuloSubst3(Term term) {
	
	return isInstantiated3() &&
	    instance3().containsAsProperSubtermModuloSubst3(term);
	// Note that the substitution is not applied to instance3().
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

    public int mapPositionWithSubst1(int n) {
	assert n == 0;
	return 0;
    }


    public final void collectFreeVariables(Collection<Variable> result) {
	result.add(this);
    } 
    
    
    public final boolean containsFreeVariables() { 
	return true;
    }

    public final boolean containsVariables() { 
	return true;
    }

    public 
	final 
	boolean containsFreeVariables(Collection<Variable> exclusions) {
	return !exclusions.contains(this);
    }
    


    public final int depth() { return 0; }

    public final int numberOfSymbols() { return 1; }

    public final int numberOfSymbolsAfterSubst1() { 
	if (isInstantiated1()) 
	    return instance1().numberOfSymbolsAfterSubst1();
	return 1; 
    }

    
    public 
	final
	int 
	numberOfNonvariableSymbolsFromCategory(int category,
					       int modulus) {
	return 0;
    }

    public 
	final 
	void 
	addNumberOfNonvariableSymbolsFromCategories(int modulus,
						    int[] result) {
	// Nothing to do here.
    }

    public String toString(boolean closed) { return _name; }
    


    //                Variable-specific methods:



    public final Bank bank() { return _bank; }
    


    //          Instantiation wrt the global substitution 1:


    
    /** <b>pre:<\b> <code>!isInstantiated1() && instance != null</code>;
     *  <b>pre:<\b> <code>!FlattermInstance.contains(instance,this)</code>
     */
    public final void instantiate1(Flatterm instance) {
	assert !isInstantiated1();
	assert instance != null;

	assert !FlattermInstance.contains(instance,this);

	_instance1 = instance;
    }

    /** Discards the instantiation of this variable.
     *  <b>pre:<\b> <code>isInstantiated1()</code>.
     */
    public final void uninstantiate1() {
	assert isInstantiated1();
	_instance1 = null;
    }

    /** Immediate instance of the variable wrt the global substitution 1,
     *  ie, if the substitution is
     *  <code>[x := y,y := a]</code>, then <code>x->instance1()<code>
     *  returns <code>y</code>, not <code>a</code>. 
     *  Returns <code>null</code> if <code>!isInstantiated1()</code>.
     */
    public final Flatterm instance1() { return _instance1; }


    /** Recursively computed instance of the variable wrt the global 
     *  substitution 1, ie, if the substitution is
     *  <code>[x := y,y := z,z := a]</code>, then 
     *  <code>x->ultimateInstance1()<code>
     *  returns <code>a</code>, not <code>y</code> or <code>z</code>. 
     *  Returns <code>null</code> if <code>!isInstantiated1()</code>.
     */
    public final Flatterm ultimateInstance1() {
	if (_instance1 == null) return null;
	if (_instance1.isVariable() && 
	    _instance1.variable().isInstantiated1())
	    return _instance1.variable().ultimateInstance1();
	return _instance1;
    }

    /** Checks if the variable is instantiated by the global substitution 1. */
    public final boolean isInstantiated1() { return _instance1 != null; }




    //          Instantiation wrt the global substitution 2:


    
    /** <b>pre:<\b> <code>!isInstantiated2() && instance != null</code>;
     *  <b>pre:<\b> unlike in {@link #instantiate1()},
     *              it is not necessary that 
     *              <code>!FlattermInstance.contains(instance,this)</code>.
     */
    public final void instantiate2(Flatterm instance) {
	assert !isInstantiated2();
	assert instance != null;

	_instance2 = instance;
    }

    /** Discards the instantiation of this variable.
     *  <b>pre:<\b> <code>isInstantiated2()</code>.
     */
    public final void uninstantiate2() {
	assert isInstantiated2();
	_instance2 = null;
    }

    /** Immediate instance of the variable wrt the global substitution 2,
     *  ie, if the substitution is
     *  <code>[x := y,y := a]</code>, then <code>x->instance2()<code>
     *  returns <code>y</code>, not <code>a</code>. 
     *  Returns <code>null</code> if <code>!isInstantiated2()</code>.
     */
    public final Flatterm instance2() { return _instance2; }


    // Note that we don't have ultimateInstance2() because 
    // we do not consider global substitution 2 transitively.

    /** Checks if the variable is instantiated by the global substitution 2. */
    public final boolean isInstantiated2() { return _instance2 != null; }







    //          Instantiation wrt the global substitution 3:


    
    /** <b>pre:<\b> <code>!isInstantiated3() && instance != null</code>;
     *  <b>pre:<\b> unlike in {@link #instantiate1()},
     *              it is not necessary that 
     *              <code>instance</code>, or any of its instances
     *              wrt global substitutions, can contain 
     *              the variable <code>this</code>.
     */
    public final void instantiate3(Term instance) {
	assert !isInstantiated3();
	assert instance != null;

	_instance3 = instance;
    }

    /** Discards the instantiation of this variable.
     *  <b>pre:<\b> <code>isInstantiated3()</code>.
     */
    public final void uninstantiate3() {
	assert isInstantiated3();
	_instance3 = null;
    }

    /** Immediate instance of the variable wrt the global substitution 3,
     *  ie, if the substitution is
     *  <code>[x := y,y := a]</code>, then <code>x->instance3()<code>
     *  returns <code>y</code>, not <code>a</code>. 
     *  Returns <code>null</code> if <code>!isInstantiated3()</code>.
     */
    public final Term instance3() { return _instance3; }


    // Note that we don't have ultimateInstance3() because 
    // we do not consider global substitution 3 transitively.

    /** Checks if the variable is instantiated by the global substitution 3. */
    public final boolean isInstantiated3() { return _instance3 != null; }






    //public final int hashCode() {
//	return _hashCode; // Must be efficient!
   // }

    //               Package access methods:

    Variable(Bank bank,int numberInBank) {
	super(Symbol.Category.Variable);
	_name = "X" + bank.id() + "_" + numberInBank;
	_bank = bank;
	_numberInBank = numberInBank;
	_nextVariableInBank = null; 
        _instance1 = null;
        _instance2 = null;
        _instance3 = null;
	// = _name.hashCode();
    }

    //               Data:


    private final String _name;

    /** Variable bank associated with this object. */
    private final Bank _bank;

    /** Uniquely identifies this variable among other variables from
     *  the same bank. 
     */ 
    private final int _numberInBank;

    /** Allows to arrange all variables of a bank in a linked list. */
    private Variable _nextVariableInBank;

    /** Instance of the variable wrt the global substitution 1. */
    private Flatterm _instance1;

    /** Instance of the variable wrt the global substitution 2. */
    private Flatterm _instance2;

    /** Instance of the variable wrt the global substitution 3. */
    private Term _instance3;

    //private int _hashCode;

}; // class Symbol

