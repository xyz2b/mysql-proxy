package org.xyz.dbproxy.infra.state.instance;

import java.util.Collections;
import java.util.Deque;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Instance state context.
 * 通过栈的操作，来实现状态转换
 */
public final class InstanceStateContext {

    private final Deque<InstanceState> currentState = new ConcurrentLinkedDeque<>(Collections.singleton(InstanceState.OK));

    /**
     * Switch state.
     *
     * @param state state
     * @param on true if state is valid, false if not
     */
    public void switchState(final InstanceState state, final boolean on) {
        if (on) {
            currentState.push(state);
        } else {
            if (getCurrentState().equals(state)) {
                recoverState();
            }
        }
    }

    private void recoverState() {
        currentState.pop();
    }

    /**
     * Get current state.
     *
     * @return current state
     */
    public InstanceState getCurrentState() {
        // 如果栈顶元素不为空，则返回栈顶元素，如果栈顶元素为空，则返回InstanceState.OK
        return Optional.ofNullable(currentState.peek()).orElse(InstanceState.OK);
    }
}
