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

/** Encapsulates operations with flatterms modulo
 *  the global substitution
 */
public class FlattermInstance {

    /** Iteration over instances of flatterms modulo the dynamically
     *  changing global substitution, with a possibility of 
     *  backtracking to a specified savepoint.
     */
    public 
	static 
	class BacktrackableIterator 
	implements java.util.Iterator<Flatterm> {
	
	public BacktrackableIterator() {
	    _currentSubterm = null;
	    _endOfLevel = null;
	    _stackSize = 0;
	    _stackCapacity = 32; // initial capacity, may grow
	    _currentSubtermForBacktracking = 
		new Flatterm[_stackCapacity];
	    _endOfLevelForBacktracking = 
		new Flatterm[_stackCapacity];
	    _currentLevelForBacktracking = 
		new int[_stackCapacity];
	    _afterIsValid = false;
	}

	/** Releases all pointers to external objects. */
	public final void clear() {
	    _currentSubterm = null;
	    _endOfLevel = null;
	    while (_stackSize > 0)
		{
		    --_stackSize;
		    _currentSubtermForBacktracking[_stackSize] = null;
		    _endOfLevelForBacktracking[_stackSize] = null;
		};
	    _afterIsValid = false;
	}


	/** <b>pre:</b> <code>term</code> may be <code>null</code>,
	 *  in which case the iterator is already at the end.
	 *  <b>post:</b> otherwise, the iterator is in the top 
	 *  position in <b>term</b>;
	 *  in particular, hasNext() && next() == <b>term</b>.
	 */
	public final void reset(Flatterm term) {

	    //System.out.println("      RESET " + term);

	    _currentSubterm = term;
	    if (term == null)
		{
		    _endOfLevel = null;
		}
	    else
		_endOfLevel = term.after();

	    _stackSize = 0;	    
	    _afterIsValid = false;

	} // reset(Flatterm term)


	/** Returns true if there is a subterm following the current symbol. */
	public final boolean hasNext() {
	    assert _currentSubterm != null || _endOfLevel == null;
	    return _currentSubterm != _endOfLevel;
	}
	
	

	/** Returns the next subterm and advances the position.
	 *  Throws {@link java.util.NoSuchElementException} if 
	 *  <code>hasNext() == false</code>.
	 */
	public final Flatterm next() throws NoSuchElementException {

	    if (!hasNext()) 
		throw new NoSuchElementException();

	    // Save the position for backtracking:

	    //System.out.println("PUSH1 " + _stackSize + " -> " + _currentSubterm + " / " + _endOfLevel);

	    save();

	    Flatterm result;

	    if (_currentSubterm.isVariable() &&
		_currentSubterm.variable().isInstantiated1())
		{
		    // Go one level down:
		    result =
			_currentSubterm.variable().ultimateInstance1();
		    _currentSubterm = result.nextCell();
		    _endOfLevel = result.after();
		    ++_currentLevel;

		    //System.out.println("           ---- down " + _currentSubterm + " / " + _endOfLevel); 

		}
	    else
		{
		    result = _currentSubterm;
		    _currentSubterm = result.nextCell();
		    // And stay in the same level.

		    //System.out.println("           ---- same level " + _endOfLevel); 
		    
		};
	    
	    _after = result.after();


	    // If this is the end of the current level, try to go 
	    // to a higher level wrt the substitution:

	    int i = _stackSize - 1;
	    

	    while (_currentSubterm == _endOfLevel && 
		   i >= 0)
		{
		    // Trying to get to a higher level 
		    // wrt the substitution:
		    while (i >= 0 && 
			   _currentLevelForBacktracking[i] >= _currentLevel) 
			--i;	    
		    

		    if (i >= 0)
			{
			    assert
				_currentLevelForBacktracking[i] == 
				_currentLevel - 1;

			    //System.out.println("   ***** diferent level " + _subtermsForBacktracking.get(i + 1));

			    _currentSubterm = 
				_currentSubtermForBacktracking[i];
			    
			    //System.out.println("CHECK " + i + " -> " + _currentSubterm);

			    assert _currentSubterm.isVariable();
			    assert _currentSubterm.variable().isInstantiated1();
			    assert 
				_currentSubterm.
				variable().ultimateInstance1().after() ==
				_endOfLevel;

			    _after = _currentSubterm.nextCell(); 
			    assert _after == _currentSubterm.after();
			    // Because a variable cannot have arguments

			    _currentSubterm = _currentSubterm.nextCell();
			    _endOfLevel = _endOfLevelForBacktracking[i];
			    _currentLevel = _currentLevelForBacktracking[i];
			};
		}; // while (_currentSubterm == _endOfLevel && i >= 0)

	    // It's still possible here that 
	    // _currentSubterm == _endOfLevel :
	    // this indicates a complete end of iteration.

	    
	    _afterIsValid = true;

	    return result;
	    
	} // next()


	
	/** Cannot be used. */
	public void remove() throws Error {
	    throw 
		new Error("Forbidden method call: logic_warehouse_je.FlattermInstance.remove()");
	}



	/** Advances the iteration to the end of the last term 
	 *  returned with {@link #next()}; works even if the term 
	 *  has zero arguments, i.e., if it is a variable.
	 *  <b>pre:</b> {@link #next()} must have been called since 
	 *  the last reset or backtrack.
	 */
	public final void skipArguments() {
	    
	    assert _afterIsValid;
	      
	    // Save the position for backtracking:

	    //System.out.println("PUSH2 " + _stackSize + " -> " + _currentSubterm + " / " + _endOfLevel);

	    save();
	    
	    _currentSubterm = _after;
	    _afterIsValid = false;

	    // If this is the end of the current level, try to go 
	    // to a higher level wrt the substitution:

	    int i = _stackSize - 1;

	    while (_currentSubterm == _endOfLevel && 
		   i >= 0)
		{
		    // Trying to get to a higher level 
		    // wrt the substitution:
		    while (i >= 0 && 
			   _currentLevelForBacktracking[i] >= _currentLevel) 
			--i;	    

		    if (i >= 0)
			{
			    assert
				_currentLevelForBacktracking[i] == 
				_currentLevel - 1;

			    
			    _currentSubterm = 
				_currentSubtermForBacktracking[i];
			    
			    assert _currentSubterm.isVariable();
			    assert _currentSubterm.variable().isInstantiated1();
			    assert 
				_currentSubterm.
				variable().ultimateInstance1().after() ==
				_endOfLevel;
			    
			    _currentSubterm = _currentSubterm.nextCell();
			    _endOfLevel = _endOfLevelForBacktracking[i];
			    _currentLevel = _currentLevelForBacktracking[i];
			};
		}; // while (_currentSubterm == _endOfLevel && i >= 0)

	    // It's still possible here that 
	    // _currentSubterm == _endOfLevel :
	    // this indicates a complete end of iteration.

	} // void skipArguments()




	/** Identifies the current state of the iteration;
	 *  can be used if we later want to backtrack to this state.
	 *  The value becomes invalid and should not be used 
	 *  after any longer backtrack.
	 */
	public final int savepoint() { return _stackSize; }


	/** Cancels the effects of all calls to {@link #next()}
	 *  and {@link #skipArguments()}
	 *  made after the savepoint <code>savepoint<\code> was made.
	 *  <b>pre:</b> <code>savepoint<\code> must be a valid 
	 *  savepoint for this iterator, ie, it must have been 
	 *  obtained by a {@link #savepoint()} call on this object, 
	 *  and there must have been no backtracks farther than 
	 *  <code>savepoint<\code> since that call to 
	 *  {@link #savepoint()}.
	 *  <b>post:</b> <code>savepoint() == savepoint<\code>
	 */
	public final void backtrackTo(int savepoint) {
	    
	    assert savepoint >= 0;
	    assert savepoint <= _stackSize;

	    _afterIsValid = false;
	    
	    if (savepoint == _stackSize) return;

	    while (_stackSize > savepoint + 1)
		{
		    forget();
		};
	    
	    assert _stackSize == savepoint + 1;
	    
	    restore();

	    
	    //System.out.println("BACKTRACK " + _currentSubterm + " / " + _endOfLevel);


	} // backtrackTo(int savepoint)


	/** Cancels the effect of the last call to {@link #next()} 
	 *  or {@link skipArguments()}.
	 */ 
	public final void backtrack() {
	    
	    assert _stackSize > 0;

	    _afterIsValid = false;
	    
	    restore();

	} // void backtrack() 


	//  
	//          Private methods:
	// 

	/** Saves _currentSubterm and _endOfLevel 
	 *  on the stack.
	 */
	private void save() {

	    assert _stackSize < _stackCapacity;

	    if (_stackSize + 1 == _stackCapacity)
		{
		    // Increment the stack capacity:
		    
		    int newStackCapacity = _stackCapacity + 32;
		    
		    Flatterm[] newCurrentSubtermForBacktracking = 
			new Flatterm[newStackCapacity];
		    Flatterm[] newLastCellOfLevelForBacktracking =
			new Flatterm[newStackCapacity];
		    int[] newCurrentLevelForBacktracking = 
			new int[newStackCapacity];
			
		    for (int n = 0; n < _stackSize; ++n)
			{
			    newCurrentSubtermForBacktracking[n] = 
				_currentSubtermForBacktracking[n];
			    newLastCellOfLevelForBacktracking[n] = 
				_endOfLevelForBacktracking[n];
			    newCurrentLevelForBacktracking[n] =
				_currentLevelForBacktracking[n];
			};

		    _stackCapacity = newStackCapacity;
		    _currentSubtermForBacktracking = 
			newCurrentSubtermForBacktracking;
		    _endOfLevelForBacktracking =
			newLastCellOfLevelForBacktracking;
		    _currentLevelForBacktracking =
			newCurrentLevelForBacktracking;
		}; // if (_stackSize >= _stackCapacity)

	    _currentSubtermForBacktracking[_stackSize] =
		_currentSubterm;
	    
	    _endOfLevelForBacktracking[_stackSize] =
		_endOfLevel;

	    _currentLevelForBacktracking[_stackSize] =
		_currentLevel;

	    ++_stackSize;

	} // save()



	/** Restore _currentSubterm and _endOfLevel 
	 *  on the stack.
	 */
	private void restore() {

	    assert _stackSize > 0;
	    
	    --_stackSize;

	    _currentSubterm = 
		_currentSubtermForBacktracking[_stackSize];
	    _endOfLevel = 
		_endOfLevelForBacktracking[_stackSize];
	    _currentLevel = 
		_currentLevelForBacktracking[_stackSize];

	    // Release the pointers:
	    _currentSubtermForBacktracking[_stackSize] = null;
	    _endOfLevelForBacktracking[_stackSize] = null;

	} // restore()
	

	/** Pops the stacks without restoring  
	 *  _currentSubterm and _endOfLevel.
	 */
	private void forget() {

	    assert _stackSize > 0;
	    
	    --_stackSize;

	    // Release the pointers:
	    _currentSubtermForBacktracking[_stackSize] = null;
	    _endOfLevelForBacktracking[_stackSize] = null;

	} // restore()
	


	


	//  
	//          Private data:
	// 

	private Flatterm _currentSubterm;
	
	private Flatterm _endOfLevel;

	/** Current depth wrt the global substitution;
	 *  following an instantiation increses this by 1.
	 */
	private int _currentLevel;



	/** Current size of the stacks for backtracking. */
	private int _stackSize;
	
	/** Current size of the arrays used for the stacks. */
	private int _stackCapacity;

	/** Stack for saving _currentSubterm for backtracking. */
	private Flatterm[] _currentSubtermForBacktracking;

	/** Stack for saving _endOfLevel for backtracking. */
	private Flatterm[] _endOfLevelForBacktracking;
	
	/** Stack for saving _currentLevel for backtracking. */
	private int[] _currentLevelForBacktracking;
	

	/** Indicates if the value of _after is valid and, therefore,
	 *  a call to {@link #skipArguments()} is possible; _afterIsValid == true
	 *  if there has been a call to {@link #next()} not followed
	 *  by {@link #reset()} or {@link #backtrack()} or {@link #backtrackTo()}
	 * {@link #skipArguments()} or {@link #clear()}.
	 */
	private boolean _afterIsValid;

	/** When <code>_afterIsValid == true</code>, <code>_after</code> 
	 *  points at the end of the term returned with the last
	 *  call to {@link #next()}.
	 */
	private Flatterm _after;

    } // class BacktrackableIterator







    /** Checks if the term, taken modulo the global substitution,
     *  contains the uninstantiated variable <code>var</code>.
     *  @param var must satisfy !var.isInstantiated1()
     *  <b>pre:</b> this term cannot contain quantifiers or abstractions
     */
    public 
	static 
	boolean contains(Flatterm term,Variable var) {

	assert !var.isInstantiated1();

	switch (term.kind())
	{
	case Term.Kind.Variable: 
		if (term.variable() == var) return true;
	  
		return term.variable().isInstantiated1() &&
		    contains(term.variable().instance1(),var);

	    case Term.Kind.CompoundTerm:
	    {
		Flatterm arg = term.nextCell();
		for (int n = 0; n < term.function().arity(); ++n)
		{
		    if (contains(arg,var)) return true;
		    arg = arg.after();
		};
		return false;
	    }

	    case Term.Kind.IndividualConstant: return false;

	    case Term.Kind.AtomicFormula: 
	    {
		Flatterm arg = term.nextCell();
		for (int n = 0; n < term.predicate().arity(); ++n)
		{
		    if (contains(arg,var)) return true;
		    arg = arg.after();
		};
		return false;
	    }

	    case Term.Kind.ConnectiveApplication:
	    {

		for (Flatterm arg = term.nextCell();
		     arg != term.after(); 
		     arg = arg.after())
		{
		    if (contains(arg,var)) return true;
		    arg = arg.after();
		};
		    
		return false;

	    }

	    case Term.Kind.QuantifierApplication: // as below
	    case Term.Kind.AbstractionTerm: 
		assert false;
		return false;

	}; // switch (kind())

	assert false;
	return false;

    } // contains(Flatterm term,Variable var)


    


    /** Checks syntactic equality between the terms
     *  modulo the global substitution.
     *  <b>pre:</b> terms cannot contain quantifier applications or 
     *       abstractions; either both terms are formulas,
     *       or both are individual-valued
     */
    public static boolean equals(Flatterm term1,Flatterm term2) {

	assert !term1.isFormula() || term2.isFormula();
	assert term1.isFormula() || !term2.isFormula();
	assert term1.kind() != Term.Kind.QuantifierApplication;
	assert term1.kind() != Term.Kind.AbstractionTerm;
	assert term2.kind() != Term.Kind.QuantifierApplication;
	assert term2.kind() != Term.Kind.AbstractionTerm;


	if (term1 == term2) return true;

	switch (term1.kind())
	{
	    case Term.Kind.Variable: 
		if (term1.variable().isInstantiated1())
		    return equals(term1.variable().instance1(),
				  term2);
	      
		return term2.isVariable() &&
		    (term1.variable() == term2.variable() ||
		     (term2.variable().isInstantiated1() &&
		      equals(term1,term2.variable().instance1())));
	  

	    case Term.Kind.CompoundTerm:
	    {
		if (term2.isVariable())
		{
		    return term2.variable().isInstantiated1() &&
			equals(term1,term2.variable().instance1());
		};

		if (!term2.isCompound() || 
		    term1.function() != term2.function())
		    return false;

		Flatterm arg1 = term1.nextCell();
		Flatterm arg2 = term2.nextCell();
		for (int n = 0; n < term1.function().arity(); ++n)
		{
		    if (!equals(arg1,arg2)) return false;
		    arg1 = arg1.after();
		    arg2 = arg2.after();
		};
		return true;
	    }

	    case Term.Kind.IndividualConstant: 	  
		if (term2.isVariable())
		    return term2.variable().isInstantiated1() &&
			equals(term1,term2.variable().instance1());

		return term2.isIndividualConstant() &&
		    term1.individualConstant() == term2.individualConstant();


	    case Term.Kind.AtomicFormula: 
	    {
		if (!term2.isAtomicFormula() ||
		    term1.predicate() != term2.predicate())
		    return false;

		Flatterm arg1 = term1.nextCell();
		Flatterm arg2 = term2.nextCell();
		for (int n = 0; n < term1.predicate().arity(); ++n)
		{
		    if (!equals(arg1,arg2)) return false;
		    arg1 = arg1.after();
		    arg2 = arg2.after();
		};
		return true;
	    }


	    case Term.Kind.ConnectiveApplication:
	    {
		if (!term2.isConnectiveApplication() || 
		    term1.connective() != term2.connective())
		    return false;
		
		Flatterm arg1 = term1.nextCell();
		Flatterm arg2 = term2.nextCell();

		do
		{
		    if (!equals(arg1,arg2)) return false;
		    arg1 = arg1.after();
		    arg2 = arg2.after();
		}
		while (arg1 != term1.after());
		
	        assert arg1 == term1.after() && arg2 == term2.after();

		return true;
	    }

	    case Term.Kind.QuantifierApplication: // as below
	    case Term.Kind.AbstractionTerm:
		assert false;
		return false;

	}; // switch (kind())

	assert false;
	return false;
  
    } // equals(Flatterm term1,Flatterm term2)


    

} // class FlattermInstance