package storage.mongoDB
import com.mongodb.ConnectionString
import com.mongodb.MongoClientException
import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.MongoClient
import com.mongodb.kotlin.client.MongoCollection
import com.mongodb.kotlin.client.MongoDatabase
import okio.Closeable

private const val ENV_CONNECTION = "DROIDMASTER"

private val envConnection = System.getenv(ENV_CONNECTION)
    ?: throw MongoClientException("Missing MONGO_CONNECTION")

class MongoDriver(nameDb: String? = null) : Closeable {
    val db: MongoDatabase
    private val client: MongoClient

    init {
        val dbName = requireNotNull(
            nameDb ?: ConnectionString(envConnection).database
        ) {
            "Database name required"
        }

        client = MongoClient.create(envConnection)
        db = client.getDatabase(dbName)
    }

    override fun close() = client.close()
}



class Collection<T : Any>(val collection: MongoCollection<T>)


inline fun <reified T : Any> MongoDriver.getCollection(id: String) =
    Collection(db.getCollection(id, T::class.java))


inline fun <reified T : Any> MongoDriver.getAllCollections() =
    db.listCollectionNames().toList().map { getCollection<T>(it) }


fun <T : Any> Collection<T>.getAllDocuments(): List<T> =
    collection.find().toList()


fun <T : Any, K> Collection<T>.getDocument(id: K): T? =
    collection.find(Filters.eq("_id", id)).firstOrNull()


fun <T : Any> Collection<T>.insertDocument(doc: T): Boolean =
    collection.insertOne(doc).insertedId != null


fun <T : Any, K> Collection<T>.replaceDocument(id: K, doc: T): Boolean =
    collection.replaceOne(Filters.eq("_id", id), doc).modifiedCount == 1L


fun <T : Any, K> Collection<T>.deleteDocument(id: K): Boolean =
    collection.deleteOne(Filters.eq("_id", id)).deletedCount == 1L


fun <T : Any> Collection<T>.deleteAllDocuments(): Boolean =
    collection.deleteMany(Filters.exists("_id")).wasAcknowledged()