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

import java.util.LinkedList;


/**
 * Common base for classes representing different kinds of terms: 
 * variables, individual constants, compound terms, atomic and
 * complex formulas. 
 */
public interface Term extends Iterable<Term> {

    public static class Kind {

	public static final int Variable = 0;
      
	public static final int CompoundTerm = 1;       
      
	public static final int IndividualConstant = 2;

	public static final int AtomicFormula = 3;       // includes propositional symbols
      
	public static final int ConnectiveApplication = 4;

	public static final int QuantifierApplication = 5;

	public static final int AbstractionTerm = 6;     // in forall x.F(x), x.F(x) is an abstraction


	public static final int TermPair = 7;            // pair constructor is used to construct tuples 
	                                                 // of arguments in compound terms and atomic formulas
    }; // class Kind


    /** Iteration over subterms of a term including all term pairs 
     *  (cf {@link logic.is.power.logic_warehouse.Term.LeanIterator}). 
     */
    public static class Iterator implements java.util.Iterator<Term> {

	/** <b>post:</b> the iterator cannot be used without a call to reset(Term term). */
	public Iterator() {
	    _currentSubterm = null;
	    _continuations = null;
	}

	/** <b>post:</b> the iterator is in the top position in <b>term</b>;
	 *  in particular, hasNext() && next() == <b>term</b>.
	 */
	public Iterator(Term term) {
	    _currentSubterm = term;
	    _continuations = new LinkedList<Term>();
	}

	/** <b>post:</b> the iterator is in the top position in <b>term</b>;
	 *  in particular, hasNext() && next() == <b>term</b>.
	 */
	public final void reset(Term term){
	    _currentSubterm = term;
	    _continuations = new LinkedList<Term>();
	}

	/** Returns true if there are more subterms. */
	public final boolean hasNext() {
	    assert _currentSubterm != null || 
		(_continuations == null || _continuations.isEmpty());
	    return _currentSubterm != null;
	}
	
	/** Returns the next subterm and advances the position.
	 *  Throws {@link java.util.NoSuchElementException} if 
	 *  <code>hasNext() == false</code>.
	 */
	public final Term next() throws java.util.NoSuchElementException {
	    
	    if (_currentSubterm == null) 
		throw new java.util.NoSuchElementException();

	    Term result = _currentSubterm;

	    switch (_currentSubterm.kind())
	    {
		
		case Kind.Variable:
		    if (_continuations.isEmpty()) 
		    {
			_currentSubterm = null;
		    }
		    else
			_currentSubterm = _continuations.removeFirst();
		    _remainder = 0;
		    break;
		    
		case Kind.CompoundTerm:
		    _currentSubterm = ((CompoundTerm)_currentSubterm).argument();
		    _remainder = 1;
		    break;
	
		case Kind.IndividualConstant:
		    if (_continuations.isEmpty()) 
		    {
			_currentSubterm = null;
		    }
		    else
			_currentSubterm = _continuations.removeFirst();
		    _remainder = 0;
		    break;
		    
		case Kind.AtomicFormula:
		    _currentSubterm = ((AtomicFormula)_currentSubterm).argument();    
		    if (_currentSubterm == null) 
			{
			    if (!_continuations.isEmpty())
				_currentSubterm = _continuations.removeFirst();
			    _remainder = 0;
			}
		    else
			_remainder = 1;
		    break;
		    
		case Kind.ConnectiveApplication:
		    _currentSubterm = ((ConnectiveApplication)_currentSubterm).argument(); 
		    _remainder = 1;
		    break;
		    
		case Kind.QuantifierApplication:
		    _currentSubterm = ((QuantifierApplication)_currentSubterm).abstraction(); 
		    _remainder = 1;
		    break;
		    
		case Kind.AbstractionTerm:
		    _currentSubterm = ((AbstractionTerm)_currentSubterm).matrix(); 
		    _remainder = 1;  
		    break;
		    
		case Kind.TermPair:
		    _continuations.addFirst(((TermPair)_currentSubterm).second());
		    _currentSubterm = ((TermPair)_currentSubterm).first(); 
		    _remainder = 2; 
		    break;
		    
		default:
		    assert false;
		    
	    }; // switch (_currentSubterm.kind())
	    	    
	    return result;
	    	    
	} // next()
	


	/** If the last call to <code>next()</code> returned <code>t</code>,
	 *  then this method skips all symbols in <code>t</code> completely;
	 *  eg, if <code>t = f(s1,s2)</code>, 
	 *  <code>skipSubtermRemainder()</code> will advance the iteration
	 *  to the end of <code>s2</code>; if <code>t</code> is a constant
	 *  or variable, then the state of the iterator does not change.
	 */
	public final void skipSubtermRemainder() {

	    if (_remainder != 0)
		{
		    if (_remainder == 2)
			{
			    assert !_continuations.isEmpty();
			    _continuations.removeFirst();
			};

		    if (_continuations.isEmpty()) 
			{
			    _currentSubterm = null;
			}
		    else
			_currentSubterm = _continuations.removeFirst();
		    _remainder = 0;
		};

	} // skipSubtermRemainder()

	
	/** Cannot be used. */
	public void remove() throws Error {
	    throw 
		new Error("Forbidden method call: logic.is.power.logic_warehouse.Term.Iterator.remove()");
	}

	
	//                   Data:

	Term _currentSubterm;

	LinkedList<Term> _continuations;

	/** Indicates which subterms have to be skipped when 
	 *  {@link #skipSubtermRemainder()} is called:
	 *  0 -> nothing needs to be done; 1 -> only _currentSubterm has
	 *  to be discarded; 2 -> _currentSubterm and the first 
	 *  element in _continuations have to be discarded.
	 */
	int _remainder;

    } // class Iterator






    /** Iteration over subterms of a term excluding any term pairs 
     *  (cf {@link logic.is.power.logic_warehouse.Term.Iterator}). Such iterators 
     *  are particularly 
     *  useful for iterating over symbols of a term.
     */
    public static class LeanIterator implements java.util.Iterator<Term> {

	/** <b>post:</b> the iterator cannot be used without a call to 
	 *  {@link #reset(Term)}. 
	 */
	public LeanIterator() {
	    _currentSubterm = null;
	    _continuations = null;
	}

	/** <b>pre:</b> <code>term</code> can be a term pair.
	 *  <b>post:</b> the iterator is in the top position in 
	 *  <code>term</code>;
	 *  in particular, <code>hasNext() && next() == term</code>.
	 */
	public LeanIterator(Term term) {
	    _currentSubterm = term;
	    _continuations = new LinkedList<Term>();
	}

	/** <b>pre:</b> <code>term</code> can be a term pair.
	 *  <b>post:</b> the iterator is in the top position in 
	 *  <code>term</code>;
	 *  in particular, <code>hasNext() && next() == term</code>.
	 */
	public final void reset(Term term){
	    _currentSubterm = term;
	    _continuations = new LinkedList<Term>();
	}

	/** Returns true if there are more subterms. */
	public final boolean hasNext() {
	    assert _currentSubterm != null || 
		(_continuations == null || _continuations.isEmpty());
	    return _currentSubterm != null;
	}
	
	/** Returns the next subterm which is not a term pair, 
	 *  and advances the position. Throws
	 *  {@link java.util.NoSuchElementException} 
	 *  if <code>hasNext() == false</code>.
	 */
	public final Term next() throws java.util.NoSuchElementException {

	    Term result;

	    do
	    { 
		result = nextSubterm();
	    }
	    while (result.isPair());

	    return result;
	}

	
	/** Cannot be used. */
	public void remove() throws Error {
	    throw 
		new Error("Forbidden method call: logic.is.power.logic_warehouse.Term.LeanIterator.remove()");
	}

	/** If the last call to <code>next()</code> returned <code>t</code>,
	 *  then this method skips all symbols in <code>t</code> completely;
	 *  eg, if <code>t = f(s1,s2)</code>, 
	 *  <code>skipSubtermRemainder()</code> will advance the iteration
	 *  to the end of <code>s2</code>; if <code>t</code> is a constant
	 *  or variable, then the state of the iterator does not change.
	 */
	public final void skipSubtermRemainder() {

	    if (_remainder == 1)
		{
		    if (_continuations.isEmpty()) 
			{
			    _currentSubterm = null;
			}
		    else
			_currentSubterm = _continuations.removeFirst();
		    _remainder = 0;
		};

	} // skipSubtermRemainder()



	//                          Private methods:

	
	/** Returns the next subterm (possibly a term pair) and advances 
	 *  the position. Throws
	 *  {@link java.util.NoSuchElementException} 
	 *  if <code>hasNext() == false</code>.
	 */
	private Term nextSubterm() throws java.util.NoSuchElementException {
	    
	    if (_currentSubterm == null) 
		throw new java.util.NoSuchElementException();

	    Term result = _currentSubterm;

	    switch (_currentSubterm.kind())
	    {
		
		case Kind.Variable:
		    if (_continuations.isEmpty()) 
		    {
			_currentSubterm = null;
		    }
		    else
			_currentSubterm = _continuations.removeFirst();
		    _remainder = 0;
		    break;
		    
		case Kind.CompoundTerm:
		    _currentSubterm = ((CompoundTerm)_currentSubterm).argument();
		    _remainder = 1;
		    break;
	
		case Kind.IndividualConstant:
		    if (_continuations.isEmpty()) 
		    {
			_currentSubterm = null;
		    }
		    else
			_currentSubterm = _continuations.removeFirst();
		    _remainder = 0;
		    break;
		    
		case Kind.AtomicFormula:
		    _currentSubterm = ((AtomicFormula)_currentSubterm).argument();    
		    if (_currentSubterm == null) 
			{
			    if (!_continuations.isEmpty())
			    _currentSubterm = _continuations.removeFirst();
			    _remainder = 0;
			}
		    else
			_remainder = 1;
		    break;
		    
		case Kind.ConnectiveApplication:
		    _currentSubterm = ((ConnectiveApplication)_currentSubterm).argument(); 
		    break;
		    
		case Kind.QuantifierApplication:
		    _currentSubterm = ((QuantifierApplication)_currentSubterm).abstraction();  
		    _remainder = 1;
		    break;
		    
		case Kind.AbstractionTerm:
		    _currentSubterm = ((AbstractionTerm)_currentSubterm).matrix();   
		    _remainder = 1; 
		    break;
		    
		case Kind.TermPair:
		    _continuations.addFirst(((TermPair)_currentSubterm).second());
		    _currentSubterm = ((TermPair)_currentSubterm).first(); 
		    // _remainder is irrelevant here
		    break;
		    
		default:
		    assert false;
		    
	    }; // switch (_currentSubterm.kind())
	    	    
	    return result;
	    	    
	} // nextSubterm()
	




	//                   Data:

	Term _currentSubterm;

	LinkedList<Term> _continuations;

	/** Indicates which subterms have to be skipped when 
	 *  {@link #skipSubtermRemainder()} is called:
	 *  0 -> nothing needs to be done; 1 ->  _currentSubterm has
	 *  to be discarded.
	 */
	int _remainder;

    } // class LeanIterator







    //                  Public methods of the interface Term:

    public int kind();
    
    public boolean isVariable();
        
    public boolean isIndividualConstant();
	
    /** True if and only if the term is a variable, constant or compound term
     *  (false if the term is a pair, abstraction or formula).
     */
    public boolean isIndividualValued();
	    
    public boolean isPair();

    public boolean isFormula();

    public boolean isAbstraction();

    /** Throws an {@link java.lang.Error} exception when applied to an 
     *  {@link logic.is.power.logic_warehouse.AbstractionTerm} or 
     *  a {@link logic.is.power.logic_warehouse.TermPair}.
     */
    public Symbol topSymbol();
	
	


     /** Simplest syntactic equality;
      *  <b>pre:</b> <code>term != null</code>
      */
    public boolean equals(Term term); 

     /** Syntactic equality on instances of the terms
      *  wrt global substitution 2.
      *  <b>pre:</b> <code>term != null</code>
      */
    public boolean equalsModuloSubst2(Term term);

     /** Syntactic equality between the instances of <code>this</code>
      *  wrt global substitution 3, and <code>term</code>.
      *  <b>pre:</b> <code>term != null</code>
      */
    public boolean equalsModuloSubst3(Term term);

     /** Simplest syntactic equality;
      *  <b>pre:</b> <code>flatterm != null</code>
      */
    public boolean equals(Flatterm flatterm);

    


    /** Checks if <code>var</code> occurs in this term
     *  as a proper subterm.
     */
    public 
	boolean 
	containsVariableAsProperSubterm(Variable var);


    public boolean containsAsProperSubterm(Term term);

    public boolean containsAsProperSubtermModuloSubst2(Term term);

    public boolean containsAsProperSubtermModuloSubst3(Term term);


    /** Subterm of this term in the position specified with 
     *  <code>n</code>; more precisely, this is the term 
     *  returned by the <code>n</code>-th call to 
     *  <code>LeanIterator.next()</code> on an iterator for this term
     *  (the counting starts with 0).
     *  <b>pre:</b> <code>n</code> must be a valid position in the term,
     *  ie, <code>0 <= n < numberOfSymbols()</code>.
     */
    public Term subtermInPosition(int n);


    /** Depth of (the subterm in) the specified position in 
     *  <code>this</code>; the depth can be zero if <code>n</code>=0.
     *  <b>pre:</b> <code>n</code> must be a valid position in the term,
     *  ie, <code>0 <= n < numberOfSymbols()</code>.
     */
    public int depthOfPosition(int n);


    /** If <code>term</code> is a subterm of <code>this</code>
     *  (as a substructure, not just as syntactically identical 
     *  to a subterm in <code>this</code>), then its first position
     *  is returned; otherwise -1 is returned.
     *  <b>pre:</b> <code>!term.isPair()</code>.
     */
    public int positionOfSubterm(Term term);
    
    
    /** Computes the position of the subterm corresponding 
     *  to <code>subtermInPosition(n)</code> in the instance
     *  of <code>this</code> under global substitution 1.
     */
    public int mapPositionWithSubst1(int n);


    /** Collects all free variables from 
     *  this term in the collection <code>result<\code>; note that result 
     *  is not emptied before the collection, so that what was in it
     *  also remains in it.
     */
    public void collectFreeVariables(Collection<Variable> result);

    /** Checks if the term contains at least one free variable. */
    public boolean containsFreeVariables();

    /** Checks if the term contains at least one variable, free or quantified. */
    public boolean containsVariables();

    /** Checks if the term contains at least one free variable not
     *  included in the collection <code>exclusions</code>.
     */
    public boolean containsFreeVariables(Collection<Variable> exclusions);

    /** Computes the depth of the term; the depth of a variable or constant 
     *  is 0;  all logic symbols and the abstraction operator add 1 to 
     *  the depth; pair constructors do not contribute to the depth count.
     */
    public int depth();

    /** Counts all symbols, including logic symbols and variables;
     *  pair constructors are not counted;
     *  an abstraction operator is counted as 1 symbol.
     */
    public int numberOfSymbols();


    /** Counts all symbols in the instance of <code>this</code>
     *  under global substitution 1, including logic symbols and variables;
     *  pair constructors are not counted;
     *  an abstraction operator is counted as 1 symbol.
     */
    public int numberOfSymbolsAfterSubst1();

    /** Counts all nonvariable symbols <code>sym</code>, 
     *  including logical symbols, but excluding pair constructors
     *  and abstraction operators, such that 
     *  <code>sym.category(modulus) == category</code>.
     */
    public 
	int 
	numberOfNonvariableSymbolsFromCategory(int category,
					       int modulus);

    /** Counts all nonvariable symbols <code>sym</code>, 
     *  including logical symbols, but excluding pair constructors
     *  and abstraction operators, such that 
     *  <code>sym.category(modulus) == category</code>,
     *  for every <code>category</code> in <code>[0,modulus - 1]</code>,
     *  and adds the numbers to the corresponding values in 
     *  <code>result[category]</code>.
     */
    public  
	void 
	addNumberOfNonvariableSymbolsFromCategories(int modulus,
						    int[] result);

    /** @param closed indicates whether grouping is required */
    public String toString(boolean closed);

}; // interface Term

