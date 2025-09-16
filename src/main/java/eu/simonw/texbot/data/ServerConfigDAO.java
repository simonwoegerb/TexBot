package eu.simonw.texbot.data;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@SuppressWarnings("unused")
@RegisterBeanMapper(ServerConfig.class)
public interface ServerConfigDAO {

    @SqlUpdate("""
                CREATE TABLE IF NOT EXISTS server_configs (
                    uuid BIGINT NOT NULL UNIQUE,
                    read_messages BOOLEAN NOT NULL
                )
            """)
    void createTable();

    @SqlUpdate("""
                INSERT INTO server_configs (uuid, read_messages)
                VALUES (:uuid, :readMessages)
            """)
    void createServer(@BindBean ServerConfig serverConfig);

    @SqlQuery("SELECT * FROM server_configs WHERE uuid = :uuid")
    ServerConfig getServer(@Bind("uuid") long uuid);

    @SqlUpdate("""
                UPDATE server_configs
                SET read_messages = :readMessages
                WHERE uuid = :uuid
            """)
    void update(@BindBean ServerConfig serverConfig);
}

