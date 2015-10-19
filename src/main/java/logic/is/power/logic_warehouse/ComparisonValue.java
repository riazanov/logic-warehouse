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

/** Values of various order-based comparison operations,
 *  in particular, reduction orderings on terms.
 */
public class ComparisonValue {

    public static final int Smaller = -1;
	/** Means that values being compared are indistinguishable
	 *  wrt the ordering; in general, this is weaker than real 
	 *  equality.
	 */
    public static final int Equivalent = 0;
    public static final int Greater = 1;
    public static final int Incomparable = 2;

    /** If <code>c == compare(x,y)</code>, then 
     *  <code>c.flip() == compare(y,x)</code>, provided that
     *  <code>compare</code> is a reasonable partial ordering.
     */
    public static int flip(int val) {
	switch (val)
	    {
	    case Smaller: return Greater;
	    case Equivalent: return Equivalent;
	    case Greater: return Smaller;
	    case Incomparable: return Incomparable;
	    }
	assert false;
	return -10;
    } // flip(int val)

  

} // class ComparisonValue