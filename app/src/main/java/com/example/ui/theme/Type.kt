package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val CinzelDisplay = FontFamily(
    Font(R.font.cinzel, FontWeight.Bold)
)

val BarlowCondensedDisplay = FontFamily(
    Font(R.font.barlow_condensed, FontWeight.ExtraBold)
)

val MontserratBody = FontFamily(
    Font(R.font.montserrat, FontWeight.Medium)
)

val ZirveTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = CinzelDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = 1.sp
    ),
    titleLarge = TextStyle(
        fontFamily = BarlowCondensedDisplay,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.8.sp
    ),
    titleMedium = TextStyle(
        fontFamily = MontserratBody,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = MontserratBody,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = BarlowCondensedDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = MontserratBody,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 12.sp
    )
)
