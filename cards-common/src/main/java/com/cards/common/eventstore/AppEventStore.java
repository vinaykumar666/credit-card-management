package com.cards.common.eventstore;

/**
 * Persists application footfall / lifecycle events for audit and product analytics.
 */
public interface AppEventStore {

    void record(AppEventRecord event);
}
