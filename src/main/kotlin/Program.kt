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
suspend fun main(args: Array<String>) {
    println("Hello World")
    val db = getDBConnection()
    if(db != null){
        println("Connection Successful!!")
        println(testSQLQueries(db))
    }
    if(db!=null) {
        val list = ArrayList<Deferred<Unit>>()
        runBlocking {
            //specifically 8 because I have 32 threads, but I doubt Dispatchers.IO has that many threads in the threadpool
            for (i in 0..8) {
                launch(Dispatchers.IO)  {
                    insertBullshit("19", db)
                }
            }
            for (i in 0..8) {
                launch(Dispatchers.IO)  {
                    insertBullshit("20", db)
                }
            }
            for (i in 0..8){
                launch(Dispatchers.IO)  {
                    insertBullshit("21", db)
                }
            }
            for (i in 0..8) {
                launch(Dispatchers.IO)  {
                    insertBullshit("22", db)
                }
            }
    }

    }

}

/**
 * Gets the database connection, we are using JDBC.
 */
fun getDBConnection(): Connection?{
    val credentials = Properties()
    credentials.put("user", "root")
    credentials.put("password", "")
    try{
        var connection = DriverManager.getConnection(
            "jdbc:mysql://127.0.0.1:3306/",
            credentials)
        return connection
    }catch (ex: SQLException){
        ex.printStackTrace()
    }
    return null
}

/**
 * this was just a test to try out some JDBC functions and to practice some kotlin
 */
fun testSQLQueries(conn: Connection): String?{
    var returnstring = ""
    try {
        var sql = conn.createStatement()
        var result = sql.executeQuery("SHOW DATABASES;")
        while (result!!.next()){
            returnstring += (result.getString("Database")+"\n")
        }
        return returnstring
    }catch (ex: SQLException){
        ex.printStackTrace()
    }
    return null
}

/**
 * This is the fun part, we will insert dummy data into the database, randomly generating numbers like AdminNumber
 * I might make it more efficient by instead incrementing the Admin Number and increasing the packet size rather than being lazy and spawning heaps of coroutines/threads
 */
suspend fun insertBullshit(year:String, conn: Connection){
    //to uppercase cos im lazy as fuck
    val alphabets = "qwertyuiopasdfghjklzxcvbnm".toUpperCase()
    val rand = Random()
    val query = "INSERT IGNORE INTO `covid19db`.`users` (`AdminNumber`, `PasswordSalt`, `PasswordHash`, `Firstname`, `MiddleName`, `LastName`, `Gender`, `isAdmin`, `isCurrent`) VALUES (?,?,?,?,?,?,?,?,?);"
    val statement = conn.prepareStatement(query)
    //10000 is just under the maximum packet size of 1GB
    println(Thread.currentThread().name)
    for (i in 0..10000) {

        statement.setString(1, year + rand.nextInt(99999).toString() + alphabets[rand.nextInt(26)])
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
    //execute all the batch statements at the end
    statement.executeBatch()

}