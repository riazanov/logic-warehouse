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


import java.util.LinkedList;

import java.lang.reflect.Array;

/**
 * Factory for agressively shared terms; converts 
 * {@link logic.is.power.logic_warehouse.Flatterm}
 * representation into {@link logic.is.power.logic_warehouse.Term}.
 */
public class TermFactory {

    public TermFactory() {
	_hashtable = 
	    (LinkedList<Term>[])Array.newInstance(LinkedList.class,
						  HashtableSize);
	// This hack is a workaround for the problem with
	// generic array initialisation in Java.
	// The following syntax causes an error:
	//_hashtable = new LinkedList<Term>[HashtableSize];

    }

    /** Returns the current statically accessible object (possibly null). */ 
    public static TermFactory current() { return _current; }

    /** Makes <code>f</code> the current statically accessible object;
     *  <b>pre:</b> <code>f</code> can be null.
     */ 
    public static void makeCurrent(TermFactory f) {
	_current = f;
    }


//     public final void resetVariableRenaming() {
//     }
    

    public final Formula createSharedFormula(Flatterm flatterm) {
	assert flatterm.isFormula();
	return (Formula)createSharedTerm(flatterm);
    }
	
    public final Term createSharedTerm(Flatterm flatterm) {

	
	// Two special cases that don't require look up in the hashtable

	if (flatterm.kind() == Term.Kind.Variable)
	    return flatterm.variable();

	if (flatterm.kind() == Term.Kind.IndividualConstant)
	    return flatterm.individualConstant();


	// More complex terms require look up in the hashtable

	int normalisedHashCode =
	    normaliseHashCode(flatterm.hashCode());


	LinkedList<Term> bucket = _hashtable[normalisedHashCode];

	if (bucket != null) 
	{
	    for (Term storedTerm : bucket)
	    {
		if (storedTerm.equals(flatterm))
		    // the required term structure already exists
		    return storedTerm;
	    };
	}
	else
	{
	    bucket = new LinkedList<Term>();
	    _hashtable[normalisedHashCode] = bucket;
	};
   
    
	// The bucket does not contain the required term. 
	// New term structure has to be created.

	Term term = null;

	switch (flatterm.kind())
	{

	    case Term.Kind.Variable: assert false; break; 

	    case Term.Kind.CompoundTerm:   
		term = new CompoundTerm(flatterm.function(),
					createSharedTuple(flatterm.nextCell(),
							  flatterm.function().arity()));
		break;


	    case Term.Kind.IndividualConstant: assert false; break;


	    case Term.Kind.AtomicFormula:   
		term = 
		    new AtomicFormula(flatterm.predicate(),
				      (flatterm.predicate().arity() == 0)?
				      null
				      :
				      createSharedTuple(flatterm.nextCell(),
							flatterm.predicate().arity()));
		break;
      


	    case Term.Kind.ConnectiveApplication: 
		term = 
		    new ConnectiveApplication(flatterm.connective(),
					      createSharedTuple(flatterm.nextCell(),
								flatterm.connective().arity()));
		break;
	

	    case Term.Kind.QuantifierApplication:
		term = 
		    new QuantifierApplication(flatterm.quantifier(),
					      createSharedTerm(flatterm.nextCell()));
		break;



	    case Term.Kind.AbstractionTerm: 

		term = 
		    new AbstractionTerm(flatterm.variable(),
					createSharedTerm(flatterm.nextCell()));
		break;

	}; // switch (flatterm->kind())
  
  
	bucket.addFirst(term);
    
	assert term.equals(flatterm);

	return term;


    } // createSharedTerm(Flatterm flatterm)





    /** Creates shared representation of the tuple of terms of 
     *  the specified arity, starting in the cell <code>flatterms</code>.
     *  <b>pre:</b> <code>arity > 0</code>
     */
    public final Term createSharedTuple(Flatterm flatterms,int arity) {

	assert arity > 0;
		
	if (arity == 1) return createSharedTerm(flatterms);
    
	int normalisedHashCode =
	    normaliseHashCode(flatterms.hashCodeOfTuple(arity));

	LinkedList<Term> bucket = _hashtable[normalisedHashCode];

	if (bucket != null) 
	{
	    for (Term storedTerm : bucket) 
	    {
		if (storedTerm.isPair() &&
                    ((TermPair)storedTerm).dimension() == arity &&
		    ((TermPair)storedTerm).equals(flatterms))
		    // the required term structure already exists
		    {
			assert ((TermPair)storedTerm).dimension() == arity;
			return storedTerm;
		    };
	    };
	}
	else
	{
	    bucket = new LinkedList<Term>();
	    _hashtable[normalisedHashCode] = bucket;
	    
	};

	// The bucket does not contain the required term. 
	// New term structure has to be creater.
  
	TermPair term = new TermPair(createSharedTerm(flatterms),
				     createSharedTuple(flatterms.after(),
						       arity - 1));
 
  
	bucket.addFirst(term);
    
	assert term.dimension() == arity;

	return term;

    } // createSharedTuple(Flatterm flatterms,int arity)
    


    /** Argument may be null if <code>pred.arity() == 0</code>. */
    public final AtomicFormula createSharedAtomicFormula(Predicate pred,
							 Term argument) {

	return (AtomicFormula)findOrShare(new AtomicFormula(pred,argument));

    } // createSharedAtomicFormula(Predicate pred,



    public 
	final 
	Term copyWithVariableRenaming(Term term,VariableRenaming renaming) {
	
	if (!term.containsVariables()) return term;
	
	Term copy = null;
	
	switch (term.kind())
	    {
		
	    case Term.Kind.Variable:
		return renaming.rename((Variable)term);

	    case Term.Kind.CompoundTerm:
		{
		    Function func = ((CompoundTerm)term).function();
		    Term arg = ((CompoundTerm)term).argument();
		    Term argCopy = copyWithVariableRenaming(arg,renaming);

		    copy = new CompoundTerm(func,argCopy);
		    break;
		}
		

	    case Term.Kind.IndividualConstant:
		assert false; // does not contain variables
		
	    case Term.Kind.AtomicFormula:
		{
		    Predicate pred = ((AtomicFormula)term).predicate();
		    Term arg = ((AtomicFormula)term).argument();
		    Term argCopy = 
			(arg == null)?
			null
			:
			copyWithVariableRenaming(arg,renaming);

		    copy = new AtomicFormula(pred,argCopy);
		    break;
		}

	    case Term.Kind.ConnectiveApplication:
		{
		    Connective con = ((ConnectiveApplication)term).connective();
		    Term arg = ((ConnectiveApplication)term).argument();
		    Term argCopy = copyWithVariableRenaming(arg,renaming);

		    copy = new ConnectiveApplication(con,argCopy);
		    break;
		    
		} 
		    
	    case Term.Kind.QuantifierApplication:
		{
		    Quantifier quant = ((QuantifierApplication)term).quantifier();
		    Term abs = ((QuantifierApplication)term).abstraction();
		    Term absCopy = copyWithVariableRenaming(abs,renaming);

		    copy = new QuantifierApplication(quant,absCopy);
		    break;
		    
		} 
		    
	    case Term.Kind.AbstractionTerm:
		{
		    Variable var = ((AbstractionTerm)term).variable();
		    Variable newVar = renaming.rename(var);
		    
		    Term matrix = ((AbstractionTerm)term).matrix();
		    Term matrixCopy = 
			copyWithVariableRenaming(matrix,renaming);
		    
		    copy = new AbstractionTerm(newVar,matrixCopy);
		    break;
		}
		    
	    case Term.Kind.TermPair:
		{
		    Term first = ((TermPair)term).first();
		    Term second = ((TermPair)term).second();

		    Term firstCopy = 
			copyWithVariableRenaming(first,renaming);
		    Term secondCopy = 
			copyWithVariableRenaming(second,renaming);
		    copy = new TermPair(firstCopy,secondCopy);
		    break;
		}
		    
	    default:
		assert false;
		    
	    }; // switch (term.kind())

	return findOrShare(copy);
	
    } // copyWithVariableRenaming(Term term,Variable renaming)




    private static int normaliseHashCode(int hashCode) {
	
	int normalisedHashCode = hashCode;

	if (normalisedHashCode < 0)
	    normalisedHashCode = -1 * normalisedHashCode;
	 
	return normalisedHashCode % HashtableSize;
    }



    private Term findOrShare(Term term) {

	int normalisedHashCode = normaliseHashCode(term.hashCode());
	 
	LinkedList<Term> bucket = _hashtable[normalisedHashCode];


	if (bucket != null) 
	    {
		for (Term storedTerm : bucket)
		    {
			if (storedTerm.equals(term))
			    // the required term structure already exists
			    return storedTerm;
		    };
	    }
	else
	    {
		bucket = new LinkedList<Term>();
		_hashtable[normalisedHashCode] = bucket;
	    };
	
  
	bucket.addFirst(term);
	
	return term;

    } // findOrShare(Term term)



    //                    Data:
    
    private static TermFactory _current = null;
    
    private static int HashtableSize = 131072;

    private final LinkedList<Term>[] _hashtable;

}; // class TermFactory
