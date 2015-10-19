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

/** Literal collection ordering obtained by flipping values of 
 *  the parameter ordering. 
 */
public class LiteralCollectionOrderingFlip 
    implements LiteralCollectionOrdering {

    /** This ordering will be computed by flipping the values computed
     *  by <code>ord</code>.
     */
    public LiteralCollectionOrderingFlip(LiteralCollectionOrdering ord) {
	_baseOrdering = ord;
    }

    public 
	final
	int 
	compare(Collection<? extends Literal> litCol1,
		Collection<? extends Literal> litCol2) {
	return ComparisonValue.flip(_baseOrdering.compare(litCol1,litCol2));
    }
    
    private LiteralCollectionOrdering _baseOrdering;

} // class LiteralCollectionOrderingFlip 