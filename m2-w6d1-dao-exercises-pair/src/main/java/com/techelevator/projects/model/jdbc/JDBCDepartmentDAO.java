package com.techelevator.projects.model.jdbc;

import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import com.techelevator.projects.model.Department;
import com.techelevator.projects.model.DepartmentDAO;

public class JDBCDepartmentDAO implements DepartmentDAO {
	
	private JdbcTemplate jdbcTemplate;

	public JDBCDepartmentDAO(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public List<Department> getAllDepartments() {
		List<Department> departments = new ArrayList<>();
		String department = "SELECT department_id,name FROM department";
		SqlRowSet departmentNextRow = jdbcTemplate.queryForRowSet(department);
		while(departmentNextRow.next()) {
			Long id = departmentNextRow.getLong("department_id");
			departments.add(mapRowToDepartment(departmentNextRow));
		}
		return departments;
	}
		
	

	private Department mapRowToDepartment(SqlRowSet departmentNextRow) {
		Department theDepartment;
		theDepartment = new Department();
		theDepartment.setId(departmentNextRow.getLong("department_id"));
		theDepartment.setName(departmentNextRow.getString("name"));
		
		return theDepartment;
	}

	@Override
	public List<Department> searchDepartmentsByName(String nameSearch) {
		List<Department> search = new ArrayList <>();
		String department = "SELECT * FROM department WHERE name ILIKE ? ";
		SqlRowSet departmentNextRow = jdbcTemplate.queryForRowSet(department, "%" + nameSearch + "%");
		while(departmentNextRow.next()) {
			Long id = departmentNextRow.getLong("department_id");
			search.add(mapRowToDepartment(departmentNextRow));
		}
		return search;
	}

	@Override
	public void updateDepartmentName(Long departmentId, String departmentName) {
		
		String updateDepartment = "UPDATE department SET name=? WHERE department_id=?";
		
		jdbcTemplate.update(updateDepartment, departmentName, departmentId );
		
		
	}

	@Override
	public Department createDepartment(String departmentName) {
		
		String insertDepartment = "INSERT INTO department (name) VALUES (?) RETURNING department_id";
		Long departmentId = jdbcTemplate.queryForObject(insertDepartment,Long.class, departmentName);
		
		return getDepartmentById(departmentId);
	}

	@Override
	public Department getDepartmentById(Long id) {
		
		Department theDepartment = null;
		String findById = "SELECT id" + "FROM department" + "WHERE id=?";
		SqlRowSet results = jdbcTemplate.queryForRowSet(findById, id);
		if( results.next()) {
			theDepartment = mapRowToDepartment(results);
		}
		
		return theDepartment;
	}

}
