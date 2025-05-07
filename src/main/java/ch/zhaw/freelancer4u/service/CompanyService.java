package ch.zhaw.freelancer4u.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import ch.zhaw.freelancer4u.model.Company;
import ch.zhaw.freelancer4u.model.CompanyCreateDTO;
import ch.zhaw.freelancer4u.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    public boolean companyExists(String id) {
        return companyRepository.existsById(id);
    }

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public Company createCompany(String name, String email) {
        Company company = new Company(name, email);
        return companyRepository.save(company);
    }
    
    public Company createCompany(CompanyCreateDTO companyCreateDTO) {
        Company company = new Company(companyCreateDTO.getName(), companyCreateDTO.getEmail());
        return companyRepository.save(company);
    }
    
    public Optional<Company> findCompanyByName(String name) {
        return companyRepository.findAll().stream()
                .filter(company -> company.getName().equalsIgnoreCase(name))
                .findFirst();
    }
    
    public Company findOrCreateCompany(String name) {
        return findCompanyByName(name)
                .orElseGet(() -> {
                    String defaultEmail = name.toLowerCase().replace(" ", ".") + "@example.com";
                    return createCompany(name, defaultEmail);
                });
    }
}