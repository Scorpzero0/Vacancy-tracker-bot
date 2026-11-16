package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class VacancyTest {

    private Company createMockCompany(String name, String url) {
        Company company = new Company();
        company.setName(name);
        company.setUrl(url);
        return company;
    }

    private Requirement createMockRequirement(int experience) {
        Requirement requirement = new Requirement();
        requirement.setExperience(experience);
        return requirement;
    }

    @Test
    void givenFullVacancyData_whenToString_thenReturnCorrectHtmlWithAllFields() {
        // Arrange
        Vacancy vacancy = new Vacancy();
        vacancy.setJobName("Java Developer");
        vacancy.setUrl("https://vacancy-url.com");
        vacancy.setSalaryMin(100000);
        vacancy.setSalaryMax(150000);
        vacancy.setCompany(createMockCompany("Google", "https://google.com"));
        vacancy.setRequirement(createMockRequirement(3));
        String result = vacancy.toString();
        assertTrue(result.contains("💼 <b><a href=\"https://google.com\">Google</a></b>"));
        assertTrue(result.contains("Java Developer"));
        assertTrue(result.contains("💰 Зарплата: от 100000 до 150000"));
        assertTrue(result.contains("⏳ Опыт: 3."));
        assertTrue(result.contains("Подробное описание вакансии <a href=\"https://vacancy-url.com\"> здесь</a>"));
    }

    @Test
    void givenMinimalVacancyData_whenToString_thenFallbackToDefaultValues() {
        Vacancy vacancy = new Vacancy();
        vacancy.setJobName("Intern");
        vacancy.setUrl("https://vacancy-url.com");
        vacancy.setCompany(null);
        vacancy.setRequirement(null);
        vacancy.setSalaryMin(null);
        vacancy.setSalaryMax(null);
        String result = vacancy.toString();
        assertTrue(result.contains("💼 <b><a href=\"https://trudvsem.ru\">Не указана</a></b>"));
        assertTrue(result.contains("Intern"));
        assertTrue(result.contains("💰 Зарплата: не указана"));
        assertTrue(result.contains("⏳ Опыт: не указан."));
    }

    @Test
    void givenCompanyWithoutUrl_whenToString_thenFallbackToTrudVsemUrl() {
        Vacancy vacancy = new Vacancy();
        vacancy.setJobName("QA Engineer");
        vacancy.setCompany(createMockCompany("Yandex", null)); // Имя есть, ссылки нет
        String result = vacancy.toString();
        assertTrue(result.contains("💼 <b><a href=\"https://trudvsem.ru\">Yandex</a></b>"));
    }

    @Test
    void givenOnlyMinSalary_whenToString_thenFormattedCorrectly() {
        Vacancy vacancy = new Vacancy();
        vacancy.setJobName("Developer");
        vacancy.setSalaryMin(50000);
        vacancy.setSalaryMax(null);
        String result = vacancy.toString();
        assertTrue(result.contains("💰 Зарплата: от 50000 "));
    }

    @Test
    void givenOnlyMaxSalary_whenToString_thenFormattedCorrectly() {
        Vacancy vacancy = new Vacancy();
        vacancy.setJobName("Developer");
        vacancy.setSalaryMin(null);
        vacancy.setSalaryMax(120000);
        String result = vacancy.toString();
        assertTrue(result.contains("💰 Зарплата:  до 120000"));
    }
}
