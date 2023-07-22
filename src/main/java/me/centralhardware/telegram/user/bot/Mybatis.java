package me.centralhardware.telegram.user.bot;

import com.clickhouse.jdbc.ClickHouseDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.sql.SQLException;

public class Mybatis {

    public static DataSource getDataSource() throws SQLException {
        return new ClickHouseDataSource(System.getenv("CLICKHOUSE_URL"));
    }

    public static final SqlSession sqlSession;

    static {
        try {
            TransactionFactory transactionFactory = new JdbcTransactionFactory();
            Environment environment = new Environment("development", transactionFactory, getDataSource());
            var configuration = new org.apache.ibatis.session.Configuration(environment);
            configuration.addMapper(StatisticMapper.class);
            sqlSession = new SqlSessionFactoryBuilder().build(configuration).openSession();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
