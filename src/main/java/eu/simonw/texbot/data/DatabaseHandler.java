package eu.simonw.texbot.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jdk.jshell.execution.JdiDefaultExecutionControl;
import org.apache.logging.log4j.core.jmx.Server;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.sql.ConnectionBuilder;

public class DatabaseHandler {
    private Jdbi jdbi;

    public DatabaseHandler() {
    }
    public Jdbi jdbi() {
        return jdbi;
    }

    public DatabaseHandler connect(String user, String password) {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setServerNames(new String[]{"db"});
        ds.setDatabaseName("texbot");
        ds.setUser(user);
        ds.setPassword(password);

        HikariConfig hc = new HikariConfig();
        hc.setDataSource(ds);
        hc.setMaximumPoolSize(6);

         jdbi = Jdbi.create(new HikariDataSource(hc))
                 .installPlugin(new PostgresPlugin())
                 .installPlugin(new SqlObjectPlugin());
         return this;
    }
    public void setup() {
        jdbi.withExtension(ServerConfigDAO.class, dao -> {
        dao.createTable();
        return null;
    });

    }

}
