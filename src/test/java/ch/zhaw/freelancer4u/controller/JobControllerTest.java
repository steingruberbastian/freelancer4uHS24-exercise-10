package ch.zhaw.freelancer4u.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Answers;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.zhaw.freelancer4u.model.Job;
import ch.zhaw.freelancer4u.model.JobType;
import ch.zhaw.freelancer4u.repository.CompanyRepository;
import ch.zhaw.freelancer4u.repository.JobRepository;
import ch.zhaw.freelancer4u.security.TestSecurityConfig;

@SpringBootTest
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class JobControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    JobRepository jobRepository;

    private static ObjectMapper objectMapper = new ObjectMapper();

    private static final String TEST_TITLE = "TEST-TITLE-abc...xyz";
    private static final String TEST_DESCRIPTION = "TEST-abc...xyz";
    private static String company_id = "";
    private static String job_id = "";

    @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
    private OpenAiChatModel chatModel;

    @BeforeEach
    void setupMockAiResponse() {
        when(chatModel.call(any(Prompt.class))
                .getResult()
                .getOutput()
                .getText()).thenReturn(TEST_TITLE);
    }

    @Test
    @Order(10)
    public void testCreateJob() throws Exception {
        // get valid company id
        company_id = getCompanyId();
        System.out.println("using company id " + company_id);

        // create a test job and convert to Json
        Job job = new Job();
        job.setTitle(TEST_TITLE);
        job.setDescription(TEST_DESCRIPTION);
        job.setJobType(JobType.TEST);
        job.setEarnings(3.1415);
        job.setCompanyId(company_id);
        String jsonBody = objectMapper.writeValueAsString(job);

        // POST Json to service with authorization header
        var result = mvc.perform(post("/api/job")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        job_id = jsonNode.get("id").asText();
        System.out.println("created job with id " + job_id);
    }

    @Test
    @Order(20)
    public void testGetJob() throws Exception {
        mvc.perform(get("/api/job/" + job_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(TEST_TITLE))
                .andExpect(jsonPath("$.description").value(TEST_DESCRIPTION))
                .andExpect(jsonPath("$.companyId").value(company_id));
    }

    @Test
    @Order(30)
    public void testDeleteJobById() throws Exception {
        mvc.perform(delete("/api/job/" + job_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(40)
    public void testGetDeletedJob() throws Exception {
        mvc.perform(get("/api/job/" + job_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    private String getCompanyId() {
        // get valid company id
        return companyRepository.findAll().get(0).getId();
    }
}