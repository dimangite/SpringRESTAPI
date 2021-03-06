package com.rupp.spring.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.rupp.spring.domain.DCustomer;
import com.rupp.spring.domain.ResponseList;

@Repository("customerDaoImpl")
public class CustomerDaoImpl implements CustomerDao {
    private static final Logger LOG = LoggerFactory.getLogger(CustomerDaoImpl.class);
    private final String TABLE = "customer";
    
    
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    public CustomerDaoImpl(DataSource dataSource) {
        //jdbcTemplate = new JdbcTemplate(DBCP2DataSourceUtils.getDataSource());
        jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    public int count() {
        final String sql = "select count(*) from " + TABLE;
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    @Override
    public ResponseList<DCustomer> getPage(int pagesize, String offset) {
        if (offset == null) {
            offset = "0";
        }
        final String sql = "select * from " + TABLE + "  limit " + pagesize + " OFFSET " + offset;
        //List<DCustomer> list = this.jdbcTemplate.queryForList(sql,DCustomer.class);
        List<DCustomer> list = this.jdbcTemplate.query(sql, new RowMapper<DCustomer>() {

            @Override
            public DCustomer mapRow(ResultSet rs, int paramInt) throws SQLException {
                final DCustomer domain = new DCustomer();
                domain.setId(rs.getLong("id"));
                domain.setFirstName(rs.getString("firstname"));
                domain.setLastName(rs.getString("lastname"));
                domain.setGender(rs.getString("gender"));
                domain.setEmail(rs.getString("email"));
                domain.setAddress(rs.getString("address"));
                domain.setPhone(rs.getString("phone"));
                domain.setCreatedDate(new Date(rs.getTimestamp("createdDate").getTime()));
                return domain;
            }
            
        });
        
        return populatePages(list, pagesize, String.valueOf(offset));
    }
    
    protected ResponseList<DCustomer> populatePages(final Collection<DCustomer> items, final int pageSize, final String cursorKey) {
        return populatePages(items, pageSize, cursorKey, null);
    }

    protected ResponseList<DCustomer> populatePages(final Collection<DCustomer> items, final int pageSize, final String cursorKey, final Integer totalCount) {

        if (items == null || items.isEmpty()) {
            return new ResponseList<DCustomer>(items);
        }

        int total;
        if (totalCount == null) {
            total = count();
        } else {
            total = totalCount;
        }

        if ("0".equals(cursorKey) && items.size() < pageSize) {
            total = items.size();
        }

        // limit = data.size();
        LOG.debug(" total records count : {} : Integer.parseInt(cursorKey) + items.size() : {} ", total,
                Integer.parseInt(cursorKey) + items.size());
        String nextCursorKey = null;

        if (items.size() == pageSize && Integer.parseInt(cursorKey) + items.size() < total) {
            nextCursorKey = String.format("%s", Integer.parseInt(cursorKey) + items.size());
        }

        LOG.debug("next cursorKey {}", nextCursorKey);

        final ResponseList<DCustomer> page = new ResponseList<DCustomer>(items, nextCursorKey);
        page.withTotal(total).withLimit(items.size());

        // populate previous
        if (!"0".equals(cursorKey)) {
            final int previousCursor = Math.abs(Integer.parseInt(cursorKey) - pageSize);
            page.withReverseCursor(String.format("%s", previousCursor));
        }

        return page;
    }
    /**
     * Returns list of categories from dummy database.
     * 
     * @return list of categories
     */
    public List<DCustomer> list() {
        final String sql = "select * from " + TABLE;
        //List<DCustomer> list = this.jdbcTemplate.queryForList(sql,DCustomer.class);
        List<DCustomer> list = this.jdbcTemplate.query(sql, new RowMapper<DCustomer>() {

            @Override
            public DCustomer mapRow(ResultSet rs, int paramInt) throws SQLException {
                final DCustomer domain = new DCustomer();
                domain.setId(rs.getLong("id"));
                domain.setFirstName(rs.getString("firstname"));
                domain.setLastName(rs.getString("lastname"));
                domain.setGender(rs.getString("gender"));
                domain.setEmail(rs.getString("email"));
                domain.setAddress(rs.getString("address"));
                domain.setPhone(rs.getString("phone"));
                domain.setCreatedDate(new Date(rs.getTimestamp("createdDate").getTime()));
                return domain;
            }
            
        });
        return list;
    }

    /**
     * Return dCustomer object for given id from dummy database. If dCustomer is not found for id, returns null.
     * 
     * @param id
     *            dCustomer id
     * @return dCustomer object for given id
     */
    public DCustomer get(Long id) {
        final String sql = "select * from " + TABLE + " where id = ?";
        
        try {
            //select for object
            final DCustomer obj = jdbcTemplate.queryForObject(sql, new Object[] { id }, new RowMapper<DCustomer>() {

                @Override
                public DCustomer mapRow(ResultSet rs, int paramInt) throws SQLException {
                    final DCustomer domain = new DCustomer();
                    domain.setId(rs.getLong("id"));
                    domain.setFirstName(rs.getString("firstname"));
                    domain.setLastName(rs.getString("lastname"));
                    domain.setGender(rs.getString("gender"));
                    domain.setEmail(rs.getString("email"));
                    domain.setAddress(rs.getString("address"));
                    domain.setPhone(rs.getString("phone"));
                    domain.setCreatedDate(new Date(rs.getTimestamp("createdDate").getTime()));
                    return domain;
                }
            });
            return obj;
        } catch (EmptyResultDataAccessException e) {
            System.out.println("NOT FOUND " + id + " in Table" );
            return null;
        }
    }

    /**
     * Create new dCustomer in dummy database. Updates the id and insert new dCustomer in list.
     * 
     * @param dCustomer
     *            DCustomer object
     * @return dCustomer object with updated id
     */
    public DCustomer create(DCustomer dCustomer) {
        final String sql = "INSERT INTO " + TABLE + " (firstname,lastname,gender,email,address,phone) VALUES (?,?,?,?,?,?)";
        jdbcTemplate.update(sql, new Object[] {
        		dCustomer.getFirstName(),
        		dCustomer.getLastName(),
        		dCustomer.getGender(),
        		dCustomer.getEmail(),
        		dCustomer.getAddress(),
        		dCustomer.getPhone(),
        		});
        return dCustomer;
    }

    /**
     * @param id
     *            the dCustomer id
     * @return id of deleted dCustomer object
     */
    public Long delete(Long id) {
        final String sql = "Delete from " + TABLE + " where id =?";
        int result = jdbcTemplate.update(sql, new Object[] { id });
        return result == 1 ? id : null;
    }

    /**
     * Update the dCustomer object for given id in dummy database. If dCustomer not exists, returns null
     * 
     * @param id
     * @param dCustomer
     * @return dCustomer object with id
     */
    public DCustomer update(DCustomer dCustomer) {

        final String sql = "UPDATE " + TABLE + " set firstname =? , lastname =? , gender =? , email =? , address =? , phone =? where id=?";
        int result = jdbcTemplate.update(sql, new Object[] {
        		dCustomer.getFirstName(),
        		dCustomer.getLastName(),
        		dCustomer.getGender(),
        		dCustomer.getEmail(),
        		dCustomer.getAddress(),
        		dCustomer.getPhone(),
        		dCustomer.getId()}
        );
        return result == 1 ? dCustomer : null;
    }
}
