package edu.ucmo.mathcs.onetimepin;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@WebMvcTest(value = PinController.class, secure = false)
public class PinControllerTests {
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private PinRepository repository;
	
	@Test
	public void testGeneratePinWithNoAccount() throws Exception {
		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.post("/api/generate")
				.accept(MediaType.APPLICATION_JSON);
		
		MvcResult result = mockMvc.perform(requestBuilder).andReturn();
		
		String expected = "{\"error\":\"account is required\"}";
		
		JSONAssert.assertEquals(expected, result.getResponse().getContentAsString(), false);
	}
	
	@Test
	public void testGeneratePinWithInvalidAccount() throws Exception {
		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.post("/api/generate?account=")
				.accept(MediaType.APPLICATION_JSON);
		
		MvcResult result = mockMvc.perform(requestBuilder).andReturn();
		
		String expected = "{\"error\":\"invalid account\"}";
		
		JSONAssert.assertEquals(expected, result.getResponse().getContentAsString(), false);
	}
	
	@Test
	public void testGeneratePinWithValidAccount() throws Exception {
		Pin pin = new Pin();
		pin.setPin("123456");
		
		when(repository.save(any(Pin.class))).thenReturn(pin);
		
		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.post("/api/generate?account=testing")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON);
		
		MvcResult result = mockMvc.perform(requestBuilder).andReturn();
		
		MockHttpServletResponse response = result.getResponse();
		
		assertEquals(HttpStatus.OK.value(), response.getStatus());
		
		String expected = "{\"pin\":123456}";
		
		JSONAssert.assertEquals(expected, response.getContentAsString(), false);
	}
	
}
