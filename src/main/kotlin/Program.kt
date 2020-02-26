import kotlinx.coroutines.*
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.coroutineContext

/**
 * Main function, self explanatory.
 */

object Program {
    @JvmStatic
    fun main(args: Array<String>) {
        println("Hello World")
        val db = getDBConnection()
        if (db != null) {
            println("Connection Successful!!")
            println(testSQLQueries(db))
        }
        if (db != null) {
            val list = ArrayList<Deferred<Unit>>()
            val nproc = Runtime.getRuntime().availableProcessors()
            runBlocking {
                //spawn i number of threads to insert bullshit into the database
                //more threads = more data.
                for (i in 0..nproc) {
                        Thread {
                            //16 as we start from year 16
                            insertBullshit((i + 4) + 16, db)
                        }.start()
                    }
                }
            }

        }

    /**
     * Gets the database connection, we are using JDBC.
     */
    fun getDBConnection(): Connection? {
        val credentials = Properties()
        credentials.put("user", "root")
        credentials.put("password", "")
        try {
            var connection = DriverManager.getConnection(
                "jdbc:mysql://127.0.0.1:3306/",
                credentials
            )
            return connection
        } catch (ex: SQLException) {
            println(ex.stackTrace)
            ex.printStackTrace()
        }
        return null
    }

    /**
     * this was just a test to try out some JDBC functions and to practice some kotlin
     */
    fun testSQLQueries(conn: Connection): String? {
        var returnstring = ""
        try {
            var sql = conn.createStatement()
            var result = sql.executeQuery("SHOW DATABASES;")
            while (result!!.next()) {
                returnstring += (result.getString("Database") + "\n")
            }
            return returnstring
        } catch (ex: SQLException) {
            ex.printStackTrace()
        }
        return null
    }
    fun insertRooms(){
        val query = "INSERT INTO `covid19db`.`locations` (`RoomName`, `TierLevel`) VALUES (?,?);"
        for(level in 1..9) {
            for (room in 1..11) {

            }
        }
    }
    fun insertTravelHistory(){

    }
    /**
     * This is the fun part, we will insert dummy data into the database, randomly generating numbers like AdminNumber
     * I might make it more efficient by instead incrementing the Admin Number and increasing the packet size rather than being lazy and spawning heaps of coroutines/threads
     */
    fun insertBullshit(year: Int, conn: Connection) {
        //to uppercase cos im lazy as fuck
        val alphabets = "qwertyuiopasdfghjklzxcvbnm".toUpperCase()
        val rand = Random()
        val query =
            "INSERT IGNORE INTO `covid19db`.`users` (`AdminNumber`, `PasswordSalt`, `PasswordHash`, `Firstname`, `MiddleName`, `LastName`, `Gender`, `isAdmin`, `isCurrent`) VALUES (?,?,?,?,?,?,?,?,?);"
        //infinite loop until we're happy
        while (true) {
            val statement = conn.prepareStatement(query)
            //10000 is just under the maximum packet size of 1GB
            for (i in 0..10000) {
                statement.setString(1, rand.nextInt(999999).toString() + alphabets[rand.nextInt(26)])
                statement.setString(2, "Password")
                statement.setString(3, "Password")
                statement.setString(4, "First")
                statement.setString(5, "Middle")
                statement.setString(6, "Last")
                statement.setInt(7, rand.nextInt(2))
                statement.setInt(8, rand.nextInt(2))
                statement.setInt(9, rand.nextInt(2))
                try {
                    statement.addBatch()
                } catch (ex: SQLException) {
                    ex.printStackTrace()
                }
            }
            println("Inserting batch of 10000")
            //execute all the batch statements at the end
            statement.executeBatch()
        }
    }
}