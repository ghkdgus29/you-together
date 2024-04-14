package site.youtogether.user.application;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import site.youtogether.IntegrationTestSupport;
import site.youtogether.room.Room;
import site.youtogether.room.infrastructure.RoomStorage;
import site.youtogether.user.Role;
import site.youtogether.user.User;
import site.youtogether.user.dto.UserInfo;
import site.youtogether.user.dto.UserRoleChangeForm;

class UserServiceTest extends IntegrationTestSupport {

	private static final Long HOST_ID = 100L;

	@Autowired
	private UserService userService;

	@Autowired
	private RoomStorage roomStorage;

	@AfterEach
	void clean() {
		roomStorage.deleteAll();
	}

	@Test
	@DisplayName("닉네임을 변경한다")
	void updateNickname() throws Exception {
		// given
		Room room = createRoom(LocalDateTime.of(2024, 4, 11, 4, 8, 0), "황똥땡의 방", "호스트 황똥땡");

		User user = User.builder()
			.userId(1L)
			.nickname("연츠비")
			.role(Role.GUEST)
			.build();
		room.enterParticipant(user, null);
		roomStorage.save(room);

		String updateNickname = "연똥땡";

		// when
		UserInfo userInfo = userService.updateUserNickname(user.getUserId(), updateNickname, room.getCode());

		// then
		assertThat(userInfo.getNickname()).isEqualTo(updateNickname);

		Room savedRoom = roomStorage.findById(room.getCode()).get();
		User participant = savedRoom.getParticipants().get(user.getUserId());
		assertThat(participant.getNickname()).isEqualTo(updateNickname);

		User savedUser = savedRoom.findParticipantBy(user.getUserId());
		assertThat(savedUser.getNickname()).isEqualTo(updateNickname);
	}

	@Test
	@DisplayName("호스트 닉네임을 변경한다")
	void updateHostNickname() throws Exception {
		// given
		String hostNickname = "호스트 황똥땡";
		String updateNickname = "내가 바로 진짜 황똥땡";
		Room room = createRoom(LocalDateTime.of(2024, 4, 11, 4, 8, 0), "황똥땡의 방", hostNickname);

		// when
		UserInfo userInfo = userService.updateUserNickname(HOST_ID, updateNickname, room.getCode());

		// then
		assertThat(userInfo.getNickname()).isEqualTo(updateNickname);

		Room savedRoom = roomStorage.findById(room.getCode()).get();
		User savedUser = savedRoom.findParticipantBy(HOST_ID);
		assertThat(savedUser.getNickname()).isEqualTo(updateNickname);
	}

	@Test
	@DisplayName("특정 유저의 역할을 변경한다")
	void changeUserRole() throws Exception {
		// given
		User user = User.builder()
			.userId(2L)
			.role(Role.GUEST)
			.build();

		Room room = createRoom(LocalDateTime.of(2024, 4, 11, 4, 8, 0), "황똥땡의 방", "황똥땡");
		room.enterParticipant(user, null);
		roomStorage.save(room);

		UserRoleChangeForm userRoleChangeForm = new UserRoleChangeForm(room.getCode(), user.getUserId(), Role.VIEWER);

		// when
		userService.changeUserRole(HOST_ID, userRoleChangeForm);

		// then
		Room savedRoom = roomStorage.findById(room.getCode()).get();
		User changedUser = savedRoom.findParticipantBy(user.getUserId());

		assertThat(changedUser.getUserId()).isEqualTo(user.getUserId());
		assertThat(changedUser.getRole()).isEqualTo(Role.VIEWER);
	}

	private Room createRoom(LocalDateTime createTime, String title, String hostNickname) {
		User user = User.builder()
			.nickname(hostNickname)
			.userId(HOST_ID)
			.role(Role.HOST)
			.build();

		Room room = Room.builder()
			.title(title)
			.host(user)
			.createdAt(createTime)
			.capacity(10)
			.build();

		roomStorage.save(room);

		return room;
	}

}
