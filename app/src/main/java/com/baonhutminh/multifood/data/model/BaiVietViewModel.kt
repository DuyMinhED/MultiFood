
import androidx.annotation.DrawableRes
import com.baonhutminh.multifood.R // Giả sử R nằm ở đây

data class BaiViet(
    val id: Int,
    val title: String,
    val content: String,
    @DrawableRes val imageResId: Int,
    val rating: Float
)

object SampleData {
    val article = BaiViet(
        id = 1,
        title = "Quán Cf FC",
        content = "Trải nghiệm một giờ làm việc tại cafe...",
        imageResId = R.drawable.ut6,
        rating = 4.5f
    )
}

