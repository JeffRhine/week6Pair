package com.techelevator.projects.model.jdbc;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.techelevator.projects.model.Department;
import com.techelevator.projects.model.Employee;

public class JDBCEmployeeDAOTest {
	
	private static SingleConnectionDataSource dataSource;
	private JDBCEmployeeDAO dao;
	private JdbcTemplate jdbcTemplate;
	private long tempId;
	private long departmentId;
	private long projectId;
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
		projectId = jdbcTemplate.queryForObject("INSERT INTO project (name) VALUES ('TEST PROJECT') RETURNING project_id",Long.class);
		dao = new JDBCEmployeeDAO(dataSource);//fresh new dao before every test
		jdbcTemplate.update("INSERT INTO employee (department_id,last_name, first_name, birth_date, gender, hire_date) VALUES (?,'Wayne','John','1919-04-06','M','1945-07-04') ",departmentId);
		tempId=jdbcTemplate.queryForObject("INSERT INTO employee (last_name, first_name, birth_date, gender, hire_date) VALUES ('Wayne','John','1919-04-06','M','1945-07-04') RETURNING employee_id" ,Long.class);
		jdbcTemplate.update("INSERT INTO project_employee (project_id,employee_id) VALUES (?,?)",projectId,tempId);
	}

	/* After each test, we rollback any changes that were made to the database so that
	 * everything is clean for the next test */
	@After
	public void rollback() throws SQLException {
		dataSource.getConnection().rollback();
	}
	@Test
	public void testGetAllEmployees() {
		List<Employee> employees = dao.getAllEmployees();
		assertNotNull(employees);
		assertEquals(2,employees.size());
		assertEquals("John",employees.get(0).getFirstName());
		assertEquals("Wayne",employees.get(0).getLastName());
		assertEquals('M',employees.get(0).getGender());
		assertEquals(LocalDate.parse("1919-04-06"),employees.get(0).getBirthDay());
		assertEquals(LocalDate.parse("1945-07-04"),employees.get(0).getHireDate());
	}

	@Test
	public void testSearchEmployeesByName() {
		List<Employee> employees = dao.getAllEmployees();
		assertNotNull(employees);
		assertEquals(2,employees.size());
		dao.searchEmployeesByName("John", "Wayne");
		assertEquals("JohnWayne",employees.get(0).getFirstName()+employees.get(0).getLastName());
	}

	@Test
	public void testGetEmployeesByDepartmentId() {
		List<Employee> employees = dao.getAllEmployees();
		assertNotNull(employees);
		assertEquals(2,employees.size());
		assertEquals(departmentId,employees.get(0).getDepartmentId());
	}

	@Test
	public void testGetEmployeesWithoutProjects() {
		List<Employee> employees = dao.getEmployeesWithoutProjects();
		assertNotNull(employees);
		assertEquals(1,employees.size());
		assertEquals("John",employees.get(0).getFirstName());
		assertEquals("Wayne",employees.get(0).getLastName());
	}

	@Test
	public void testGetEmployeesByProjectId() {
		List<Employee> employees = dao.getEmployeesByProjectId(projectId);
		assertNotNull(employees);
		assertEquals(1,employees.size());
		assertEquals("John",employees.get(0).getFirstName());
		assertEquals("Wayne",employees.get(0).getLastName());
	}

	@Test
	public void testChangeEmployeeDepartment() {
		 dao.changeEmployeeDepartment(tempId, departmentId);;
		 List<Employee> employees = dao.getAllEmployees();
			assertNotNull(employees);
			assertEquals(2,employees.size());
		assertEquals(departmentId,employees.get(1).getDepartmentId());
	}

}
