package site.youtogether.room.presentation;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static site.youtogether.exception.ErrorType.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import jakarta.servlet.http.Cookie;
import site.youtogether.RestDocsSupport;
import site.youtogether.exception.room.SingleRoomParticipationViolationException;
import site.youtogether.room.dto.RoomCode;
import site.youtogether.room.dto.RoomSettings;
import site.youtogether.util.api.ResponseResult;

class RoomControllerTest extends RestDocsSupport {

	@Test
	@DisplayName("방 생성 성공")
	void createRoomSuccess() throws Exception {
		// given
		// Setting up request data for creating a room
		RoomSettings roomSettings = RoomSettings.builder()
			.capacity(10)
			.title("재밌는 쇼츠 같이 보기")
			.password(null)
			.build();

		// Setting up response data for the created room
		RoomCode roomCode = new RoomCode("1e7050f7d7");
		given(roomService.create(anyString(), anyString(), any(RoomSettings.class)))
			.willReturn(roomCode);

		// when / then
		String cookieName = cookieProperties.getName();

		mockMvc.perform(post("/rooms")
				.content(objectMapper.writeValueAsString(roomSettings))
				.contentType(MediaType.APPLICATION_JSON))
			.andDo(print())
			.andExpect(status().isCreated())
			.andExpect(cookie().exists(cookieName))
			.andExpect(cookie().domain(cookieName, cookieProperties.getDomain()))
			.andExpect(cookie().path(cookieName, cookieProperties.getPath()))
			.andExpect(cookie().sameSite(cookieName, cookieProperties.getSameSite()))
			.andExpect(cookie().maxAge(cookieName, cookieProperties.getExpiry()))
			.andExpect(cookie().httpOnly(cookieName, true))
			.andExpect(cookie().secure(cookieName, true))
			.andExpect(jsonPath("$.code").value(HttpStatus.CREATED.value()))
			.andExpect(jsonPath("$.status").value(HttpStatus.CREATED.getReasonPhrase()))
			.andExpect(jsonPath("$.result").value(ResponseResult.ROOM_CREATION_SUCCESS.getDescription()))
			.andExpect(jsonPath("$.data.roomCode").value(roomCode.getRoomCode()))
			.andDo(document("create-room-success",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestFields(
					fieldWithPath("capacity").type(JsonFieldType.NUMBER).description("정원"),
					fieldWithPath("title").type(JsonFieldType.STRING).description("제목"),
					fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호").optional()
				),
				responseFields(
					fieldWithPath("code").type(JsonFieldType.NUMBER).description("코드"),
					fieldWithPath("status").type(JsonFieldType.STRING).description("상태"),
					fieldWithPath("result").type(JsonFieldType.STRING).description("결과"),
					fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
					fieldWithPath("data.roomCode").type(JsonFieldType.STRING).description("방 식별 코드")
				)
			));
	}

	@Test
	@DisplayName("방 생성 실패: 요청 데이터 오류가 발생했습니다")
	void createRoomFail_RoomSettingError() throws Exception {
		// given
		// Setting up request data for creating a room
		RoomSettings roomSettings = RoomSettings.builder()
			.capacity(11)
			.title(" ")
			.password("a1b2")
			.build();

		// when / then
		mockMvc.perform(post("/rooms")
				.content(objectMapper.writeValueAsString(roomSettings))
				.contentType(MediaType.APPLICATION_JSON))
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(cookie().doesNotExist(cookieProperties.getName()))
			.andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
			.andExpect(jsonPath("$.result").value(ResponseResult.EXCEPTION_OCCURRED.getDescription()))
			.andExpect(jsonPath("$.data").isArray())
			.andDo(document("create-room-fail-room-setting-error",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestFields(
					fieldWithPath("capacity").type(JsonFieldType.NUMBER).description("정원"),
					fieldWithPath("title").type(JsonFieldType.STRING).description("제목"),
					fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호").optional()
				),
				responseFields(
					fieldWithPath("code").type(JsonFieldType.NUMBER).description("코드"),
					fieldWithPath("status").type(JsonFieldType.STRING).description("상태"),
					fieldWithPath("result").type(JsonFieldType.STRING).description("결과"),
					fieldWithPath("data").type(JsonFieldType.ARRAY).description("응답 데이터"),
					fieldWithPath("data[].type").type(JsonFieldType.STRING).description("오류 타입"),
					fieldWithPath("data[].message").type(JsonFieldType.STRING).description("오류 메시지")
				)
			));
	}

	@Test
	@DisplayName("방 생성 실패: 다수의 방에 참여할 수 없습니다")
	void createRoomFail_SingleRoomParticipantViolation() throws Exception {
		// given
		// Setting up session cookie and request data for creating a room
		// This indicates that a session cookie is already present, implying participation in a room
		Cookie sessionCookie = new Cookie(cookieProperties.getName(), "a85192c998454a1ea055");
		RoomSettings roomSettings = RoomSettings.builder()
			.capacity(10)
			.title("재밌는 쇼츠 같이 보기")
			.password(null)
			.build();

		// when / then
		mockMvc.perform(post("/rooms")
				.content(objectMapper.writeValueAsString(roomSettings))
				.contentType(MediaType.APPLICATION_JSON)
				.cookie(sessionCookie))
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(cookie().doesNotExist(cookieProperties.getName()))
			.andExpect(jsonPath("$.code").value(SINGLE_ROOM_PARTICIPATION_VIOLATION.getStatus().value()))
			.andExpect(jsonPath("$.status").value(SINGLE_ROOM_PARTICIPATION_VIOLATION.getStatus().getReasonPhrase()))
			.andExpect(jsonPath("$.result").value(ResponseResult.EXCEPTION_OCCURRED.getDescription()))
			.andExpect(jsonPath("$.data").isArray())
			.andExpect(jsonPath("$.data[0].type").value(SingleRoomParticipationViolationException.class.getSimpleName()))
			.andExpect(jsonPath("$.data[0].message").value(SINGLE_ROOM_PARTICIPATION_VIOLATION.getMessage()))
			.andDo(document("create-room-fail-single-room-participant-violation",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestFields(
					fieldWithPath("capacity").type(JsonFieldType.NUMBER).description("정원"),
					fieldWithPath("title").type(JsonFieldType.STRING).description("제목"),
					fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호").optional()
				),
				responseFields(
					fieldWithPath("code").type(JsonFieldType.NUMBER).description("코드"),
					fieldWithPath("status").type(JsonFieldType.STRING).description("상태"),
					fieldWithPath("result").type(JsonFieldType.STRING).description("결과"),
					fieldWithPath("data").type(JsonFieldType.ARRAY).description("응답 데이터"),
					fieldWithPath("data[].type").type(JsonFieldType.STRING).description("오류 타입"),
					fieldWithPath("data[].message").type(JsonFieldType.STRING).description("오류 메시지")
				)
			));
	}

	@Test
	@DisplayName("방 나가기 성공")
	void leaveRoomSuccess() throws Exception {
		// given
		// Preparing session cookie and room code for leaving a room
		Cookie sessionCookie = new Cookie(cookieProperties.getName(), "a85192c998454a1ea055");
		String roomCode = "1e7050f7d7";

		// when / then
		String cookieName = cookieProperties.getName();

		mockMvc.perform(delete("/rooms/" + roomCode + "/users")
				.cookie(sessionCookie))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(cookie().exists(cookieName))
			.andExpect(cookie().domain(cookieName, cookieProperties.getDomain()))
			.andExpect(cookie().path(cookieName, cookieProperties.getPath()))
			.andExpect(cookie().sameSite(cookieName, cookieProperties.getSameSite()))
			.andExpect(cookie().maxAge(cookieName, 0))
			.andExpect(cookie().httpOnly(cookieName, true))
			.andExpect(cookie().secure(cookieName, true))
			.andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.status").value(HttpStatus.OK.getReasonPhrase()))
			.andExpect(jsonPath("$.result").value(ResponseResult.ROOM_LEAVE_SUCCESS.getDescription()))
			.andDo(document("leave-room-success",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				responseFields(
					fieldWithPath("code").type(JsonFieldType.NUMBER).description("코드"),
					fieldWithPath("status").type(JsonFieldType.STRING).description("상태"),
					fieldWithPath("result").type(JsonFieldType.STRING).description("결과"),
					fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터")
				)
			));
	}

}
