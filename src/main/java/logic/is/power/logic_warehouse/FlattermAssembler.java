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


import java.util.List;

import java.util.Iterator;


/**
 * Implements symbol-by-symbol assembling of flatterms;
 * also allows insertion (of copies) of whole terms.
 */
public class FlattermAssembler {

    public FlattermAssembler() {
    }

    /** Prepares the object for assembling a new flatterm. */
    public final void reset() {
	_assembledTerm = null;
	_last = null;
    }

    /** Finalises the assembling; after a call to <code>wrapUp()</code>
     *  the assembled term is completely linked and can be accessed
     *  via {@link #assembledTerm()}.
     *  <b>pre:</b> A whole term must have been submitted and nothing 
     *  else, so that complete linking is possible.
     */
    public final void wrapUp() {
	assert _assembledTerm != null;
	
	link(_assembledTerm);
	
	assert _assembledTerm.lastCell() == _last;
    }
      
    public final Flatterm assembledTerm() {
	assert _assembledTerm != null;
	assert _assembledTerm.lastCell() == _last;
	return _assembledTerm;
    }


    //            Low-level functionality:

    public final void pushVar(Variable var) {
	if (_last == null) 
	{
	    _assembledTerm = Flatterm.newVariableCell(var);
	    _last = _assembledTerm;
	}
	else
	{
	    _last.setNextCell(Flatterm.newVariableCell(var));
	    _last = _last.nextCell();
	    
	};
    } // pushVar(Variable var)
      
    public final void pushFunc(Function func) {
	if (_last == null) {
	    _assembledTerm = Flatterm.newCompoundTermCell(func);
	    _last = _assembledTerm;
	}
	else
	{
	    _last.setNextCell(Flatterm.newCompoundTermCell(func));
	    _last = _last.nextCell();
	};
    } // pushFunc(Function func)

    public final void pushConst(IndividualConstant c) {
	if (_last == null) {
	    _assembledTerm = Flatterm.newIndividualConstantCell(c);
	    _last = _assembledTerm;
	}
	else
	{
	    _last.setNextCell(Flatterm.newIndividualConstantCell(c));
	    _last = _last.nextCell();
	};
    } // pushConst(IndividualConstant c)

    public final void pushPred(Predicate pred) {
	if (_last == null) {
	    _assembledTerm = Flatterm.newAtomicFormulaCell(pred);
	    _last = _assembledTerm;
	}
	else
	{
	    _last.setNextCell(Flatterm.newAtomicFormulaCell(pred));
	    _last = _last.nextCell();
	};
	
    } // pushPred(Predicate pred)


    public final void pushConnective(Connective con) {
	if (_last == null) {
	    _assembledTerm = Flatterm.newConnectiveApplicationCell(con);
	    _last = _assembledTerm;
	}
	else
	{
	    _last.setNextCell(Flatterm.newConnectiveApplicationCell(con));
	    _last = _last.nextCell();
	};
    } // pushConnective(Connective con)


    public final void pushQuant(Quantifier quant) {
	if (_last == null) {
	    _assembledTerm = Flatterm.newQuantifierApplicationCell(quant);
	    _last = _assembledTerm;
	}
	else
	{
	    _last.setNextCell(Flatterm.newQuantifierApplicationCell(quant));
	    _last = _last.nextCell();
	};
    } // pushQuant(Quantifier quant)

      
    public final void pushAbstractionVar(Variable var) {
	if (_last == null) {
	    _assembledTerm = Flatterm.newAbstractionCell(var);
	    _last = _assembledTerm;
	}
	else
	{
	    _last.setNextCell(Flatterm.newAbstractionCell(var));
	    _last = _last.nextCell();
	};
    } // pushAbstractionVar(Variable var)


    public final void pushSymbol(Symbol sym) {

	switch (sym.category())
	    {
	    case Symbol.Category.Variable:
		pushVar((Variable)sym);
		return;

	    case Symbol.Category.Function:
		pushFunc((Function)sym);
		return;

	    case Symbol.Category.IndividualConstant:
		pushConst((IndividualConstant)sym);
		return;

	    case Symbol.Category.Predicate:
		pushPred((Predicate)sym);
		return;

	    case Symbol.Category.Connective:
		pushConnective((Connective)sym);
		return;

	    case Symbol.Category.Quantifier:
		pushQuant((Quantifier)sym);
		return;
		
	    }; // switch (sym.category())
	
	assert false;

    } // pushSymbol(Symbol sym)

      
    //        Pushing whole term/formula copies:

    /** <b>post:</b> {@link #wrapUp()} has not been called; do it explicitly
     *  if the term is all you want to assemble.
     */
    public final void pushCopyOf(Flatterm term) {

	switch (term.kind()) 
	{
	    case Term.Kind.Variable: 
		pushVar(term.variable()); 
		return;
	    
	    case Term.Kind.CompoundTerm:  
		pushFunc(term.function());
		for (Flatterm subterm = term.nextCell();
		     subterm != term.after();
		     subterm = subterm.after())
		    pushCopyOf(subterm);
		return;
		     
	    case Term.Kind.IndividualConstant: 
		pushConst(term.individualConstant());
		return;

	    case Term.Kind.AtomicFormula:
		pushPred(term.predicate());
		for (Flatterm subterm = term.nextCell();
		     subterm != term.after();
		     subterm = subterm.after())
		    pushCopyOf(subterm);
		return;
      
	    case Term.Kind.ConnectiveApplication:
		pushConnective(term.connective());
		for (Flatterm subform = term.nextCell();
		     subform != term.after();
		     subform = subform.after())
		    pushCopyOf(subform);
		return;
    

	    case Term.Kind.QuantifierApplication:
		pushQuant(term.quantifier());
		pushCopyOf(term.nextCell());
		return;


	    case Term.Kind.AbstractionTerm:
		pushAbstractionVar(term.variable());
		pushCopyOf(term.nextCell());
		return;

	}; // switch (term.kind())
    
	assert false;

    } // pushCopyOf(Flatterm term)

      
      /** <b>pre:</b> <code>Signature.current() != null</code>. 
       * <b>post:</b> {@link #wrapUp()} has not been called; do it explicitly
       *  if the term is all you want to assemble.
       */
    public final void pushInputTerm(InputSyntax.Term term,
			      InputVariableRenaming variableRenaming) {
	
	
	assert variableRenaming != null;


	if (term.isVariable())
	{
	    pushVar(variableRenaming.rename(term.variable()));
	}
	else // non-variable term
	{
	    InputSyntax.FunctionalSymbol func = term.functionalSymbol();

	    List<InputSyntax.Term> arguments = term.arguments();
		    
	    if (func.arity() == 0) // constant
	    {
		assert arguments == null || 
		    arguments.size() == 0;

		IndividualConstant c = 
		    Signature.current().representationForConstant(func.name());
		pushConst(c);
	    }
	    else // compound term
	    {

		assert arguments != null;
		assert arguments.size() == func.arity();
	    
		Function f = 
		    Signature.current().representationForFunction(func.name(),
								  func.arity());

		pushFunc(f);
	    
		for (InputSyntax.Term arg : arguments)
		    pushInputTerm(arg,variableRenaming);
	    };

	}; // if (func.arity() == 0) 

  
    } // pushInputTerm(InputSyntax.Term term,..)




    /** <b>pre:</b> <code>Signature.current() != null</code>;
     *  also, if the formula is all you want to collect, precede this 
     *  call by {@link #reset()};
     *  <b>post:</b> {@link #wrapUp()} has not been called; do it explicitly
     *  if the formula is all you want to assemble. 
     */
    public final void pushInputFormula(InputSyntax.Formula form,
				       InputVariableRenaming variableRenaming) {

	if (form.isQuantified())
	{
	    Quantifier nativeQuant = null;
	    switch (form.quantifier()) 
	    {
	    case InputSyntax.Quantifier.ForAll:
		nativeQuant = Quantifier.getForAll();
		break;
	    case InputSyntax.Quantifier.Exist:
		nativeQuant = Quantifier.getExist();
		break;
	    default:
		assert false;
		break;
	    };
      
	    pushQuant(nativeQuant);
      
	    assert form.quantifiedVariables() != null; 
	    assert form.quantifiedVariables().size() > 0;
	
	    for (InputSyntax.Variable var : form.quantifiedVariables())
		pushAbstractionVar(variableRenaming.rename(var));

	    pushInputFormula(form.matrix(),variableRenaming);

	}
	else if (form.isNegated()) {
      
	    pushConnective(Connective.getNot());
      
	    pushInputFormula(form.formulaUnderNegation(),variableRenaming);

	}
	else if (form.isBinary()) 
	{
	    Connective nativeConnective = null;
      
	    switch (form.binaryConnective())
	    {
		case InputSyntax.BinaryConnective.Equivalent:
		    nativeConnective = Connective.getEquivalent();
		    break;
		case InputSyntax.BinaryConnective.Implies:
		    nativeConnective = Connective.getImplies();
		    break;
		case InputSyntax.BinaryConnective.ReverseImplies:
		    nativeConnective = Connective.getReverseImplies();
		    break;
		case InputSyntax.BinaryConnective.NotEquivalent:
		    nativeConnective = Connective.getNotEquivalent();
		    break;
		case InputSyntax.BinaryConnective.NotOr:
		    nativeConnective = Connective.getNotOr();
		    break;
		case InputSyntax.BinaryConnective.NotAnd:
		    nativeConnective = Connective.getNotAnd();
		    break;
		default:
		    assert false;
		    break;
	    };
      
	    pushConnective(nativeConnective);

	    pushInputFormula(form.leftArgument(),variableRenaming);

	    pushInputFormula(form.rightArgument(),variableRenaming);

	}
	else if (form.isAssociative()) 
	{
	    Connective nativeConnective = null;
      
	    switch (form.associativeConnective())
	    {
		case InputSyntax.AssociativeConnective.And:
		    nativeConnective = Connective.getAnd();
		    break;
		case InputSyntax.AssociativeConnective.Or:
		    nativeConnective = Connective.getOr();
		    break;
		default:
		    assert false;
		    break;
	    };

	    List<InputSyntax.Formula> argumentFormulas = 
		form.connectiveArguments();

	    assert argumentFormulas != null;
	    assert argumentFormulas.size() > 1;

	    Iterator<InputSyntax.Formula> arg = 
		argumentFormulas.listIterator();

	    for (int n = 0; n < argumentFormulas.size(); ++n)
	    {
		if (n + 1 < argumentFormulas.size())
		    pushConnective(nativeConnective);
		pushInputFormula(argumentFormulas.get(n),variableRenaming);
	    };
            
	}
	else 
	{
	    assert form.isAtomic();
	
	    List<InputSyntax.Term> argumentTerms = form.predicateArguments();
	
	    InputSyntax.Predicate predicate = form.predicate();
	    
	
	    assert argumentTerms != null || predicate.arity() == 0;
	    assert argumentTerms == null || 
		predicate.arity() == argumentTerms.size();
	
	    Predicate pred = 
		Signature.current().representationForPredicate(predicate.name(),
							       predicate.arity(),
							       predicate.isInfix());		
	    pushPred(pred);
      
	    if (argumentTerms != null) 
	    {
		for (InputSyntax.Term arg : argumentTerms)
		    pushInputTerm(arg,variableRenaming);
	    };
      
	};
	   

    } // pushInputFormula(InputSyntax.Formula form,..)


    /** <b>pre:</b> <code>Signature.current() != null</code>;
     *  also, if the literal is all you want to collect, precede this 
     *  call by {@link #reset()};
     *  <b>post:</b> {@link #wrapUp()} has not been called; do it explicitly
     *  if the literal is all you want to assemble.
     */
    public final void pushInputLiteral(InputSyntax.Literal lit,
				 InputVariableRenaming variableRenaming) {
	

	Predicate pred = 
	    Signature.current().representationForPredicate(lit.predicate().name(),
							   lit.predicate().arity(),
							   lit.predicate().isInfix());					         
	if (!lit.isPositive()) 
	    pushConnective(Connective.getNot());

	pushPred(pred);

    
	List<InputSyntax.Term> arguments = lit.predicateArguments();

	if (arguments != null) 
	{
	    for (InputSyntax.Term arg : arguments)
		pushInputTerm(arg,variableRenaming);
	};


    } // pushInputLiteral(InputSyntax.Literal lit,..)




    /** Copies the term into the current position in the term
     *  being assembled; the copying is done modulo the variable
     *  renaming, which is extended on the fly.
     */
    public final void pushTerm(Term term,
			 VariableRenaming variableRenaming) {

	
	switch (term.kind())
	{
	    case Term.Kind.Variable: 
	    {
		Variable newVar = variableRenaming.rename((Variable)term);
		pushVar(newVar);
		return;
	    }

	    case Term.Kind.CompoundTerm: 
		pushFunc(((CompoundTerm)term).function());
		pushTerm(((CompoundTerm)term).argument(),variableRenaming);
		return;

	    case Term.Kind.IndividualConstant:
		pushConst((IndividualConstant)term);
		return;

	    case Term.Kind.AtomicFormula:
		pushPred(((AtomicFormula)term).predicate());
		if (((AtomicFormula)term).predicate().arity() != 0)
		    pushTerm(((AtomicFormula)term).argument(),
			     variableRenaming);
		return;

	    case Term.Kind.ConnectiveApplication:
		pushConnective(((ConnectiveApplication)term).connective());
		pushTerm(((ConnectiveApplication)term).argument(),
			 variableRenaming);
		return;

	    case Term.Kind.QuantifierApplication:
		pushQuant(((QuantifierApplication)term).quantifier());
		pushTerm(((QuantifierApplication)term).abstraction(),
			 variableRenaming);
		return;

	    case Term.Kind.AbstractionTerm:   
		pushAbstractionVar(variableRenaming.
				   rename(((AbstractionTerm)term).variable()));
		pushTerm(((AbstractionTerm)term).matrix(),variableRenaming);
		return;

	    case Term.Kind.TermPair: 
		pushTerm(((TermPair)term).first(),variableRenaming);
		pushTerm(((TermPair)term).second(),variableRenaming);
		return;

	} // switch (term.kind())
    
	assert false;

    } // pushTerm(Term term,..)



     
/** Copies the literal specified with the polarity and the atom
 *  (which is not necessarily an atomic formula) into
 *  the current position in the term being assembled;
 *  the copying is done modulo the variable renaming, which is 
 *  extended on the fly.
 *  <b>pre:</b> if the literal is all you want to collect, precede this 
 *  call by {@link #reset()};
 *  <b>post:</b> {@link #wrapUp()} has not been called; do it explicitly
 *  if the literal is all you want to assemble.
 */
    public final void pushLiteral(boolean positive,
			    Term atom,
			    VariableRenaming variableRenaming) {
	if (!positive)
	    pushConnective(Connective.getNot());

	pushTerm(atom,variableRenaming);

    } // pushLiteral(boolean positive,..)







    /** Copies the term into the current position in the term
     *  being assembled; the copying is done literally, ie, no variable
     *  renaming is applied.
     */
    public final void pushTerm(Term term) {

	
	switch (term.kind())
	{
	    case Term.Kind.Variable: 
	    {
		pushVar((Variable)term);
		return;
	    }

	    case Term.Kind.CompoundTerm: 
		pushFunc(((CompoundTerm)term).function());
		pushTerm(((CompoundTerm)term).argument());
		return;

	    case Term.Kind.IndividualConstant:
		pushConst((IndividualConstant)term);
		return;

	    case Term.Kind.AtomicFormula:
		pushPred(((AtomicFormula)term).predicate());
		if (((AtomicFormula)term).predicate().arity() != 0)
		    pushTerm(((AtomicFormula)term).argument());
		return;

	    case Term.Kind.ConnectiveApplication:
		pushConnective(((ConnectiveApplication)term).connective());
		pushTerm(((ConnectiveApplication)term).argument());
		return;

	    case Term.Kind.QuantifierApplication:
		pushQuant(((QuantifierApplication)term).quantifier());
		pushTerm(((QuantifierApplication)term).abstraction());
		return;

	    case Term.Kind.AbstractionTerm:   
		pushAbstractionVar(((AbstractionTerm)term).variable());
		pushTerm(((AbstractionTerm)term).matrix());
		return;

	    case Term.Kind.TermPair: 
		pushTerm(((TermPair)term).first());
		pushTerm(((TermPair)term).second());
		return;

	} // switch (term.kind())
    
	assert false;

    } // pushTerm(Term term)



     
    /** Copies the literal specified with the polarity and the atom
     *  (which is not necessarily an atomic formula) into
     *  the current position in the term being assembled;
     *  the copying is done literally, ie, no variable renaming is applied.
     *  <b>pre:</b> if the literal is all you want to collect, precede this 
     *  call by {@link #reset()};
     *  <b>post:</b> {@link #wrapUp()} has not been called; do it explicitly
     *  if the literal is all you want to assemble.
     */
    public final void pushLiteral(boolean positive,
			    Term atom) {
	if (!positive)
	    pushConnective(Connective.getNot());

	pushTerm(atom);

    } // pushLiteral(boolean positive,..)





/** Copies the specified term (which can be a formula, although 
 *  <em>it cannot contain quantifiers or abstraction</em>) into 
 *  the current position in the term 
 *  being assembled; the copying is done modulo the global 
 *  substitution, ie, all instantiated variables are replaced
 *  with the corresponding terms.
 *  <b>post:</b> {@link #wrapUp()} has not been called; do it explicitly
 *  if the term is all you want to assemble.
 */
    public final void pushTermWithGlobSubst(Flatterm term) {
   
	switch (term.kind())
	{
	    case Term.Kind.Variable: 
		if (term.variable().isInstantiated1())
		{
		    pushTermWithGlobSubst(term.variable().instance1());
		}
		else
		    pushVar(term.variable());
		return;

	    case Term.Kind.CompoundTerm:
		pushFunc(term.function());
		for (Flatterm arg = term.nextCell();
		     arg != term.after();
		     arg = arg.after())
		    pushTermWithGlobSubst(arg);
		return;

	    case Term.Kind.IndividualConstant: 
		pushConst(term.individualConstant());
		return;

	    case Term.Kind.AtomicFormula: 
		pushPred(term.predicate());
		for (Flatterm arg = term.nextCell();
		     arg != term.after();
		     arg = arg.after())
		    pushTermWithGlobSubst(arg);
		return;
      
	    case Term.Kind.ConnectiveApplication:
		pushConnective(term.connective());
		for (Flatterm arg = term.nextCell();
		     arg != term.after();
		     arg = arg.after())
		    pushTermWithGlobSubst(arg);
		return;

	}; // switch (term.kind())

	assert false;

    } // pushTermWithGlobSubst(Flatterm term)

    
    /** Copies the specified literal (which is not necessarily an 
     *  ordinary literal, although 
     *  <em>it cannot contain quantifiers or abstraction</em> 
     *  into the current position in the term 
     *  being assembled; the copying is done modulo the global 
     *  substitution, ie, all instantiated variables are replaced
     *  with the corresponding terms.
     *  <b>pre:</b> if the literal is all you want to collect, precede this 
     *  call by {@link #reset()};
     *  <b>post:</b> {@link #wrapUp()} has not been called; do it explicitly
     *  if the literal is all you want to assemble.
     */
    public final void pushLiteralWithGlobSubst(Flatterm lit) {
 
	pushTermWithGlobSubst(lit);

    }


    /** Copies the specified term (which can be a formula, although 
     *  <em>it cannot contain quantifiers or abstraction</em>) into 
     *  the current position in the term 
     *  being assembled; the copying is done modulo the global 
     *  substitution, ie, all instantiated variables are replaced
     *  with the corresponding terms.
     *  <b>pre:</b> <code>term != null</code>
     *  <b>post:</b> {@link #wrapUp()} has not been called; do it explicitly
     *  if the term is all you want to assemble.
     */
    public final void pushTermWithGlobSubst(Term term) {
	
	assert term != null;

	switch (term.kind()) 
	{
	case Term.Kind.Variable: 
		if (((Variable)term).isInstantiated1()) {
		    pushTermWithGlobSubst(((Variable)term).instance1());
		}
		else
		    pushVar((Variable)term);
		return;


	    case Term.Kind.CompoundTerm: 
		pushFunc(((CompoundTerm)term).function());
		pushTermWithGlobSubst(((CompoundTerm)term).argument());
		return;


	    case Term.Kind.IndividualConstant:
		pushConst((IndividualConstant)term);
		return;


	    case Term.Kind.AtomicFormula:      
		pushPred(((AtomicFormula)term).predicate());
		if (((AtomicFormula)term).argument() != null)
		    pushTermWithGlobSubst(((AtomicFormula)term).argument());
		return;


	    case Term.Kind.ConnectiveApplication:
		pushConnective(((ConnectiveApplication)term).connective());
		pushTermWithGlobSubst(((ConnectiveApplication)term).argument());
		return;

	    case Term.Kind.QuantifierApplication: // as below
	    case Term.Kind.AbstractionTerm:   
		assert false;
		return;


	    case Term.Kind.TermPair:     
		pushTermWithGlobSubst(((TermPair)term).first());
		pushTermWithGlobSubst(((TermPair)term).second());
		return;

	}; // switch (term.kind()) 

	assert false;

    } // pushTermWithGlobSubst(Term term)

    
    
    /** Copies the specified literal (which is not necessarily an 
     *  ordinary literal, although 
     *  <em>it cannot contain quantifiers or abstraction</em>) 
     *  into the current position in the term 
     *  being assembled; the copying is done modulo the global 
     *  substitution, ie, all instantiated variables are replaced
     *  with the corresponding terms.
     *  <b>pre:</b> if the literal is all you want to collect, precede this 
     *  call by {@link #reset()};
     *  <b>post:</b> {@link #wrapUp()} has not been called; do it explicitly
     *  if the literal is all you want to assemble.
     */
    public final void pushLiteralWithGlobSubst(boolean positive,Term atom) {

	if (!positive)
	    pushConnective(Connective.getNot());
	
	pushTermWithGlobSubst(atom);

	wrapUp();

    } // pushLiteralWithGlobSubst(boolean positive,Term atom)





/** Copies the specified term (which can be a formula, although 
 *  <em>it cannot contain quantifiers or abstraction</em>) into 
 *  the current position in the term 
 *  being assembled; the copying is done modulo the global 
 *  substitution, ie, all instantiated variables are replaced
 *  with the corresponding terms, and modulo the variable renaming,
 *  which is extended on the fly.
 *  <b>post:</b> {@link #wrapUp()} has not been called; do it explicitly
 *  if the term is all you want to assemble.
 */
    public final void pushTermWithGlobSubst(Flatterm term,
				      VariableRenaming variableRenaming) {
   
	switch (term.kind())
	{
	    case Term.Kind.Variable: 
		if (term.variable().isInstantiated1())
		{
		    pushTermWithGlobSubst(term.variable().instance1(),
					  variableRenaming);
		}
		else
		    pushVar(variableRenaming.rename(term.variable()));
		return;

	    case Term.Kind.CompoundTerm:
		pushFunc(term.function());
		for (Flatterm arg = term.nextCell();
		     arg != term.after();
		     arg = arg.after())
		    pushTermWithGlobSubst(arg,variableRenaming);
		return;

	    case Term.Kind.IndividualConstant: 
		pushConst(term.individualConstant());
		return;

	    case Term.Kind.AtomicFormula: 
		pushPred(term.predicate());
		for (Flatterm arg = term.nextCell();
		     arg != term.after();
		     arg = arg.after())
		    pushTermWithGlobSubst(arg,variableRenaming);
		return;
      
	    case Term.Kind.ConnectiveApplication:
		pushConnective(term.connective());
		for (Flatterm arg = term.nextCell();
		     arg != term.after();
		     arg = arg.after())
		    pushTermWithGlobSubst(arg,variableRenaming);
		return;

	}; // switch (term.kind())

	assert false;

    } // pushTermWithGlobSubst(Flatterm term,VariableRenaming variableRenaming)

    
    /** Copies the specified literal (which is not necessarily an 
     *  ordinary literal, although 
     *  <em>it cannot contain quantifiers or abstraction</em> 
     *  into the current position in the term 
     *  being assembled; the copying is done modulo the global 
     *  substitution, ie, all instantiated variables are replaced
     *  with the corresponding terms, and modulo the variable renaming,
     *  which is extended on the fly.
     *  <b>pre:</b> if the literal is all you want to collect, precede this 
     *  call by {@link #reset()};
     *  <b>post:</b> {@link #wrapUp()} has not been called; do it explicitly
     *  if the literal is all you want to assemble.
     */
    public final void pushLiteralWithGlobSubst(Flatterm lit,VariableRenaming variableRenaming) {
 
	pushTermWithGlobSubst(lit,variableRenaming);

    }


    /** Copies the specified term (which can be a formula, although 
     *  <em>it cannot contain quantifiers or abstraction</em>) into 
     *  the current position in the term 
     *  being assembled; the copying is done modulo the global 
     *  substitution, ie, all instantiated variables are replaced
     *  with the corresponding terms, and modulo the variable renaming,
     *  which is extended on the fly.
     *  <b>pre:</b> <code>term != null</code>
     *  <b>post:</b> {@link #wrapUp()} has not been called; do it explicitly
     *  if the term is all you want to assemble.
     */
    public final void pushTermWithGlobSubst(Term term,VariableRenaming variableRenaming) {
	
	assert term != null;

	switch (term.kind()) 
	{
	    case Term.Kind.Variable: 
		if (((Variable)term).isInstantiated1()) {
		    pushTermWithGlobSubst(((Variable)term).instance1());
		}
		else
		    pushVar(variableRenaming.rename((Variable)term));
		return;


	    case Term.Kind.CompoundTerm: 
		pushFunc(((CompoundTerm)term).function());
		pushTermWithGlobSubst(((CompoundTerm)term).argument(),variableRenaming);
		return;


	    case Term.Kind.IndividualConstant:
		pushConst((IndividualConstant)term);
		return;


	    case Term.Kind.AtomicFormula:      
		pushPred(((AtomicFormula)term).predicate());
		if (((AtomicFormula)term).argument() != null)
		    pushTermWithGlobSubst(((AtomicFormula)term).argument(),
					  variableRenaming);
		return;


	    case Term.Kind.ConnectiveApplication:
		pushConnective(((ConnectiveApplication)term).connective());
		pushTermWithGlobSubst(((ConnectiveApplication)term).argument(),
				      variableRenaming);
		return;

	    case Term.Kind.QuantifierApplication: // as below
	    case Term.Kind.AbstractionTerm:   
		assert false;
		return;


	    case Term.Kind.TermPair:     
		pushTermWithGlobSubst(((TermPair)term).first(),variableRenaming);
		pushTermWithGlobSubst(((TermPair)term).second(),variableRenaming);
		return;

	}; // switch (term.kind()) 

	assert false;

    } // pushTermWithGlobSubst(Term term,VariableRenaming variableRenaming)

    
    
    /** Copies the specified literal (which is not necessarily an 
     *  ordinary literal, although 
     *  <em>it cannot contain quantifiers or abstraction</em>) 
     *  into the current position in the term 
     *  being assembled; the copying is done modulo the global 
     *  substitution, ie, all instantiated variables are replaced
     *  with the corresponding terms, and modulo the variable renaming,
     *  which is extended on the fly.
     *  <b>pre:</b> if the literal is all you want to collect, precede this 
     *  call by {@link #reset()};
     *  <b>post:</b> {@link #wrapUp()} has not been called; do it explicitly
     *  if the literal is all you want to assemble.
     */
    public final void pushLiteralWithGlobSubst(boolean positive,
					 Term atom,
					 VariableRenaming variableRenaming) {

	if (!positive)
	    pushConnective(Connective.getNot());
	
	pushTermWithGlobSubst(atom,variableRenaming);

	wrapUp();

    } // pushLiteralWithGlobSubst(boolean positive,Term atom,VariableRenaming variableRenaming)







    /** Copies the specified term (which can be a formula, although 
     *  <em>it cannot contain quantifiers or abstraction</em>) into 
     *  the current position in the term 
     *  being assembled; the copying is done modulo the global 
     *  substitution 2, ie, all instantiated variables are replaced
     *  with the corresponding terms. Note that global substitution 2
     *  is not considered transitively.
     *  <b>pre:</b> <code>term != null</code>
     *  <b>post:</b> {@link #wrapUp()} has not been called; do it explicitly
     *  if the term is all you want to assemble.
     */
    public final void pushTermWithGlobSubst2(Term term) {
	
	assert term != null;

	switch (term.kind()) 
	{
	    case Term.Kind.Variable: 
		if (((Variable)term).isInstantiated2()) {
		    pushCopyOf(((Variable)term).instance2());
		}
		else
		    pushVar((Variable)term);
		return;


	    case Term.Kind.CompoundTerm: 
		pushFunc(((CompoundTerm)term).function());
		pushTermWithGlobSubst2(((CompoundTerm)term).argument());
		return;


	    case Term.Kind.IndividualConstant:
		pushConst((IndividualConstant)term);
		return;


	    case Term.Kind.AtomicFormula:      
		pushPred(((AtomicFormula)term).predicate());
		if (((AtomicFormula)term).argument() != null)
		    pushTermWithGlobSubst2(((AtomicFormula)term).argument());
		return;


	    case Term.Kind.ConnectiveApplication:
		pushConnective(((ConnectiveApplication)term).connective());
		pushTermWithGlobSubst2(((ConnectiveApplication)term).argument());
		return;

	    case Term.Kind.QuantifierApplication: // as below
	    case Term.Kind.AbstractionTerm:   
		assert false;
		return;


	    case Term.Kind.TermPair:     
		pushTermWithGlobSubst2(((TermPair)term).first());
		pushTermWithGlobSubst2(((TermPair)term).second());
		return;

	}; // switch (term.kind()) 

	assert false;

    } // pushTermWithGlobSubst2(Term term)





    /** Copies the specified term (which can be a formula, although 
     *  <em>it cannot contain quantifiers or abstraction</em>) into 
     *  the current position in the term 
     *  being assembled; the copying is done modulo the global 
     *  substitution 3, ie, all instantiated variables are replaced
     *  with the corresponding terms, and the specified variable renaming.
     *  Note that global substitution 3 is not considered transitively.
     *  <b>pre:</b> <code>term != null</code>
     *  <b>post:</b> {@link #wrapUp()} has not been called; do it explicitly
     *  if the term is all you want to assemble.
     */
    public final void pushTermWithGlobSubst3(Term term,
				       VariableRenaming variableRenaming) {
	
	assert term != null;

	switch (term.kind()) 
	{
	    case Term.Kind.Variable: 
		if (((Variable)term).isInstantiated3()) {
		    pushTerm(((Variable)term).instance3(),variableRenaming);
		}
		else
		    pushVar(variableRenaming.rename((Variable)term));
		return;


	    case Term.Kind.CompoundTerm: 
		pushFunc(((CompoundTerm)term).function());
		pushTermWithGlobSubst3(((CompoundTerm)term).argument(),
				       variableRenaming);
		return;


	    case Term.Kind.IndividualConstant:
		pushConst((IndividualConstant)term);
		return;


	    case Term.Kind.AtomicFormula:      
		pushPred(((AtomicFormula)term).predicate());
		if (((AtomicFormula)term).argument() != null)
		    pushTermWithGlobSubst3(((AtomicFormula)term).argument(),
					   variableRenaming);
		return;


	    case Term.Kind.ConnectiveApplication:
		pushConnective(((ConnectiveApplication)term).connective());
		pushTermWithGlobSubst3(((ConnectiveApplication)term).argument(),
				       variableRenaming);
		return;

	    case Term.Kind.QuantifierApplication: // as below
	    case Term.Kind.AbstractionTerm:   
		assert false;
		return;


	    case Term.Kind.TermPair:     
		pushTermWithGlobSubst3(((TermPair)term).first(),
				       variableRenaming);
		pushTermWithGlobSubst3(((TermPair)term).second(),
				       variableRenaming);
		return;

	}; // switch (term.kind()) 

	assert false;

    } // pushTermWithGlobSubst3(Term term,..)





    //                   Private methods:


    /** Sets last cell pointers in all cell of the whole term that starts
     *  with the specified pointer.
     */
    private void link(Flatterm term) {

	switch (term.kind()) 
	{
	    case Term.Kind.Variable:
		// nothing to do here, the term is alredy linked
		assert term.lastCell() == term;
		return;
	    
	    case Term.Kind.CompoundTerm:
	    {
		Flatterm arg = term.nextCell();
		Flatterm lastArg = null;
		for (int i = 0; i < term.function().arity(); ++i)
		{
		    assert arg != null;
		    link(arg);
		    if (i + 1 == term.function().arity())
			lastArg = arg;
		    arg = arg.after();
		};	

	        assert lastArg != null; 
		// because term.function().arity() > 0

		term.setLastCell(lastArg.lastCell());
		return;
	    }   
		     
	    case Term.Kind.IndividualConstant:
		// nothing to do here, the term is alredy linked
		assert term.lastCell() == term;
		return;

	    case Term.Kind.AtomicFormula:  
	    {        

		Flatterm arg = term.nextCell(); 
		Flatterm lastArg = null;
		for (int i = 0; i < term.predicate().arity(); ++i)
		{
		    assert arg != null; 
		    link(arg);

		    if (i + 1 == term.predicate().arity())
			lastArg = arg;
		    arg = arg.after();			
		};
	
		if (lastArg == null) {
		    // the term is just a propositional variable
		    term.setLastCell(term);
		}
		else
		    term.setLastCell(lastArg.lastCell());

		return;      
	    }
 
	    case Term.Kind.ConnectiveApplication:
	    {
		Flatterm arg = term.nextCell();
		Flatterm lastArg = null;

		for (int i = 0; i < term.connective().arity(); ++i)
		{
		    assert arg != null;
		    link(arg);

		    if (i + 1 == term.connective().arity())
			lastArg = arg;
		    arg = arg.after();
		};

		assert lastArg != null; 
		// because term.connective().arity() > 0
	
		term.setLastCell(lastArg.lastCell());

		return;
	    }   
    

	    case Term.Kind.QuantifierApplication:
		assert term.nextCell() != null;
		link(term.nextCell());
		term.setLastCell(term.nextCell().lastCell());
		return;


	    case Term.Kind.AbstractionTerm:
		assert term.nextCell() != null; 
		link(term.nextCell());
		term.setLastCell(term.nextCell().lastCell());
		return;
		
	}; // switch (term.kind())
	
	assert false;

    } // link(Flatterm term) 



    //                   Data:


    private Flatterm _assembledTerm;
    
    private Flatterm _last;
    
    
}; // class FlattermAssembler
