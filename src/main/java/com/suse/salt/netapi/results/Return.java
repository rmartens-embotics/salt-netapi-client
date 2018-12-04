package com.suse.salt.netapi.results;

import java.util.Optional;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a Salt API result.
 *
 * @param <T> The type of the value this result holds.
 */
public class Return<T> {

    @SerializedName("return")
    private T result;

    @SerializedName("retcode")
    private Optional<Integer> retcode = Optional.empty();

    public Optional<Integer> getRetcode() {
        return this.retcode;
    }

    /**
     * Returns the value of this result.
     *
     * @return The value of this result.
     */
    public T getResult() {
        return result;
    }
}
