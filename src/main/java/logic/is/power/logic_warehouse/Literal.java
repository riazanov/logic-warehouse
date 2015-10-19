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
 *  generalised literals in which atoms are not necessarily
 *  atomic formulas.
 */
public abstract class Literal {

    /** @return !isNegative() */
    public final boolean isPositive() { return !isNegative(); }

    public abstract boolean isNegative();

    public abstract Formula atom();


    /** @return atom().isEquality() */
    public boolean isEquality() { 
	return atom().isEquality();
    }

    /** <b>pre:</b> <code>atom().isAtomic() && 
     *              ((AtomicFormula)atom()).predicate().arity() == 2</code>
     */
    public Term firstArg() {
	assert atom().isAtomic();
	assert ((AtomicFormula)atom()).predicate().arity() == 2;
	return ((AtomicFormula)atom()).firstArg();
    }
    
    /** Numeric position of the first argument of the literal's (binary) atom
     *  in the atom.
     *  <b>pre:</b> <code>atom().isAtomic() && 
     *              ((AtomicFormula)atom()).predicate().arity() == 2</code>
     */
    public int firstArgPosition() {
	assert atom().isAtomic();
	assert ((AtomicFormula)atom()).predicate().arity() == 2;
	return ((AtomicFormula)atom()).firstArgPosition();
    }
    
    

    /** <b>pre:</b> <code>atom().isAtomic() && 
     *              ((AtomicFormula)atom()).predicate().arity() == 2</code>
     */
    public Term secondArg() {
	assert atom().isAtomic();
	assert ((AtomicFormula)atom()).predicate().arity() == 2;
	return ((AtomicFormula)atom()).secondArg();
    }


    /** Numeric position of the second argument of the literal's (binary) atom
     *  in the atom.
     *  <b>pre:</b> <code>atom().isAtomic() && 
     *              ((AtomicFormula)atom()).predicate().arity() == 2</code>
     */
    public int secondArgPosition() {
	assert atom().isAtomic();
	assert ((AtomicFormula)atom()).predicate().arity() == 2;
	return ((AtomicFormula)atom()).secondArgPosition();
    }


    /** @return true iff the atom is not an atomic formula, or the literal
     *  is a negation of built-in true or false 
     */
    public final boolean isGeneral() {
	return !atom().isAtomic() ||
	    (isNegative() &&
	     (((AtomicFormula)atom()).predicate().isBuiltInTrue() ||
	      ((AtomicFormula)atom()).predicate().isBuiltInFalse()));
    }
    
    

    /** @return isNegative() == lit.isNegative() &&
     *	    atom().equalsModuloSubst3(lit.atom())
     */
    public final boolean  equalsModuloSubst3(Literal lit) {
	return isNegative() == lit.isNegative() &&
	    ((Term)atom()).equalsModuloSubst3((Term)lit.atom());
    }

} // abstract class Literal 