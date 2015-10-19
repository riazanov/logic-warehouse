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


/** Partial ordering on collections of literals obtained as a multiset
 *  extension of the specified literal ordering. 
 */
public class LiteralOrderingMultisetExtension 
    implements LiteralCollectionOrdering { 

    public LiteralOrderingMultisetExtension(LiteralOrdering litOrd)
    {
	_literalOrdering = litOrd;
    }


    /** <b>pre:</b> for each <code>lit</code> in <code>litCol1</code>,
     *  <code>litCol2</code>, <code>!lit.isGeneral()</code> must hold. 
     */
    public 
	int 
	compare(Collection<? extends Literal> litCol1,
		Collection<? extends Literal> litCol2) {
	      
	if (litCol1.isEmpty())
	    {
		return 
		    (litCol2.isEmpty())?  
		    ComparisonValue.Equivalent
		    :
		    ComparisonValue.Smaller;
	    }
	else if (litCol2.isEmpty())
	    return ComparisonValue.Greater;
	    

	// Make copies of the collections so that we can
	// remove literals from them:
	
	LinkedList<Literal> tmpLitCol1 = new LinkedList<Literal>(litCol1);
	LinkedList<Literal> tmpLitCol2 = new LinkedList<Literal>(litCol2);
	
	//  Minimise the comparison by removing equivalent literals on both sides:

	Iterator<Literal> iter1 = tmpLitCol1.iterator();
	
	do
	    {
		Literal lit1 = iter1.next();

		Iterator<Literal> iter2 = tmpLitCol2.iterator();

		do
		    {
			Literal lit2 = iter2.next();

			int cmp = 
			    _literalOrdering.compare(lit1,lit2);
			
			if (cmp == ComparisonValue.Equivalent)
			    {
				iter1.remove();
				iter2.remove();
				
				if (tmpLitCol1.isEmpty())
				    {
					if (tmpLitCol2.isEmpty())
					    {
						return ComparisonValue.Equivalent;
					    }
					else
					    return ComparisonValue.Smaller;
				    }
				else if (tmpLitCol2.isEmpty())
				    {
					return ComparisonValue.Greater;
				    };

				break;
			    }; // if (cmp == ComparisonValue.Equivalent)
		    }
		while (iter2.hasNext());
	    }
	while (iter1.hasNext());

	// Now tmpLitCol1 and tmpLitCol2 don't have common literals 
	// modulo ComparisonValue.Equivalent.

	iter1 = tmpLitCol1.iterator();
	
	do
	    {
		Literal lit1 = iter1.next();

		Iterator<Literal> iter2 = tmpLitCol2.iterator();

		do
		    {
			Literal lit2 = iter2.next();

			int cmp = 
			    _literalOrdering.compare(lit1,lit2);
			
			assert cmp != ComparisonValue.Equivalent;

			if (cmp == ComparisonValue.Greater)
			    {
				iter2.remove();
				
				if (tmpLitCol2.isEmpty()) return ComparisonValue.Greater;
			    }
			else if (cmp == ComparisonValue.Smaller)
			    {
				iter1.remove();

				if (tmpLitCol1.isEmpty()) return ComparisonValue.Smaller;

				break;
			    };
		    }
		while (iter2.hasNext());
	    }
	while (iter1.hasNext());

	assert !tmpLitCol1.isEmpty();
	assert !tmpLitCol2.isEmpty();
	
	// No literal in tmpLitCol1 is comparable with a literal in tmpLitCol2.

	return ComparisonValue.Incomparable;

    } // compare(Collection<? extends Literal> litCol1,..
    
    



    
    //        Data:

    private LiteralOrdering _literalOrdering;

} // class LiteralOrderingMultisetExtension 