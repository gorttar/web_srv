/*
 * Copyright (c) 2018 Andrey Antipov. All Rights Reserved.
 */
package cdf;

import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

/**
 * shorthand for {@link DoubleUnaryOperator} created because life is too short to print and read
 * {@link #apply(Double)}, {@link #apply(double)}, {@link #applyAsDouble(Double)} and {@link #applyAsDouble(double)}
 * every time you want to simply apply function to it's argument
 *
 * @author Andrey Antipov (gorttar@gmail.com) (2018-04-13)
 */
@FunctionalInterface
public interface DUO extends
        DoubleUnaryOperator,
        Function<Double, Double>,
        DoubleFunction<Double>,
        ToDoubleFunction<Double> {
    double a(double x);

    @Override
    default double applyAsDouble(double x) {
        return a(x);
    }

    @Override
    default Double apply(double x) {
        return a(x);
    }

    @Override
    default Double apply(Double x) {
        return a(x);
    }

    @Override
    default double applyAsDouble(Double x) {
        return a(x);
    }
}