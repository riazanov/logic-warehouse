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
   * Common base for classes representing functions <em>and individual
   * constants</em>; such objects sometimes can be given general treatment.
   */

public abstract class FunctionalSymbol extends SignatureSymbol {

    
    /** Creates a symbol of the specified category and arity.
     *  <b>pre:</b> <code>cat</code> must have an appropriate value, ie,
     *  <code>cat == IndividualConstant || cat == Function</code>.
     *  <b>pre:</b> <code>arity</code> must be 0 if 
     *  <code>cat == IndividualConstant</code>.
     */
    public FunctionalSymbol(int cat,int arity) {
	super(cat,arity);

	assert  
	    cat == Symbol.Category.IndividualConstant || 
	    cat == Symbol.Category.Function;
	assert 
	    cat != Symbol.Category.IndividualConstant || arity == 0;
    }

    /** Creates an individual constant.  */
    public FunctionalSymbol() {
	super(Symbol.Category.IndividualConstant,0);
    }

    
    public abstract String name();


}; // class Symbol

