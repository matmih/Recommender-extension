package com.rapidminer.utils;


/**
*Copyright (C) 2010 Steffen Rendle, Zeno Gantner
*Copyright (C) 2011 Zeno Gantner

*This file is originally part of MyMediaLite.

*Ported by Matej Mihelcic (Ruðer Boškoviæ Institute) 25.07.2011
*/

public class Random extends java.util.Random {

	  static final long serialVersionUID=3453434;
		private static Random instance = null;

		/// <summary>Default constructor</summary>
		public Random() { super();}

		/// <summary>Creates a Random object initialized with a seed</summary>
		/// <param name="seed">An integer for initializing the random number generator</param>
		public Random(int seed) { super(seed);}

		/// <summary>Initializes the instance with a given random seed</summary>
		/// <param name="seed">a seed value</param>
		public static void InitInstance(int seed)
		{
			instance = new Random(seed);
		}

		/// <summary>Gets the instance. If it does not exist yet, it will be created.</summary>
		/// <returns>the singleton instance</returns>
		public static Random GetInstance()
		{
			if (instance == null)
				instance = new Random();
			return instance;
		}

		private double sqrt_e_div_2_pi = Math.sqrt(Math.E / (2 * Math.PI));

		/// <summary>Nexts the exp</summary>
		/// <param name="lambda"></param>
		/// <returns></returns>
		public double NextExp(double lambda)
		{
			double u = this.nextDouble();
			return -(1 / lambda) * Math.log1p(- u);
		}

		/// <summary>Get the next number from the standard normal distribution</summary>
		/// <returns>a random number drawn from the standard normal distribution</returns>
		public double NextNormal()
		{
			double y;
			double x;
			do
			{
				double u = this.nextDouble();
				x = this.NextExp(1);
				y = 2 * u * sqrt_e_div_2_pi * Math.exp(-x);
			} while ( y < (2 / (2 * Math.PI)) * Math.exp(-0.5 * x * x));
			if (this.nextDouble() < 0.5)
				return x;
			else
				return -x;
		}

		/// <summary>Draw the next number from a normal distribution</summary>
		/// <param name="mean">mean of the Gaussian</param>
		/// <param name="stdev">standard deviation of the Gaussian</param>
		/// <returns>a random number drawn from a normal distribution with the given mean and standard deviation</returns>
		public double NextNormal(double mean, double stdev)
		{
			return mean + stdev * NextNormal();
		}
	}
	
