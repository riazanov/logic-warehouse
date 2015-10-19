
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


import java.util.HashMap;


/**
 * Keeps an injective mapping from native variables 
 * (logic.is.power.logic_warehouse.Variable)
 * to themselves; the source of fresh native variables can be specified.
 */
public class VariableRenaming {


    /** Makes the renaming empty; does not reset <code>freshVarBank</code>.
     *  @param freshVarBank <code>!= null</code>, will be used to get 
     *         fresh native variables until the next call to
     *         <code>reset(Variable.Bank)</code>
     */
    public VariableRenaming() {
	_freshVariableBank = null;
	_map = new HashMap<Variable,Variable>();
    }



    /** Makes the renaming empty; does not reset <code>freshVarBank</code>.
     *  @param freshVarBank <code>!= null</code>, will be used to get 
     *         fresh native variables until the next call to
     *         <code>reset(Variable.Bank)</code>
     */
    public final void reset(Variable.Bank freshVarBank) {
	assert freshVarBank != null;
	_freshVariableBank = freshVarBank;
	_map.clear();
    }
    
    /** Makes the renaming empty; does not change the associated
     *  variable bank.
     */
    public final void clear() { _map.clear(); }
    
    
    /** Maps the variable into its new name; if <code>var</code> has already
     *  been mapped, the same variable is returned;
     *  if <code>var</code> has not been mapped since the last
     *  reset(Variable.Bank), then a fresh variable
     *  is reseved in the current variable bank.
     */
    public final Variable rename(Variable var) {
	assert var != null;

	Variable result = _map.get(var);

	if (result == null) 
	{
	    // New variable, has not been mapped yet.
	    result = _freshVariableBank.reserveVariable();
	    _map.put(var,result);
	};

	return result;

    } // rename(Variable var)
    
    


    //                      Data:

    private Variable.Bank _freshVariableBank;

    private HashMap<Variable,Variable> _map;

}; // class VariableRenaming
