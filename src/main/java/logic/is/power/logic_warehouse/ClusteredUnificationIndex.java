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

/**  
 * Encapsulates all indexing operations and data for
 * <a href="{@docRoot}/resources/glossary.html#clustered_unification">clustered unification</a>.
 * The implementation is based on discrimination trees.  
 * It is a simplified and cleaner reimplementation of 
 * discrimination trees for unification in 
 * <a href="{@docRoot}/resources/references.html#vampire_kernel_6_0">[Vampire kernel v6.0 sources]</a>.
 * <code>IndexedObject</code> is the class of objects that have to be
 *  indexed with the term-cluster pairs; must be default constructible
 *  Indexed terms cannot contain quantifiers or abstractions, although
 *  they can contain connective applications.
 *  Uniqueness of symbol objects is not assumed, i.e., comparisons
 *  are made with {@link logic.is.power.logic_warehouse#Symbol#equals()} 
 *  rather than with "==".
 *  TODO: Lists of leaves should be replaced with a more efficient 
 *        data structure, possibly some kind of trees, possibly balanced.
 *  TODO: For multiple retrievals with incremental unifiers, useful, e.g., for 
 *        resolution, we have to implement retrieval with a dynamically
 *        generated variable renaming that will be applied to retrieved
 *        indexed objects.
 */
public class ClusteredUnificationIndex<IndexedObject> {

    // IMPORTANT ASSUMPTIONS ABOUT THE SHAPE OF THE INDEX TREES:
    // (1) No ClusterClearBitTestNode can directly preceed 
    //     a ClusterSetBitTestNode.
    // (2) At a symbol node level, the alternatives are ordered
    //     so that smaller symbols always preceed greater symbols
    //     wrt the method 
    //     ClusteredUnificationIndex<IndexedObject>.greater(..).
    //     In particular, variable nodes preceed all other kinds
    //     of symbol nodes. This allows more efficient retrieval
    //     and maintenance. In the future, some sort of hashing
    //     should be considered to deal with high branching degrees
    //     at higher levels in the tree.


    
    //
    //                     Retrieval:
    //
    
    /** Retrieval with a state; use an instance of this class to enumerate
     *  all successful unifications betwen a query term and all indexed
     *  terms from a specified cluster; several retrieval states can co-exist
     *  in parallel, but retrieval cannot be interrupted with maintenance operations.
     *  @param unifier where to accumulate the unifiers for retrieved objects
     */
    public /* inner */ class Retrieval extends RetrievalImpl
    { 
	/** Same as <code>Retrieval(null)</code>. */
	public Retrieval() {
	    super(null);
	}

	/** @param unifier can be null, in which case a 
	 *  <code>Substitution1</code> object will be created
	 *  internally.
	 */
	public Retrieval(Substitution1 unifier) {
	    super(unifier);
	}


    } //  class Retrieval 


    /** Counts the number of entries in the index corresponding
     *  to the specified term-cluster pair.
     */
    public final int count(Term term,BitSet cluster) {

	if (term.isVariable())
	    {
		TreeMap<Variable,LeafNode> varToLeaves =
		    _variableIndex.get(cluster);
		if (varToLeaves == null) return 0;
		
		LeafNode leaves = varToLeaves.get((Variable)term.topSymbol());
		assert leaves != null;
		
		return countInLeaves(cluster,leaves);
	    }
	else // nonvariable term/formula
	    {
		int hashCode = 
		    term.topSymbol().hashCode() % NonvariableHashSize;
		Term.LeanIterator iter = new Term.LeanIterator(term);

		return countInTree(iter,cluster,_nonvariableHash[hashCode]);
	    }
    } // count(Term term,BitSet cluster)




    //
    //                   Maintenance:
    //

 

    public ClusteredUnificationIndex() { 
	_nonvariableHash = new NonleafTreeNode[NonvariableHashSize];
	_variableIndex = 
	    new HashMap<BitSet, TreeMap<Variable,LeafNode>>();
	_retrievalLocks = 0;
    }


    /** Integrates the term into the index as a member of 
     *  the specified cluster; the term need not be a new
     *  term for the index, ie, it may have been already inserted
     *  previously with this or a different cluster; 
     *  <code>obj</code> is added to the collection of objects indexed by
     *  the term-cluster pair.
     *  <b>IMPORTANT:</b> As any other maintenance operation, this method
     *             cannot be used during a retrieval cycle, ie,
     *             between calls to {@link logic.is.power.logic_warehouse#ClusteredUnificationIndex#Retrieval#resetQuery(Flatterm,BitSet)}
     *             and {@link logic.is.power.logic_warehouse#ClusteredUnificationIndex#Retrieval#finishRetrieval()}.
     */
    public final void insert(Term term,BitSet cluster,IndexedObject obj) {

        //System.out.println("INSERT " + term + " CL=" + cluster);

	
	assert _retrievalLocks == 0;
	      

	if (term.isVariable())
	    {
		TreeMap<Variable,LeafNode> varToLeaves =
		    _variableIndex.get(cluster);
		
		if (varToLeaves == null)
		    {
			varToLeaves = new TreeMap<Variable,LeafNode>();
			_variableIndex.put(cluster,varToLeaves);
		    };

		LeafNode newLeaf = 
		    new LeafNode(obj,
				 cluster,
				 varToLeaves.get((Variable)term.topSymbol()));

		varToLeaves.put((Variable)term.topSymbol(),newLeaf);		
	    }
	else // nonvariable term/formula
	    {
		int hashCode = 
		    term.topSymbol().hashCode() % NonvariableHashSize;
		Term.LeanIterator iter = new Term.LeanIterator(term);

		BitSet setBitChecksToInsert = 
		    (BitSet)cluster.clone();
		BitSet clearBitChecksToInsert = 
		    (BitSet)cluster.clone();
		clearBitChecksToInsert.flip(0,
					    clearBitChecksToInsert.size());

		BitSet setBitChecksToPush = 
		    new BitSet(cluster.size());
		BitSet clearBitChecksToPush = 
		    new BitSet(cluster.size());
		    
		_nonvariableHash[hashCode] = 
		    insertIntoTree(iter,
				   cluster,
				   setBitChecksToInsert,
				   clearBitChecksToInsert,
				   setBitChecksToPush,
				   clearBitChecksToPush,
				   obj,
				   _nonvariableHash[hashCode]);
	    };
	    
	//System.out.println(toString());

	assert count(term,cluster) != 0;
	
    } // insert(Term term,BitSet cluster,IndexedObject obj)


      
    /** Allows to remove some selected indexed objecs associated
     *  with the specified term-cluster pair;
     *  this method works by enumerating <em>all</em> indexed objecs 
     *  associated with the term-cluster pair and applying the unary 
     *  predicate <code>hasToBeRemoved</code> to them; if the predicate 
     *  returns <code>true</code> on an indexed
     *  object, the object is destroyed and completely removed from 
     *  the index.
     *  
     *  @param term 
     *  @param cluster 
     *  @param hasToBeRemoved an unary predicate on <code>IndexedObject</code> 
     *         that is used to decide which indexed objects associated with 
     *         the term-cluster pair have to be actually removed
     *         from the index; <code>hasToBeRemoved.evaluate()</code> will be applied 
     *         by this method to all indexed object associated
     *         with the term-cluster pair, and, those objects on
     *         which <code>hasToBeRemoved.evaluate()</code> returns <code>true</code>, 
     *         are destroyed and completely removed from the index.
     *  <b>IMPORTANT:</b> As any other maintenance operation, this method
     *             cannot be used during a retrieval cycle, ie, between calls to 
     *             {@link logic.is.power.logic_warehouse#ClusteredUnificationIndex#Retrieval#resetQuery(Flatterm,BitSet)}
     *             and {@link logic.is.power.logic_warehouse#ClusteredUnificationIndex#Retrieval#finishRetrieval()}.
     */
    public final void erase(Term term,
		      BitSet cluster,
		      UnaryPredicateObject<IndexedObject> hasToBeRemoved) {


	//System.out.println("ERASE " + term + " CL=" + cluster);

	assert _retrievalLocks == 0;

	if (term.isVariable())
	    {
		TreeMap<Variable,LeafNode> varToLeaves =
		    _variableIndex.get(cluster);
		if (varToLeaves == null) return;
		
		LeafNode leaves = varToLeaves.get((Variable)term.topSymbol());
		assert leaves != null;
		
		try
		    {
			leaves = eraseLeaves(cluster,hasToBeRemoved,leaves,true);
			
			if (leaves == null)
			    {
				varToLeaves.remove((Variable)term.topSymbol());
				
				if (varToLeaves.isEmpty())
				    _variableIndex.remove(cluster);
			    }
			else
			    varToLeaves.put((Variable)term.topSymbol(),
					    leaves);
		    }
		catch (TermClusterPairNotFoundException ex)
		    {
			// nothing here: varToLeaves remains unchanged
		    }
		
	    }
	else // nonvariable term/formula
	    {
		int hashCode = 
		    term.topSymbol().hashCode() % NonvariableHashSize;
		Term.LeanIterator iter = new Term.LeanIterator(term);

		try
		    {
			_nonvariableHash[hashCode] = 
			    eraseFromTree(iter,
					  cluster,
					  hasToBeRemoved,
					  _nonvariableHash[hashCode]);
		    }
		catch (TermClusterPairNotFoundException ex)
		    {
			// nothing here: _nonvariableHash[hashCode] remains
			// unchanged
		    }
	    };

	//System.out.println(toString());

    } // erase(Term term,..)


      /** Allows to relocate objects indexed by some term-cluster pair
       *  to a different cluster but with the same term;
       *  this method works by enumerating <em>all</em> indexed objecs associated
       *  with the term-cluster pair and applying the unary predicate
       *  <code>hasToBeRelocated</code> to them; if the predicate returns 
       *  <code>true</code> on an indexed object, the object gets associated with 
       *  <code>newCluster</code>.
       *
       *  
       *  @param term 
       *  @param cluster current cluster of the indexed objects that have 
       *         to be relocated
       *  @param newCluster new cluster to associate the indexed objects with
       *  @param hasToBeRelocated a unary predicate that 
       *         is used to decide which indexed objects associated with 
       *         the term-cluster pair have to be relocated to the new cluster;
       *         <code>hasToBeRelocated.evaluate()</code> will be applied 
       *         by this method to all indexed object associated
       *         with the term-cluster pair, and, those objects on
       *         which <code>hasToBeRelocated.evaluate()</code> returns 
       *         <code>true</code>, are detached 
       *         from the current cluster and associated with the new cluster.
       *  <b>IMPORTANT:</b> As any other maintenance operation, this method
       *             cannot be used during a retrieval cycle, ie,
       *             between calls to {@link logic.is.power.logic_warehouse#ClusteredUnificationIndex#Retrieval#resetQuery(Flatterm,BitSet)}
       *             and {@link logic.is.power.logic_warehouse#ClusteredUnificationIndex#Retrieval#finishRetrieval()}.
       */
    public final void relocate(Term term,
			 BitSet cluster,
			 BitSet newCluster,
			 UnaryPredicateObject<IndexedObject> hasToBeRelocated) {

	//System.out.println("RELOCATE " + term + " CL=" + cluster +
	//" ---> " + newCluster);

	assert _retrievalLocks == 0;

	if (term.isVariable())
	    {
		TreeMap<Variable,LeafNode> varToLeaves =
		    _variableIndex.get(cluster);
		if (varToLeaves == null) return;
		
		TreeMap<Variable,LeafNode> newVarToLeaves =
		    _variableIndex.get(newCluster);
		
		LeafNode newClusterLeaves = null;

		if (newVarToLeaves == null)
		    {
			newVarToLeaves = new TreeMap<Variable,LeafNode>();
			_variableIndex.put(newCluster,newVarToLeaves);
		    }
		else
		    newClusterLeaves = 
			newVarToLeaves.get((Variable)term.topSymbol());


		LeafNode leaves = varToLeaves.get((Variable)term.topSymbol());
		assert leaves != null;
		
		while (leaves != null && 
		       hasToBeRelocated.evaluate((IndexedObject)leaves.indexedObject()))
		    {
			LeafNode tmp = leaves;
			leaves = leaves.otherLeaves();
			tmp.setCluster(newCluster);
			tmp.setOtherLeaves(newClusterLeaves);
			newClusterLeaves = tmp;
		    };



		if (leaves == null)
		    {
			varToLeaves.remove((Variable)term.topSymbol());
			
			if (varToLeaves.isEmpty())
			    _variableIndex.remove(cluster);
		    }
		else // leaves != null
		    {
			LeafNode prevLeaf = leaves; 
			while (prevLeaf.otherLeaves() != null)
			    if (hasToBeRelocated.
				evaluate((IndexedObject)
					 prevLeaf.otherLeaves().indexedObject()))
				{
				    LeafNode tmp = prevLeaf.otherLeaves();
				    prevLeaf.setOtherLeaves(tmp.otherLeaves());
				    tmp.setCluster(newCluster);
				    tmp.setOtherLeaves(newClusterLeaves);
				    newClusterLeaves = tmp;
				}
			    else
				prevLeaf = prevLeaf.otherLeaves();

			varToLeaves.put((Variable)term.topSymbol(),
					leaves);
		    };

		newVarToLeaves.put((Variable)term.topSymbol(),newClusterLeaves);

	    }
	else // nonvariable term/formula
	    {
		int hashCode = 
		    term.topSymbol().hashCode() % NonvariableHashSize;
		Term.LeanIterator iter = new Term.LeanIterator(term);
		    
		try
		    {
			_nonvariableHash[hashCode] = 
			    relocateInTree(iter,
					   cluster,
					   newCluster,
					   hasToBeRelocated,
					   _nonvariableHash[hashCode]);
		    }
		catch (TermClusterPairNotFoundException ex)
		    {
			// nothing here: _nonvariableHash[hashCode] remains
			// unchanged
		    }
	    };


	assert count(term,newCluster) != 0;

        //System.out.println(toString());


    } // relocate(Term term,..)
	       




      //
      //                     Printing:
      //


    public String toString() {

	String result = 
	    "===================== CLUSTERED UNIFICATION INDEX: ==================\n";

	
	for (int i = 0; i < NonvariableHashSize; ++i)
	    if (_nonvariableHash[i] != null)
		result += "\n\n======= TOP LEVEL HASH " + i + "\n" +
		    treeToString(_nonvariableHash[i]);
	
	result += 
	    "================= END OF CLUSTERED UNIFICATION INDEX. ================\n";

	return result;

    } // String toString()





    //                     Private types:

    /** Common base for all kinds of nodes in the discrimination trees. */
    private static abstract class TreeNode {
	
	public static class Kind {
	    
	    /** No instances! */
	    private Kind() {}
	    
	    public static final int Variable = 0;
	    public static final int Function = 1;
	    public static final int Constant = 2;
	    public static final int Predicate = 3;
	    public static final int Connective = 4;
	    public static final int ClusterSetBitTest = 5;
	    public static final int ClusterClearBitTest = 6;
	    public static final int Leaf = 7;
	} // class Kind

	public TreeNode(int kind) {
	    _kind = kind;
	}

	public final int kind() { return _kind; }

	public final boolean isLeaf() { return _kind == Kind.Leaf; }

	public final boolean isClusterTest() {
	    return _kind == Kind.ClusterSetBitTest ||
		_kind == Kind.ClusterClearBitTest;
	}
	    
	public final boolean isNonvariableSymbolNode() {
	    return _kind == Kind.Function ||
		_kind == Kind.Constant ||
		_kind == Kind.Predicate ||
		_kind == Kind.Connective;
	}

	public final boolean isVariableNode() {
	    return _kind == Kind.Variable;
	}

	public abstract String toString();
	
	private int _kind;

    } // class TreeNode 


    private static abstract class NonleafTreeNode extends TreeNode {
	
	public NonleafTreeNode(int kind,TreeNode treeBelow) {
	    super(kind);
	    _treeBelow = treeBelow;
	}

	public abstract String toString();

	
	public final TreeNode treeBelow() { return _treeBelow; }
	
	public final void setTreeBelow(TreeNode tree) { _treeBelow = tree; }
	
	private TreeNode _treeBelow;

    } // class NonleafTreeNode


    private 
	static 
	abstract class NonleafTreeNodeWithAlternative extends NonleafTreeNode {
	
	public NonleafTreeNodeWithAlternative(int kind,
					      TreeNode treeBelow,
					      NonleafTreeNode alternative) {
	    super(kind,treeBelow);
	    _alternative = alternative;
	    assert 
		_alternative == null ||
		!isNonvariableSymbolNode() ||
		!_alternative.isVariableNode();
	}

	public final NonleafTreeNode alternative() { return _alternative; }
	
	public final void setAlternative(NonleafTreeNode alt) { 
	    _alternative = alt; 
	    assert 
		_alternative == null ||
		!isNonvariableSymbolNode() ||
		!_alternative.isVariableNode();
	}

	public abstract String toString();

	private NonleafTreeNode _alternative;

    } // class NonleafNodeWithAlternative


    /** Some common functionality for {@link logic.is.power.logic_warehouse#ClusteredUnificationIndex.VariableNode},
     *  {@link logic.is.power.logic_warehouse#ClusteredUnificationIndex.FunctionNode},{@link logic.is.power.logic_warehouse#ClusteredUnificationIndex.ConstantNode},{@link logic.is.power.logic_warehouse#ClusteredUnificationIndex.PredicateNode}
     *  and {@link logic.is.power.logic_warehouse#ClusteredUnificationIndex.ConnectiveNode}.
     */
    private static class SymbolNode extends NonleafTreeNodeWithAlternative {

	public SymbolNode(int kind,
			  Symbol symbol,
			  TreeNode treeBelow,
			  NonleafTreeNode alternative) {
	    super(kind,treeBelow,alternative);
	    _symbol = symbol;
	}
	
	public String toString() { 
	    switch (kind())
		{
		case Kind.Variable: return ((VariableNode)this).toString();
		case Kind.Function: return ((FunctionNode)this).toString();
		case Kind.Constant: return ((ConstantNode)this).toString();
		case Kind.Predicate: return ((PredicateNode)this).toString();
		case Kind.Connective: return ((ConnectiveNode)this).toString();
		};
	    
	    assert false;
	    return null;

	} // String toString()
	    

	public final Symbol symbol() { return _symbol; }
	
	protected Symbol _symbol;

    } // interface SymbolNode


    private static class VariableNode extends SymbolNode {

	public VariableNode(Variable var,TreeNode treeBelow) {
	    super(Kind.Variable,var,treeBelow,null);
	}

	public final Variable variable() { return (Variable)_symbol; } 

	public String toString() { return "VAR " + variable(); }

    } // class VariableNode


    private static class FunctionNode extends SymbolNode {

	public FunctionNode(Function fun,TreeNode treeBelow) {
	    super(Kind.Function,fun,treeBelow,null);
	}

	public final Function function() { return (Function)_symbol; }
 
	public String toString() { return "FUNC " + function(); }

    } // class FunctionNode

    private static class ConstantNode extends SymbolNode {

	public ConstantNode(IndividualConstant c,TreeNode treeBelow) {
	    super(Kind.Constant,c,treeBelow,null);
	}


	public final IndividualConstant constant() { return (IndividualConstant)_symbol; } 

	public String toString() { return "CONST " + constant(); }

    } // class ConstantNode


    private static class PredicateNode extends SymbolNode {

	public PredicateNode(Predicate pred,TreeNode treeBelow) {
	    super(Kind.Predicate,pred,treeBelow,null);
	}

	public final Predicate predicate() { return (Predicate)_symbol; }

	public String toString() { return "PRED " + predicate(); }

    } // class PredicateNode


    private static class ConnectiveNode extends SymbolNode {

	public ConnectiveNode(Connective con,TreeNode treeBelow) {
	    super(Kind.Connective,con,treeBelow,null);
	}


	public final Connective connective() { return (Connective)_symbol; } 

	public String toString() { return "CONN " + connective(); }

    } // class ConnectiveNode


    /** Common base for cluster bit check nodes. */
    private static abstract class ClusterTestNode extends NonleafTreeNode {
	
	/** <b>pre:</b> <code>!mask.isEmpty()</code>. */
	public ClusterTestNode(int kind,BitSet mask,TreeNode treeBelow) {
	    super(kind,treeBelow);
	    assert kind == TreeNode.Kind.ClusterSetBitTest ||
		kind == TreeNode.Kind.ClusterClearBitTest;
	    assert !mask.isEmpty();
	    _mask = (BitSet)mask.clone();
	}
	
	public final BitSet mask() { return _mask; }

	public final void setMask(BitSet newMask) { _mask = newMask; }

	public abstract boolean test(BitSet cluster);

	public abstract String toString();

	private BitSet _mask;

    } // class ClusterTestNode


    private static class ClusterSetBitTestNode extends ClusterTestNode {
	
	/** <b>pre:</b> <code>!mask.isEmpty()</code>. */
	public ClusterSetBitTestNode(BitSet mask,TreeNode treeBelow) {	    
	    super(Kind.ClusterSetBitTest,mask,treeBelow);
	}
	
	/** Checks that all the bits specified in this node 
	 *  are set in <code>cluster</code>.
	 */
	public final boolean test(BitSet cluster) {
	    BitSet invertedCluster = (BitSet)cluster.clone();
	    invertedCluster.flip(0,invertedCluster.size());
	    return !mask().intersects(invertedCluster);
	}

	public String toString() { return "SET_BITS " + mask(); }


    } // class ClusterSetBitTestNode
    

    private static class ClusterClearBitTestNode extends ClusterTestNode {
	
	/** <b>pre:</b> <code>!mask.isEmpty()</code>. */
	public ClusterClearBitTestNode(BitSet mask,TreeNode treeBelow) {
	    super(Kind.ClusterClearBitTest,mask,treeBelow);
	}

	/** Checks that all the bits specified in this node 
	 *  are clear in <code>cluster</code>.
	 */
	public final boolean test(BitSet cluster) {
	    return !mask().intersects(cluster);
	}

	public String toString() { return "CLEAR_BITS " + mask(); }

    } // class ClusterClearBitTestNode
    



    private static class LeafNode<IndexedObject> extends TreeNode {
	
	public LeafNode(IndexedObject obj,
			BitSet cluster,
			LeafNode otherLeaves) {
	    super(Kind.Leaf);
	    _indexedObject = obj;
	    _cluster = cluster;
	    _otherLeaves = otherLeaves;
	}
	
	public final IndexedObject indexedObject() { return _indexedObject; }
	
	public final BitSet cluster() { return _cluster; }

	public final LeafNode otherLeaves() { return _otherLeaves; }
	
	public final void setCluster(BitSet cluster) { _cluster = cluster; }

	public final void setOtherLeaves(LeafNode otherLeaves) {
	    _otherLeaves = otherLeaves;
	}

	public String toString() { 
	    return 
		"LEAF CLUSTER=" + cluster() + " " +
		"OBJ= " + indexedObject(); 
	}

	private IndexedObject _indexedObject;

	private BitSet _cluster;

	private LeafNode _otherLeaves;

    } // class LeafNode
    



    /** Implementation of {@link logic.is.power.logic_warehouse#ClusteredUnificationIndex#Retrieval}, just to separate 
     *  the implementation from the interface.
     */
    private /* inner */ class RetrievalImpl {

	/** @param unifier can be null, in which case a 
	 *  <code>Substitution1</code> object will be created
	 *  internally.
	 */
	public RetrievalImpl(Substitution1 unifier) {
	    if (unifier == null)
		{
		    _unifier = new Substitution1();
		}
	    else
		_unifier = unifier;
		
	    _currentQuery = null;
	    _queryCursor = 
		new FlattermInstance.BacktrackableIterator();
	    _stackSize = 0;
	    _backtrackPointStack = new Vector<BacktrackPoint>(128,64);
	    // 128=initial capacity
	    // 64=capacity increment
	    _backtrackPointStack.setSize(128);
	}


	public final Flatterm currentQuery() { return _currentQuery; }

	public final Substitution1 unifier() { return _unifier; }

	/** Initiates a new cycle of retrieval with the specified query term
	 *  and the cluster from which indexed objects must be taken.
	 *  <b>IMPORTANT:</b> Must be always matched by a later call to {@link #finish()}.
	 */
	public final void resetQuery(Flatterm queryTerm,BitSet cluster) {
	    

	    //System.out.println("QUERY " + queryTerm + " : " + cluster);
	    //System.out.println(ClusteredUnificationIndex.this.toString());

	    ++_retrievalLocks;


	    assert _currentQuery == null;
	    assert _unifier.empty();

      

	    _currentQuery = queryTerm;
	    _currentCluster = cluster;
	    
	    _queryIsVariable = _currentQuery.isVariable();
	    if (_queryIsVariable &&_currentQuery.variable().isInstantiated1()) 
		_currentQuery = _currentQuery.variable().ultimateInstance1();
		
	    _queryIsVariable = _currentQuery.isVariable();

	    _queryCursor.reset(_currentQuery);


	    if (_queryIsVariable)
		{
		    _currentHashCode = 0;
		    _indexCursor = _nonvariableHash[_currentHashCode];
		    while (_indexCursor == null && _currentHashCode + 1 < NonvariableHashSize)
			{
			    ++_currentHashCode;
			    _indexCursor = _nonvariableHash[_currentHashCode];
			};

		}
	    else
		{
		    int hashCode = 
			_currentQuery.symbol().hashCode() %
			NonvariableHashSize;

                                       assert hashCode >= 0; // because Symbol.hashCode() >= 0
                    
		    _indexCursor = _nonvariableHash[hashCode];
		};
	    
	    _freshQuery = true;
		    
	    _stackSize = 0; 

	    _retrievingFromVarIndex = false;

	} // resetQuery(Flatterm queryTerm,BitSet cluster)
		      
      
	/** Tries to retrieve another indexed object from the current 
	 *  query cluster, that is associated with a term unifiable with
	 *  the current query term; adjusts the global substitution
	 *  accordingly.
	 *  @param retrievedIndexedObject where the retrieved indexed
	 *         object is assigned if the retrieval is successfull
	 *  @return false if no more objects can be retrieved for 
	 *          this query
	 */
	public final boolean retrieveNext(Ref<IndexedObject> retrievedIndexedObject) 
	{
	    assert _currentQuery != null;
	    assert retrievedIndexedObject != null;

	    //System.out.println(" retrieve next?");

	    if (_retrievingFromVarIndex)
		return retrieveNextFromVarIndex(retrievedIndexedObject);

	    if (_freshQuery)
		{
		    _freshQuery = false;

		    if (!completeSearch()) 
			{
			    if (_queryIsVariable)
				{
				    // Try another hash code:

				    ++_currentHashCode;

				    while (_currentHashCode < NonvariableHashSize)
					{

					    _indexCursor = _nonvariableHash[_currentHashCode];
					    
					    if (_indexCursor != null)
						{
						    // Try to find something in this tree:
						    _queryCursor.reset(_currentQuery);
						    _stackSize = 0; 
						    if (completeSearch())
							{
							    retrievedIndexedObject.content = 
								(IndexedObject)
								((LeafNode)_indexCursor).
								indexedObject();
							    
							    return true;
							};
						};

					    ++_currentHashCode;

					}; // while (_currentHashCode < NonvariableHashSize)
				}; // if (_queryIsVariable)

			    _retrievingFromVarIndex = true;
			    _freshQuery = true;

			    return retrieveNextFromVarIndex(retrievedIndexedObject);

			}; // if (!completeSearch())
		}
	    else // !_freshQuery
		{
		    // Try another leaf:

		    assert _indexCursor.isLeaf();
		    
		    do
			{
			    _indexCursor = ((LeafNode)_indexCursor).otherLeaves();
			}
		    while (_indexCursor != null &&
			   !((LeafNode)_indexCursor).cluster().equals(_currentCluster));
		    
		    if (_indexCursor == null)
			{
			    // No more good leaves here, try to backtrack:
			    if (!backtrack() || !completeSearch())
				{

				    if (_queryIsVariable)
					{
					    // Try another hash code:
					    
					    ++_currentHashCode;
					    
					    while (_currentHashCode < NonvariableHashSize)
						{
						    _indexCursor = _nonvariableHash[_currentHashCode];
						    
						    if (_indexCursor != null)
							{
							    // Try to find something in this tree:
							    _queryCursor.reset(_currentQuery);
							    _stackSize = 0; 
							    if (completeSearch())
								{
								    retrievedIndexedObject.content = 
									(IndexedObject)
									((LeafNode)_indexCursor).
									indexedObject();
								    
								    return true;
								};
							};
						    
						    ++_currentHashCode;

						}; // while (_currentHashCode < NonvariableHashSize)
					}; // if (_queryIsVariable)

				    _retrievingFromVarIndex = true;
				    _freshQuery = true;
				    
				    return retrieveNextFromVarIndex(retrievedIndexedObject);

				}; // if (!backtrack() || !completeSearch())
			}; // if (_indexCursor == null)

		    assert _indexCursor != null;

		}; // if (_freshQuery)

	    assert _indexCursor != null;

	    retrievedIndexedObject.content = 
		(IndexedObject)((LeafNode)_indexCursor).indexedObject();

	    return true;

	} // retrieveNext(Ref<IndexedObject> retrievedIndexedObject)





	/** Must close every retrieval cycle regardless of whether
	 *  the last call to {@link logic.is.power.logic_warehouse#ClusteredUnificationIndex#retrieveNext()}
	 *  succeeded or not; after a call to this method, the global
	 *  substitution is restored to the state it was in before 
	 *  the corresponding call to {@link logic.is.power.logic_warehouse#ClusteredUnificationIndex#resetQuery(Flatterm,BitSet)}
	 *  (unless, of course, something else is changing the substitution).
	 */
	public final void finish() { 

	    assert _currentQuery != null;
	    
	    //System.out.println("    FINISH " + _currentQuery);

	    _unifier.uninstantiateAll();
	    _currentQuery = null;
	    _currentCluster = null;
	    _queryCursor.clear();
	    _indexCursor = null;
	    _currentVar = null; 
	    _currentVarLeaf = null;

	    while (_stackSize != 0)
		{
		    --_stackSize;
		    _backtrackPointStack.get(_stackSize).clear();
		};

	    --_retrievalLocks;
	} // finish()



	//
	//          Private classes:
	//
	
	/** Represents intervals of tree paths corresponding to well-formed
	 *  <b>individual-valued</b> terms that can instantiate specified variables.
	 */
	private /* inner */ class TreePathTerm {

	    public TreePathTerm(Variable var) {
		_variable = var;
		_nodeStack = new LinkedList<SymbolNode>();
		_holeStack = new LinkedList<Integer>();
		_flatterm = new FlattermAssembler();
	    }
	    	    
	    /** The value of <code>var</code> in the constructor call 
	     *  that created this object.
	     */
	    public final Variable variable() { return _variable; }

	    /** Flatterm corresponding to the tree path interval.
	     *  <b>pre:</b> the tree path interval represents a complete
	     *  well-formed term.
	     */
	    public final Flatterm flatterm() { return _flatterm.assembledTerm(); } 

	    /** Checks if the collected tree path interval represents a simple
	     *  term syntactically identical to {@link #variable()}.
	     */
	    public final boolean isIdenticalToVar() {
		return _flatterm.assembledTerm().isVariable() &&
		    (_flatterm.assembledTerm().variable() == _variable ||
		     (_flatterm.assembledTerm().variable().isInstantiated1() &&
		      _flatterm.assembledTerm().variable().ultimateInstance1().isVariable() &&
		      _flatterm.assembledTerm().variable().ultimateInstance1().variable() == _variable));
	    }


	    public final boolean findFirst() {

		_openHoles = 1;

		while (true)
		    {
			while (smallStepForward()) {};
			
			// Either all holes are filled, or a dead end
			// is reached and a backtrack is required.
			
			if (_indexCursor != null &&
			    _openHoles == 0)
			    {
				assembleFlatterm();
				return true;
			    };

			if (!treePathBacktrack()) return false;
		    }
		
	    } // findFirst()
	    

	    public final boolean findNext() {

		if (!treePathBacktrack()) return false;

		while (true)
		    {
			while (smallStepForward()) {};
			
			// Either all holes are filled, or a dead end
			// is reached and a backtrack is required.
			
			if (_indexCursor != null &&
			    _openHoles == 0)
			    {
				assembleFlatterm();
				return true;
			    };

			if (!treePathBacktrack()) return false;
		    }

	    } // findNext()



	    public final String toString() {
		
		return
		    "Tree-path term:\n" +
		    "  Var = " + _variable + "\n" +
		    "  Holes = " + _openHoles + "\n" +
		    "  Node stack = " + _nodeStack + "\n" +
		    "  Hole stack = " + _holeStack;

	    } // toString()


	    private boolean atTopLevel() { return _nodeStack.isEmpty(); }

	    
	    private boolean smallStepForward() {
	    
		if (_openHoles == 0) return false;

		while (_indexCursor != null)
		    {
			if (!passClusterTests()) return false;

			switch (_indexCursor.kind())
			    {
			    case TreeNode.Kind.Variable:
				{
				    Variable nodeVar = ((VariableNode)_indexCursor).variable();

				    if (nodeVar == _variable ||
					(nodeVar.isInstantiated1() && 
					 nodeVar.ultimateInstance1().isVariable() &&
					 nodeVar.ultimateInstance1().variable() == _variable))
					if (_nodeStack.isEmpty())
					    {
						// The whole tree-path term is just this variable:
						_nodeStack.addLast((VariableNode)_indexCursor);
						_holeStack.addLast(new Integer(_openHoles));
						--_openHoles;
						_indexCursor = 
						    ((VariableNode)_indexCursor).treeBelow();
						return true;
					    }
					else
					    {
						// Look among the alternatives:
						_indexCursor = 
						    ((VariableNode)_indexCursor).alternative();
						return true;
					    };
				    

				    if (nodeVar.isInstantiated1() &&
					FlattermInstance.contains(nodeVar.ultimateInstance1(),
								  _variable))
					{
					    _indexCursor = 
						((VariableNode)_indexCursor).alternative();
					    return true;
					};

				    _nodeStack.addLast((VariableNode)_indexCursor);
				    _holeStack.addLast(new Integer(_openHoles));
				    --_openHoles;
				    _indexCursor = 
					((VariableNode)_indexCursor).treeBelow();
				    return true;
				}
			
			    case TreeNode.Kind.Function:
				_nodeStack.addLast((FunctionNode)_indexCursor);
				_holeStack.addLast(new Integer(_openHoles));
				_openHoles += 
				    ((FunctionNode)_indexCursor).function().arity() - 
				    1;
				_indexCursor = 
				    ((FunctionNode)_indexCursor).treeBelow();
				return true;

			    case TreeNode.Kind.Constant:
				_nodeStack.addLast((ConstantNode)_indexCursor);
				_holeStack.addLast(new Integer(_openHoles));
				--_openHoles;
				_indexCursor = 
				    ((ConstantNode)_indexCursor).treeBelow();
				return true;

			    case TreeNode.Kind.Predicate: // as below
			    case TreeNode.Kind.Connective:
				if (atTopLevel())
				    {
					// Assignment of boolean-valued terms to variables
					// is not allowed.
					_indexCursor = 
					    ((ConstantNode)_indexCursor).alternative();
				    }
				else
				    {
					// Nested boolean-valued terms are allowed:
					_nodeStack.addLast((SymbolNode)_indexCursor);
					_holeStack.addLast(new Integer(_openHoles));
					_openHoles += 
					    ((SymbolNode)_indexCursor).symbol().arity() - 1;
					_indexCursor = 
					    ((SymbolNode)_indexCursor).treeBelow();
					return true;
				    }

			    } // switch (_indexCursor.kind())

		    }; // while (_indexCursor != null)

		return false;

	    } // smallStepForward()




	    private boolean treePathBacktrack() {

		while (!_nodeStack.isEmpty())
		    {
			assert !_holeStack.isEmpty();
			_openHoles = _holeStack.removeLast().intValue();
			_indexCursor = _nodeStack.removeLast().alternative();
			if (_indexCursor != null) return true;
		    };

		return false;

	    } // treePathBacktrack()



	    private void assembleFlatterm() {

		_flatterm.reset();
		
		for (SymbolNode node : _nodeStack)
		    _flatterm.pushSymbol(node.symbol());
		
		_flatterm.wrapUp();

	    } // assembleFlatterm()




	    //
	    //       Private data:
	    //  
	    

	    private Variable _variable;

	    private int _openHoles;

	    private LinkedList<SymbolNode> _nodeStack;
	    
	    private LinkedList<Integer> _holeStack;

	    private FlattermAssembler _flatterm;

	} // class TreePathTerm


	/** TODO: can be optimised by making it static */
	private /* inner */ class BacktrackPoint {

	    public BacktrackPoint(TreePathTerm treePath,
				  int queryCursSavepoint)
	    {
		treePathTerm = treePath;
		queryCursorSavepoint = queryCursSavepoint;
		assert treePath != null;
	    }

	    /** <b>pre:</b> <code>indexCurs</code> can be null. */
	    public BacktrackPoint(int queryCursSavepoint,
				  int unifSavepoint,
				  TreeNode indexCurs)
	    {
		treePathTerm = null;
		queryCursorSavepoint = queryCursSavepoint;
		unifierSavepoint = unifSavepoint;
		indexCursor = indexCurs;
	    }

	    public final void set(TreePathTerm treePath,
			    int queryCursSavepoint)
	    {
		treePathTerm = treePath;
		queryCursorSavepoint = queryCursSavepoint;
	    }

	    /** <b>pre:</b> <code>indexCurs</code> can be null. */
	    public final void set(int queryCursSavepoint,
			    int unifSavepoint,
			    TreeNode indexCurs)
	    {
		treePathTerm = null;
		queryCursorSavepoint = queryCursSavepoint;
		unifierSavepoint = unifSavepoint;
		indexCursor = indexCurs;
	    }
	    
	    /** Releases all external pointers. */
	    public final void clear() {
		treePathTerm = null;
		indexCursor = null;
	    }
	    
	    public TreePathTerm treePathTerm;

	    public int queryCursorSavepoint;

	    public int unifierSavepoint;
	    
	    public TreeNode indexCursor;
	    
	} // class BacktrackPoint





	//
	//          Private methods:
	//


	/** Find the first tree path term whose corresponding flatterm
	 *  can be assigned to <code>var</code> in the unifier.
	 *  @return null if such tree path term cannot be found
	 */
	private TreePathTerm findFirstPathTerm(Variable var) {

	    TreePathTerm result = new TreePathTerm(var);

	    if (result.findFirst()) return result;
	    
	    return null;

	} // findFirstPathTerm(Variable var)
	


	
      
	/** Tries to retrieve another indexed object from the current 
	 *  query cluster, that is associated with a variable unifiable with
	 *  the current query term; adjusts the global substitution
	 *  accordingly.
	 *  @param retrievedIndexedObject where the retrieved indexed
	 *         object is assigned if the retrieval is successfull
	 *  @return false if no more objects can be retrieved for 
	 *          this query
	 */
	public 
	    final 
	    boolean 
	    retrieveNextFromVarIndex(Ref<IndexedObject> retrievedIndexedObject) {

	    if (_freshQuery)
		{
		    _freshQuery = false;
		    
		    TreeMap<Variable,LeafNode> varToLeavesMap = 
			_variableIndex.get(_currentCluster);
		    
		    if (varToLeavesMap == null) return false;

		    _currentVar = varToLeavesMap.entrySet().iterator();
		    
		    _unifierSavepointBeforeRetrievalFromVarIndex = 
			_unifier.savepoint();

		}
	    else // !_freshQuery
		{
		    // Try another leave for the same index variable:
		    
		    if (_currentVarLeaf != null)
			{
			    retrievedIndexedObject.content =
				(IndexedObject)_currentVarLeaf.indexedObject();

			    _currentVarLeaf = _currentVarLeaf.otherLeaves();
		    
			    return true;
			};

		    _unifier.backtrackTo(_unifierSavepointBeforeRetrievalFromVarIndex);




		}; // if (_freshQuery)


		    
	    while (_currentVar.hasNext())
		{
		    Map.Entry<Variable,LeafNode> varAndLeaves = _currentVar.next();
		    
		    if (Unification.unify(varAndLeaves.getKey(),_currentQuery,_unifier))
			{
			    retrievedIndexedObject.content =
				(IndexedObject)varAndLeaves.getValue().indexedObject();
			    
			    _currentVarLeaf = varAndLeaves.getValue().otherLeaves();
			    
			    return true;
			};
		    
		};
	    
	    return false;

	} // retrieveNextFromVarIndex(Ref<IndexedObject> retrievedIndexedObject)
	



	private boolean completeSearch() {
	    // NOTE THAT WE CANNOT AFFORD RECURSION
	    // ALONG BACKTRACKING HERE: THIS WOULD CAUSE
	    // STACK OVERFLOWS.

	    while (true)
		{
		    while (stepForward()) 
			{
			    // empty
			};

		    // Either found a good leaf or backtrack
		    // is required.
		    
		    if (_indexCursor != null &&
			_indexCursor.isLeaf())
			return true;
		    

	    
		    ///System.out.println("REACHED " + _indexCursor);
			
		    if (!backtrack()) return false;

		}

	} // completeSearch()




	private boolean stepForward() {

	    assert GlobalEventCounter.inc();

	    if (_indexCursor == null) 
		{
		    
	    
		    //System.out.println("INDEX CURSOR NULL");
		    
		    return false;
		};

	    if (!passClusterTests())
		{

	    
		    //System.out.println("CLUSTER TEST FAILURE");

		    return false;
		};

	    if (!_queryCursor.hasNext())
		{
		    // end of query
			
	    
		    //System.out.println("QUERY END");

		    assert _indexCursor.isLeaf();

	    
		    //System.out.println("   FIRST CANDIDATE LEAF " + _indexCursor);
		    

		    // Get to the right leaf:
		    
		    while (_indexCursor != null &&
			   !((LeafNode)_indexCursor).cluster().
			   equals(_currentCluster))
			{
			    _indexCursor = 
				((LeafNode)_indexCursor).otherLeaves();

			    //System.out.println("   GOOD LEAF " + _indexCursor);

			};


		    return false; 
		    // indicates that this was the last step.
		};

	    
	    int queryCursorSavepoint = _queryCursor.savepoint();
	    
	    Flatterm querySubterm = _queryCursor.next();


	    
	    //System.out.println("Q= " + querySubterm + "   I= " + _indexCursor + "   U= " + _unifier);

	    switch (querySubterm.kind())
		{
		   
		case Term.Kind.Variable:
		    {
			Variable var = querySubterm.variable();

			assert !var.isInstantiated1(); 
			// because FlattermInstance.BacktrackableIterator
			// works in this manner.


			// A tree path interval corresponding to a term
			// to be assigned to the variable in the unifier.
			TreePathTerm treePathTerm = findFirstPathTerm(var);
			if (treePathTerm == null)
			    {
				_queryCursor.backtrackTo(queryCursorSavepoint);

				//System.out.println("tree-path not found");

				return false;
			    };

			// Instantiate the variable, if necessary:
			
			if (!treePathTerm.isIdenticalToVar())
			    _unifier.instantiate(var,treePathTerm.flatterm());


			// Save data for backtracking:
			//    treePathTerm
			//    _queryCursor.savepoint() (NOT queryCursorSavepoint!)
			
			_backtrackPointStack.setSize(_stackSize + 1);
			if (_backtrackPointStack.get(_stackSize) == null)
			    {
				_backtrackPointStack.set(_stackSize,
							 new BacktrackPoint(treePathTerm,
									    _queryCursor.savepoint()));
				
			    }
			else
			    _backtrackPointStack.get(_stackSize).set(treePathTerm,
								     _queryCursor.savepoint());
			
			++_stackSize;

			return true;
		    } // case Variable:



		case Term.Kind.CompoundTerm: // as below 
		case Term.Kind.IndividualConstant: // as below (can be slightly optimised)
		case Term.Kind.AtomicFormula: // as below
		case Term.Kind.ConnectiveApplication:
		    if (_indexCursor.kind() == TreeNode.Kind.Variable)
			{
			    int unifierSavepoint = _unifier.savepoint();
			    
			    do
				{
				    if (Unification.unify(((VariableNode)_indexCursor).
							  variable(),
							  querySubterm,
							  _unifier))
					{
					    // Save data for backtracking:
					    //    queryCursorSavepoint
					    //    unifierSavepoint
					    //    ((VariableNode)_indexCursor).alternative()
					    
					    _backtrackPointStack.setSize(_stackSize + 1);
					    if (_backtrackPointStack.get(_stackSize) == null)
						{
						    _backtrackPointStack.
							set(_stackSize,
							    new BacktrackPoint(queryCursorSavepoint,
									       unifierSavepoint,
									       ((VariableNode)_indexCursor).
									       alternative()));
						}
					    else
						_backtrackPointStack.
						    get(_stackSize).
						    set(queryCursorSavepoint,
							unifierSavepoint,
							((VariableNode)_indexCursor).alternative());
					    
					    ++_stackSize;

					    _queryCursor.skipArguments();
					    _indexCursor = 
						((VariableNode)_indexCursor).treeBelow();


					    //System.out.println("UNIFIABLE: " + _unifier);

					    return true;
					}; // if (Unification.unify(((VariableNode)_indexCursor).

				    
				    //System.out.println("NONUNIFIABLE: " + _unifier);

				    _indexCursor = 
					((VariableNode)_indexCursor).alternative(); 

				    if (_indexCursor == null || !passClusterTests())
					{
					    _queryCursor.backtrackTo(queryCursorSavepoint);
					    return false;
					};
				}
			    while (_indexCursor.kind() == TreeNode.Kind.Variable);

			}; // if (_indexCursor.kind() == TreeNode.Kind.Variable)

		    

		    assert 
			_indexCursor.kind() == TreeNode.Kind.Constant ||
			_indexCursor.kind() == TreeNode.Kind.Function ||
			_indexCursor.kind() == TreeNode.Kind.Predicate ||
			_indexCursor.kind() == TreeNode.Kind.Connective;
			    

		    // Skip alternatives with smaller symbols:


		    while (greater(querySubterm.symbol(),
				   ((SymbolNode)_indexCursor).symbol()))
			{
			    _indexCursor = 
				((SymbolNode)_indexCursor).alternative();
			    if (_indexCursor == null || !passClusterTests()) 
				{
				    _queryCursor.backtrackTo(queryCursorSavepoint);
				    return false;
				};


			    assert 
				_indexCursor.kind() == TreeNode.Kind.Constant ||
				_indexCursor.kind() == TreeNode.Kind.Function ||
				_indexCursor.kind() == TreeNode.Kind.Predicate ||
				_indexCursor.kind() == TreeNode.Kind.Connective;

			}; // while (greater(querySubterm.symbol(),



		    if (((SymbolNode)_indexCursor).symbol().
			equals(querySubterm.symbol()))
			{

			    // This point is not backtrackable, so
			    // we are not saving it.

			    _indexCursor = 
				((SymbolNode)_indexCursor).treeBelow();
			    return true;
			};
			    
		    // Symbol clash: the constant was not found
		    // among the alternatives.


		    _queryCursor.backtrackTo(queryCursorSavepoint);
		    return false;

		}; // switch (querySubterm.kind())

	    // Note that neither quantifiers nor abstractions
	    // are allowed in queries or indexed terms.

	    assert false;
	    return false;

	} // stepForward()




	private boolean backtrack() {

	    assert GlobalEventCounter.inc();

	    
	    //System.out.println("   backtrack");

	    while (_stackSize != 0)
		{
		    --_stackSize;
		    BacktrackPoint point = 
			_backtrackPointStack.get(_stackSize);
		    
		    if (point.treePathTerm == null)
			{
			    // Restore the "registers":
			    _queryCursor.backtrackTo(point.queryCursorSavepoint);
			    _unifier.backtrackTo(point.unifierSavepoint);
			    _indexCursor = point.indexCursor;
			    point.clear();

			    if (_indexCursor != null) return true;
			}
		    else
			{
			    _queryCursor.backtrackTo(point.queryCursorSavepoint);

			    assert 
				point.treePathTerm.variable().isInstantiated1() !=
				point.treePathTerm.isIdenticalToVar();

			    if (point.treePathTerm.variable().isInstantiated1())
				_unifier.backtrack();

			    assert !point.treePathTerm.variable().isInstantiated1();

			    if (point.treePathTerm.findNext())
				{
				    // New instantiation for the variable, if necessary:
				    if (!point.treePathTerm.isIdenticalToVar())
					_unifier.instantiate(point.treePathTerm.variable(),
							     point.treePathTerm.flatterm());

				    ++_stackSize;

				    return true;
				};	
			};
		}; // while (_stackSize != 0)

	    return false;

	} // backtrack()


	private boolean passClusterTests() {
	    
	    //System.out.println("   test cluster");

	    assert _indexCursor != null;

	    if (_indexCursor.isClusterTest())
		{
		    if (!((ClusterTestNode)_indexCursor).test(_currentCluster))
			{

			    //System.out.println("/1/CLUSTER " + _currentCluster + " DOES NOT PASS " + ((ClusterTestNode)_indexCursor));

			    return false;
			};
		    
		    _indexCursor = 
			((ClusterTestNode)_indexCursor).treeBelow();
		    
		    if (_indexCursor.isClusterTest())
			{
			    if (!((ClusterTestNode)_indexCursor).test(_currentCluster))
				{
				    
				    //System.out.println("/2/CLUSTER " + _currentCluster + " DOES NOT PASS " + ((ClusterTestNode)_indexCursor));
				    
				    return false;
				};

			    _indexCursor = 
				((ClusterTestNode)_indexCursor).
				treeBelow();
			    assert _indexCursor != null;
			    assert !_indexCursor.isClusterTest();
			};
		};

	    return true;

	} // passClusterTests()




	//
	//          Private data:
	//


	private Substitution1 _unifier;

	private boolean _queryIsVariable;

	private Flatterm _currentQuery;

	private BitSet _currentCluster;

	/** Indicates if the query has just been reset, i.e., 
	 *  there were no calls to {@link logic.is.power.logic_warehouse#ClusteredUnificationIndex#retrieveNext(retrievedIndexedObject)}.
	 */
	private boolean _freshQuery;

	private 
	    FlattermInstance.BacktrackableIterator
	    _queryCursor;
	
	private TreeNode _indexCursor;

	/** Identifies the index tree being used currently. */
	private int _currentHashCode;

	/** Identifies, together with <code>_currentVarLeaf</code>; 
	 *  the current position in the variable index. 
	 */
	private Iterator<Map.Entry<Variable,LeafNode>> _currentVar;

	/** Identifies, together with <code>_currentVarLeaves</code>; 
	 *  the current position in the variable index. 
	 */
	private LeafNode _currentVarLeaf;
	
	private int _unifierSavepointBeforeRetrievalFromVarIndex;


	//     Stacks for backtracking:

	private int _stackSize; 

	private Vector<BacktrackPoint> _backtrackPointStack;

	/** Indicates whether currently we are retrieving from the index
	 *  for variables.
	 */
	private boolean _retrievingFromVarIndex;

    } // class RetrievalImpl


    /** Exceptions thrown when deletion or relocation methods cannot
     *  find the term-cluster pair being deleted or relocated.
     */
    private static class TermClusterPairNotFoundException extends java.lang.Exception {

	public TermClusterPairNotFoundException() {}

    } // class TermClusterPairNotFoundException




    //                     Private methods:
    
    /** <b>pre:</b> <code>leaf</code> can be <code>null</code>. */
    private int countInLeaves(BitSet cluster,LeafNode leaf) {
	
	if (leaf == null) return 0;

	return 
	    (leaf.cluster().equals(cluster))?
	    1 + countInLeaves(cluster,leaf.otherLeaves())
	    :
	    countInLeaves(cluster,leaf.otherLeaves())
	    ;

    } // countInLeaves(BitSet cluster,LeafNode leaf)


    
    /** <b>pre:</b> <code>tree</code> can be <code>null</code>. */
    private int countInTree(Term.LeanIterator termIterator,
			    BitSet cluster,
			    TreeNode tree) {

	if (tree == null) return 0;


	if (!termIterator.hasNext())
	    {
		// end of the term

		assert tree.isLeaf() || 
		    tree.kind() == TreeNode.Kind.ClusterSetBitTest ||
		    tree.kind() == TreeNode.Kind.ClusterClearBitTest; 
		
		if (tree.kind() == TreeNode.Kind.ClusterSetBitTest ||
		    tree.kind() == TreeNode.Kind.ClusterClearBitTest)
		    {
			// Check bits in the cluster:
			if (!((ClusterTestNode)tree).test(cluster))
			    return 0;
			
			return
			    countInTree(termIterator,
					cluster,
					((NonleafTreeNode)tree).
					treeBelow());
		    };

		assert tree.isLeaf();

		return countInLeaves(cluster,(LeafNode)tree);

	    }; // if (!termIterator.hasNext())


	assert (tree instanceof NonleafTreeNode);

	return countInTree(termIterator.next(),
			   termIterator,
			   cluster,
			   (NonleafTreeNode)tree);

    } // countInTree(Term.LeanIterator termIterator,..)




    /** <b>pre:</b> <code>tree != null</code>. */
    private int countInTree(Term currentSubterm,
			    Term.LeanIterator termIterator,
			    BitSet cluster,
			    NonleafTreeNode tree) {


	assert tree != null;

	
	//System.out.println("COUNTING:   TERM= " + currentSubterm + "  TREE= " + tree);

	switch (tree.kind())
	    {
	    case TreeNode.Kind.Variable: // as below
	    case TreeNode.Kind.Function: // as below
	    case TreeNode.Kind.Constant: // as below
	    case TreeNode.Kind.Predicate: // as below
	    case TreeNode.Kind.Connective:
		
		// Same treatment for all symbol nodes:

		if (currentSubterm.topSymbol() == ((SymbolNode)tree).symbol()) {
		    
		    return countInTree(termIterator,
				       cluster,
				       tree.treeBelow());
		}
		else if (((SymbolNode)tree).alternative() == null || 
			 greater(((SymbolNode)tree).symbol(),
				 currentSubterm.topSymbol())) {
		    // The term is not in the index!
		    return 0;
		}
		else {

		    return countInTree(currentSubterm,
				       termIterator,
				       cluster,
				       ((SymbolNode)tree).alternative());
		}


	    case TreeNode.Kind.ClusterSetBitTest:
		{
		    // Check the cluster:
		    
		    BitSet invertedCluster = (BitSet)cluster.clone();
		    invertedCluster.flip(0,invertedCluster.size());
		    
		    if (((ClusterSetBitTestNode)tree).mask().
			intersects(invertedCluster))
			// At least one set-bit check fails 
			// on the cluster => this term-cluster pair 
			// is not in the index!
			return 0;
		    
		    return countInTree(currentSubterm,
				       termIterator,
				       cluster,
				       (NonleafTreeNode)tree.treeBelow());


		} // case ClusterSetBitTest:


	    case TreeNode.Kind.ClusterClearBitTest:
		{
		    // Check the cluster:
		    
		    if (((ClusterClearBitTestNode)tree).mask().
			intersects(cluster))
			// At least one clear-bit check fails 
			// on the cluster => this term-cluster pair 
			// is not in the index!
			return 0;
		    
		    return countInTree(currentSubterm,
				       termIterator,
				       cluster,
				       (NonleafTreeNode)tree.treeBelow());


		} // case ClusterClearBitTest:


	    case TreeNode.Kind.Leaf:
		assert false;
		return 0;

	    }; // switch (tree.kind())

	assert false;
	return 0;

    } // countInTree(Term currentSubterm,..)




    /** Integrates the remainder of a term, represented by 
     *  <code>termIterator</code>, into the specified branch
     *  of an index tree, in accordance with the specified 
     *  cluster.
     *  <b>pre:</b> <code>tree</code> can be <code>null</code>. 
     *  @param termIterator remainder of the index term
     *  @param cluster
     *  @param setBitChecksToInsert set-bit checks that have not
     *         been performed yet in the index branch above 
     *         the current position
     *  @param clearBitChecksToInsert clear-bit checks that have not
     *         been performed yet in the index branch above 
     *         the current position
     *  @param setBitChecksToPush set-bit checks that failed
     *         on <code>cluster</code> and, therefore, have 
     *         to be pushed down the tree
     *  @param clearBitChecksToPush clear-bit checks that failed
     *         on <code>cluster</code> and, therefore, have 
     *         to be pushed down the tree
     *  @param obj indexed object
     *  @param tree where to insert
     */
    private 
	TreeNode 
	insertIntoTree(Term.LeanIterator termIterator,
		       BitSet cluster,
		       BitSet setBitChecksToInsert,
		       BitSet clearBitChecksToInsert,
		       BitSet setBitChecksToPush,
		       BitSet clearBitChecksToPush,
		       IndexedObject obj,
		       TreeNode tree) {

	if (tree == null) 
	    return addBitChecks(setBitChecksToInsert,
				clearBitChecksToInsert,
				createBranch(termIterator,cluster,obj));

	if (!termIterator.hasNext())
	    {
		// end of the term

		assert tree.isLeaf() || 
		    tree.kind() == TreeNode.Kind.ClusterSetBitTest ||
		    tree.kind() == TreeNode.Kind.ClusterClearBitTest; 

		if (tree.kind() == TreeNode.Kind.ClusterSetBitTest)
		    {
			((ClusterSetBitTestNode)tree).mask().and(cluster);
			// Keep only these checks.

			if (((ClusterSetBitTestNode)tree).mask().isEmpty())
			    {
				// No more checks: remove the node completely.
				return 
				    insertIntoTree(termIterator,
						   cluster,
						   null,
						   null,
						   null,
						   null,
						   obj,
						   ((NonleafTreeNode)tree).
						   treeBelow());
			    }
			else {
			    // At least something remains of the node.
			    
			    TreeNode newTreeBelow =
				insertIntoTree(termIterator,
					       cluster,
					       null,
					       null,
					       null,
					       null,
					       obj,
					       ((NonleafTreeNode)tree).
					       treeBelow());
				
			    ((NonleafTreeNode)tree).setTreeBelow(newTreeBelow);
			    
			    return tree;
			    
			} // if (remainingChecks.isEmpty())

		    }; // if (tree.kind() == TreeNode.Kind.ClusterSetBitTest)

		if (tree.kind() == TreeNode.Kind.ClusterClearBitTest)
		    {
			((ClusterClearBitTestNode)tree).mask().andNot(cluster);
			// Keep only these checks.

			if (((ClusterClearBitTestNode)tree).mask().isEmpty())
			    {
				// No more checks: remove the node completely.
				return 
				    insertIntoTree(termIterator,
						   cluster,
						   null,
						   null,
						   null,
						   null,
						   obj,
						   ((NonleafTreeNode)tree).
						   treeBelow());
			    }
			else {
			    // At least something remains of the node.
			    
			    TreeNode newTreeBelow =
				insertIntoTree(termIterator,
					       cluster,
					       null,
					       null,
					       null,
					       null,
					       obj,
					       ((NonleafTreeNode)tree).
					       treeBelow());
				
			    ((NonleafTreeNode)tree).setTreeBelow(newTreeBelow);
			    
			    return tree;
			    
			} // if (remainingChecks.isEmpty())

		    }; // if (tree.kind() == TreeNode.Kind.ClusterClearBitTest)

		assert tree.isLeaf();

		return new LeafNode(obj,cluster,(LeafNode)tree);

	    }; // if (!termIterator.hasNext())

	  
	// tree != null && termIterator.hasNext()
		
	assert (tree instanceof NonleafTreeNode);					
	return insertIntoTree(termIterator.next(),
			      termIterator,
			      cluster,
			      setBitChecksToInsert,
			      clearBitChecksToInsert,
			      setBitChecksToPush,
			      clearBitChecksToPush,
			      obj,
			      (NonleafTreeNode)tree);

    } // insert(Term.LeanIterator termIterator,..)




    /** <b>pre:</b> <code>tree != null</code>. */
    private 
	NonleafTreeNode insertIntoTree(Term currentSubterm,
				       Term.LeanIterator termIterator,
				       BitSet cluster,
				       BitSet setBitChecksToInsert,
				       BitSet clearBitChecksToInsert,
				       BitSet setBitChecksToPush,
				       BitSet clearBitChecksToPush,
				       IndexedObject obj,
				       NonleafTreeNode tree) {
	

	assert tree != null;
	
	switch (tree.kind())
	    {
	    case TreeNode.Kind.Variable: // as below
	    case TreeNode.Kind.Function: // as below
	    case TreeNode.Kind.Constant: // as below
	    case TreeNode.Kind.Predicate: // as below
	    case TreeNode.Kind.Connective:

		// Same treatment for all symbol nodes:

		if (currentSubterm.topSymbol().
		    equals(((SymbolNode)tree).symbol())) {
		    
		    // Push the postponed checks into the alternative branch.
		    TreeNode newAlternative = 
			addBitChecks(setBitChecksToPush,
				     clearBitChecksToPush,
				     ((SymbolNode)tree).alternative());

		    ((SymbolNode)tree).
			setAlternative((NonleafTreeNode)newAlternative);
		    


		    tree.setTreeBelow(insertIntoTree(termIterator,
						     cluster,
						     setBitChecksToInsert,
						     clearBitChecksToInsert,
						     setBitChecksToPush,
						     clearBitChecksToPush,
						     obj,
						     tree.treeBelow()));
		}
		else // !currentSubterm.topSymbol().equals(((SymbolNode)tree).symbol())
		    {
			
			if (greater(((SymbolNode)tree).symbol(),
				    currentSubterm.topSymbol()))
			    {
				// Don't go into the alternative so that
				// the ordering of nodes is preserved:
				

				SymbolNode newNode = 
				    newSymbolNode(currentSubterm.topSymbol(),
						  insertIntoTree(termIterator,
								 cluster,
								 setBitChecksToInsert,
								 clearBitChecksToInsert,
								 setBitChecksToPush,
								 clearBitChecksToPush,
								 obj,
								 null));
				
				// Push the postponed checks into the alternative branch.
				TreeNode alternative = 
				    addBitChecks(setBitChecksToPush,
						 clearBitChecksToPush,
						 tree);

				newNode.setAlternative((NonleafTreeNode)alternative);
				    
				return newNode;
			    }
			else if (((SymbolNode)tree).alternative() == null) {

			    // Push the postponed checks into the tree below 
			    // the current node.
			    TreeNode newTreeBelow = 
				addBitChecks(setBitChecksToPush,
					     clearBitChecksToPush,
					     ((SymbolNode)tree).treeBelow());
			    
			    ((SymbolNode)tree).setTreeBelow(newTreeBelow);
			    
			    SymbolNode newNode = 
				newSymbolNode(currentSubterm.topSymbol(),
					      insertIntoTree(termIterator,
							     cluster,
							     setBitChecksToInsert,
							     clearBitChecksToInsert,
							     setBitChecksToPush,
							     clearBitChecksToPush,
							     obj,
							     null));
			    ((SymbolNode)tree).setAlternative(newNode);
			}
			else 
			    {
				// Go into the alternative.


				// Push the postponed checks into the tree below 
				// the current node.
				TreeNode newTreeBelow = 
				    addBitChecks(setBitChecksToPush,
						 clearBitChecksToPush,
						 ((SymbolNode)tree).treeBelow());
				

				((SymbolNode)tree).setTreeBelow(newTreeBelow);
			
				NonleafTreeNode newAlternative =
				    insertIntoTree(currentSubterm,
						   termIterator,
						   cluster,
						   setBitChecksToInsert,
						   clearBitChecksToInsert,
						   setBitChecksToPush,
						   clearBitChecksToPush,
						   obj,
						   ((SymbolNode)tree).alternative());
				

				
				// Go into the alternative:
				((SymbolNode)tree).setAlternative(newAlternative);
			    }
		    } // if (currentSubterm.topSymbol() == ((SymbolNode)tree).symbol())

		return tree;



	    case TreeNode.Kind.ClusterSetBitTest:
		{
		    BitSet mask = ((ClusterSetBitTestNode)tree).mask();

		    BitSet remainingChecks = (BitSet)mask.clone();
		    remainingChecks.and(cluster);
		    // Here remainingChecks contains checks that 
		    // can be left in this node.

		    assert (tree.treeBelow() instanceof NonleafTreeNode);
		    // Because we have currentSubterm to insert!

			
		    if (remainingChecks.isEmpty()) {
			    
			// The node has to go. All the checks have to be
			// pushed down the tree.

			setBitChecksToPush.or(mask);

			// setBitChecksToInsert does not change here.

			return insertIntoTree(currentSubterm,
					      termIterator,
					      cluster,
					      setBitChecksToInsert,
					      clearBitChecksToInsert,
					      setBitChecksToPush,
					      clearBitChecksToPush,
					      obj,
					      (NonleafTreeNode)tree.treeBelow());
		    }
		    else {

			// remainingChecks will remain in the node.
			((ClusterSetBitTestNode)tree).setMask(remainingChecks);

			// No need to do checks from remainingChecks 
			// below this node:
			setBitChecksToInsert.xor(remainingChecks);

			
			// Some checks are to be pushed down the tree: 
			setBitChecksToPush.or(mask);
			setBitChecksToPush.xor(remainingChecks);
			
			tree.setTreeBelow(insertIntoTree(currentSubterm,
							 termIterator,
							 cluster,
							 setBitChecksToInsert,
							 clearBitChecksToInsert,
							 setBitChecksToPush,
							 clearBitChecksToPush,
							 obj,
							 (NonleafTreeNode) tree.treeBelow()));


			return tree;
			    
		    } // if (remainingChecks.isEmpty()) 
			
		} // case ClusterSetBitTest:




	    case TreeNode.Kind.ClusterClearBitTest:
		{
		    BitSet mask = ((ClusterClearBitTestNode)tree).mask();

		    BitSet remainingChecks = (BitSet)mask.clone();
		    remainingChecks.andNot(cluster);
		    // Here remainingChecks contains checks that 
		    // can be left in this node.


		    assert (tree.treeBelow() instanceof NonleafTreeNode);
		    // Because we have currentSubterm to insert!

			
		    if (remainingChecks.isEmpty()) {
			    
			// The node has to go. All the checks have to be
			// pushed down the tree.

			clearBitChecksToPush.or(mask);

			// clearBitChecksToInsert does not change here.

			return insertIntoTree(currentSubterm,
					      termIterator,
					      cluster,
					      setBitChecksToInsert,
					      clearBitChecksToInsert,
					      setBitChecksToPush,
					      clearBitChecksToPush,
					      obj,
					      (NonleafTreeNode)
					      tree.treeBelow());
		    }
		    else {

			// remainingChecks will remain in the node.
			((ClusterClearBitTestNode)tree).setMask(remainingChecks);

			// No need to do checks from remainingChecks 
			// below this node:
			clearBitChecksToInsert.xor(remainingChecks);

			
			// Some checks are to be pushed down the tree: 
			clearBitChecksToPush.or(mask);
			clearBitChecksToPush.xor(remainingChecks);
			
			tree.setTreeBelow(insertIntoTree(currentSubterm,
							 termIterator,
							 cluster,
							 setBitChecksToInsert,
							 clearBitChecksToInsert,
							 setBitChecksToPush,
							 clearBitChecksToPush,
							 obj,
							 (NonleafTreeNode)
							 tree.treeBelow()));

			return tree;
			    
		    } // if (remainingChecks.isEmpty()) 
			

		} // case ClusterClearBitTest:



	    case TreeNode.Kind.Leaf:
		assert false;
		return null;

	    }; // switch (tree.kind())

	assert false;
	return null;

    } // insertIntoTree(Term currentSubterm,..)




    private TreeNode createBranch(Term.LeanIterator termIterator,
				  BitSet cluster,
				  IndexedObject obj) {
	
	if (!termIterator.hasNext())
	    // end of the term
	    return new LeafNode(obj,cluster,null);
     	
	Term subterm = termIterator.next();
	
	TreeNode result = createBranch(termIterator,cluster,obj);
	
	switch (subterm.kind())
	    {
	    case Term.Kind.Variable:
		return new VariableNode((Variable)subterm,result);
		
	    case Term.Kind.CompoundTerm: 
		return new FunctionNode(((CompoundTerm)subterm).function(),
					result);
		
	    case Term.Kind.IndividualConstant:
		return new ConstantNode((IndividualConstant)subterm,
					result);

	    case Term.Kind.AtomicFormula:
		return new PredicateNode(((AtomicFormula)subterm).predicate(),
					 result);
      
	    case Term.Kind.ConnectiveApplication:
		return new ConnectiveNode(((ConnectiveApplication)subterm).connective(),
					  result);

	    case Term.Kind.QuantifierApplication:
		assert false;
		return null;

	    case Term.Kind.AbstractionTerm: 
		assert false;
		return null;

	    case Term.Kind.TermPair:
		assert false;
		return null;

	    }; // switch (subter.kind())

	assert false;
	return null;

    } // createBranch(Term.LeanIterator termIterator,..)



    /** Adds the specified clear-bit checks in the beginning of the tree. 
     *  <b>pre:</b> <code>tree != null</code>
     */
    private static TreeNode addClearBitChecks(BitSet clearBitChecks, 
					      TreeNode tree) {
	
	assert tree != null;

	// The order of insertion of set-bit checks and clear-bit
	// checks is important: we assume and guarantee that
	// no ClusterClearBitTestNode can directly preceed
	// a ClusterSetBitTestNode in the tree.

	if (clearBitChecks.isEmpty())
	    return tree;

	switch (tree.kind()) {
	    
	case TreeNode.Kind.Variable: // as below
	case TreeNode.Kind.Function: // as below
	case TreeNode.Kind.Constant: // as below
	case TreeNode.Kind.Predicate: // as below
	case TreeNode.Kind.Connective:
	    // Same treatment for all symbol nodes.
	    return
		new ClusterClearBitTestNode(clearBitChecks,
					    tree);
	    
	case TreeNode.Kind.ClusterSetBitTest:
	    // We are assuming here that no ClusterClearBitTest
	    // can directly preceed a ClusterSetBitTest.
	    ((NonleafTreeNode)tree).
		setTreeBelow(addClearBitChecks(clearBitChecks,
					       ((NonleafTreeNode)tree).
					       treeBelow()));
	    return tree;
	    
	case TreeNode.Kind.ClusterClearBitTest:
	    // Add new checks to this node.
	    ((ClusterClearBitTestNode)tree).mask().or(clearBitChecks);
	    return tree;
	    
	    
	    
	case TreeNode.Kind.Leaf:
	    return
		new ClusterClearBitTestNode(clearBitChecks,
					    tree);
	    
	} // switch (tree.kind())
		
	assert false;
	return null;

    } // addClearBitChecks(BitSet clearBitChecks, 



    /** Adds the specified set-bit checks in the beginning of the tree. 
     *  <b>pre:</b> <code>tree != null</code>
     */
    private static TreeNode addSetBitChecks(BitSet setBitChecks,
					    TreeNode tree) {
	
	assert tree != null;

	// The order of insertion of set-bit checks and clear-bit
	// checks is important: we assume and guarantee that
	// no ClusterClearBitTestNode can directly preceed
	// a ClusterSetBitTestNode in the tree.

	if (setBitChecks.isEmpty()) 
	    return tree;

	switch (tree.kind()) {
	    
	case TreeNode.Kind.Variable: // as below
	case TreeNode.Kind.Function: // as below
	case TreeNode.Kind.Constant: // as below
	case TreeNode.Kind.Predicate: // as below
	case TreeNode.Kind.Connective:
	    // Same treatment for all symbol nodes.
	    return 
		new ClusterSetBitTestNode(setBitChecks,
					  tree);
	    
	case TreeNode.Kind.ClusterSetBitTest:
	    // Add new checks to this node.
	    ((ClusterSetBitTestNode)tree).mask().or(setBitChecks);
	    return tree;
	    
	    
	case TreeNode.Kind.ClusterClearBitTest:
	    // We are assuming here that no ClusterClearBitTest
	    // can directly preceed a ClusterSetBitTest.
	    assert 
		((NonleafTreeNode)tree).treeBelow().kind() !=
		TreeNode.Kind.ClusterSetBitTest;
	    return 
		new ClusterSetBitTestNode(setBitChecks,
					  tree);
	    
	case TreeNode.Kind.Leaf:
	    return
		new ClusterSetBitTestNode(setBitChecks,
					  tree);
	    
	} // switch (tree.kind())
	
	assert false;
	return null;

    } // addSetBitChecks(BitSet setBitChecks,..)




    /** Adds the specified bit checks in the beginning of the tree. 
     *  <b>pre:</b> <code>tree</code> can be <code>null</code>, in which
     *  case <code>null</code> is returned.
     */
    private static TreeNode addBitChecks(BitSet setBitChecks,
					 BitSet clearBitChecks, 
					 TreeNode tree) {
	
	if (tree == null) return null;

	// The order of insertion of set-bit checks and clear-bit
	// checks is important: we assume and guarantee that
	// no ClusterClearBitTestNode can directly preceed
	// a ClusterSetBitTestNode in the tree.

	return addSetBitChecks(setBitChecks,
			       addClearBitChecks(clearBitChecks, 
						 tree));


    } // addBitChecks(BitSet setBitChecks,..)
	




    
    /** <b>pre:</b> <code>tree</code> can be <code>null</code>. */
    private 
	TreeNode 
	eraseFromTree(Term.LeanIterator termIterator,
		      BitSet cluster,
		      UnaryPredicateObject<IndexedObject> hasToBeRemoved,
		      TreeNode tree) 
    throws TermClusterPairNotFoundException {
	
	if (tree == null) throw new TermClusterPairNotFoundException();

	if (!termIterator.hasNext())
	    {
		// end of the term

		assert tree.isLeaf() || 
		    tree.kind() == TreeNode.Kind.ClusterSetBitTest ||
		    tree.kind() == TreeNode.Kind.ClusterClearBitTest; 
		
		if (tree.kind() == TreeNode.Kind.ClusterSetBitTest ||
		    tree.kind() == TreeNode.Kind.ClusterClearBitTest)
		    return
			eraseFromTree(termIterator,
				      cluster,
				      hasToBeRemoved,
				      ((NonleafTreeNode)tree).
				      treeBelow());

		assert tree.isLeaf();

		return 
		    pullCommonChecksFromLeaves(eraseLeaves(cluster,
							   hasToBeRemoved,
							   (LeafNode)tree,
							   true));

	    }; // if (!termIterator.hasNext())


	assert (tree instanceof NonleafTreeNode);

	return eraseFromTree(termIterator.next(),
			     termIterator,
			     cluster,
			     hasToBeRemoved,
			     (NonleafTreeNode)tree);

    } // eraseFromTree(Term.LeanIterator termIterator,..)
	


    
    /** <b>pre:</b> <code>tree != null</code>. */
    private 
	NonleafTreeNode 
	eraseFromTree(Term currentSubterm,
		      Term.LeanIterator termIterator,
		      BitSet cluster,
		      UnaryPredicateObject<IndexedObject> hasToBeRemoved,
		      NonleafTreeNode tree)  
	throws TermClusterPairNotFoundException {
	
	assert tree != null;

	
	switch (tree.kind())
	    {
	    case TreeNode.Kind.Variable: // as below
	    case TreeNode.Kind.Function: // as below
	    case TreeNode.Kind.Constant: // as below
	    case TreeNode.Kind.Predicate: // as below
	    case TreeNode.Kind.Connective:
		
		// Same treatment for all symbol nodes:

		if (currentSubterm.topSymbol() == ((SymbolNode)tree).symbol()) {
		    
		    TreeNode newTreeBelow = 
			eraseFromTree(termIterator,
				      cluster,
				      hasToBeRemoved,
				      tree.treeBelow());

		    if (newTreeBelow == null) 
			return 
			    ((SymbolNode)tree).alternative();

		    tree.setTreeBelow(newTreeBelow);

		    return pullCommonChecks((SymbolNode)tree);
		}
		else if (((SymbolNode)tree).alternative() == null || 
			 greater(((SymbolNode)tree).symbol(),
				 currentSubterm.topSymbol())) {
		    // The term is not in the index!
		    throw new TermClusterPairNotFoundException();
		}
		else {

		    NonleafTreeNode newAlternative =
			eraseFromTree(currentSubterm,
				      termIterator,
				      cluster,
				      hasToBeRemoved,
				      ((SymbolNode)tree).alternative());

		    ((SymbolNode)tree).setAlternative(newAlternative);

		    return pullCommonChecks((SymbolNode)tree);
		}





	    case TreeNode.Kind.ClusterSetBitTest:
		{
		    // Check the cluster:
		    
		    BitSet invertedCluster = (BitSet)cluster.clone();
		    invertedCluster.flip(0,invertedCluster.size());
		    
		    if (((ClusterSetBitTestNode)tree).mask().
			intersects(invertedCluster))
			// At least one set-bit check fails 
			// on the cluster => this term-cluster pair 
			// is not in the index!
			throw new TermClusterPairNotFoundException();
			
		    return
			eraseFromTree(currentSubterm,
				      termIterator,
				      cluster,
				      hasToBeRemoved,
				      (NonleafTreeNode)tree.treeBelow());

		} // case ClusterSetBitTest:


	    case TreeNode.Kind.ClusterClearBitTest:
		{
		    // Check the cluster:
		    
		    if (((ClusterClearBitTestNode)tree).mask().
			intersects(cluster))
			// At least one clear-bit check fails 
			// on the cluster => this term-cluster pair 
			// is not in the index!
			throw new TermClusterPairNotFoundException();

		    return
			eraseFromTree(currentSubterm,
				      termIterator,
				      cluster,
				      hasToBeRemoved,
				      (NonleafTreeNode)tree.treeBelow());

		} // case ClusterClearBitTest:


	    case TreeNode.Kind.Leaf:
		assert false;
		return null;

	    }; // switch (tree.kind())

	assert false;
	return null;

    } // eraseFromTree(Term currentSubterm,..)



    
    /** <b>pre:</b> <code>leaf</code> can be <code>null</code>. */
    private 
	LeafNode 
	eraseLeaves(BitSet cluster,
		    UnaryPredicateObject<IndexedObject> hasToBeRemoved,
		    LeafNode leaf,
		    boolean throwIfNotFound) 
    throws TermClusterPairNotFoundException {
	
	if (leaf == null) 
	    if (throwIfNotFound) 
		{
		    throw new TermClusterPairNotFoundException();
		}
	    else return null;

	if (leaf.cluster().equals(cluster) &&
	    hasToBeRemoved.evaluate((IndexedObject)leaf.indexedObject()))
	    return 
		eraseLeaves(cluster,
			    hasToBeRemoved,
			    leaf.otherLeaves(),
			    false);
	    
	leaf.setOtherLeaves(eraseLeaves(cluster,
					hasToBeRemoved,
					leaf.otherLeaves(),
					throwIfNotFound));

	return leaf;

    } // eraseLeaves(BitSet cluster,..)


    
    private 
	static 
	TreeNode 
	pullCommonChecksFromLeaves(LeafNode tree) {
	
	if (tree == null) return null;


	Pair<BitSet,BitSet> commonBitChecks =
	    commonChecksForLeaves(tree);

	TreeNode result = tree;

	if (!commonBitChecks.second.isEmpty())
	    {
		result = new ClusterClearBitTestNode(commonBitChecks.second,
						     result);

	    };

	if (!commonBitChecks.first.isEmpty())
	    {
		result = new ClusterSetBitTestNode(commonBitChecks.first,
						   result);

	    };
	
	return result;

    } // pullCommonChecksFromLeaves(LeafNode tree) 
	

    private 
	static 
	Pair<BitSet,BitSet>
	commonChecksForLeaves(LeafNode tree) {

	assert tree != null;

	BitSet setBitChecks = 
	    (BitSet)tree.cluster().clone();
	BitSet clearBitChecks =
	    (BitSet)tree.cluster().clone();
	clearBitChecks.flip(0,clearBitChecks.size());
	
	Pair<BitSet,BitSet> result = 
	    new Pair<BitSet,BitSet>(setBitChecks,
				    clearBitChecks);
	
	while (tree.otherLeaves() != null)
	    {
		tree = tree.otherLeaves();
		result.first.and(tree.cluster());
		result.second.andNot(tree.cluster());
	    };

	return result;

    } // commonChecksForLeaves(LeafNode tree)
    
    


    private 
	static 
	NonleafTreeNode 
	pullCommonChecks(NonleafTreeNodeWithAlternative tree) {


	assert tree != null;

	if (tree.alternative() == null)
	    {
		ClusterSetBitTestNode setBitChecks = null;
		ClusterClearBitTestNode clearBitChecks = null;
		
		TreeNode newTreeBelow = tree.treeBelow();

		if (newTreeBelow.kind() == 
		    TreeNode.Kind.ClusterSetBitTest)
		    {
			setBitChecks = (ClusterSetBitTestNode)newTreeBelow;
			newTreeBelow = 
			    ((ClusterSetBitTestNode)newTreeBelow).treeBelow();

		    };

		if (newTreeBelow.kind() == 
		    TreeNode.Kind.ClusterClearBitTest)
		    {
			clearBitChecks = (ClusterClearBitTestNode)newTreeBelow;
			newTreeBelow = 
			    ((ClusterClearBitTestNode)newTreeBelow).treeBelow();
		    };


		assert newTreeBelow.kind() != 
		    TreeNode.Kind.ClusterSetBitTest;
		assert newTreeBelow.kind() != 
		    TreeNode.Kind.ClusterClearBitTest;

		NonleafTreeNode result = tree;

		result.setTreeBelow(newTreeBelow);
		
		if (clearBitChecks != null)
		    {
			clearBitChecks.setTreeBelow(result);
			result = clearBitChecks;
		    };

		if (setBitChecks != null)
		    {
			setBitChecks.setTreeBelow(result);
			result = setBitChecks;
		    };
		
		return result;
	    }
	else // tree.alternative() != null
	    {
		// Bit checks from the tree below:
		ClusterSetBitTestNode setBitChecks1 = null;
		ClusterClearBitTestNode clearBitChecks1 = null;
		
		TreeNode treeBelow = tree.treeBelow();

		if (treeBelow.kind() == TreeNode.Kind.ClusterSetBitTest)
		    {
			setBitChecks1 = (ClusterSetBitTestNode)treeBelow;
			treeBelow = 
			    ((NonleafTreeNode)treeBelow).treeBelow();
		    };

		if (treeBelow.kind() == TreeNode.Kind.ClusterClearBitTest)
		    {
			clearBitChecks1 = (ClusterClearBitTestNode)treeBelow;
			treeBelow = 
			    ((NonleafTreeNode)treeBelow).treeBelow();
		    };



		// Bit checks from the alternative:
		ClusterSetBitTestNode setBitChecks2 = null;
		ClusterClearBitTestNode clearBitChecks2 = null;

		NonleafTreeNode alternative = tree.alternative();

		if (alternative.kind() == TreeNode.Kind.ClusterSetBitTest)
		    {
			setBitChecks2 = (ClusterSetBitTestNode)alternative;
			alternative = (NonleafTreeNode)alternative.treeBelow();
		    };

		if (alternative.kind() == TreeNode.Kind.ClusterClearBitTest)
		    {
			clearBitChecks2 = (ClusterClearBitTestNode)alternative;
			alternative = (NonleafTreeNode)alternative.treeBelow();
		    };
		

		BitSet newSetBitChecks = null;
		BitSet newClearBitChecks = null;
		   

		if (clearBitChecks1 != null && clearBitChecks2 != null)
		    {
			if (clearBitChecks1.mask().
			    intersects(clearBitChecks2.mask()))
			    {
				// There is an overlap that has to be lifted up.
				newClearBitChecks = 
				    (BitSet)clearBitChecks1.mask().clone();
				newClearBitChecks.and(clearBitChecks2.mask());
				
				assert !newClearBitChecks.isEmpty();
				
				// Remove the lifted checks from clearBitChecks1
				// and clearBitChecks2:
				
				clearBitChecks1.mask().andNot(newClearBitChecks);
				clearBitChecks2.mask().andNot(newClearBitChecks);
				
			    }; // if (clearBitChecks1.intersects(clearBitChecks2))
		    }; // if (clearBitChecks1 != null && clearBitChecks2 != null)


		if (setBitChecks1 != null && setBitChecks2 != null)
		    {
			if (setBitChecks1.mask().
			    intersects(setBitChecks2.mask()))
			    {
				// There is an overlap that has to be lifted up.
				newSetBitChecks = 
				    (BitSet)setBitChecks1.mask().clone();
				newSetBitChecks.and(setBitChecks2.mask());
				
				assert !newSetBitChecks.isEmpty();
				
				// Remove the lifted checks from setBitChecks1
				// and setBitChecks2:
				
				setBitChecks1.mask().andNot(newSetBitChecks);
				setBitChecks2.mask().andNot(newSetBitChecks);
				
			    }; // if (setBitChecks1.intersects(setBitChecks2))

			
		    }; // if (setBitChecks1 != null && setBitChecks2 != null)





		// Remove old nodes with empty masks:

		if (clearBitChecks1 != null && clearBitChecks1.mask().isEmpty())
		    {
			// Remove this node:
			
			if (setBitChecks1 == null)
			    {
				tree.setTreeBelow(clearBitChecks1.treeBelow());
			    }
			else
			    setBitChecks1.setTreeBelow(clearBitChecks1.treeBelow());

		    };

		if (setBitChecks1 != null && setBitChecks1.mask().isEmpty())
		    // Remove this node:
		    tree.setTreeBelow(setBitChecks1.treeBelow());


		
		if (clearBitChecks2 != null && clearBitChecks2.mask().isEmpty())
		    {
			// Remove this node:
			
			if (setBitChecks2 == null)
			    {
				tree.setAlternative((NonleafTreeNode)
						    clearBitChecks2.treeBelow());
			    }
			else
			    setBitChecks2.setTreeBelow(clearBitChecks2.treeBelow());

		    };

		if (setBitChecks2 != null && setBitChecks2.mask().isEmpty())
		    // Remove this node:
		    tree.setAlternative((NonleafTreeNode)setBitChecks2.treeBelow());



		// Add new nodes if necessary:

		NonleafTreeNode result = tree;

		if (newClearBitChecks != null)
		    result = new ClusterClearBitTestNode(newClearBitChecks,
							 result);

		if (newSetBitChecks != null)
		    result = new ClusterSetBitTestNode(newSetBitChecks,
						       result);


		return result;

	    } // if (tree.alternative() == null)
	    
    } // pullCommonChecks(NonleafTreeNodeWithAlternative tree)



    
    /** <b>pre:</b> <code>tree</code> can be <code>null</code>. */
    private 
	TreeNode 
	relocateInTree(Term.LeanIterator termIterator,
		       BitSet cluster,
		       BitSet newCluster,
		       UnaryPredicateObject<IndexedObject> hasToBeRelocated,
		       TreeNode tree) 
    throws TermClusterPairNotFoundException {
	
	if (tree == null) throw new TermClusterPairNotFoundException();
	
	if (!termIterator.hasNext())
	    {
		// end of the term
		
		assert tree.isLeaf() || 
		    tree.kind() == TreeNode.Kind.ClusterSetBitTest ||
		    tree.kind() == TreeNode.Kind.ClusterClearBitTest; 
		
		if (tree.kind() == TreeNode.Kind.ClusterSetBitTest ||
		    tree.kind() == TreeNode.Kind.ClusterClearBitTest)
		    return
			relocateInTree(termIterator,
				       cluster,
				       newCluster,
				       hasToBeRelocated,
				       ((NonleafTreeNode)tree).
				       treeBelow());
		assert tree.isLeaf();
		
		relocateLeaves(cluster,
			       newCluster,
			       hasToBeRelocated,
			       (LeafNode)tree);
		
		return 
		    pullCommonChecksFromLeaves((LeafNode)tree);
		
	    }; // if (!termIterator.hasNext())
	
	
	assert (tree instanceof NonleafTreeNode);
	
	return relocateInTree(termIterator.next(),
			      termIterator,
			      cluster,
			      newCluster,
			      hasToBeRelocated,
			      (NonleafTreeNode)tree);
	
    } // relocateInTree(Term.LeanIterator termIterator,..)





    
    /** <b>pre:</b> <code>tree != null</code>. */
    private 
	NonleafTreeNode 
	relocateInTree(Term currentSubterm,
		       Term.LeanIterator termIterator,
		       BitSet cluster,
		       BitSet newCluster,
		       UnaryPredicateObject<IndexedObject> hasToBeRelocated,
		       NonleafTreeNode tree) 
	throws TermClusterPairNotFoundException {
	
	assert tree != null;
	
	switch (tree.kind())
	    {
	    case TreeNode.Kind.Variable: // as below
	    case TreeNode.Kind.Function: // as below
	    case TreeNode.Kind.Constant: // as below
	    case TreeNode.Kind.Predicate: // as below
	    case TreeNode.Kind.Connective:
		
		// Same treatment for all symbol nodes:

		if (currentSubterm.topSymbol() == ((SymbolNode)tree).symbol()) {
		    
		    TreeNode newTreeBelow = 
			relocateInTree(termIterator,
				       cluster,
				       newCluster,
				       hasToBeRelocated,
				       tree.treeBelow());

		    tree.setTreeBelow(newTreeBelow);

		    return pullCommonChecks((SymbolNode)tree);
		}
		else if (((SymbolNode)tree).alternative() == null || 
			 greater(((SymbolNode)tree).symbol(),
				 currentSubterm.topSymbol())) {
		    // The term is not in the index!
		    throw new TermClusterPairNotFoundException();
		}
		else {

		    NonleafTreeNode newAlternative =
			relocateInTree(currentSubterm,
				       termIterator,
				       cluster,
				       newCluster,
				       hasToBeRelocated,
				       ((SymbolNode)tree).alternative());

		    ((SymbolNode)tree).setAlternative(newAlternative);

		    return pullCommonChecks((SymbolNode)tree);
		}




	    case TreeNode.Kind.ClusterSetBitTest:
		{
		    // Check the cluster:
		    
		    BitSet invertedCluster = (BitSet)cluster.clone();
		    invertedCluster.flip(0,invertedCluster.size());
		    
		    if (((ClusterSetBitTestNode)tree).mask().
			intersects(invertedCluster))
			// At least one set-bit check fails 
			// on the cluster => this term-cluster pair 
			// is not in the index!
			throw new TermClusterPairNotFoundException();

		    NonleafTreeNode newTreeBelow = 
			relocateInTree(currentSubterm,
				       termIterator,
				       cluster,
				       newCluster,
				       hasToBeRelocated,
				       (NonleafTreeNode)tree.treeBelow());
			
		    ((ClusterSetBitTestNode)tree).mask().and(newCluster);
		    if (((ClusterSetBitTestNode)tree).mask().isEmpty())
			// The node is completely removed.
			return newTreeBelow;

		    // Try to merge the node with the root node in newTreeBelow:

		    if (newTreeBelow.kind() == TreeNode.Kind.ClusterSetBitTest)
			{
			    ((ClusterSetBitTestNode)newTreeBelow).
				mask().
				or(((ClusterSetBitTestNode)tree).mask());
			    
			    return newTreeBelow;
			};

		    // newTreeBelow does not start with a ClusterSetBitTestNode:

		    tree.setTreeBelow(newTreeBelow);

		    return tree;

		} // case ClusterSetBitTest:


	    case TreeNode.Kind.ClusterClearBitTest:
		{
		    // Check the cluster:
		    
		    if (((ClusterClearBitTestNode)tree).mask().
			intersects(cluster))
			// At least one clear-bit check fails 
			// on the cluster => this term-cluster pair 
			// is not in the index!
			throw new TermClusterPairNotFoundException();

		    NonleafTreeNode newTreeBelow = 
			relocateInTree(currentSubterm,
				       termIterator,
				       cluster,
				       newCluster,
				       hasToBeRelocated,
				       (NonleafTreeNode)tree.treeBelow());

		    
		    ((ClusterClearBitTestNode)tree).mask().andNot(newCluster);
		    if (((ClusterClearBitTestNode)tree).mask().isEmpty())
			// The node is completely removed.
			return newTreeBelow;

		    // Try to merge the node with the highest ClusterClearBitTestNode
		    // in newTreeBelow:

		    ClusterClearBitTestNode clearBitTest;

		    if (newTreeBelow.kind() == TreeNode.Kind.ClusterClearBitTest)
			{
			    clearBitTest = (ClusterClearBitTestNode)newTreeBelow;
			}
		    else if (newTreeBelow.kind() == TreeNode.Kind.ClusterSetBitTest &&
			     newTreeBelow.treeBelow().kind() ==  
			     TreeNode.Kind.ClusterClearBitTest)
			{
			    clearBitTest = 
				(ClusterClearBitTestNode)newTreeBelow.treeBelow();
			}
		    else
			{
			    // No appropriate ClusterClearBitTestNode
			    // in newTreeBelow.

			    if (newTreeBelow.kind() == TreeNode.Kind.ClusterSetBitTest)
				{
				    // (ClusterClearBitTestNode)tree has to be pushed under
				    // the ClusterClearBitTest node:
				    
				    ((ClusterClearBitTestNode)tree).
					setTreeBelow(((ClusterSetBitTestNode)newTreeBelow).
						     treeBelow());
				    
				    ((ClusterSetBitTestNode)newTreeBelow).
					setTreeBelow(tree);

				    return newTreeBelow;
				};

			    tree.setTreeBelow(newTreeBelow);			    
			    return tree;
			};

		    clearBitTest.mask().or(((ClusterClearBitTestNode)tree).mask());

		    return newTreeBelow;

		} // case ClusterClearBitTest:


	    case TreeNode.Kind.Leaf:
		assert false;
		return null;

	    }; // switch (tree.kind())

	assert false;
	return null;

    } // relocateInTree(Term currentSubterm,..)




    /** <b>pre:</b> <code>leaf</code> can be <code>null</code>. */
    private static void relocateLeaves(BitSet cluster,
				       BitSet newCluster,
				       UnaryPredicateObject hasToBeRelocated,
				       LeafNode leaf) 
    throws TermClusterPairNotFoundException {

	boolean somethingChanged = false;

	while (leaf != null)
	    {
		if (leaf.cluster().equals(cluster) &&
		    hasToBeRelocated.evaluate(leaf.indexedObject()))
		    {
			somethingChanged = true;
			leaf.setCluster((BitSet)newCluster.clone());
		    };
		leaf = leaf.otherLeaves();
	    };
	
	if (!somethingChanged) throw new TermClusterPairNotFoundException();


    } // relocateLeaves(BitSet cluster,..)


	


    private static SymbolNode newSymbolNode(Symbol sym,
					    TreeNode treeBelow) {

	switch (sym.category())
	    {
	    case Symbol.Category.Connective: 
		return new ConnectiveNode((Connective)sym,treeBelow);
	    case Symbol.Category.Function:
		return new FunctionNode((Function)sym,treeBelow);
	    case Symbol.Category.IndividualConstant: 
		return new ConstantNode((IndividualConstant)sym,treeBelow);
	    case Symbol.Category.Predicate: 
		return new PredicateNode((Predicate)sym,treeBelow);
	    case Symbol.Category.Quantifier: 
		assert false;
		return null;
		
	    case Symbol.Category.Variable:  
		return new VariableNode((Variable)sym,treeBelow);
	    }; // switch (sym.category())

	assert false;
	return null;

    } // newSymbolNode(Symbol sym,..)


    private static String treeToString(TreeNode tree) {
	
	assert tree != null;
	
	HashMap<TreeNode,Integer> nodeNumbers =
	    new HashMap<TreeNode,Integer>();
	nodeNumbers.put(tree,new Integer(0));

	Ref<Integer> nextFreshNodeNumber = 
	    new Ref<Integer>(new Integer(1));

	return treeToString(tree,nodeNumbers,nextFreshNodeNumber);
	
    } // treeToString(TreeNode tree)


    private static String treeToString(TreeNode tree,
				       HashMap<TreeNode,Integer> nodeNumbers,
				       Ref<Integer> nextFreshNodeNumber) {
	
	assert tree != null;

	Integer currentNumber = nodeNumbers.get(tree);

	String result = "[" + currentNumber + "] ";
       
	switch (tree.kind())
	    {
	    case TreeNode.Kind.Variable: // as below
	    case TreeNode.Kind.Function: // as below
	    case TreeNode.Kind.Constant: // as below
	    case TreeNode.Kind.Predicate: // as below
	    case TreeNode.Kind.Connective: 
		// Same treatment for all symbol nodes.
		nodeNumbers.put(((SymbolNode)tree).treeBelow(),
				nextFreshNodeNumber.content);
		nextFreshNodeNumber.content = nextFreshNodeNumber.content + 1;

		result += tree.toString();

		if (((SymbolNode)tree).alternative() != null)
		    {
			nodeNumbers.put(((SymbolNode)tree).alternative(),
					nextFreshNodeNumber.content);

			result += ", ALT [" + nextFreshNodeNumber.content + "]";
			nextFreshNodeNumber.content = nextFreshNodeNumber.content + 1;
		    };


		result += "\n" + treeToString(((SymbolNode)tree).treeBelow(),
					      nodeNumbers,
					      nextFreshNodeNumber);
		
		nodeNumbers.remove(((SymbolNode)tree).treeBelow());
		
		if (((SymbolNode)tree).alternative() != null)
		    result += "\n     ALTERNATIVE TO [" + currentNumber + "]\n" +
			treeToString(((SymbolNode)tree).alternative(),
				     nodeNumbers,
				     nextFreshNodeNumber);

		nodeNumbers.remove(((SymbolNode)tree).alternative());
		
		return result;


	    case TreeNode.Kind.ClusterSetBitTest: // as below
	    case TreeNode.Kind.ClusterClearBitTest:
		nodeNumbers.put(((NonleafTreeNode)tree).treeBelow(),
				nextFreshNodeNumber.content);
		nextFreshNodeNumber.content = nextFreshNodeNumber.content + 1;

		result += tree.toString() + "\n" +
		    treeToString(((NonleafTreeNode)tree).treeBelow(),
				 nodeNumbers,
				 nextFreshNodeNumber);

		nodeNumbers.remove(((NonleafTreeNode)tree).treeBelow());
		
		return result;

	    case TreeNode.Kind.Leaf:
		result += tree.toString() + "\n";
		if (((LeafNode)tree).otherLeaves() != null)
		    {
			nodeNumbers.put(((LeafNode)tree).otherLeaves(),
					nextFreshNodeNumber.content);
			nextFreshNodeNumber.content = nextFreshNodeNumber.content + 1;
			result += treeToString(((LeafNode)tree).otherLeaves(),
					       nodeNumbers,
					       nextFreshNodeNumber);
			nodeNumbers.remove(((LeafNode)tree).otherLeaves());
		    };
		return result;
			    
	    }; // switch (tree.kind())

	assert false;
	return null;

    } // treeToString(TreeNode tree,..)
	

    /** Compares two symbols; any variable is smaller than any
     *  nonvariable symbol.
     */
    private static boolean greater(Symbol sym1,Symbol sym2) {

	if (sym1.isVariable())
	    {
		if (sym2.isVariable()) 
		    return sym1.compareTo(sym2) > 0;
		return false;
	    }
	else if (sym2.isVariable())
	    {
		return true;
	    }
	else 
	    return sym1.compareTo(sym2) > 0;

    } // greater(Symbol sym1,Symbol sym2)





    //                     Data:

    private static final int NonvariableHashSize = 1000;
        
    private TreeNode[] _nonvariableHash;

    private HashMap<BitSet, TreeMap<Variable,LeafNode>> _variableIndex;

    /** Number of locks set by retrieval operation in progress;
     *  should be 0 when any maintenance is performed,
     *  otherwise an error exception is thrown.
     */
    private int _retrievalLocks;

}; // class ClusteredUnificationIndex<IndexedObject>

