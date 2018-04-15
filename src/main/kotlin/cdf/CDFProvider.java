/*
 * Copyright (c) 2018 Andrey Antipov. All Rights Reserved.
 */
package cdf;

import java.util.Map;

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2018-04-14)
 */
@FunctionalInterface
public interface CDFProvider {
    DUO cdf(Map<Double, Double> knownPoints);
}