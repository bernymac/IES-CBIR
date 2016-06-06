/**
 *    Copyright 2015 João Rodrigues

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package NovaSYS.IES_CBIR.utils.OPE;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Random;


public class HGDist {

	private static final BigDecimal HALF = new BigDecimal("0.5");
	private static final BigDecimal QUARTER = new BigDecimal("0.25");
	private static final BigDecimal ZERO = BigDecimal.ZERO;
	private static final BigDecimal ONE = BigDecimal.ONE;
	private static final BigDecimal TWO = new BigDecimal("2");
	private static final BigDecimal THREE = new BigDecimal("3");
	private static final BigDecimal SEVEN = new BigDecimal("7");
	private static final BigDecimal HUNDRED = new BigDecimal("100");
	private static final BigDecimal FIFTY = new BigDecimal("50");
	
	private static MathContext MCprecision;

	public static BigDecimal AFC(final BigDecimal I)
	{
		/*
		 * FUNCTION TO EVALUATE LOGARITHM OF THE FACTORIAL I
		 * IF (I .GT. 7), USE STIRLING'S APPROXIMATION
		 * OTHERWISE,  USE TABLE LOOKUP
		 */
		double AL[] =
				new double[]{ 0.0, 0.0, 0.6931471806, 1.791759469, 3.178053830, 4.787491743,
				6.579251212, 8.525161361 };

		if (I.compareTo(SEVEN) <= 0) {
			int vPos = (int) Math.round(I.doubleValue());
			return new BigDecimal(AL[vPos]);
		} else {
			BigDecimal LL = log(I);
			return I.add(HALF).multiply(LL).subtract(I).add(new BigDecimal("0.399089934"));
		}
	}

	public static BigDecimal RAND(Random prng, int precision) {
		return newRandomBigDecimal(prng, precision);
	}

	public static BigInteger HGD(final BigInteger KK, final BigInteger NN1, final BigInteger NN2, Random prng) {
		/*
		 * XXX
		 * NTL is single-threaded by design: there is a global precision
		 * setting, which gets switched back and forth all over the place
		 * (see NTL's RR.c).  We should hold a lock around any BigDecimal usage,
		 * or re-implement the relevant parts of NTL::RR with a scoped
		 * precision parameter..
		 */
		int precision = (NN1.add(NN2).add(KK)).bitLength() + 10;
		MCprecision = new MathContext(precision);

		BigDecimal JX;      // the result
		BigDecimal TN, N1, N2, K;
		BigDecimal P, U, V, A, IX, XL, XR, M;
		BigDecimal KL, KR, LAMDL, LAMDR, NK, NM, P1, P2, P3;

		boolean REJECT;
		BigDecimal MINJX, MAXJX;

		double CON = 57.56462733;
		double DELTAL = 0.0078;
		double DELTAU = 0.0034;
		double SCALE = 1.0e25;
		
		/*
		 * CHECK PARAMETER VALIDITY
		 * (NN1 < 0) || (NN2 < 0) || (KK < 0) || (KK > NN1 + NN2)
		 */
		if (
				(NN1.compareTo(BigInteger.ZERO) < 0) ||
				(NN2.compareTo(BigInteger.ZERO) < 0) ||
				(KK.compareTo(BigInteger.ZERO) < 0) ||
				(KK.compareTo(NN1.add(NN2)) > 0)
				) {
			System.err.println("ERROR -> false");
			return null;
		}

		/*
		 * INITIALIZE
		 */
		REJECT = true;

		if (NN1.compareTo(NN2) >= 0) {
			N1 = new BigDecimal(NN2);
			N2 = new BigDecimal(NN1);
		} else {
			N1 = new BigDecimal(NN1);
			N2 = new BigDecimal(NN2);
		}

		TN = N1.add(N2);
		
		if (new BigDecimal(KK.add(KK)).compareTo(TN) >= 0)  {
			K = TN.subtract(new BigDecimal(KK));
		} else {
			K = new BigDecimal(KK);
		}

		M = (K.add(ONE)).multiply(N1.add(ONE)).divide(TN.add(TWO), MCprecision);

		if (K.subtract(N2).compareTo(ZERO) < 0) {
			MINJX = ZERO;
		} else {
			MINJX = K.subtract(N2);
		}

		if (N1.compareTo(K) < 0) {
			MAXJX = N1;
		} else {
			MAXJX = K;
		}
		
		/*
		 * GENERATE RANDOM VARIATE
		 */
		if (MINJX == MAXJX)  {
			/*
			 * ...DEGENERATE DISTRIBUTION...
			 */
			IX = MAXJX;
		} else if (M.subtract(MINJX).compareTo(BigDecimal.TEN) < 0) {
			/*
			 * ...INVERSE TRANSFORMATION...
			 * Shouldn't really happen in OPE because M will be on the order of N1.
			 * In practice, this does get invoked.
			 */
			BigDecimal W;
			if (K.compareTo(N2) < 0) {
				W = BigFunctions.exp((
						new BigDecimal(CON).add(AFC(N2))).add(AFC(N1.add(N2).subtract(K))).subtract(AFC(N2.subtract(K))).subtract(AFC(N1.add(N2))),
						precision);
			} else {
				W = BigFunctions.exp(
						new BigDecimal(CON).add(AFC(N1)).add(AFC(K)).subtract(AFC(K.subtract(N2))).subtract(AFC(N1.add(N2))),
						precision
						);
			}
			boolean exit10 = false;
			
			do { //label10
				P  = W;
				IX = MINJX;
				U  = RAND(prng, precision).multiply(new BigDecimal(SCALE));

				boolean exit20 = false;

				do { //label20
					if (U.compareTo(P) > 0) {
						U  = U.subtract(P);
						P  = P.multiply((N1.subtract(IX)).multiply(K.subtract(IX)));
						IX = IX.add(ONE);
						P  = P.divide(IX, MCprecision).divide((N2.subtract(K).add(IX)), MCprecision);
						if (IX.compareTo(MAXJX) > 0)
							exit20 = true;
					} else {
						exit10 = true;
						exit20 = true;
					}
				} while(!exit20);
			} while(!exit10);
		} else {
			/*
			 * ...H2PE...
			 */
			BigDecimal S = sqrtNewtonRaphson(
					(TN.subtract(K)).multiply(K).multiply(N1).multiply(N2).divide((TN.subtract(ONE)), MCprecision).divide(TN, MCprecision).divide(TN, MCprecision),
					ONE,
					new BigDecimal(precision)
					);
			
			/*
			 * ...REMARK:  D IS DEFINED IN REFERENCE WITHOUT INT.
			 * THE TRUNCATION CENTERS THE CELL BOUNDARIES AT 0.5
			 */
			BigDecimal D = (new BigDecimal("1.5").multiply(S)).setScale(0,BigDecimal.ROUND_DOWN).add(HALF);
			XL = (M.subtract(D).add(HALF)).setScale(0,BigDecimal.ROUND_DOWN);
			XR = (M.add(D).add(HALF)).setScale(0,BigDecimal.ROUND_DOWN);
			A = AFC(M).add(AFC(N1.subtract(M))).add(AFC(K.subtract(M))).add(AFC(N2.subtract(K).add(M)));
			
			BigDecimal Z1 = AFC(XL);
			BigDecimal Z2 = AFC(N1.subtract(XL));
			BigDecimal Z3 = AFC(K.subtract(XL));
			BigDecimal Z4 = AFC(N2.subtract(K).add(XL));
			
			BigDecimal expon = A.subtract( Z1 ).subtract( Z2 ).subtract( Z3 ).subtract( Z4 );
			
			KL =  BigFunctions.exp(expon, precision);
			KR = BigFunctions.exp(
					A.subtract(AFC(XR.subtract(ONE))).subtract(AFC(N1.subtract(XR).add(ONE))).subtract(AFC(K.subtract(XR).add(ONE))).subtract(AFC(N2.subtract(K).add(XR).subtract(ONE))),
					precision
					);
			LAMDL = log(
					XL.multiply(N2.subtract(K).add(XL)).divide(N1.subtract(XL).add(ONE), MCprecision).divide(K.subtract(XL).add(ONE), MCprecision)
					).negate();
			LAMDR = log(
					(N1.subtract(XR).add(ONE)).multiply(K.subtract(XR).add(ONE)).divide(XR, MCprecision).divide(N2.subtract(K).add(XR), MCprecision)					).negate();
			P1 = TWO.multiply(D);
			P2 = KL.divide(LAMDL, MCprecision).add(P1);
			P3 = KR.divide(LAMDR, MCprecision).add(P2);
			

			boolean exit30_1 = false;
			do { //label30_2
				do { //label30_1
					U = RAND(prng, precision).multiply(P3);
					V = RAND(prng, precision);

					if (U.compareTo(P1) < 0)  {
						/* ...RECTANGULAR REGION... */
						IX    = XL.add(U);
						exit30_1 = true;
					} else if  (U.compareTo(P2) <=0 )  {
						/* ...LEFT TAIL... */
						IX = log(V).divide(LAMDL, MCprecision).add(XL);
						if (IX.compareTo(MINJX) < 0) {
							continue; //label30
						} else {
							V = V.multiply(U.subtract(P1)).multiply(LAMDL);
							exit30_1 = true;
						}
					} else  {
						/* ...RIGHT TAIL... */
						IX = XR.subtract(log(V).divide(LAMDR, MCprecision));
						if (IX.compareTo(MAXJX) > 0)  {
							continue; //label30
						} else {
							V = V.multiply(U.subtract(P2)).multiply(LAMDR);
							exit30_1 = true;
						}
					}

				} while(!exit30_1);

				/*
				 * ...ACCEPTANCE/REJECTION TEST...
				 */
				BigDecimal F;
				if ((M.compareTo(HUNDRED) < 0) || (IX.compareTo(FIFTY) <= 0))  {
					/* ...EXPLICIT EVALUATION... */
					F = ONE;
					if (M.compareTo(IX) < 0) {
						for (BigDecimal I = M.add(ONE); I.compareTo(IX) < 0; I = I.add(ONE)) {
							/*40*/ F = F.multiply(N1.subtract(I).add(ONE)).multiply(K.subtract(I).add(ONE)).divide(N2.subtract(K).add(I), MCprecision).divide(I, MCprecision);
						}
					} else if (M.compareTo(IX) > 0) {
						for (BigDecimal I = IX.add(ONE); I.compareTo(M) < 0; I = I.add(ONE)) {
							/*50*/ F = F.multiply(I).multiply(N2.subtract(K).add(I)).divide(N1.subtract(I), MCprecision).divide(K.subtract(I), MCprecision);
						}
					}
					if (V.compareTo(F) <= 0)  {
						REJECT = false;
					}
				} else {
					/* ...SQUEEZE USING UPPER AND LOWER BOUNDS... */

					BigDecimal Y   = IX;
					BigDecimal Y1  = Y.add(ONE);
					BigDecimal YM  = Y.subtract(M);
					BigDecimal YN  = N1.subtract(Y).add(ONE);
					BigDecimal YK  = K.subtract(Y).add(ONE);
					NK     = N2.subtract(K).add(Y1);
					BigDecimal R   = YM.negate().divide(Y1, MCprecision);
					S      = YM.divide(YN, MCprecision);
					BigDecimal T   = YM.divide(YK, MCprecision);
					BigDecimal E   = YM.negate().divide(NK, MCprecision);
					BigDecimal G   = YN.multiply(YK).divide(Y1.multiply(NK), MCprecision).subtract(ONE);
					BigDecimal DG  = ONE;
					if (G.compareTo(ZERO) < 0)  { DG = ONE.add(G); }
					BigDecimal GU  = G.multiply(ONE.add(G.multiply(G.multiply(THREE).subtract(HALF))));
					BigDecimal GL  = GU.subtract(QUARTER.multiply(sqr(sqr(G)).divide(DG, MCprecision)));
					BigDecimal XM  = M.add(HALF);
					BigDecimal XN  = N1.subtract(M).add(HALF);
					BigDecimal XK  = K.subtract(M).add(HALF);
					NM     = N2.subtract(K).add(XM);
					BigDecimal UB  = Y.multiply(GU).subtract(M.multiply(GL)).add(new BigDecimal(DELTAU)).add(
							XM.multiply(R).multiply(R.multiply(R.divide(THREE, MCprecision).subtract(HALF)).add(ONE))).add(
							XN.multiply(S).multiply(S.multiply(S.divide(THREE, MCprecision).subtract(HALF)).add(ONE))).add(
							XK.multiply(T).multiply(T.multiply(T.divide(THREE, MCprecision).subtract(HALF)).add(ONE))).add(
							NM.multiply(E).multiply(E.multiply(E.divide(THREE, MCprecision).subtract(HALF)).add(ONE)));

					/* ...TEST AGAINST UPPER BOUND... */

					BigDecimal ALV = log(V);
					if (ALV.compareTo(UB) > 0) {
						REJECT = true;
					} else {
						/* ...TEST AGAINST LOWER BOUND... */

						BigDecimal DR = XM.multiply(sqr(sqr(R)));
						if (R.compareTo(ZERO) < 0) {
							DR = DR.divide(ONE.add(R), MCprecision);
						}
						BigDecimal DS = XN.multiply(sqr(sqr(S)));
						if (S.compareTo(ZERO) < 0) {
							DS = DS.divide(ONE.add(S), MCprecision);
						}
						BigDecimal DT = XK.multiply(sqr(sqr(T)));
						if (T.compareTo(ZERO) < 0) {
							DT = DT.divide(ONE.add(T), MCprecision);
						}
						BigDecimal DE = NM.multiply(sqr(sqr(E)));
						if (E.compareTo(ZERO) < 0) {
							DE = DE.divide(ONE.add(E), MCprecision);
						}
						if (ALV.compareTo(UB.subtract(QUARTER.multiply(DR.add(DS).add(DT).add(DE))).add((Y.add(M)).multiply(GL.subtract(GU))).subtract(new BigDecimal(DELTAL))) < 0) {
							REJECT = false;
						} else {
							/* ...STIRLING'S FORMULA TO MACHINE ACCURACY... */

							if (ALV.compareTo(
									(A.subtract(AFC(IX)).subtract(
											AFC(N1.subtract(IX))).subtract(AFC(K.subtract(IX))).subtract(AFC(N2.subtract(K).subtract(IX))))) <= 0) {
								REJECT = false;
							} else {
								REJECT = true;
							}
						}
					}
				}
			} while(REJECT);
		}

		/*
		 * RETURN APPROPRIATE VARIATE
		 */

		if (KK.add(KK).compareTo(TN.toBigInteger()) >= 0) {
			if (NN1.compareTo(NN2) > 0) {
				IX = new BigDecimal(KK.subtract(NN2)).add(IX);
			} else {
				IX = new BigDecimal(NN1).subtract(IX);
			}
		} else {
			if (NN1.compareTo(NN2) > 0) {
				IX = new BigDecimal(KK).subtract(IX);
			}
		}
		JX = IX;
		return JX.toBigInteger();
	}
	
	public static BigDecimal log(BigDecimal x) {
		return BigFunctions.ln(x, MCprecision.getPrecision());
	}
	
	/**
	 * NATURAL log.  (Log base e)
	 * @param bd
	 * @return Natural log of bd
	 */
	public static BigDecimal logDouble(BigDecimal bd) {
		double d = bd.doubleValue();
		double result = 0.0;
		result = Math.log(d);
		if(Double.isNaN(result) || Double.isInfinite(result))
			return new BigDecimal("0");
		BigDecimal ret = new BigDecimal(Math.log(d));
		return ret.setScale(bd.scale(), BigDecimal.ROUND_HALF_EVEN);
	}

	private static BigDecimal newRandomBigDecimal(Random r, int precision) {
		BigInteger n = BigInteger.TEN.pow(precision);
		return new BigDecimal(newRandomBigInteger(n, r), precision);
	}

	private static BigInteger newRandomBigInteger(BigInteger n, Random rnd) {
		BigInteger r;
		do {
			r = new BigInteger(n.bitLength(), rnd);
		} while (r.compareTo(n) >= 0);

		return r;
	}

	private static final BigDecimal SQRT_DIG = new BigDecimal(150);
	private static final BigDecimal SQRT_PRE = new BigDecimal(10).pow(SQRT_DIG.intValue());

	/**
	 * Private utility method used to compute the square root of a BigDecimal.
	 * 
	 * @author Luciano Culacciatti 
	 * @url http://www.codeproject.com/Tips/257031/Implementing-SqrtRoot-in-BigDecimal
	 */
	private static BigDecimal sqrtNewtonRaphson(BigDecimal c, BigDecimal xn, BigDecimal precision){
		BigDecimal fx = xn.pow(2).add(c.negate());
		BigDecimal fpx = xn.multiply(new BigDecimal(2));
		BigDecimal xn1 = fx.divide(fpx,2*SQRT_DIG.intValue(),BigDecimal.ROUND_HALF_DOWN);
		xn1 = xn.add(xn1.negate());
		BigDecimal currentSquare = xn1.pow(2);
		BigDecimal currentPrecision = currentSquare.subtract(c);
		currentPrecision = currentPrecision.abs();
		if (currentPrecision.compareTo(precision) <= -1){
			return xn1;
		}
		return sqrtNewtonRaphson(c, xn1, precision);
	}

	/**
	 * Uses Newton Raphson to compute the square root of a BigDecimal.
	 * 
	 * @author Luciano Culacciatti 
	 * @url http://www.codeproject.com/Tips/257031/Implementing-SqrtRoot-in-BigDecimal
	 */
	public static BigDecimal bigSqrt(BigDecimal c){
		return sqrtNewtonRaphson(c,new BigDecimal(1),new BigDecimal(1).divide(SQRT_PRE));
	}
	
	public static BigDecimal sqr(BigDecimal c){
		return c.multiply(c);
	}

}

