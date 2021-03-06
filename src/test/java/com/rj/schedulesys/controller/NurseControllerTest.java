package com.rj.schedulesys.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.rj.schedulesys.config.TestConfiguration;
import com.rj.schedulesys.util.TestUtil;
import com.rj.schedulesys.view.model.NurseViewModel;
import com.rj.schedulesys.view.model.PhoneNumberViewModel;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(TestConfiguration.class)
public class NurseControllerTest {

	private @Autowired WebApplicationContext context;
	
	private MockMvc mockMvc;
	
	@Before
	public void setup(){
		mockMvc = MockMvcBuilders.webAppContextSetup(context)
				.build();
	}
	
	@Test
	public void test_findOne_WithNonExistingNurseId() throws Exception{
		mockMvc.perform(get("/nurses/{id}", 0))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$", is("No nurse found with id : 0")));
	}
	
	@Test
	public void test_findOne_WithExistingNurseId() throws Exception{
		mockMvc.perform(get("/nurses/{id}", 1))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.lastName", is("ZERBO")))
			.andExpect(jsonPath("$.firstName", is("Boureima Edouard")))
			.andExpect(jsonPath("$.positionName", is("RN")))
			.andExpect(jsonPath("$.comment", is("COMMENT ON EMPLOYEE WITH ID 1")));
	}
	
	@Test
	public void test_findAllLicense() throws Exception{
		mockMvc.perform(get("/nurses/{id}/licenses", 2))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$", hasSize(2)));
	}
	
	@Test
	public void test_create_WithNonExistingPosition() throws IOException, Exception{
		PhoneNumberViewModel aNewPhoneNumberViewModel = TestUtil.aNewPhoneNumberViewModel(null, "(718)-720-9836", "PRIMARY", "MOBILE");
		NurseViewModel aNewNurseViewModel = TestUtil.aNewNurseViewModel(0L, "firstName", "lastName", "This position does not exist"
				, true, new Date(), null, null, null);
		aNewNurseViewModel.setPhoneNumbers(Arrays.asList(aNewPhoneNumberViewModel));
		mockMvc.perform(post("/nurses")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(TestUtil.convertObjectToJsonBytes(aNewNurseViewModel)))
		.andExpect(status().is4xxClientError())
		.andExpect(jsonPath("$", is("No position found with name : This position does not exist")));
	}
	
	
	@Test
	public void test_create_WithExisingPosition() throws IOException, Exception{
		
		PhoneNumberViewModel aNewPhoneNumberViewModel = TestUtil.aNewPhoneNumberViewModel(null, "(718)-720-9836", "PRIMARY", "MOBILE");
		
		NurseViewModel aNewNurseViewModel = TestUtil.aNewNurseViewModel(0L, "firstName", "lastName", "LNP"
				, true, new Date(), null, null, null);
		
		aNewNurseViewModel.setPhoneNumbers(Arrays.asList(aNewPhoneNumberViewModel));
		
		mockMvc.perform(post("/nurses")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(TestUtil.convertObjectToJsonBytes(aNewNurseViewModel)))
		.andExpect(status().isCreated())
		.andExpect(jsonPath("$", is("Nurse successfully created")));
	}
	
	@Test
	public void test_update_WithNonExistingNurse() throws IOException, Exception{
		NurseViewModel aNewNurseViewModel = TestUtil.aNewNurseViewModel(0L, "firstName", "lastName", "LNP"
				, true, new Date(), null, null, null);
		mockMvc.perform(put("/nurses/{id}", 0L)
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(TestUtil.convertObjectToJsonBytes(aNewNurseViewModel)))
		.andExpect(status().isNotFound())
		.andExpect(jsonPath("$", is("No nurse found with id : 0")));
	}
	
	@Test
	public void test_update_WithExistingNurse() throws IOException, Exception{
		PhoneNumberViewModel aNewPhoneNumberViewModel = TestUtil.aNewPhoneNumberViewModel(null, "(718)-720-9836", "PRIMARY", "MOBILE");
		NurseViewModel aNewNurseViewModel = TestUtil.aNewNurseViewModel(2L, "firstName", "lastName", "LNP"
				, true, new Date(), null, null, null);
		aNewNurseViewModel.setPhoneNumbers(Arrays.asList(aNewPhoneNumberViewModel));
		mockMvc.perform(put("/nurses/{id}", 2)
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(TestUtil.convertObjectToJsonBytes(aNewNurseViewModel)))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$", is("Nurse successfully updated")));
	}
	
	@Test
	public void test_delete_WithNonExistingNurse() throws Exception{
		mockMvc.perform(delete("/nurses/{id}", 0))
		.andExpect(status().isNotFound())
		.andExpect(jsonPath("$", is("No nurse found with id : 0")));
	}
	
	@Test
	public void test_delete_WithExistingNurse() throws Exception{
		mockMvc.perform(delete("/nurses/{id}", 3))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$", is("Nurse successfully deleted")));
	}
	
	@Test
	public void test_deletePhoneNumber_WithPrimaryPhoneNumber() throws Exception{
		mockMvc.perform(delete("/nurses/{id}/phone-numbers/{phomeNumberId}", 1, 1))
			.andExpect(status().is5xxServerError())
			.andExpect(jsonPath("$", is("Primary phone number can not be deleted")));
	}
	
	@Test
	public void test_deletePhoneNumber_WithSecondaryPhoneNumber() throws Exception{
		mockMvc.perform(delete("/nurses/{id}/phone-numbers/{phomeNumberId}", 1, 2))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", is("Phone number successfully deleted")));
	}
	
}