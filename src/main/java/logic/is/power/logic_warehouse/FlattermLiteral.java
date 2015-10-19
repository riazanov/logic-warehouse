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

/** Suggested common base for classes representing 
 *  flatterm-based generalised literals in which atoms are 
 *  not necessarily atomic formulas.
 */
public abstract class FlattermLiteral {

    /** @return !isNegative() */
    public final boolean isPositive() { return !isNegative(); }

    public abstract boolean isNegative();

    public abstract Flatterm atom();

    /** @return atom().isEquality() */
    public final boolean isEquality() { 
	return atom().isAtomicFormula() &&
	    atom().predicate().isEquality();
    }


    /** @return true if the literal is positive literal with built-in true 
     *  as the atom, or a negated built-in false.
     */
    public final boolean isBuiltInTrue() { 

	return atom().isAtomicFormula() && 
	    ((atom().predicate().isBuiltInFalse() && isNegative()) ||
	     (atom().predicate().isBuiltInTrue() && isPositive()));
    }

    /** @return true if the literal is positive literal with built-in false 
     *  as the atom, or a negated built-in true.
     */
    public final boolean isBuiltInFalse() { 

	return atom().isAtomicFormula() && 
	    ((atom().predicate().isBuiltInTrue() && isNegative()) ||
	     (atom().predicate().isBuiltInFalse() && isPositive()));
    }

    /** @return !atom().isAtomic() */
    public final boolean isGeneral() {
	return !atom().isAtomicFormula();
    }
    
    /** Checks light-weight semantic equivalence on literals:
     *  non-equality literals are testet fully syntactically
     *  and equality literals are tested modulo symmetry.
     *  <b>pre:</b> <code>lit instanceof FlattermLiteral</code>.
     */
    public final boolean equals(Object lit) {
	assert lit instanceof FlattermLiteral;
	if (isNegative() != ((FlattermLiteral)lit).isNegative())
	    return false;
	
	return 
	    atom().wholeTermEquals(((FlattermLiteral)lit).atom()) ||
	    (isEquality() &&
	     ((FlattermLiteral)lit).isEquality() &&
	     atom().
	     firstArg().
	     wholeTermEquals(((FlattermLiteral)lit).atom().secondArg()) &&
	     atom().
	     secondArg().
	     wholeTermEquals(((FlattermLiteral)lit).atom().firstArg()));
	     
    }

    /** @return isNegative() != lit.isNegative() &&
     *	        atom().wholeTermEquals(lit.atom())
     */
    public final boolean isComplementaryTo(FlattermLiteral lit) {
	return isNegative() != lit.isNegative() &&
	    atom().wholeTermEquals(lit.atom());
    }
    
    /** Checks if this literal is of the form <code>t = t</code>.*/
    public final boolean isTautologicalEquality() {
	return isPositive() && 
	    isEquality() &&
	    atom().firstArg().wholeTermEquals(atom().secondArg());
    }
    
    /** Checks if this literal is of the form <code>t != t</code>.*/
    public final boolean isContradictoryEquality() {
	return isNegative() && 
	    isEquality() &&
	    atom().firstArg().wholeTermEquals(atom().secondArg());
    }
    


} // abstract class FlattermLiteral 