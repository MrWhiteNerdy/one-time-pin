package edu.ucmo.mathcs.onetimepin;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;

@RunWith(SpringRunner.class)
@WebMvcTest(value = PinController.class, secure = false)
public class PinControllerTests {

	@Rule
	public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext context;

	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
				.apply(documentationConfiguration(this.restDocumentation)).build();
	}

    private static String GENERATE_ENDPOINT = "/api/generate";
    private static String CLAIM_ENDPOINT = "/api/claim";
	
	@MockBean
	private PinRepository repository;
	
	@Test
	public void testGeneratePinWithNoAccount() throws Exception {
		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.post(GENERATE_ENDPOINT)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON);

		MvcResult result = mockMvc.perform(requestBuilder).andReturn();

		String expected = "{\"error\":\"account is required\"}";
		
		JSONAssert.assertEquals(expected, result.getResponse().getContentAsString(), false);
	}
	
	@Test
	public void testGeneratePinWithEmptyAccount() throws Exception {
		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.post(GENERATE_ENDPOINT)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"account\": \"\"}")
				.accept(MediaType.APPLICATION_JSON);
		
		MvcResult result = mockMvc.perform(requestBuilder).andReturn();
		
		String expected = "{\"error\":\"invalid account\"}";
		
		JSONAssert.assertEquals(expected, result.getResponse().getContentAsString(), false);
	}
	
	@Test
	public void testGeneratePinWithValidAccountAndNoCreateUser() throws Exception {
		Pin pin = new Pin();
		pin.setPin("123456");
		
		when(repository.save(any(Pin.class))).thenReturn(pin);
		
		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.post(GENERATE_ENDPOINT)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"account\": \"testing\"}")
				.accept(MediaType.APPLICATION_JSON);

		MvcResult result = mockMvc.perform(requestBuilder).andReturn();

		String expected = "{\"error\":\"create user is required\"}";

		JSONAssert.assertEquals(expected, result.getResponse().getContentAsString(), false);
	}

	@Test
	public void testGeneratePinWithValidAccountAndEmptyCreateUser() throws Exception {
		Pin pin = new Pin();
		pin.setPin("123456");

		when(repository.save(any(Pin.class))).thenReturn(pin);

		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.post(GENERATE_ENDPOINT)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"account\": \"testing\", \"createUser\": \"\"}")
				.accept(MediaType.APPLICATION_JSON);

		MvcResult result = mockMvc.perform(requestBuilder).andReturn();

		String expected = "{\"error\":\"empty create user\"}";

		JSONAssert.assertEquals(expected, result.getResponse().getContentAsString(), false);
	}

	@Test
	public void testGeneratePinWithValidAccountAndValidCreateUser() throws Exception {
		Pin pin = new Pin();
		pin.setPin("123456");

		when(repository.save(any(Pin.class))).thenReturn(pin);

		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.post(GENERATE_ENDPOINT)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"account\": \"testing\", \"createUser\": \"test user\"}")
				.accept(MediaType.APPLICATION_JSON);

		MvcResult result = mockMvc.perform(requestBuilder)
				.andDo(document("generate",
						requestFields(
								fieldWithPath("account")
										.type(JsonFieldType.STRING)
										.description("The account to link to the generated pin"),
								fieldWithPath("createUser")
										.type(JsonFieldType.STRING)
										.description("The creating user to link to the generated pin"))))
				.andDo(document("generate",
						responseFields(
								fieldWithPath("pin")
										.type(JsonFieldType.STRING)
										.description("The generated pin"))))
				.andReturn();


		MockHttpServletResponse response = result.getResponse();

		assertEquals(HttpStatus.OK.value(), response.getStatus());

		String expected = "{\"pin\":\"123456\"}";

		JSONAssert.assertEquals(expected, response.getContentAsString(), false);
	}

	@Test
	public void testClaimPinWithNoAccount() throws Exception {
		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.post(CLAIM_ENDPOINT)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON);

		MvcResult result = mockMvc.perform(requestBuilder).andReturn();

		String expected = "{\"error\":\"account is required\"}";

		JSONAssert.assertEquals(expected, result.getResponse().getContentAsString(), false);
	}

	@Test
	public void testClaimPinWithEmptyAccount() throws Exception {
		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.post(CLAIM_ENDPOINT)
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
				.post(CLAIM_ENDPOINT)
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
				.post(CLAIM_ENDPOINT)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"account\": \"testing\", \"pin\": \"\"}");

		MvcResult result = mockMvc.perform(requestBuilder).andReturn();

		String expected = "{\"error\":\"empty pin\"}";

		JSONAssert.assertEquals(expected, result.getResponse().getContentAsString(), false);
	}

    @Test
    public void testClaimPinWithIncorrectFormattedPin() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post(CLAIM_ENDPOINT)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"account\": \"testing\", \"pin\": \"932716\", \"claimUser\":\"test claim\"}");

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        String expected = "{\"error\":\"pin not in correct format\"}";

        JSONAssert.assertEquals(expected, result.getResponse().getContentAsString(), false);
    }

	@Test
	public void testClaimPinWithInvalidPin() throws Exception {
		ArrayList<Pin> pins = new ArrayList<>();
		pins.add(new Pin("testing", "123456"));

		when(repository.findPinsByAccount("testing")).thenReturn(pins);

		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.post(CLAIM_ENDPOINT)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"account\": \"testing\", \"pin\": \"932715\", \"claimUser\":\"test claim\"}");

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
		Pin pin = new Pin("testing", "932715");
		pin.setExpireTimestamp(c.getTime());
		pins.add(pin);

		when(repository.findPinsByAccount("testing")).thenReturn(pins);

		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.post(CLAIM_ENDPOINT)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"account\": \"testing\", \"pin\": \"932715\", \"claimUser\":\"test claim\"}");

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
		Pin pin = new Pin("testing", "932715");
		pin.setExpireTimestamp(c.getTime());
		pin.setClaimIp("1010101");

		pins.add(pin);
		when(repository.findPinsByAccount("testing")).thenReturn(pins);

		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.post(CLAIM_ENDPOINT)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"account\": \"testing\", \"pin\": \"932715\", \"claimUser\":\"test claim\"}");

		MvcResult result = mockMvc.perform(requestBuilder).andReturn();

		String expected = "{\"error\":\"The requested pin has already been claimed\"}";

		JSONAssert.assertEquals(expected, result.getResponse().getContentAsString(), false);
	}

	@Test
	public void testClaimPinWithNoClaimUser() throws Exception {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.YEAR, 1);
		ArrayList<Pin> pins = new ArrayList<>();
		Pin pin = new Pin("testing", "932715");
		pin.setExpireTimestamp(c.getTime());

		pins.add(pin);
		when(repository.findPinsByAccount("testing")).thenReturn(pins);

		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.post(CLAIM_ENDPOINT)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"account\": \"testing\", \"pin\": \"932715\"}");

		MvcResult result = mockMvc.perform(requestBuilder).andReturn();

		String expected = "{\"error\":\"claim user is required\"}";

		JSONAssert.assertEquals(expected, result.getResponse().getContentAsString(), false);
	}

	@Test
	public void testClaimPinWithEmptyClaimUser() throws Exception {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.YEAR, 1);
		ArrayList<Pin> pins = new ArrayList<>();
		Pin pin = new Pin("testing", "932715");
		pin.setExpireTimestamp(c.getTime());

		pins.add(pin);
		when(repository.findPinsByAccount("testing")).thenReturn(pins);

		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.post(CLAIM_ENDPOINT)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"account\": \"testing\", \"pin\": \"932715\", \"claimUser\":\"\"}");

		MvcResult result = mockMvc.perform(requestBuilder).andReturn();

		String expected = "{\"error\":\"empty claim user\"}";

		JSONAssert.assertEquals(expected, result.getResponse().getContentAsString(), false);
	}

	@Test
	public void testClaimPinWithValidValues() throws Exception {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.YEAR, 1);
		ArrayList<Pin> pins = new ArrayList<>();
		Pin pin = new Pin("testing", "932715");
		pin.setExpireTimestamp(c.getTime());

		pins.add(pin);
		when(repository.findPinsByAccount("testing")).thenReturn(pins);

		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.post(CLAIM_ENDPOINT)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"account\": \"testing\", \"pin\": \"932715\", \"claimUser\":\"test claim\"}");

		MvcResult result = mockMvc.perform(requestBuilder)
				.andDo(document("claim",
						requestFields(
								fieldWithPath("account")
								.type(JsonFieldType.STRING)
								.description("The account used to generate the pin"),
								fieldWithPath("pin")
								.type(JsonFieldType.STRING)
								.description("The pin that was generated"),
								fieldWithPath("claimUser")
								.type(JsonFieldType.STRING)
								.description("The user who is claiming the pin"))
						))
				.andDo(document("claim",
						responseFields(
						        fieldWithPath("success")
								.type(JsonFieldType.STRING)
								.description("The success message"))))
				.andReturn();

		String expected = "{\"success\":\"The pin has been successfully claimed\"}";

		JSONAssert.assertEquals(expected, result.getResponse().getContentAsString(), false);
	}
	
}
