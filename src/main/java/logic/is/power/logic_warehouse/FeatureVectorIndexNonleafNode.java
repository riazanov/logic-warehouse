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


/** Nonleaf nodes of {@link logic.is.power.logic_warehouse.FeatureVectorIndexNode}. */
/* package */ class FeatureVectorIndexNonleafNode<IndexedObject> 
		  implements FeatureVectorIndexNode<IndexedObject>
{

    public 
	FeatureVectorIndexNonleafNode(int value,
				      FeatureVectorIndexNode<IndexedObject> treeBelow,				 
				      FeatureVectorIndexNonleafNode<IndexedObject> greater)
	{
	    _value = value;
	    _treeBelow = treeBelow;
	    _greater = greater;

	    assert _greater == null || _greater.value() > _value; 
	}

    public final int value() { return _value; }

    public final FeatureVectorIndexNode<IndexedObject> treeBelow() {
	return _treeBelow;
    }
    
    public 
	final 
	void 
	setTreeBelow(FeatureVectorIndexNode<IndexedObject> treeBelow) {
	_treeBelow = treeBelow;
    }

    public final FeatureVectorIndexNonleafNode<IndexedObject> greater() {
	return _greater;
    }

    public 
	final 
	void 
	setGreater(FeatureVectorIndexNonleafNode<IndexedObject> greater) {
	_greater = greater;
	assert _greater == null || _greater.value() > _value; 
    }
    
    


    private int _value;
    
    private FeatureVectorIndexNode<IndexedObject> _treeBelow;

    private FeatureVectorIndexNonleafNode<IndexedObject> _greater;

} // class FeatureVectorIndexNonleafNode<IndexedObject> 
