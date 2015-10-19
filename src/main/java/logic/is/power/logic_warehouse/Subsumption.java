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

/** Multiset-mode subsumption on lists of literals. */
public class Subsumption {

    public Subsumption(Collection<? extends FlattermLiteral> clause) {
	_testee = new FlattermLiteral[32]; // initial size
	_testeeLitIsCaptured = new boolean[32]; // initial size
	_subsumer = new Literal[32]; // initial size
	_subsumerLitCaptures = new int[32]; // initial size
	_substSavepoint = new int[32]; // initial size
	_sizeOfTestee = 0;
	_sizeOfSubsumer = 0;
	setTestee(clause);
    }

    public Subsumption() {
	_testee = new FlattermLiteral[32]; // initial size
	_testeeLitIsCaptured = new boolean[32]; // initial size
	_subsumer = new Literal[32]; // initial size
	_subsumerLitCaptures = new int[32]; // initial size
	_substSavepoint = new int[32]; // initial size
	_sizeOfTestee = 0;
	_sizeOfSubsumer = 0;
    }
    
    /** Releases all pointers to external objects. */
    public final void clear() {
	while (_sizeOfTestee > 0)
	    {
		--_sizeOfTestee;
		_testee[_sizeOfTestee] = null;
	    };
	while (_sizeOfSubsumer > 0)
	    {
		--_sizeOfSubsumer;
		_subsumer[_sizeOfSubsumer] = null;
	    };
    } // clear() 

    /** Prepares the clause for multiple attempts to subsume it
     *  with other clauses.
     */
    public 
	final 
	void setTestee(Collection<? extends FlattermLiteral> clause) {

	assert !clause.isEmpty();

	clear();

	_sizeOfTestee = clause.size();
	if (_sizeOfTestee > _testee.length)
	    {
		_testee = new FlattermLiteral[_sizeOfTestee];
		_testeeLitIsCaptured = new boolean[_sizeOfTestee];
	    };

	Iterator<? extends FlattermLiteral> iter = clause.iterator();
	
	for (int i = 0; i < _sizeOfTestee; ++i)
	    {
		_testee[i] = iter.next();
		_testeeLitIsCaptured[i] = false;
	    };
    }
    

    public final boolean subsumeBy(Collection<? extends Literal> clause,
				   Substitution2 witnessSubst) {
	assert !clause.isEmpty();

	if (clause.size() > _subsumer.length)
	    {
		_subsumer = new Literal[clause.size()];
		_subsumerLitCaptures = new int[clause.size()];
		_substSavepoint = new int[clause.size()];
	    };
	_sizeOfSubsumer = 0;
	
	_subst = witnessSubst;

	for (Literal lit : clause)
	    {
		_substSavepoint[_sizeOfSubsumer] = _subst.savepoint();

		int i;

		do 
		    {
			i = 0;
			while (i < _sizeOfTestee &&
			       (_testeeLitIsCaptured[i] ||
				lit.isNegative() != _testee[i].isNegative() ||
				!Matching2.match(lit.atom(),
						 _testee[i].atom(),
						 _subst)))
			    ++i;
		    }		
		while (i == _sizeOfTestee &&
		       redoPrevious(_sizeOfSubsumer));

		
		if (i == _sizeOfTestee) 
		    {
			return false;
		    }
		else
		    {
			_subsumer[_sizeOfSubsumer] = lit;

			// Memorise the testee literal captured by this 
			// subsumer literal:
			_subsumerLitCaptures[_sizeOfSubsumer] = i;

			++_sizeOfSubsumer;

			// Label _testee[i] as captured:
			_testeeLitIsCaptured[i] = true;
		    };

	    }; // for (Literal lit : clause)

	return true;
	
    } // subsumeBy(Collection<? extends Literal> clause,..)


    /** <b>pre:</b> <code>!clause.isEmpty()</code> */
    public final boolean subsumeBy(Collection<? extends Literal> clause) {
	
	assert !clause.isEmpty();

	Substitution2 subst = new Substitution2();
	
	boolean result = subsumeBy(clause,subst);

	subst.uninstantiateAll();
	
	return result;
    }

    

    /** Multiset-mode forward subsumption-oriented subsumption test; 
     *  if successfull, adjucts the witness substitution.
     */
    public
	static
	final boolean subsumes(Collection<? extends Literal> clause1,
			       Collection<? extends FlattermLiteral> clause2,
			       Substitution2 witnessSubst) 
    {
	
	assert !clause1.isEmpty();

	assert !clause2.isEmpty();

	Subsumption attempt = new Subsumption(clause2);

	return attempt.subsumeBy(clause1,witnessSubst);

    } // subsumes(Collection<? extends Literal> clause1,..)


    /** Multiset-mode forward subsumption-oriented subsumption test; 
     *  if successfull, adjucts the witness substitution.
     */
    public 
	static
	boolean subsumes(Collection<? extends Literal> clause1,
			 Collection<? extends FlattermLiteral> clause2) 
    {
	
	assert !clause1.isEmpty();
	assert !clause2.isEmpty();

	Substitution2 subst = new Substitution2();
	
	boolean result = subsumes(clause1,clause2,subst);

	subst.uninstantiateAll();
	
	return result;

    } // subsumes(Collection<? extends Literal> clause1,..)



    // 
    //         Private methods:
    //                 

    private boolean redoPrevious(int top) {

	if (top == 0) return false;

	int n = top - 1;

	Literal lit = _subsumer[n];

	_subst.backtrackTo(_substSavepoint[n]);
	
	int testeeLitNum = _subsumerLitCaptures[n];
	
	_testeeLitIsCaptured[testeeLitNum] = false;

	++testeeLitNum;
	
	while (testeeLitNum < _sizeOfTestee)
	    {
		if (!_testeeLitIsCaptured[testeeLitNum] &&
		    lit.isNegative() == 
		    _testee[testeeLitNum].isNegative() &&
		    Matching2.match(lit.atom(),
				    _testee[testeeLitNum].atom(),
				    _subst))
		    {
			// _substSavepoint[n] does not change.
			// _subsumer[n] does not change.
			_subsumerLitCaptures[n] = testeeLitNum;
			_testeeLitIsCaptured[testeeLitNum] = true;
			return true;
		    };
		
		++testeeLitNum;

	    }; // while (testeeLitNum < _sizeOfTestee)

	
	// No more matches for lit, try to change some
	// previous matches:
	
	while (redoPrevious(top - 1))
	    {
		testeeLitNum = 0;
	
		while (testeeLitNum < _sizeOfTestee)
		    {
			if (!_testeeLitIsCaptured[testeeLitNum] &&
			    lit.isNegative() == 
			    _testee[testeeLitNum].isNegative() &&
			    Matching2.match(lit.atom(),
					    _testee[testeeLitNum].atom(),
					    _subst))
			    {
				// _substSavepoint[n] does not change.
				// _subsumer[n] does not change.
				_subsumerLitCaptures[n] = testeeLitNum;
				_testeeLitIsCaptured[testeeLitNum] = true;
				return true;
			    };
			
			++testeeLitNum;
			
		    }; // while (testeeLitNum < _sizeOfTestee)		
	    }; // while (redoPrevious(top - 1))

	return false;

    } // redoPrevious()



    // 
    //         Private data:
    //                 

    private FlattermLiteral[] _testee;

    private boolean[] _testeeLitIsCaptured;
    
    private int _sizeOfTestee;

    private Literal[] _subsumer;

    private int[] _subsumerLitCaptures;
    
    private int[] _substSavepoint;

    private int _sizeOfSubsumer;


    private Substitution2 _subst;

} // class Subsumption