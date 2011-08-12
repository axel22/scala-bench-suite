
public class FDistribution {

	// Lanczos Gamma Function approximation - Coefficients
	private double[] lgfCoeff = { 1.000000000190015, 76.18009172947146, -86.50532032941677, 24.01409824083091, -1.231739572450155, 0.1208650973866179E-2, -0.5395239384953E-5 };

	// Lanczos Gamma Function approximation - small gamma
	private double lgfGamma = 5.0;

	// Lanczos Gamma Function approximation - N (number of coefficients -1)
	int lgfN = 6;

	private double precision = 1e-3;

	public static void main(String[] args) {
		double var = new FDistribution().AFishF(0.01, 1, 2);
		System.out.println(var);
	}

	public double FishF(double f, int n1, int n2) {
		double Pi = Math.PI;
		double PiD2 = Pi / 2;
		double x = n2 / (n1 * f + n2);
		if ((n1 % 2) == 0) {
			return calculateStat(1 - x, n2, n1 + n2 - 4, n2 - 2) * Math.pow(x, n2 / 2.0);
		}
		if ((n2 % 2) == 0) {
			return 1 - calculateStat(x, n1, n1 + n2 - 4, n1 - 2) * Math.pow(1 - x, n1 / 2.0);
		}
		double th = Math.atan(Math.sqrt(n1 * f / n2));
		double a = th / PiD2;
		double sth = Math.sin(th);
		double cth = Math.cos(th);
		if (n2 > 1) {
			a = a + sth * cth * calculateStat(cth * cth, 2, n2 - 3, -1) / PiD2;
		}
		if (n1 == 1) {
			return 1 - a;
		}
		double c = 4 * calculateStat(sth * sth, n2 + 1, n1 + n2 - 4, n2 - 2) * sth * Math.pow(cth, n2) / Pi;
		if (n2 == 1) {
			return 1 - a + c / 2;
		}
		double k = 2;
		while (k <= (n2 - 1) / 2) {
			c = c * k / (k - .5);
			k = k + 1;
		}
		return 1 - a + c;
	}

	public double calculateStat(double q, int i, int j, int b) {
		double zz = 1;
		double z = zz;
		double k = i;
		while (k <= j) {
			zz = zz * q * k / (k - b);
			z = z + zz;
			k = k + 2;
		}
		return z;
	}

	public double AFishF(double p, int n1, int n2) {
		double v = 0.5;
		double dv = 0.5;
		double f = 0;
		while (dv > 1e-10) {
			f = 1 / v - 1;
			dv = dv / 2;
			if (FishF(f, n1, n2) > p) {
				v = v - dv;
			}
			else {
				v = v + dv;
			}
		}
		return f;
	}

	private double FInverse(double probability, int n1, int n2) {

		double upper = 10;
		double lower = 0;
		double mid = 0;
		double fu = 0;
		double fl = 0;

		while (true) {
			fu = FCDF(upper, n1, n2) - probability;
			fl = FCDF(lower, n1, n2) - probability;
			
			if (Math.abs(fu) < precision) {
				return upper;
			}
			System.out.println(fu + probability);
			if (Math.abs(fl) < precision) {
				return lower;
			}
			System.out.println(fl + probability);
			
			mid = (upper + lower) / 2;
			
			if ((FCDF(mid, n1, n2) - probability) * fu < 0) {
				fl = mid;
			}
			else if ((FCDF(mid, n1, n2) - probability) * fl < 0) {
				fu = mid;
			}
			else {
				return 0;
			}
		}
	}

	private double FCDF(double fValue, int n1, int n2) {
		return 1 - regularizedBetaFunction(n1 / 2, n2 / 2, n2 / (n2 + n1 * fValue));
	}

	public double regularizedBetaFunction(double a, double b, double x) {
		if (x < 0.0D || x > 1.0D) {
			throw new IllegalArgumentException("Argument x, " + x + ", must be lie between 0 and 1 (inclusive)");
		}

		double ibeta = 0.0D;

		if (x == 0.0D) {
			ibeta = 0.0D;
		}
		else {
			if (x == 1.0D) {
				ibeta = 1.0D;
			}
			else {
				// Term before continued fraction
				ibeta = Math.exp(logGamma(a + b) - logGamma(a) - logGamma(b) + a * Math.log(x) + b * Math.log(1.0D - x));
				// Continued fraction
				if (x < (a + 1.0D) / (a + b + 2.0D)) {
					ibeta = ibeta * continuedFraction(a, b, x) / a;
				}
				else {
					// Use symmetry relationship
					ibeta = 1.0D - ibeta * continuedFraction(b, a, 1.0D - x) / b;
				}
			}
		}
		return ibeta;
	}

	public double logGamma(double x) {

		double xcopy = x;
		double fg = 0.0D;
		double first = x + lgfGamma + 0.5;
		double second = lgfCoeff[0];

		if (x >= 0.0) {
			first -= (x + 0.5) * Math.log(first);
			for (int i = 1; i <= lgfN; i++) {
				second += lgfCoeff[i] / ++xcopy;
			}
			fg = Math.log(Math.sqrt(2.0 * Math.PI) * second / x) - first;
		}
		else {
			fg = Math.PI / (gamma(1.0D - x) * Math.sin(Math.PI * x));

			if (fg != 1.0 / 0.0 && fg != -1.0 / 0.0) {
				if (fg < 0) {
					throw new IllegalArgumentException("\nThe gamma function is negative");
				}
				else {
					fg = Math.log(fg);
				}
			}
		}
		return fg;
	}

	// Gamma function
	// Lanczos approximation (6 terms)
	// retained for backward compatibity
	public double gamma(double x) {

		double xcopy = x;
		double first = x + lgfGamma + 0.5;
		double second = lgfCoeff[0];
		double fg = 0.0D;

		if (x >= 0.0) {
			first = Math.pow(first, x + 0.5) * Math.exp(-first);
			for (int i = 1; i <= lgfN; i++)
				second += lgfCoeff[i] / ++xcopy;
			fg = first * Math.sqrt(2.0 * Math.PI) * second / x;
		}
		else {
			fg = -Math.PI / (x * gamma(-x) * Math.sin(Math.PI * x));
		}
		return fg;
	}

	// Incomplete fraction summation used in the method regularisedBetaFunction
	public double continuedFraction(double a, double b, double x) {

		final int maxIterative = 500;
		final double tolerance = 1.0e-8;

		// A small number close to the smallest representable floating point
		// number
		final double FPMIN = 1e-300;

		double c = 1.0D;
		double d = 1.0D - (a + b) * x / (a + 1);

		if (Math.abs(d) < FPMIN) {
			d = FPMIN;
		}
		d = 1.0D / d;

		double h = d;
		double aa = 0.0D;
		double del = 0.0D;
		int i = 1, j = 0;
		boolean test = true;

		while (test) {
			j = 2 * i;
			aa = i * (b - i) * x / ((a - 1 + j) * (a + j));
			d = 1.0D + aa * d;
			if (Math.abs(d) < FPMIN) {
				d = FPMIN;
			}
			c = 1.0D + aa / c;
			if (Math.abs(c) < FPMIN) {
				c = FPMIN;
			}
			d = 1.0D / d;
			h *= d * c;
			aa = -(a + i) * (a + b + i) * x / ((a + j) * (a + 1 + j));
			d = 1.0D + aa * d;
			if (Math.abs(d) < FPMIN) {
				d = FPMIN;
			}
			c = 1.0D + aa / c;
			if (Math.abs(c) < FPMIN) {
				c = FPMIN;
			}
			d = 1.0D / d;
			del = d * c;
			h *= del;
			i++;
			if (Math.abs(del - 1.0D) < tolerance) {
				test = false;
			}
			if (i > maxIterative) {
				test = false;
				System.out.println("Maximum number of iterations (" + maxIterative + ") exceeded in continuedFraction in incompleteBeta");
			}
		}
		return h;

	}

}
