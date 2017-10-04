package com.techelevator.projects.model.jdbc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.techelevator.projects.model.Employee;
import com.techelevator.projects.model.Project;
import com.techelevator.projects.model.ProjectDAO;

public class JDBCProjectDAO implements ProjectDAO {

	private JdbcTemplate jdbcTemplate;

	public JDBCProjectDAO(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	@Override
	public List<Project> getAllActiveProjects() {
		List<Project> projects = new ArrayList<>();
		String project = "SELECT * FROM project WHERE to_date > current_date OR (from_date IS NOT NULL AND to_date IS NULL)";
		SqlRowSet projectNextRow = jdbcTemplate.queryForRowSet(project);
		while(projectNextRow.next()) {
			Long id = projectNextRow.getLong("project_id");
			projects.add(mapRowToProject(projectNextRow));
		}
		return projects;
	}
	
	
	private Project mapRowToProject(SqlRowSet projectNextRow) {  
		Project theProject;
		theProject = new Project();
		theProject.setId(projectNextRow.getLong("project_id"));
		theProject.setName(projectNextRow.getString("name"));
		//theProject.setStartDate(projectNextRow.getDate("from_date").toLocalDate());
		//theProject.setEndDate(projectNextRow.getDate("to_date").toLocalDate());
		return theProject;
	}


	@Override
	public void removeEmployeeFromProject(Long projectId, Long employeeId) {			
			String removeEmployee = "DELETE FROM project_employee WHERE project_id =? AND employee_id =? "  ;			
			jdbcTemplate.update(removeEmployee, projectId, employeeId);			
		}
	@Override
	public void addEmployeeToProject(Long projectId, Long employeeId) {		
		String addEmployee = "INSERT INTO project_employee  (project_id,employee_id) Values (?,?)";	
		jdbcTemplate.update(addEmployee, projectId, employeeId);			
	}
	@Override
	public Project createProject(String name) {
		
		Project theProject = new Project();
		theProject.setName(name);	
		String insertProject = "INSERT INTO project (name) VALUES (?) RETURNING project_id";
		theProject.setId(jdbcTemplate.queryForObject(insertProject,Long.class, name));
		
		return theProject;
	}
	@Override
	public List<Employee> getAllEmployeesForProject (Long projectId){
		List<Employee> theEmployee = new ArrayList<>();
		String findById = "SELECT * FROM employee e JOIN project_employee pe ON pe.employee_id = e.employee_id WHERE pe.project_id =?";
		SqlRowSet results = jdbcTemplate.queryForRowSet(findById, projectId);
		while(results.next()) {
			//Long idS = results.getLong("project_id");
			theEmployee.add(mapRowToEmployee(results));
		}
		return theEmployee; 
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
}
