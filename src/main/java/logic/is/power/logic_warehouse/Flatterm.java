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
 * List-based flatterms representing both formulas and individual terms; 
 * instances of the class represent building cells of flatterms.
 */
public class Flatterm {
    
    /** Does not really initialise the cell as a part of a term 
     *  representation. 
     */
    public Flatterm() {
    }
    

    public static Flatterm newVariableCell(Variable var) {
	Flatterm result = new Flatterm();
	result._kindTag = Term.Kind.Variable;
	result._symbol = var;
	return result;
    } 

    public static Flatterm newCompoundTermCell(Function func) {
	Flatterm result = new Flatterm();
	result._kindTag = Term.Kind.CompoundTerm;
	result._symbol = func;
	return result;
    } 

    public static Flatterm newIndividualConstantCell(IndividualConstant c) {
	Flatterm result = new Flatterm();
	result._kindTag = Term.Kind.IndividualConstant;
	result._symbol = c;
	return result;
    } 

    public static Flatterm newAtomicFormulaCell(Predicate pred) {
	Flatterm result = new Flatterm();
	result._kindTag = Term.Kind.AtomicFormula;
	result._symbol = pred;
	return result;
    } 

    public static Flatterm newConnectiveApplicationCell(Connective con) {
	Flatterm result = new Flatterm();
	result._kindTag = Term.Kind.ConnectiveApplication;
	result._symbol = con;
	return result;
    } 

    public static Flatterm newQuantifierApplicationCell(Quantifier quant) {
	Flatterm result = new Flatterm();
	result._kindTag = Term.Kind.QuantifierApplication;
	result._symbol = quant;
	return result;
    } 

    public static Flatterm newAbstractionCell(Variable var) {
	Flatterm result = new Flatterm();
	result._kindTag = Term.Kind.AbstractionTerm;
	result._symbol = var;
	return result;
    } 
    

    public final int kind() { return _kindTag; }

    public final boolean isVariable() {
	return _kindTag == Term.Kind.Variable;
    } 


    public final boolean isCompound() {
	return _kindTag == Term.Kind.CompoundTerm;
    } 
    

    public final boolean isIndividualConstant() {
	return _kindTag == Term.Kind.IndividualConstant;
    } 


    public final boolean isAtomicFormula() {
	return _kindTag == Term.Kind.AtomicFormula;
    } 

    public final boolean isConnectiveApplication() {
	return _kindTag == Term.Kind.ConnectiveApplication;
    } 

    public final boolean isQuantifierApplication() {
	return _kindTag == Term.Kind.QuantifierApplication;
    } 

    public final boolean isAbstraction() {
	return _kindTag == Term.Kind.AbstractionTerm;
    } 

    /** @return <code>isAtomicFormula() || isConnectiveApplication() || 
     *	  isQuantifierApplication()</code>.
     */
    public final boolean isFormula() {
	return isAtomicFormula() || 
	    isConnectiveApplication() || 
	    isQuantifierApplication();
    }

    /** @return <code>isAtomicFormula() ||
     *                (isConnectiveApplication() &&
     *	               connective().id() == Connective.Id.Not &&
     *	               nextCell().isAtomicFormula())</code>.
     */
    public final boolean isSimpleLiteral() {
	return isAtomicFormula() ||
	    (isConnectiveApplication() &&
	     connective().id() == Connective.Id.Not &&
	     nextCell().isAtomicFormula());
    }

    /** @return <code>isSimpleLiteral() && 
     *                atom().predicate().isEquality()</code>.
     */
    public final boolean isEqualityLiteral() {
	return isSimpleLiteral() &&
	    atom().predicate().isEquality();
    }


    /** Pointer to the next cell; this can be (i) next cell in the
     *  term that starts with this cell, or (ii) first cell in another
     *  subterm of a larger flatterm, eg, if this cell is a complete term,
     *  or (iii) null, if no term follows this one.
     */
    public final Flatterm nextCell() { return _nextCell; }
    
    /** <b> post: </b> <code>getNextCell() == nextCell</code>. */
    public final void setNextCell(Flatterm nextCell) {
	_nextCell = nextCell;
    }
    

    /** Pointer to the last cell in the representation of the term
     *  that starts with this cell; cannot be null in a fully-formed term.
     */
    public final Flatterm lastCell() {
	if (_lastCell == null) return this;
	return _lastCell;
    }

    /** <b>post:</b> <code>getLastCell() == lastCell</code>. */
    public final void setLastCell(Flatterm lastCell) {
	if (lastCell == this) 
	{
	    _lastCell = null;
	}
	else
	    _lastCell = lastCell;
    } 

    /** Pointer to the term that follows the term starting with this cell
     *  in a larger flatterm; computed as <code>lastCell().nextCell()</code>. 
     *  <b>pre:</b> <code>getLastCell() != null</code>. 
     */
    public final Flatterm after() {
	return lastCell().nextCell();
    } 


    public final Symbol symbol() { return _symbol; }

    /** <b>pre:</b> <code>kind() == Term.Kind.Variable || 
     *     kind() == Term.Kind.AbstractionTerm</code>.
     */
    public final Variable variable() {
	assert _kindTag == Term.Kind.Variable ||
	    _kindTag == Term.Kind.AbstractionTerm;
	return (Variable)_symbol;
    } 

    /** <b>pre:</b> <code>kind() == Term.Kind.CompoundTerm</code>. */
    public final Function function() {
	assert _kindTag == Term.Kind.CompoundTerm;
	return (Function)_symbol;
    } 

    /** <b>pre:</b> <code>kind() == Term.Kind.IndividualConstant</code>. */
    public final IndividualConstant individualConstant() {
	assert _kindTag == Term.Kind.IndividualConstant;
	return (IndividualConstant)_symbol;
    } 

    /** <b>pre:</b> <code>kind() == Term::AtomicFormula</code>. */
    public final Predicate predicate() {
	assert _kindTag == Term.Kind.AtomicFormula;
	return (Predicate)_symbol;
    } 

    /** <b>pre:</b> <code>kind() == Term::ConnectiveApplication</code>. */
    public final Connective connective() {
	assert _kindTag == Term.Kind.ConnectiveApplication;
	return (Connective)_symbol;
    } 

    /** <b>pre:</b> <code>kind() == Term::QuantifierApplication</code>. */
    public final Quantifier quantifier() {
	assert _kindTag == Term.Kind.QuantifierApplication;
	return (Quantifier)_symbol;
    } 

    /** @return isConnectiveApplication() && 
     *  connective().id() == Connective.Id.Not
     */
    public final boolean isNegative() {
	return isConnectiveApplication() && 
	    connective().id() == Connective.Id.Not;
    }

    /** @return !isNegative() */
    public final boolean isPositive() { return !isNegative(); }

    /** Removes the negation, if there is one. */
    public final Flatterm atom() { 
	if (isConnectiveApplication() && 
	    connective().id() == Connective.Id.Not)
	    return nextCell();
	return this;
    }

      /** <b>pre:</b> <code>kind() == Term.Kind.CompoundTerm ||
       *       (kind() == Term.Kind.AtomicFormula &&
       *        predicate().arity() > 0) ||
       *       (kind() == Term.Kind.ConnectiveApplication &&
       *        connective().id() != Connective.Id.Not)</code>.
       */
      public final Flatterm firstArg() {
	  assert  kind() == Term.Kind.CompoundTerm ||
              (kind() == Term.Kind.AtomicFormula &&
               predicate().arity() > 0) ||
              (kind() == Term.Kind.ConnectiveApplication &&
               connective().id() != Connective.Id.Not);
	  return nextCell();
      }  


    /** <b>pre:</b> <code>(kind() == Term.Kind.CompoundTerm && 
     *        function().arity() > 1) ||
     *       (kind() == Term.Kind.AtomicFormula &&
     *        predicate().arity() > 1) ||
     *       (kind() == Term.Kind.ConnectiveApplication &&
     *        connective().id() != Connective.Id.Not)</code>.
     */
    public final Flatterm secondArg() {
	assert (kind() == Term.Kind.CompoundTerm && 
		function().arity() > 1) ||
            (kind() == Term.Kind.AtomicFormula &&
             predicate().arity() > 1) ||
            (kind() == Term.Kind.ConnectiveApplication &&
             connective().id() != Connective.Id.Not); 
	return nextCell().after();
    }  


    /** Hash code of the whole term that starts with this cell. 
     *  See also  
     *  {@link logic.is.power.logic_warehouse.AbstractionTerm#hashCode()},
     *  {@link logic.is.power.logic_warehouse.CompoundTerm#hashCode()}, 
     *  {@link logic.is.power.logic_warehouse.AtomicFormula#hashCode()},
     *  {@link logic.is.power.logic_warehouse.ConnectiveApplication#hashCode()},
     *  {@link logic.is.power.logic_warehouse.QuantifierApplication#hashCode()}.
     */
    public final int hashCode() {

	switch (kind())
	{
	    case Term.Kind.Variable: 
		// Must correspond to Term.hashCode()
		return variable().hashCode();

	    case Term.Kind.CompoundTerm:   
		// Must correspond to CompoundTerm.hashCode()
		return function().hashCode() * 5 + 
		    nextCell().hashCodeOfTuple(function().arity());

	    case Term.Kind.IndividualConstant: 
		// Must correspond to Term.hashCode()
		return individualConstant().hashCode();

	    case Term.Kind.AtomicFormula: 
		// Must correspond to AtomicFormula.hashCode()   
		return 
		    (predicate().arity() == 0)?
		    predicate().hashCode() 
		    :
		    (predicate().hashCode() * 5 + 
		     nextCell().hashCodeOfTuple(predicate().arity()));

      
	    case Term.Kind.ConnectiveApplication:
		// Must correspond to ConnectiveApplication.hashCode()
		return connective().hashCode() * 5 + 
		    nextCell().hashCodeOfTuple(connective().arity());

	    case Term.Kind.QuantifierApplication:
		// Must correspond to QuantifierApplication.hashCode()
		return quantifier().hashCode() * 5 + nextCell().hashCode();

	    case Term.Kind.AbstractionTerm:   
		// Must correspond to AbstractionTerm.hashCode()
		return 
		    variable().hashCode() * 5 + nextCell().hashCode();


	}; // switch (kind())
	
	assert false;
	return 0;

    } //  hashCode()


    /** Hash code of the tuple of terms of the given arity,
     *  that starts in this cell. See also TermPair.hashCode().
     *  @param arity > 0
     */
    public final int hashCodeOfTuple(int arity) {
	assert arity > 0;

	if (arity == 1) return hashCode();

	// Must correspond to TermPair::hashCode()
	return 
	    hashCode() * 5 + after().hashCodeOfTuple(arity - 1);
  
    } // hashCodeOfTuple(int arity)

    /** Compares just the cell objects, not the whole terms.
     *  <b>pre:</b> <code>obj instanceof Flatterm</code>
     */ 
    public final boolean equals(Object obj) {
	
	return _kindTag == ((Flatterm)obj)._kindTag &&
	    _symbol.equals(((Flatterm)obj)._symbol);
    } 

    
    /** Checks syntactic equality between the whole term
     *  starting with this cell, and the instance of 
     *  <code>term</code> wrt global substitution 2.
     *  <b>pre:</b> <code>term != null</code>
     */
    public final boolean equalsModuloSubst2(Term term) { 
	
	if (term.isVariable())
	    if (((Variable)term).isInstantiated2())
		{
		    return equals(((Variable)term).instance2());
		}
	    else
		return isVariable() && 
		    variable().equals((Variable)term);
	
	switch (kind())
	    {
	    case Term.Kind.Variable:
		assert !term.isVariable();
		return false;
		
	    case Term.Kind.CompoundTerm:   
		return 
		    term.kind() == Term.Kind.CompoundTerm &&
		    function().equals(((CompoundTerm)term).function()) &&
		    nextCell().tupleEqualsModuloSubst2(((CompoundTerm)term).
						       argument());


	    case Term.Kind.IndividualConstant: 
		return term.isIndividualConstant() &&
		    individualConstant().equals((IndividualConstant)term);

	    case Term.Kind.AtomicFormula: 
		return 
		    term.kind() != Term.Kind.AtomicFormula &&
		    predicate().equals(((AtomicFormula)term).predicate()) &&
		    (((AtomicFormula)term).argument() == null ||
		     nextCell().tupleEqualsModuloSubst2(((AtomicFormula)term).
							argument()));

      
	    case Term.Kind.ConnectiveApplication:
		return 
		    term.kind() != Term.Kind.ConnectiveApplication &&
		    connective().equals(((ConnectiveApplication)term).
					connective()) &&
		    nextCell().tupleEqualsModuloSubst2(((ConnectiveApplication)term).
						       argument());
		    
	    case Term.Kind.QuantifierApplication:
		return 
		    term.kind() != Term.Kind.QuantifierApplication &&
		    quantifier().equals(((QuantifierApplication)term).
					quantifier()) &&
		    nextCell().equalsModuloSubst2(((QuantifierApplication)term).
						  abstraction());

	    case Term.Kind.AbstractionTerm:   
		return 
		    term.kind() != Term.Kind.AbstractionTerm &&
		    variable() == ((AbstractionTerm)term).variable() &&
		    nextCell().equalsModuloSubst2(((AbstractionTerm)term).
						  matrix());
		

	}; // switch (kind())
	

	assert false;

	return false;

    } // equalsModuloSubst2(Term term)



    /** Checks syntactic equality between some tuple of terms
     *  starting with this cell, and the instance of 
     *  <code>term</code> wrt global substitution 2.
     *  <b>pre:</b> <code>term</code> may be <code>null</code>,
     *  in which case <code>true</code> is returned
     */
    public final boolean tupleEqualsModuloSubst2(Term term) { 
	
	if (term == null) return true;

	if (term.isPair())
	    {
		return equalsModuloSubst2(((TermPair)term).first()) &&
		    after().
		    tupleEqualsModuloSubst2(((TermPair)term).second());
	    }
	else
	    return equalsModuloSubst2(term);

    } // tupleEqualsModuloSubst2(Term term)


    /** Checks if the whole term starting with this cell,
     *  contains the instance of 
     *  <code>term</code> wrt global substitution 2.
     */
    public 
	final 
	boolean containsAsProperSubtermModuloSubst2(Term term) {
	
	for (Flatterm cell = nextCell(); 
	     cell != after(); 
	     cell = cell.nextCell())
	    if (cell.equalsModuloSubst2(term))
		return true;

	return false;

    } // containsAsProperSubtermModuloSubst2(Term term)



    /** Checks of the whole term starting with this cell is syntactically
     *  equal to the whole term starting with <code>term</code>.
     */
    public final boolean wholeTermEquals(Flatterm term) {
	
	Flatterm afterThis = after();
	
	Flatterm here = this;
	Flatterm there = term;

	while (here != afterThis)
	    {
		assert there != term.after();
		if (!here.equals(there)) return false;
		here = here.nextCell();
		there = there.nextCell();
	    };
	
	assert there == term.after();
	
	return true;
	
    } // wholeTermEquals(Flatterm term)

    /** Checks if the whole term starting with this cell,
     *  contains a subterm syntactically equal to <code>term</code>.
     */
    public 
	final 
	boolean containsAsProperSubterm(Flatterm term) {
	
	for (Flatterm cell = nextCell(); 
	     cell != after(); 
	     cell = cell.nextCell())
	    if (cell.wholeTermEquals(term))
		return true;

	return false;

    } // containsAsProperSubterm(Term term)


    
    public 
	final 
	boolean containsVariable(Variable var) {
	
	for (Flatterm cell = this; 
	     cell != after(); 
	     cell = cell.nextCell())
	    if (cell.isVariable() && cell.variable() == var)
		return true;
	
	return false;

    } // containsVariable(Variable var) 
	



    /** Counts the number of all symbols in the whole term starting
     *  with this cell; an abstraction operator is counted as 1 symbol.
     */
    public final int numberOfSymbols() {
	int result = 1;
	for (Flatterm cell = this; 
	     cell != lastCell(); 
	     cell = cell.nextCell())
	    ++result;
	return result;
    }

    /** Counts the number of all symbols in the instance of the whole 
     *  term starting with this cell, under global substitution 1; 
     *  an abstraction operator is counted as 1 symbol.
     */
    public final int numberOfSymbolsAfterSubst1() {

	int result = 0;
	
	for (Flatterm cell = this; 
	     cell != after(); 
	     cell = cell.nextCell())
	    if (cell.isVariable() &&
		cell.variable().isInstantiated1())
		{
		    result += 
			cell.
			variable().
			instance1().
			numberOfSymbolsAfterSubst1();
		}
	    else
		++result;
	
	return result;

    } // numberOfSymbolsAfterSubst1()

    
    /** Counts all nonvariable symbols <code>sym</code>, 
     *  including logical symbols, but excluding pair constructors
     *  and abstraction operators, such that 
     *  <code>sym.category(modulus) == category</code>,
     *  in the whole term starting with this cell.
     */
    public 
	final 
	int 
	numberOfNonvariableSymbolsFromCategory(int category,
					       int modulus) {	
	int result = 0;
	for (Flatterm cell = this; 
	     cell != after(); 
	     cell = cell.nextCell())
	    if (!cell.isVariable() && 
		!cell.isAbstraction() &&
		cell.symbol().category(modulus) == category)
		++result;
	return result;
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
	
	for (Flatterm cell = this; 
	     cell != after(); 
	     cell = cell.nextCell())
	    if (!cell.isVariable() && 
		!cell.isAbstraction())
		result[cell.symbol().category(modulus)] += 1;
	
    } // addNumberOfNonvariableSymbolsFromCategories(int modulus,


    
    /** All variables occuring in the whole term starting with
     *  this cell, possibly with duplicates. 
     */
    public final List<Variable> variableList() {
	LinkedList<Variable> result = 
	    new LinkedList<Variable>();
	for (Flatterm t = this; t != after(); t = t.nextCell())
	    if (t.isVariable()) result.addLast(t.variable());
	return result;
    }

    
    /** All variables occuring in the whole term starting with
     *  this cell. 
     */
    public final Set<Variable> variableSet() {
	TreeSet<Variable> result = 
	    new TreeSet<Variable>();
	for (Flatterm t = this; t != after(); t = t.nextCell())
	    if (t.isVariable()) result.add(t.variable());
	return result;
    }


    

    /** Subterm of the whole term starting with this cell,
     *  in the position specified with 
     *  <code>n</code>; more precisely.
     *  <b>pre:</b> <code>n</code> must be a valid position in the term,
     *  ie, <code>0 <= n < numberOfSymbols()</code>.
     */
    public final Flatterm subtermInPosition(int n) {
	assert 0 <= n;
	assert n < numberOfSymbols();
	Flatterm res = this;
	while (n > 0) 
	    {
		res = res.nextCell();
		--n;
	    };
	return res;
    }

    
    /** Replaces the proper subterm <code>subterm</code> in the term 
     *  starting with this cell, by <code>replacement</code>.
     */
    public 
	final 
	void replace(Flatterm subterm,Flatterm replacement) {

	for (Flatterm cell1 = this; cell1 != after(); cell1 = cell1.nextCell())
	    {
		if (cell1.nextCell() == subterm)
		    {
			cell1.setNextCell(replacement);

			// Fix previous cells:

			for (Flatterm cell2 = this;
			     cell2 != replacement;
			     cell2 = cell2.nextCell())
			    {
				if (cell2.lastCell() == subterm)
				    {
					cell2.setLastCell(replacement);
				    }
				else if (cell2.lastCell() == subterm.lastCell())
				    cell2.setLastCell(replacement.lastCell());
			    };

			replacement.lastCell().setNextCell(subterm.after());

			return;
		    };
	    };
    } // replace(Flatterm subterm,Flatterm replacement)




    
    /** Instantiates <code>var</code> in the non-variable term 
     *  starting with this cell, by (copies of) <code>instance</code>.
     */
    public 
	final 
	void instantiate(Variable var,Flatterm instance) {
	
        //System.out.println("AGGGGGGGGGGAAAA " + this.toString() + "    AFTER=" + after());
        
	assert !isVariable();
	
	for (Flatterm cell1 = this; cell1 != lastCell(); cell1 = cell1.nextCell())
	    {
		assert cell1 != null;
                
                //System.out.println("           OOO " + cell1.symbol());
                
		assert cell1.nextCell() != null;

		if (cell1.nextCell().isVariable() &&
		    cell1.nextCell().variable() == var)
		    {
			Flatterm instanceCopy = instance.copy();
			
			Flatterm varSubterm = cell1.nextCell();
			cell1.setNextCell(instanceCopy);

			// Fix previous cells:

			for (Flatterm cell2 = this;
			     cell2 != instanceCopy;
			     cell2 = cell2.nextCell())
			    {
				if (cell2.lastCell() == varSubterm)
				    {
					cell2.setLastCell(instanceCopy.lastCell());
				    }
			    };

			instanceCopy.lastCell().setNextCell(varSubterm.after());

			return;
		    };
	    }; // for (Flatterm cell1 = this; cell1 != after(); cell1 = cell1.nextCell())

    } // instantiate(Variable var,Flatterm instance) 



    
    public final Flatterm copy() {

	FlattermAssembler assembler = new FlattermAssembler();

	assembler.pushCopyOf(this);
	
	assembler.wrapUp();

	return assembler.assembledTerm();

    } // copy()



    public final String toString() { return toString(false); } 


    public final String toString(boolean closed) {
		
	switch (kind())
	{
	    case Term.Kind.Variable: return _symbol.toString();

	    case Term.Kind.CompoundTerm: 
	    {
		String result = function().toString() + "(";
		
		Flatterm arg = nextCell();

		result += arg.toString();

		for (int n = 1; n < function().arity(); ++n)
		{
		    arg = arg.after();
		    
		    result += "," + arg;
		};
		    		
		return result + ")";
	    }


	    case Term.Kind.IndividualConstant: return _symbol.toString();

	    case Term.Kind.AtomicFormula: 
		if (predicate().isEquality() || predicate().isInfix())
		{
		    String result = 
			firstArg() + " " + predicate() + " " + secondArg();
		    
		    if (closed) result = "(" + result + ")";
		    
		    return result;
		}
		else if (predicate().arity() == 0) 
		{
		    // Propositional variable. 
		    return predicate().toString();
		}
		else
		{
		    String result = predicate() + "(" + nextCell();
		    
		    Flatterm arg = nextCell();

		    for (int n = 1; n < predicate().arity(); ++n)
		    {
			arg = arg.after();
			
			result += "," + arg;
		    };
		    
		    return result + ")";
		}
	    
	    case Term.Kind.ConnectiveApplication:
		if (connective() == Connective.getNot()) 
		{
		    if (nextCell().isAtomicFormula() &&
			nextCell().predicate().isEquality())
		    {
			String result = 
			    nextCell().firstArg() + 
			    " != " +
			    nextCell().secondArg(); 
			
			if (closed) result = "(" + result + ")";
		    
			return result;
		    }
		    else
			return "~" + nextCell().toString(true);
		}
		else // binary (associative or non-associative) connective
		{
		    assert connective().arity() == 2;
		    
		    String result = 
			nextCell().toString(true) +
			" " + connective() + " " + 
			nextCell().after().toString(true);

		    if (closed) result = "(" + result + ")";
		    
		    return result;
		}


	    case Term.Kind.QuantifierApplication:
		return quantifier().toString() + " " + nextCell();

	    case Term.Kind.AbstractionTerm: 
		return "[" + variable() + "] : " + nextCell().toString(true);

	}; // switch (kind())
	
	assert false;

	return null;

    } // toString(boolean closed)


    
    /** Iteration over flatterms (ignoring the variable instantiation), 
     *  with a possibility of backtracking to a specified savepoint.
     */
    public 
	static 
	class BacktrackableIterator 
	implements java.util.Iterator<Flatterm> {


	public BacktrackableIterator() {
	    _currentSubterm = null;
	    _end = null;
	    _subtermsForBacktracking = 
		new Vector<Flatterm>(128,64); 
	    // 128 = initial capacity
	    // 64 = capacity increment
	    _subtermsForBacktracking.setSize(128);
	    _stackSize = 0;
	    _afterIsValid = false;
	}
	
	/** Releases all pointers to external objects. */
	public final void clear() {
	    _currentSubterm = null;
	    _end = null;
	    while (_stackSize > 0)
		{
		    --_stackSize;
		    _subtermsForBacktracking.set(_stackSize,null);
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
	    _currentSubterm = term;
	    if (term == null)
		{
		    _end = null;
		}
	    else
		_end = term.after();

	    _stackSize = 0;	    
	    _afterIsValid = false;
	}
	


	/** Returns true if there is a subterm following the current symbol. */
	public final boolean hasNext() {
	    return _currentSubterm != null &&
		_currentSubterm != _end;
	}

	
	/** Returns the next subterm and advances the position.
	 *  Throws {@link java.util.NoSuchElementException} if 
	 *  <code>hasNext() == false</code>.
	 */
	public final Flatterm next() throws NoSuchElementException {
	    if (!hasNext()) 
		throw new NoSuchElementException();

	    // Save the position for backtracking:
	    _subtermsForBacktracking.setSize(_stackSize + 1);
	    _subtermsForBacktracking.set(_stackSize,_currentSubterm);
	    ++_stackSize;
	    
	    Flatterm result = _currentSubterm;
	    _after = _currentSubterm.after();
	    _afterIsValid = true;
	    
	    _currentSubterm = _currentSubterm.nextCell();

	    return result;
	    
	} // next()


	
	/** Cannot be used. */
	public void remove() throws Error {
	    throw 
		new Error("Forbidden method call: logic.is.power.logic_warehouse.FlattermInstance.remove()");
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
	    _subtermsForBacktracking.setSize(_stackSize + 1);
	    _subtermsForBacktracking.set(_stackSize,_currentSubterm);
	    ++_stackSize;

	    _currentSubterm = _after;
	    _afterIsValid = false;

	} // skipArguments()



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
		    --_stackSize;
		    // Release the pointer:
		    _subtermsForBacktracking.set(_stackSize,null);
		};

	    assert _stackSize == savepoint + 1;
	    
	    --_stackSize;
	    
	    _currentSubterm = 
		_subtermsForBacktracking.get(_stackSize);
	    
	    // Release the pointer:
	    _subtermsForBacktracking.set(_stackSize,null);
	    

	} // backtrackTo(int savepoint



	/** Cancels the effect of the last call to {@link #next()} 
	 *  or {@link skipArguments()}.
	 */ 
	public final void backtrack() {
	    
	    assert _stackSize > 0;

	    _afterIsValid = false;

	    --_stackSize;
	    
	    _currentSubterm = 
		_subtermsForBacktracking.get(_stackSize);

	    // Release the pointer:
	    _subtermsForBacktracking.set(_stackSize,null);

	} // void backtrack()



	//  
	//          Private data:
	// 

	private Flatterm _currentSubterm;

	private Flatterm _end;

	/** Stack for backtrack points. */
	private final Vector<Flatterm> _subtermsForBacktracking;

	/** Current size of the stack for backtracking. */
	private int _stackSize;
	
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






    //                   Data:

    

    /** Indicates the syntactic kind of the term that starts with this cell. */
    private int _kindTag;

    /** Allows to link cells in linked lists that represent whole terms;
     *  _nextCell may point to another cell in the term that starts
     *  with this cell, or it may point to another subterm of a larger 
     *  flatterm, eg, if this cell represents a whole term, or it may be null
     *  if no term follows this one.
     */
    private Flatterm _nextCell;
    
    /** If the term that starts with this cell has more than one cell,
     *  then _lastCell points to the last cell in the term;
     *  otherwise, _lastCell == null.
     */
    private Flatterm _lastCell;


    /** Main content of this cell. */
    private Symbol _symbol;

} // class Flatterm
