package site.youtogether.room.application;

import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import site.youtogether.room.Room;
import site.youtogether.room.dto.CreatedRoomInfo;
import site.youtogether.room.dto.RoomList;
import site.youtogether.room.dto.RoomSettings;
import site.youtogether.room.infrastructure.RoomStorage;
import site.youtogether.user.Role;
import site.youtogether.user.User;
import site.youtogether.user.infrastructure.UserStorage;
import site.youtogether.user.infrastructure.UserTrackingStorage;
import site.youtogether.util.RandomUtil;

@Service
@RequiredArgsConstructor
public class RoomService {

	private final RoomStorage roomStorage;
	private final UserStorage userStorage;
	private final UserTrackingStorage userTrackingStorage;

	public CreatedRoomInfo create(String cookieValue, RoomSettings roomSettings, LocalDateTime now) {
		Long userId = userTrackingStorage.save(cookieValue);

		User host = User.builder()
			.userId(userId)
			.nickname(RandomUtil.generateUserNickname())
			.role(Role.HOST)
			.build();

		Room room = Room.builder()
			.capacity(roomSettings.getCapacity())
			.title(roomSettings.getTitle())
			.password(roomSettings.getPassword())
			.createdAt(now)
			.host(host)
			.build();

		userStorage.save(host);
		roomStorage.save(room);

		return new CreatedRoomInfo(room, host);
	}

	public RoomList fetchAll(Pageable pageable, String keyword) {
		Slice<Room> roomSlice = roomStorage.findSliceBy(pageable, keyword);
		return new RoomList(roomSlice);
	}

}
