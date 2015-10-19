
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



/**
 * Common base for classes representing functions, individual constants
 * and predicates (but not variables or logic symbols, such as connectives 
 * and quantifiers); such objects sometimes can be given general treatment.
 * Note that {@link logic.is.power.logic_warehouse.SignatureSymbol} is not a base 
 * for {@link logic.is.power.logic_warehouse.Variable}, 
 * {@link logic.is.power.logic_warehouse.Connective} or 
 * {@link logic.is.power.logic_warehouse.Quantifier}.
 */
public abstract class SignatureSymbol extends Symbol {

    public SignatureSymbol(int category,int arity) {
	super(category,arity);
	assert category == Category.Function ||
	    category == Category.IndividualConstant ||
	    category == Category.Predicate;
	_weight = 1;
	_priority = 0;
    }

    /** Sets the weight of the symbol; the weight has no fixed 
     *  meaning - it can be used differently for different purposes.
     *  <b>pre:</b> <code>w > 0</code>. 
     */
    public final void setWeight(int w) { 
	_weight = w; 
    }

    /** The weight of the symbol; the weight has no fixed 
     *  meaning - it can be used differently for different purposes.
     *  @return <code>value > 0</code>
     */
    public final int weight() { return _weight; }
    

    /** Sets the priority of the symbol; can be any value; 
     *  the priority has no fixed meaning - it can be 
     *  used differently for different purposes.
     */
    public final void setPriority(int p) { _priority = p; }

    /** The priority of the symbol; can be any value; 
     *  the priority has no fixed meaning - it can be 
     *  used differently for different purposes; default = 0.
     */
    public final int priority() { return _priority; }


    /** The weight of the symbol; the weight has no fixed 
     *  meaning - it can be used differently for different purposes;
     *  should be > 0; default = 1.     
     */
    private int _weight;

    private int _priority;

}; // class SignatureSymbol
