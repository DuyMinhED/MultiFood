// Bạn cần import R từ package của dự án
// import com.baonhutminh.multifood.R
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.baonhutminh.multifood.R // Giả sử R nằm ở đây

// SỬA 1: Đổi tên data class sang tiếng Anh (quy ước chung) và dùng @DrawableRes
data class Article(
    val id: Int,
    val title: String,
    val content: String,
    @DrawableRes val imageResId: Int, // Dùng @DrawableRes để đảm bảo chỉ truyền vào ID tài nguyên drawable
    val rating: Float // Dùng Float cho điểm đánh giá (ví dụ: 4.5)
)

// SỬA 2: Đổi tên object và cung cấp dữ liệu mẫu hợp lệ
object SampleData {
    val article = Article(
        id = 1,
        title = "Quán Cf FC",
        content = "Trải nghiệm một giờ làm việc tại cafe...",
        // SỬA 3: Tham chiếu đến ID hình ảnh một cách chính xác
        imageResId = R.drawable.ut6, // Thay 'cafe_image' bằng tên file ảnh của bạn trong /res/drawable
        rating = 4.5f
    )
}

// SỬA 4: Đổi tên Composable để tránh trùng lặp và làm rõ chức năng
@Composable
fun ArticleCard(
    article: Article,
    onLikeClick: () -> Unit,
    onViewClick: () -> Unit,
    modifier: Modifier = Modifier // Thêm modifier làm tham số
) {
    // Dùng Card để có nền, bo góc và đổ bóng đẹp hơn
    Card(
        // SỬA 5: Dùng modifier linh hoạt
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            // Thêm padding bên trong Card
            modifier = Modifier.padding(16.dp)
        ) {
            // SỬA 6: Bỏ comment và hiển thị hình ảnh đúng cách
            Image(
                painter = painterResource(id = article.imageResId),
                contentDescription = article.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp), // Giới hạn chiều cao của ảnh
                contentScale = ContentScale.Crop // Đảm bảo ảnh lấp đầy mà không bị méo
            )

            Spacer(modifier = Modifier.height(16.dp)) // Thêm khoảng cách

            // SỬA 7: Áp dụng style cho Text để trông đẹp hơn
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = article.content,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // SỬA 8: Thêm khoảng cách giữa các nút
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp) // Tự động thêm khoảng cách 8.dp giữa các item
            ) {
                Button(
                    onClick = onLikeClick,
                    modifier = Modifier.weight(1f) // Chia đều không gian cho các nút
                ) {
                    Text(text = "Thích")
                }
                Button(
                    onClick = onViewClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Xem")
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ArticleCardPreview() {
    // SỬA 9: Bọc Preview trong Theme để thấy đúng style
    // MultiFoodTheme {
    ArticleCard(
        article = SampleData.article,
        onLikeClick = {},
        onViewClick = {}
    )
    // }
}