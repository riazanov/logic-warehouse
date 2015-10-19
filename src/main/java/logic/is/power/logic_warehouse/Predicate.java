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


public class Predicate extends SignatureSymbol {

    //          Abstract in FunctionalSymbol:

    public final String name() { return _name; }

    public final boolean isEquality() { return _kind == Kind.EQUALITY; }

    public final boolean isInfix() { return _isInfix; }

    public final boolean isBuiltInTrue() { return _kind == Kind.BUILT_IN_TRUE; }

    public final boolean isBuiltInFalse() { return _kind == Kind.BUILT_IN_FALSE; }

    
    public static class Kind {
	
	public static final int EQUALITY = 0; 
	public static final int REGULAR = 1;
	public static final int BUILT_IN_TRUE = 2;
	public static final int BUILT_IN_FALSE = 3;
	
    } // class Kind


    //               Package access methods:


    /** Note that this constructor is private; objects of this class
     *  can only be created/destroyed inside
     *  {@link logic.is.power.logic_warehouse.Signature}.
     */
    Predicate(String name,int arity,int kind,boolean infix) {
	super(Symbol.Category.Predicate,arity);
	_name = name;
	_kind = kind;
	_isInfix = infix;
    }

    final void makeInfix() { _isInfix = true; }
    
    //               Data:


    private final String _name;

    private final int _kind;

    private boolean _isInfix;


}; // class Predicate

