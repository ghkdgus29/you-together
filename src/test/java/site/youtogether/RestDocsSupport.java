package site.youtogether;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import site.youtogether.config.PropertiesConfig;
import site.youtogether.config.property.CookieProperties;
import site.youtogether.room.application.RoomService;
import site.youtogether.room.presentation.RoomController;

@WebMvcTest(controllers = {
	RoomController.class
})
@AutoConfigureRestDocs
@Import(PropertiesConfig.class)
public abstract class RestDocsSupport {

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected ObjectMapper objectMapper;

	@Autowired
	protected CookieProperties cookieProperties;

	@MockBean
	protected RoomService roomService;

}