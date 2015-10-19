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
 * Representation for incremental substitutions with a possibility
 * of backtracking; these substitutions affect instance1() values in 
 * variables. See also {@link logic.is.power.logic_warehouse.Substitution2} and 
 * {@link logic.is.power.logic_warehouse.Substitution3} that affect
 * instance1() and instance2() correspondingly.
 */
public class Substitution1 {

    public Substitution1() {
	_instantiatedVariables = 
	    new Vector<Variable>(64,32); 
	// 64=initial capacity 
	// 32=increment
	_instantiatedVariables.setSize(64);
	_stackSize = 0;
    }

    
    /** Indicates that no variable instantiations are currently registered. */
    public final boolean empty() { return _stackSize == 0; }

    /** Identifies the current state of the substitution;
     *  can be used if we later want to backtrack to this state.
     *  The value becomes invalid and should not be used 
     *  after any longer backtrack or a call to {@link #uninstantiateAll()}.
     */
    public final int savepoint() { return _stackSize; }
      
    /** Undoes all variable instantiations registered since after
     *  the savepoint <code>savepoint<\code> was made.
     *  <b>pre:</b> <code>savepoint<\code> must be a valid savepoint for 
     *       this substitution,
     *       ie, it must have been obtained by a {@link #savepoint()} call
     *       on this object, and there must have been no backtracks
     *       farther than <code>savepoint<\code> since that call to  
     *       {@link #savepoint()}.
     *  <b>post:</b> <code>savepoint() == savepoint<\code>
     */
    public final void backtrackTo(int savepoint) {
	assert _stackSize >= savepoint;
	while (_stackSize > savepoint)
	{
	    --_stackSize;
	    _instantiatedVariables.get(_stackSize).uninstantiate1();
	    _instantiatedVariables.set(_stackSize,null);
	};
    }

    /** Undoes the last variable instantiation. */
    public void backtrack() {
	assert _stackSize > 0;
	--_stackSize;
	_instantiatedVariables.get(_stackSize).uninstantiate1();
	_instantiatedVariables.set(_stackSize,null);
    }


    /** Cancels all registered instantiations. */
    public final void uninstantiateAll() { 
	
	while (_stackSize != 0)
	{
	    --_stackSize;
	    _instantiatedVariables.get(_stackSize).uninstantiate1();
	    _instantiatedVariables.set(_stackSize,null);
	};

    }
      
    /** Calls <code>var.instantiate1(instance)</code> and registers this 
     *  variable assignment.
     *  <b>pre:</b> <code>!var.isInstantiated1()</code>
     *  <b>post:</b> <code>var.isInstantiated1()</code>
     */
    public final void instantiate(Variable var,Flatterm instance) {
	assert !var.isInstantiated1();
	var.instantiate1(instance);
	_instantiatedVariables.setSize(_stackSize + 1);
	_instantiatedVariables.set(_stackSize,var);
	++_stackSize;
    }
    
    /** Checks if some instantiation of <code>var</code> was registered after
     *  the savepoint <code>savepoint</code> was made.
     *  <b>pre:</b> it is not necessary that <code>var</code> be instantiated;
     *       <code>savepoint</code> must be a valid savepoint for 
     *       this substitution,
     *       ie, it must have been obtained by a {@link #savepoint()} call
     *       on this object, and there must have been no backtracks
     *       farther than <code>savepoint</code> since that call to 
     *       {@link #savepoint()}.
     */
    public final boolean variableWasInstantiatedAfter(Variable var,int savepoint) {
	for (int n = savepoint; n < _stackSize; ++n)
	    if (_instantiatedVariables.get(n) == var)
		return true;
	return false;
    }
    
    /** All variables instantiated after the savepoint. */
    public 
	final 
	List<Variable> 
	variablesInstatiatedAfter(int savepoint) {
	LinkedList<Variable> result = new LinkedList<Variable>();
	for (int n = savepoint; n < _stackSize; ++n)
	    result.addLast(_instantiatedVariables.get(n));
	return result;
    }

    public String toString() {
	String result = "";
	for (int n = 0; n < _stackSize; ++n)
	{
	    result += _instantiatedVariables.get(n) +
		" <- " +
		_instantiatedVariables.get(n).instance1();
	    if (n + 1 < _stackSize)
		result += ", ";
	};
	return result;
    }
    
    
    //                         Data:


    /** Memory of the stack for instantiated variables. */
    private Vector<Variable> _instantiatedVariables;
    
    /** Current size of the stack for instantiated variables. */
    private int _stackSize;

}; // class Substitution1
