package br.com.yuri.alpha7.application;

public interface UnitOfWork {
    void execute(Runnable action);
}
