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
 * Representation of connective symbols (including nullary ones).
 */

public class Connective extends Symbol {

    public static class Id {
	public static final int Not = 0;
	public static final int And = 1;
	public static final int Or = 2;
	public static final int Equivalent = 3;
	public static final int Implies = 4;
	public static final int ReverseImplies = 5;
	public static final int NotEquivalent = 6;
	public static final int NotOr = 7;
	public static final int NotAnd = 8;
    };
    
    public final int id() { return _id; }
    
    public String name() {
	switch (_id) 
	{
	    case Id.Not: return "~"; 
	    case Id.And: return "&";
	    case Id.Or: return "|";
	    case Id.Equivalent: return "<=>";
	    case Id.Implies: return "=>";
	    case Id.ReverseImplies: return "<=";
	    case Id.NotEquivalent: return "<~>";
	    case Id.NotOr: return "~|";
	    case Id.NotAnd: return "~&";
	};
	assert false;
	return null;
    }

    public final boolean isAssociative() { 
      return _id == Id.And || _id == Id.Or;
    }

    public static Connective get(int id) {
	switch (id) 
	{
	    case Id.Not: return getNot();
	    case Id.And: return getAnd();
	    case Id.Or: return getOr();
	    case Id.Equivalent: return getEquivalent();
	    case Id.Implies: return getImplies();
	    case Id.ReverseImplies: return getReverseImplies();
	    case Id.NotEquivalent: return getNotEquivalent();
	    case Id.NotOr: return getNotOr();
	    case Id.NotAnd: return getNotAnd();
	};
	assert false;
	return null;	
    }

    public static Connective getNot() {
	return _not;
    }


    public static Connective getAnd() {
	return _and;
    }


    public static Connective getOr() {
	return _or;
    }


    public static Connective getEquivalent() {
	return _equivalent;
    }


    public static Connective getImplies() {
	return _implies;
    }


    public static Connective getReverseImplies() {
	return _reverseImplies;
    }


    public static Connective getNotEquivalent() {
	return _notEquivalent;
    }


    public static Connective getNotOr() {
	return _notOr;
    }


    public static Connective getNotAnd() {
	return _notAnd;
    }





    //                   Private methods:


    /** Note that this constructor is private; objects of this class
     *  can only be created/destroyed inside other methods.
     */
    private Connective(int id,int arity) {
	super(Symbol.Category.Connective,arity);
	_id = id;
    }


    //                   Data:

    private int _id;
    
    private static Connective _not = new Connective(Id.Not,1);
    private static Connective _and = new Connective(Id.And,2);
    private static Connective _or = new Connective(Id.Or,2);
    private static Connective _equivalent = new Connective(Id.Equivalent,2);
    private static Connective _implies = new Connective(Id.Implies,2);
    private static Connective _reverseImplies = 
    new Connective(Id.ReverseImplies,2);
    private static Connective _notEquivalent = 
    new Connective(Id.NotEquivalent,2);
    private static Connective _notOr = new Connective(Id.NotOr,2);
    private static Connective _notAnd = new Connective(Id.NotAnd,2);
    
    
}; // class Symbol

