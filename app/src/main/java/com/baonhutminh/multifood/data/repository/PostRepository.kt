package com.baonhutminh.multifood.data.repository

import com.baonhutminh.multifood.data.model.Post
import kotlinx.coroutines.delay

class PostRepository {

    suspend fun getArtilce():List<Post>{

        delay(3000)

        return listOf(
            Post(
                id = "1",
                title = "Quán Bún Bò Huế O Loan",
                content = "Nước lèo đậm đà, thịt nhiều.",
                images = listOf(
                    "https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.tripadvisor.com%2FRestaurant_Review-g293925-d10167203-Reviews-Cong_Cafe-Ho_Chi_Minh_City.html&psig=AOvVaw2As-dyNt4m_GnY-R6A6swj&ust=1762060503886000&source=images&cd=vfe&opi=89978449&ved=0CBUQjRxqFwoTCMCzooaZ0JADFQAAAAAdAAAAABAE",
                    "https://example.com/image2.jpg"
                ),
                rating = 4.5f,
                address = "123 Nguyễn Văn Cừ, Q.5, TP.HCM",
                comments = listOf("Ngon quá!", "Sẽ quay lại"),
                date = "2025-10-30",
                author = "Nguyễn Minh"
            ),
            Post(
                id = "2",
                title = "Cà phê Nâu Đá Corner",
                content = "Không gian yên tĩnh, giá rẻ.",
                images = listOf("https://example.com/cf1.jpg"),
                rating = 4.2f,
                address = "45 Pasteur, Q.1, TP.HCM",
                comments = listOf("Phục vụ dễ thương!"),
                date = "2025-11-01",
                author = "Trần Anh"
            )
        )

    }


    suspend fun getArticleById(): Post {
        return Post(
            id = "2",
            title = "Cà phê Nâu Đá Corner",
            content = "Không gian yên tĩnh, giá rẻ.",
            images = listOf("https://example.com/cf1.jpg"),
            rating = 4.2f,
            address = "45 Pasteur, Q.1, TP.HCM",
            comments = listOf("Phục vụ dễ thương!"),
            date = "2025-11-01",
            author = "Trần Anh"
        )
    }

}