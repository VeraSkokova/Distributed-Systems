package ru.nsu.ccfit.skokova.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;
import ru.nsu.ccfit.skokova.project.generated.Node;
import ru.nsu.ccfit.skokova.project.generated.Tag;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class OsmDao extends JdbcDaoSupport {
    @Autowired
    DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    private void init() {
        setDataSource(dataSource);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void saveNode(Node node) {
        Timestamp timestamp = new Timestamp(node.getTimestamp().toGregorianCalendar().getTimeInMillis());

        String query = "INSERT INTO node (id, lat, lon, username, uid, visible, version, changeset, time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(query, node.getId(),
                node.getLat(),
                node.getLon(),
                node.getUser(),
                node.getUid(),
                node.isVisible(),
                node.getVersion(),
                node.getChangeset(),
                timestamp);
    }

    public void saveNodes(List<Node> nodes) {
        String query = "INSERT INTO node (id, lat, lon, username, uid, visible, version, changeset, time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(query, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Node node = nodes.get(i);
                ps.setLong(1, node.getId().longValue());
                ps.setDouble(2, node.getLat());
                ps.setDouble(3, node.getLon());
                ps.setString(4, node.getUser());
                ps.setLong(5, node.getUid().longValue());
                ps.setBoolean(6, node.isVisible());
                ps.setLong(7, node.getVersion().longValue());
                ps.setLong(8, node.getChangeset().longValue());

                Timestamp timestamp = new Timestamp(node.getTimestamp().toGregorianCalendar().getTimeInMillis());
                ps.setTimestamp(9, timestamp);
            }

            @Override
            public int getBatchSize() {
                return nodes.size();
            }
        });
    }

    public void saveTag(Node node, Tag tag) {
        String query = "INSERT INTO tag (node_id, k, v) VALUES (?, ?, ?)";
        jdbcTemplate.update(query, node.getId(), tag.getK(), tag.getV());
    }

    public void saveTags(Node node, List<Tag> tags) {
        String query = "INSERT INTO tag (node_id, k, v) VALUES (?, ?, ?)";
        jdbcTemplate.batchUpdate(query, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Tag tag = tags.get(i);
                ps.setLong(1, node.getId().longValue());
                ps.setString(2, tag.getK());
                ps.setString(3, tag.getV());
            }

            @Override
            public int getBatchSize() {
                return tags.size();
            }
        });
    }

    public List<Node> getNearNodes(double lat, double lon, int distance) {
        String query = "select node.lat as lat, node.lon as lon from node " +
                "where earth_box(ll_to_earth(?, ?), ?) @> earth_box(ll_to_earth(lat, lon), 1)\n" +
                "  AND earth_distance(ll_to_earth(?, ?), ll_to_earth(lat, lon)) < ?";
        return jdbcTemplate.query(query,
                new Object[]{lat, lon, distance, lat, lon, distance},
                new RowMapperResultSetExtractor<>((rs, rowNum) -> {
                    Node node = new Node();
                    node.setLat(rs.getDouble("lat"));
                    node.setLon(rs.getDouble("lon"));
                    return node;
                }));
    }
}
