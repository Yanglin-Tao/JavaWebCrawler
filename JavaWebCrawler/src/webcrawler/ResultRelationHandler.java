package webcrawler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 
 * Handle insert keyword relations to database
 *
 */

public class ResultRelationHandler {
	private int resultId;
	private int relationId;
	private Connection connection;
	
	public ResultRelationHandler(int resultId, int relationId, Connection connection) {
		this.resultId = resultId;
		this.relationId = relationId;
		this.connection = connection;
	}
	
	private int getNextResultRelationId() {
        String sql = "SELECT MAX(id) AS max_id FROM ResultRelation";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("max_id") + 1; 
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }
	
	private boolean isResultRelationExists() {
        String sql = "SELECT COUNT(*) FROM ResultRelation WHERE result_id = ? AND relation_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, resultId);
            preparedStatement.setInt(2, relationId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
	
	public void insertResultRelation() {
		if (!isResultRelationExists()) {
	        int resultRelationId = getNextResultRelationId(); 
	        String sql = "INSERT INTO ResultRelation (id, result_id, relation_id) VALUES (?, ?, ?)";
	        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
	        	preparedStatement.setInt(1, resultRelationId);
	            preparedStatement.setInt(2, resultId);
	            preparedStatement.setInt(3, relationId);
	            preparedStatement.executeUpdate();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
		}
    }
}
