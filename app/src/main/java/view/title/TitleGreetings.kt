package view.title

object TitleGreetings {
    val list = listOf(
        "Como posso ajudar?",
        "Em que posso ser útil?",
        "O que vamos explorar hoje?",
        "Por onde começamos?",
        "Tens alguma pergunta?",
        "Vamos aprender algo novo?",
        "O que gostarias de saber?",
        "Em que estás a pensar?",
        "Pronto para começar?",
        "O que vamos fazer hoje?",
        "Como posso facilitar o teu dia?",
        "Vamos transformar ideias em realidade?"
    )

    fun random(): String {
        return list.random()
    }
}