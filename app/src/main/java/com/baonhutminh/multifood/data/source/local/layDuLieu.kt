package com.baonhutminh.multifood.data.source.local

import BaiViet
import com.baonhutminh.multifood.R

object layDulieu{

    fun layToanBoBaiViet(): List<BaiViet> {
        return listOf(
            BaiViet(
                id = 1,
                title = "Quán CF FC",
                content = "Trải nghiệm một giờ làm việc tại cafe..bfhbsdhivbsidvbiugvidsb v abfijdbfo wwkfwjfhie wifbiwhf kjeffwgo.",
                imageResId = R.drawable.ut6,
                rating = 4.5f
            ),
            BaiViet(
                id = 2,
                title = "Cửa hàng bánh ngọt Sweet Home",
                content = "Thưởng thức bánh và không gian ấm cúng...",
                imageResId = R.drawable.ut6,
                rating = 4.8f
            ),
            BaiViet(
                id = 2,
                title = "Cửa hàng bánh ngọt Sweet Home",
                content = "Thưởng thức bánh và không gian ấm cúng...",
                imageResId = R.drawable.ut6,
                rating = 4.8f
            ),
            BaiViet(
                id = 2,
                title = "Cửa hàng bánh ngọt Sweet Home",
                content = "Thưởng thức bánh và không gian ấm cúng...",
                imageResId = R.drawable.ut6,
                rating = 4.8f
            )
        )
    }

}