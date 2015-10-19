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
 * Common base for classes representing different kinds of symbols:
 * variables, functions, predicates, individual constants, as well as 
 * connectives and quantifiers.
 */
public abstract class Symbol implements Comparable<Symbol> {

    public static class Category {

	public static final int Variable = 0;

	public static final int Function = 1;

	public static final int IndividualConstant = 2;

	public static final int Predicate = 3;

	public static final int Connective = 4;

	public static final int Quantifier = 5;
    };
    
    /** Creates a symbol of the specified category and arity.
     *  <b>pre:</b> <code>arity</code> must be 0 if 
     *  <code>cat == Variable || cat == IndividualConstant</code>.
     */
    public Symbol(int cat,int arity) {

	assert 
	    (cat != Category.Variable && 
	     cat != Category.IndividualConstant) ||
	    arity == 0;

	_category = cat;
	_arity = arity;
	_uniqueObjectId = _nextUniqueObjectId;
	++_nextUniqueObjectId;
    }

    /** Creates a nullary symbol of the specified category. */
    public Symbol(int cat) {
	_category = cat;
	_arity = 0;
	_uniqueObjectId = _nextUniqueObjectId;
	++_nextUniqueObjectId;
    }


    public final int category() { return _category; }    

    public final boolean isVariable() { 
	return _category == Category.Variable;
    }

    public final boolean isConnective() { 
	return _category == Category.Connective;
    }

    public final boolean isQuantifier() { 
	return _category == Category.Quantifier;
    }

    public final int arity() { return _arity; }
    
    public abstract String name();
    
    /** @return >= 0 */
    public final int hashCode() { return _uniqueObjectId; }
     
    public final int numericId() { return _uniqueObjectId; }

    /** Normalised hash code.
     *  <b>pre:</b> <code>modulus > 0</code>
     *  @return abs(hashCode() % modulus)
     */
    public final int category(int modulus) {
	assert modulus > 0;
	return Math.abs(hashCode() % modulus);
    }

 
    public final boolean equals(Object obj) {
	return this == obj; 
	// may be changed in the future if there is a need
	// for signature mapping.
    }

    public final int compareTo(Symbol sym) {
	return _uniqueObjectId - sym._uniqueObjectId;
	// may be changed in the future if there is a need
	// for signature mapping.
    }


    /** Attaches the application-specific info to the symbol. */
    public final void setInfo(Object obj) {
	_info = obj;
    }
    
    /** Application-specific info attached to the symbol. */
    public final Object info() {
	return _info;
    }

    public String toString() { return name(); }
    

    


   
    //                  Data:


    private static int _nextUniqueObjectId = 0;
    
    private int _category;
    
    private int _arity;

    private int _uniqueObjectId; 
    
    /** Application-specific info that may be attached to a symbol. */
    private Object _info;

}; // class Symbol

