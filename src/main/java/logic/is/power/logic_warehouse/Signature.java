
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
 * Factory for instances of the class
 * {@link logic.is.power.logic_warehouse.SignatureSymbol}.
 */
public class Signature {

    /** @param mainEqualitySymbol specifies the main equality predicate
     *         name; all other equality predicates will be converted to it.
     *  @param mainDisequalitySymbol if this is non-null and 
     *         <code>mainEqualityIsInfix == true</code>,
     *         negation of equality will be printed infixly with 
     *         this symbol.
     *  @param mainEqualityIsInfix specifies whether the main equality
     *         predicate should be output as infix.
     *  @param otherEqualityPredicates specifies other possible names
     *         for equality predicates; they will be converted
     *         to the main equality predicate.
     */
    public Signature(String mainEqualitySymbol,
		     String mainDisequalitySymbol,
		     boolean mainEqualityIsInfix,
		     Collection<String> otherEqualityPredicates,
		     String mainBuiltInTrueSymbol,
		     Collection<String> otherBuiltInTrueSymbols,
		     String mainBuiltInFalseSymbol,
		     Collection<String> otherBuiltInFalseSymbols) {
	_mainEqualitySymbol = mainEqualitySymbol;
	_mainEqualityIsInfix = mainEqualityIsInfix;
	_otherEqualityPredicates = 
	    new HashSet<String>(otherEqualityPredicates);
	_equalityPredicate = 
	    new Predicate(_mainEqualitySymbol,
			  2,
			  Predicate.Kind.EQUALITY,
			  _mainEqualityIsInfix);
	_mainBuiltInTrueSymbol = mainBuiltInTrueSymbol;
	_builtInTrue = 
	    new Predicate(_mainBuiltInTrueSymbol,
			  0,
			  Predicate.Kind.BUILT_IN_TRUE,
			  false);
	_otherBuiltInTrueSymbols = 
	    new HashSet<String>(otherBuiltInTrueSymbols);
	
	_mainBuiltInFalseSymbol = mainBuiltInFalseSymbol;
	_builtInFalse = 
	    new Predicate(_mainBuiltInFalseSymbol,
			  0,
			  Predicate.Kind.BUILT_IN_FALSE,
			  false);
	_otherBuiltInFalseSymbols = 
	    new HashSet<String>(otherBuiltInFalseSymbols);
			  
	_predicateTable = new HashMap<String,LinkedList<Predicate>>();
	_functionTable = new HashMap<String,LinkedList<Function>>();
	_constantTable = new HashMap<String,IndividualConstant>();
	_nextSkolemConstantIndex = 0;
	_nextSkolemFunctionIndex = 0;
	_nextSkolemPredicateIndex = 0;
    }
    
    /** Returns the current statically accessible object (possibly null). */ 
    public static Signature current() { return _current; }

    /** Makes <code>sig</code> the current statically accessible object;
     *  <code>sig</code> can be null.
     */ 
    public static void makeCurrent(Signature sig) {
	_current = sig;
    }

    
    public final Predicate equalityPredicate() {
	return _equalityPredicate;
    }
    
    public final Predicate builtInTrue() {
	return _builtInTrue;
    }

    public final Predicate builtInFalse() {
	return _builtInFalse;
    }

    /** Finds or creates a representation for the predicate with 
     *  the specified name and arity; the representation is unique 
     *  wrt this instance of Signature.
     *  Set <code>infix</code> to true if you want the predicate to be output as 
     *  an infix one. Note that setting <code>infix</code> to false will not
     *  make the predicate non-infix if it was made infix earlier.
     */
    public final Predicate representationForPredicate(String name,
						      int arity,
						      boolean infix) {

	if (arity == 2 &&
	    (name.equals(_mainEqualitySymbol) ||
	     _otherEqualityPredicates.contains(name)))
	    return _equalityPredicate;

	if (arity == 0)
	    {
		if (name.equals(_mainBuiltInTrueSymbol) ||
		    _otherBuiltInTrueSymbols.contains(name))
		    return _builtInTrue;
		if (name.equals(_mainBuiltInFalseSymbol) ||
		    _otherBuiltInFalseSymbols.contains(name))
		    return _builtInFalse;
	    };

	LinkedList<Predicate> predicates = _predicateTable.get(name);
	
	
	if (predicates == null)
	    {
		predicates = new LinkedList<Predicate>();
		_predicateTable.put(name,predicates);
	    }
	else
	    {
		for (Predicate pred : predicates)
		    if (pred.arity() == arity)
			{
			    if (infix) pred.makeInfix();
			    return pred;
			};
	    };
	
	Predicate result = 
	    new Predicate(name,arity,Predicate.Kind.REGULAR,infix);
	
	predicates.addFirst(result);
	
	return result;
	
    } // representationForPredicate(String name,..)

    /** Same as <code>representationForPredicate(name,arity,false)</code>. */
    public final Predicate representationForPredicate(String name,int arity) {
	return representationForPredicate(name,arity,false);
    } 


    
    /** Finds or creates a representation for the functional symbol with 
     *  the specified name and arity; the representation is unique 
     *  wrt this instance of Signature.
     *  <b>pre:</b> <code>arity</code> may be zero.
     */
    public 
    final 
	FunctionalSymbol 
	representationForFunctionalSymbol(String name,int arity) {
	
	return (arity == 0)? 
	    representationForConstant(name) 
	    : 
	    representationForFunction(name,arity);

    } // representationForFunctionalSymbol(String name,int arity)

    /** Finds or creates a representation for the function with 
     *  the specified name and arity; the representation is unique 
     *  wrt this instance of Signature.
     *  <b>pre:</b> <code>arity > 0</code>.
     */
    public final Function representationForFunction(String name,int arity) {
	
	assert arity > 0;

	Function result;

	LinkedList<Function> functions = _functionTable.get(name);

	if (functions == null)
	{
	    functions = new LinkedList<Function>();
	    _functionTable.put(name,functions);
	}
	else
	{
	    for (Function func : functions)
		if (func.arity() == arity)
		    return func;
	};

	result = new Function(name,arity);

	functions.addFirst(result);
	
	return result;

    } // representationForFunction(String name,int arity)




    /** Finds or creates a representation for the individual constant with 
     *  the specified name; the representation is unique 
     *  wrt this instance of Signature.
     */
    public final IndividualConstant representationForConstant(String name) {
	
	IndividualConstant result = _constantTable.get(name);

	if (result == null) 
	{
	    result = new IndividualConstant(name);

	    _constantTable.put(name,result);
	};

	return result;

    } // representationForConstant(String name)



   /** Creates a skolem functional symbol
    *  with the given arity, whose name starts with <code>namePrefix</code>.
    *  The symbol is only guaranteed to be fresh wrt this signature object,
    *  not globally.
    */
    public 
	final 
	FunctionalSymbol skolemFunctionalSymbol(String namePrefix,int arity) {
	
	return (arity == 0)? 
	    skolemConstant(namePrefix) : skolemFunction(namePrefix,arity);

    } // skolemFunctionalSymbol(String namePrefix,int arity)



    /** Creates a non-constant skolem function
     *  with the given arity (<code> > 0</code>), whose name starts with 
     *  <code>namePrefix</code>.
     *  The symbol is only guaranteed to be fresh wrt this signature object,
     *  not globally.
     */
    public final Function skolemFunction(String namePrefix,int arity) {
	
	String fullName;

	do 
	{
	    fullName = namePrefix + _nextSkolemFunctionIndex;
	
	    ++_nextSkolemFunctionIndex;
	}
	while (containsSymbolWithName(fullName));
	
	// Unique name generated.
	
	return representationForFunction(fullName,arity);

    } //  skolemFunction(String namePrefix,int arity)


    /** Creates a skolem predicate with the given arity, 
     *  whose name starts with <code>namePrefix</code>.
     *  The symbol is only guaranteed to be fresh wrt this signature object, 
     *  not globally.
     */
    public final IndividualConstant skolemConstant(String namePrefix) {
	
	String fullName;

	do 
	{
	    fullName = namePrefix + _nextSkolemConstantIndex;
	
	    ++_nextSkolemConstantIndex;
	}
	while (containsSymbolWithName(fullName));
	
	// Unique name generated.
	
	return representationForConstant(fullName);

    } // skolemConstant(String namePrefix)





    /** Creates a skolem predicate with the given arity, 
     *  whose name starts with <code>namePrefix</code>.
     *  The symbol is only guaranteed to be fresh wrt this signature object, 
     *  not globally.
     */
    public final Predicate skolemPredicate(String namePrefix,int arity) {
	
	
	String fullName;

	do 
	{
	    fullName = namePrefix + _nextSkolemPredicateIndex;
	
	    ++_nextSkolemPredicateIndex;
	}
	while (containsSymbolWithName(fullName));
	
	// Unique name generated.
	
	return representationForPredicate(namePrefix,arity);

    } // skolemPredicate(String namePrefix,int arity)




    //                        Private methods:

    /** Checks if this signature contains any symbol with the specified name. */
    private boolean containsSymbolWithName(String name) {
	
	return _predicateTable.containsKey(name) ||
	    _functionTable.containsKey(name) ||
	    _constantTable.containsKey(name);

    } // containsSymbolWithName(String name)


    //                        Data:

    private static Signature _current = null;
    
    private final String _mainEqualitySymbol;

    private final boolean _mainEqualityIsInfix;

    private final HashSet<String> _otherEqualityPredicates;

    private final Predicate _equalityPredicate;

    private final String _mainBuiltInTrueSymbol;

    private final HashSet<String> _otherBuiltInTrueSymbols;

    private final Predicate _builtInTrue;

    private final String _mainBuiltInFalseSymbol;

    private final HashSet<String> _otherBuiltInFalseSymbols;
    
    private final Predicate _builtInFalse;

    private final HashMap<String,LinkedList<Predicate>> _predicateTable;

    private final HashMap<String,LinkedList<Function>> _functionTable;

    private final HashMap<String,IndividualConstant> _constantTable;

    private int _nextSkolemConstantIndex;

    private int _nextSkolemFunctionIndex;

    private int _nextSkolemPredicateIndex;

}; // class Signature
