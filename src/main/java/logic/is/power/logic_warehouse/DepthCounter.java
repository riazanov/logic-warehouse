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


/** Counts depth when a term is traversed left-to-right depth-first. */
public class DepthCounter {

    /** The current depth value is set to 0. */
    public DepthCounter() {	
	_currentDepth = 0;
	_numberOfTopHoles = 1; 
	_holeStack = new int[32]; 
	_holeStackSize = 0;	
    }

    /** The current depth value is set to <code>d</code>. */
    public DepthCounter(int d) {	
	_currentDepth = d;
	_numberOfTopHoles = 1;	
	_holeStack = new int[32]; 
	_holeStackSize = 0;		
    }

    /** The current depth value is set to 0. */
    public final void reset() {
	_currentDepth = 0;
	_numberOfTopHoles = 1;
	_holeStackSize = 0;	
    }
    
    /** The current depth value is set to <code>d</code>. */
    public final void reset(int d) {
	_currentDepth = 0;
	_numberOfTopHoles = 1;
	_holeStackSize = 0;	
    }
    
    /** The current value of the counter. */
    public final int currentDepth() {
	return _currentDepth;
    }

    /** Informs the depth counter that a variable has just been examined 
     *  within the traversal.
     */
    public final void passVariableOrIndividualConstant() {
	assert _numberOfTopHoles > 0;
	--_numberOfTopHoles;
	if (_numberOfTopHoles == 0)
	    {
		// Need to go up.
		while (_holeStackSize != 0 && _numberOfTopHoles == 0)
		    {
			--_currentDepth;
			--_holeStackSize;
			_numberOfTopHoles = _holeStack[_holeStackSize];
		    };
		assert _currentDepth >= 0;
	    };
    } // passVariableOrIndividualConstant()


    /** Informs the depth counter that a function or a predicate
     *  with the specified arity has just been examined within the traversal.
     */
    public final void passFunctionOrPredicate(int arity) {
	assert _numberOfTopHoles > 0;
	if (arity == 0)
	    {
		passVariableOrIndividualConstant();
		return;
	    };
	--_numberOfTopHoles;
	saveHoles();
	_numberOfTopHoles = arity;
	++_currentDepth;
	
    } // passFunctionOrPredicate(int arity)

    public final void passConnective(int arity) {
	passFunctionOrPredicate(arity);
    } 

    public final void passQuantifier() {
	assert _numberOfTopHoles > 0;
	saveHoles();
	_numberOfTopHoles = 1;
	++_currentDepth;
    } 

    public final void passAbstraction() {
	assert _numberOfTopHoles > 0;
	saveHoles();
	_numberOfTopHoles = 1;
	++_currentDepth;
    } 



    //                   Private methods:

    private void saveHoles() {
	if (_holeStackSize == _holeStack.length)
	    {
		// Resize:
		int[] oldStack =  _holeStack;
		_holeStack = new int[_holeStackSize + 32];
		for (int i = 0; i < _holeStackSize; ++i)
		    _holeStack[i] = oldStack[i];
	    };
	_holeStack[_holeStackSize] = _numberOfTopHoles;
	++_holeStackSize;
    } // saveHoles()

    

    //                  Data:


    private int _currentDepth;
    
    private int _numberOfTopHoles;

    private int[] _holeStack; 
    
    private int _holeStackSize;

} // class DepthCounter 