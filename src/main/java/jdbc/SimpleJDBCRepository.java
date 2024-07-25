package jdbc;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleJDBCRepository {

    private Connection connection = null;
    private PreparedStatement ps = null;
    private Statement st = null;

    private static final CustomDataSource CUSTOM_CONNECTOR = CustomDataSource.getInstance();
    private static final String createUserSQL = """
            INSERT INTO myusers (firstname, lastname, age)
            VALUES (?, ?, ?)""";
    private static final String updateUserSQL = """
            UPDATE  myusers 
            SET firstname = ?,
                lastname = ?,
                age = ?
            WHERE id = ?
            """;
    private static final String deleteUser = "DELETE FROM myusers WHERE id = ?";
    private static final String findUserByIdSQL = "SELECT * FROM myusers WHERE id = ?";
    private static final String findUserByNameSQL = "SELECT * FROM myusers WHERE firstname LIKE ?";
    private static final String findAllUserSQL = "SELECT * FROM myusers";

    public Long createUser(User user) {
        try (Connection connection = CUSTOM_CONNECTOR.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(createUserSQL,
                                                                               Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, user.getFirstName());
            preparedStatement.setString(2, user.getLastName());
            preparedStatement.setInt(3, user.getAge());
            preparedStatement.executeUpdate();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getLong("id");
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public User findUserById(Long userId) {
        try (Connection connection = CUSTOM_CONNECTOR.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(findUserByIdSQL)) {
            preparedStatement.setLong(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return User.builder()
                        .id(resultSet.getLong("id"))
                        .firstName(resultSet.getString("firstname"))
                        .lastName(resultSet.getString("lastname"))
                        .age(resultSet.getInt("age"))
                        .build();
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public User findUserByName(String userName) {
        try (Connection connection = CUSTOM_CONNECTOR.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(findUserByNameSQL)) {
            preparedStatement.setString(1, userName);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return User.builder()
                        .id(resultSet.getLong("id"))
                        .firstName(resultSet.getString("firstname"))
                        .lastName(resultSet.getString("lastname"))
                        .age(resultSet.getInt("age"))
                        .build();
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<User> findAllUser() {
        List<User> users = new ArrayList<>();
        try (Connection connection = CUSTOM_CONNECTOR.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(findAllUserSQL)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                users.add(User.builder()
                                  .id(resultSet.getLong("id"))
                                  .firstName(resultSet.getString("firstname"))
                                  .lastName(resultSet.getString("lastname"))
                                  .age(resultSet.getInt("age"))
                                  .build());
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public User updateUser(User user) {
        try (Connection connection = CUSTOM_CONNECTOR.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(updateUserSQL,
                                                                               Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, user.getFirstName());
            preparedStatement.setString(2, user.getLastName());
            preparedStatement.setInt(3, user.getAge());
            preparedStatement.setLong(4, user.getId());
            preparedStatement.executeUpdate();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                user.setId(generatedKeys.getLong("id"));
                return user;

            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteUser(Long userId) {
        try (Connection connection = CUSTOM_CONNECTOR.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(deleteUser)) {
            preparedStatement.setLong(1, userId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
