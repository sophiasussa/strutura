package com.example.application.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.SQLException;

import com.example.application.model.UnidMedida;
public class UnidMedidaRepository {

    private static final Logger logger = LoggerFactory.getLogger(UnidMedidaRepository.class);
    private Connection connection;

    public UnidMedidaRepository() throws SQLException {
        this.connection = DBConnection.getInstance().getConnection();
    }

    public boolean inserir(UnidMedida unidMedida) {
        String sql = "INSERT INTO unidMedida (nome) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, unidMedida.getNome());
            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                logger.info("Unidade de Medida inserida com sucesso: " + unidMedida.getNome());
                return true;
            } else {
                logger.warn("Nenhuma linha inserida para a Unidade de Medida: " + unidMedida.getNome());
                return false;
            }
        } catch (SQLException e) {
            logger.error("Erro ao inserir Unidade de Medida: " + unidMedida.getNome(), e);
            throw new RuntimeException("Erro ao processar a solicitação. Tente novamente.", e);
        } catch (Exception e) {
            logger.error("Erro inesperado ao inserir Unidade de Medida: " + unidMedida.getNome(), e);
            throw new RuntimeException("Erro inesperado ao processar a solicitação. Tente novamente.", e);
        }
    }

    public boolean alterar(UnidMedida unidMedida) {
        String sql = "UPDATE unidMedida SET nome = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, unidMedida.getNome());
            stmt.setInt(2, unidMedida.getId());
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                logger.info("Unidade de Medida atualizada com sucesso: " + unidMedida.getNome());
                return true;
            } else {
                logger.warn("Nenhuma linha atualizada para a Unidade de Medida com ID: " + unidMedida.getId());
                return false;
            }
        } catch (SQLException e) {
            logger.error("Erro ao alterar Unidade de Medida com ID: " + unidMedida.getId(), e);
            throw new RuntimeException("Erro ao processar a solicitação. Tente novamente.", e);
        } catch (Exception e) {
            logger.error("Erro inesperado ao alterar Unidade de Medida com ID: " + unidMedida.getId(), e);
            throw new RuntimeException("Erro inesperado ao processar a solicitação. Tente novamente.", e);
        }
    }

    public boolean excluir(UnidMedida unidMedida) {
        String sql = "DELETE FROM unidMedida WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, unidMedida.getId());
            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                logger.info("Unidade de Medida excluída com sucesso: " + unidMedida.getId());
                return true;
            } else {
                logger.warn("Nenhuma linha excluída para a Unidade de Medida com ID: " + unidMedida.getId());
                return false;
            }
        } catch (SQLException e) {
            logger.error("Erro ao excluir Unidade de Medida com ID: " + unidMedida.getId(), e);
            throw new RuntimeException("Erro ao processar a solicitação. Tente novamente.", e);
        } catch (Exception e) {
            logger.error("Erro inesperado ao excluir Unidade de Medida com ID: " + unidMedida.getId(), e);
            throw new RuntimeException("Erro inesperado ao processar a solicitação. Tente novamente.", e);
        }
    }

    public List<UnidMedida> pesquisarTodos() {
        List<UnidMedida> lista = new ArrayList<>();
        String sql = "SELECT * FROM unidMedida";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet resultSet = stmt.executeQuery()) {

            while (resultSet.next()) {
                UnidMedida unidMedida = new UnidMedida();
                unidMedida.setId(resultSet.getInt("id"));
                unidMedida.setNome(resultSet.getString("nome"));
                lista.add(unidMedida);
            }
            logger.info("Pesquisadas " + lista.size() + " unidades de medida.");
        } catch (SQLException e) {
            logger.error("Erro ao pesquisar todas as unidades de medida.", e);
            throw new RuntimeException("Erro ao processar a solicitação. Tente novamente.", e);
        } catch (Exception e) {
            logger.error("Erro inesperado ao pesquisar todas as unidades de medida.", e);
            throw new RuntimeException("Erro inesperado ao processar a solicitação. Tente novamente.", e);
        }
        return lista;
    }
}