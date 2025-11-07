package com.baonhutminh.multifood.data.repository

import com.baonhutminh.multifood.data.model.Post
import kotlinx.coroutines.delay

class PostRepository {

    suspend fun getPosts():List<Post>{

        delay(3000)

        return listOf(
            Post(
                id = "1",
                title = "Quán Bún Bò Huế O Loan",
                content = "Nước lèo đậm đà, thịt nhiều.",
                images = listOf(
                    "https://images.unsplash.com/photo-1553621042-f6e147245754"
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
                images = listOf(
                    "https://images.unsplash.com/photo-1600891964599-f61ba0e24092"
                ),
                rating = 4.2f,
                address = "45 Pasteur, Q.1, TP.HCM",
                comments = listOf("Phục vụ dễ thương!"),
                date = "2025-11-01",
                author = "Trần Anh"
            ),
            Post(
                id = "1",
                title = "Quán Bún Bò Huế O Loan",
                content = "Nước lèo đậm đà, thịt nhiều.",
                images = listOf(
                    "https://images.unsplash.com/photo-1553621042-f6e147245754"
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
                images = listOf(
                    "https://images.unsplash.com/photo-1600891964599-f61ba0e24092"
                ),
                rating = 4.2f,
                address = "45 Pasteur, Q.1, TP.HCM",
                comments = listOf("Phục vụ dễ thương!"),
                date = "2025-11-01",
                author = "Trần Anh"
            ),
            Post(
                id = "1",
                title = "Quán Bún Bò Huế O Loan",
                content = "Nước lèo đậm đà, thịt nhiều.",
                images = listOf(
                    "https://images.unsplash.com/photo-1553621042-f6e147245754"
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
                images = listOf(
                    "https://images.unsplash.com/photo-1600891964599-f61ba0e24092"
                ),
                rating = 4.2f,
                address = "45 Pasteur, Q.1, TP.HCM",
                comments = listOf("Phục vụ dễ thương!"),
                date = "2025-11-01",
                author = "Trần Anh"
            )
        )
    }
}