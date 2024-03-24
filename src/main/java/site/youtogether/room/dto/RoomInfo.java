package site.youtogether.room.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import site.youtogether.room.Room;

@Getter
@AllArgsConstructor
public class RoomInfo {

	private final String code;
	private final int capacity;
	private final String title;
	private final int currentParticipantsCount;
	private final boolean passwordExist;

	public RoomInfo(Room room) {
		this.code = room.getCode();
		this.capacity = room.getCapacity();
		this.title = room.getTitle();
		this.currentParticipantsCount = room.getParticipants().size();
		this.passwordExist = (room.getPassword() != null);
	}

}
