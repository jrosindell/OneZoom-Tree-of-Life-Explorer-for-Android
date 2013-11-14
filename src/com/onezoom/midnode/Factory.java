package com.onezoom.midnode;

public interface Factory {
	Visualizer createVisualizer();
	TraitsCaculator createTraitsCaculator();
	Precalculator createPrecalculator();
	PositionData createPositionData();
	PositionCalculator createPositionCalculator();
	Initializer createInitializer();
}
