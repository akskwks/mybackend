package com.app.mybackend.memo.dto;

/**
*
* 클래스명 : MemoRequest
* 역할 : 메모 등록 및 수정에 필요한 입력 데이터를 전달하는 요청 DTO
* 생성일 : 2026-09-17
*
**/
public record MemoRequest(
        String memoTitle,
        String memoCnnt,
        String memoSort
) {
}
