package com.techelevator.projects.model.jdbc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.techelevator.projects.model.Department;
import com.techelevator.projects.model.Employee;
import com.techelevator.projects.model.EmployeeDAO;

public class JDBCEmployeeDAO implements EmployeeDAO {

	private JdbcTemplate jdbcTemplate;

	public JDBCEmployeeDAO(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	@Override
	public List<Employee> getAllEmployees() {
		List<Employee> employees = new ArrayList<>();
		String employee = "SELECT * FROM employee";
		SqlRowSet employeeNextRow = jdbcTemplate.queryForRowSet(employee);
		while(employeeNextRow.next()) {
			Long id = employeeNextRow.getLong("employee_id");
			employees.add(mapRowToEmployee(employeeNextRow));
		}
		return employees;
	}
	
	private Employee mapRowToEmployee(SqlRowSet employeeNextRow) {
		Employee theEmployee;
		theEmployee = new Employee();
		theEmployee.setId(employeeNextRow.getLong("employee_id"));
		theEmployee.setFirstName(employeeNextRow.getString("first_name"));
		theEmployee.setLastName(employeeNextRow.getString("last_name"));
		theEmployee.setGender(employeeNextRow.getString("gender").charAt(0));
		theEmployee.setBirthDay(employeeNextRow.getDate("birth_date").toLocalDate());
		theEmployee.setHireDate(employeeNextRow.getDate("hire_date").toLocalDate());
		theEmployee.setDepartmentId(employeeNextRow.getLong("department_id"));
		
		
		return theEmployee;
	}

	@Override
	public List<Employee> searchEmployeesByName(String firstNameSearch, String lastNameSearch) {
		List<Employee> search = new ArrayList <>();
		String employee = "SELECT * FROM employee WHERE first_name ILIKE ? OR last_name ILIKE ? ";
		SqlRowSet employeeNextRow = jdbcTemplate.queryForRowSet(employee, "%" + firstNameSearch + "%" , "%" + lastNameSearch + "%");
		while(employeeNextRow.next()) {
			Long id = employeeNextRow.getLong("employee_id");
			search.add(mapRowToEmployee(employeeNextRow));
		}
		return search;
	}

	@Override
	public List<Employee> getEmployeesByDepartmentId(long id) {
			ArrayList<Employee> theEmployee = new ArrayList<>();
			String findById = "SELECT * FROM employee WHERE department_id=?";
			SqlRowSet results = jdbcTemplate.queryForRowSet(findById, id);
			while(results.next()) {
				Employee employee = mapRowToEmployee(results);
				theEmployee.add(employee);
			}
			return theEmployee; 
		}

	@Override
	public List<Employee> getEmployeesWithoutProjects() {
		List<Employee> theEmployee = new ArrayList<>();
		String withoutProjects = "SELECT * FROM employee e LEFT JOIN project_employee pe ON pe.employee_id = e.employee_id WHERE pe.project_id IS NULL";
		SqlRowSet results = jdbcTemplate.queryForRowSet(withoutProjects);
		while(results.next()) {
			theEmployee.add(mapRowToEmployee(results));
		}
		return theEmployee;
	}
	@Override
	public List<Employee> getEmployeesByProjectId(Long projectId) {
		List<Employee> theEmployee = new ArrayList<>();
		String findById = "SELECT * FROM employee e JOIN project_employee pe ON pe.employee_id = e.employee_id WHERE pe.project_id =?";
		SqlRowSet results = jdbcTemplate.queryForRowSet(findById, projectId);
		while(results.next()) {
			//Long idS = results.getLong("project_id");
			theEmployee.add(mapRowToEmployee(results));
		}
		return theEmployee; 
	}

	@Override
	public void changeEmployeeDepartment(Long employeeId, Long departmentId) {
		
		String updateEmployee = "UPDATE employee SET department_id=? WHERE employee_id=?";
		
		jdbcTemplate.update(updateEmployee, departmentId, employeeId );
		
		
	}
	public Employee createEmployee(String firstName, String lastName, LocalDate birthDate , char gender, LocalDate hireDate) {
	
		Employee theEmployee = new Employee();
		theEmployee.setFirstName(firstName);
		theEmployee.setLastName(lastName);
		theEmployee.setBirthDay(birthDate);
		theEmployee.setGender(gender);
		theEmployee.setHireDate(hireDate);
		
		String insertEmployee = "INSERT INTO employee (first_name,last_name,birth_date,hire_date,gender) VALUES (?,?,?,?,?) RETURNING employee_id";
		theEmployee.setId(jdbcTemplate.queryForObject(insertEmployee,Long.class, firstName,lastName,birthDate,gender,hireDate));
		
		return theEmployee;
	}
}
