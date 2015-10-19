
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


/**
 * Represents a separate attempt to unify two terms, that can be
 * later reversed by cancelling the corresponding variable instantiations.
 */
public class UnificationCapsule {

    public UnificationCapsule() {
    }
    
    /** Tries to unify the terms; if succeeds, makes a savepoint in 
     *  <code>subst</code> and registers the corresponding variable 
     *  instantiation in <code>subst</code>, so that later they can be 
     *  undone by a call to {@link #reverse()}; if the unification attemt 
     *  fails, <code>subst</code> remains 
     *  unchanged and no variables get instantiated.
     *  <b>pre:</b> neither <code>term1</code>, nor <code>term2</code> 
     *  contains abstractions or quantifiers; they are either both formulas,
     *  or both individual-valued; <code>subst != null</code>
     *  <b>VERY IMPORTANT:</b> the terms are considered modulo the current
     *             global substitution 1, ie, if the unification process meets
     *             an instantiated variable, it deepens into the instance.
     */
    public final boolean unify(Flatterm term1,Flatterm term2,Substitution1 subst) {
	
	assert subst != null;

	_unifier = subst;
	_unifierSavepoint = _unifier.savepoint();
	
	if (Unification.unify(term1,term2,_unifier)) return true;
	
	_unifier = null;
	
	return false;
	
    } // unify(Flatterm term1,Flatterm term2,Substitution1 subst)


    /** Tries to unify the terms; if succeeds, makes a savepoint in 
     *  <code>subst</code> and registers the corresponding variable 
     *  instantiation in <code>subst</code>, so that later they can be 
     *  undone by a call to {@link #reverse()}; if the unification attemt 
     *  fails, <code>subst</code> remains 
     *  unchanged and no variables get instantiated.
     *  <b>pre:</b> neither <code>var.instance()</code>, 
     *  nor <code>term</code> contains abstractions or quantifiers;
     *  they are either both formulas, or both individual-valued.
     *  <b>VERY IMPORTANT:</b> the terms are considered modulo the current
     *             global substitution 1, ie, if the unification process meets
     *             an instantiated variable, it deepens into the instance.
     */
    private final boolean unify(Variable var,Flatterm term,Substitution1 subst) {

	_unifier = subst;
	_unifierSavepoint = _unifier.savepoint();
	
	if (Unification.unify(var,term,_unifier)) return true;

	_unifier = null;
	
	return false;

    } // unify(Variable var,Flatterm term,Substitution1 subst)




    /** Similar to <code>unify(term1,term2,subst)</code> except that only 
     *  variables from <code>term1</code> are allowed to get instantiated.
     *  <b>pre:</b> neither <code>term1</code>, nor <code>term2</code> 
     *  contains abstractions or quantifiers; they are either both formulas,
     *  or both individual-valued; they also do not contain common variables.
     *  <b>VERY IMPORTANT:>/b> the terms are considered modulo the current
     *             global substitution 1, ie, if the unification process meets
     *             an instantiated variable, it deepens into the instance.
     */
    public final boolean match(Flatterm term1,Flatterm term2,Substitution1 subst) {

	
	_unifier = subst;
	_unifierSavepoint = _unifier.savepoint();
	
	if (Matching1.match(term1,term2,_unifier)) return true;
	
	_unifier = null;
	
	return false;
	
    } // match(Flatterm term1,Flatterm term2,Substitution1 subst)




    /** Reverses the effect of the recent successful unification attempt
     *  by uninstantiating the corresponding variable; the corresponding
     *  substitution backtracks to the savepoint made just before
     *  the unification attempt.
     *  <b>pre:</b>  <code>this</code> must represent a recent successful 
     *  unification attempt that has not been reversed.
     */
    public final void reverse() {
	assert _unifier != null;
	_unifier.backtrackTo(_unifierSavepoint);
	_unifier = null;
    } 





    //                         Data:


    /** Nonnull only when the object represents a successful unification 
     *  attempt that has not been reversed.
     */
    private Substitution1 _unifier;

    private int _unifierSavepoint;


}; // class UnificationCapsule
