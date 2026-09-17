package com.app.mybackend.memo.dto;

public record MemoRequest(
        String memoTitle,
        String memoCnnt,
        String memoSort
) {
}
