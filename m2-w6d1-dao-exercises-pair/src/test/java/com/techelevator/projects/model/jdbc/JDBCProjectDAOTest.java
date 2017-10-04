package com.techelevator.projects.model.jdbc;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import com.techelevator.projects.model.Employee;
import com.techelevator.projects.model.Project;

public class JDBCProjectDAOTest {
	private static SingleConnectionDataSource dataSource;
	private JDBCProjectDAO dao;
	private JdbcTemplate jdbcTemplate;
	private long tempId;
	private long departmentId;
	private long projectId;
	private long employeeId;
	/* Before any tests are run, this method initializes the datasource for testing. */
	@BeforeClass
	public static void setupDataSource() {
		dataSource = new SingleConnectionDataSource();
		dataSource.setUrl("jdbc:postgresql://localhost:5432/projects");
		dataSource.setUsername("postgres");
		dataSource.setPassword("postgres1");
		/* The following line disables autocommit for connections 
		 * returned by this DataSource. This allows us to rollback
		 * any changes after each test */
		dataSource.setAutoCommit(false);
	}
	
	/* After all tests have finished running, this method will close the DataSource */
	@AfterClass
	public static void closeDataSource() throws SQLException {
		dataSource.destroy();
	}

	
	
	@Before
	public void setup() {
		jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update("DELETE FROM project_employee");
		jdbcTemplate.update("DELETE FROM employee");
		jdbcTemplate.update("DELETE FROM department");
		jdbcTemplate.update("DELETE FROM project");
		departmentId = jdbcTemplate.queryForObject("INSERT INTO department (name) VALUES ('TEST DEPT') RETURNING department_id",Long.class);
		projectId = jdbcTemplate.queryForObject("INSERT INTO project (name,from_date) VALUES ('TEST PROJECT','2017-08-28') RETURNING project_id",Long.class);
		dao = new JDBCProjectDAO(dataSource);//fresh new dao before every test
		jdbcTemplate.update("INSERT INTO employee (department_id,last_name, first_name, birth_date, gender, hire_date) VALUES (?,'Wayne','Bob','1918-04-05','M','1945-07-04') ",departmentId);
		tempId=jdbcTemplate.queryForObject("INSERT INTO employee (last_name, first_name, birth_date, gender, hire_date) VALUES ('Wayne','John','1919-04-06','M','1945-07-04') RETURNING employee_id" ,Long.class);
		employeeId=jdbcTemplate.queryForObject("INSERT INTO employee (last_name, first_name, birth_date, gender, hire_date) VALUES ('Wayne','Ricky','1921-04-14','M','1945-07-04') RETURNING employee_id" ,Long.class);
		jdbcTemplate.update("INSERT INTO project_employee (project_id,employee_id) VALUES (?,?)",projectId,tempId);
	}

	/* After each test, we rollback any changes that were made to the database so that
	 * everything is clean for the next test */
	@After
	public void rollback() throws SQLException {
		dataSource.getConnection().rollback();
	}
	@Test
	public void testGetAllActiveProjects() {
		List<Project> projects = dao.getAllActiveProjects();
		assertNotNull(projects);
		assertEquals(1,projects.size());
		assertEquals("TEST PROJECT",projects.get(0).getName());
	}

	@Test
	public void testRemoveEmployeeFromProject() {
		dao.removeEmployeeFromProject(projectId, tempId);
		List<Project> projects = dao.getAllActiveProjects();
		assertNotNull(projects);
		assertEquals(1,projects.size());
		List<Employee> employee = dao.getAllEmployeesForProject(projectId);
		assertEquals(0,employee.size());
	}

	@Test
	public void testAddEmployeeToProject() {
		dao.addEmployeeToProject(projectId, employeeId);
		List<Project> projects = dao.getAllActiveProjects();
		System.out.println(projects);
		assertNotNull(projects);
		assertEquals(1,projects.size());
		List<Employee> employee = dao.getAllEmployeesForProject(projectId);
		System.out.println(employee);
		assertNotNull(employee);
		assertEquals(2,employee.size());
	}

}
