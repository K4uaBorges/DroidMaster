package storage.mongoDB

import errors.MongoClientException

class MongoStorage<Key, Data>(
    collectionName: String,
    driver: MongoDriver,
    val serializer: Serializer<Data>,
) : Storage<Key,Data> {

    data class Doc(val id: String, val data: String)

    val docs = driver.getCollection<Doc>(collectionName)

    private fun toDoc(key: Key, data: Data): Doc =
        Doc(key.toString(), serializer.serialize(data))

    override fun create(k: Key, data: Data) {
        try {
            docs.insertDocument<Doc>(toDoc(k, data))
        } catch (e: MongoClientException) {
            throw MongoClientException("Document $k already exists")
        }
    }

    override fun read(k: Key): Data? =
        docs.getDocument(k.toString())?.let {
            serializer.deserialize(it.data)
        }


    override fun update(k: Key, data: Data) {
        try {
            docs.replaceDocument(k.toString(), toDoc(k, data))
        } catch (e: MongoClientException) {
            throw MongoClientException("Document $k does not exist to update")
        }
    }

    override fun delete(k: Key) {
        try {
            docs.deleteDocument(k.toString())
        } catch (e: MongoClientException) {
            throw MongoClientException("Document $k does not exist to update")
        }
    }
}