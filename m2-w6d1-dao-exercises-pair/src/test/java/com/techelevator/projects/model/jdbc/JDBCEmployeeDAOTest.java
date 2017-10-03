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
		dao = new JDBCEmployeeDAO(dataSource);//fresh new dao before every test
		jdbcTemplate.execute("INSERT INTO employee (last_name, first_name, birth_date, gender, hire_date) VALUES ('Wayne','John','1919-04-06','M','1945-07-04') ");
		tempId=jdbcTemplate.queryForObject("INSERT INTO employee (last_name, first_name, birth_date, gender, hire_date) VALUES ('Wayne','John','1919-04-06','M','1945-07-04') RETURNING employee_id" ,Long.class);
		jdbcTemplate.update("INSERT INTO project_employee (project_id,employee_id) VALUES (1,?)",tempId);
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
		assertEquals(0,employees.get(0).getDepartmentId());
	}

	@Test
	public void testGetEmployeesWithoutProjects() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetEmployeesByProjectId() {
		List<Employee> employees = dao.getAllEmployees();
		SqlRowSet getEP = jdbcTemplate.queryForObject("SELECT last_name,first_name FROM project_employee WHERE project_id=1");
		assertNotNull(employees);
		assertEquals(2,employees.size());
		assertEquals(getEP,employees.get(1));
	}

	@Test
	public void testChangeEmployeeDepartment() {
		fail("Not yet implemented");
	}
//	private Employee getEmployee(String first_name, String last_name, LocalDate birth_date , char gender, LocalDate hire_date) {
//		Employee theEmployee = new Employee();
//		theEmployee.setFirstName(first_name);
//		theEmployee.setLastName(last_name);
//		theEmployee.setBirthDay(birth_date);
//		theEmployee.setGender(gender);
//		theEmployee.setHireDate(hire_date);
//		return theEmployee;
//	}
//	private void assertEmployeesAreEqual(Employee expected, Employee actual) {
//		assertEquals(expected.getId(), actual.getId());
//		assertEquals(expected.getFirstName(), actual.getFirstName());
//		assertEquals(expected.getLastName(), actual.getLastName());
//		assertEquals(expected.getBirthDay(), actual.getBirthDay());
//		assertEquals(expected.getGender(), actual.getGender());
//		assertEquals(expected.getHireDate(), actual.getHireDate());
//	}
}
