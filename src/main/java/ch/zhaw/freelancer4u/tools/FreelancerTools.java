package ch.zhaw.freelancer4u.tools;

import java.util.List;
import java.util.Optional;

import org.springframework.ai.tool.annotation.Tool;

import ch.zhaw.freelancer4u.model.Company;
import ch.zhaw.freelancer4u.model.Job;
import ch.zhaw.freelancer4u.model.JobType;
import ch.zhaw.freelancer4u.repository.JobRepository;
import ch.zhaw.freelancer4u.repository.CompanyRepository;
import ch.zhaw.freelancer4u.service.CompanyService;

public class FreelancerTools {
    private JobRepository jobRepository;
    private CompanyService companyService;
    private CompanyRepository companyRepository;

    public FreelancerTools(JobRepository jobRepository, CompanyService companyService, CompanyRepository companyRepository) {
        this.jobRepository = jobRepository;
        this.companyService = companyService;
        this.companyRepository = companyRepository;
    }

    @Tool(description = "Information about the jobs in the database.")
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    @Tool(description = "Information about the companies in the database.")
    public List<Company> getAllCompanies() {
        return companyService.getAllCompanies();
    }
    
    @Tool(description = "Create a new company in the database.")
    public Company createCompany(String name, String email) {
        // Prüfen, ob das Unternehmen bereits existiert
        Optional<Company> existingCompany = findCompanyByName(name);
        if (existingCompany.isPresent()) {
            return existingCompany.get();
        }
        
        // Neues Unternehmen erstellen
        Company newCompany = new Company(name, email);
        return companyRepository.save(newCompany);
    }
    
    @Tool(description = "Create a new job in the database. If the company does not exist, it will be created.")
    public Job createJob(String title, String description, JobType jobType, double earnings, String companyName, String companyEmail) {
        // Prüfen, ob das Unternehmen existiert, sonst erstellen
        Optional<Company> existingCompany = findCompanyByName(companyName);
        Company company;
        
        if (existingCompany.isPresent()) {
            company = existingCompany.get();
        } else {
            // Neues Unternehmen erstellen
            company = createCompany(companyName, companyEmail);
        }
        
        // Neuen Job erstellen
        Job newJob = new Job(title, jobType, earnings, company.getId());
        return jobRepository.save(newJob);
    }
    
    // Hilfsmethode zum Finden eines Unternehmens anhand des Namens
    private Optional<Company> findCompanyByName(String name) {
        List<Company> allCompanies = companyService.getAllCompanies();
        return allCompanies.stream()
                .filter(company -> company.getName().equalsIgnoreCase(name))
                .findFirst();
    }
}