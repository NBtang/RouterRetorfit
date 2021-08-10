package me.laotang.router;

public interface Call<T> {
    T execute();
    Call<T> clone();
}
