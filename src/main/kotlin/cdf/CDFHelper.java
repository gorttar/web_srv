/*
 * Copyright (c) 2018 Andrey Antipov. All Rights Reserved.
 */
package cdf;

import static com.google.common.base.Preconditions.checkArgument;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.Map;

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2018-04-14)
 */
public class CDFHelper {
    private CDFHelper() {
    }

    public static CDFProvider cdfProvider(@Nonnull Map<Double, Double> referencePoints) {
        checkArgument(
                referencePoints.size() > 1,
                "There should be at least two reference points but found %s", referencePoints);
        final double[][] points = referencePoints.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(e -> new double[]{e.getKey(), e.getValue()})
                .toArray(double[][]::new);
        checkArgument(Math.abs(points[0][1]) < 1e-6, "Minimal reference point value should be 0 but found %s", points[0][1]);


        throw new AssertionError("Not implemented");
    }
}