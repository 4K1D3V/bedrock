package org.slimecraft.bedrock.task;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.slimecraft.bedrock.internal.Bedrock;
import org.slimecraft.bedrock.util.Ticks;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class TaskBuilder {
    private Long delay;
    private Long repeat;
    private Consumer<Task> whenRan;
    private Consumer<Task> whenStopped;
    private long expireAfter;
    private boolean async;
    private BiConsumer<Task, Throwable> whenError;

    public TaskBuilder() {
    }

    public TaskBuilder delay(long delay) {
        this.delay = delay;
        return this;
    }

    public TaskBuilder repeat(long repeat) {
        this.repeat = repeat;
        return this;
    }

    public TaskBuilder whenRan(Consumer<Task> whenRan) {
        this.whenRan = whenRan;
        return this;
    }

    public TaskBuilder whenStopped(Consumer<Task> whenStopped) {
        this.whenStopped = whenStopped;
        return this;
    }

    public TaskBuilder expireAfter(long expireAfter) {
        this.expireAfter = expireAfter;
        return this;
    }

    public TaskBuilder async() {
        this.async = true;
        return this;
    }

    public TaskBuilder whenError(BiConsumer<Task, Throwable> whenError) {
        this.whenError = whenError;
        return this;
    }

    public Task run() {
        return this.fromFields();
    }

    private Task fromFields() {
        final BukkitTask bukkitTask;
        final Task[] mutableTask = new Task[1];
        final Runnable runnable = () -> {
            final Task task = mutableTask[0];
            if (task == null) return;
            if (whenRan != null) {
                if (whenError != null) {
                     try {
                         whenRan.accept(task);
                     } catch (Throwable throwable) {
                         whenError.accept(task, throwable);
                     }
                } else {
                    whenRan.accept(task);
                }
            }
            task.incrementTimesRan();
        };

        if (delay == null && repeat == null) {
            if (!async) {
                bukkitTask = Bukkit.getScheduler().runTask(Bedrock.getPlugin(), runnable);
            } else {
                bukkitTask = Bukkit.getScheduler().runTaskAsynchronously(Bedrock.getPlugin(), runnable);
            }
        } else if (delay != null && repeat == null) {
            if (!async) {
                bukkitTask = Bukkit.getScheduler().runTaskLater(Bedrock.getPlugin(), runnable, delay);
            } else {
                bukkitTask = Bukkit.getScheduler().runTaskLaterAsynchronously(Bedrock.getPlugin(), runnable, delay);
            }
        } else if (delay == null) {
            if (!async) {
                bukkitTask = Bukkit.getScheduler().runTaskTimer(Bedrock.getPlugin(), runnable, Ticks.none(), repeat);
            } else {
                bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(Bedrock.getPlugin(), runnable, Ticks.none(), repeat);
            }
        } else {
            if (!async) {
                bukkitTask = Bukkit.getScheduler().runTaskTimer(Bedrock.getPlugin(), runnable, delay, repeat);
            } else {
                bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(Bedrock.getPlugin(), runnable, delay, repeat);
            }
        }
        mutableTask[0] = new Task(bukkitTask, expireAfter, whenStopped);

        return mutableTask[0];
    }
}
