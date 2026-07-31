package com.cards.common.channel;

/**
 * Thread-local store for the current request's {@link ChannelClientContext}.
 * Filters set the context at the start of a request and clear it at the end so tenant ids are available deeper in the call stack.
 */
public final class ChannelClientHolder {

    private static final ThreadLocal<ChannelClientContext> CONTEXT = new ThreadLocal<>();

    private ChannelClientHolder() {
    }

    /**
     * Stores the channel/client context for the current thread (request).
     *
     * @param context tenant identity for this request; may be null
     */
    public static void set(ChannelClientContext context) {
        CONTEXT.set(context);
    }

    /**
     * Returns the channel/client context for the current thread, if any.
     *
     * @return the stored context, or null when none was set
     */
    public static ChannelClientContext get() {
        return CONTEXT.get();
    }

    /**
     * Removes the context from the current thread to avoid leaks after the request ends.
     */
    public static void clear() {
        CONTEXT.remove();
    }
}
