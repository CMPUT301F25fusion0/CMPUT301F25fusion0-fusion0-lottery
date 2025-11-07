package com.example.fusion0_lottery;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

public class TaskUtil {

    // Returns a successful Task (for mocking success)
    public static <T> Task<T> successTask(T result) {
        return Tasks.forResult(result);
    }

    // Returns a failed Task (for mocking failure)
    public static <T> Task<T> failureTask(Exception e) {
        return Tasks.forException(e);
    }
}
