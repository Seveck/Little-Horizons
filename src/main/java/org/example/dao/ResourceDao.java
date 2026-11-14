package org.example.dao;

import org.example.model.Resource;
import org.example.model.ResourceCategory;
import org.example.model.ResourceStatus;
import org.example.util.DatabaseConnection;
import org.example.util.ErrorLogger;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ResourceDao {

    // Insert a new resource category
    public int insertCategory(ResourceCategory category) {
        String sql = "INSERT INTO categorias_recurso (nombre_categoria, descripcion) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        category.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(ResourceDao.class, "insertCategory", e);
        }
        return -1;
    }

    // List all resource categories
    public List<ResourceCategory> findAllCategories() {
        List<ResourceCategory> list = new ArrayList<>();
        String sql = "SELECT * FROM categorias_recurso ORDER BY id_categoria";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ResourceCategory rc = new ResourceCategory();
                rc.setId(rs.getInt("id_categoria"));
                rc.setName(rs.getString("nombre_categoria"));
                rc.setDescription(rs.getString("descripcion"));
                list.add(rc);
            }
        } catch (SQLException e) {
            ErrorLogger.log(ResourceDao.class, "findAllCategories", e);
        }
        return list;
    }

    // Find category by id
    public ResourceCategory findCategoryById(int categoryId) {
        String sql = "SELECT * FROM categorias_recurso WHERE id_categoria = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ResourceCategory rc = new ResourceCategory();
                    rc.setId(rs.getInt("id_categoria"));
                    rc.setName(rs.getString("nombre_categoria"));
                    rc.setDescription(rs.getString("descripcion"));
                    return rc;
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(ResourceDao.class, "findCategoryById", e);
        }
        return null;
    }

    // Insert a new resource
    public int insertResource(Resource resource) {
        String sql = "INSERT INTO recursos (codigo_unico, nombre_recurso, id_categoria, "
                + "ubicacion_fisica, estado_recurso, fecha_adquisicion) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, resource.getUniqueCode());
            ps.setString(2, resource.getName());
            ps.setInt(3, resource.getCategoryId());
            ps.setString(4, resource.getPhysicalLocation());
            ps.setString(5, resource.getStatus().getDbValue());
            ps.setDate(6, resource.getAcquisitionDate() != null ? Date.valueOf(resource.getAcquisitionDate()) : null);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        resource.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(ResourceDao.class, "insertResource", e);
        }
        return -1;
    }

    // Update resource status (Disponible, Prestado, En Mantenimiento, Dado de Baja)
    public boolean updateStatus(int resourceId, ResourceStatus status) {
        String sql = "UPDATE recursos SET estado_recurso = ? WHERE id_recurso = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.getDbValue());
            ps.setInt(2, resourceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorLogger.log(ResourceDao.class, "updateStatus", e);
            return false;
        }
    }

    // Find resource by id
    public Resource findById(int resourceId) {
        String sql = "SELECT * FROM recursos WHERE id_recurso = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, resourceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return this.mapResourceRow(rs);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(ResourceDao.class, "findById", e);
        }
        return null;
    }

    // Find resource by unique code
    public Resource findByCode(String code) {
        String sql = "SELECT * FROM recursos WHERE codigo_unico = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return this.mapResourceRow(rs);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(ResourceDao.class, "findByCode", e);
        }
        return null;
    }

    // List all resources
    public List<Resource> findAllResources() {
        List<Resource> list = new ArrayList<>();
        String sql = "SELECT * FROM recursos ORDER BY id_recurso";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(this.mapResourceRow(rs));
            }
        } catch (SQLException e) {
            ErrorLogger.log(ResourceDao.class, "findAllResources", e);
        }
        return list;
    }

    // Helper to convert ResultSet row to Resource
    private Resource mapResourceRow(ResultSet rs) throws SQLException {
        Resource r = new Resource();
        r.setId(rs.getInt("id_recurso"));
        r.setUniqueCode(rs.getString("codigo_unico"));
        r.setName(rs.getString("nombre_recurso"));
        r.setCategoryId(rs.getInt("id_categoria"));
        r.setPhysicalLocation(rs.getString("ubicacion_fisica"));
        r.setStatus(ResourceStatus.fromDbValue(rs.getString("estado_recurso")));
        Date acq = rs.getDate("fecha_adquisicion");
        if (acq != null) {
            r.setAcquisitionDate(acq.toLocalDate());
        }
        return r;
    }
}
