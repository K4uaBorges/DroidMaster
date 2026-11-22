package view.theme

import androidx.compose.ui.graphics.Color

data class DroidMasterColors(

//  GENERAL APP COLORS
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val quaternary: Color,

//  USER COLORS
    val userBubbleColor: Color,
    val userTextColor: Color,

//  TIME COLORS
    val timeTextColor: Color,
    val timeBubbleColor: Color,

//  INPUT RELATED COLORS
    val bubbleColor: Color,
    val inputTextColor: Color,
    val placeholderColor: Color,
    val buttonColor: Color,
    val disabledButtonColor: Color,
    val iconColor: Color,
    val disabledIconColor: Color,
)


object DroidMasterPalettes {

    val main = DroidMasterColors(
        primary = Color(0xFFFF7A59),
        secondary = Color(0xFFFFE6DC),
        tertiary = Color(0xFFA84F35),
        quaternary = Color.White,

        userBubbleColor = Color(0xFFFF7A59),
        userTextColor = Color.White,

        timeBubbleColor = Color(0xFFFFE6DC),
        timeTextColor = Color(0xFFA84F35),

        bubbleColor = Color(0xFFFFE6DC),
        inputTextColor = Color.Black,
        placeholderColor = Color(0xFF8F7D76),
        buttonColor = Color(0xFFFF7A59),
        disabledButtonColor = Color(0xFFFFE6DC),
        iconColor = Color.White,
        disabledIconColor = Color.Black
    )

    val ocean = DroidMasterColors(
        primary = Color(0xFF1976D2),
        secondary = Color(0xFFE3F2FD),
        tertiary = Color(0xFF0D47A1),
        quaternary = Color.White,

        userBubbleColor = Color(0xFF1976D2),
        userTextColor = Color.White,

        timeBubbleColor = Color(0xFFE3F2FD),
        timeTextColor = Color(0xFF0D47A1),

        bubbleColor = Color(0xFFE3F2FD),
        inputTextColor = Color.Black,
        placeholderColor = Color(0xFF8F7D76),
        buttonColor = Color(0xFF1976D2),
        disabledButtonColor = Color.Transparent,
        iconColor = Color.White,
        disabledIconColor = Color.Black
    )

}




//    val Primary = Color(0xFFFF7A59),
//    val Secundary = Color(0xFFFFE6DC),
//    val Tertiary = Color(0xFFA84F35)
//    val Quaternary = Color.White
//
//    val UserBubbleColor = Color(0xFFFF7A59)
//    val TextColor = Color.White
//    val TimeTextColor = Color(0xFFA84F35)
//    val TimeBubbleColor = Color(0xFFFFE6DC)
//
//    val InputBubbleColor = Color(0xFFFFE6DC)
//    val InputTextColor = Color.Black
//    val InputPlaceholderColor = Color(0xFF8F7D76)
//    val SendButtonColor = Color(0xFFFF7A59)                     // Color(0xFFA84F35)
//    val SendIconColor = Color.White
//    val DisabledSendIconColor = Color.Black
//    val DisabledSendButtonColor = Color.Transparent
