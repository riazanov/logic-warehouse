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

/** Literal ordering obtained as a composition of the parameter orderings:
 *  if the first ordering produces 
 *  {@link logic.is.power.cushion.ComparisonValue.Equivalent}, we proceed with
 *  the second, etc, until some other value is computed or the last
 *  member ordering is computed.
 */
public class LiteralOrderingComposition {

    /** This ordering will be computed as a composition of 
     *  <code>members</code>.
     * @param members may be empty, in which case this ordering
     *        will always compute {@link logic.is.power.cushion.ComparisonValue.Equivalent}
     */
    public 
	LiteralOrderingComposition(Collection<LiteralOrdering> members) {
	
	assert !members.isEmpty();
	
	_members = new LinkedList<LiteralOrdering>(members);
    }

    
    /** <b>pre:</b> <code>!lit1.isGeneral() && !lit2.isGeneral()</code> */
    public 
	final
	int 
	compare(Literal lit1,Literal lit2) {
	for (LiteralOrdering mem : _members)
	    {
		int cmp = mem.compare(lit1,lit2);
		if (cmp != ComparisonValue.Equivalent)
		    return cmp;
	    };
	return ComparisonValue.Equivalent;
    }
    
    /** <b>pre:</b> <code>!lit1.isGeneral() && !lit2.isGeneral()</code> */
    public 
	final
	int 
	compare(FlattermLiteral lit1,FlattermLiteral lit2) {
	for (LiteralOrdering mem : _members)
	    {
		int cmp = mem.compare(lit1,lit2);
		if (cmp != ComparisonValue.Equivalent)
		    return cmp;
	    };
	return ComparisonValue.Equivalent;
    }
    
    
    private LinkedList<LiteralOrdering> _members;


} // class LiteralOrderingComposition 