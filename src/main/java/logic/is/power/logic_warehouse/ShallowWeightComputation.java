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


/** Computation of 
 *  <a href="{@docRoot}/resources/glossary.html#shallow_weight">shallow weights</a> 
 *  of terms in various representations. Connective, quantifiers and
 *  abstractions are assumed to have weight 1. 
 *  Pair constructors are assumed to have weight 0.
 */


public class ShallowWeightComputation {

    /** Constructs an object that will compute shallow weight
     *  taking into consideration only symbols down to <code>maxDepth</code>,
     *  ie, all symbols below <code>maxDepth</code> will be ignored.
     *  <b>pre:</b> <code>maxDepth >= 0</code>
     */
    public ShallowWeightComputation(int maxDepth) {
	_maxDepth = maxDepth;
    }

    public final WeightPolynomial computeWeight(Term term) {
	WeightPolynomial result = new WeightPolynomial();
	addWeight(term,result);
	return result;
    }
    
    /** Adds the shallow weight of the term to <code>w</code>. */
    public final void addWeight(Term term,WeightPolynomial w) {
	addWeight(term,_maxDepth,w);
    } 

    
    public final WeightPolynomial computeWeight(Flatterm term) {
	WeightPolynomial result = new WeightPolynomial();
	addWeight(term,result);
	return result;
    }

    /** Adds the shallow weight of the term to <code>w</code>. */
    public final void addWeight(Flatterm term,WeightPolynomial w) {
	addWeight(term,_maxDepth,w);
    } 

    

    //                 Private methods:

    
    private void addWeight(Term term,int depthLimit,WeightPolynomial w) {
	
	assert depthLimit >= 0;

	switch (term.kind())
	    {
	    case Term.Kind.Variable:
		w.add((Variable)term);
		break;
	    case Term.Kind.CompoundTerm:       
		w.add(((CompoundTerm)term).function().weight());
		if (depthLimit > 0)
		    addWeight(((CompoundTerm)term).argument(),depthLimit - 1,w);
		break;
	    case Term.Kind.IndividualConstant:
		w.add(((IndividualConstant)term).weight());
		break;
	    case Term.Kind.AtomicFormula: 
		w.add(((AtomicFormula)term).predicate().weight());
		if (((AtomicFormula)term).argument() != null &&
		    depthLimit > 0)
		    addWeight(((AtomicFormula)term).argument(),
			      depthLimit - 1,
			      w);
		break;   
	    case Term.Kind.ConnectiveApplication:
		w.add(1);
		if (depthLimit > 0)
		    addWeight(((ConnectiveApplication)term).argument(),
			      depthLimit - 1,
			      w);
		break;
	    case Term.Kind.QuantifierApplication:
		w.add(1);
		if (depthLimit > 0)
		    addWeight(((QuantifierApplication)term).abstraction(),
			      depthLimit - 1,
			      w);
		break;   
	    case Term.Kind.AbstractionTerm: 
		w.add(1);
		if (depthLimit > 0)
		    addWeight(((AbstractionTerm)term).matrix(),
			      depthLimit - 1,
			      w);
		break;  
	    case Term.Kind.TermPair:
		// We don't change the depth here:
		addWeight(((TermPair)term).first(),depthLimit,w);
		addWeight(((TermPair)term).second(),depthLimit,w);
		break;  
	    default:
		assert false;
		return;
	    }; // switch (term.kind())
	
    } // addWeight(Term term,int depthLimit,WeightPolynomial w)

    
    private void addWeight(Flatterm term,int depthLimit,WeightPolynomial w) {

	assert depthLimit >= 0;

	switch (term.kind())
	    {
	    case Term.Kind.Variable:
		w.add(term.variable());
		break;
	    case Term.Kind.CompoundTerm:       
		w.add(term.function().weight());
		if (depthLimit > 0)
		    {
			for (Flatterm arg = term.nextCell();
			     arg != term.after();
			     arg = arg.after())
			    addWeight(arg,depthLimit - 1,w);

		    };
		break;
	    case Term.Kind.IndividualConstant:
		w.add(term.individualConstant().weight());
		break;
	    case Term.Kind.AtomicFormula: 
		w.add(term.predicate().weight());
		if (depthLimit > 0)
		    {
			for (Flatterm arg = term.nextCell();
			     arg != term.after();
			     arg = arg.after())
			    addWeight(arg,depthLimit - 1,w);

		    };
		break;   
	    case Term.Kind.ConnectiveApplication:
		w.add(1);
		if (depthLimit > 0)
		    {
			for (Flatterm arg = term.nextCell();
			     arg != term.after();
			     arg = arg.after())
			    addWeight(arg,depthLimit - 1,w);

		    };
		break;   
	    case Term.Kind.QuantifierApplication:
		w.add(1);
		if (depthLimit > 0)
		    addWeight(term.nextCell(),depthLimit - 1,w);
		break;   
	    case Term.Kind.AbstractionTerm: 
		w.add(1);
		if (depthLimit > 0)
		    addWeight(term.nextCell(),depthLimit - 1,w);
		break;  
	    default:
		assert false;
		return;
	    }; // switch (term.kind())	

    } // addWeight(Flatterm term,int depthLimit,WeightPolynomial w)


    //                    Data:



    private final int _maxDepth;

} // class ShallowWeightComputation 