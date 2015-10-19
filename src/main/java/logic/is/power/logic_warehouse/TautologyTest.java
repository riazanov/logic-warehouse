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

/** Static methods for detecting propositional and equational tautologies. */
public class TautologyTest {

    /** Checks if the list of literals contains a pair of complementary 
     *  literals.
     */
    public 
	static 
	boolean 
	isPropositionalTautology(Iterable<? extends FlattermLiteral> clause) 
    {
	Iterator<? extends FlattermLiteral> iter1 = 
	    clause.iterator();
	
	while (iter1.hasNext())
	    {		
		FlattermLiteral lit1 = iter1.next();

		if (lit1.isBuiltInTrue()) return true;

		Iterator<? extends FlattermLiteral> iter2 = 
		    clause.iterator();
		
		FlattermLiteral lit2 = iter2.next();
		
		while (lit2 != lit1)
		    {
			if (lit1.isComplementaryTo(lit2))
			    return true;

			lit2 = iter2.next();
		    };

	    }; // while (iter1.hasNext())

	return false;

    } // isPropositionalTautology(Iterable<? extends FlattermLiteral> clause)
    

    /** Checks if one of the literals is of the form <code>t == t</code>. */
    public 
	static 
	boolean isEquationalTautology(Iterable<? extends FlattermLiteral> clause) 
    {
	for (FlattermLiteral lit : clause)
	    if (lit.isTautologicalEquality())
		return true;
	return false;
    }

} // class TautologyTest