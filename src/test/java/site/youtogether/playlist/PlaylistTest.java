package site.youtogether.playlist;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import site.youtogether.exception.playlist.InvalidVideoOrderException;
import site.youtogether.exception.playlist.PlaylistEmptyException;

class PlaylistTest {

	@Test
	@DisplayName("재생 목록에 영상을 추가할 수 있다")
	void addVideoSuccess() {
		// given
		Playlist playlist = new Playlist("room code");
		Video video = createVideo("id");

		// when
		playlist.add(video);

		// then
		assertThat(playlist.getVideos()).hasSize(1);
		assertThat(playlist.getVideos().get(0).getVideoId()).isEqualTo("id");
	}

	@Test
	@DisplayName("재생 목록에서 영상을 삭제할 수 있다")
	void deleteVideoSuccess() {
		// given
		Playlist playlist = new Playlist("room code");
		Video video1 = createVideo("id-1");
		Video video2 = createVideo("id-2");

		playlist.add(video1);
		playlist.add(video2);

		// when
		playlist.delete(1);

		// then
		assertThat(playlist.getVideos()).hasSize(1);
		assertThat(playlist.getVideos().get(0).getVideoId()).isEqualTo("id-1");
	}

	@Test
	@DisplayName("영상의 인덱스가 재생 목록의 인덱스를 벗어나면 예외가 발생한다")
	void deleteVideoFail() {
		// given
		Playlist playlist = new Playlist("room code");
		Video video1 = createVideo("id-1");
		Video video2 = createVideo("id-2");

		playlist.add(video1);
		playlist.add(video2);

		// when / then
		assertThatThrownBy(() -> playlist.delete(2))
			.isInstanceOf(InvalidVideoOrderException.class);
	}

	@Test
	@DisplayName("재생 목록의 다음 영상을 재생할 수 있다")
	void playNextVideoSuccess() {
		// given
		Playlist playlist = new Playlist("room code");
		Video video1 = createVideo("id-1");
		Video video2 = createVideo("id-2");

		playlist.add(video1);
		playlist.add(video2);

		// when
		playlist.playNext();

		// then
		assertThat(playlist.getVideos()).hasSize(1);
		assertThat(playlist.getVideos().get(0).getVideoId()).isEqualTo("id-2");
	}

	@Test
	@DisplayName("재생 목록이 비어있는 경우 다음 영상을 재생할 수 없다")
	void playNextVideoFail() {
		// given
		Playlist playlist = new Playlist("room code");

		// when / then
		assertThatThrownBy(() -> playlist.playNext())
			.isInstanceOf(PlaylistEmptyException.class);
	}

	private Video createVideo(String id) {
		return Video.builder()
			.videoId(id)
			.videoTitle("title")
			.channelTitle("channel")
			.duration(10L)
			.thumbnail("thumbnail")
			.build();
	}

}
