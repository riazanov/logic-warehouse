
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
 * Abstract syntax for first-order logic, intended to be used
 * to isolate various modules processing FOL formulas from 
 * the actual representation of the formulas.
 */
public interface InputSyntax {

    
    /** Does not include associative connectives. */
    public static class BinaryConnective {
	public static final int Equivalent = 0;
	public static final int Implies = 1;
	public static final int ReverseImplies = 2;
	public static final int NotEquivalent = 3;
	public static final int NotOr = 4;
	public static final int NotAnd = 5;
		
	public static String toString(int val) {
	    switch (val) 
	    {
		case Equivalent: return "<=>";
		case Implies: return "=>";
		case ReverseImplies: return "<=";
		case NotEquivalent: return "<~>";
		case NotOr: return "~|";
		case NotAnd: return "~&";
	    };
	    assert false;
	    return null;
	}
	

    };

    
    public static class AssociativeConnective {
	public static final int And = 0;
	public static final int Or = 1;
	
	public static String toString(int val) {
	    switch (val) 
	    {
		case And: return "&";
		case Or: return "|";
	    };
	    assert false;
	    return null;
	}
	
    };

    public static class Quantifier {
	
	public static final int ForAll = 0;
	
	public static final int Exist = 1;

	public static String toString(int val) {
	    switch (val) 
	    {
		case ForAll: return "!";
		case Exist: return "?";
	    };
	    assert false;
	    return null;
	}
	
    };

    public interface Formula {

	/** Implementation must check if this is a quantified formula. */
	public boolean isQuantified();

	/** Implementation must check if this is an application of negation. */
	public boolean isNegated();

	/** Implementation must check if this is an application 
	 *  of a binary connective. 
	 */
	public boolean isBinary();

        /** Implementation must check if this is an application
         *  of an associative connective.
	 */
	public boolean isAssociative();

	/** Implementation must check if this is an atomic formula. */
	public boolean isAtomic();
	
	/** <b>pre:</b> <code>isQuantified()</code>. */
	public int quantifier();
	
	/** <b>pre:</b> <code>isQuantified()</code>. */
	public List<Variable> quantifiedVariables();
	
	/** <b>pre:</b> <code>isQuantified()</code>. */
	public Formula matrix();

	/** <b>pre:</b> <code>isNegated()</code>. */
	public Formula formulaUnderNegation(); 
	
	/** <b>pre:</b> <code>isBinary()</code>. */
	public int binaryConnective();

	/** <b>pre:</b> <code>isBinary()</code>. */
	public Formula leftArgument(); 
	
	/** <b>pre:</b> <code>isBinary()</code>. */
	public Formula rightArgument(); 

	/** <b>pre:</b> <code>isAssociative()</code>. */
	public int associativeConnective();

	/** <b>pre:</b> <code>isAssociative()</code>. */
	public List<Formula> connectiveArguments(); 
			
	/** <b>pre:</b> <code>isAtomic()</code>. */
	public Predicate predicate();

	/** <b>pre:</b> <code>isAtomic()</code>. */
	public List<Term> predicateArguments();
 
	public Set<Variable> freeVariables();

	/** @param closed indicates whether grouping is required */
	public String toString(boolean closed);
	
    }; // interface Formula




    public interface Literal {
	
	public boolean isPositive();

	public boolean isNegative();
	
	public Predicate predicate(); 

	public List<Term> predicateArguments(); 

	public Set<Variable> freeVariables();

	/** @param closed indicates whether grouping is required */
	public String toString(boolean closed);
	
    }; // interface Literal 




    public interface Term {
   
	public boolean isVariable();
	
	public boolean isNonVariable();

	/** @return isNonVariable() && functionalSymbol().arity() == 0 */
	public boolean isConstant();

	/** <b>pre:</b> isVariable(). */
	public Variable variable();

	/** May be a constant. <b>pre:</b> isNonVariable(). */
	public FunctionalSymbol functionalSymbol();
	
	/** May be null or simply empty if <code>functionalSymbol()</code> 
	 *  is a constant.
	 *  <b>pre:</b> <code>isNonVariable()</code>. 
	 */
	public List<Term> arguments();

	/** All free variables occuring in the term. */
	public Set<Variable> freeVariables();
	
	public boolean equals(Object obj);
	
    }; // interface Term
 


    public interface Predicate extends Comparable<Predicate> {

	public String name();

	public int arity();

	public boolean isInfix();

	/** If <code>isInfix() == true</code> and 
	 *  <code>negatedInfixName() != null</code>, then negative literals
	 *  with this predicate should be printed using 
	 *  <code>negatedInfixName()</code> infixly.
	 */
	public String negatedInfixName();
	
	public boolean equals(Object obj);

    }; // interface Predicate

    
    /** To represent both constants and nonnullary functions. */
    public interface FunctionalSymbol extends Comparable<FunctionalSymbol> {
	
	public String name();

	public int arity();
	
	public boolean equals(Object obj);

    }; 

   

    public interface Variable extends Comparable<Variable> {
	
	public String name();
	
	public int hashCode();

	public int compareTo(Variable var);
	
	public boolean equals(Object obj);
	
    };
    

}; // interface InputSyntax
