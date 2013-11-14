package com.onezoom.midnode.displayBinary;

import com.onezoom.midnode.Factory;
import com.onezoom.midnode.Initializer;
import com.onezoom.midnode.PositionCalculator;
import com.onezoom.midnode.PositionData;
import com.onezoom.midnode.Precalculator;
import com.onezoom.midnode.TraitsCaculator;
import com.onezoom.midnode.Visualizer;

public class BinaryFactory implements Factory {

	@Override
	public Visualizer createVisualizer() {
		return new BinaryVisualizer();
	}

	@Override
	public TraitsCaculator createTraitsCaculator() {
		return new BinaryTraitsCalculator();
	}

	@Override
	public Precalculator createPrecalculator() {
		return new BinaryPrecalculator();
	}

	@Override
	public PositionData createPositionData() {
		return new BinaryPositionData();
	}

	@Override
	public PositionCalculator createPositionCalculator() {
		return new BinaryPositionCalculator();
	}

	@Override
	public Initializer createInitializer() {
		return new BinaryInitializer();
	}

}
