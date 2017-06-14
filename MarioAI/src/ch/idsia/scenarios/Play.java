/*
s * Copsyright (c) 2009-2010, Sergey Karakovskiy and Julian Togelius
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Mario AI nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package ch.idsia.scenarios;

import java.util.ArrayList;
import java.util.Random;

import ch.idsia.benchmark.mario.engine.GlobalOptions;

import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.benchmark.tasks.MarioCustomSystemOfValues;
import ch.idsia.tools.MarioAIOptions;

/**
* Created by IntelliJ IDEA.
* User: julian
* Date: May 5, 2009
* Time: 12:46:43 PM
*/

/**
 * The <code>Play</code> class shows how simple is to run a MarioAI Benchmark.
 * It shows how to set up some parameters, create a task, use the
 * CmdLineParameters class to set up options from command line if any. Defaults
 * are used otherwise.
 *
 * @author Julian Togelius, Sergey Karakovskiy
 * @version 1.0, May 5, 2009
 */

public final class Play {
	/**
	 * <p>
	 * An entry point of the class.
	 * </p>
	 *
	 * @param args
	 *            input parameters for customization of the benchmark.
	 * @see ch.idsia.scenarios.oldscenarios.MainRun
	 * @see ch.idsia.tools.MarioAIOptions
	 * @see ch.idsia.benchmark.mario.simulation.SimulationOptions
	 * @since MarioAI-0.1
	 */

	static boolean all = false;
	static boolean visualize = !all;
	static boolean enemies = true;
	static int amountOfMaps = 500;

	public static void main(String[] args) {

		if (all) {
			for (int i = 12; i < 16; i++) {
				manyMaps(amountOfMaps, i);
			}
			System.exit(0);
		} else {
			final MarioAIOptions marioAIOptions = new MarioAIOptions(args);
			marioAIOptions.setFPS(24);
			final BasicTask basicTask = new BasicTask(marioAIOptions);
			GlobalOptions.changeScale2x();
			marioAIOptions.setVisualization(true);

			marioAIOptions.setLevelDifficulty(10);
			if (!enemies) {
				marioAIOptions.setEnemies("off");
			}

			int seed = new Random().nextInt(400);
			System.out.println(seed);


			marioAIOptions.setLevelRandSeed(6);

			final MarioCustomSystemOfValues m = new MarioCustomSystemOfValues();
			basicTask.doEpisodes(1, false, 1);
			System.out.println("\nEvaluationInfo: \n" + basicTask.getEnvironment().getEvaluationInfoAsString());
			System.out.println(
					"\nCustom : \n" + basicTask.getEnvironment().getEvaluationInfo().computeWeightedFitness(m));
			System.exit(0);
		}
	}

	public static void manyMaps(int howMany, int difficulty) {
		int lost = 0;
		ArrayList<Integer> listOfLost = new ArrayList<>();
		for (int i = 0; i < howMany; i++) {
			final MarioAIOptions marioAIOptions = new MarioAIOptions();
			marioAIOptions.setVisualization(visualize);
			if (marioAIOptions.isVisualization()) {
				marioAIOptions.setFPS(24);
				if (!GlobalOptions.isScale2x) {
					GlobalOptions.changeScale2x();
				}
			}
			final BasicTask basicTask = new BasicTask(marioAIOptions);
			if (!enemies) {
				marioAIOptions.setEnemies("off");
			}
			marioAIOptions.setLevelDifficulty(difficulty);
			marioAIOptions.setLevelRandSeed(i);
			basicTask.runSingleEpisode(1);
			if (basicTask != null && basicTask.getEvaluationInfo() != null) {
				if (basicTask.getEvaluationInfo().marioStatus != Mario.STATUS_WIN) {
					if (basicTask.getEvaluationInfo().distancePassedCells < basicTask.getEvaluationInfo().levelLength) {
						listOfLost.add(i);
						lost++;
					}
				}
			}
			if (i > 0 && i % 25 == 0) {
				System.out.print(".");
			}
		}
		lost = 0;
		ArrayList<Integer> listOfLost2 = new ArrayList<>();
		for (Integer i : listOfLost) {
			final MarioAIOptions marioAIOptions = new MarioAIOptions();
			marioAIOptions.setVisualization(visualize);
			if (marioAIOptions.isVisualization()) {
				marioAIOptions.setFPS(24);
				if (!GlobalOptions.isScale2x) {
					GlobalOptions.changeScale2x();
				}
			}
			final BasicTask basicTask = new BasicTask(marioAIOptions);
			if (!enemies) {
				marioAIOptions.setEnemies("off");
			}
			marioAIOptions.setLevelDifficulty(difficulty);
			marioAIOptions.setLevelRandSeed(i);
			basicTask.runSingleEpisode(1);
			if (basicTask != null && basicTask.getEvaluationInfo() != null) {
				if (basicTask.getEvaluationInfo().marioStatus != Mario.STATUS_WIN) {
					if (basicTask.getEvaluationInfo().distancePassedCells < basicTask.getEvaluationInfo().levelLength) {
						listOfLost2.add(i);
						lost++;
					}
				}
			}
		}
		lost = 0;
		System.out.println("Difficulty: " + difficulty);
		for (Integer i : listOfLost2) {
			final MarioAIOptions marioAIOptions = new MarioAIOptions();
			marioAIOptions.setVisualization(true);
			marioAIOptions.setFPS(24);
			final BasicTask basicTask = new BasicTask(marioAIOptions);
			if (!GlobalOptions.isScale2x) {
				GlobalOptions.changeScale2x();
			}
			if (!enemies) {
				marioAIOptions.setEnemies("off");
			}
			marioAIOptions.setLevelDifficulty(difficulty);
			marioAIOptions.setLevelRandSeed(i);
			basicTask.runSingleEpisode(1);
			if (basicTask != null && basicTask.getEvaluationInfo() != null) {
				if (basicTask.getEvaluationInfo().marioStatus != Mario.STATUS_WIN) {
					if (basicTask.getEvaluationInfo().distancePassedCells < basicTask.getEvaluationInfo().levelLength) {
						System.out.println("LOST: " + i + " " + basicTask.getEvaluationInfo().Memo);
						lost++;
					}
				}
			}
		}
		System.out.println(
				"Done, won: " + (amountOfMaps - lost) + " = " + (100 - ((double) lost / amountOfMaps) * 100) + "%");
		System.out.println();
	}

}
