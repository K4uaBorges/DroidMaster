package storage.mongoDB

import com.mongodb.assertions.Assertions.*
import errors.MongoClientException
import org.junit.Assert.assertThrows
import org.junit.Test

class MongoDriverTest {

    class trans : Serializer<String> {
        override fun serialize(d: String): String {
            val sb = StringBuilder()
            sb.append(d)

            return sb.toString()
        }

    override fun deserialize(txt: String): String = txt
}
    val db = MongoDriver()
    val doc = MongoStorage<String, String>(
        "test",
        db,
        serializer = trans()
    )


    @Test
    fun createText() {
        doc.create("Testes", "teste, Analise")
    }

    @Test
    fun updateText(){
        doc.update("Testes", "teste, Feito")
    }

    @Test
    fun readText(){
        doc.read("Testes")
    }

    @Test
    fun deleteText(){
        doc.delete("Teste")
    }

}