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

/** Values returned by comparisons of numeric-valued functions. */
public class FunctionComparisonValue {
    /** <code>f(..) < g(..)</code> for any values of the arguments. */
    public static final int AlwaysSmaller = -2;
	/** <code>f(..) <= g(..)</code> for any values of the arguments. */
    public static final int CanBeSmallerOrEquivalent = -1;
	/** <code>f(..) == g(..)</code> for any values of the arguments. */
    public static final int AlwaysEquivalent = 0;
	/** <code>f(..) >= g(..)</code> for any values of the arguments. */
    public static final int CanBeGreaterOrEquivalent = 1;
	/** <code>f(..) > g(..)</code> for any values of the arguments. */
    public static final int AlwaysGreater = 2;
	/** <code>f(..)</code> and <code>g(..)</code> can compare
	 *  differently for different values of the arguments, ie,
	 *  we may have <code>f(..) < g(..)</code> for some values
	 *  and <code>f(..) > g(..)</code> for some other values.
	 */
    public static final int Volatile = 3;
    
    public static int flip(int val) {
	switch (val)
	    {
	    case AlwaysSmaller: return AlwaysGreater;
	    case CanBeSmallerOrEquivalent: return CanBeGreaterOrEquivalent;
	    case AlwaysEquivalent: return AlwaysEquivalent;
	    case CanBeGreaterOrEquivalent: return CanBeSmallerOrEquivalent;
	    case AlwaysGreater: return AlwaysSmaller;
	    case Volatile: return Volatile;
	    };
	assert false;
	return -1000;
    }

    
    /** More rough comparison value. */
    public final int toComparisonValue(int functionComparisonValue) {
	switch (functionComparisonValue)
	    {
	    case AlwaysSmaller: return ComparisonValue.Smaller;
	    case CanBeSmallerOrEquivalent: return ComparisonValue.Incomparable;
	    case AlwaysEquivalent: return ComparisonValue.Equivalent;
	    case CanBeGreaterOrEquivalent: return ComparisonValue.Incomparable;
	    case AlwaysGreater: return ComparisonValue.Greater;
	    case Volatile: return ComparisonValue.Incomparable;
	    };
	assert false;
	return -1000;
    }

} // class FunctionComparisonValue