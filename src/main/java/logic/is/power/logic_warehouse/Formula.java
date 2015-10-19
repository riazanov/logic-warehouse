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

/** Common base for all classes representing boolean-valued terms. */
public abstract class Formula 
    extends Literal 
    implements Term 
{

    public abstract int kind();

    public final boolean isAtomic() {
	return kind() == Term.Kind.AtomicFormula;
    }

    public final boolean isVariable() { return false; }
        
    public final boolean isIndividualConstant() { return false; }
	
    public final boolean isIndividualValued() { return false; }
	    
    public final boolean isPair() { return false; }

    public final boolean isFormula() { return true; }

    public final boolean isAbstraction() { return false; }

    /** @return isAtomic() && atom().isEquality() */
    public boolean isEquality() { 
	return isAtomic() && atom().isEquality(); 
    }

    /** Throws an {@link java.lang.Error} exception when applied to an 
     *  {@link logic.is.power.logic_warehouse.AbstractionTerm} or 
     *  a {@link logic.is.power.logic_warehouse.TermPair}.
     */
    public abstract Symbol topSymbol();
	
	
    public abstract boolean isNegative();

    public abstract Formula atom();


    

     /** Simplest syntactic equality. */
    public abstract boolean equals(Flatterm flatterm);

    /** Collects all free variables from 
     *  this term in the collection <code>result<\code>; note that result 
     *  is not emptied before the collection, so that what was in it
     *  also remains in it.
     */
    public abstract void collectFreeVariables(Collection<Variable> result);

    /** @param closed indicates whether grouping is required */
    public abstract String toString(boolean closed);


} //  abstract class Formula