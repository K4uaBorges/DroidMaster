package model

sealed class Shared() {
    class People : Shared()
    class File : Shared()
}