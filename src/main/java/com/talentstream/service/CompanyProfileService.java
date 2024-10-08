package com.talentstream.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.talentstream.dto.CompanyProfileDTO;
import com.talentstream.entity.CompanyProfile;
import com.talentstream.entity.JobRecruiter;
import com.talentstream.exception.CustomException;
import com.talentstream.repository.CompanyProfileRepository;
import com.talentstream.repository.JobRecruiterRepository;
import java.util.Optional;

@Service
public class CompanyProfileService {

    private final CompanyProfileRepository companyProfileRepository;

    @Autowired
    JobRecruiterRepository jobRecruiterRepository;

    // Initializes the CompanyProfileService with the specified repository.
    @Autowired
    public CompanyProfileService(CompanyProfileRepository companyProfileRepository) {
        this.companyProfileRepository = companyProfileRepository;
    }

    // Saves a new CompanyProfile for the specified job recruiter if it doesn't
    // already exist.
    public String saveCompanyProfile(CompanyProfileDTO companyProfileDTO, Long jobRecruiterId) throws Exception {
        JobRecruiter jobRecruiter = jobRecruiterRepository.findByRecruiterId(jobRecruiterId);
        if (jobRecruiter == null)
            throw new CustomException("Recruiter not found for ID: " + jobRecruiterId, HttpStatus.NOT_FOUND);
        else {
            if (!companyProfileRepository.existsByJobRecruiterId(jobRecruiterId)) {
                CompanyProfile companyProfile = convertDTOToEntity(companyProfileDTO);
                companyProfile.setJobRecruiter(jobRecruiter);
                companyProfile.setApprovalStatus("APPROVED");
                companyProfileRepository.save(companyProfile);
                return "profile saved sucessfully";
            } else {
                throw new CustomException("CompanyProfile was already updated.", HttpStatus.OK);
            }
        }

    }

    // Retrieves a CompanyProfile by its ID and converts it to a CompanyProfileDTO
    // if found.
    public Optional<CompanyProfileDTO> getCompanyProfileById(Long id) {
        Optional<CompanyProfile> companyProfile = companyProfileRepository.findById(id);
        return companyProfile.map(this::convertEntityToDTO);
    }

    // Converts a CompanyProfile entity to a CompanyProfileDTO.
    private CompanyProfileDTO convertEntityToDTO(CompanyProfile companyProfile) {
        CompanyProfileDTO dto = new CompanyProfileDTO();
        dto.setId(companyProfile.getId());
        dto.setCompanyName(companyProfile.getCompanyName());
        dto.setWebsite(companyProfile.getWebsite());
        dto.setPhoneNumber(companyProfile.getPhoneNumber());
        dto.setEmail(companyProfile.getEmail());
        dto.setHeadOffice(companyProfile.getHeadOffice());
        dto.setSocialProfiles(companyProfile.getSocialProfiles());
        return dto;
    }

    // Converts a CompanyProfileDTO to a CompanyProfile entity.
    private CompanyProfile convertDTOToEntity(CompanyProfileDTO companyProfileDTO) {
        CompanyProfile entity = new CompanyProfile();

        entity.setId(companyProfileDTO.getId());
        entity.setCompanyName(companyProfileDTO.getCompanyName());
        entity.setWebsite(companyProfileDTO.getWebsite());
        entity.setPhoneNumber(companyProfileDTO.getPhoneNumber());
        entity.setEmail(companyProfileDTO.getEmail());
        entity.setHeadOffice(companyProfileDTO.getHeadOffice());
        entity.setSocialProfiles(companyProfileDTO.getSocialProfiles());
        return entity;
    }

    // Checks the approval status of a company profile based on the recruiter ID.
    public String checkApprovalStatus(Long jobRecruiterId) {
        CompanyProfile companyProfile = companyProfileRepository.findByJobRecruiter_RecruiterId(jobRecruiterId);

        if (companyProfile != null) {
            String approvalStatus = companyProfile.getApprovalStatus();

            switch (approvalStatus.toLowerCase()) {
                case "pending":
                    return "pending";
                case "approved":
                    return "approved";
                case "rejected":
                    return "rejected";
                default:
                    throw new CustomException("Invalid approval status.", HttpStatus.BAD_REQUEST);
            }
        } else {
            return "Profile not found";
        }
    }

    // Updates the approval status of a company profile based on the recruiter ID.
    public String updateApprovalStatus(Long jobRecruiterId, String newStatus) {
        CompanyProfile companyProfile = companyProfileRepository.findByJobRecruiter_RecruiterId(jobRecruiterId);

        if (companyProfile != null) {
            companyProfile.setApprovalStatus(newStatus);
            companyProfileRepository.save(companyProfile);

            return "Approval status updated successfully with: " + newStatus;
        } else {
            throw new CustomException("CompanyProfile not found for jobRecruiterId: " + jobRecruiterId,
                    HttpStatus.NOT_FOUND);
        }
    }

}