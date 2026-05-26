import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.sicemultiplatform.database.AppDatabase
import com.example.sicemultiplatform.utils.DatabaseDriverFactory
import java.io.File // Asegúrate de importar esto

class DesktopDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver {
        val userHome = System.getProperty("user.home")
        val databaseDir = File(userHome, ".sice_net")
        if (!databaseDir.exists()) {
            databaseDir.mkdirs() // Crear la carpeta si no existe
        }

        val databasePath = File(databaseDir, "sice.db").absolutePath
        val driver = JdbcSqliteDriver("jdbc:sqlite:$databasePath")

        try {
            AppDatabase.Schema.create(driver)
        } catch (e: Exception) {

        }
        return driver
    }
}