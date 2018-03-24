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

import java.util.*;

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
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON);
		
		MvcResult result = mockMvc.perform(requestBuilder).andReturn();
		
		String expected = "{\"error\":\"account is required\"}";
		
		JSONAssert.assertEquals(expected, result.getResponse().getContentAsString(), false);
	}
	
	@Test
	public void testGeneratePinWithEmptyAccount() throws Exception {
		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.post("/api/generate")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"account\": \"\"}")
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
				.post("/api/generate")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"account\": \"testing\"}")
				.accept(MediaType.APPLICATION_JSON);
		
		MvcResult result = mockMvc.perform(requestBuilder).andReturn();
		
		MockHttpServletResponse response = result.getResponse();
		
		assertEquals(HttpStatus.OK.value(), response.getStatus());
		
		String expected = "{\"pin\":123456}";
		
		JSONAssert.assertEquals(expected, response.getContentAsString(), false);
	}
	@Test
	public void testClaimPinWithNoAccount() throws Exception {
		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.post("/api/claim")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON);

		MvcResult result = mockMvc.perform(requestBuilder).andReturn();

		String expected = "{\"error\":\"account is required\"}";

		JSONAssert.assertEquals(expected, result.getResponse().getContentAsString(), false);
	}

	@Test
	public void testClaimPinWithEmptyAccount() throws Exception {
		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.post("/api/claim")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"account\": \"\"}");

		MvcResult result = mockMvc.perform(requestBuilder).andReturn();

		String expected = "{\"error\":\"empty account\"}";

		JSONAssert.assertEquals(expected, result.getResponse().getContentAsString(), false);
	}

	@Test
	public void testClaimPinWithNoPin() throws Exception {
		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.post("/api/claim")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"account\": \"testing\"}");

		MvcResult result = mockMvc.perform(requestBuilder).andReturn();

		String expected = "{\"error\":\"pin is required\"}";

		JSONAssert.assertEquals(expected, result.getResponse().getContentAsString(), false);
	}

	@Test
	public void testClaimPinWithEmptyPin() throws Exception {
		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.post("/api/claim?account=testing&pin=")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"account\": \"testing\", \"pin\": \"\"}");

		MvcResult result = mockMvc.perform(requestBuilder).andReturn();

		String expected = "{\"error\":\"empty pin\"}";

		JSONAssert.assertEquals(expected, result.getResponse().getContentAsString(), false);
	}

	@Test
	public void testClaimPinWithInvalidPin() throws Exception {
		ArrayList<Pin> pins = new ArrayList<>();
		pins.add(new Pin("testing", "123456"));

		when(repository.findPinsByAccount("testing")).thenReturn(pins);

		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.post("/api/claim")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"account\": \"testing\", \"pin\": \"123457\"}");

		MvcResult result = mockMvc.perform(requestBuilder).andReturn();

		String expected = "{\"error\":\"The requested pin was invalid\"}";

		JSONAssert.assertEquals(expected, result.getResponse().getContentAsString(), false);
	}

	@Test
	public void testClaimPinPastExpirationDate() throws Exception {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.YEAR, -1);
		ArrayList<Pin> pins = new ArrayList<>();
		Pin pin = new Pin("testing", "123456");
		pin.setExpireTimestamp(c.getTime());
		pins.add(pin);

		when(repository.findPinsByAccount("testing")).thenReturn(pins);

		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.post("/api/claim")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"account\": \"testing\", \"pin\": \"123456\"}");

		MvcResult result = mockMvc.perform(requestBuilder).andReturn();

		String expected = "{\"error\":\"The requested pin was valid but has expired\"}";

		JSONAssert.assertEquals(expected, result.getResponse().getContentAsString(), false);
	}

	@Test
	public void testClaimPinThatHasBeenClaimed() throws Exception {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.YEAR, 1);
		ArrayList<Pin> pins = new ArrayList<>();
		Pin pin = new Pin("testing", "123456");
		pin.setExpireTimestamp(c.getTime());
		pin.setClaimIp("1010101");

		pins.add(pin);
		when(repository.findPinsByAccount("testing")).thenReturn(pins);

		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.post("/api/claim")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"account\": \"testing\", \"pin\": \"123456\"}");

		MvcResult result = mockMvc.perform(requestBuilder).andReturn();

		String expected = "{\"error\":\"The requested pin has already been claimed\"}";

		JSONAssert.assertEquals(expected, result.getResponse().getContentAsString(), false);
	}

	@Test
	public void testClaimPinWithValidValues() throws Exception {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.YEAR, 1);
		ArrayList<Pin> pins = new ArrayList<>();
		Pin pin = new Pin("testing", "123456");
		pin.setExpireTimestamp(c.getTime());

		pins.add(pin);
		when(repository.findPinsByAccount("testing")).thenReturn(pins);

		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.post("/api/claim")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"account\": \"testing\", \"pin\": \"123456\"}");

		MvcResult result = mockMvc.perform(requestBuilder).andReturn();

		String expected = "{\"success\":\"The pin has been successfully claimed\"}";

		JSONAssert.assertEquals(expected, result.getResponse().getContentAsString(), false);
	}
}
