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
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ResourceDao {

    // Insert a resource category
    public int insertCategory(ResourceCategory category) {
        String sql = "INSERT INTO categorias_recurso (nombre_categoria, descripcion) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    category.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(ResourceDao.class, "insertCategory", e);
        }
        return -1;
    }

    // List all categories
    public List<ResourceCategory> findAllCategories() {
        List<ResourceCategory> list = new ArrayList<>();
        String sql = "SELECT * FROM categorias_recurso ORDER BY nombre_categoria";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ResourceCategory c = new ResourceCategory();
                c.setId(rs.getInt("id_categoria"));
                c.setName(rs.getString("nombre_categoria"));
                c.setDescription(rs.getString("descripcion"));
                list.add(c);
            }
        } catch (SQLException e) {
            ErrorLogger.log(ResourceDao.class, "findAllCategories", e);
        }
        return list;
    }

    // Find category by ID
    public ResourceCategory findCategoryById(int categoryId) {
        String sql = "SELECT * FROM categorias_recurso WHERE id_categoria = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ResourceCategory c = new ResourceCategory();
                    c.setId(rs.getInt("id_categoria"));
                    c.setName(rs.getString("nombre_categoria"));
                    c.setDescription(rs.getString("descripcion"));
                    return c;
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(ResourceDao.class, "findCategoryById", e);
        }
        return null;
    }

    // Insert a new resource item
    public int insertResource(Resource resource) {
        String sql = "INSERT INTO recursos (codigo_recurso, nombre_recurso, id_categoria, descripcion, "
                + "fecha_adquisicion, ubicacion_fisica, estado_recurso) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, resource.getCode());
            ps.setString(2, resource.getName());
            ps.setInt(3, resource.getCategoryId());
            ps.setString(4, resource.getDescription());
            if (resource.getAcquisitionDate() != null) {
                ps.setDate(5, Date.valueOf(resource.getAcquisitionDate()));
            } else {
                ps.setNull(5, Types.DATE);
            }
            ps.setString(6, resource.getPhysicalLocation());
            ps.setString(7, resource.getStatus().getDbValue());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    resource.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(ResourceDao.class, "insertResource", e);
        }
        return -1;
    }

    // Update resource status
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

    // Find resource by ID
    public Resource findById(int resourceId) {
        String sql = "SELECT r.*, cr.nombre_categoria FROM recursos r "
                + "INNER JOIN categorias_recurso cr ON r.id_categoria = cr.id_categoria "
                + "WHERE r.id_recurso = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, resourceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return this.mapResource(rs);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(ResourceDao.class, "findById", e);
        }
        return null;
    }

    // Find resource by unique inventory code
    public Resource findByCode(String code) {
        String sql = "SELECT r.*, cr.nombre_categoria FROM recursos r "
                + "INNER JOIN categorias_recurso cr ON r.id_categoria = cr.id_categoria "
                + "WHERE r.codigo_recurso = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return this.mapResource(rs);
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
        String sql = "SELECT r.*, cr.nombre_categoria FROM recursos r "
                + "INNER JOIN categorias_recurso cr ON r.id_categoria = cr.id_categoria "
                + "ORDER BY r.codigo_recurso";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(this.mapResource(rs));
            }
        } catch (SQLException e) {
            ErrorLogger.log(ResourceDao.class, "findAllResources", e);
        }
        return list;
    }

    private Resource mapResource(ResultSet rs) throws SQLException {
        Resource r = new Resource();
        r.setId(rs.getInt("id_recurso"));
        r.setCode(rs.getString("codigo_recurso"));
        r.setName(rs.getString("nombre_recurso"));
        r.setCategoryId(rs.getInt("id_categoria"));
        r.setDescription(rs.getString("descripcion"));
        Date acq = rs.getDate("fecha_adquisicion");
        if (acq != null) {
            r.setAcquisitionDate(acq.toLocalDate());
        }
        r.setPhysicalLocation(rs.getString("ubicacion_fisica"));
        r.setStatus(ResourceStatus.fromDbValue(rs.getString("estado_recurso")));
        r.setCategoryName(rs.getString("nombre_categoria"));
        return r;
    }
}
