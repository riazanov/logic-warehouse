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




/** Various static utilities. */
public class Util {

    public static int compare(int x,int y) {
	if (x > y) return ComparisonValue.Greater;
	if (x < y) return ComparisonValue.Smaller;
	return ComparisonValue.Equivalent;
    }

    public static int compare(long x,long y) {
	if (x > y) return ComparisonValue.Greater;
	if (x < y) return ComparisonValue.Smaller;
	return ComparisonValue.Equivalent;
    }

    public static int compare(float x,float y) {
	if (x > y) return ComparisonValue.Greater;
	if (x < y) return ComparisonValue.Smaller;
	return ComparisonValue.Equivalent;
    }

    public static int compare(double x,double y) {
	if (x > y) return ComparisonValue.Greater;
	if (x < y) return ComparisonValue.Smaller;
	return ComparisonValue.Equivalent;
    }


} // class Util