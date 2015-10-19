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

import logic.is.power.cushion.*;

/** Generic feature vector index. */
public class FeatureVectorIndex<IndexedObject> {
  
    public FeatureVectorIndex(int numberOfFeatures) {
	_tree = null;
	_numberOfFeatures = numberOfFeatures;
    }

    /** Inserts the feature vector into the index; returns the reference
     *  where the indexed value can be placed.
     *  <b>pre:<b> the feature vector can be already in the index,
     *             in which case the returned value is still valid
     */
    public final Ref<IndexedObject> insert(FeatureVector key){

	assert key.length() == _numberOfFeatures;

	Ref<Ref<IndexedObject>> ref = 
	    new Ref<Ref<IndexedObject>>();

	_tree = insertIntoTree(_tree,key,0,ref);

	assert ref.content != null;

	return ref.content;

    } // insert(FeatureVector key)
   


    /** Tries to reach an indexed object corresponding to the given 
     *  feature vector.
     *  @return null if the index does not contain this vector
     */
    public final Ref<IndexedObject> find(FeatureVector key) {
	
	return findInTree(_tree,key,0);


    } // find(FeatureVector key)
 

    /** Tries to remove the feature vector from the index;
     *  if succedes, the indexed value is returned in 
     *  <code>value</code>.
     */
    public final boolean remove(FeatureVector key,
				Ref<IndexedObject> value) {

	Ref<Boolean> ref = new Ref<Boolean>();

	_tree = removeFromTree(_tree,key,0,ref);
	
	return ref.content.booleanValue();

    } // remove(FeatureVector key,..)
    





    // 
    //              Retrieval of subsuming and subsumed:
    //

    
    
    /** Iteration over all indexed values corresponding
     *  to feature vectors subsuming a specified vector.
     */
    public class Subsuming 
	implements java.util.Iterator<IndexedObject> {

	
	public Subsuming() {
	    _backtrackPoints = 
		new FeatureVectorIndexNode[16]; 
	    // 16 = defalt size, may be resized
	}


	/** Releases all pointers to external objects. */
	public final void clear() {

	    //System.out.println("CLEAR");

	    _key = null;

	    for (int i = 0; i < _backtrackPoints.length; ++i)
		{
		    _backtrackPoints[i] = null;
		};

	    _nextLeaf = null;

	} // clear() 



	/** Starts the iteration over all indexed values 
	 *  from the host index, corresponding
	 *  to feature vectors subsuming <code>key</code>.
	 */
	public 
	    final 
	    void reset(FeatureVector key) {

	    //System.out.println("RESET " + key);

	    assert key.length() == _numberOfFeatures;

	    if (key.length() > _backtrackPoints.length)
		_backtrackPoints = 
		    new FeatureVectorIndexNode[key.length()];
	    
	    if (_tree == null)
		{
		    _nextLeaf = null;
		    return;
		};

	    _key = key;

	    FeatureVectorIndexNode<IndexedObject> node =
		(FeatureVectorIndexNode<IndexedObject>)_tree;

	    if (((FeatureVectorIndexNonleafNode<IndexedObject>)node).value() >
			_key.get(0))
		{
		    _nextLeaf = null;
		    return;
		};

	    // To enable complete()/backtrack() calls:
	    _stackSize = 0;
	    _backtrackPoints[0] = node;

	    while (!complete())
		{
		    if (!backtrack())
			{
			    _nextLeaf = null;
			    return;
			};

		};
	    
	    assert _nextLeaf != null;
	    return;

	} // reset(FeatureVectorIndex<IndexedObject> index,..)

	
	public final boolean hasNext() { return _nextLeaf != null; } 
	    
	
	public 
	    final 
	    IndexedObject next() throws java.util.NoSuchElementException {
	    
	   //System.out.println("NEXT");

	    if (_nextLeaf == null)
		throw new java.util.NoSuchElementException();

	    IndexedObject result = _nextLeaf.content;

	    // Backtrack:

	    _stackSize = _numberOfFeatures;

	    while (backtrack())
		{
		    if (complete())
			{
			    assert _nextLeaf != null;
			    return result;
			};

		}; // while (backtrack())
	    
	   
	    _nextLeaf = null;

	    return result;

	} // next()



	/** Cannot be used. */
	public void remove() throws Error {
	    throw 
		new Error("Forbidden method call: logic_warehouse_je.FeatureVectorIndex.Subsuming.remove()");
	}



	private boolean backtrack() {
	    
	    //System.out.println("  BACKTRACK " + _stackSize);

	    --_stackSize;

	    FeatureVectorIndexNonleafNode<IndexedObject> node;
	    while (_stackSize >= 0)
		{
		   //System.out.println("    AAA " + _stackSize);

		    assert _backtrackPoints[_stackSize] != null;
		    node = 
			((FeatureVectorIndexNonleafNode<IndexedObject>)
			 _backtrackPoints[_stackSize]).greater();
		    
		    if (node == null ||
			node.value() > _key.get(_stackSize))
			{
			    _backtrackPoints[_stackSize] = null;
			    --_stackSize;
			}
		    else
			{
			    _backtrackPoints[_stackSize] = node;
			    return true;
			};
		    
		}; // while (_stackSize >= 0)

	    return false;

	} // backtrack()



	private boolean complete() {
	    
	   //System.out.println("  COMPLETE " + _stackSize);

	    assert _stackSize >= 0;
	    assert _stackSize < _numberOfFeatures;
	    assert _backtrackPoints[_stackSize] != null;


	    FeatureVectorIndexNode<IndexedObject> node =
		((FeatureVectorIndexNonleafNode<IndexedObject>)
		 _backtrackPoints[_stackSize]).treeBelow();
	    
	    ++_stackSize;
	    
	    while (_stackSize < _numberOfFeatures)
		{
		    if (((FeatureVectorIndexNonleafNode<IndexedObject>)node).value() >
			_key.get(_stackSize))
			{
			    assert _backtrackPoints[_stackSize] == null;

			    return false;
			};

		    _backtrackPoints[_stackSize] = node;

		    node = 
			((FeatureVectorIndexNonleafNode<IndexedObject>)node).
			treeBelow();
		    
		    ++_stackSize;

		}; // while (_stackSize < _numberOfFeatures)


	    _nextLeaf =
		(FeatureVectorIndexLeafNode<IndexedObject>)
		node;
	    assert _nextLeaf != null;
	    
	    return true;

	} // complete()
	    


	
	//              Data:


	private FeatureVector _key;

	private FeatureVectorIndexNode<IndexedObject>[] _backtrackPoints;

	private int _stackSize;
	
	private FeatureVectorIndexLeafNode<IndexedObject> _nextLeaf;

    } // class Subsuming 






    
    /** Iteration over all indexed values corresponding
     *  to feature vectors subsumed by a specified vector.
     */
    public class Subsumed 
	implements java.util.Iterator<IndexedObject> {

	
	public Subsumed() {
	    _backtrackPoints = 
		new FeatureVectorIndexNode[16]; 
	    // 16 = defalt size, may be resized
	}

	/** Releases all pointers to external objects. */
	public final void clear() {
	    _key = null;

	    for (int i = 0; i < _numberOfFeatures; ++i)
		{
		    _backtrackPoints[i] = null;
		};

	    _nextLeaf = null;

	}


	/** Starts the iteration over all indexed values 
	 *  from the host index, corresponding
	 *  to feature vectors subsumed by <code>key</code>.
	 */
	public 
	    final 
	    void reset(FeatureVector key) {

	    assert key.length() == _numberOfFeatures;

	    if (_tree == null)
		{
		    _nextLeaf = null;
		    return;
		};

	    if (key.length() > _backtrackPoints.length)
		_backtrackPoints = 
		    new FeatureVectorIndexNode[key.length()];
	    
	    _key = key;

	    FeatureVectorIndexNode<IndexedObject> node =
		(FeatureVectorIndexNode<IndexedObject>)_tree;

	    while (((FeatureVectorIndexNonleafNode<IndexedObject>)node).value() <
		   _key.get(0))
		{
		    node =
			((FeatureVectorIndexNonleafNode<IndexedObject>)node).
			greater();
		    if (node == null)
			{
			    _nextLeaf = null;
			    return;
			};
		};

	    // To enable complete()/backtrack() calls:
	    _stackSize = 0;
	    _backtrackPoints[0] = node;
	    
	    while (!complete())
		{
		    if (!backtrack())
			{
			    _nextLeaf = null;
			    return;
			};

		};
	    
	    assert _nextLeaf != null;
	    return;


	} // reset(FeatureVector key)


	public final boolean hasNext() { return _nextLeaf != null; } 
	    
	
	public 
	    final 
	    IndexedObject next() throws java.util.NoSuchElementException {
	    
	   //System.out.println("NEXT");

	    if (_nextLeaf == null)
		throw new java.util.NoSuchElementException();

	    IndexedObject result = _nextLeaf.content;

	    // Backtrack:

	    _stackSize = _numberOfFeatures;

	    while (backtrack())
		{
		    if (complete())
			{
			    assert _nextLeaf != null;
			    return result;
			};

		}; // while (backtrack())
	    
	   
	    _nextLeaf = null;

	    return result;

	} // next()

	    
	    
	

	/** Cannot be used. */
	public void remove() throws Error {
	    throw 
		new Error("Forbidden method call: logic_warehouse_je.FeatureVectorIndex.Subsumed.remove()");
	}


	private boolean backtrack() {
	    
	   //System.out.println("  BACKTRACK " + _stackSize);

	    assert (_stackSize == _backtrackPoints.length) ||
		(_backtrackPoints[_stackSize] == null);

	    --_stackSize;

	    FeatureVectorIndexNonleafNode<IndexedObject> node;
	    while (_stackSize >= 0)
		{
		   //System.out.println("    AAA " + _stackSize);

		    assert _backtrackPoints[_stackSize] != null;
		    node = 
			((FeatureVectorIndexNonleafNode<IndexedObject>)
			 _backtrackPoints[_stackSize]).greater();
		    
		    if (node == null)
			{
			    _backtrackPoints[_stackSize] = null;
			    --_stackSize;
			}
		    else
			{
			    assert node.value() > _key.get(_stackSize);

			    _backtrackPoints[_stackSize] = node;
			    return true;
			};
		    
		}; // while (_stackSize >= 0)

	    return false;

	} // backtrack()



	private boolean complete() {
	    
	   //System.out.println("  COMPLETE " + _stackSize);

	    assert _stackSize >= 0;
	    assert _stackSize < _numberOfFeatures;
	    assert _backtrackPoints[_stackSize] != null;


	    FeatureVectorIndexNode<IndexedObject> node =
		((FeatureVectorIndexNonleafNode<IndexedObject>)
		 _backtrackPoints[_stackSize]).treeBelow();
	    
	    ++_stackSize;
	    
	    while (_stackSize < _numberOfFeatures)
		{

		    while (((FeatureVectorIndexNonleafNode<IndexedObject>)node).value() <
			   _key.get(_stackSize))
			{
			    assert _backtrackPoints[_stackSize] == null;
			    
			    node =
				((FeatureVectorIndexNonleafNode<IndexedObject>)node).
				greater();
			    if (node == null) return false;
			};

		    _backtrackPoints[_stackSize] = node;

		    node = 
			((FeatureVectorIndexNonleafNode<IndexedObject>)node).
			treeBelow();
		    
		    ++_stackSize;

		}; // while (_stackSize < _numberOfFeatures)


	    _nextLeaf =
		(FeatureVectorIndexLeafNode<IndexedObject>)
		node;
	    assert _nextLeaf != null;
	    
	    return true;

	} // complete()
	    



	
	//              Data:


	private FeatureVector _key;


	private FeatureVectorIndexNode<IndexedObject>[] _backtrackPoints;

	private int _stackSize;
	
	private FeatureVectorIndexLeafNode<IndexedObject> _nextLeaf;


    } // class Subsumed







    // 
    //              Maintenance:
    //



    private 
	FeatureVectorIndexNode<IndexedObject> 
	insertIntoTree(FeatureVectorIndexNode<IndexedObject> tree,
		       FeatureVector key,
		       int firstFeature,
		       Ref<Ref<IndexedObject>> leafRef) {
	
	if (tree == null)
	    return createBranch(key,firstFeature,leafRef);
	
	assert firstFeature <= _numberOfFeatures;

	if (firstFeature == _numberOfFeatures)
	    {
		// tree is the leaf corresponding to the vector.

		assert tree instanceof FeatureVectorIndexLeafNode;
		
		leafRef.content = 
		    (Ref<IndexedObject>)
		    (FeatureVectorIndexLeafNode<IndexedObject>)tree;

		return tree;
	    };
	
	int feature = key.get(firstFeature);
	
	if (feature == ((FeatureVectorIndexNonleafNode<IndexedObject>)tree).value())
	    {
		FeatureVectorIndexNode<IndexedObject> newTreeBelow =
		    insertIntoTree(((FeatureVectorIndexNonleafNode<IndexedObject>)tree).
				   treeBelow(),
				   key,
				   firstFeature + 1,
				   leafRef);

		((FeatureVectorIndexNonleafNode<IndexedObject>)tree).
		    setTreeBelow(newTreeBelow);
		return tree;
	    };

	if (feature > ((FeatureVectorIndexNonleafNode<IndexedObject>)tree).value())
	    {
		FeatureVectorIndexNode<IndexedObject> newGreater =
		    insertIntoTree(((FeatureVectorIndexNonleafNode<IndexedObject>)tree).
				   greater(),
				   key,
				   firstFeature,
				   leafRef);
		((FeatureVectorIndexNonleafNode<IndexedObject>)tree).
		    setGreater((FeatureVectorIndexNonleafNode<IndexedObject>)newGreater);
		return tree;
	    };

	// feature < ((FeatureVectorIndexNonleafNode<IndexedObject>)tree).value()

	FeatureVectorIndexNonleafNode<IndexedObject> result =
	    (FeatureVectorIndexNonleafNode<IndexedObject>)
	    createBranch(key,firstFeature,leafRef);
			
	result.setGreater((FeatureVectorIndexNonleafNode<IndexedObject>)tree);

	return result;

    } // insertIntoTree(FeatureVectorIndexNode<IndexedObject> tree,..)



    private 
	final
	FeatureVectorIndexNode<IndexedObject> 
	createBranch(FeatureVector key,
		     int firstFeature,
		     Ref<Ref<IndexedObject>> leafRef) {
	
	assert firstFeature <= _numberOfFeatures;
	
	if (firstFeature == _numberOfFeatures)
	    {
		// Create just the leaf
		FeatureVectorIndexLeafNode<IndexedObject> result =
		    new FeatureVectorIndexLeafNode<IndexedObject>();		
		leafRef.content = (Ref<IndexedObject>)result;
		return result;
	    };

	return
	    new FeatureVectorIndexNonleafNode<IndexedObject>(key.get(firstFeature),
							     createBranch(key,
									  firstFeature + 1,
									  leafRef),
							     null);

    } // createBranch(FeatureVector key,..)

    
    private 
	Ref<IndexedObject>
	findInTree(FeatureVectorIndexNode<IndexedObject> tree,
		   FeatureVector key,
		   int firstFeature) {
	
	if (tree == null) return null;
	
	assert firstFeature <= _numberOfFeatures;

	if (firstFeature == _numberOfFeatures)
	    {
		// tree is the leaf corresponding to the vector.
		
		assert tree instanceof FeatureVectorIndexLeafNode;

		return 
		    (Ref<IndexedObject>)
		    (FeatureVectorIndexLeafNode<IndexedObject>)tree;
	    };
	
	int feature = key.get(firstFeature);
	
	if (feature == 
	    ((FeatureVectorIndexNonleafNode<IndexedObject>)tree).value())
	    return 
		findInTree(((FeatureVectorIndexNonleafNode<IndexedObject>)tree).
			   treeBelow(),
			   key,
			   firstFeature + 1);


	if (feature > 
	    ((FeatureVectorIndexNonleafNode<IndexedObject>)tree).value())
	    return 
		findInTree(((FeatureVectorIndexNonleafNode<IndexedObject>)tree).
			   greater(),
			   key,
			   firstFeature);
	
	// feature < ((FeatureVectorIndexNonleafNode<IndexedObject>)tree).value()

	return null;

    } // findInTree(FeatureVectorIndexNode<IndexedObject> tree,





    private 
	FeatureVectorIndexNode<IndexedObject> 
	removeFromTree(FeatureVectorIndexNode<IndexedObject> tree,
		       FeatureVector key,
		       int firstFeature,
		       Ref<Boolean> successRef) {
	
	if (tree == null)
	    {
		successRef.content = new Boolean(false);
		return null;
	    };

	assert firstFeature <= _numberOfFeatures;
	
	if (firstFeature == _numberOfFeatures)
	    {
		// tree is the leaf corresponding to the vector.
		successRef.content = new Boolean(true);
		return null;
	    };


	int feature = key.get(firstFeature);
	
	if (feature == 
	    ((FeatureVectorIndexNonleafNode<IndexedObject>)tree).value())
	    {
		FeatureVectorIndexNode<IndexedObject> newTreeBelow =
		    removeFromTree(((FeatureVectorIndexNonleafNode<IndexedObject>)tree).
				   treeBelow(),
				   key,
				   firstFeature + 1,
				   successRef);
		if (newTreeBelow == null) 
		    {
			assert successRef.content;
			return 
			    ((FeatureVectorIndexNonleafNode<IndexedObject>)tree).
			    greater();
		    };

		((FeatureVectorIndexNonleafNode<IndexedObject>)tree).
		    setTreeBelow(newTreeBelow);
		return tree;
	    };


	if (feature > 
	    ((FeatureVectorIndexNonleafNode<IndexedObject>)tree).value())
	    {
		FeatureVectorIndexNode<IndexedObject> newGreater =
		    removeFromTree(((FeatureVectorIndexNonleafNode<IndexedObject>)tree).
				   greater(),
				   key,
				   firstFeature,
				   successRef);
		((FeatureVectorIndexNonleafNode<IndexedObject>)tree).
		    setGreater((FeatureVectorIndexNonleafNode<IndexedObject>)newGreater);
		return tree;
	    };

	// feature < ((FeatureVectorIndexNonleafNode<IndexedObject>)tree).value()
	
	successRef.content = new Boolean(false);
	return tree;

    } // removeFromTree(FeatureVectorIndexNode<IndexedObject> tree,
	

    //                 
    //                 Data:
    //                 
    
    private FeatureVectorIndexNode<IndexedObject> _tree;

    /** Determined by the first inserted vector. */
    private int _numberOfFeatures;

} // class FeatureVectorIndex<IndexedObject>