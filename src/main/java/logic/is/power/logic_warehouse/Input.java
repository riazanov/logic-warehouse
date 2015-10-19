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
 * Simple implementation of {@link logic.is.power.logic_warehouse.InputSyntax};
 * provides actual datastructures
 * to represent formulas, clauses and terms; can be used to represent
 * results of a concrete syntax parser, which can be directly communicated
 * to a module that accepts {@link logic.is.power.logic_warehouse.InputSyntax}; 
 * roughly, this class 
 * is a factory for formulas, clauses and terms in a representation
 * that is compliant with InputSyntax.
 */
public class Input implements InputSyntax {


    public static class Formula implements InputSyntax.Formula {
	

	//          Methods prescribed by InputSyntax.Formula:

	public boolean isQuantified() { 
	    return _kind == Kind.Quantified;
	}

	public boolean isNegated() { 
	    return _kind == Kind.Negated;
	}

	public boolean isBinary() { 
	    return _kind == Kind.Binary;
	}

	public boolean isAssociative() { 
	    return _kind == Kind.Associative;
	}

	public boolean isAtomic() { 
	    return _kind == Kind.Atomic;
	}

	public int quantifier() {
	    assert isQuantified();
	    return ((Integer)_quantifierOrConnectiveOrPredicate).intValue();
	}
	
	public List<InputSyntax.Variable> quantifiedVariables() {
	    assert isQuantified();
	    return 
		(List<InputSyntax.Variable>)_argumentsOrArgument1OrVariables;
	}
	
	public InputSyntax.Formula matrix()  {
	    assert isQuantified();
	    return (InputSyntax.Formula)_argument2OrMatrix;
	}

	public InputSyntax.Formula formulaUnderNegation() {
	    assert isNegated();
	    return (InputSyntax.Formula)_argumentsOrArgument1OrVariables;
	}
	
	public int binaryConnective() {
	    assert isBinary();
	    return ((Integer)_quantifierOrConnectiveOrPredicate).intValue();
	}

	public InputSyntax.Formula leftArgument() {
	    assert isBinary();
	    return (InputSyntax.Formula)_argumentsOrArgument1OrVariables;
	}

	public InputSyntax.Formula rightArgument() {
	    assert isBinary();
	    return (InputSyntax.Formula)_argument2OrMatrix;
	}
	
	public int associativeConnective() {
	    assert isAssociative();
	    return 
		((Integer)_quantifierOrConnectiveOrPredicate).intValue();
	}

	public List<InputSyntax.Formula> connectiveArguments() {
	    assert isAssociative();
	    return 
		(List<InputSyntax.Formula>)_argumentsOrArgument1OrVariables;
	}
			
	public InputSyntax.Predicate predicate() {
	    assert isAtomic();
	    return (InputSyntax.Predicate)_quantifierOrConnectiveOrPredicate;
	}

	public List<InputSyntax.Term> predicateArguments() { 
	    assert isAtomic();
	    return (List<InputSyntax.Term>)_argumentsOrArgument1OrVariables;
	}

	

	public Set<InputSyntax.Variable> freeVariables() {

	    Set<InputSyntax.Variable> result;

	    switch (_kind)
		{
		case Kind.Quantified:
		    result = matrix().freeVariables();
		    result.removeAll(quantifiedVariables());
		    return result;
		case Kind.Negated:
		    return formulaUnderNegation().freeVariables();
		case Kind.Binary:
		    result = leftArgument().freeVariables();
		    result.addAll(rightArgument().freeVariables());
		    return result;
		case Kind.Associative:
		    result = new TreeSet<InputSyntax.Variable>();
		    for (InputSyntax.Formula arg : connectiveArguments())
			result.addAll(arg.freeVariables());
		    return result;
		case Kind.Atomic:
		    result = new TreeSet<InputSyntax.Variable>();
		    for (InputSyntax.Term arg : predicateArguments())
			result.addAll(arg.freeVariables());
		    return result;
		    
		default:
		    assert false;
		    return null;

		} //switch (_kind)

	    } // freeVariables()



	public String toString(boolean closed) {

	    if (isQuantified())
	    {
		String result = 
		    InputSyntax.Quantifier.toString(quantifier()) + "[";
		
		Iterator<InputSyntax.Variable> var =
		    quantifiedVariables().listIterator();
		
		assert var.hasNext();

		result += var.next();

		while (var.hasNext())
		    result += ", " + var.next();
		
		result += "] : " + matrix().toString(true);

		if (closed) result = "(" + result + ")";

		return result;
	    }
	    else if (isNegated())
	    {
	       return "~" + formulaUnderNegation().toString(true);
	    }
	    else if (isBinary()) 
	    {
		String result = 
		    leftArgument().toString() + 
		    " " + InputSyntax.BinaryConnective.toString(binaryConnective()) + " " + 
		    rightArgument();

		if (closed) result = "(" + result + ")";

		return result;
	    }
	    else if (isAssociative())
	    {
		
		Iterator<InputSyntax.Formula> arg = 
		    connectiveArguments().listIterator();
		
		assert arg.hasNext();

		String result = arg.next().toString();
		
		assert arg.hasNext();
		
		do 
		{
		    result += " " + InputSyntax.AssociativeConnective.toString(associativeConnective()) + " " + arg.next();
		}
		while (arg.hasNext());
		
		if (closed) result = "(" + result + ")";

		return result;
		
	    }
	    else if (isAtomic())
	    {
		if (predicate().isInfix())
		{
		    String result = 
			predicateArguments().get(0) + 
			" " + predicate() + " " +
			predicateArguments().get(1);
		
		    if (closed) result = "(" + result + ")";

		    return result;
		}
		else
		{
		    String result = predicate().toString();

		    if (predicateArguments() == null ||
			predicateArguments().isEmpty()) 
			return result;

		    Iterator<InputSyntax.Term> arg = 
			predicateArguments().listIterator();

		    assert arg.hasNext();

		    result += "(" + arg.next();

		    while (arg.hasNext())
			result += "," + arg.next();

		    result += ")";

		    return result;
		}
	    };
	    
	    assert false;
		
	    return null;

	} // toString(boolean closed)



	//           More public methods:

	public String toString() { return toString(false); }


	//            Package access methods:
	
	Formula(int quant,
		List<InputSyntax.Variable> vars,
		InputSyntax.Formula matrix) {
	    _kind = Kind.Quantified;
	    _quantifierOrConnectiveOrPredicate = new Integer(quant);
	    _argumentsOrArgument1OrVariables = vars;
	    _argument2OrMatrix = matrix;
	} 
		
	
	/** Creates the negation of the formula. */
	Formula(InputSyntax.Formula formulaUnderNegation) {
	    _kind = Kind.Negated;
	    _argumentsOrArgument1OrVariables = formulaUnderNegation;
	}

	Formula(int binCon,
		InputSyntax.Formula leftArg,
		InputSyntax.Formula rightArg) {
	    _kind = Kind.Binary;
	    _quantifierOrConnectiveOrPredicate = new Integer(binCon);
	    _argumentsOrArgument1OrVariables = leftArg;
	    _argument2OrMatrix = rightArg;
	}
	      
	Formula(int assocCon,
		List<InputSyntax.Formula> args) {
	    _kind = Kind.Associative;
	    _quantifierOrConnectiveOrPredicate = new Integer(assocCon);
	    _argumentsOrArgument1OrVariables = args;
	}

	Formula(InputSyntax.Predicate pred,
		List<InputSyntax.Term> args) {
	    _kind = Kind.Atomic;
	    _quantifierOrConnectiveOrPredicate = pred;
	    _argumentsOrArgument1OrVariables = args;
	}
		

	//            Private types:
	
	private static class Kind {
	    public static final int Quantified = 0;
	    public static final int Negated = 1;
	    public static final int Binary = 2;
	    public static final int Associative = 3;
	    public static final int Atomic = 4;
	}; 
	


	//            Data:
	
	private int _kind;

	/** Keeps the top symbol whose type depends on 
	 *  the kind of the formula.  
	 */
	private Object _quantifierOrConnectiveOrPredicate;

	/** Keeps the list of arguments of an associative connective,
	 *  or arguments in a predicate application,
	 *  or the first argument of a binary connective, or the argument
	 *  of negation, or the quantified variables in a quantified formula.
	 */
	private Object _argumentsOrArgument1OrVariables;
	
	/** Keeps the second argument of a binary connective,
	 *  or the matrix in a quantified formula.
	 */
	private Object _argument2OrMatrix;
	
    }; // class Formula



    public static class Literal implements InputSyntax.Literal {
	

	//          Methods prescribed by InputSyntax.Literal:

	public boolean isPositive() { return _isPositive; }

	public boolean isNegative() { return !_isPositive; }
	
	public InputSyntax.Predicate predicate() { return _atom.predicate(); }

	public List<InputSyntax.Term> predicateArguments() {
	    return _atom.predicateArguments();
	}

	public Set<InputSyntax.Variable> freeVariables() {
	    TreeSet<InputSyntax.Variable> result = 
		new TreeSet<InputSyntax.Variable>();
	    for (InputSyntax.Term arg : _atom.predicateArguments())
		result.addAll(arg.freeVariables());
	    return result;
	}

	public String toString(boolean closed) {

	    if (isPositive()) return _atom.toString(closed);

	    if (predicate().isInfix() &&
		predicate().negatedInfixName() != null)
	    {
		// Special syntax:
		
		String result = 
		    _atom.predicateArguments().get(0) + 
		    predicate().negatedInfixName() +
		    _atom.predicateArguments().get(1);

		if (closed) result = "(" + result + ")";

		return result;
	    }
	    else
	    {
		return "~" + _atom.toString(true);
	    }
	} // toString(boolean closed)


	//           More public methods:

	public String toString() { return toString(false); }



	//            Package access methods:
	
	Literal(boolean positive,InputSyntax.Formula atom) {
	    _isPositive = positive;
	    _atom = atom;
	}


	//             Data:

	private boolean _isPositive;
	
	private InputSyntax.Formula _atom;

    }; // class Literal 





    public static class Term implements InputSyntax.Term {


	//          Methods prescribed by InputSyntax.Term:
   
	public boolean isVariable() { return _isVariable; }
	
	public boolean isNonVariable() { return !_isVariable; }

	/** @return isNonVariable() && functionalSymbol().arity() == 0 */
	public boolean isConstant() {
	    return isNonVariable() && functionalSymbol().arity() == 0;
	}

	public InputSyntax.Variable variable() {
	    assert isVariable();
	    return (InputSyntax.Variable)_topSymbol;
	}

	public InputSyntax.FunctionalSymbol functionalSymbol() {
	    assert isNonVariable();
	    return (InputSyntax.FunctionalSymbol)_topSymbol;
	}
	
	public List<InputSyntax.Term> arguments() {
	    assert isNonVariable();
	    return _arguments;
	}

	public Set<InputSyntax.Variable> freeVariables() {
	    TreeSet<InputSyntax.Variable> result = 
		new TreeSet<InputSyntax.Variable>();
	    if (_isVariable)
		{
		    result.add((InputSyntax.Variable)_topSymbol);
		}
	    else
		{
		    if (_arguments != null)
			for (InputSyntax.Term arg : _arguments)
			    result.addAll(arg.freeVariables());
		};
	    return result;
	}

	//           More public methods:

	public String toString() { 
	    if (isVariable()) return variable().toString();
	    if (arguments() != null && !arguments().isEmpty())
	    {
		String result = functionalSymbol() + "(";

		Iterator<InputSyntax.Term> arg = 
		    arguments().listIterator();

		assert arg.hasNext();

		result += arg.next().toString();

		while (arg.hasNext())
		    result += "," + arg.next();

		result += ")";
		
		return result;
	    }
	    else
		return functionalSymbol().toString();
	}


	/** Note that <code>obj</code> may be of a diferent subclass of
	 *  InputSyntax.Term and still be equal to <code>this</code>.
	 */
	public boolean equals(Object obj) {
	    if (obj == null ||
		!(obj instanceof InputSyntax.Term))
		return false;
	    
	    if (isVariable())
		return ((Term)obj).isVariable() &&
		    variable().equals(((InputSyntax.Term)obj).variable());

	    if (!functionalSymbol().equals(((InputSyntax.Term)obj).functionalSymbol()))
		return false;
	    
	    if (arguments() != null)
		{
		    assert ((Term)obj).arguments() != null;
		    assert 
			arguments().size() == 
			((InputSyntax.Term)obj).arguments().size();

		    Iterator<InputSyntax.Term> arg1 = arguments().iterator();
		    Iterator<InputSyntax.Term> arg2 = 
			((InputSyntax.Term)obj).arguments().iterator();
		    while (arg1.hasNext())
			{
			    if (!arg1.next().equals(arg2.next()))
				return false;
			};

		};

	    return true;
	    
	} // equals(Object obj)
	

	//            Package access methods:

	Term(InputSyntax.Variable var) {
	    _isVariable = true;
	    _topSymbol = var;
	}

        /** <b>pre:</b> <code>arguments != null && 
         *   func.arity() == arguments.size()</code>. 
         */
	Term(InputSyntax.FunctionalSymbol func,
	     List<InputSyntax.Term> arguments) {
	    
	    assert arguments != null;
	    assert func.arity() == arguments.size();

	    _isVariable = false;
	    _topSymbol = func;
	    _arguments = arguments;
	}


	Term(InputSyntax.FunctionalSymbol con) {
	    _isVariable = false;
	    _topSymbol = con;
	    _arguments = null;
	}


	//                Data:

	private boolean _isVariable;

	/** Variable or functional symbol. */
	private Object _topSymbol; 

	private List<InputSyntax.Term> _arguments;

    }; // case Term


    
    public static class Predicate implements InputSyntax.Predicate {


	//          Methods prescribed by InputSyntax.Predicate:

	public String name() { return _name; }

	public int arity() { return _arity; }

	public boolean isInfix() {
	    return _isInfix;
	}

	/** If <code>isInfix() == true</code> and 
	 *  <code>negatedInfixName() != null</code>, then negative literals
	 *  with this predicate should be printed using 
	 *  <code>negatedInfixName()</code> infixly.
	 */
	public String negatedInfixName() {
	    return _negatedInfixName;
	}
	
	public int compareTo(InputSyntax.Predicate pred) {
	    int nameComparison = _name.compareTo(pred.name());
	    if (nameComparison != 0) return nameComparison;
	    return _arity - pred.arity();
	}



	//           More public methods:

	public String toString() { return name(); }


	/** Note that <code>obj</code> may be of a diferent subclass of
	 *  InputSyntax.Predicate and still be equal 
	 *  to <code>this</code>.
	 */
	public boolean equals(Object obj) {
	    return obj != null && 
		(obj instanceof InputSyntax.Predicate) &&
		name().equals(((InputSyntax.Predicate)obj).name()) &&
		arity() == ((InputSyntax.Predicate)obj).arity();
	}


	//                 Package access methods:

	Predicate(String name,int arity,boolean infix,String negatedInfixName) {
	    _name = name;
	    _arity = arity;
	    _isInfix = infix;
	    _negatedInfixName = negatedInfixName;
	}

	Predicate(String name,int arity) {
	    _name = name;
	    _arity = arity;
	    _isInfix = false;
	}


	//                 Data:

	private String _name;

	private int _arity;

	private boolean _isInfix;

	private String _negatedInfixName;

    }; // class Predicate


    public static class FunctionalSymbol 
	implements InputSyntax.FunctionalSymbol {
	
	//          Methods prescribed by InputSyntax.FunctionalSymbol:


	public int compareTo(InputSyntax.FunctionalSymbol sym) {
	    int nameComparison = _name.compareTo(sym.name());
	    if (nameComparison != 0) return nameComparison;
	    return _arity - sym.arity();
	}
  

	public String name() { return _name; }

	public int arity() { return _arity; }

	//           More public methods:

	public String toString() { return name(); }


	/** Note that <code>obj</code> may be of a diferent subclass of
	 *  InputSyntax.FunctionalSymbol and still be equal 
	 *  to <code>this</code>.
	 */
	public boolean equals(Object obj) {
	    return obj != null && 
		(obj instanceof InputSyntax.FunctionalSymbol) &&
		name().equals(((InputSyntax.FunctionalSymbol)obj).name()) &&
		arity() == ((InputSyntax.FunctionalSymbol)obj).arity();
	}

	//                 Package access methods:

	FunctionalSymbol(String name,int arity) {
	    _name = name;
	    _arity = arity;
	}

	//                 Data:

	private String _name;

	private int _arity;


    }; // class FunctionalSymbol 


    public static class Variable implements InputSyntax.Variable {
		
	//          Methods prescribed by InputSyntax.Variable:

	public String name() { return _name; }

	public int hashCode() {
	    return _name.hashCode(); // need not be too efficient
	}

	public int compareTo(InputSyntax.Variable var) {
	    return _name.compareTo(var.name());
	}

	
	//           More public methods:

	public String toString() { return name(); }

	/** Note that <code>obj</code> may be of a diferent subclass of
	 *  InputSyntax.Variable and still be equal 
	 *  to <code>this</code>.
	 */
	public boolean equals(Object obj) {
	    return obj != null && 
		(obj instanceof InputSyntax.Variable) &&
		name().equals(((InputSyntax.Variable)obj).name());
	}

	//                 Package access methods:

	Variable(String name) {
	    _name = name;
	}

	//                 Data:

	private String _name;

    }; // class Variable
    



    //                  Public methods for the class Input:

    public Input() {
    } 

    

    //   Creating formulas:

    /** <b>pre:</b> <code>variables != null && variables.size() > 0</code>. */
    public 
    Formula 
    createQuantifiedFormula(int quantifier,
			    List<InputSyntax.Variable> variables,
			    InputSyntax.Formula matrix) {
	return new Formula(quantifier,variables,matrix);
    }

    public Formula 
    createNegatedFormula(InputSyntax.Formula immediateSubformula) {
	return new Formula(immediateSubformula);
    }
      
    public Formula
    createBinaryFormula(int binConnective,
			InputSyntax.Formula argument1,
			InputSyntax.Formula argument2) {
	return new Formula(binConnective,argument1,argument2);
    }
			 
    /** <b>pre:</b> <code>arguments != null &&  
     *      arguments.size() > 1</code>.
     */
    public 
    Formula 
    createAssociativeFormula(int assocConnective,
			     List<InputSyntax.Formula> arguments) {
	return new Formula(assocConnective,arguments);
    }
			       

    /** <b>pre:</b> <code>arguments</code> can be null or empty. */
    public Formula createAtomicFormula(InputSyntax.Predicate predicate,
				       List<InputSyntax.Term> arguments) {
	return new Formula(predicate,arguments);
    }

    /** <b>pre:</b> <code>predicate.arity() == 2</code>. */
    public Formula createAtomicFormula(InputSyntax.Predicate predicate,
				       InputSyntax.Term arg1,
				       InputSyntax.Term arg2) {
	LinkedList<InputSyntax.Term> arguments = 
	    new LinkedList<InputSyntax.Term>();
	arguments.addLast(arg1);
	arguments.addLast(arg2);
	return createAtomicFormula(predicate,arguments);
    }



      
    //   Creating literals:

    
    /** <b>pre:</b> <code>atom.isAtomic()</code>. */
    public Literal createLiteral(boolean positive,Formula atom) {
	return new Literal(positive,atom);
    }


    /** <b>pre:</b> <code>arguments</code> can be null or empty. */
    public Literal createLiteral(boolean positive,
				 InputSyntax.Predicate predicate,
				 List<InputSyntax.Term> arguments) {
	return new Literal(positive,
			   createAtomicFormula(predicate,
					       arguments));
    }
	

    /** <b>pre:</b> <code>predicate.arity() == 2</code>. */
    public Literal createLiteral(boolean positive,
				 InputSyntax.Predicate predicate,
				 InputSyntax.Term arg1,
				 InputSyntax.Term arg2) {
	return new Literal(positive,
			   createAtomicFormula(predicate,
					       arg1,
					       arg2));
    }
	

    //   Creating terms:
	
    public Term createTerm(InputSyntax.Variable var) {
	return new Term(var);
    }
	

    /** <b>pre:</b> <code>arguments != null && 
     *  func.arity() == arguments.size()</code>. 
     */
    public Term createTerm(InputSyntax.FunctionalSymbol func,
			   List<InputSyntax.Term> arguments) {
	return new Term(func,arguments);
    }
	
    /** <b>pre:</b> <code>con.arity() == 0</code>. */
    public Term createTerm(InputSyntax.FunctionalSymbol con) {
	return new Term(con);
    }
	
	
    //   Creating signature and variable symbols:

    
    
	
    /**  @return createPredicate(name,arity,false) */
    public Predicate createPredicate(String name,int arity) {
	return createPredicate(name,arity,false,null);
    } 

    /**  <b>pre:</b> <code>(!infix || arity == 2)</code>.
     */ 
    public Predicate createPredicate(String name,
				     int arity,
				     boolean infix,
				     String negatedInfixName) {
	assert !infix || arity == 2;
		
	return new Predicate(name,arity,infix,negatedInfixName);
    } 
	
    /** Works for constants too, just set <code>arity == 0</code>. */
    public FunctionalSymbol createFunctionalSymbol(String name,int arity) {
	return new FunctionalSymbol(name,arity);
    } 


    public Variable createVariable(String name) {
	return new Variable(name);
    } 


    

    //     Variable instantiation:
    
    
    public static class UninstantiatedVariableException extends Exception {
	
	public UninstantiatedVariableException(InputSyntax.Variable var) {
	    super("Variable " + var + " is uninstantiated.");
	    _variable = var;
	}
	
	public InputSyntax.Variable variable() { 
	    return _variable;
	}

	private InputSyntax.Variable _variable;
    }


    /** @throws UninstantiatedVariableException if some variable in 
     *          <code>lit</code> is not instantiated by <code>subst</code>
     */
    public 
	Literal 
	instantiate(InputSyntax.Literal lit,
		    Map<? extends InputSyntax.Variable,? extends InputSyntax.Term> subst) 
    throws UninstantiatedVariableException {

	
	LinkedList<InputSyntax.Term> newArguments =	
	    new LinkedList<InputSyntax.Term>();
	
	for (InputSyntax.Term arg : lit.predicateArguments())
	    newArguments.addLast(instantiate(arg,subst));


	return createLiteral(lit.isPositive(),
			     lit.predicate(),
			     newArguments);
    } // instantiate(Literal lit,


    
    public 
	InputSyntax.Term
	instantiate(InputSyntax.Term term,
		    Map<? extends InputSyntax.Variable,? extends InputSyntax.Term> subst) 
	throws UninstantiatedVariableException {
	
	if (term.isVariable())
	    {
		InputSyntax.Term instance = subst.get(term.variable());
		
		if (instance == null)
		    throw new UninstantiatedVariableException(term.variable());
		
		return instance;
	    }
	else if (term.isConstant())
	    {
		return term;
	    }
	else
	    {
		// Compound term.
		
		LinkedList<InputSyntax.Term> newArguments =	
		    new LinkedList<InputSyntax.Term>();
		
		for (InputSyntax.Term arg : term.arguments())
		    newArguments.addLast(instantiate(arg,subst));
		
		return createTerm(term.functionalSymbol(),newArguments);
	    }

    } // instantiate(Term term,



    /** @throws UninstantiatedVariableException if some variable in 
     *          <code>lit</code> is not instantiated by <code>subst</code>
     */
    public 
	Literal 
	instantiate1(InputSyntax.Literal lit,
		     Map<String,? extends InputSyntax.Term> subst) 
    throws UninstantiatedVariableException {

	
	LinkedList<InputSyntax.Term> newArguments =	
	    new LinkedList<InputSyntax.Term>();
	
	for (InputSyntax.Term arg : lit.predicateArguments())
	    newArguments.addLast(instantiate1(arg,subst));


	return createLiteral(lit.isPositive(),
			     lit.predicate(),
			     newArguments);
    } // instantiate(Literal lit,


    
    public 
	InputSyntax.Term
	instantiate1(InputSyntax.Term term,
		     Map<String,? extends InputSyntax.Term> subst) 
	throws UninstantiatedVariableException {
	
	if (term.isVariable())
	    {
		InputSyntax.Term instance = subst.get(term.variable().name());
		
		if (instance == null)
		    throw new UninstantiatedVariableException(term.variable());
		
		return instance;
	    }
	else if (term.isConstant())
	    {
		return term;
	    }
	else
	    {
		// Compound term.
		
		LinkedList<InputSyntax.Term> newArguments =	
		    new LinkedList<InputSyntax.Term>();
		
		for (InputSyntax.Term arg : term.arguments())
		    newArguments.addLast(instantiate1(arg,subst));
		
		return createTerm(term.functionalSymbol(),newArguments);
	    }

    } // instantiate(Term term,



}; // class Input
