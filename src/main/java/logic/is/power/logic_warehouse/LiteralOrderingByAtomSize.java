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

/** Literal ordering that compares atom sizes. */
public class LiteralOrderingByAtomSize implements LiteralOrdering {


    /** <b>pre:</b> <code>!lit1.isGeneral() && !lit2.isGeneral()</code> */
    public 
	final
	int 
	compare(Literal lit1,Literal lit2) {
	return Util.compare(lit1.atom().numberOfSymbols(),
			    lit2.atom().numberOfSymbols());
    }
    
    /** <b>pre:</b> <code>!lit1.isGeneral() && !lit2.isGeneral()</code> */
    public 
	final
	int 
	compare(FlattermLiteral lit1,FlattermLiteral lit2) {
	return Util.compare(lit1.atom().numberOfSymbols(),
			    lit2.atom().numberOfSymbols());
    }



} // class LiteralOrderingByAtomSize