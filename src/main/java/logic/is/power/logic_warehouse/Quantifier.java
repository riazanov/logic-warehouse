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
 * Representation of quantifier symbols.
 */
public class Quantifier extends Symbol  {

    public static class Id 
    { 
	public static final int ForAll = 0;
	public static final int Exist = 1;
    };

    public final int id() { return _id; }

    // Abstract in Symbol:

    public String name() {
	switch (_id) 
	{
	case Id.ForAll: return "!"; 
	case Id.Exist: return "?";
	};
	assert false;
	return null;
    }



    public static Quantifier get(int id) {
	switch (id) 
	{
	    case Id.ForAll: return getForAll();
	    case Id.Exist: return getExist();
	};
	assert false;
	return null;	
    }

    public static Quantifier getForAll() { return _forAll; }


    public static Quantifier getExist() { return _exist; }


    //               Private methods:
    

    /** Note that this constructor has package; objects of this class
     *  can only be created/destroyed inside other methods.
     */
    Quantifier(int id) {
	super(Symbol.Category.Quantifier,1);
	_id = id;
    }
    
    //               Data:

    private int _id;

    private static Quantifier _forAll = new Quantifier(Id.ForAll);
    private static Quantifier _exist = new Quantifier(Id.Exist);

}; // class Quantifier

