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

/** Extension of {@link logic.is.power.logic_warehouse#ReductionOrdering#current()}
 *  to literals; has only one instance.
 */
public class AdmissibleLiteralOrdering implements LiteralOrdering {

    /** Behaviour depends on the value of
     *  {@link logic.is.power.logic_warehouse#ReductionOrdering#current()}.
     *  <b>pre:</b> <code>!lit1.isGeneral() && !lit2.isGeneral()</code> 
     */
    public 
	final
	int 
	compare(Literal lit1,Literal lit2) {
	assert !lit1.isGeneral();
	assert !lit2.isGeneral();

	if (lit1 == lit2) return ComparisonValue.Equivalent;

	if (lit1.isEquality())
	    {
		if (lit2.isEquality())
		    {
			return compareEqualities(lit1,lit2);
		    }
		else
		    // An equality literal is smaller that any 
		    // non-equality literal:  
		    return ComparisonValue.Smaller;
		    
	    }
	else if (lit2.isEquality())
	    {
		// A non-equality literal is greater that any equality:  
		return ComparisonValue.Greater;
	    }
	else // !lit1.isEquality() && !lit2.isEquality()
	    return compareNonEqualities(lit1,lit2);

    } // compare(Literal lit1,Literal lit2)
    

    /** Behaviour depends on the value of
     *  {@link logic.is.power.logic_warehouse#ReductionOrdering#current()}.
     *  <b>pre:</b> <code>!lit1.isGeneral() && !lit2.isGeneral()</code> 
     */
    public 
	final
	int 
	compare(FlattermLiteral lit1,FlattermLiteral lit2) {
	assert !lit1.isGeneral();
	assert !lit2.isGeneral();

	if (lit1 == lit2) return ComparisonValue.Equivalent;

	if (lit1.isEquality())
	    {
		if (lit2.isEquality())
		    {
			return compareEqualities(lit1,lit2);
		    }
		else
		    // An equality literal is smaller that any 
		    // non-equality literal:  
		    return ComparisonValue.Smaller;
		    
	    }
	else if (lit2.isEquality())
	    {
		// A non-equality literal is greater that any equality:  
		return ComparisonValue.Greater;
	    }
	else // !lit1.isEquality() && !lit2.isEquality()
	    return compareNonEqualities(lit1,lit2);

    } // compare(FlattermLiteral lit1,FlattermLiteral lit2)
    

    /** Returns the unique instance. */ 
    public static AdmissibleLiteralOrdering some() { return _some; }

    


    //                     Private methods:
    
    private AdmissibleLiteralOrdering() {}



    private 
	int 
	compareEqualities(Literal lit1,Literal lit2) {
	
	assert lit1.isEquality();
	assert lit2.isEquality();

	if (lit1.isPositive() == lit2.isPositive())
	    return compareEqualityAtoms(lit1.atom(),lit2.atom());

	if (lit1.isPositive())
	    return 
		FunctionComparisonValue.flip(compareNegEqWithPosEq(lit1.atom(),lit2.atom()));

	assert lit2.isPositive();
	return 
	    compareNegEqWithPosEq(lit1.atom(),lit2.atom());


    } // compareEqualities(Literal lit1,Literal lit2)
	




    private 
	int 
	compareNonEqualities(Literal lit1,Literal lit2) {
	

	assert !lit1.isEquality();
	assert !lit2.isEquality();

	int cmp = 
	    ReductionOrdering.current().compare(lit1.atom(),lit2.atom());

	if (cmp != ComparisonValue.Equivalent) return cmp;

	// Same atoms. Compare the polarities: 
	
	if (lit1.isPositive() == lit2.isPositive())
	    return ComparisonValue.Equivalent;

	return 
	    (lit1.isPositive())? 
	    ComparisonValue.Smaller 
	    :
	    ComparisonValue.Greater;
	    

    } // compareNonEqualities(Literal lit1,Literal lit2)
	





    private 
	int 
	compareEqualityAtoms(Formula atom1,Formula atom2) {
	
	// Note that we only assume transitivity of the reduction
	// ordering in a weak sense, so that 
	// s < t && t < u implies that s >= t is impossible, 
	// but does not guarantee s < u, i.e., s * u is possible.


	assert atom1.isAtomic();
	assert atom1.isEquality();
	assert atom2.isAtomic();
	assert atom2.isEquality();

	Term s1 = ((AtomicFormula)atom1).firstArg();
	Term s2 = ((AtomicFormula)atom1).secondArg();
	Term t1 = ((AtomicFormula)atom2).firstArg();
	Term t2 = ((AtomicFormula)atom2).secondArg();
	
	// Compare multisets {s1,s2} and {t1,t2}:

	
	int cmpS1T1;
	int cmpS1T2;
	int cmpS2T1;
	int cmpS2T2;
	
	int cmpS1S2;
	int cmpT1T2;
	

	cmpS1T1 =
	    ReductionOrdering.current().compare(s1,t1);

	switch (cmpS1T1) 
	    {
	    case ComparisonValue.Smaller: 
		// s1 < t1

		cmpS2T1 = ReductionOrdering.current().compare(s2,t1);
	    
		switch (cmpS2T1) 
		    {		
		    case  ComparisonValue.Smaller: 
			// s1 < t1, s2 < t1
			return ComparisonValue.Smaller;

		    case ComparisonValue.Equivalent: 
			// s1 < t1, s2 = t1
			assert 
			    ReductionOrdering.current().compare(s1,s2) == 
			    ComparisonValue.Smaller;

			cmpS2T2 = 
			    ReductionOrdering.current().compare(s2,t2);
		    
			switch (cmpS2T2) 
			    {
			    case ComparisonValue.Smaller: 
				// s1 < t1, s2 = t1, s2 < t2
				return ComparisonValue.Smaller;
			    
			    case ComparisonValue.Equivalent: 
				// s1 < t1, s2 = t1, s2 = t2
				return ComparisonValue.Smaller;

			    case ComparisonValue.Greater: 
				// s1 < t1, s2 = t1, s2 > t2
			    
				return 
				    ReductionOrdering.current().compare(s1,t2);

			    case ComparisonValue.Incomparable: 
				// s1 < t1, s2 = t1, s2 * t2
				cmpS1T2 = 
				    ReductionOrdering.current().compare(s1,t2);
			    
				assert cmpS1T2 != ComparisonValue.Equivalent;
			    
				return cmpS1T2;

			    }; // switch (cmpS2T2) 
		    
			break; // case Equivalent


		    case ComparisonValue.Greater: 
			// s1 < t1, s2 > t1

			cmpS2T2 = 
			    ReductionOrdering.current().compare(s2,t2);

			switch (cmpS2T2)
			    {
      
			    case ComparisonValue.Smaller: 
				// s1 < t1, s2 > t1, s2 < t2
				return ComparisonValue.Smaller;

			    case ComparisonValue.Equivalent: 
				// s1 < t1, s2 > t1, s2 = t2

				assert 
				    ReductionOrdering.current().compare(t2,t1) == 
				    ComparisonValue.Greater;
			    
				return ComparisonValue.Smaller;

			    case ComparisonValue.Greater: 
				// s1 < t1, s2 > t1, s2 > t2
				return ComparisonValue.Greater;

			    case ComparisonValue.Incomparable: 
				// s1 < t1, s2 > t1, s2 * t2
				return ComparisonValue.Incomparable;

			    }; // switch (cmpS2T2)
		    
			break; // case Greater
		    


		    case ComparisonValue.Incomparable: 
			// s1 < t1, s2 * t1

			cmpS2T2 = 
			    ReductionOrdering.current().compare(s2,t2);

		    
			switch (cmpS2T2)
			    {
      
			    case ComparisonValue.Smaller: 
				// s1 < t1, s2 * t1, s2 < t2
				return ComparisonValue.Smaller;

			    case ComparisonValue.Equivalent: 
				// s1 < t1, s2 * t1, s2 = t2 
				return ComparisonValue.Smaller;

			    case ComparisonValue.Greater: 
				// s1 < t1, s2 * t1, s2 > t2
				cmpS1S2 = 
				    ReductionOrdering.current().compare(s1,s2);
				
				switch (cmpS1S2)
				    {
				    case ComparisonValue.Smaller: 
					// s1 < t1, s2 * t1, s2 > t2, s1 < s2

					cmpT1T2 = 
					    ReductionOrdering.current().compare(t1,t2);

					assert cmpT1T2 != ComparisonValue.Equivalent;

					return 
					    (cmpT1T2 == ComparisonValue.Smaller)? 
					    ComparisonValue.Greater 
					    : 
					    ComparisonValue.Incomparable; 

				    case ComparisonValue.Equivalent: 
					// s1 < t1, s2 * t1, s2 > t2, s1 = s2
					assert false;
					return ComparisonValue.Incomparable;

				    case ComparisonValue.Greater: 
					// s1 < t1, s2 * t1, s2 > t2, s1 > s2
					return ComparisonValue.Smaller;

				    case ComparisonValue.Incomparable: 
					// s1 < t1, s2 * t1, s2 > t2, s1 * s2
					cmpT1T2 = 
					    ReductionOrdering.current().compare(t1,t2);
				    
					assert cmpT1T2 != ComparisonValue.Equivalent;
				    
					return 
					    (cmpT1T2 == ComparisonValue.Smaller)? 
					    ComparisonValue.Greater 
					    : 
					    ComparisonValue.Incomparable; 
				    
				    }; // switch (cmpS1S2)

				break; // case Greater

			    case ComparisonValue.Incomparable: 
				// s1 < t1, s2 * t1, s2 * t2
				return ComparisonValue.Incomparable;

			    }; // switch (cmpS2T2)
		    
			break; // case Incomparable

		    }; // switch (cmpS2T1)
	    
		break; // case Smaller
	    


	    case ComparisonValue.Equivalent: // s1 = t1

		cmpS2T2 = ReductionOrdering.current().compare(s2,t2);

		switch (cmpS2T2)
		    {
		    case ComparisonValue.Smaller: 
			// s1 = t1, s2 < t2
			return ComparisonValue.Smaller;

		    case ComparisonValue.Equivalent: 
			// s1 = t1, s2 = t2
			return ComparisonValue.Equivalent;

		    case ComparisonValue.Greater: 
			// s1 = t1, s2 > t2
			return ComparisonValue.Greater;

		    case ComparisonValue.Incomparable: 
			// s1 = t1, s2 * t2
			cmpS1T2 = ReductionOrdering.current().compare(s1,t2);
	    
			switch (cmpS1T2)
			    {
			    case ComparisonValue.Smaller: 
				// s1 = t1, s2 * t2, s1 < t2

				cmpS1S2 = 
				    ReductionOrdering.current().compare(s1,s2);
			    
				assert cmpS1S2 != ComparisonValue.Equivalent;
			    
				return 
				    (cmpS1S2 == ComparisonValue.Greater)? 
				    ComparisonValue.Smaller 
				    : 
				    ComparisonValue.Incomparable;

			    
			    case ComparisonValue.Equivalent: 
				// s1 = t1, s2 * t2, s1 = t2

				cmpS1S2 = 
				    ReductionOrdering.current().compare(s1,s2);

				assert cmpS1S2 != ComparisonValue.Equivalent;

				return ComparisonValue.flip(cmpS1S2);


			    case ComparisonValue.Greater: 
				// s1 = t1, s2 * t2, s1 > t2

				cmpS1S2 = 
				    ReductionOrdering.current().compare(s1,s2);
			    
				assert cmpS1S2 != ComparisonValue.Equivalent;
			    
				return 
				    (cmpS1S2 == ComparisonValue.Smaller)? 
				    ComparisonValue.Greater 
				    : 
				    ComparisonValue.Incomparable;


			    case ComparisonValue.Incomparable: 
				// s1 = t1, s2 * t2, s1 * t2
				return ComparisonValue.Incomparable;

			    }; // switch (cmpS1T2)
	    	    
			break; // case Incomparable

		    }; // switch (cmpS2T2)
	    
		break; // case Equivalent



	    case ComparisonValue.Greater: // s1 > t1
	    
		cmpS1T2 = ReductionOrdering.current().compare(s1,t2);
	    
		switch (cmpS1T2)
		    {
		    case ComparisonValue.Smaller: 
			// s1 > t1, s1 < t2
			cmpS2T2 = ReductionOrdering.current().compare(s2,t2);
		    
			switch (cmpS2T2)
			    {
			    case ComparisonValue.Smaller: 
				// s1 > t1, s1 < t2, s2 < t2
				return ComparisonValue.Smaller;

			    case ComparisonValue.Equivalent: 
				// s1 > t1, s1 < t2, s2 = t2
				return ComparisonValue.Greater;

			    case ComparisonValue.Greater: 
				// s1 > t1, s1 < t2, s2 > t2
				return ComparisonValue.Greater;

			    case ComparisonValue.Incomparable: 
				// s1 > t1, s1 < t2, s2 * t2
			    
				cmpS1S2 = 
				    ReductionOrdering.current().compare(s1,s2);
			    
				assert cmpS1S2 != ComparisonValue.Equivalent;

				return 
				    (cmpS1S2 == ComparisonValue.Greater)? 
				    ComparisonValue.Smaller 
				    : 
				    ComparisonValue.Incomparable;

			    }; // switch (cmpS2T2)
		    
			break; // case Smaller


		    case ComparisonValue.Equivalent: 
			// s1 > t1, s1 = t2
		    
			cmpS2T2 = ReductionOrdering.current().compare(s2,t2);

			switch (cmpS2T2)
			    {
			    case ComparisonValue.Smaller: 
				// s1 > t1, s1 = t2, s2 < t2
				return ReductionOrdering.current().compare(s2,t1);

			    case ComparisonValue.Equivalent: 
				// s1 > t1, s1 = t2, s2 = t2
				return ComparisonValue.Greater;

			    case ComparisonValue.Greater: 
				// s1 > t1, s1 = t2, s2 > t2
				return ComparisonValue.Greater;

			    case ComparisonValue.Incomparable: 
				// s1 > t1, s1 = t2, s2 * t2
			    
				cmpS2T1 = 
				    ReductionOrdering.current().compare(s2,t1);

				assert cmpS2T1 != ComparisonValue.Equivalent;
			    
				return 
				    (cmpS2T1 == ComparisonValue.Smaller)? 
				    ComparisonValue.Smaller 
				    : 
				    ComparisonValue.Incomparable;

			    }; // switch (cmpS2T2)
		    
			break; // case Equivalent


		    case ComparisonValue.Greater: 
			// s1 > t1, s1 > t2
			return ComparisonValue.Greater;
		    

		    case ComparisonValue.Incomparable: 
			// s1 > t1, s1 * t2

			cmpS2T2 = ReductionOrdering.current().compare(s2,t2);
		    

			switch (cmpS2T2)
			    {
			    case ComparisonValue.Smaller: 
				// s1 > t1, s1 * t2, s2 < t2

				cmpS1S2 = 
				    ReductionOrdering.current().compare(s1,s2);

				assert cmpS1S2 != ComparisonValue.Equivalent;
			    
				return
				    (cmpS1S2 == ComparisonValue.Smaller)? 
				    ComparisonValue.Greater 
				    : 
				    ComparisonValue.Incomparable;

			    case ComparisonValue.Equivalent: 
				// s1 > t1, s1 * t2, s2 = t2 
				return ComparisonValue.Greater;

			    case ComparisonValue.Greater: 
				// s1 > t1, s1 * t2, s2 > t2
				return ComparisonValue.Greater;

			    case ComparisonValue.Incomparable: 
				// s1 > t1, s1 * t2, s2 * t2
				return ComparisonValue.Incomparable;

			    }; // switch (cmpS2T2)
		    
			break; // case Incomparable

		    }; // switch (cmpS1T2)

		break; // case Greater


	    case ComparisonValue.Incomparable: 
		// s1 * t1
	    
		cmpS1T2 = 
		    ReductionOrdering.current().compare(s1,t2);

		switch (cmpS1T2)
		    {
		    case ComparisonValue.Smaller: 
			// s1 * t1, s1 < t2

			cmpS2T2 = 
			    ReductionOrdering.current().compare(s2,t2);
		    
			switch (cmpS2T2)
			    {
			    case ComparisonValue.Smaller: 
				// s1 * t1, s1 < t2, s2 < t2
				return ComparisonValue.Smaller;

			    case ComparisonValue.Equivalent: 
				// s1 * t1, s1 < t2, s2 = t2
			    
				cmpS2T1 = 
				    ReductionOrdering.current().compare(s2,t1);
			    
				assert cmpS2T1 != ComparisonValue.Equivalent;
			    
				return 
				    (cmpS2T1 == ComparisonValue.Smaller)? 
				    ComparisonValue.Smaller 
				    : 
				    ComparisonValue.Incomparable;

			    case ComparisonValue.Greater: 
				// s1 * t1, s1 < t2, s2 > t2
			    
				cmpS2T1 = 
				    ReductionOrdering.current().compare(s2,t1);
			    
				return 
				    (cmpS2T1 == ComparisonValue.Equivalent)? 
				    ComparisonValue.Smaller 
				    : 
				    cmpS2T1;

			    case ComparisonValue.Incomparable: 
				// s1 * t1, s1 < t2, s2 * t2

				cmpS2T1 = 
				    ReductionOrdering.current().compare(s2,t1);

			    
				switch (cmpS2T1)
				    {

				    case ComparisonValue.Smaller: 
					// s1 * t1, s1 < t2, s2 * t2, s2 < t1
					return ComparisonValue.Smaller;

				    case ComparisonValue.Equivalent:  
					// s1 * t1, s1 < t2, s2 * t2, s2 = t1
					return ComparisonValue.Smaller;
				    
				    case ComparisonValue.Greater: 
					// s1 * t1, s1 < t2, s2 * t2, s2 > t1

					cmpS1S2 = 
					    ReductionOrdering.current().compare(s1,s2);
				    
					assert cmpS1S2 != ComparisonValue.Equivalent;
				    
					return 
					    (cmpS1S2 == ComparisonValue.Greater)? 
					    ComparisonValue.Smaller 
					    : 
					    ComparisonValue.Incomparable;


				    case ComparisonValue.Incomparable: 
					// s1 * t1, s1 < t2, s2 * t2, s2 * t1

					return ComparisonValue.Incomparable;

				    }; // switch (cmpS2T1)
			    
				break; // case Incomparable

			    }; // switch (cmpS1T2)
		    
			break; // case Smaller



		    case ComparisonValue.Equivalent: 
			// s1 * t1, s1 = t2
			cmpS2T1 = 
			    ReductionOrdering.current().compare(s2,t1);
		    
			return cmpS2T1;


		    case ComparisonValue.Greater: 
			// s1 * t1, s1 > t2
			cmpS2T1 = 
			    ReductionOrdering.current().compare(s2,t1);
		    
			switch (cmpS2T1)
			    {
			    case ComparisonValue.Smaller: 
				// s1 * t1, s1 > t2, s2 < t1 
				cmpS1S2 = 
				    ReductionOrdering.current().compare(s1,s2);
			    
				assert cmpS1S2 != ComparisonValue.Equivalent;

				return 
				    (cmpS1S2 == ComparisonValue.Smaller)? 
				    ComparisonValue.Smaller 
				    : 
				    ComparisonValue.Incomparable;

			    case ComparisonValue.Equivalent: 
				// s1 * t1, s1 > t2, s2 = t1 
				return ComparisonValue.Greater;

			    case ComparisonValue.Greater: 
				// s1 * t1, s1 > t2, s2 > t1 
				return ComparisonValue.Greater;

			    case ComparisonValue.Incomparable: 
				// s1 * t1, s1 > t2, s2 * t1 
				cmpT1T2 = 
				    ReductionOrdering.current().compare(t1,t2);
			    
				assert cmpT1T2 != ComparisonValue.Equivalent;
			    
				return 
				    (cmpT1T2 == ComparisonValue.Smaller)? 
				    ComparisonValue.Greater 
				    : 
				    ComparisonValue.Incomparable;

			    }; // switch (cmpS2T1)

			break; // case Greater
		    


		    case ComparisonValue.Incomparable: 
			// s1 * t1, s1 * t2
			cmpS2T1 = 
			    ReductionOrdering.current().compare(s2,t1);
			    
			switch (cmpS2T1)
			    {
			    case ComparisonValue.Smaller: 
				// s1 * t1, s1 * t2, s2 < t1
				cmpS1S2 = 
				    ReductionOrdering.current().compare(s1,s2);
			    
				assert cmpS1S2 != ComparisonValue.Equivalent;

				return 
				    (cmpS1S2 == ComparisonValue.Smaller)? 
				    ComparisonValue.Smaller 
				    : 
				    ComparisonValue.Incomparable;

			    case ComparisonValue.Equivalent: 
				// s1 * t1, s1 * t2, s2 = t1
				return ComparisonValue.Incomparable;

			    case ComparisonValue.Greater: 
				// s1 * t1, s1 * t2, s2 > t1
				cmpS2T2 = 
				    ReductionOrdering.current().compare(s2,t2);
			    
				switch (cmpS2T2)
				    {
				
				    case ComparisonValue.Smaller: 
					// s1 * t1, s1 * t2, s2 > t1, s2 < t2
					cmpS1S2 = 
					    ReductionOrdering.current().compare(s1,s2);
				    
					assert cmpS1S2 != ComparisonValue.Equivalent;

					return 
					    (cmpS1S2 == ComparisonValue.Smaller)? 
					    ComparisonValue.Smaller 
					    : 
					    ComparisonValue.Incomparable;

				    case ComparisonValue.Equivalent: 
					// s1 * t1, s1 * t2, s2 > t1, s2 = t2
					return ComparisonValue.Incomparable;

				    case ComparisonValue.Greater: 
					// s1 * t1, s1 * t2, s2 > t1, s2 < t2
					return ComparisonValue.Greater;

				    case ComparisonValue.Incomparable: 
					// s1 * t1, s1 * t2, s2 > t1, s2 < t2
					return ComparisonValue.Incomparable;

				    }; // switch (cmpS2T2)
			    
				break; // case Greater

 
			    case ComparisonValue.Incomparable: 
				// s1 * t1, s1 * t2, s2 * t1
				cmpS2T2 = 
				    ReductionOrdering.current().compare(s2,t2);

				switch (cmpS2T2) 
				    {
				    case ComparisonValue.Smaller: 
					// s1 * t1, s1 * t2, s2 * t1, s2 < t2
					cmpS1S2 = 
					    ReductionOrdering.current().compare(s1,s2);
				    
					assert cmpS1S2 != ComparisonValue.Equivalent;

					return 
					    (cmpS1S2 == ComparisonValue.Smaller)? 
					    ComparisonValue.Smaller 
					    : 
					    ComparisonValue.Incomparable;
				    
				    case ComparisonValue.Equivalent: 
					// s1 * t1, s1 * t2, s2 * t1, s2 = t2
					return ComparisonValue.Incomparable;

				    case ComparisonValue.Greater: 
					// s1 * t1, s1 * t2, s2 * t1, s2 > t2
					cmpT1T2 = 
					    ReductionOrdering.current().compare(t1,t2);
				    
					assert cmpT1T2 != ComparisonValue.Equivalent;

					return 
					    (cmpT1T2 == ComparisonValue.Smaller)? 
					    ComparisonValue.Greater 
					    : 
					    ComparisonValue.Incomparable;

				    case ComparisonValue.Incomparable: 
					// s1 * t1, s1 * t2, s2 * t1, s2 * t2
					return ComparisonValue.Incomparable;

				    }; // switch (cmpS2T2)

				break; // case Incomparable

			    }; // switch (cmpS2T1)

			break; // case Incomparable

		    }; // switch (cmpS1T2)
	    
		break; // case Incomparable

	    }; // switch (cmpS1T1)

 
	assert false;
	
	return ComparisonValue.Incomparable;


    } // compareEqualityAtoms(Formula atom1,Formula atom2)
	




    

    private 
	int 
	compareNegEqWithPosEq(Formula atom1,Formula atom2) {
	

	// Note that we only assume transitivity of the reduction
	// ordering in a weak sense, so that 
	// s < t && t < u implies that s >= t is impossible, 
	// but does not guarantee s < u, i.e., s * u is possible.

	assert atom1.isAtomic();
	assert atom1.isEquality();
	assert atom2.isAtomic();
	assert atom2.isEquality();

	Term s1 = ((AtomicFormula)atom1).firstArg();
	Term s2 = ((AtomicFormula)atom1).secondArg();
	Term t1 = ((AtomicFormula)atom2).firstArg();
	Term t2 = ((AtomicFormula)atom2).secondArg();
	
	

	// Compare multisets {s1,s1,s2,s2} (from s1 != s2) and 
	// {t1,t2} (from t1 == t2).

	int cmpS1T1;
	int cmpS1T2;
	int cmpS2T1;
	int cmpS2T2;
	int cmpS1S2;
	int cmpT1T2;
	
	cmpS1T1 = ReductionOrdering.current().compare(s1,t1);


	switch (cmpS1T1) 
	    {
	    case ComparisonValue.Smaller: 
		// s1 < t1

		cmpS2T1 = ReductionOrdering.current().compare(s2,t1);
	    
		switch (cmpS2T1) 
		    {		
		    case ComparisonValue.Smaller: 
			// s1 < t1, s2 < t1
			return ComparisonValue.Smaller;

		    case ComparisonValue.Equivalent: 
			// s1 < t1, s2 = t1
			assert 
			    ReductionOrdering.current().compare(s1,s2) == 
			    ComparisonValue.Smaller;
			cmpS2T2 = ReductionOrdering.current().compare(s2,t2);
		    
			switch (cmpS2T2) 
			    {
			    case ComparisonValue.Smaller: 
				// s1 < t1, s2 = t1, s2 < t2
				return ComparisonValue.Smaller;
			    
			    case ComparisonValue.Equivalent: 
				// s1 < t1, s2 = t1, s2 = t2
				return ComparisonValue.Greater; 
				// Because {s1,s1,s2,s2} > {s2,s2} = {t1,t2}
				
			    case ComparisonValue.Greater: 
				// s1 < t1, s2 = t1, s2 > t2
				return ComparisonValue.Greater;  
				// Because {s1,s1,s2,s2} > {s2,s2} > {s2,t2} = {t1,t2}

			    case ComparisonValue.Incomparable: 
				// s1 < t1, s2 = t1, s2 * t2
				cmpS1T2 = ReductionOrdering.current().compare(s1,t2);
				assert cmpS1T2 != ComparisonValue.Equivalent;
				return cmpS1T2;

			    }; // switch (cmpS2T2) 
		    
			break; // case ComparisonValue.Equivalent


		    case ComparisonValue.Greater: 
			// s1 < t1, s2 > t1
			cmpS2T2 = ReductionOrdering.current().compare(s2,t2);

			switch (cmpS2T2)
			    {
      
			    case ComparisonValue.Smaller: 
				// s1 < t1, s2 > t1, s2 < t2
				return ComparisonValue.Smaller;

			    case ComparisonValue.Equivalent: 
				// s1 < t1, s2 > t1, s2 = t2
				assert 
				    ReductionOrdering.current().compare(t2,t1) == 
				    ComparisonValue.Greater;
			    
				return ComparisonValue.Greater; 
				// Because {s1,s1,s2,s2} > {s2,s2} > {t1,s2} = {t1,t2}

			    case ComparisonValue.Greater: 
				// s1 < t1, s2 > t1, s2 > t2
				return ComparisonValue.Greater;

			    case ComparisonValue.Incomparable: 
				// s1 < t1, s2 > t1, s2 * t2
				return ComparisonValue.Incomparable;

			    }; // switch (cmpS2T2)
		    
			break; // case ComparisonValue.Greater
		    


		    case ComparisonValue.Incomparable: 
			// s1 < t1, s2 * t1
			cmpS2T2 = ReductionOrdering.current().compare(s2,t2);

		    
			switch (cmpS2T2)
			    {
      
			    case ComparisonValue.Smaller: 
				// s1 < t1, s2 * t1, s2 < t2
				return ComparisonValue.Smaller;

			    case ComparisonValue.Equivalent: 
				// s1 < t1, s2 * t1, s2 = t2 
				cmpS1S2  = ReductionOrdering.current().compare(s1,s2);
			    
				assert cmpS1S2 != ComparisonValue.Equivalent;

				return
				    (cmpS1S2 == ComparisonValue.Greater)? 
				    ComparisonValue.Smaller 
				    : 
				    ComparisonValue.Incomparable;



			    case ComparisonValue.Greater: 
				// s1 < t1, s2 * t1, s2 > t2
			    
				cmpS1S2 = ReductionOrdering.current().compare(s1,s2);
				
				switch (cmpS1S2)
				    {
				    case ComparisonValue.Smaller: 
					// s1 < t1, s2 * t1, s2 > t2, s1 < s2

					cmpT1T2 =
					    ReductionOrdering.current().compare(t1,t2);

					assert cmpT1T2 != ComparisonValue.Equivalent;

					return 
					    (cmpT1T2 == ComparisonValue.Smaller)? 
					    ComparisonValue.Greater 
					    : 
					    ComparisonValue.Incomparable; 


				    case ComparisonValue.Equivalent: 
					// s1 < t1, s2 * t1, s2 > t2, s1 = s2
					assert false;
					return ComparisonValue.Incomparable;

				    case ComparisonValue.Greater: 
					// s1 < t1, s2 * t1, s2 > t2, s1 > s2
					return ComparisonValue.Smaller;

				    case ComparisonValue.Incomparable: 
					// s1 < t1, s2 * t1, s2 > t2, s1 * s2
					cmpT1T2 = 
					    ReductionOrdering.current().compare(t1,t2);
				    
					assert cmpT1T2 != ComparisonValue.Equivalent;
				    
					return 
					    (cmpT1T2 == ComparisonValue.Smaller)? 
					    ComparisonValue.Greater 
					    : 
					    ComparisonValue.Incomparable; 
				    
				    }; // switch (cmpS1S2)

				break; // case ComparisonValue.Greater

			    case ComparisonValue.Incomparable: 
				// s1 < t1, s2 * t1, s2 * t2
				return ComparisonValue.Incomparable;

			    }; // switch (cmpS2T2)
		    
			break; // case ComparisonValue.Incomparable

		    }; // switch (cmpS2T1)
	    
		break; // case ComparisonValue.Smaller
	    


	    case ComparisonValue.Equivalent: 
		// s1 = t1

		cmpS2T2 = ReductionOrdering.current().compare(s2,t2);

		switch (cmpS2T2)
		    {
		    case ComparisonValue.Smaller: 
			// s1 = t1, s2 < t2
			cmpS1T2 = ReductionOrdering.current().compare(s1,t2);
		    
			switch (cmpS1T2) 
			    {
			    case ComparisonValue.Smaller: 
				// s1 = t1, s2 < t2, s1 < t2
				return ComparisonValue.Smaller;

			    case ComparisonValue.Equivalent: 
				// s1 = t1, s2 < t2, s1 = t2
				return ComparisonValue.Greater;

			    case ComparisonValue.Greater: 
				// s1 = t1, s2 < t2, s1 > t2
				return ComparisonValue.Greater;

			    case ComparisonValue.Incomparable: 
				// s1 = t1, s2 < t2, s1 * t2
				cmpS1S2 = ReductionOrdering.current().compare(s1,s2);
			    
				assert cmpS1S2 != ComparisonValue.Equivalent;
				return 
				    (cmpS1S2 == ComparisonValue.Smaller)?
				    ComparisonValue.Smaller 
				    : 
				    ComparisonValue.Incomparable;
			    
			    }; // switch (cmpS1T2) 



		    case ComparisonValue.Equivalent: 
			// s1 = t1, s2 = t2
			return ComparisonValue.Greater; 
			// Because {s1,s1,s2,s2} > {s1,s2} = {t1,t2}

		    case ComparisonValue.Greater: 
			// s1 = t1, s2 > t2
			return ComparisonValue.Greater;


		    case ComparisonValue.Incomparable: 
			// s1 = t1, s2 * t2
			cmpS1T2 = ReductionOrdering.current().compare(s1,t2);
	    
			switch (cmpS1T2)
			    {
			    case ComparisonValue.Smaller: 
				// s1 = t1, s2 * t2, s1 < t2

				cmpS1S2 = 
				    ReductionOrdering.current().compare(s1,s2);
			    
				assert cmpS1S2 != ComparisonValue.Equivalent;
			    
				return 
				    (cmpS1S2 == ComparisonValue.Greater)? 
				    ComparisonValue.Smaller 
				    : 
				    ComparisonValue.Incomparable;

			    
			    case ComparisonValue.Equivalent: 
				// s1 = t1, s2 * t2, s1 = t2
				return ComparisonValue.Greater; 
				// Because {s1,s1,s2,s2} > {s1,s1} = {t1,t2}


			    case ComparisonValue.Greater: 
				// s1 = t1, s2 * t2, s1 > t2
				return ComparisonValue.Greater; 
				// Because {s1,s1,s2,s2} > {s1,s1} > {s1,t2} = {t1,t2}
				

			    case ComparisonValue.Incomparable: 
				// s1 = t1, s2 * t2, s1 * t2
				return ComparisonValue.Incomparable;

			    }; // switch (cmpS1T2)
	    	    
			break; // case ComparisonValue.Incomparable

		    }; // switch (cmpS2T2)
	    
		break; // case ComparisonValue.Equivalent



	    case ComparisonValue.Greater: 
		// s1 > t1
	    
		cmpS1T2 = ReductionOrdering.current().compare(s1,t2);
	    
		switch (cmpS1T2)
		    {
		    case ComparisonValue.Smaller: 
			// s1 > t1, s1 < t2
	    
			cmpS2T2 = ReductionOrdering.current().compare(s2,t2);
		    
			switch (cmpS2T2)
			    {
			    case ComparisonValue.Smaller: 
				// s1 > t1, s1 < t2, s2 < t2
				return ComparisonValue.Smaller;

			    case ComparisonValue.Equivalent: 
				// s1 > t1, s1 < t2, s2 = t2
				return ComparisonValue.Greater;

			    case ComparisonValue.Greater: 
				// s1 > t1, s1 < t2, s2 > t2
				return ComparisonValue.Greater;

			    case ComparisonValue.Incomparable: 
				// s1 > t1, s1 < t2, s2 * t2
			    
				cmpS1S2 = ReductionOrdering.current().compare(s1,s2);

				assert cmpS1S2 != ComparisonValue.Equivalent;
				return 
				    (cmpS1S2 == ComparisonValue.Greater)? 
				    ComparisonValue.Smaller 
				    : 
				    ComparisonValue.Incomparable;

			    }; // switch (cmpS2T2)
		    
			break; // case ComparisonValue.Smaller


		    case ComparisonValue.Equivalent: 
			// s1 > t1, s1 = t2
			return ComparisonValue.Greater;


		    case ComparisonValue.Greater: 
			// s1 > t1, s1 > t2
			return ComparisonValue.Greater;
		    

		    case ComparisonValue.Incomparable: 
			// s1 > t1, s1 * t2

			cmpS2T2 = ReductionOrdering.current().compare(s2,t2);
		    

			switch (cmpS2T2)
			    {
			    case ComparisonValue.Smaller: 
				// s1 > t1, s1 * t2, s2 < t2

				cmpS1S2 = 
				    ReductionOrdering.current().compare(s1,s2);

				assert cmpS1S2 != ComparisonValue.Equivalent;
			    
				return 
				    (cmpS1S2 == ComparisonValue.Smaller)? 
				    ComparisonValue.Greater 
				    : 
				    ComparisonValue.Incomparable;

			    case ComparisonValue.Equivalent: 
				// s1 > t1, s1 * t2, s2 = t2 
				return ComparisonValue.Greater;

			    case ComparisonValue.Greater: 
				// s1 > t1, s1 * t2, s2 > t2
				return ComparisonValue.Greater;

			    case ComparisonValue.Incomparable: 
				// s1 > t1, s1 * t2, s2 * t2
				return ComparisonValue.Incomparable;

			    }; // switch (cmpS2T2)
		    
			break; // case ComparisonValue.Incomparable

		    }; // switch (cmpS1T2)

		break; // case ComparisonValue.Greater


	    case ComparisonValue.Incomparable: 
		// s1 * t1
	    
		cmpS1T2 = 
		    ReductionOrdering.current().compare(s1,t2);

		switch (cmpS1T2)
		    {
		    case ComparisonValue.Smaller: 
			// s1 * t1, s1 < t2

			cmpS2T2 = 
			    ReductionOrdering.current().compare(s2,t2);
		    
			switch (cmpS2T2)
			    {
			    case ComparisonValue.Smaller: 
				// s1 * t1, s1 < t2, s2 < t2
				return ComparisonValue.Smaller;

			    case ComparisonValue.Equivalent: 
				// s1 * t1, s1 < t2, s2 = t2
			    
				cmpS2T1 = ReductionOrdering.current().compare(s2,t1);
			    
				assert cmpS2T1 != ComparisonValue.Equivalent;
				return 
				    (cmpS2T1 == ComparisonValue.Smaller)? 
				    ComparisonValue.Smaller 
				    : 
				    ComparisonValue.Incomparable;

			    case ComparisonValue.Greater: 
				// s1 * t1, s1 < t2, s2 > t2
				cmpS2T1 = ReductionOrdering.current().compare(s2,t1);
				return cmpS2T1;




			    case ComparisonValue.Incomparable: 
				// s1 * t1, s1 < t2, s2 * t2
				cmpS2T1 = ReductionOrdering.current().compare(s2,t1);

			    
				switch (cmpS2T1)
				    {

				    case ComparisonValue.Smaller: 
					// s1 * t1, s1 < t2, s2 * t2, s2 < t1
					return ComparisonValue.Smaller;

				    case ComparisonValue.Equivalent:  // s1 * t1, s1 < t2, s2 * t2, s2 = t1
				    
					cmpS1S2 = 
					    ReductionOrdering.current().compare(s1,s2);
				    
					switch (cmpS1S2)
					    {
					    case ComparisonValue.Smaller: 
						// s1 * t1, s1 < t2, s2 * t2, s2 < t1, s1 < s2
						return ComparisonValue.Incomparable;
					    case ComparisonValue.Equivalent: 
						// s1 * t1, s1 < t2, s2 * t2, s2 < t1, s1 = s2
						return ComparisonValue.Smaller;
					    case ComparisonValue.Greater: 
						// s1 * t1, s1 < t2, s2 * t2, s2 < t1, s1 > s2
						return ComparisonValue.Smaller;
					    case ComparisonValue.Incomparable: 
						// s1 * t1, s1 < t2, s2 * t2, s2 < t1, s1 * s2
						return ComparisonValue.Incomparable;
					    }; // switch (cmpS1S2)


				    
				    case ComparisonValue.Greater: 
					// s1 * t1, s1 < t2, s2 * t2, s2 > t1

					cmpS1S2 = ReductionOrdering.current().compare(s1,s2);
				    
					switch (cmpS1S2)
					    {
					    case ComparisonValue.Smaller: 
						// s1 * t1, s1 < t2, s2 * t2, s2 > t1, s1 < s2
						return ComparisonValue.Greater;
					    case ComparisonValue.Equivalent: 
						// s1 * t1, s1 < t2, s2 * t2, s2 > t1, s1 < s2 
						assert false;
						return ComparisonValue.Incomparable;
					    case ComparisonValue.Greater: 
						// s1 * t1, s1 < t2, s2 * t2, s2 > t1, s1 < s2 
						return ComparisonValue.Greater;
					    case ComparisonValue.Incomparable: 
						// s1 * t1, s1 < t2, s2 * t2, s2 > t1, s1 < s2
						return ComparisonValue.Incomparable;
					    }; // switch (cmpS1S2)
				    				    

				    case ComparisonValue.Incomparable: 
					// s1 * t1, s1 < t2, s2 * t2, s2 * t1
					return ComparisonValue.Incomparable;

				    }; // switch (cmpS2T1)
			    
				break; // case ComparisonValue.Incomparable

			    }; // switch (cmpS1T2)
		    
			break; // case ComparisonValue.Smaller


		    case ComparisonValue.Equivalent: 
			// s1 * t1, s1 = t2

			cmpS2T1 = ReductionOrdering.current().compare(s2,t1);

			switch (cmpS2T1) 
			    {
			    case ComparisonValue.Smaller: 
				// s1 * t1, s1 = t2, s2 < t1
			    
				cmpS1S2 =  ReductionOrdering.current().compare(s1,s2);

				assert cmpS1S2 != ComparisonValue.Equivalent;
				return 
				    (cmpS1S2 == ComparisonValue.Smaller)? 
				    ComparisonValue.Smaller 
				    : 
				    ComparisonValue.Incomparable;

			    case ComparisonValue.Equivalent: 
				// s1 * t1, s1 = t2, s2 = t1
				return ComparisonValue.Greater; 
				// Because {s1,s1,s2,s2} > {s1,s2} = {t1,t2}
 

			    case ComparisonValue.Greater: 
				// s1 * t1, s1 = t2, s2 > t1
				return ComparisonValue.Greater;  
				// Because {s1,s1,s2,s2} > {s1,s2} > {t1,s1} = {t1,t2}
			    
			    case ComparisonValue.Incomparable: 
				// s1 * t1, s1 = t2, s2 * t1
				return ComparisonValue.Incomparable;

			    }; // switch (cmpS2T1) 


		    case ComparisonValue.Greater: 
			// s1 * t1, s1 > t2

			cmpS2T1 = ReductionOrdering.current().compare(s2,t1);
		    
			switch (cmpS2T1)
			    {
			    case ComparisonValue.Smaller: 
				// s1 * t1, s1 > t2, s2 < t1 
			    
				cmpS1S2 = ReductionOrdering.current().compare(s1,s2);
			    
				assert cmpS1S2 != ComparisonValue.Equivalent;
				return 
				    (cmpS1S2 == ComparisonValue.Smaller)? 
				    ComparisonValue.Smaller 
				    : 
				    ComparisonValue.Incomparable;

			    case ComparisonValue.Equivalent: 
				// s1 * t1, s1 > t2, s2 = t1 
				return ComparisonValue.Greater;

			    case ComparisonValue.Greater: 
				// s1 * t1, s1 > t2, s2 > t1 
				return ComparisonValue.Greater;

			    case ComparisonValue.Incomparable: 
				// s1 * t1, s1 > t2, s2 * t1 

				cmpT1T2 = ReductionOrdering.current().compare(t1,t2);
			    
				assert cmpT1T2 != ComparisonValue.Equivalent;
			    
				return 
				    (cmpT1T2 == ComparisonValue.Smaller)? 
				    ComparisonValue.Greater 
				    : 
				    ComparisonValue.Incomparable;

			    }; // switch (cmpS2T1)

			break; // case ComparisonValue.Greater
		    
				   


		    case ComparisonValue.Incomparable: 
			// s1 * t1, s1 * t2
		    
			cmpS2T1 = ReductionOrdering.current().compare(s2,t1);
			    
			switch (cmpS2T1)
			    {
			    case ComparisonValue.Smaller: 
				// s1 * t1, s1 * t2, s2 < t1

				cmpS1S2 = ReductionOrdering.current().compare(s1,s2);
			    
				assert cmpS1S2 != ComparisonValue.Equivalent;
				return 
				    (cmpS1S2 == ComparisonValue.Smaller)? 
				    ComparisonValue.Smaller 
				    : 
				    ComparisonValue.Incomparable;

			    case ComparisonValue.Equivalent: 
				// s1 * t1, s1 * t2, s2 = t1
				return ComparisonValue.Incomparable;

			    case ComparisonValue.Greater: 
				// s1 * t1, s1 * t2, s2 > t1
			    
				cmpS2T2 = ReductionOrdering.current().compare(s2,t2);
			    
				switch (cmpS2T2)
				    {
				
				    case ComparisonValue.Smaller: 
					// s1 * t1, s1 * t2, s2 > t1, s2 < t2

					cmpS1S2 = ReductionOrdering.current().compare(s1,s2);
				    
					assert cmpS1S2 != ComparisonValue.Equivalent;
					return 
					    (cmpS1S2 == ComparisonValue.Smaller)? 
					    ComparisonValue.Smaller 
					    : 
					    ComparisonValue.Incomparable;

				    case ComparisonValue.Equivalent: 
					// s1 * t1, s1 * t2, s2 > t1, s2 = t2
					return ComparisonValue.Greater; 
					// Because {s1,s1,s2,s2} > {s2,s2} > {t1,s2} = {t1,t2}

				    case ComparisonValue.Greater: 
					// s1 * t1, s1 * t2, s2 > t1, s2 < t2
					return ComparisonValue.Greater;

				    case ComparisonValue.Incomparable: 
					// s1 * t1, s1 * t2, s2 > t1, s2 < t2
					return ComparisonValue.Incomparable;

				    }; // switch (cmpS2T2)
			    
				break; // case ComparisonValue.Greater


			    case ComparisonValue.Incomparable: 
				// s1 * t1, s1 * t2, s2 * t1
				cmpS2T2 = ReductionOrdering.current().compare(s2,t2);

				switch (cmpS2T2) 
				    {
				    case ComparisonValue.Smaller: 
					// s1 * t1, s1 * t2, s2 * t1, s2 < t2
				    
					cmpS1S2 = ReductionOrdering.current().compare(s1,s2);
				    
					assert cmpS1S2 != ComparisonValue.Equivalent;
					return 
					    (cmpS1S2 == ComparisonValue.Smaller)? 
					    ComparisonValue.Smaller 
					    : 
					    ComparisonValue.Incomparable;
				    
				    case ComparisonValue.Equivalent: 
					// s1 * t1, s1 * t2, s2 * t1, s2 = t2
					return ComparisonValue.Incomparable;

				    case ComparisonValue.Greater: 
					// s1 * t1, s1 * t2, s2 * t1, s2 > t2
				    
					cmpT1T2 = ReductionOrdering.current().compare(t1,t2);
				    
					assert cmpT1T2 != ComparisonValue.Equivalent;

					return 
					    (cmpT1T2 == ComparisonValue.Smaller)? 
					    ComparisonValue.Greater 
					    : 
					    ComparisonValue.Incomparable;

				    case ComparisonValue.Incomparable: 
					// s1 * t1, s1 * t2, s2 * t1, s2 * t2
					return ComparisonValue.Incomparable;

				    }; // switch (cmpS2T2)

				break; // case ComparisonValue.Incomparable

			    }; // switch (cmpS2T1)

			break; // case ComparisonValue.Incomparable

		    }; // switch (cmpS1T2)
	    
		break; // case ComparisonValue.Incomparable

	    }; // switch (cmpS1T1)

	assert false;

	return ComparisonValue.Incomparable;

    } // compareNegEqWithPosEq(Formula atom1,Formula atom2)








    private 
	int 
	compareEqualities(FlattermLiteral lit1,FlattermLiteral lit2) {
	
	assert lit1.isEquality();
	assert lit2.isEquality();

	if (lit1.isPositive() == lit2.isPositive())
	    return compareEqualityAtoms(lit1.atom(),lit2.atom());

	if (lit1.isPositive())
	    return 
		FunctionComparisonValue.flip(compareNegEqWithPosEq(lit1.atom(),lit2.atom()));

	assert lit2.isPositive();
	return 
	    compareNegEqWithPosEq(lit1.atom(),lit2.atom());


    } // compareEqualities(FlattermLiteral lit1,FlattermLiteral lit2)
	














    private 
	int 
	compareNonEqualities(FlattermLiteral lit1,FlattermLiteral lit2) {
	

	assert !lit1.isEquality();
	assert !lit2.isEquality();

	int cmp = 
	    ReductionOrdering.current().compare(lit1.atom(),lit2.atom());

	if (cmp != ComparisonValue.Equivalent) return cmp;

	// Same atoms. Compare the polarities: 
	
	if (lit1.isPositive() == lit2.isPositive())
	    return ComparisonValue.Equivalent;

	return 
	    (lit1.isPositive())? 
	    ComparisonValue.Smaller 
	    :
	    ComparisonValue.Greater;
	    

    } // compareNonEqualities(FlattermLiteral lit1,FlattermLiteral lit2)
	


    private 
	int 
	compareEqualityAtoms(Flatterm atom1,Flatterm atom2) {
	
	// Note that we only assume transitivity of the reduction
	// ordering in a weak sense, so that 
	// s < t && t < u implies that s >= t is impossible, 
	// but does not guarantee s < u, i.e., s * u is possible.


	assert atom1.isAtomicFormula();
	assert atom1.predicate().isEquality();
	assert atom2.isAtomicFormula();
	assert atom2.predicate().isEquality();

	Flatterm s1 = atom1.nextCell();
	Flatterm s2 = s1.after();
	Flatterm t1 = atom2.nextCell();
	Flatterm t2 = t1.after();
	
	// Compare multisets {s1,s2} and {t1,t2}:

	
	int cmpS1T1;
	int cmpS1T2;
	int cmpS2T1;
	int cmpS2T2;
	
	int cmpS1S2;
	int cmpT1T2;
	

	cmpS1T1 =
	    ReductionOrdering.current().compare(s1,t1);

	switch (cmpS1T1) 
	    {
	    case ComparisonValue.Smaller: 
		// s1 < t1

		cmpS2T1 = ReductionOrdering.current().compare(s2,t1);
	    
		switch (cmpS2T1) 
		    {		
		    case ComparisonValue.Smaller: 
			// s1 < t1, s2 < t1
			return ComparisonValue.Smaller;

		    case ComparisonValue.Equivalent: 
			// s1 < t1, s2 = t1
			assert 
			    ReductionOrdering.current().compare(s1,s2) == 
			    ComparisonValue.Smaller;

			cmpS2T2 = 
			    ReductionOrdering.current().compare(s2,t2);
		    
			switch (cmpS2T2) 
			    {
			    case ComparisonValue.Smaller: 
				// s1 < t1, s2 = t1, s2 < t2
				return ComparisonValue.Smaller;
			    
			    case ComparisonValue.Equivalent: 
				// s1 < t1, s2 = t1, s2 = t2
				return ComparisonValue.Smaller;

			    case ComparisonValue.Greater: 
				// s1 < t1, s2 = t1, s2 > t2
			    
				return 
				    ReductionOrdering.current().compare(s1,t2);

			    case ComparisonValue.Incomparable: 
				// s1 < t1, s2 = t1, s2 * t2
				cmpS1T2 = 
				    ReductionOrdering.current().compare(s1,t2);
			    
				assert cmpS1T2 != ComparisonValue.Equivalent;
			    
				return cmpS1T2;

			    }; // switch (cmpS2T2) 
		    
			break; // case ComparisonValue.Equivalent


		    case ComparisonValue.Greater: 
			// s1 < t1, s2 > t1

			cmpS2T2 = 
			    ReductionOrdering.current().compare(s2,t2);

			switch (cmpS2T2)
			    {
      
			    case ComparisonValue.Smaller: 
				// s1 < t1, s2 > t1, s2 < t2
				return ComparisonValue.Smaller;

			    case ComparisonValue.Equivalent: 
				// s1 < t1, s2 > t1, s2 = t2

				assert 
				    ReductionOrdering.current().compare(t2,t1) == 
				    ComparisonValue.Greater;
			    
				return ComparisonValue.Smaller;

			    case ComparisonValue.Greater: 
				// s1 < t1, s2 > t1, s2 > t2
				return ComparisonValue.Greater;

			    case ComparisonValue.Incomparable: 
				// s1 < t1, s2 > t1, s2 * t2
				return ComparisonValue.Incomparable;

			    }; // switch (cmpS2T2)
		    
			break; // case ComparisonValue.Greater
		    


		    case ComparisonValue.Incomparable: 
			// s1 < t1, s2 * t1

			cmpS2T2 = 
			    ReductionOrdering.current().compare(s2,t2);

		    
			switch (cmpS2T2)
			    {
      
			    case ComparisonValue.Smaller: 
				// s1 < t1, s2 * t1, s2 < t2
				return ComparisonValue.Smaller;

			    case ComparisonValue.Equivalent: 
				// s1 < t1, s2 * t1, s2 = t2 
				return ComparisonValue.Smaller;

			    case ComparisonValue.Greater: 
				// s1 < t1, s2 * t1, s2 > t2
				cmpS1S2 = 
				    ReductionOrdering.current().compare(s1,s2);
				
				switch (cmpS1S2)
				    {
				    case ComparisonValue.Smaller: 
					// s1 < t1, s2 * t1, s2 > t2, s1 < s2

					cmpT1T2 = 
					    ReductionOrdering.current().compare(t1,t2);

					assert cmpT1T2 != ComparisonValue.Equivalent;

					return 
					    (cmpT1T2 == ComparisonValue.Smaller)? 
					    ComparisonValue.Greater 
					    : 
					    ComparisonValue.Incomparable; 

				    case ComparisonValue.Equivalent: 
					// s1 < t1, s2 * t1, s2 > t2, s1 = s2
					assert false;
					return ComparisonValue.Incomparable;

				    case ComparisonValue.Greater: 
					// s1 < t1, s2 * t1, s2 > t2, s1 > s2
					return ComparisonValue.Smaller;

				    case ComparisonValue.Incomparable: 
					// s1 < t1, s2 * t1, s2 > t2, s1 * s2
					cmpT1T2 = 
					    ReductionOrdering.current().compare(t1,t2);
				    
					assert cmpT1T2 != ComparisonValue.Equivalent;
				    
					return 
					    (cmpT1T2 == ComparisonValue.Smaller)? 
					    ComparisonValue.Greater 
					    : 
					    ComparisonValue.Incomparable; 
				    
				    }; // switch (cmpS1S2)

				break; // case ComparisonValue.Greater

			    case ComparisonValue.Incomparable: 
				// s1 < t1, s2 * t1, s2 * t2
				return ComparisonValue.Incomparable;

			    }; // switch (cmpS2T2)
		    
			break; // case ComparisonValue.Incomparable

		    }; // switch (cmpS2T1)
	    
		break; // case ComparisonValue.Smaller
	    


	    case ComparisonValue.Equivalent: // s1 = t1

		cmpS2T2 = ReductionOrdering.current().compare(s2,t2);

		switch (cmpS2T2)
		    {
		    case ComparisonValue.Smaller: 
			// s1 = t1, s2 < t2
			return ComparisonValue.Smaller;

		    case ComparisonValue.Equivalent: 
			// s1 = t1, s2 = t2
			return ComparisonValue.Equivalent;

		    case ComparisonValue.Greater: 
			// s1 = t1, s2 > t2
			return ComparisonValue.Greater;

		    case ComparisonValue.Incomparable: 
			// s1 = t1, s2 * t2
			cmpS1T2 = ReductionOrdering.current().compare(s1,t2);
	    
			switch (cmpS1T2)
			    {
			    case ComparisonValue.Smaller: 
				// s1 = t1, s2 * t2, s1 < t2

				cmpS1S2 = 
				    ReductionOrdering.current().compare(s1,s2);
			    
				assert cmpS1S2 != ComparisonValue.Equivalent;
			    
				return 
				    (cmpS1S2 == ComparisonValue.Greater)? 
				    ComparisonValue.Smaller 
				    : 
				    ComparisonValue.Incomparable;

			    
			    case ComparisonValue.Equivalent: 
				// s1 = t1, s2 * t2, s1 = t2

				cmpS1S2 = 
				    ReductionOrdering.current().compare(s1,s2);

				assert cmpS1S2 != ComparisonValue.Equivalent;

				return ComparisonValue.flip(cmpS1S2);


			    case ComparisonValue.Greater: 
				// s1 = t1, s2 * t2, s1 > t2

				cmpS1S2 = 
				    ReductionOrdering.current().compare(s1,s2);
			    
				assert cmpS1S2 != ComparisonValue.Equivalent;
			    
				return 
				    (cmpS1S2 == ComparisonValue.Smaller)? 
				    ComparisonValue.Greater 
				    : 
				    ComparisonValue.Incomparable;


			    case ComparisonValue.Incomparable: 
				// s1 = t1, s2 * t2, s1 * t2
				return ComparisonValue.Incomparable;

			    }; // switch (cmpS1T2)
	    	    
			break; // case ComparisonValue.Incomparable

		    }; // switch (cmpS2T2)
	    
		break; // case ComparisonValue.Equivalent



	    case ComparisonValue.Greater: // s1 > t1
	    
		cmpS1T2 = ReductionOrdering.current().compare(s1,t2);
	    
		switch (cmpS1T2)
		    {
		    case ComparisonValue.Smaller: 
			// s1 > t1, s1 < t2
			cmpS2T2 = ReductionOrdering.current().compare(s2,t2);
		    
			switch (cmpS2T2)
			    {
			    case ComparisonValue.Smaller: 
				// s1 > t1, s1 < t2, s2 < t2
				return ComparisonValue.Smaller;

			    case ComparisonValue.Equivalent: 
				// s1 > t1, s1 < t2, s2 = t2
				return ComparisonValue.Greater;

			    case ComparisonValue.Greater: 
				// s1 > t1, s1 < t2, s2 > t2
				return ComparisonValue.Greater;

			    case ComparisonValue.Incomparable: 
				// s1 > t1, s1 < t2, s2 * t2
			    
				cmpS1S2 = 
				    ReductionOrdering.current().compare(s1,s2);
			    
				assert cmpS1S2 != ComparisonValue.Equivalent;

				return 
				    (cmpS1S2 == ComparisonValue.Greater)? 
				    ComparisonValue.Smaller 
				    : 
				    ComparisonValue.Incomparable;

			    }; // switch (cmpS2T2)
		    
			break; // case ComparisonValue.Smaller


		    case ComparisonValue.Equivalent: 
			// s1 > t1, s1 = t2
		    
			cmpS2T2 = ReductionOrdering.current().compare(s2,t2);

			switch (cmpS2T2)
			    {
			    case ComparisonValue.Smaller: 
				// s1 > t1, s1 = t2, s2 < t2
				return ReductionOrdering.current().compare(s2,t1);

			    case ComparisonValue.Equivalent: 
				// s1 > t1, s1 = t2, s2 = t2
				return ComparisonValue.Greater;

			    case ComparisonValue.Greater: 
				// s1 > t1, s1 = t2, s2 > t2
				return ComparisonValue.Greater;

			    case ComparisonValue.Incomparable: 
				// s1 > t1, s1 = t2, s2 * t2
			    
				cmpS2T1 = 
				    ReductionOrdering.current().compare(s2,t1);

				assert cmpS2T1 != ComparisonValue.Equivalent;
			    
				return 
				    (cmpS2T1 == ComparisonValue.Smaller)? 
				    ComparisonValue.Smaller 
				    : 
				    ComparisonValue.Incomparable;

			    }; // switch (cmpS2T2)
		    
			break; // case ComparisonValue.Equivalent


		    case ComparisonValue.Greater: 
			// s1 > t1, s1 > t2
			return ComparisonValue.Greater;
		    

		    case ComparisonValue.Incomparable: 
			// s1 > t1, s1 * t2

			cmpS2T2 = ReductionOrdering.current().compare(s2,t2);
		    

			switch (cmpS2T2)
			    {
			    case ComparisonValue.Smaller: 
				// s1 > t1, s1 * t2, s2 < t2

				cmpS1S2 = 
				    ReductionOrdering.current().compare(s1,s2);

				assert cmpS1S2 != ComparisonValue.Equivalent;
			    
				return
				    (cmpS1S2 == ComparisonValue.Smaller)? 
				    ComparisonValue.Greater 
				    : 
				    ComparisonValue.Incomparable;

			    case ComparisonValue.Equivalent: 
				// s1 > t1, s1 * t2, s2 = t2 
				return ComparisonValue.Greater;

			    case ComparisonValue.Greater: 
				// s1 > t1, s1 * t2, s2 > t2
				return ComparisonValue.Greater;

			    case ComparisonValue.Incomparable: 
				// s1 > t1, s1 * t2, s2 * t2
				return ComparisonValue.Incomparable;

			    }; // switch (cmpS2T2)
		    
			break; // case ComparisonValue.Incomparable

		    }; // switch (cmpS1T2)

		break; // case ComparisonValue.Greater


	    case ComparisonValue.Incomparable: 
		// s1 * t1
	    
		cmpS1T2 = 
		    ReductionOrdering.current().compare(s1,t2);

		switch (cmpS1T2)
		    {
		    case ComparisonValue.Smaller: 
			// s1 * t1, s1 < t2

			cmpS2T2 = 
			    ReductionOrdering.current().compare(s2,t2);
		    
			switch (cmpS2T2)
			    {
			    case ComparisonValue.Smaller: 
				// s1 * t1, s1 < t2, s2 < t2
				return ComparisonValue.Smaller;

			    case ComparisonValue.Equivalent: 
				// s1 * t1, s1 < t2, s2 = t2
			    
				cmpS2T1 = 
				    ReductionOrdering.current().compare(s2,t1);
			    
				assert cmpS2T1 != ComparisonValue.Equivalent;
			    
				return 
				    (cmpS2T1 == ComparisonValue.Smaller)? 
				    ComparisonValue.Smaller 
				    : 
				    ComparisonValue.Incomparable;

			    case ComparisonValue.Greater: 
				// s1 * t1, s1 < t2, s2 > t2
			    
				cmpS2T1 = 
				    ReductionOrdering.current().compare(s2,t1);
			    
				return 
				    (cmpS2T1 == ComparisonValue.Equivalent)? 
				    ComparisonValue.Smaller 
				    : 
				    cmpS2T1;

			    case ComparisonValue.Incomparable: 
				// s1 * t1, s1 < t2, s2 * t2

				cmpS2T1 = 
				    ReductionOrdering.current().compare(s2,t1);

			    
				switch (cmpS2T1)
				    {

				    case ComparisonValue.Smaller: 
					// s1 * t1, s1 < t2, s2 * t2, s2 < t1
					return ComparisonValue.Smaller;

				    case ComparisonValue.Equivalent:  
					// s1 * t1, s1 < t2, s2 * t2, s2 = t1
					return ComparisonValue.Smaller;
				    
				    case ComparisonValue.Greater: 
					// s1 * t1, s1 < t2, s2 * t2, s2 > t1

					cmpS1S2 = 
					    ReductionOrdering.current().compare(s1,s2);
				    
					assert cmpS1S2 != ComparisonValue.Equivalent;
				    
					return 
					    (cmpS1S2 == ComparisonValue.Greater)? 
					    ComparisonValue.Smaller 
					    : 
					    ComparisonValue.Incomparable;


				    case ComparisonValue.Incomparable: 
					// s1 * t1, s1 < t2, s2 * t2, s2 * t1

					return ComparisonValue.Incomparable;

				    }; // switch (cmpS2T1)
			    
				break; // case ComparisonValue.Incomparable

			    }; // switch (cmpS1T2)
		    
			break; // case ComparisonValue.Smaller



		    case ComparisonValue.Equivalent: 
			// s1 * t1, s1 = t2
			cmpS2T1 = 
			    ReductionOrdering.current().compare(s2,t1);
		    
			return cmpS2T1;


		    case ComparisonValue.Greater: 
			// s1 * t1, s1 > t2
			cmpS2T1 = 
			    ReductionOrdering.current().compare(s2,t1);
		    
			switch (cmpS2T1)
			    {
			    case ComparisonValue.Smaller: 
				// s1 * t1, s1 > t2, s2 < t1 
				cmpS1S2 = 
				    ReductionOrdering.current().compare(s1,s2);
			    
				assert cmpS1S2 != ComparisonValue.Equivalent;

				return 
				    (cmpS1S2 == ComparisonValue.Smaller)? 
				    ComparisonValue.Smaller 
				    : 
				    ComparisonValue.Incomparable;

			    case ComparisonValue.Equivalent: 
				// s1 * t1, s1 > t2, s2 = t1 
				return ComparisonValue.Greater;

			    case ComparisonValue.Greater: 
				// s1 * t1, s1 > t2, s2 > t1 
				return ComparisonValue.Greater;

			    case ComparisonValue.Incomparable: 
				// s1 * t1, s1 > t2, s2 * t1 
				cmpT1T2 = 
				    ReductionOrdering.current().compare(t1,t2);
			    
				assert cmpT1T2 != ComparisonValue.Equivalent;
			    
				return 
				    (cmpT1T2 == ComparisonValue.Smaller)? 
				    ComparisonValue.Greater 
				    : 
				    ComparisonValue.Incomparable;

			    }; // switch (cmpS2T1)

			break; // case ComparisonValue.Greater
		    


		    case ComparisonValue.Incomparable: 
			// s1 * t1, s1 * t2
			cmpS2T1 = 
			    ReductionOrdering.current().compare(s2,t1);
			    
			switch (cmpS2T1)
			    {
			    case ComparisonValue.Smaller: 
				// s1 * t1, s1 * t2, s2 < t1
				cmpS1S2 = 
				    ReductionOrdering.current().compare(s1,s2);
			    
				assert cmpS1S2 != ComparisonValue.Equivalent;

				return 
				    (cmpS1S2 == ComparisonValue.Smaller)? 
				    ComparisonValue.Smaller 
				    : 
				    ComparisonValue.Incomparable;

			    case ComparisonValue.Equivalent: 
				// s1 * t1, s1 * t2, s2 = t1
				return ComparisonValue.Incomparable;

			    case ComparisonValue.Greater: 
				// s1 * t1, s1 * t2, s2 > t1
				cmpS2T2 = 
				    ReductionOrdering.current().compare(s2,t2);
			    
				switch (cmpS2T2)
				    {
				
				    case ComparisonValue.Smaller: 
					// s1 * t1, s1 * t2, s2 > t1, s2 < t2
					cmpS1S2 = 
					    ReductionOrdering.current().compare(s1,s2);
				    
					assert cmpS1S2 != ComparisonValue.Equivalent;

					return 
					    (cmpS1S2 == ComparisonValue.Smaller)? 
					    ComparisonValue.Smaller 
					    : 
					    ComparisonValue.Incomparable;

				    case ComparisonValue.Equivalent: 
					// s1 * t1, s1 * t2, s2 > t1, s2 = t2
					return ComparisonValue.Incomparable;

				    case ComparisonValue.Greater: 
					// s1 * t1, s1 * t2, s2 > t1, s2 < t2
					return ComparisonValue.Greater;

				    case ComparisonValue.Incomparable: 
					// s1 * t1, s1 * t2, s2 > t1, s2 < t2
					return ComparisonValue.Incomparable;

				    }; // switch (cmpS2T2)
			    
				break; // case ComparisonValue.Greater


			    case ComparisonValue.Incomparable: 
				// s1 * t1, s1 * t2, s2 * t1
				cmpS2T2 = 
				    ReductionOrdering.current().compare(s2,t2);

				switch (cmpS2T2) 
				    {
				    case ComparisonValue.Smaller: 
					// s1 * t1, s1 * t2, s2 * t1, s2 < t2
					cmpS1S2 = 
					    ReductionOrdering.current().compare(s1,s2);
				    
					assert cmpS1S2 != ComparisonValue.Equivalent;

					return 
					    (cmpS1S2 == ComparisonValue.Smaller)? 
					    ComparisonValue.Smaller 
					    : 
					    ComparisonValue.Incomparable;
				    
				    case ComparisonValue.Equivalent: 
					// s1 * t1, s1 * t2, s2 * t1, s2 = t2
					return ComparisonValue.Incomparable;

				    case ComparisonValue.Greater: 
					// s1 * t1, s1 * t2, s2 * t1, s2 > t2
					cmpT1T2 = 
					    ReductionOrdering.current().compare(t1,t2);
				    
					assert cmpT1T2 != ComparisonValue.Equivalent;

					return 
					    (cmpT1T2 == ComparisonValue.Smaller)? 
					    ComparisonValue.Greater 
					    : 
					    ComparisonValue.Incomparable;

				    case ComparisonValue.Incomparable: 
					// s1 * t1, s1 * t2, s2 * t1, s2 * t2
					return ComparisonValue.Incomparable;

				    }; // switch (cmpS2T2)

				break; // case ComparisonValue.Incomparable

			    }; // switch (cmpS2T1)

			break; // case ComparisonValue.Incomparable

		    }; // switch (cmpS1T2)
	    
		break; // case ComparisonValue.Incomparable

	    }; // switch (cmpS1T1)

 
	assert false;
	
	return ComparisonValue.Incomparable;


    } // compareEqualityAtoms(Flatterm atom1,Flatterm atom2)
	





    private 
	int 
	compareNegEqWithPosEq(Flatterm atom1,Flatterm atom2) {
	

	// Note that we only assume transitivity of the reduction
	// ordering in a weak sense, so that 
	// s < t && t < u implies that s >= t is impossible, 
	// but does not guarantee s < u, i.e., s * u is possible.


	assert atom1.isAtomicFormula();
	assert atom1.predicate().isEquality();
	assert atom2.isAtomicFormula();
	assert atom2.predicate().isEquality();

	Flatterm s1 = atom1.nextCell();
	Flatterm s2 = s1.after();
	Flatterm t1 = atom2.nextCell();
	Flatterm t2 = t1.after();
	

	// Compare multisets {s1,s1,s2,s2} (from s1 != s2) and 
	// {t1,t2} (from t1 == t2).

	int cmpS1T1;
	int cmpS1T2;
	int cmpS2T1;
	int cmpS2T2;
	int cmpS1S2;
	int cmpT1T2;
	
	cmpS1T1 = ReductionOrdering.current().compare(s1,t1);


	switch (cmpS1T1) 
	    {
	    case ComparisonValue.Smaller: 
		// s1 < t1

		cmpS2T1 = ReductionOrdering.current().compare(s2,t1);
	    
		switch (cmpS2T1) 
		    {		
		    case ComparisonValue.Smaller: 
			// s1 < t1, s2 < t1
			return ComparisonValue.Smaller;

		    case ComparisonValue.Equivalent: 
			// s1 < t1, s2 = t1
			assert 
			    ReductionOrdering.current().compare(s1,s2) == 
			    ComparisonValue.Smaller;
			cmpS2T2 = ReductionOrdering.current().compare(s2,t2);
		    
			switch (cmpS2T2) 
			    {
			    case ComparisonValue.Smaller: 
				// s1 < t1, s2 = t1, s2 < t2
				return ComparisonValue.Smaller;
			    
			    case ComparisonValue.Equivalent: 
				// s1 < t1, s2 = t1, s2 = t2
				return ComparisonValue.Greater; 
				// Because {s1,s1,s2,s2} > {s2,s2} = {t1,t2}
				
			    case ComparisonValue.Greater: 
				// s1 < t1, s2 = t1, s2 > t2
				return ComparisonValue.Greater;  
				// Because {s1,s1,s2,s2} > {s2,s2} > {s2,t2} = {t1,t2}

			    case ComparisonValue.Incomparable: 
				// s1 < t1, s2 = t1, s2 * t2
				cmpS1T2 = ReductionOrdering.current().compare(s1,t2);
				assert cmpS1T2 != ComparisonValue.Equivalent;
				return cmpS1T2;

			    }; // switch (cmpS2T2) 
		    
			break; // case ComparisonValue.Equivalent


		    case ComparisonValue.Greater: 
			// s1 < t1, s2 > t1
			cmpS2T2 = ReductionOrdering.current().compare(s2,t2);

			switch (cmpS2T2)
			    {
      
			    case ComparisonValue.Smaller: 
				// s1 < t1, s2 > t1, s2 < t2
				return ComparisonValue.Smaller;

			    case ComparisonValue.Equivalent: 
				// s1 < t1, s2 > t1, s2 = t2
				assert 
				    ReductionOrdering.current().compare(t2,t1) == 
				    ComparisonValue.Greater;
			    
				return ComparisonValue.Greater; 
				// Because {s1,s1,s2,s2} > {s2,s2} > {t1,s2} = {t1,t2}

			    case ComparisonValue.Greater: 
				// s1 < t1, s2 > t1, s2 > t2
				return ComparisonValue.Greater;

			    case ComparisonValue.Incomparable: 
				// s1 < t1, s2 > t1, s2 * t2
				return ComparisonValue.Incomparable;

			    }; // switch (cmpS2T2)
		    
			break; // case ComparisonValue.Greater
		    


		    case ComparisonValue.Incomparable: 
			// s1 < t1, s2 * t1
			cmpS2T2 = ReductionOrdering.current().compare(s2,t2);

		    
			switch (cmpS2T2)
			    {
      
			    case ComparisonValue.Smaller: 
				// s1 < t1, s2 * t1, s2 < t2
				return ComparisonValue.Smaller;

			    case ComparisonValue.Equivalent: 
				// s1 < t1, s2 * t1, s2 = t2 
				cmpS1S2  = ReductionOrdering.current().compare(s1,s2);
			    
				assert cmpS1S2 != ComparisonValue.Equivalent;

				return
				    (cmpS1S2 == ComparisonValue.Greater)? 
				    ComparisonValue.Smaller 
				    : 
				    ComparisonValue.Incomparable;



			    case ComparisonValue.Greater: 
				// s1 < t1, s2 * t1, s2 > t2
			    
				cmpS1S2 = ReductionOrdering.current().compare(s1,s2);
				
				switch (cmpS1S2)
				    {
				    case ComparisonValue.Smaller: 
					// s1 < t1, s2 * t1, s2 > t2, s1 < s2

					cmpT1T2 =
					    ReductionOrdering.current().compare(t1,t2);

					assert cmpT1T2 != ComparisonValue.Equivalent;

					return 
					    (cmpT1T2 == ComparisonValue.Smaller)? 
					    ComparisonValue.Greater 
					    : 
					    ComparisonValue.Incomparable; 


				    case ComparisonValue.Equivalent: 
					// s1 < t1, s2 * t1, s2 > t2, s1 = s2
					assert false;
					return ComparisonValue.Incomparable;

				    case ComparisonValue.Greater: 
					// s1 < t1, s2 * t1, s2 > t2, s1 > s2
					return ComparisonValue.Smaller;

				    case ComparisonValue.Incomparable: 
					// s1 < t1, s2 * t1, s2 > t2, s1 * s2
					cmpT1T2 = 
					    ReductionOrdering.current().compare(t1,t2);
				    
					assert cmpT1T2 != ComparisonValue.Equivalent;
				    
					return 
					    (cmpT1T2 == ComparisonValue.Smaller)? 
					    ComparisonValue.Greater 
					    : 
					    ComparisonValue.Incomparable; 
				    
				    }; // switch (cmpS1S2)

				break; // case ComparisonValue.Greater

			    case ComparisonValue.Incomparable: 
				// s1 < t1, s2 * t1, s2 * t2
				return ComparisonValue.Incomparable;

			    }; // switch (cmpS2T2)
		    
			break; // case ComparisonValue.Incomparable

		    }; // switch (cmpS2T1)
	    
		break; // case ComparisonValue.Smaller
	    


	    case ComparisonValue.Equivalent: 
		// s1 = t1

		cmpS2T2 = ReductionOrdering.current().compare(s2,t2);

		switch (cmpS2T2)
		    {
		    case ComparisonValue.Smaller: 
			// s1 = t1, s2 < t2
			cmpS1T2 = ReductionOrdering.current().compare(s1,t2);
		    
			switch (cmpS1T2) 
			    {
			    case ComparisonValue.Smaller: 
				// s1 = t1, s2 < t2, s1 < t2
				return ComparisonValue.Smaller;

			    case ComparisonValue.Equivalent: 
				// s1 = t1, s2 < t2, s1 = t2
				return ComparisonValue.Greater;

			    case ComparisonValue.Greater: 
				// s1 = t1, s2 < t2, s1 > t2
				return ComparisonValue.Greater;

			    case ComparisonValue.Incomparable: 
				// s1 = t1, s2 < t2, s1 * t2
				cmpS1S2 = ReductionOrdering.current().compare(s1,s2);
			    
				assert cmpS1S2 != ComparisonValue.Equivalent;
				return 
				    (cmpS1S2 == ComparisonValue.Smaller)?
				    ComparisonValue.Smaller 
				    : 
				    ComparisonValue.Incomparable;
			    
			    }; // switch (cmpS1T2) 



		    case ComparisonValue.Equivalent: 
			// s1 = t1, s2 = t2
			return ComparisonValue.Greater; 
			// Because {s1,s1,s2,s2} > {s1,s2} = {t1,t2}

		    case ComparisonValue.Greater: 
			// s1 = t1, s2 > t2
			return ComparisonValue.Greater;


		    case ComparisonValue.Incomparable: 
			// s1 = t1, s2 * t2
			cmpS1T2 = ReductionOrdering.current().compare(s1,t2);
	    
			switch (cmpS1T2)
			    {
			    case ComparisonValue.Smaller: 
				// s1 = t1, s2 * t2, s1 < t2

				cmpS1S2 = 
				    ReductionOrdering.current().compare(s1,s2);
			    
				assert cmpS1S2 != ComparisonValue.Equivalent;
			    
				return 
				    (cmpS1S2 == ComparisonValue.Greater)? 
				    ComparisonValue.Smaller 
				    : 
				    ComparisonValue.Incomparable;

			    
			    case ComparisonValue.Equivalent: 
				// s1 = t1, s2 * t2, s1 = t2
				return ComparisonValue.Greater; 
				// Because {s1,s1,s2,s2} > {s1,s1} = {t1,t2}


			    case ComparisonValue.Greater: 
				// s1 = t1, s2 * t2, s1 > t2
				return ComparisonValue.Greater; 
				// Because {s1,s1,s2,s2} > {s1,s1} > {s1,t2} = {t1,t2}
				

			    case ComparisonValue.Incomparable: 
				// s1 = t1, s2 * t2, s1 * t2
				return ComparisonValue.Incomparable;

			    }; // switch (cmpS1T2)
	    	    
			break; // case ComparisonValue.Incomparable

		    }; // switch (cmpS2T2)
	    
		break; // case ComparisonValue.Equivalent



	    case ComparisonValue.Greater: 
		// s1 > t1
	    
		cmpS1T2 = ReductionOrdering.current().compare(s1,t2);
	    
		switch (cmpS1T2)
		    {
		    case ComparisonValue.Smaller: 
			// s1 > t1, s1 < t2
	    
			cmpS2T2 = ReductionOrdering.current().compare(s2,t2);
		    
			switch (cmpS2T2)
			    {
			    case ComparisonValue.Smaller: 
				// s1 > t1, s1 < t2, s2 < t2
				return ComparisonValue.Smaller;

			    case ComparisonValue.Equivalent: 
				// s1 > t1, s1 < t2, s2 = t2
				return ComparisonValue.Greater;

			    case ComparisonValue.Greater: 
				// s1 > t1, s1 < t2, s2 > t2
				return ComparisonValue.Greater;

			    case ComparisonValue.Incomparable: 
				// s1 > t1, s1 < t2, s2 * t2
			    
				cmpS1S2 = ReductionOrdering.current().compare(s1,s2);

				assert cmpS1S2 != ComparisonValue.Equivalent;
				return 
				    (cmpS1S2 == ComparisonValue.Greater)? 
				    ComparisonValue.Smaller 
				    : 
				    ComparisonValue.Incomparable;

			    }; // switch (cmpS2T2)
		    
			break; // case ComparisonValue.Smaller


		    case ComparisonValue.Equivalent: 
			// s1 > t1, s1 = t2
			return ComparisonValue.Greater;


		    case ComparisonValue.Greater: 
			// s1 > t1, s1 > t2
			return ComparisonValue.Greater;
		    

		    case ComparisonValue.Incomparable: 
			// s1 > t1, s1 * t2

			cmpS2T2 = ReductionOrdering.current().compare(s2,t2);
		    

			switch (cmpS2T2)
			    {
			    case ComparisonValue.Smaller: 
				// s1 > t1, s1 * t2, s2 < t2

				cmpS1S2 = 
				    ReductionOrdering.current().compare(s1,s2);

				assert cmpS1S2 != ComparisonValue.Equivalent;
			    
				return 
				    (cmpS1S2 == ComparisonValue.Smaller)? 
				    ComparisonValue.Greater 
				    : 
				    ComparisonValue.Incomparable;

			    case ComparisonValue.Equivalent: 
				// s1 > t1, s1 * t2, s2 = t2 
				return ComparisonValue.Greater;

			    case ComparisonValue.Greater: 
				// s1 > t1, s1 * t2, s2 > t2
				return ComparisonValue.Greater;

			    case ComparisonValue.Incomparable: 
				// s1 > t1, s1 * t2, s2 * t2
				return ComparisonValue.Incomparable;

			    }; // switch (cmpS2T2)
		    
			break; // case ComparisonValue.Incomparable

		    }; // switch (cmpS1T2)

		break; // case ComparisonValue.Greater


	    case ComparisonValue.Incomparable: 
		// s1 * t1
	    
		cmpS1T2 = 
		    ReductionOrdering.current().compare(s1,t2);

		switch (cmpS1T2)
		    {
		    case ComparisonValue.Smaller: 
			// s1 * t1, s1 < t2

			cmpS2T2 = 
			    ReductionOrdering.current().compare(s2,t2);
		    
			switch (cmpS2T2)
			    {
			    case ComparisonValue.Smaller: 
				// s1 * t1, s1 < t2, s2 < t2
				return ComparisonValue.Smaller;

			    case ComparisonValue.Equivalent: 
				// s1 * t1, s1 < t2, s2 = t2
			    
				cmpS2T1 = ReductionOrdering.current().compare(s2,t1);
			    
				assert cmpS2T1 != ComparisonValue.Equivalent;
				return 
				    (cmpS2T1 == ComparisonValue.Smaller)? 
				    ComparisonValue.Smaller 
				    : 
				    ComparisonValue.Incomparable;

			    case ComparisonValue.Greater: 
				// s1 * t1, s1 < t2, s2 > t2
				cmpS2T1 = ReductionOrdering.current().compare(s2,t1);
				return cmpS2T1;




			    case ComparisonValue.Incomparable: 
				// s1 * t1, s1 < t2, s2 * t2
				cmpS2T1 = ReductionOrdering.current().compare(s2,t1);

			    
				switch (cmpS2T1)
				    {

				    case ComparisonValue.Smaller: 
					// s1 * t1, s1 < t2, s2 * t2, s2 < t1
					return ComparisonValue.Smaller;

				    case ComparisonValue.Equivalent:  // s1 * t1, s1 < t2, s2 * t2, s2 = t1
				    
					cmpS1S2 = 
					    ReductionOrdering.current().compare(s1,s2);
				    
					switch (cmpS1S2)
					    {
					    case ComparisonValue.Smaller: 
						// s1 * t1, s1 < t2, s2 * t2, s2 < t1, s1 < s2
						return ComparisonValue.Incomparable;
					    case ComparisonValue.Equivalent: 
						// s1 * t1, s1 < t2, s2 * t2, s2 < t1, s1 = s2
						return ComparisonValue.Smaller;
					    case ComparisonValue.Greater: 
						// s1 * t1, s1 < t2, s2 * t2, s2 < t1, s1 > s2
						return ComparisonValue.Smaller;
					    case ComparisonValue.Incomparable: 
						// s1 * t1, s1 < t2, s2 * t2, s2 < t1, s1 * s2
						return ComparisonValue.Incomparable;
					    }; // switch (cmpS1S2)


				    
				    case ComparisonValue.Greater: 
					// s1 * t1, s1 < t2, s2 * t2, s2 > t1

					cmpS1S2 = ReductionOrdering.current().compare(s1,s2);
				    
					switch (cmpS1S2)
					    {
					    case ComparisonValue.Smaller: 
						// s1 * t1, s1 < t2, s2 * t2, s2 > t1, s1 < s2
						return ComparisonValue.Greater;
					    case ComparisonValue.Equivalent: 
						// s1 * t1, s1 < t2, s2 * t2, s2 > t1, s1 < s2 
						assert false;
						return ComparisonValue.Incomparable;
					    case ComparisonValue.Greater: 
						// s1 * t1, s1 < t2, s2 * t2, s2 > t1, s1 < s2 
						return ComparisonValue.Greater;
					    case ComparisonValue.Incomparable: 
						// s1 * t1, s1 < t2, s2 * t2, s2 > t1, s1 < s2
						return ComparisonValue.Incomparable;
					    }; // switch (cmpS1S2)
				    				    

				    case ComparisonValue.Incomparable: 
					// s1 * t1, s1 < t2, s2 * t2, s2 * t1
					return ComparisonValue.Incomparable;

				    }; // switch (cmpS2T1)
			    
				break; // case ComparisonValue.Incomparable

			    }; // switch (cmpS1T2)
		    
			break; // case ComparisonValue.Smaller


		    case ComparisonValue.Equivalent: 
			// s1 * t1, s1 = t2

			cmpS2T1 = ReductionOrdering.current().compare(s2,t1);

			switch (cmpS2T1) 
			    {
			    case ComparisonValue.Smaller: 
				// s1 * t1, s1 = t2, s2 < t1
			    
				cmpS1S2 =  ReductionOrdering.current().compare(s1,s2);

				assert cmpS1S2 != ComparisonValue.Equivalent;
				return 
				    (cmpS1S2 == ComparisonValue.Smaller)? 
				    ComparisonValue.Smaller 
				    : 
				    ComparisonValue.Incomparable;

			    case ComparisonValue.Equivalent: 
				// s1 * t1, s1 = t2, s2 = t1
				return ComparisonValue.Greater; 
				// Because {s1,s1,s2,s2} > {s1,s2} = {t1,t2}
 

			    case ComparisonValue.Greater: 
				// s1 * t1, s1 = t2, s2 > t1
				return ComparisonValue.Greater;  
				// Because {s1,s1,s2,s2} > {s1,s2} > {t1,s1} = {t1,t2}
			    
			    case ComparisonValue.Incomparable: 
				// s1 * t1, s1 = t2, s2 * t1
				return ComparisonValue.Incomparable;

			    }; // switch (cmpS2T1) 


		    case ComparisonValue.Greater: 
			// s1 * t1, s1 > t2

			cmpS2T1 = ReductionOrdering.current().compare(s2,t1);
		    
			switch (cmpS2T1)
			    {
			    case ComparisonValue.Smaller: 
				// s1 * t1, s1 > t2, s2 < t1 
			    
				cmpS1S2 = ReductionOrdering.current().compare(s1,s2);
			    
				assert cmpS1S2 != ComparisonValue.Equivalent;
				return 
				    (cmpS1S2 == ComparisonValue.Smaller)? 
				    ComparisonValue.Smaller 
				    : 
				    ComparisonValue.Incomparable;

			    case ComparisonValue.Equivalent: 
				// s1 * t1, s1 > t2, s2 = t1 
				return ComparisonValue.Greater;

			    case ComparisonValue.Greater: 
				// s1 * t1, s1 > t2, s2 > t1 
				return ComparisonValue.Greater;

			    case ComparisonValue.Incomparable: 
				// s1 * t1, s1 > t2, s2 * t1 

				cmpT1T2 = ReductionOrdering.current().compare(t1,t2);
			    
				assert cmpT1T2 != ComparisonValue.Equivalent;
			    
				return 
				    (cmpT1T2 == ComparisonValue.Smaller)? 
				    ComparisonValue.Greater 
				    : 
				    ComparisonValue.Incomparable;

			    }; // switch (cmpS2T1)

			break; // case ComparisonValue.Greater
		    
				   


		    case ComparisonValue.Incomparable: 
			// s1 * t1, s1 * t2
		    
			cmpS2T1 = ReductionOrdering.current().compare(s2,t1);
			    
			switch (cmpS2T1)
			    {
			    case ComparisonValue.Smaller: 
				// s1 * t1, s1 * t2, s2 < t1

				cmpS1S2 = ReductionOrdering.current().compare(s1,s2);
			    
				assert cmpS1S2 != ComparisonValue.Equivalent;
				return 
				    (cmpS1S2 == ComparisonValue.Smaller)? 
				    ComparisonValue.Smaller 
				    : 
				    ComparisonValue.Incomparable;

			    case ComparisonValue.Equivalent: 
				// s1 * t1, s1 * t2, s2 = t1
				return ComparisonValue.Incomparable;

			    case ComparisonValue.Greater: 
				// s1 * t1, s1 * t2, s2 > t1
			    
				cmpS2T2 = ReductionOrdering.current().compare(s2,t2);
			    
				switch (cmpS2T2)
				    {
				
				    case ComparisonValue.Smaller: 
					// s1 * t1, s1 * t2, s2 > t1, s2 < t2

					cmpS1S2 = ReductionOrdering.current().compare(s1,s2);
				    
					assert cmpS1S2 != ComparisonValue.Equivalent;
					return 
					    (cmpS1S2 == ComparisonValue.Smaller)? 
					    ComparisonValue.Smaller 
					    : 
					    ComparisonValue.Incomparable;

				    case ComparisonValue.Equivalent: 
					// s1 * t1, s1 * t2, s2 > t1, s2 = t2
					return ComparisonValue.Greater; 
					// Because {s1,s1,s2,s2} > {s2,s2} > {t1,s2} = {t1,t2}

				    case ComparisonValue.Greater: 
					// s1 * t1, s1 * t2, s2 > t1, s2 < t2
					return ComparisonValue.Greater;

				    case ComparisonValue.Incomparable: 
					// s1 * t1, s1 * t2, s2 > t1, s2 < t2
					return ComparisonValue.Incomparable;

				    }; // switch (cmpS2T2)
			    
				break; // case ComparisonValue.Greater


			    case ComparisonValue.Incomparable: 
				// s1 * t1, s1 * t2, s2 * t1
				cmpS2T2 = ReductionOrdering.current().compare(s2,t2);

				switch (cmpS2T2) 
				    {
				    case ComparisonValue.Smaller: 
					// s1 * t1, s1 * t2, s2 * t1, s2 < t2
				    
					cmpS1S2 = ReductionOrdering.current().compare(s1,s2);
				    
					assert cmpS1S2 != ComparisonValue.Equivalent;
					return 
					    (cmpS1S2 == ComparisonValue.Smaller)? 
					    ComparisonValue.Smaller 
					    : 
					    ComparisonValue.Incomparable;
				    
				    case ComparisonValue.Equivalent: 
					// s1 * t1, s1 * t2, s2 * t1, s2 = t2
					return ComparisonValue.Incomparable;

				    case ComparisonValue.Greater: 
					// s1 * t1, s1 * t2, s2 * t1, s2 > t2
				    
					cmpT1T2 = ReductionOrdering.current().compare(t1,t2);
				    
					assert cmpT1T2 != ComparisonValue.Equivalent;

					return 
					    (cmpT1T2 == ComparisonValue.Smaller)? 
					    ComparisonValue.Greater 
					    : 
					    ComparisonValue.Incomparable;

				    case ComparisonValue.Incomparable: 
					// s1 * t1, s1 * t2, s2 * t1, s2 * t2
					return ComparisonValue.Incomparable;

				    }; // switch (cmpS2T2)

				break; // case ComparisonValue.Incomparable

			    }; // switch (cmpS2T1)

			break; // case ComparisonValue.Incomparable

		    }; // switch (cmpS1T2)
	    
		break; // case ComparisonValue.Incomparable

	    }; // switch (cmpS1T1)

	assert false;

	return ComparisonValue.Incomparable;

    } // compareNegEqWithPosEq(Flatterm atom1,Flatterm atom2)



    //                        Data:

    private static AdmissibleLiteralOrdering  _some = 
	new AdmissibleLiteralOrdering();

} // class AdmissibleLiteralOrdering
