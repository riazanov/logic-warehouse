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

/** Implements multiset-mode backward subsumption-oriented 
 *  subsumption tests on lists of literals (without indexing). 
 */
public class BackwardSubsumption {

    public BackwardSubsumption(Collection<? extends Literal> clause) {
	_testee = new Literal[32]; // initial size
	_testeeLitIsCaptured = new boolean[32]; // initial size
	_subsumer = new Literal[32]; // initial size
	_subsumerLitCaptures = new int[32]; // initial size
	_substSavepoint = new int[32]; // initial size
	_sizeOfTestee = 0;
	_sizeOfSubsumer = 0;
	setSubsumer(clause);
    }

    public BackwardSubsumption() {
	_testee = new Literal[32]; // initial size
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

    /** Prepares the clause for multiple attempts to subsume by it
     *  other clauses.
     */
    public 
	final 
	void setSubsumer(Collection<? extends Literal> clause) {


	assert !clause.isEmpty();

	clear();

	_sizeOfSubsumer = clause.size();
	if (_sizeOfSubsumer > _subsumer.length)
	    {
		_subsumer = new Literal[_sizeOfSubsumer];
		_subsumerLitCaptures = new int[_sizeOfSubsumer];
		_substSavepoint = new int[_sizeOfSubsumer];
	    };

	Iterator<? extends Literal> iter = clause.iterator();
	
	for (int i = 0; i < _sizeOfSubsumer; ++i)
	    {
		_subsumer[i] = iter.next();
	    };
    }
    

    /** Tries to subsume the given clause by the current subsumer 
     *  (set by the last call to {@link #setSubsumer}.
     */
    public final boolean subsume(Collection<? extends Literal> clause,
				 Substitution3 witnessSubst) {
	assert !clause.isEmpty();

	//System.out.println("B SUBSUME? " + clause);


	_sizeOfTestee = clause.size();

	if (_sizeOfTestee > _testee.length)
	    {
		_testee = new Literal[_sizeOfTestee];
		_testeeLitIsCaptured = new boolean[_sizeOfTestee];
	    };
	
	Iterator<? extends Literal> iter = clause.iterator();
	
	for (int i = 0; i < _sizeOfTestee; ++i)
	    {
		_testee[i] = iter.next();
		_testeeLitIsCaptured[i] = false;
	    };
	
	_subst = witnessSubst;

	
	for (int m = 0; m < _sizeOfSubsumer; ++m)
	    {
		Literal subsumerLit = _subsumer[m];
		
		_substSavepoint[m] = _subst.savepoint();

		boolean foundTesteeLit;

		do
		    {
			int n;
			
			for (n = 0; n < _sizeOfTestee; ++n)
			    if (!_testeeLitIsCaptured[n] &&
				subsumerLit.isNegative() ==
				_testee[n].isNegative() &&
				Matching3.match(subsumerLit.atom(),
						_testee[n].atom(),
						_subst))
				{
				    
				    //System.out.println("1MATCH! " + subsumerLit + " ==> " + _testee[n] + " /// " + _subst);
				    
				    _subsumerLitCaptures[m] = n;
				    _testeeLitIsCaptured[n] = true;
				    break;
				}; // if (!_testeeLitIsCaptured[n]] &&..
			
			if (n == _sizeOfTestee) 
			    {
				if (!redoPrevious(m)) return false;
				foundTesteeLit = false;
			    }
			else 
			    foundTesteeLit = true;
		    }
		while (!foundTesteeLit);

	    }; // for (int m = 0; m < _sizeOfSubsumer; ++m)

	assert subsumeModuloSubst3(clause);

	return true;
	
    } // subsume(Collection<? extends Literal> clause,..)




    /** <b>pre:</b> <code>!clause.isEmpty()</code> */
    public final boolean subsume(Collection<? extends Literal> clause) {
	
	assert !clause.isEmpty();

	Substitution3 subst = new Substitution3();
	
	boolean result = subsume(clause,subst);

	subst.uninstantiateAll();
	
	return result;
    }

    

    /** Multiset-mode backward subsumption-oriented subsumption test; 
     *  if successfull, adjucts the witness substitution.
     */
    public
	static
	final boolean subsumes(Collection<? extends Literal> clause1,
			       Collection<? extends Literal> clause2,
			       Substitution3 witnessSubst) 
    {
	
	assert !clause1.isEmpty();

	assert !clause2.isEmpty();

	BackwardSubsumption attempt = new BackwardSubsumption(clause1);

	return attempt.subsume(clause2,witnessSubst);

    } // subsumes(Collection<? extends Literal> clause1,..)


    /** Multiset-mode backward subsumption-oriented subsumption test. */
    public 
	static
	boolean subsumes(Collection<? extends Literal> clause1,
			 Collection<? extends Literal> clause2) 
    {
	
	assert !clause1.isEmpty();
	assert !clause2.isEmpty();

	Substitution3 subst = new Substitution3();
	
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
		    Matching3.match(lit.atom(),
				    _testee[testeeLitNum].atom(),
				    _subst))
		    {

			//System.out.println("2MATCH! " + lit + " ==> " + _testee[testeeLitNum] + " /// " + _subst);

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
			    Matching3.match(lit.atom(),
					    _testee[testeeLitNum].atom(),
					    _subst))
			    {

				//System.out.println("3MATCH! " + lit + " ==> " + _testee[testeeLitNum] + " /// " + _subst);

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


    /** Checks if the current subsumer image under global substitution 3
     *  propositionally subsumes <code>clause</clause>;
     *  can be used to check results of a successful subsumption;
     *  the subsumption is tested in the set mode.
     */
    private 
	boolean 
	subsumeModuloSubst3(Collection<? extends Literal> clause) {

	// Check that every subsumer literal has an equal
	// counterpart in the given clause:

	for (int n = 0; n < _sizeOfSubsumer; ++n)
	    {
		boolean subsumerLitMapped = false;

		for (Literal lit : clause)
		    {
			if (_subsumer[n].equalsModuloSubst3(lit))
			    {
				subsumerLitMapped = true;
				break;
			    };
		    }; // for (Literal lit : clause)

		if (!subsumerLitMapped) return false;
		
	    }; // for (int n = 0; n < _sizeOfSubsumer; ++n)
	
	return true; 

    } // subsumeModuloSubst3(Collection<? extends Literal> clause)
	

    // 
    //         Private data:
    //                 

    private Literal[] _testee;

    private boolean[] _testeeLitIsCaptured;
    
    private int _sizeOfTestee;

    private Literal[] _subsumer;

    private int[] _subsumerLitCaptures;
    
    private int[] _substSavepoint;

    private int _sizeOfSubsumer;


    private Substitution3 _subst;

} // class BackwardSubsumption