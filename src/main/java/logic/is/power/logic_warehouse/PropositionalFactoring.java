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


/** Static methods for simplifying clauses by propositional
 *  factoring (removing duplicate literals). 
 */
public class PropositionalFactoring {

    /** Removes all duplicate literals. 
     *  @return true if at least one literal was removed
     */
    public 
	static
	boolean simplify(Iterable<? extends FlattermLiteral> clause) {

	Iterator<? extends FlattermLiteral> iter1 = 
	    clause.iterator();
	
	LinkedList<FlattermLiteral> literalsToRemove =
	    new LinkedList<FlattermLiteral>();
	
	while (iter1.hasNext())
	    {		
		FlattermLiteral lit1 = iter1.next();

		if (lit1.isBuiltInFalse())
		    {
			literalsToRemove.add(lit1); 			
		    }
		else
		    {
			Iterator<? extends FlattermLiteral> iter2 = 
			    clause.iterator();
			
			FlattermLiteral lit2 = iter2.next();
			
			while (lit2 != lit1)
			    {
				if (lit1.equals(lit2))
				    literalsToRemove.add(lit2); 
				
				lit2 = iter2.next();
			    };
		    }

	    }; // while (iter1.hasNext())


	
	if (literalsToRemove.isEmpty()) return false;

	// Note that literalsToRemove can contain duplicates.
	do
	    {
		FlattermLiteral lit = literalsToRemove.removeFirst();
		
		iter1 = clause.iterator(); 
		
		while (iter1.hasNext())
		    if (iter1.next() == lit)
			{
			    iter1.remove();
			    break;
			};
	    }
	while (!literalsToRemove.isEmpty());
	
	return true;
	
    } // simplify(Iterable<? extends FlattermLiteral> clause)

} // class PropositionalFactoring 