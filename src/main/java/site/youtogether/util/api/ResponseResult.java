package site.youtogether.util.api;

import lombok.Getter;

@Getter
public enum ResponseResult {

	// Common
	EXCEPTION_OCCURRED("예외가 발생했습니다"),

	// Room
	ROOM_CREATION_SUCCESS("방 생성에 성공했습니다"),
	ROOM_LIST_FETCH_SUCCESS("방 목록 조회에 성공했습니다"),
	ROOM_ENTER_SUCCESS("방 입장에 성공했습니다"),

	// User
	USER_NICKNAME_FETCH_SUCCESS("유저 닉네임 조회에 성공했습니다"),
	USER_NICKNAME_UPDATE_SUCCESS("유저 닉네임 변경에 성공했습니다");

	private final String description;

	ResponseResult(String description) {
		this.description = description;
	}

}
