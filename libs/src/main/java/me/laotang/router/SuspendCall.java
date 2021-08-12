package me.laotang.router;

import kotlinx.coroutines.CancellableContinuation;

public interface SuspendCall<T> {
    void execute(CancellableContinuation<T> continuation);
}
