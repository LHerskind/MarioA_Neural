package fagprojekt_GANN;

import java.util.Random;

import com.sun.org.apache.bcel.internal.generic.IRETURN;

import ch.idsia.evolution.Evolvable;

public class MultiLayerNeuralNetwork implements Evolvable {

	private final Random random = new Random();

	int hiddenLayers = 1;
	private double[][][] connections;
	private double[][] neuronLayers; // 0 = input , end = output, rest = hidden
	private double learningRate = 0.7;
	private double mutationChange = 3 / 4; // 1 > x > 0
	private double limit = 0.8;
	private boolean twoSides = false; // True = +/-, false = +
	int numberOfInputs;

	public MultiLayerNeuralNetwork(int numberOfInputs, int numberOfOutputs) {
		this.numberOfInputs = numberOfInputs;
		int hiddenNeurons = (numberOfInputs + numberOfOutputs) / 2;
		neuronLayers = new double[1 + hiddenLayers][];
		for (int i = 0; i < hiddenLayers; i++) {
			neuronLayers[i] = new double[hiddenNeurons];
		}
		neuronLayers[hiddenLayers] = new double[numberOfOutputs];

		connections = new double[1 + hiddenLayers][][];
		connections[0] = new double[numberOfInputs][hiddenNeurons];
		for (int i = 1; i < hiddenLayers; i++) {
			connections[i] = new double[hiddenNeurons][hiddenNeurons];
		}
		connections[hiddenLayers] = new double[hiddenNeurons][numberOfOutputs];
		mutate();
	}

	public MultiLayerNeuralNetwork(double[][][] connections) {
		this.connections = connections;
		this.numberOfInputs = connections[0].length;
		neuronLayers = new double[connections.length][];

		for (int i = 0; i < connections.length; i++) {
			neuronLayers[i] = new double[connections[i][0].length];
		}
	}

	@Override
	public MultiLayerNeuralNetwork getNewInstance() {
		return new MultiLayerNeuralNetwork(connections[0].length, connections[hiddenLayers][0].length);
	}

	@Override
	public MultiLayerNeuralNetwork copy() {
		return new MultiLayerNeuralNetwork(copy(connections));
	}
	
	public double[][][] getConnections(){
		return connections;
	}
		
	public double[][][] copy(double[][][] original) {
		double[][][] copy = new double[original.length][][];
		for (int i = 0; i < original.length; i++) {
			copy[i] = new double[original[i].length][];
			for (int j = 0; j < original[i].length; j++) {
				copy[i][j] = new double[original[i][j].length];
				for (int k = 0; k < original[i][j].length; k++) {
					copy[i][j][k] = original[i][j][k];
				}
			}
		}
		return copy;
	}

	@Override
	public void reset() {
		for (int i = 0; i < neuronLayers.length; i++) {
			clearArray(neuronLayers[i]);
		}
	}

	public void mutate() {
		for (int i = 0; i < connections.length; i++) {
			mutate(connections[i]);
		}
	}

	public void mutate(double[][] array) {
		for (int i = 0; i < array.length; i++) {
			mutate(array[i]);
		}
	}

	/**
	 * (2* getRandom() - 1) = -1;1 , getRandom() = 0;1
	 * 
	 * @param array
	 */
	public void mutate(double[] array) {
		for (int i = 0; i < array.length; i++) {
			double randMut = twoSides ? (2 * getRandom() - 1) : getRandom();
			double mutateDegree = getRandom() > mutationChange ? (randMut) * learningRate : 0;
			array[i] += mutateDegree;
		}
	}

	private double getRandom() {
		return random.nextGaussian();
	}

	private void clearArray(double[] array) {
		for (int i = 0; i < array.length; i++) {
			array[i] = 0;
		}
	}

	public boolean[] getOutput(double[] inputs) {
		if (inputs.length != numberOfInputs) {
			System.out.println("ERROR");
		}
		calculateAllSteps(inputs);

		boolean[] output = new boolean[9]; // Husk, der er 9 knapper, vi bruger dog kun 6
		for (int i = 0; i < neuronLayers[hiddenLayers].length; i++) {
			output[i] = (neuronLayers[hiddenLayers][i] > limit);
		}
		return output;
	}

	public void calculateAllSteps(double[] inputs) {
		reset();
		calculateOneStep(inputs, neuronLayers[0], connections[0]);
		bounder(neuronLayers[0]);
		for (int i = 0; i < hiddenLayers; i++) {
			calculateOneStep(neuronLayers[i], neuronLayers[i + 1], connections[i + 1]);
			bounder(neuronLayers[i + 1]);
		}
	}

	public void calculateOneStep(double[] fromLayer, double[] toLayer, double[][] connection) {
		for (int i = 0; i < toLayer.length; i++) {
			for (int j = 0; j < fromLayer.length; j++) {
				double weight = connection[j][i];
				toLayer[i] += weight * fromLayer[j];
			}
		}
	}

	private double sigmoid(double x) {
		return 1 / (1 + Math.exp(-x));
	}

	public void bounder(double[] array) {
		for (int i = 0; i < array.length; i++) {
			array[i] = sigmoid(array[i]);
			// array[i] = Math.tanh(array[i]);
			// array[i] = array[i] > limit ? Math.tanh(array[i]) : 0;
		}
	}

}
