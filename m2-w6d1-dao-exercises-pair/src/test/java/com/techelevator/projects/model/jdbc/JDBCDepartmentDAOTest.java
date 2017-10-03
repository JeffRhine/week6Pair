package com.techelevator.projects.model.jdbc;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.ArrayList;
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
import com.techelevator.projects.model.jdbc.JDBCDepartmentDAO;
public class JDBCDepartmentDAOTest {
	
	private static SingleConnectionDataSource dataSource;
	private JDBCDepartmentDAO dao;
	private JdbcTemplate jdbcTemplate;
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
		dao = new JDBCDepartmentDAO(dataSource);//fresh new dao before every test
		
	}

	/* After each test, we rollback any changes that were made to the database so that
	 * everything is clean for the next test */
	@After
	public void rollback() throws SQLException {
		dataSource.getConnection().rollback();
	}

	@Test
	public void testGetAllDepartments() {
		String deptName="MY NEW TEST DEPT";
		String deptName2="MY NEW TEST DEPT2";
		Department newDept= dao.createDepartment(deptName);
		Department newDept2= dao.createDepartment(deptName2);
		assertNotNull(newDept);
		assertNotNull(newDept2);
		List<Department> results = dao.getAllDepartments();
		assertEquals(2, results.size());
		SqlRowSet results2 = jdbcTemplate.queryForRowSet("SELECT * FROM department");
		assertTrue("There were no departments in the DB",results2.next());
		assertEquals(deptName, results2.getString("name"));
		assertEquals(newDept.getId(), (Long)results2.getLong("department_id"));
	}
	
	

	
	
	
	@Test
	public void testSearchDepartmentsByName() {
		String deptName="MY NEW TEST DEPT";
		Department newDept= dao.createDepartment(deptName);
		assertNotNull(newDept);
		List<Department> departments = dao.searchDepartmentsByName(deptName);
		assertEquals(1,departments.size());
		assertEquals(departments.get(0).getName(), deptName);
		
	}


	@Test
	public void testUpdateDepartmentName() {
		String deptName="MY NEW TEST DEPT";
		Department newDept= dao.createDepartment(deptName);
		assertNotNull(newDept);
		String deptName2 ="MY NEW TEST DEPT2";
		dao.updateDepartmentName(newDept.getId(), deptName2);
		Department department = dao.getDepartmentById(newDept.getId());
	    assertEquals(deptName2, department.getName());

	}

	@Test
	public void testCreateDepartment() {
		String deptName="MY NEW TEST DEPT";
		Department newDept= dao.createDepartment(deptName);
		assertNotNull(newDept);
		SqlRowSet results = jdbcTemplate.queryForRowSet("SELECT * FROM department");
		assertTrue("There were no departments in the DB",results.next());
		assertEquals(deptName, results.getString("name"));
		assertEquals(newDept.getId(), (Long)results.getLong("department_id"));
		assertFalse("Too many rows", results.next());
	}

	@Test
	public void testGetDepartmentById() {
		String deptName="MY NEW TEST DEPT";
		Department newDept= dao.createDepartment(deptName);
		assertNotNull(newDept);
		List<Department> departments = dao.searchDepartmentsByName(deptName);
		assertEquals(1,departments.size());
		assertEquals(departments.get(0).getId(), newDept.getId());
		
	}

}
