
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


/** Common abstraction for reduction orderings. */
public abstract class ReductionOrdering {

    public abstract int compare(Term term1,Term term2);

    public abstract int compare(Flatterm term1,Flatterm term2);

    /** Compares the instances of the terms modulo global substitution 2. */
    public
	abstract
	int compareModuloSubst2(Term term1,Term term2);

    /** Checks if the instance of <code>term1</code> wrt global substitution 2,
     *  is greater than the instance of <code>term2</code>.
     */
    public abstract boolean greaterModuloSubst2(Term term1,Term term2);

    /** Checks if the instance of <code>term1</code> wrt global substitution 3,
     *  is greater than the instance of <code>term2</code>.
     */
    public abstract boolean greaterModuloSubst3(Term term1,Term term2);

    /** Checks if there may be a substitution instantiating only variables 
     *  from <code>term1</code>, that would make <code>term1</code> greater 
     *  than <code>term2</code>;
     *  false positives are possible, false negatives are not.
     */
    public abstract boolean canBeGreaterModuloSubst(Term term1,Term term2);


    /** Returns the current statically accessible object (possibly null). */ 
    public static ReductionOrdering current() { return _current; }

    /** Makes <code>ord</code> the current statically accessible object;
     *  <code>ord</code> can be null.
     */ 
    public static void makeCurrent(ReductionOrdering ord) {
	_current = ord;
    }


    //                        Data:

    private static ReductionOrdering _current = null;
    

} // abstract class ReductionOrdering