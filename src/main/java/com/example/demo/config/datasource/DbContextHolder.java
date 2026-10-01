package com.example.demo.config.datasource;

public class DbContextHolder {
    private static final ThreadLocal<DatabaseType> CONTEXT = new ThreadLocal<>();

    public static void setDatabaseType(DatabaseType type) {
        CONTEXT.set(type);
    }

    public static DatabaseType getDatabaseType() {
        DatabaseType type = CONTEXT.get();
        return type != null ? type : DatabaseType.MYSQL;
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
