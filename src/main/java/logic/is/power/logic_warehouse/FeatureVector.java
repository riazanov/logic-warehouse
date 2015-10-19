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


/** A very general abstraction of feature vectors to be used
 *  in {@link logic.is.power.logic_warehouse.FeatureVectorIndex};
 *  the nested class provides a simple instance. 
 */
public interface FeatureVector {

    public int length();

    public int get(int n);
    

    public static class ArrayBased implements FeatureVector {
    
	public ArrayBased(int[] array) {
	    _array = array;
	}

	public ArrayBased(int length) {
	    _array = new int[length];
	}
	
	public final int length() { return _array.length; }

	public final int get(int n) { return _array[n]; }

	public final void set(int n,int value) { _array[n] = value; }

	public String toString() {
	    String result = "[";
	    for (int i = 0; i < length(); ++i)
		{
		    if (i > 0)
			result += ", ";
		    result += get(i);
		};
	    return result + "]";
	}
	

	private final int[] _array;


    } // class ArrayBased



} // interface FeatureVector