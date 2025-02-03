package com.example.realestate.converters;

/**
 * Converter interface
 *
 * @param <S> source entity
 * @param <D> destination entity
 */
public interface Converter<S, D> {
    /**
     * Convert source entity to destination entity.
     *
     * @param source source entity
     * @return destination entity
     */
    D convert(S source);
}
