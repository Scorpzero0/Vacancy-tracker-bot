package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Vacancy {

    private String id;
    @JsonProperty("vac_url")
    private String url;
    private Region region;
    private Company company;
    private Requirement requirement;
    @JsonProperty("creation-date")
    private String creationDate;
    private String salary;
    @JsonProperty("salary_min")
    private Integer salaryMin;
    @JsonProperty("salary_max")
    private Integer salaryMax;
    @JsonProperty("job-name")
    private String jobName;

    @Override
    public String toString() {
        String experience = (requirement != null)
                ? String.valueOf(requirement.getExperience())
                : "не указан";
        String companyName = (company != null && company.getName() != null)
                ? company.getName()
                : "Не указана";
        String salaryInfo = (salaryMin == null && salaryMax == null)
                ? "не указана"
                : getSalaryInfo(salaryMin, salaryMax);
        String companyUrl = (company != null && company.getUrl() != null && !company.getUrl().isEmpty())
                ? company.getUrl()
                : "https://trudvsem.ru";
        return "💼 <b><a href=\"" + companyUrl + "\">" + companyName + "</a></b>" + System.lineSeparator()
                + jobName + System.lineSeparator()
                + "💰 Зарплата: " + salaryInfo + System.lineSeparator()
                + "⏳ Опыт: " + experience + "." + System.lineSeparator()
                + "Подробное описание вакансии <a href=\"" + url + "\"> здесь</a>" + System.lineSeparator();
    }

    private String getSalaryInfo(Integer salaryMin, Integer salaryMax) {
        String salaryMaxInfo = (salaryMax == null) ? "" : "до " + salaryMax;
        String salaryMinInfo = (salaryMin == null) ? "" : "от " + salaryMin;
        return salaryMinInfo + " " + salaryMaxInfo;
    }
}
