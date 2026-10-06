package errors

sealed class Error(val msg : String) : Throwable()

class MongoClientException(msg: String): Error(msg)