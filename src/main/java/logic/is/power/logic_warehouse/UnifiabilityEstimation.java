
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


import logic.is.power.cushion.*;


/** Instances of this class heuristically estimate the ability
 *  of a given term to unify with other terms; unifiability is 
 *  computed as follows: 
 *  (a) <code>unifiability(x) = 1</code>,
 *  if <code>x</code> is a free variable occuring uniquely;
 *  (b) <code>unifiability(x) = duplicateVarUnifiability</code>
 *  if <code>x</code> is a free variable occuring more than once;
 *  <code>0 < duplicateVarUnifiability <= 1</code>;
 *  (c) if <code>c</code> is a function, constant, nonnullary predicate or 
 *  propositional constant,
 *  <code>unifiability(c) = funcUnifiability</code>,
 *  <code>unifiability(c) = constUnifiability</code>,
 *  <code>unifiability(c) = predUnifiability</code> or
 *  <code>unifiability(c) = propConstUnifiability</code> correspondingly;
 *  note that
 *  <code>0 < funcUnifiability,constUnifiability,predUnifiability,propConstUnifiability <= 1</code>;
 *  (d) <code>unifiability(f(s)) = unifiability(f) * 
 *     (unifiability(s) + (1 - unifiability(s)) * deepeningCoeff)</code>,
 *  0 <= deepeningCoeff <= 1;
 *  (e) <code>unifiability((s,t)) = unifiability(s) * unifiability(t)</code>;
 *  currently we assume that unifiability estimation cannot
 *  be applied to terms containing quantifiers, abstractions 
 *  or connectives;
 *  terms are examined only downto the specified depth 
 *  <code>maxDepth</code>.
 */
public class UnifiabilityEstimation {

    
    public UnifiabilityEstimation(int maxDepth,
				  float deepeningCoeff,
				  float duplicateVarUnifiability,
				  float constUnifiability,
				  float funcUnifiability,
				  float propConstUnifiability,
				  float predUnifiability)
    {
	assert maxDepth >= 0;
	assert deepeningCoeff >= 0;
	assert deepeningCoeff <= 1;
	assert duplicateVarUnifiability > 0;
	assert duplicateVarUnifiability <= 1;
	assert constUnifiability > 0;
	assert constUnifiability <= 1;
	assert funcUnifiability > 0;
	assert funcUnifiability <= 1;
	assert propConstUnifiability > 0;
	assert propConstUnifiability <= 1;
	assert predUnifiability > 0;
	assert predUnifiability <= 1;

	_log = null;

	_maxDepth = maxDepth;
	
	_deepeningCoeff = deepeningCoeff;

	_duplicateVarUnifiability = duplicateVarUnifiability;
	_constUnifiability = constUnifiability;
	_funcUnifiability = funcUnifiability;
	_propConstUnifiability = propConstUnifiability;
	_predUnifiability = predUnifiability;

	_weightComputation = new ShallowWeightComputation(_maxDepth);
    } // UnifiabilityEstimation(int maxDepth,..)
    

    /** If <code>log != null</code>, it will receive the results of all
     *  computations.
     */
    public 
	final 
	void setLogging(SimpleReceiver<Pair<Term,Float>> log) {
	_log = log;
    }


    /** Returns the current statically accessible object (possibly null). */ 
    public static UnifiabilityEstimation current() { return _current; }

    /** Makes <code>ue</code> the current statically accessible object;
     *  <code>ue</code> can be null.
     */ 
    public static void makeCurrent(UnifiabilityEstimation ue) {
	_current = ue;
    }


    /** <b>pre:</b> <code>term</code> cannot contain quantifiers,
     *  abstractions or connectives.
     *  @return > 0
     */
    public final float estimateUnifiability(Term term) {

	if (term.isVariable()) return 1;

	// Temporary inefficient solution:

	WeightPolynomial weight =
	    _weightComputation.computeWeight(term);
     
	float result = estimateUnifiability(term,_maxDepth,weight);

	if (_log != null)
	    _log.receive(new Pair(term,new Float(result)));
	

	assert result > 0;

	return result;

    } // estimateUnifiability(Term term)
    



    
    //                Private methods:

    private float estimateUnifiability(Term term,
				       int depthLimit,
				       WeightPolynomial weight) {
	
	assert depthLimit >= 0;
	
	switch (term.kind())
	    {
	    case Term.Kind.Variable:
		{
		    int occurences = weight.coefficient((Variable)term);
		    assert occurences > 0;
		    //System.out.println(occurences + " OCCURS OF " + term +
		    //		       "  IN " + weight);
		    
		    if (occurences > 1) return _duplicateVarUnifiability;
		    return 1;
		}
	    case Term.Kind.CompoundTerm:   
		{
		    if (depthLimit == 0) return _funcUnifiability;
		    
		    float argUnif = 
			 estimateUnifiability(((CompoundTerm)term).argument(),
					      depthLimit - 1,
					      weight);

		    
		    
		    float result = 
			_funcUnifiability * 
			(argUnif + (1 - argUnif) * _deepeningCoeff);
		    
		    assert result >= 0;
		    assert result <= 1;
		    
		    if (result == 0) return Float.MIN_VALUE;
		    
		    return result;
		}
		

	    case Term.Kind.IndividualConstant:
		    return _constUnifiability;

	    case Term.Kind.AtomicFormula: 
		{
		    if (((AtomicFormula)term).argument() == null) 
			return _propConstUnifiability;

		    if (depthLimit == 0)
			return _predUnifiability;

		    float argUnif = 
			 estimateUnifiability(((AtomicFormula)term).argument(),
					      depthLimit - 1,
					      weight);

		    float result = 
			_predUnifiability * 
			(argUnif + (1 - argUnif) * _deepeningCoeff);
		    
		    assert result >= 0;
		    assert result <= 1;

		    
		    if (result == 0) return Float.MIN_VALUE;

		    return result;
		}


	    case Term.Kind.ConnectiveApplication:
		assert false;
		return 0;
	    case Term.Kind.QuantifierApplication:
		assert false;
		return 0;
	    case Term.Kind.AbstractionTerm: 
		assert false;
		return 0;
	    case Term.Kind.TermPair:
		{
		    // We don't change the depth here:
		    float result1 = 
			estimateUnifiability(((TermPair)term).first(),
					     depthLimit,
					     weight);
		    float result2 = 
			estimateUnifiability(((TermPair)term).second(),
					     depthLimit,
					     weight);
		    float result = result1 * result2;
		    assert result >= 0;
		    assert result <= 1;

		    if (result == 0) return Float.MIN_VALUE;

		    return result;
		}

	    default:
		assert false;
		return 0;
	    } // switch (term.kind())

    } // estimateUnifiability(Term term,int maxDepth,..)




    //                Data:

    private static UnifiabilityEstimation _current = null;

    private SimpleReceiver<Pair<Term,Float>> _log;

    private final int _maxDepth;
    private final float _deepeningCoeff;
    private final float _duplicateVarUnifiability;
    private final float _constUnifiability;
    private final float _funcUnifiability;
    private final float _propConstUnifiability;
    private final float _predUnifiability;


    private final ShallowWeightComputation _weightComputation;

} // class UnifiabilityEstimation