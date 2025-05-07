package ch.zhaw.freelancer4u.controller;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ch.zhaw.freelancer4u.model.Job;
import ch.zhaw.freelancer4u.model.JobCreateDTO;
import ch.zhaw.freelancer4u.model.JobType;
import ch.zhaw.freelancer4u.repository.JobRepository;
import ch.zhaw.freelancer4u.service.CompanyService;
import ch.zhaw.freelancer4u.service.RoleService;

@RestController
@RequestMapping("/api")
public class JobController {

    @Autowired
    JobRepository jobRepository;

    @Autowired
    CompanyService companyService;

    @Autowired
    RoleService roleService;

    @Autowired
    OpenAiChatModel chatModel;

    @PostMapping("/job")
    public ResponseEntity<?> createJob(@RequestBody JobCreateDTO fDTO) {
        if (!roleService.userHasRole("admin")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if (!companyService.companyExists(fDTO.getCompanyId())) {
            return new ResponseEntity<>("Company not found", HttpStatus.BAD_REQUEST);
        }
        var generatedTitle = chatModel.call(new Prompt(
                "Der Titel lautet bisher: '" + fDTO.getTitle()
                        + "'. Falls nötig, verbessere den Titel anhand der folgenden Beschreibung: "
                        + fDTO.getDescription() + ". Gib nur den neuen Titel zurück."));
        var title = generatedTitle.getResult().getOutput().getText();
        Job fDAO = new Job(title, fDTO.getJobType(), fDTO.getEarnings(), fDTO.getCompanyId());
        Job f = jobRepository.save(fDAO);
        return new ResponseEntity<>(f, HttpStatus.CREATED);
    }

    @GetMapping("/job")
    public ResponseEntity<Page<Job>> getAllJobs(
            @RequestParam(required = false) Double min,
            @RequestParam(required = false) JobType type,
            @RequestParam(required = false, defaultValue = "1") Integer pageNumber,
            @RequestParam(required = false, defaultValue = "5") Integer pageSize) {
        Page<Job> allJobs;
        if (min == null && type == null) {
            allJobs = jobRepository.findAll(PageRequest.of(pageNumber - 1, pageSize));
        } else {
            if (min != null && type != null) {
                allJobs = jobRepository.findByJobTypeAndEarningsGreaterThan(type, min,
                        PageRequest.of(pageNumber - 1, pageSize));
            } else if (min != null) {
                allJobs = jobRepository.findByEarningsGreaterThan(min, PageRequest.of(pageNumber - 1, pageSize));
            } else {
                allJobs = jobRepository.findByJobType(type, PageRequest.of(pageNumber - 1, pageSize));
            }
        }
        return new ResponseEntity<>(allJobs, HttpStatus.OK);
    }

    @DeleteMapping("/job")
    public ResponseEntity<String> deleteAllJobs() {
        if (!roleService.userHasRole("admin")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        jobRepository.deleteAll();
        return ResponseEntity.status(HttpStatus.OK).body("DELETED");
    }

}
